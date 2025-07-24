import time
import requests
from requests.auth import HTTPBasicAuth

COUCHBASE_HOST = "localhost"
USERNAME = "Administrator"
PASSWORD = "password"
BUCKET_NAME = "yelp_reviews"
BUCKET_RAM_MB = 18432
CLUSTER_RAM_MB = 18432
INDEX_RAM_MB = 6144
SCOPE = "yelp"
COLLECTIONS = ["review", "user", "business", "checkin", "tip"]

def wait_for_couchbase():
    print("Attente de Couchbase ...")
    while True:
        try:
            r = requests.get(
                f"http://{COUCHBASE_HOST}:8091/pools",
                auth=HTTPBasicAuth(USERNAME, PASSWORD)
            )
            if r.status_code == 200:
                print("Couchbase est prêt.")
                break
            else:
                print("❌ Erreur:", r.status_code, r.text)
        except requests.exceptions.ConnectionError:
            pass
        time.sleep(1)

def enable_services():
    print("Activation des services...")
    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/node/controller/setupServices",
        auth=HTTPBasicAuth(USERNAME, PASSWORD),
        data={"services": "kv,index,n1ql"}
    )
    if r.status_code not in (200, 400):
        print("❌ Erreur activation services:", r.text)
    else:
        print("✅ Services activés. reponse:", r.text)

def set_max_parallelism():
    print("Configuration du parallélisme maximum...")
    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/settings/querySettings",
        auth=HTTPBasicAuth(USERNAME, PASSWORD),
        data={"queryMaxParallelism": "1"}
    )
    if r.status_code != 200:
        print("❌ Erreur configuration queryMaxParallelism:", r.text)
    else:
        print("✅ maxParallelism défini")

def init_cluster():
    print("Initialisation du cluster...")

    # Chemin de données
    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/nodes/self/controller/settings",
        auth=HTTPBasicAuth(USERNAME, PASSWORD),
        data={"path": "/opt/couchbase/var/lib/couchbase/data"}
    )
    if r.status_code not in (200, 400):
        print("❌ Échec chemin données:", r.text)
    else:
        print("✅ Chemin de données configuré.")

    # Mémoire
    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/pools/default",
        auth=HTTPBasicAuth(USERNAME, PASSWORD),
        data={
            "memoryQuota": CLUSTER_RAM_MB,
            "indexMemoryQuota": INDEX_RAM_MB
        }
    )
    if r.status_code not in (200, 400):
        print("❌ Échec mémoire:", r.text)
    else:
        print("✅ Mémoire configurée.")

    # Utilisateur
    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/settings/web",
        data={
            "port": "8091",
            "username": USERNAME,
            "password": PASSWORD
        }
    )
    if r.status_code not in (200, 400):
        print("❌ Échec création utilisateur:", r.text)
    else:
        print("✅ Utilisateur configuré.")

def create_bucket():
    print(f"Création du bucket '{BUCKET_NAME}'...")

    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/pools/default/buckets",
        auth=HTTPBasicAuth(USERNAME, PASSWORD),
        data={
            "name": BUCKET_NAME,
            "ramQuotaMB": BUCKET_RAM_MB,
            "bucketType": "couchbase"
        }
    )
    if r.status_code == 202:
        print("✅ Bucket en cours de création.")
    elif r.status_code == 200:
        print("✅ Bucket déjà existant.")
    else:
        print("❌ Erreur création bucket:", r.text)

def set_index_storage_mode():
    print("Configuration du mode de stockage des index (plasma)...")
    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/settings/indexes",
        auth=HTTPBasicAuth(USERNAME, PASSWORD),
        data={"storageMode": "plasma"}
    )
    if r.status_code != 200:
        print("❌ Erreur configuration storageMode:", r.text)
    else:
        print("✅ storageMode défini")

def create_scope_and_collections():
    print(f"Création du scope {SCOPE} et des collections associées ...")

    # Créer le scope 
    r = requests.post(
        f"http://{COUCHBASE_HOST}:8091/pools/default/buckets/{BUCKET_NAME}/scopes",
        auth=HTTPBasicAuth(USERNAME, PASSWORD),
        data={"name": SCOPE}
    )
    if r.status_code not in (200, 400):
        print("❌ Erreur création scope:", r.text)

    for collection_name in COLLECTIONS:
        r = requests.post(
            f"http://{COUCHBASE_HOST}:8091/pools/default/buckets/{BUCKET_NAME}/scopes/{SCOPE}/collections",
            auth=HTTPBasicAuth(USERNAME, PASSWORD),
            data={"name": collection_name}
        )
        if r.status_code not in (200, 400):
            print(f"❌ Erreur création collection '{collection_name}':", r.text)
        else:
            print(f"✅ Collection '{collection_name}' créée.")


def main():
    wait_for_couchbase()
    enable_services()
    set_index_storage_mode()
    init_cluster()
    time.sleep(3)
    create_bucket()
    time.sleep(3)
    create_scope_and_collections()
    set_max_parallelism()

main()
