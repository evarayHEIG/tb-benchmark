package backend.model.query;

import backend.database.CouchbaseManager;
import backend.model.options.DBType;
import backend.model.options.QueryType;

/**
 * The {@code CBQuery} class represents a Couchbase database query.
 * It extends the {@code Query} class and is specifically designed for Couchbase queries.
 * This class encapsulates the query string, its type, and the Couchbase database manager.
 *
 * @author Eva Ray
 */
public class CBQuery extends Query {

    /**
     * Constructs a new {@code CBQuery} instance with the specified query string and query type.
     *
     * @param query the Couchbase query string to be executed
     * @param qType the type of query (e.g., SELECT, INSERT, UPDATE)
     */
    public CBQuery(String query, QueryType qType) {
        // Call the superclass constructor and pass directly the Couchbase database type and manager
        super(DBType.COUCHBASE, query, qType, CouchbaseManager.getInstance());
    }
}
