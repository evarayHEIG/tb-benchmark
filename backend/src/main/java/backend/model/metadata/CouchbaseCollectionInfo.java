package backend.model.metadata;

/**
 * The {@code CouchbaseCollectionInfo} class represents metadata about a Couchbase collection.
 * It includes the collection name, scope name, and the size of the collection.
 *
 * @author Eva Ray
 */
public class CouchbaseCollectionInfo {

    private final String collectionName;
    private final String scopeName;
    private final long collectionSize;

    /**
     * Constructs a new {@code CouchbaseCollectionInfo} instance with the specified collection name,
     * scope name, and collection size.
     *
     * @param collectionName the name of the Couchbase collection
     * @param scopeName      the name of the scope containing the collection
     * @param indexSize      the size of the collection in bytes
     */
    public CouchbaseCollectionInfo(String collectionName, String scopeName, long indexSize) {
        this.collectionName = collectionName;
        this.scopeName = scopeName;
        this.collectionSize = indexSize;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getScopeName() {
        return scopeName;
    }

    public long getCollectionSize() {
        return collectionSize;
    }
}
