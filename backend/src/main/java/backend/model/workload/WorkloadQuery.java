package backend.model.workload;

import backend.model.options.QueryType;

/**
 * The {@code WorkloadQuery} class represents a query in a workload.
 * It contains the type of query and its ratio in the workload. The ratio indicates the proportion of this query type
 * relative to the total number of queries in the workload.
 * This class is used to define the distribution of different types of queries in a workload.
 *
 * @author Eva Ray
 */
public class WorkloadQuery {

    private QueryType type;
    private double ratio;

    public WorkloadQuery(){

    }

    /**
     * Constructs a new {@code WorkloadQuery} instance with the specified query type and ratio.
     *
     * @param type the type of query (e.g., SELECT, INSERT, UPDATE)
     * @param ratio the ratio of this query type in the workload
     */
    public WorkloadQuery(QueryType type, double ratio) {
        this.type = type;
        this.ratio = ratio;
    }

    public QueryType getType() {
        return type;
    }

    public void setType(QueryType type) {
        this.type = type;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    /**
     * Returns the ratio of this query type in the workload.
     * If the provided type matches the type of this query, it returns the ratio; otherwise, it returns 0.0.
     *
     * @param type the query type to check against
     * @return the ratio of this query type if it matches, otherwise 0.0
     */
    public double getRatioForType(QueryType type) {
        if (this.type.equals(type)) {
            return ratio;
        }
        return 0.0;
    }
}
