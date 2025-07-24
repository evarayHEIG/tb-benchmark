import sys
import time
from datetime import timedelta
import os
from couchbase.cluster import Cluster
from couchbase.options import ClusterOptions
from couchbase.auth import PasswordAuthenticator
from couchbase.exceptions import (CouchbaseException, ScopeAlreadyExistsException,
                                  CollectionAlreadyExistsException, TimeoutException)
from couchbase.management.queries import (
    CreatePrimaryQueryIndexOptions,
    DropQueryIndexOptions,
    DropPrimaryQueryIndexOptions
)
import subprocess

# --- Connection Details ---
CB_HOST = "127.0.0.1"
CB_USERNAME = "Administrator"
CB_PASSWORD = "password"
BUCKET_NAME = "yelp_reviews"
SOURCE_SCOPE = "yelp"
TEMP_DIR = "../../data/yelp_ndjson/yelp_small_medium_ndjson"

# --- Configuration ---
DATASET_CONFIG = {
    "small": 0.33,
    "medium": 0.67
}

def execute_n1ql_query(cluster, query, **kwargs):
    """Helper function to execute a N1QL query and print status."""
    try:
        result = cluster.query(query, **kwargs)
        meta = result.metadata()
        if meta and hasattr(meta, 'metrics') and meta.metrics():
            print(f"  -> Success. Mutation count: {meta.metrics().mutation_count()}")
        else:
            print("  -> Success.")
        return result
    except TimeoutException:
        print("  -> Query timed out. This is expected for very large DELETEs.")
        print("     The operation is likely still running in the background. Continuing...")
    except CouchbaseException as e:
        print(f"  -> FAILED: {e}", file=sys.stderr)
        raise

def create_primary_index(cluster, index_name, scope, collection):
    if index_exists(cluster, index_name, scope, collection):
        drop_primary_index(cluster, scope, collection, index_name)

    start_time = time.time()
    cluster.query_indexes().create_primary_index(
        BUCKET_NAME,
        CreatePrimaryQueryIndexOptions(index_name=index_name, scope_name=scope, collection_name=collection, timeout=timedelta(minutes=60))
    )
    elapsed_time = time.time() - start_time
    print(f"Primary index '{index_name}' created in {elapsed_time:.2f} seconds on {collection} collection.")

def create_secondary_index(cluster, index_name, scope, collection, field):
    if index_exists(cluster, index_name, scope, collection):
        drop_secondary_index(cluster, index_name, scope, collection)

    start_time = time.time()
    cluster.query_indexes().create_index(
        BUCKET_NAME,
        index_name,
        [field],
        scope_name=scope,
        collection_name=collection,
        timeout=timedelta(minutes=60)
    )

    elapsed_time = time.time() - start_time
    print(f"Secondary index '{index_name}' created in {elapsed_time:.2f} seconds on {collection}.{field}.")

def index_exists(cluster, index_name, scope, collection):
    indexes = cluster.query_indexes().get_all_indexes(BUCKET_NAME,
                                                     scope_name=scope,
                                                     collection_name=collection)
    return any(idx.name == index_name for idx in indexes)

def drop_secondary_index(cluster, index_name, scope, collection):
    try:
        cluster.query_indexes().drop_index(
            BUCKET_NAME,
            index_name,
            DropQueryIndexOptions(scope_name=scope, collection_name=collection)
        )
        print(f"Old deleted secondary '{index_name}' is deleted.")
    except Exception as e:
        print(f"Cannot delete secondary index '{index_name}': {e}")

def drop_primary_index(cluster, scope, collection, index_name=None):
    try:
        cluster.query_indexes().drop_primary_index(
            BUCKET_NAME,
            DropPrimaryQueryIndexOptions(
                scope_name=scope,
                collection_name=collection,
                index_name=index_name
            )
        )
        print("Old primary index deleted.")
    except Exception as e:
        print(f"Cannot delete primary index: {e}")

