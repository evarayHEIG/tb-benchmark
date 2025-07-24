package backend.model.workload;

import backend.model.options.QueryType;
import backend.model.options.WorkloadType;

import java.util.List;

/**
 * The {@code Workload} class represents a collection of queries that can be executed as part of a workload.
 * It encapsulates the type of workload and the list of queries associated with it.
 * This class provides methods to retrieve the type of workload, the queries, and their respective types and ratios.
 *
 * @author Eva Ray
 */
public class Workload {

    WorkloadType type;
    List<WorkloadQuery> queries;

    /**
     * Constructs a new {@code Workload} instance with the specified type and list of queries.
     *
     * @param type    the type of workload (e.g., READ, WRITE)
     * @param queries the list of queries associated with this workload
     */
    public Workload(WorkloadType type, List<WorkloadQuery> queries) {
        this.type = type;
        this.queries = queries;
    }

    public WorkloadType getType() {
        return type;
    }

    public void setType(WorkloadType type) {
        this.type = type;
    }

    public List<WorkloadQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<WorkloadQuery> queries) {
        this.queries = queries;
    }

    /**
     * Returns an array of query types for the queries in this workload.
     *
     * @return an array of {@link QueryType} representing the types of queries in this workload
     */
    public QueryType[] getQueryTypes() {
        return queries.stream()
                .map(WorkloadQuery::getType)
                .toArray(QueryType[]::new);
    }

    /**
     * Returns the ratio for a specific query type in this workload.
     *
     * @param type the {@link QueryType} for which to retrieve the ratio
     * @return the ratio of the specified query type, or 0.0 if not found
     */
    public double getRatioForType(QueryType type) {
        return queries.stream()
                .filter(query -> query.getType().equals(type))
                .findFirst()
                .map(WorkloadQuery::getRatio)
                .orElse(0.0);
    }
}
