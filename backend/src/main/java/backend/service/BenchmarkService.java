package backend.service;

import backend.database.CouchbaseManager;
import backend.database.DatabaseManager;
import backend.database.JSONBManager;
import backend.database.RelManager;
import backend.model.metadata.IndexInfo;
import backend.model.options.DBSize;
import backend.model.options.DBType;
import backend.model.options.QueryType;
import backend.model.query.Query;
import backend.model.query.QueryFactory;
import backend.model.request.*;
import backend.model.result.Result;
import backend.model.result.ResultWithIndex;
import backend.model.result.WorkloadResult;
import backend.model.workload.Workload;
import backend.model.workload.WorkloadFactory;
import backend.model.workload.WorkloadQuery;
import backend.service.logging.BenchmarkLogger;
import backend.service.logging.ConsoleLogger;

import java.util.*;

/**
 * {@code BenchmarkService} is responsible for executing benchmarks across multiple
 * database types (Couchbase, PostgreSQL, PostgreSQL JSONB).
 * <p>
 * It supports executing both single query benchmarks and complex workloads,
 * either predefined or custom.
 * <p>
 * Index creation and cleanup are handled automatically, and results include
 * both performance metrics and index metadata.
 *
 * @author Eva Ray
 */
public class BenchmarkService {

    // Factories for creating queries and workloads. They are attributes to avoid calling static methods repeatedly.
    private final QueryFactory queryFactory;
    private final WorkloadFactory workloadFactory;
    // Logger for outputting benchmark results and errors.
    private final BenchmarkLogger logger;

    /**
     * Constructs a {@code BenchmarkService} with a default console logger.
     */
    public BenchmarkService() {
        this(new ConsoleLogger());
    }

    /**
     * Constructs a {@code BenchmarkService} with custom factories and logger.
     *
     * @param queryFactory    the factory for creating queries
     * @param workloadFactory the factory for creating workloads
     * @param logger          the benchmark logger
     */
    public BenchmarkService(QueryFactory queryFactory, WorkloadFactory workloadFactory, BenchmarkLogger logger) {
        this.queryFactory = queryFactory;
        this.workloadFactory = workloadFactory;
        this.logger = logger;
    }

    /**
     * Constructs a {@code BenchmarkService} with a specified logger and default factories. Allows for injection of a
     * custom logger.
     *
     * @param logger the benchmark logger
     */
    public BenchmarkService(BenchmarkLogger logger) {
        this(QueryFactory.getInstance(), WorkloadFactory.getInstance(), logger);
    }

    /**
     * Returns the list of database managers corresponding to the selected database types.
     *
     * @param dbTypes the list of selected database types
     * @return a list of corresponding database managers
     */
    private List<DatabaseManager> getManagers(List<DBType> dbTypes) {
        List<DatabaseManager> managers = new ArrayList<>();
        for (DBType dbType : dbTypes) {
            switch (dbType) {
                case COUCHBASE -> managers.add(CouchbaseManager.getInstance());
                case POSTGRESQL -> managers.add(RelManager.getInstance());
                case POSTGRESQL_JSONB -> managers.add(JSONBManager.getInstance());
            }
        }
        return managers;
    }

