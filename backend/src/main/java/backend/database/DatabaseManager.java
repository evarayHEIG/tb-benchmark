package backend.database;

import backend.model.metadata.CacheInfo;
import backend.model.metadata.IndexInfo;
import backend.model.result.Result;
import backend.model.options.DBType;
import backend.model.query.Query;
import backend.model.request.Index;

import java.text.DecimalFormat;
import java.util.List;

/**
 * The {@code DatabaseManager} class is an abstract class that defines the contract for database management operations.
 * It provides methods for running queries, managing indexes, and retrieving database information.
 *
 * This class serves as a base for specific database managers like CouchbaseManager and RelManager,
 * which implement the actual database operations.
 *
 * @author Eva Ray
 */
public abstract class DatabaseManager {

    protected static final int WARMUP_EXECUTIONS = 2;
    protected final DecimalFormat df = new DecimalFormat("0.00");

    /**
     * Runs a query against the database.
     *
     * @param query the query to be executed
     * @param nbExecutions the number of times to execute the query
     * @param scope the scope of the query execution
     * @param indexes the list of indexes to be used for the query
     * @return a Result object containing the results of the query execution
     * @throws Exception if an error occurs during query execution
     */
    public abstract Result run(Query query, int nbExecutions, String scope, List<Index> indexes) throws Exception;

    /**
     * Retrieves the type of the database this manager is connected to.
     *
     * @return the DBType of the database
     */
    public abstract DBType getType();

    /**
     * Retrieves the initial connection time to the database.
     *
     * @return the initial connection time in milliseconds
     */
    public abstract long getInitialConnectionTime();

    /**
     * Retrieves the indexes information for a given scope.
     *
     * @param scope the scope for which to retrieve index information
     * @return a list of IndexInfo objects containing details about the indexes
     */
    public abstract List<IndexInfo> getIndexesInfo(String scope);

    /**
     * Retrieves cache information for the database.
     *
     * @param nbExecutions the number of executions to consider for cache information
     * @return a CacheInfo object containing cache hit and miss statistics
     */
    public abstract CacheInfo getCacheInfo(int nbExecutions);

    /**
     * Creates indexes in the database for a given scope.
     *
     * @param scope the scope in which to create the indexes
     * @param indexes the list of indexes to be created
     */
    public abstract void createIndexes(String scope, List<Index> indexes);

    /**
     * Drops indexes from the database for a given scope.
     *
     * @param scope the scope from which to drop the indexes
     * @param indexes the list of indexes to be dropped
     */
    public abstract void dropIndexes(String scope, List<Index> indexes);

    /**
     * Warms up the database by executing a query in a specified scope.
     *
     * @param query the query to be executed for warming up
     * @param scope the scope in which to execute the warmup query
     */
    public abstract void warmup(Query query, String scope);

    /**
     * Calculates the Transactions Per Second (TPS) based on the average latency. Here a transaction is defined as a
     * single query execution.
     *
     * @param avgLatency the average latency in milliseconds
     * @return the calculated TPS formatted to two decimal places
     */
    protected double TPS(double avgLatency) {
        double tps = 1000 / avgLatency;
        return Double.parseDouble(df.format(tps).replace(',', '.'));
    }

}
