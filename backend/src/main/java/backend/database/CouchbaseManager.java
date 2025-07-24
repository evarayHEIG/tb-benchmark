package backend.database;

import backend.controller.CouchbaseApiController;
import backend.model.metadata.*;
import backend.model.query.Query;
import backend.model.request.Index;
import backend.model.result.Result;
import backend.parser.CouchbaseTimingsParser;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryProfile;
import com.couchbase.client.java.query.QueryResult;
import backend.model.options.DBType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * The {@code CouchbaseManager} class is responsible for managing Couchbase database operations.
 * It provides methods to connect to the Couchbase cluster, run queries, create and drop indexes,
 * and retrieve cache and index information.
 * This class implements the singleton pattern to ensure only one instance exists throughout the application.
 *
 * @author Eva Ray
 */
public class CouchbaseManager extends DatabaseManager {

    // Singleton instance
    private static CouchbaseManager instance;

    // Constants for Couchbase connection
    private static final String HOST = "couchbase://127.0.0.1";
    private static final String USERNAME = "Administrator";
    private static final String PASSWORD = "password";
    private static final String BUCKET = "yelp_reviews";

    private static long initialConnectionTime = 0;
    double[] executionTimes;
    private String timingsProfile;

    private static Cluster cluster = null;
    private static Bucket bucket = null;

    // Parser for Couchbase execution plans (timings)
    private final CouchbaseTimingsParser parser;
    // API controller for Couchbase operations
    private final CouchbaseApiController apiController;

