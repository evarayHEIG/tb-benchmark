package backend.model.options;

/**
 * The {@code WorkloadType} enum represents different types of workloads that can be executed in a database.
 * It is used to categorize workloads into Complete, Filter, Join, Imbrication Operations, Data Analysis, and Custom.
 * This enum is used in the benchmark configuration files to specify the type of workload being executed for testing.
 *
 * @author Eva Ray
 */
public enum WorkloadType {
    COMPLETE("Complete"),
    FILTER("Filter"),
    JOIN("Join"),
    IMBRICATION_OPERATIONS("Imbrication Operations"),
    DATA_ANALYSIS("Data Analysis"),
    CUSTOM("Custom");

    private final String name;

    /**
     * Constructs a new {@code WorkloadType} instance with the specified name.
     *
     * @param name the name of the workload type
     */
    WorkloadType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