def import_and_index_subset(cluster, size_name):
    """
    Imports data from existing JSON files and creates indexes.
    """
    target_scope = f"{SOURCE_SCOPE}_{size_name}"
    collections = ["business", "user", "checkin", "tip", "review"]

    print("-" * 60)
    print(f"Processing dataset size: '{size_name}'")
    print(f"Target scope: `{BUCKET_NAME}`.`{target_scope}`")
    print("-" * 60)

    bucket = cluster.bucket(BUCKET_NAME)
    collection_manager = bucket.collections()

    # 1. Create Scope and Collections (if they don't exist)
    try:
        collection_manager.create_scope(target_scope)
        print(f"Scope '{target_scope}' created.")
    except ScopeAlreadyExistsException:
        print(f"Scope '{target_scope}' already exists.")
    
    for coll_name in collections:
        try:
            collection_manager.create_collection(target_scope, coll_name)
            print(f"  Collection '{coll_name}' created in scope '{target_scope}'.")
        except CollectionAlreadyExistsException:
            print(f"  Collection '{coll_name}' already exists in scope '{target_scope}'.")

    print("\nWaiting 5 seconds for cluster metadata to propagate...")
    time.sleep(5)

    # 2. Clear out old data for a clean run
    print("\nClearing target collections...")
    for coll_name in collections:
        n1ql_coll_name = f"`{coll_name}`" if coll_name == "user" else coll_name
        query = f"DELETE FROM `{BUCKET_NAME}`.`{target_scope}`.{n1ql_coll_name};"
        execute_n1ql_query(cluster, query)

    # 3. Import business collection
    print("\nImporting business collection...")
    import_collection(target_scope, "business", os.path.join(TEMP_DIR, f"{target_scope}_business.json"))

    # Create primary and secondary indexes for the business collection
    print("\nCreating primary and secondary indexes for 'business' collection...")
    # Create primary index on business collection
    print("\nCreating primary index for 'business' collection...")
    create_primary_index(cluster, "business_primary_index", target_scope, "business")
    # Create secondary index on business_id
    print("\nCreating secondary index on 'business_id' for 'business' collection...")
    create_secondary_index(cluster, "idx_business_id", target_scope, "business", "business_id")

    # 4. Import related collections
    print("\nImporting related collections (review, checkin, tip)...")
    for coll_name in ["checkin", "review", "tip"]:
        import_collection(target_scope, coll_name, os.path.join(TEMP_DIR, f"{target_scope}_{coll_name}.json"))

        # Create primary indexes for each collection
        print(f"\nCreating primary indexes for '{coll_name}' collection...")
        create_primary_index(cluster, f"{coll_name}_primary_index", target_scope, coll_name)
        # Create secondary index on business_id for each collection
        print(f"\nCreating secondary index on 'business_id' for '{coll_name}' collection...")
        create_secondary_index(cluster, f"idx_{coll_name}_business_id", target_scope, coll_name, "business_id")

    # Create secondary index on user_id for review collection
    create_secondary_index(cluster, "idx_review_user_id", target_scope, "review", "user_id")
    # Create secondary index on user_id for tip collection
    create_secondary_index(cluster, "idx_tip_user_id", target_scope, "tip", "user_id")

    # 5. Import 'user' collection
    print("\nImporting 'user' collection...")
    import_collection(target_scope, "user", os.path.join(TEMP_DIR, f"{target_scope}_user.json"))

    # Create primary index for user collection
    print("\nCreating primary index for 'user' collection...")
    create_primary_index(cluster, "user_primary_index", target_scope, "user")
    # Create secondary index on user_id for user collection
    print("\nCreating secondary index on 'user_id' for 'user' collection...")
    create_secondary_index(cluster, "idx_user_id", target_scope, "user", "user_id")
    
    print(f"\nSuccessfully imported and indexed the '{size_name}' dataset in scope '{target_scope}'.")

def import_collection(target_scope, collection, file_path):
    """Importer une collection avec cbimport"""
    print(f"\nImporting collection {collection} in {target_scope}...")
    
    # Verify that the file exists
    if not os.path.exists(file_path):
        print(f"Error: File {file_path} does not exist. Skipping import.")
        return False
    
    cbimport_cmd = [
        "cbimport", "json",
        "--format", "lines",
        "-c", f"http://{CB_HOST}:8091",
        "-u", CB_USERNAME,
        "-p", CB_PASSWORD,
        "-b", BUCKET_NAME,
        "--scope-collection-exp", f"{target_scope}.{collection}",
        "-g", '%id%',
        "-d", f"file://{file_path}",
        "-t", "4",
        "--ignore-fields", "id",
    ]
    
    time_start = time.time()
    try:
        result = subprocess.run(cbimport_cmd, check=True, capture_output=True, text=True)
        print(result.stdout)
    except subprocess.CalledProcessError as e:
        print(f"Error running cbimport for {collection}:")
        print(e.stderr)
        return False
    
    time_elapsed = time.time() - time_start
    print(f"Time to import '{collection}': {time_elapsed:.2f} seconds")

    return True

def main():
    start_time = time.time()
    auth = PasswordAuthenticator(CB_USERNAME, CB_PASSWORD)
    options = ClusterOptions(auth, query_timeout=timedelta(minutes=60))
    
    cluster = None
    try:
        cluster = Cluster(f"couchbase://{CB_HOST}", options)
        cluster.wait_until_ready(timedelta(seconds=15))
        print("Couchbase connection successful.")

        for size_name in DATASET_CONFIG.keys():
           import_and_index_subset(cluster, size_name)

    except Exception as e:
        print(f"An error occurred: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        if cluster:
            cluster.close()
            time_elapsed = time.time() - start_time
            print("\nScript finished. Couchbase connection closed. Total time: {:.2f} seconds".format(time_elapsed))

if __name__ == "__main__":
    main()