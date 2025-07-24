package backend.model.result;

import backend.model.metadata.IndexInfo;
import backend.model.options.QueryType;

import java.util.List;
import java.util.Map;

/**
 * The {@code WorkloadResult} class encapsulates the results of a workload analysis.
 * It contains a list of index information and a map of query types to their respective results.
 *
 * @author Eva Ray
 */
public class WorkloadResult {

    List<IndexInfo> indexInfo;
    // Map of query types to their results
    Map<QueryType, Result> results;

    /**
     * Constructs a new {@code WorkloadResult} instance with the specified index information
     * and results for different query types.
     *
     * @param indexInfo the list of index information
     * @param results   the map of query types to their results
     */
    public WorkloadResult(List<IndexInfo> indexInfo, Map<QueryType, Result> results) {
        this.indexInfo = indexInfo;
        this.results = results;
    }

    public List<IndexInfo> getIndexInfo() {
        return indexInfo;
    }

    public void setIndexInfo(List<IndexInfo> indexInfo) {
        this.indexInfo = indexInfo;
    }

    public Map<QueryType, Result> getResults() {
        return results;
    }

    public void setResults(Map<QueryType, Result> results) {
        this.results = results;
    }
}
