package backend.model.result;

import backend.model.metadata.CacheInfo;

/**
 * The {@code Result} class encapsulates the results of a database query performance analysis.
 * It includes metrics such as average execution time, queries per second, explain plan,
 * initial connection time, standard deviation, variance, 95th percentile, and cache information.
 *
 * @author Eva Ray
 */
public class Result {

    private final String query;
    private final double avgExecutionTime;
    private final double queryPerSecond;
    private final String explainPlan;
    private final long initialConnectionTime;
    private final double standardDeviation;
    private final double variance;
    private final double percentile95;
    private final CacheInfo cacheInfo;

    /**
     * Constructs a new {@code Result} instance with the specified parameters.
     *
     * @param query                the SQL query executed
     * @param avgExecutionTime     the average execution time of the query in milliseconds
     * @param queryPerSecond       the number of queries executed per second
     * @param explainPlan          the explain plan for the query
     * @param initialConnectionTime the time taken to establish the initial connection in milliseconds
     * @param standardDeviation    the standard deviation of execution times
     * @param variance             the variance of execution times
     * @param percentile95         the 95th percentile of execution times
     * @param cacheInfo            cache information related to the query performance
     */
    public Result(String query, double avgExecutionTime, double queryPerSecond, String explainPlan, long initialConnectionTime,
                  double standardDeviation, double variance, double percentile95, CacheInfo cacheInfo) {
        this.query = query;
        this.avgExecutionTime = avgExecutionTime;
        this.queryPerSecond = queryPerSecond;
        this.explainPlan = explainPlan;
        this.initialConnectionTime = initialConnectionTime;
        this.standardDeviation = standardDeviation;
        this.variance = variance;
        this.percentile95 = percentile95;
        this.cacheInfo = cacheInfo;
    }

    public String getQuery() {
        return query;
    }

    public double getAvgExecutionTime() {
        return avgExecutionTime;
    }

    public double getQueryPerSecond() {
        return queryPerSecond;
    }

    public String getExplainPlan() {
        return explainPlan;
    }

    public long getInitialConnectionTime() {
        return initialConnectionTime;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getVariance() {
        return variance;
    }

    public double getPercentile95() {
        return percentile95;
    }

    public CacheInfo getCacheInfo() {
        return cacheInfo;
    }
}
