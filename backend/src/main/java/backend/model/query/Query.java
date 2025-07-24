package backend.model.query;

import backend.database.DatabaseManager;
import backend.model.options.DBType;
import backend.model.options.QueryType;

/**
 * The {@code Query} class represents a database query with its associated properties.
 * It encapsulates the database type, the query string, the type of query, and the database manager.
 * This class serves as a base class for specific types of queries, such as Couchbase queries.
 *
 * @author Eva Ray
 */
public abstract class Query {

    private final DBType dbType;
    private final String query;
    private final QueryType qType;
    private final DatabaseManager manager;

    /**
     * Constructs a new {@code Query} instance with the specified parameters.
     *
     * @param dbType the type of database (e.g., Couchbase, PostgreSQL)
     * @param query the query string to be executed
     * @param qType the type of query (e.g., SELECT, INSERT, UPDATE)
     * @param manager the database manager responsible for executing the query
     */
    public Query(DBType dbType, String query, QueryType qType, DatabaseManager manager) {
        this.dbType = dbType;
        this.query = query;
        this.qType = qType;
        this.manager = manager;
    }

    public DBType getDbType() {
        return dbType;
    }

    public String getQuery() {
        return query;
    }

    public QueryType getQueryType() {
        return qType;
    }

    public DatabaseManager getManager() {
        return manager;
    }
}
