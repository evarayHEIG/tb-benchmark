import subprocess
import time
import os

COLLECTIONS = ["business", "user", "review", "checkin", "tip"]
DATA_DIR = "../../data/yelp/clean_data"
COUCHBASE_HOST = "http://localhost:8091"
CB_USER = "Administrator"
CB_PASS = "password"
BUCKET = "yelp_reviews"
SCOPE = "yelp"

primary_key = {
    "business": "%business_id%",
    "user": "%user_id%",
    "review": "%review_id%",
    "checkin": "#UUID#",
    "tip": "#UUID#"
}

def import_collection(collection):
    ndjson_path = os.path.join(DATA_DIR, f"{collection}.json")
    cbimport_cmd = [
        "cbimport", "json",
        "--format", "lines",
        "-c", COUCHBASE_HOST,
        "-u", CB_USER,
        "-p", CB_PASS,
        "-b", BUCKET,
        "--scope-collection-exp", f"{SCOPE}.{collection}",
        "-g", primary_key[collection],
        "-d", f"file://{ndjson_path}"
    ]
    print(f"Importing collection '{collection}' from {ndjson_path}…")
    try:
        result = subprocess.run(cbimport_cmd, check=True, capture_output=True, text=True)
        print(result.stdout)
        print(result.stderr)
    except subprocess.CalledProcessError as e:
        print(f"Error during cbimport for collection '{collection}' :")
        print(e.stdout)
        print(e.stderr)

if __name__ == "__main__":
    total_start = time.time()
    for collection in COLLECTIONS:
        time_start = time.time()
        import_collection(collection)
        time_elapsed = time.time() - time_start
        print(f"Import time for collection '{collection}' : {time_elapsed:.2f} seconds\n")
    total_elapsed = time.time() - total_start
    print(f"Total import time for all collections : {total_elapsed:.2f} seconds")