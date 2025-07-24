package backend.model.result;

import backend.model.metadata.CacheInfo;
import backend.model.metadata.IndexInfo;

import java.util.List;

/**
 * The {@code ResultWithIndex} class extends the {@code Result} class to include additional
 * information about database indexes. It contains a list of {@code IndexInfo} objects that
 * provide details about the indexes used in the query execution.
 *
 * @author Eva Ray
 */
public class ResultWithIndex extends Result{

    private List<IndexInfo> indexInfo;

    /**
     * Constructs a new {@code ResultWithIndex} instance with the specified parameters.
     *
     * @param query                the SQL query executed
     * @param avgExecutionTime     the average execution time of the query
     * @param queryPerSecond       the number of queries executed per second
     * @param explainPlan          the explain plan for the query
     * @param initialConnectionTime the time taken to establish the initial connection
     * @param standardDeviation    the standard deviation of execution times
     * @param variance             the variance of execution times
     * @param percentile95         the 95th percentile of execution times
     * @param cacheInfo            information about cache hits and misses
     * @param indexInfo            a list of index information related to the query
     */
    public ResultWithIndex(String query, double avgExecutionTime, double queryPerSecond, String explainPlan,
                           long initialConnectionTime, double standardDeviation, double variance, double percentile95,
                           CacheInfo cacheInfo, List<IndexInfo> indexInfo) {
        super(query, avgExecutionTime, queryPerSecond, explainPlan, initialConnectionTime, standardDeviation, variance, percentile95, cacheInfo);
        this.indexInfo = indexInfo;
    }

/**
     * Constructs a new {@code ResultWithIndex} instance from an existing {@code Result} object
     * and a list of index information.
     *
     * @param result    the existing result object containing query execution details
     * @param indexInfo a list of index information related to the query
     */
    public ResultWithIndex(Result result, List<IndexInfo> indexInfo) {
        super(result.getQuery(), result.getAvgExecutionTime(), result.getQueryPerSecond(), result.getExplainPlan(),
              result.getInitialConnectionTime(), result.getStandardDeviation(), result.getVariance(), result.getPercentile95(),
              result.getCacheInfo());
        this.indexInfo = indexInfo;
    }

    public List<IndexInfo> getIndexInfo() {
        return indexInfo;
    }

    public void setIndexInfo(List<IndexInfo> indexInfo) {
        this.indexInfo = indexInfo;
    }
}
