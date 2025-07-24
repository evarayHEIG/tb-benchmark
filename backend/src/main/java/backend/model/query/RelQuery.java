package backend.model.query;

import backend.database.RelManager;
import backend.model.options.DBType;
import backend.model.options.QueryType;

/**
 * The {@code RelQuery} class represents a PostgreSQL relational query.
 * It extends the class {@code Query}.
 * This class encapsulates the query string, its type, and the relational database manager.
 *
 * @author Eva Ray
 */
public class RelQuery extends Query{

    /**
     * Constructs a new {@code RelQuery} instance with the specified query string and query type.
     *
     * @param query the PostgreSQL relational query string to be executed
     * @param qType the type of query (e.g., SELECT, INSERT, UPDATE)
     */
    public RelQuery(String query, QueryType qType) {
        // Call the superclass constructor and pass directly the PostgreSQL database type and manager
        super(DBType.POSTGRESQL, query, qType, RelManager.getInstance());
    }
}
