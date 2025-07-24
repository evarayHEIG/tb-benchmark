package backend.model.request;

import backend.model.workload.WorkloadQuery;

import java.util.List;

/**
 * The {@code CustomWorkloadRequest} class represents a request for a custom workload
 * in a benchmarking context. It extends the {@code BenchmarkRequest} class and includes
 * a list of custom workload queries.
 *
 * The attributes of this class match the attributes of the benchmark configuration files, so that
 * Javalin can automatically convert the JSON files into instances of this class.
 * Getters and setters are mandatory for this conversion to work correctly.
 *
 * @author Eva Ray
 */
public class CustomWorkloadRequest extends BenchmarkRequest {

    private List<WorkloadQuery> customWorkloadQueries;

    public List<WorkloadQuery> getCustomWorkloadQueries() {
        return customWorkloadQueries;
    }

    public void setCustomWorkloadQueries(List<WorkloadQuery> customWorkloadQueries) {
        this.customWorkloadQueries = customWorkloadQueries;
    }
}
