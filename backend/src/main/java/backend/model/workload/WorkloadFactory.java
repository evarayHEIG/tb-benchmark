package backend.model.workload;

import backend.model.options.QueryType;
import backend.model.options.WorkloadType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code WorkloadFactory} class is a singleton factory for creating and managing different types of workloads.
 * It provides methods to create predefined workloads based on the {@link WorkloadType} enum and allows for custom workloads.
 * This class ensures that each workload type is created only once and cached for future use.
 *
 * @author Eva Ray
 */
public class WorkloadFactory {

    // Singleton instance
    private static WorkloadFactory instance;

    // Map to hold the cached workloads
    private final Map<WorkloadType, Workload> workloads = new HashMap<>();

    /**
     * Private constructor to prevent instantiation
     */
    private WorkloadFactory() {

    }

    /**
     * Adds a workload to the factory's cache.
     *
     * @param workload the workload to add
     */
    private void addWorkload(Workload workload) {
        if (workload != null) {
            workloads.put(workload.getType(), workload);
        }
    }

    /**
     * Creates a predefined workload based on the specified type.
     *
     * @param type the type of workload to create
     * @return the created workload
     */
    private Workload createWorkload(WorkloadType type) {
        return switch (type) {
            case FILTER -> new Workload(
                    WorkloadType.FILTER,
                    new ArrayList<WorkloadQuery>() {{
                        add(new WorkloadQuery(QueryType.FILTER, 0.3));
                        add(new WorkloadQuery(QueryType.FILTER_IS_MISSING, 0.5));
                        add(new WorkloadQuery(QueryType.FILTER4, 0.2));
                    }});
            case JOIN -> new Workload(
                    WorkloadType.JOIN,
                    new ArrayList<WorkloadQuery>() {{
                        add(new WorkloadQuery(QueryType.JOIN1, 0.3));
                        add(new WorkloadQuery(QueryType.JOIN_FILTER, 0.7));
                    }});
            case IMBRICATION_OPERATIONS -> new Workload(
                    WorkloadType.IMBRICATION_OPERATIONS,
                    new ArrayList<WorkloadQuery>() {{
                        add(new WorkloadQuery(QueryType.NEST, 0.3));
                        add(new WorkloadQuery(QueryType.NEST_AGG, 0.1));
                        add(new WorkloadQuery(QueryType.UNNEST, 0.4));
                        add(new WorkloadQuery(QueryType.UNNEST_GROUP_BY, 0.4));
                    }});
            case DATA_ANALYSIS -> new Workload(
                    WorkloadType.DATA_ANALYSIS,
                    new ArrayList<WorkloadQuery>() {{
                        add(new WorkloadQuery(QueryType.UNNEST, 0.4));
                        add(new WorkloadQuery(QueryType.JOIN_FILTER, 0.35));
                        add(new WorkloadQuery(QueryType.AGG, 0.25));
                    }});
            case COMPLETE -> new Workload(
                    WorkloadType.COMPLETE,
                    new ArrayList<WorkloadQuery>() {{
                        add(new WorkloadQuery(QueryType.FILTER, 1));
                        add(new WorkloadQuery(QueryType.FILTER_IS_MISSING, 1));
                        add(new WorkloadQuery(QueryType.FILTER4, 1));
                        add(new WorkloadQuery(QueryType.JOIN1, 1));
                        add(new WorkloadQuery(QueryType.JOIN_FILTER, 1));
                        add(new WorkloadQuery(QueryType.NEST, 1));
                        add(new WorkloadQuery(QueryType.UNNEST, 1));
                        add(new WorkloadQuery(QueryType.UNNEST_GROUP_BY, 1));
                        add(new WorkloadQuery(QueryType.AGG, 1));
                        add(new WorkloadQuery(QueryType.SELECT, 1));
                        add(new WorkloadQuery(QueryType.ARRAY, 1));
                        add(new WorkloadQuery(QueryType.IMBRICATION_FILTER, 1));
                    }});
            case CUSTOM -> null; // Les workloads personnalisés sont créés via createCustomWorkload
        };
    }

    /**
     * Creates a custom workload with the specified queries.
     *
     * @param queries the list of queries to include in the custom workload
     * @return the created custom workload
     * @throws IllegalArgumentException if the queries list is null or empty
     */
    public Workload createCustomWorkload(ArrayList<WorkloadQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            throw new IllegalArgumentException("Invalid workload type or queries");
        }
        Workload customWorkload = new Workload(WorkloadType.CUSTOM, queries);
        addWorkload(customWorkload);
        return customWorkload;
    }


    /**
     * Returns the workload of the specified type.
     * If the workload is already cached, it returns the cached version, if not, it creates a new one and caches it.
     *
     * @param type the type of workload to retrieve
     * @return the workload of the specified type, or null if it cannot be created
     */
    public Workload getWorkload(WorkloadType type) {
        // Return the cached workload if it exists
        if (workloads.containsKey(type)) {
            return workloads.get(type);
        }

        // Otherwise, create a new workload of the specified type
        Workload workload = createWorkload(type);
        if (workload != null) {
            workloads.put(type, workload);
        }
        return workload;
    }

    /**
     * Returns the singleton instance of the WorkloadFactory.
     * If the instance does not exist, it creates a new one.
     *
     * @return the singleton instance of WorkloadFactory
     */
    public static WorkloadFactory getInstance() {
        if (instance == null) {
            instance = new WorkloadFactory();
        }
        return instance;
    }
}
