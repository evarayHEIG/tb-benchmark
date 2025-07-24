package backend.model.query;

import backend.database.JSONBManager;
import backend.model.options.DBType;
import backend.model.options.QueryType;

/**
 * The {@code JSONBQuery} class represents a PostgreSQL JSONB query.
 * It extends the {@code Query} class and is specifically designed for JSONB queries in PostgreSQL.
 * This class encapsulates the query string, its type, and the JSONB database manager.
 *
 * @author Eva Ray
 */
public class JSONBQuery extends Query {

    /**
     * Constructs a new {@code JSONBQuery} instance with the specified query string and query type.
     *
     * @param query the PostgreSQL JSONB query string to be executed
     * @param qType the type of query (e.g., SELECT, INSERT, UPDATE)
     */
    public JSONBQuery(String query, QueryType qType) {
        // Call the superclass constructor and pass directly the PostgreSQL JSONB database type and manager
        super(DBType.POSTGRESQL_JSONB, query, qType, JSONBManager.getInstance());
    }
}