    private CouchbaseManager() {
        super();
        executionTimes = new double[0];
        apiController = new CouchbaseApiController(HOST, USERNAME, PASSWORD);
        long start = System.currentTimeMillis();
        try {
            // Initialize Couchbase cluster connection
            cluster = Cluster.connect(HOST, ClusterOptions.clusterOptions(USERNAME, PASSWORD));
            long end = System.currentTimeMillis();
            initialConnectionTime = end - start;
            // Create and connect to the specified bucket
            bucket = cluster.bucket(BUCKET);
            bucket.waitUntilReady(Duration.ofSeconds(10));
            // Ping the cluster to ensure connection is established
            cluster.ping();
        } catch (Exception e) {
            System.err.println("Error during Couchbase initialisation : " + e);
        }
        this.parser = new CouchbaseTimingsParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getInitialConnectionTime() {
        return initialConnectionTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DBType getType() {
        return DBType.COUCHBASE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result run(Query query, int nbExecutions, String scope, List<Index> indexes) throws Exception {

        warmup(query, scope);

        String actualQuery = query.getQuery();

        System.out.println("Running Couchbase query " + query.getQueryType().getName() + " " + nbExecutions + " times in scope: " + scope + ": \n" + actualQuery);

        this.executionTimes = benchmarkQuery(bucket.scope(scope), nbExecutions, actualQuery);
        DescriptiveStatistics stats = new DescriptiveStatistics(this.executionTimes);
        double avgLatency = Double.parseDouble(df.format(stats.getMean()).replace(',', '.'));
        double variance = Double.parseDouble(df.format(stats.getVariance()).replace(',', '.'));
        double stdDev = Double.parseDouble(df.format(stats.getStandardDeviation()).replace(',', '.'));
        double p95 = Double.parseDouble(df.format(stats.getPercentile(95)).replace(',', '.'));

        System.out.println("Query benchmarking completed. Average latency: " + avgLatency + " ms");

        return new Result(actualQuery, avgLatency, TPS(avgLatency), parser.parseProfile(timingsProfile).toIndentedString(3), initialConnectionTime,
                stdDev, variance, p95, getCacheInfo(nbExecutions));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warmup(Query query, String scope) {
        System.out.println("Warming up Couchbase for query: " + query.getQueryType().getName());
        String actualQuery = query.getQuery();

        var result = benchmarkQuery(bucket.scope(scope), WARMUP_EXECUTIONS, actualQuery);

        System.out.println("Warming up completed. Average latency: " + Arrays.stream(result).average().orElse(0.0) + " ms");
    }

    /**
     * Executes a query on the specified scope a certain number of times and returns the execution times in an array.
     *
     * @param scope  the Couchbase scope to execute the query on
     * @param nbRuns the number of times to run the query
     * @param query  the N1QL query string to execute
     * @return an array of execution times in milliseconds
     */
    private double[] benchmarkQuery(Scope scope, int nbRuns, String query) {
        List<Double> executionTimes = new ArrayList<>();
        for (int i = 0; i < nbRuns; i++) {
            QueryResult result = scope.query(query,
                    QueryOptions.queryOptions()
                            .metrics(true) // Enable metrics to get execution time
                            .timeout(Duration.ofMinutes(180)) // Set a long timeout for the query
                            .profile(QueryProfile.TIMINGS) // Enable profiling to get detailed execution timings
            );

            if (result.metaData().profile().isPresent()) {
                // Retrieve execution plan (timings) from the result
                this.timingsProfile = result.metaData().profile().get().toString();
            }

            result.metaData().metrics().ifPresent(metrics ->
                // Add the execution time to the list of execution times
                executionTimes.add((double) metrics.executionTime().toMillis())
            );

        }
        return executionTimes.stream().mapToDouble(d -> d).toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createIndexes(String scope, List<Index> indexes) {
        System.out.println("Creating indexes in scope: " + scope);

        if (indexes == null || indexes.isEmpty()) {
            System.out.println("No indexes to create.");
            return;
        }

        try {
            for (Index index : indexes) {
                // Using the Couchbase SDK to create indexes
                bucket.scope(scope).collection(index.getTable()).queryIndexes().createIndex(index.getName(), index.getFields());
            }
        } catch (Exception e) {
            System.err.println("Error while creating indexes: " + e.getMessage());
        }

        System.out.println(indexes.size() + " indexes created successfully in scope: " + scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndexes(String scope, List<Index> indexes) {

        if (indexes == null || indexes.isEmpty()) {
            System.out.println("No indexes to drop in scope: " + scope);
            return;
        }

        System.out.println("Dropping indexes in scope: " + scope);

        for (Index index : indexes) {
            try {
                // Using the Couchbase SDK to drop indexes
                bucket.scope(scope).collection(index.getTable()).queryIndexes().dropIndex(index.getName());
            } catch (Exception e) {
                System.err.println("Error while dropping index " + index.getName() + ": " + e.getMessage());
            }
        }
        System.out.println(indexes.size() + " indexes dropped successfully in scope: " + scope);
    }

    public List<CouchbaseCollectionInfo> getCollectionsInfo(String scope) {
        List<CouchbaseCollectionInfo> collections = new ArrayList<>();

        // Query to retrieve collection information from the specified scope using keyspaces_info
        String query = """
                SELECT name,
                       size
                FROM system:keyspaces_info s
                WHERE s.`scope` = $scope
                """;

        try {
            QueryResult result = bucket.scope(scope).query(query,
                    QueryOptions.queryOptions()
                            .timeout(Duration.ofMinutes(10))
                            .parameters(JsonObject.create().put("scope", scope)) // Parameterize the scope
            );

            for (JsonObject row : result.rowsAsObject()) {
                String name = row.getString("name");
                long size = row.getLong("size");

                CouchbaseCollectionInfo collectionInfo = new CouchbaseCollectionInfo(name, scope, size);
                collections.add(collectionInfo);
            }
        } catch (Exception e) {
            System.err.println("Error while retrieving collections info: " + e.getMessage());
        }

        return collections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IndexInfo> getIndexesInfo(String scope) {
        try {
            List<CouchbaseCollectionInfo> collections = getCollectionsInfo(scope);
            // Retrieve index statistics for the specified bucket using the API controller
            List<CouchbaseIndexStats> indexStats = apiController.getIndexStats(BUCKET);

            List<IndexInfo> indexInfoList = new ArrayList<>();
            // Add index information to the list
            for (CouchbaseIndexStats stats : indexStats) {
                for (CouchbaseCollectionInfo collection : collections) {
                    if (stats.scopeName().equals(scope) && stats.collectionName().equals(collection.getCollectionName())) {
                        long indexSize = stats.indexSize();
                        long tableSize = collection.getCollectionSize();
                        double indexSizeRatio = calculateIndexSizeRatio(indexSize, tableSize);

                        IndexInfo indexInfo = new IndexInfo(
                                stats.indexName(),
                                collection.getCollectionName(),
                                apiController.formatByteSize(tableSize),
                                apiController.formatByteSize(indexSize),
                                indexSizeRatio
                        );
                        indexInfoList.add(indexInfo);
                    }
                }
            }
            return indexInfoList;
        } catch (Exception e) {
            System.err.println("Error while retrieving indexes info: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Calculates the ratio of index size to table size and formats it to two decimal places.
     *
     * @param indexSize the size of the index in bytes
     * @param tableSize the size of the table in bytes
     * @return the ratio of index size to table size as a percentage, formatted to two decimal places
     */
    private double calculateIndexSizeRatio(long indexSize, long tableSize) {
        if (tableSize == 0) {
            return 0.0;
        }

        return Double.parseDouble(df.format((double) indexSize / (double) tableSize * 100).replace(',', '.'));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheInfo getCacheInfo(int nbExecutions) {
        return getCacheInfoRecursive(nbExecutions, 0);
    }

    /**
     * Recursively retrieves cache information from the Couchbase API controller.
     * If cache information is not available, it retries up to 10 times with a 1-second delay.
     *
     * @param nbExecutions the number of executions to consider for cache statistics
     * @param retryCount   the current retry count
     * @return CacheInfo object containing estimated cache hits, background fetches, and cache hit rate
     */
    private CacheInfo getCacheInfoRecursive(int nbExecutions, int retryCount) {
        if (executionTimes.length == 0) {
            System.out.println("No execution times available for cache info.");
            return null;
        }
        try {
            // Retrieve cache statistics from the API controller. They are time series data, so we need to calculate an average.
            CouchbaseCacheStats cacheStats = apiController.getCacheStats(BUCKET);
            if (cacheStats == null) {
                System.out.println("No cache stats available.");
                return null;
            }

            int totalTimeinSeconds = (int) Math.ceil(Arrays.stream(executionTimes).sum() / 1000);

            double cacheMissRate = getSmoothedCacheInfo(cacheStats.cacheMissRate(), totalTimeinSeconds);
            double bgFetches = (getSmoothedCacheInfo(cacheStats.bgFetches(), totalTimeinSeconds) * totalTimeinSeconds) / nbExecutions;
            double ops = getSmoothedCacheInfo(cacheStats.ops(), totalTimeinSeconds);
            double cacheHitsRate = 100 - cacheMissRate;
            double estimatedCacheHits = (ops * cacheHitsRate * totalTimeinSeconds) / nbExecutions;

            // If cache info is not available yet, retry after 1 second
            if ((int) bgFetches == 0 && (int) estimatedCacheHits == 0 && retryCount < 10) {
                System.out.println("Cache info not available yet, waiting for 1 seconds before retrying...");
                sleep(1000);
                return getCacheInfoRecursive(nbExecutions, retryCount + 1);
            }

            return new CacheInfo((int) estimatedCacheHits, (int) bgFetches, (int) cacheHitsRate);
        } catch (Exception e) {
            System.err.println("Error while retrieving Couchbase cache info: " + e.getMessage());
        }
        return null;
    }

    /**
     * Calculates the smoothed cache information based on the provided data and total time in seconds.
     * It creates a sliding window that has the size of the minimum of the total execution time in seconds and 60 seconds.
     * This method computes the average of non-zero values within the window to provide a smoothed cache statistic.
     *
     * @param data              the array of cache data
     * @param totalTimeinSeconds the total time in seconds to consider for smoothing
     * @return the smoothed cache information as a double value
     */
    private double getSmoothedCacheInfo(double[] data, int totalTimeinSeconds) {
        int windowSize = Math.min(totalTimeinSeconds, 60); // max 1 minute of history returned by the API

        if (windowSize <= 0) {
            return 0.0; // No data available
        }

        double totalValue = 0.0;
        int nbNonZeroValues = 0;
        // Get the last non null windowSize values from the data array
        for (int i = data.length - 1; i >= 60 - windowSize; i--) {
            if (data[i] != 0) {
                nbNonZeroValues++;
                totalValue += data[i];
            }
        }

        return Double.parseDouble(df.format(nbNonZeroValues > 0 ? totalValue / nbNonZeroValues : totalValue).replace(',', '.'));
    }


    /**
     * Shuts down the Couchbase cluster connection.
     * This method should be called when the application is terminating to release resources.
     */
    public static void shutdown() {
        if (cluster != null) {
            cluster.disconnect();
        }
    }

    /**
     * Returns the singleton instance of CouchbaseManager.
     * If the instance is not created yet, it initializes a new instance.
     *
     * @return the singleton instance of CouchbaseManager
     */
    public static CouchbaseManager getInstance() {
        if (instance == null) {
            instance = new CouchbaseManager();
        }
        return instance;
    }
}