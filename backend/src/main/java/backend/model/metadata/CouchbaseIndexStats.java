package backend.model.metadata;

/**
 * The {@code CouchbaseIndexStats} record holds statistics related to Couchbase indexes.
 * It includes the scope name, collection name, index name, and the size of the index.
 *
 * @param scopeName       the name of the scope containing the index
 * @param collectionName  the name of the collection containing the index
 * @param indexName       the name of the index
 * @param indexSize       the size of the index in bytes
 *
 * @author Eva Ray
 */
public record CouchbaseIndexStats(String scopeName, String collectionName, String indexName, long indexSize) {

}