    /**
     * Returns the scope for a given database type and size. Scope is the terminology used in Couchbase, schema is
     * the terminology used in PostgreSQL.
     *
     * @param dbType the database type
     * @param dbSize the selected size
     * @return the scope name
     */
    private String getScope(DBType dbType, DBSize dbSize) {
        return switch (dbType) {
            case COUCHBASE -> switch (dbSize) {
                case SMALL -> "yelp_small";
                case MEDIUM -> "yelp_medium";
                case LARGE -> "yelp";
            };
            case POSTGRESQL, POSTGRESQL_JSONB -> switch (dbSize) {
                case SMALL -> "yelp_small";
                case MEDIUM -> "yelp_medium";
                case LARGE -> "public";
            };
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    /**
     * Returns the list of indexes for a given database type.
     *
     * @param indexes the map of indexes
     * @param dbType  the target database type
     * @return the list of indexes for that type
     */
    private List<Index> getIndexes(Map<DBType, List<Index>> indexes, DBType dbType) {
        return indexes != null ? indexes.getOrDefault(dbType, Collections.emptyList()) : Collections.emptyList();
    }

    /**
     * A functional interface for running a benchmark task.
     * It takes a scope as input and returns a result of type T.
     *
     * @param <T> the type of the result
     */
    @FunctionalInterface
    private interface BenchmarkTask<T> {

        /**
         * Runs the benchmark task within the specified scope.
         *
         * @param scope the scope in which to run the task
         * @return the result of the task
         * @throws Exception if an error occurs during execution
         */
        T run(String scope) throws Exception;
    }

    /**
     * A record to hold the result of a benchmark task along with index information.
     *
     * @param <T> the type of the task result
     */
    private record BenchmarkResult<T>(T taskResult, List<IndexInfo> indexInfo) {
    }

    /**
     * Executes a benchmark task for a given database manager, including index creation
     * and cleanup.
     *
     * @param manager   the database manager
     * @param dbSize    the selected database size
     * @param indexList the list of indexes to apply
     * @param task      the task to execute
     * @param <T>       the type of result produced
     * @return an optional result containing the task output and index metadata
     */
    private <T> Optional<BenchmarkResult<T>> executeBenchmarkForManager(DatabaseManager manager, DBSize dbSize,
                                                                        List<Index> indexList, BenchmarkTask<T> task) {

        logger.logSubHeader("Running benchmark for " + manager.getType().getName());
        String scope = getScope(manager.getType(), dbSize);

        try {
            // Create indexes if provided
            manager.createIndexes(scope, indexList);
            // Run the benchmark task
            T taskResult = task.run(scope);
            // Retrieve index information after the task execution
            List<IndexInfo> indexInfo = manager.getIndexesInfo(scope);
            return Optional.of(new BenchmarkResult<>(taskResult, indexInfo));
        } catch (Exception e) {
            logger.logError("Benchmark failed for " + manager.getType(), e);
            return Optional.empty();
        } finally {
            if (indexList != null && !indexList.isEmpty()) {
                logger.log("Dropping indexes for " + manager.getType());
                // Clean up indexes after the task execution if they were created
                manager.dropIndexes(scope, indexList);
            }
        }
    }

    /**
     * Executes a generic single query benchmark for a specific database manager.
     *
     * @param results            the map to store results
     * @param manager            the database manager
     * @param query              the query to execute
     * @param indexes            the indexes to apply
     * @param numberOfExecutions the number of times to execute the query
     * @param selectedSize       the selected database size
     * @param request            the benchmark request containing additional parameters
     * @return an optional result containing task output and index metadata
     */
    private Optional<BenchmarkResult<Result>> executeGenericBenchmarkSingleQueryForManager(
            Map<DBType, ResultWithIndex> results,
            DatabaseManager manager, Query query,
            Map<DBType, List<Index>> indexes, int numberOfExecutions,
            DBSize selectedSize, BenchmarkRequest request) {

        List<Index> indexList = getIndexes(indexes, manager.getType());
        BenchmarkTask<Result> task = (scope) -> manager.run(query, numberOfExecutions, scope, indexList);

        return executeBenchmarkForManager(manager, selectedSize, indexList, task);
    }

    /**
     * Runs a predefined single query benchmark on selected databases.
     *
     * @param request the unique request containing query and database options
     * @return a map of results per database type
     */
    public Map<DBType, ResultWithIndex> runSingleQueryBenchmark(UniqueRequest request) {
        logger.logHeader("SINGLE QUERY BENCHMARK (" + request.getSelectedQuery().getName().toUpperCase() + ")");

        List<DatabaseManager> managers = getManagers(request.getSelectedDatabases());
        Map<DBType, ResultWithIndex> results = new TreeMap<>(Comparator.comparing(DBType::getName));

        // Iterate over each manager and execute the query
        for (DatabaseManager manager : managers) {

            // Retrieve the predefined query from the factory
            Query query = queryFactory.getQuery(manager.getType(), request.getSelectedQuery());

            executeGenericBenchmarkSingleQueryForManager(
                    results, manager, query, request.getIndexes(),
                    request.getNumberOfExecutions(), request.getSelectedSize(), request
            ).ifPresent(result -> results.put(
                    manager.getType(),
                    new ResultWithIndex(result.taskResult(), result.indexInfo())
            ));

        }
        logger.logEnd();
        return results;
    }

    /**
     * Runs a custom single query benchmark with user-defined queries.
     *
     * @param request the custom query request
     * @return a map of results per database type
     */
    public Map<DBType, ResultWithIndex> runCustomSingleQueryBenchmark(CustomRequest request) {
        logger.logHeader("CUSTOM QUERY BENCHMARK");

        Map<DBType, ResultWithIndex> results = new TreeMap<>(Comparator.comparing(DBType::getName));

        // Iterate over each database type and execute the custom query
        for (DBType dbType : request.getSelectedDatabases()) {
            String queryString = request.getCustomQueries().get(dbType);
            if (queryString == null) {
                logger.logError("No custom query provided for database type: " + dbType, null);
            }

            // Create the custom query using the factory
            Query query = queryFactory.createCustomQuery(dbType, queryString);
            DatabaseManager manager = query.getManager();
            executeGenericBenchmarkSingleQueryForManager(results, manager, query, request.getIndexes(), request.getNumberOfExecutions(), request.getSelectedSize(), request)
                    .ifPresent(result -> results.put(
                            manager.getType(),
                            new ResultWithIndex(result.taskResult(), result.indexInfo())
                    ));
            ;
        }
        logger.logEnd();
        return results;
    }

    /**
     * Runs a generic workload benchmark for multiple databases and queries.
     *
     * @param header       a descriptive header for the benchmark log
     * @param workload     the workload containing query types and ratios
     * @param dbTypes      the database types to run against
     * @param dbSize       the size of the dataset
     * @param nbExecutions the number of executions per query type
     * @param indexes      the indexes to create per database type
     * @return a map of workload results per database type
     */
    public Map<DBType, WorkloadResult> runGenericWorkloadBenchmark(
            String header, Workload workload, List<DBType> dbTypes, DBSize dbSize,
            int nbExecutions, Map<DBType, List<Index>> indexes
    ) {
        logger.logHeader(header);


        List<DatabaseManager> managers = getManagers(dbTypes);
        Map<DBType, WorkloadResult> results = new TreeMap<>(Comparator.comparing(DBType::getName));

        // Iterate over each database manager and execute the workload
        for (DatabaseManager manager : managers) {
            List<Index> indexList = getIndexes(indexes, manager.getType());

            BenchmarkTask<Map<QueryType, Result>> task = (scope) -> {
                Map<QueryType, Result> queryResults = new TreeMap<>(Comparator.comparing(QueryType::getName));
                for (QueryType queryType : workload.getQueryTypes()) {
                    Query query = queryFactory.getQuery(manager.getType(), queryType);
                    // Determine the number of executions for this query type based on its ratio
                    int nbExecForQuery = (int) Math.ceil(nbExecutions * workload.getRatioForType(queryType));
                    Result result = manager.run(query, nbExecForQuery, scope, indexList);
                    queryResults.put(queryType, result);
                }
                return queryResults;
            };

            executeBenchmarkForManager(manager, dbSize, indexList, task)
                    .ifPresent(res -> results.put(
                            manager.getType(),
                            new WorkloadResult(res.indexInfo(), res.taskResult())
                    ));
        }
        logger.logEnd();
        return results;
    }

    /**
     * Runs a predefined workload benchmark.
     *
     * @param request the workload request
     * @return a map of workload results per database type
     */
    public Map<DBType, WorkloadResult> runWorkloadBenchmark(WorkloadRequest request) {

        // Retrieve the workload from the factory based on the selected workload type
        Workload workload = workloadFactory.getWorkload(request.getSelectedWorkload());
        String header = "WORKLOAD BENCHMARK (" + request.getSelectedWorkload().getName().toUpperCase() + ")";

        return runGenericWorkloadBenchmark(
                header,
                workload,
                request.getSelectedDatabases(),
                request.getSelectedSize(),
                request.getNumberOfExecutions(),
                request.getIndexes()
        );
    }

    /**
     * Runs a workload benchmark using custom-defined workload queries.
     *
     * @param request the custom workload request
     * @return a map of workload results per database type
     */
    public Map<DBType, WorkloadResult> runCustomWorkloadBenchmark(CustomWorkloadRequest request) {
        // Create a custom workload using the provided queries with the factory
        Workload workload = workloadFactory.createCustomWorkload((ArrayList<WorkloadQuery>) request.getCustomWorkloadQueries());
        return runGenericWorkloadBenchmark(
                "CUSTOM WORKLOAD BENCHMARK",
                workload,
                request.getSelectedDatabases(),
                request.getSelectedSize(),
                request.getNumberOfExecutions(),
                request.getIndexes()
        );
    }

    /**
     * Shuts down any resources used by the benchmark service.
     */
    public void shutdown() {
        CouchbaseManager.shutdown();
    }
}