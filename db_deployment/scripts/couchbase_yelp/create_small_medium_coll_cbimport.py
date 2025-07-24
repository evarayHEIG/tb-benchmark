import shutil
import sys
import time
from datetime import timedelta
import subprocess
import os
import json
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

# --- Connection Details ---
CB_HOST = "localhost"
CB_USERNAME = "Administrator"
CB_PASSWORD = "password"
BUCKET_NAME = "yelp_reviews"
SOURCE_SCOPE = "yelp"
TEMP_DIR = "./temp_exports"

# --- Configuration ---
DATASET_CONFIG = {
    "small": 0.33,
    #"medium": 0.67
}


def execute_n1ql_query(cluster, query, **kwargs):
    """Helper function to execute a N1QL query and print status."""
    try:
        query_preview = query.replace('\n', ' ').replace('\r', ' ').strip()
        # print(f"Executing: {query_preview}...")
        result = cluster.query(query, **kwargs)
        meta = result.metadata()
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
    BUCKET_NAME,  # bucket name seulement
    index_name,   # nom de l’index
    [field],      # liste des champs
    scope_name=scope,         # scope explicite
    collection_name=collection,  # collection explicite
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

def create_subset(cluster, size_name, percentage):
    """
    Creates a new scope and populates it with a subset of the data.
    """
    target_scope = f"{SOURCE_SCOPE}_{size_name}"
    collections = ["business", "user", "checkin", "tip", "review"]

    print("-" * 60)
    print(f"Processing dataset size: '{size_name}' ({percentage:.0%})")
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

    # 3. Populate 'business' collection using your verified, reproducible method
    print("\nExporting business ids...")    
    export_business_subset(cluster, target_scope, percentage)

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

    # 4. Populate related collections based on the new business subset
    print("\nExporting and importing related collections (review, checkin, tip)...")
    for coll_name in ["checkin", "review", "tip"]:
        
        export_filtered_collection_on_business(cluster, coll_name, target_scope)
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

    # 5. Populate 'user' collection based on the users present in the new reviews
    print("\nExporting and importing 'user' collection...")
    export_filtered_collection_on_user_test(cluster, "user", target_scope)
    import_collection(target_scope, "user", os.path.join(TEMP_DIR, f"{target_scope}_user.json"))

    # Create primary index for user collection
    print("\nCreating primary index for 'user' collection...")
    create_primary_index(cluster, "user_primary_index", target_scope, "user")
    # Create secondary index on user_id for user collection
    print("\nCreating secondary index on 'user_id' for 'user' collection...")
    create_secondary_index(cluster, "idx_user_id", target_scope, "user", "user_id")
    
    print(f"\nSuccessfully created the '{size_name}' dataset in scope '{target_scope}'.")

def ensure_directories():
    """Créer le répertoire temporaire s'il n'existe pas"""
    if not os.path.exists(TEMP_DIR):
        os.makedirs(TEMP_DIR)

def delete_temp_directory():
    """Supprimer le répertoire temporaire s'il existe"""
    if os.path.exists(TEMP_DIR):
        for filename in os.listdir(TEMP_DIR):
            file_path = os.path.join(TEMP_DIR, filename)
            try:
                if os.path.isfile(file_path) or os.path.islink(file_path):
                    os.unlink(file_path)
                elif os.path.isdir(file_path):
                    shutil.rmtree(file_path)
            except Exception as e:
                print(f"Error while deleting {file_path}: {e}")
        os.rmdir(TEMP_DIR)

def export_business_subset(cluster, target_scope, percentage):
    """Exporter un sous-ensemble de business"""
    print(f"\nExport of a subset of {percentage:.0%} from business...")
    export_file = os.path.join(TEMP_DIR, f"{target_scope}_business.json")
    
    # Requête pour extraire un sous-ensemble de business
    query = f"""
    SELECT META(b).id AS id, b.*
    FROM `{BUCKET_NAME}`.`{SOURCE_SCOPE}`.business AS b
    ORDER BY HASHBYTES(b.business_id)
    LIMIT (
        SELECT RAW ROUND(COUNT(*) * {percentage})
        FROM `{BUCKET_NAME}`.`{SOURCE_SCOPE}`.business
    )[0];
    """
    
    result = execute_n1ql_query(cluster, query)
    
    # Écrire les résultats dans un fichier ndjson
    with open(export_file, 'w') as f:
        for row in result:
            f.write(json.dumps(row) + '\n')
    
    print(f"Sous-ensemble de business exporté dans {export_file}")

