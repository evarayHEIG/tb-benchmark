package backend.model.request;

import backend.model.options.WorkloadType;

/**
 * The {@code WorkloadRequest} class represents a request for a specific workload type in a benchmark.
 * It extends the {@code BenchmarkRequest} class and includes a field for the selected workload type.
 * This class is used to encapsulate the workload selection in benchmark requests.
 *
 * The attributes of this class match the attributes of the benchmark configuration files, so that
 * Javalin can automatically convert the JSON files into instances of this class.
 * Getters and setters are mandatory for this conversion to work correctly.
 *
 * @author Eva Ray
 */
public class WorkloadRequest extends  BenchmarkRequest {
    private WorkloadType selectedWorkload;

    public WorkloadRequest() {
    }

    public WorkloadType getSelectedWorkload() {
        return selectedWorkload;
    }

    public void setSelectedWorkload(WorkloadType selectedWorkload) {
        this.selectedWorkload = selectedWorkload;
    }
}