import time
import requests
from requests.auth import HTTPBasicAuth

COUCHBASE_HOST = "localhost"
USERNAME = "Administrator"
PASSWORD = "password"
BUCKET_NAME = "yelp_reviews"
SCOPE_NAME = "yelp"

def create_index(index_name, bucket, scope, collection, field):
    print(f"Creating index '{index_name}' on {collection}.{field}...")

    url = f"http://{COUCHBASE_HOST}:8093/query"
    query = f"""
    CREATE INDEX {index_name}
    ON `{bucket}`.`{scope}`.`{collection}`({field});
    """
    data = {
        "statement": query
    }
    try:
        response = requests.post(url, auth=HTTPBasicAuth(USERNAME, PASSWORD), json=data)
        if response.status_code == 200:
            print(f"Index '{index_name}' successfuly created on {collection}.{field}.")
        else:
            print(f"Error during index creation '{index_name}':", response.text)
    except Exception as e:
        print(f"Connection error : {e}")

def create_primary_index(index_name, bucket, scope, collection):
    print(f"Creating primary index '{index_name}' on {collection} collection...")

    url = f"http://{COUCHBASE_HOST}:8093/query"
    query = f"""
    CREATE PRIMARY INDEX {index_name}
    ON `{bucket}`.`{scope}`.`{collection}`;
    """
    data = {
        "statement": query
    }
    try:
        start_time = time.time()
        response = requests.post(url, auth=HTTPBasicAuth(USERNAME, PASSWORD), json=data)

        if response.status_code == 200:
            elapsed_time = time.time() - start_time
            print(f"Primary index '{index_name}' created in {elapsed_time:.2f} seconds on {collection} collection.")
        else:
            print(f"Error during index creation '{index_name}':", response.text)
    except Exception as e:
        print(f"Connection error : {e}")

def main():
    
    time_start = time.time()
    # Primary indexes
    create_primary_index("business_primary_index", BUCKET_NAME, SCOPE_NAME, "business")
    create_primary_index("review_primary_index", BUCKET_NAME, SCOPE_NAME, "review")
    create_primary_index("user_primary_index", BUCKET_NAME, SCOPE_NAME, "user")
    create_primary_index("checkin_primary_index", BUCKET_NAME, SCOPE_NAME, "checkin")
    create_primary_index("tip_primary_index", BUCKET_NAME, SCOPE_NAME, "tip")

    # Secondary indexes
    create_index("idx_business_id", BUCKET_NAME, SCOPE_NAME, "business", "business_id")
    create_index("idx_user_id", BUCKET_NAME, SCOPE_NAME, "user", "user_id")
    create_index("idx_review_business_id", BUCKET_NAME, SCOPE_NAME, "review", "business_id")
    create_index("idx_review_user_id", BUCKET_NAME, SCOPE_NAME, "review", "user_id")
    create_index("idx_checkin_business_id", BUCKET_NAME, SCOPE_NAME, "checkin", "business_id")
    create_index("idx_tip_user_id", BUCKET_NAME, SCOPE_NAME, "tip", "user_id")
    create_index("idx_tip_business_id", BUCKET_NAME, SCOPE_NAME, "tip", "business_id")
    time_elapsed = time.time() - time_start

    print(f"Total time for index creation : {time_elapsed:.2f} seconds")
if __name__ == "__main__":
    main()