def export_filtered_collection_on_business(cluster, collection, target_scope):
    """Exporter une collection filtrée par business_ids ou user_ids"""
    print(f"\nExporting the filtered collection {collection}...")
    export_file = os.path.join(TEMP_DIR, f"{target_scope}_{collection}.json")
    
    query = f"""
    SELECT META(t).id AS id, t.*
    FROM `{BUCKET_NAME}`.`{target_scope}`.business AS b
    JOIN `{BUCKET_NAME}`.`{SOURCE_SCOPE}`.{collection} AS t 
    ON t.business_id = b.business_id;
    """

    result = execute_n1ql_query(cluster, query)
    
    with open(export_file, 'w') as f:
        for row in result:
            f.write(json.dumps(row) + '\n')
    
    print(f"{collection} documents exported in {export_file}")

def export_filtered_collection_on_user(cluster, collection, target_scope):
    """Exporter une collection filtrée par business_ids ou user_ids"""
    print(f"\nExporting the filtered collection {collection}...")
    export_file = os.path.join(TEMP_DIR, f"{target_scope}_{collection}.json")
    
    query = f"""
    SELECT DISTINCT META(u).id AS id, u.*
    FROM `{BUCKET_NAME}`.`{target_scope}`.review AS r
    JOIN `{BUCKET_NAME}`.`{SOURCE_SCOPE}`.`user` AS u
    ON r.user_id = u.user_id
    """

    result = execute_n1ql_query(cluster, query)
    
    with open(export_file, 'w') as f:
        for row in result:
            f.write(json.dumps(row) + '\n')
    
    print(f"{collection} documents exported in {export_file}")

def export_filtered_collection_on_user_test(cluster, collection, target_scope):
    """Exporter une collection filtrée par business_ids ou user_ids en 2 étapes pour améliorer les performances"""
    print(f"\nExporting the filtered collection {collection}...")
    export_file = os.path.join(TEMP_DIR, f"{target_scope}_{collection}.json")
    
    # Étape 1: Récupérer la liste des user_ids uniques
    print("  Step 1: Extracting unique user_ids...")
    user_ids_query = f"""
    SELECT DISTINCT r.user_id
    FROM `{BUCKET_NAME}`.`{target_scope}`.review AS r
    """
    
    start_time = time.time()
    user_ids_result = execute_n1ql_query(cluster, user_ids_query)
    
    # Extraire les user_ids dans une liste
    user_ids = []
    for row in user_ids_result:
        if row.get('user_id'):
            user_ids.append(row['user_id'])
    
    step1_time = time.time() - start_time
    print(f"  → {len(user_ids)} unique user_ids extracted in {step1_time:.2f} seconds")
    
    # Étape 2: Récupérer les documents utilisateurs correspondants par lots
    print("  Step 2: Retrieve user documents...")
    
    # Utiliser un fichier temporaire pour écrire les résultats au fur et à mesure
    start_time = time.time()
    
    # Taille de lot pour les requêtes
    batch_size = 10000
    total_users = 0
    
    with open(export_file, 'w') as f:
        # Traiter les user_ids par lots pour éviter des requêtes trop grandes
        for i in range(0, len(user_ids), batch_size):
            batch = user_ids[i:i+batch_size]
            
            # Construire la partie IN de la requête
            user_ids_list = ", ".join([f"'{uid}'" for uid in batch])
            
            # Requête pour récupérer les utilisateurs du lot
            users_query = f"""
            SELECT META(u).id AS id, u.*
            FROM `{BUCKET_NAME}`.`{SOURCE_SCOPE}`.`user` AS u
            WHERE u.user_id IN [{user_ids_list}]
            """
            
            users_result = execute_n1ql_query(cluster, users_query)
            
            # Écrire les résultats dans le fichier
            batch_count = 0
            for row in users_result:
                f.write(json.dumps(row) + '\n')
                batch_count += 1
            
            total_users += batch_count
    
    step2_time = time.time() - start_time
    print(f"{collection}: {total_users} documents exported to {export_file} in {step2_time:.2f} seconds")
    print(f"Total time: {step1_time + step2_time:.2f} seconds")

def import_collection(target_scope, collection, file_path):
    """Importer une collection avec cbimport"""
    print(f"\nImportating collection {collection} in {target_scope}...")
    
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
        print(f"Error during cbimport execution for {collection}:")
        print(e.stderr)
        return False
    
    time_elapsed = time.time() - time_start
    print(f"Time to import '{collection}' : {time_elapsed:.2f} seconds")

    return True

def main():

    ensure_directories()

    start_time = time.time()
    auth = PasswordAuthenticator(CB_USERNAME, CB_PASSWORD)
    options = ClusterOptions(auth, query_timeout=timedelta(minutes=60))
    
    cluster = None
    try:
        cluster = Cluster(f"couchbase://{CB_HOST}", options)
        cluster.wait_until_ready(timedelta(seconds=15))
        print("Couchbase connection successful.")

        for size_name, percentage in DATASET_CONFIG.items():
           create_subset(cluster, size_name, percentage)

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