package backend.service;

import backend.model.options.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * The {@code MetadataService} class provides methods to retrieve metadata options
 * for various enum types. It uses reflection to dynamically access enum values and
 * their associated names.
 *
 * @author Eva Ray
 */
public class MetadataService {

    /**
     * Converts a list of enum values into a list of maps containing "value" and "label" pairs.
     * Each map can represent an option in a dropdown or selection list.
     * - "value" is the raw enum name (e.g., POSTGRESQL).
     * - "label" is a more human-readable label (from getName() if available, otherwise from toString()).
     *
     * @param enumList list of enum constants (of any enum type)
     * @return list of maps with keys "value" and "label"
     */
    private List<Map<String, String>> getEnumOptions(List<? extends Enum<?>> enumList) {
        List<Map<String, String>> options = new ArrayList<>();

        // Iterate through each enum value and create a map for it
        for (Enum<?> enumValue : enumList) {
            Map<String, String> option = new HashMap<>();
            option.put("value", enumValue.name());

            // Try to call getName() if it exists to get a nicer label
            try {
                Method getNameMethod = enumValue.getClass().getMethod("getName");
                option.put("label", (String) getNameMethod.invoke(enumValue));
            } catch (Exception e) {
                // If getName() method doesn't exist or fails, fallback to toString()
                option.put("label", enumValue.toString());
            }
            // Add the constructed option to the result list
            options.add(option);
        }
        return options;
    }

    /**
     * Returns the available query type options as a list of value-label pairs.
     */
    public List<Map<String, String>> getQueryOptions() {
        return getEnumOptions(List.of(QueryType.values()));
    }

    /**
     * Returns the available database type options as a list of value-label pairs.
     */
    public List<Map<String, String>> getDatabaseOptions() {
        return getEnumOptions(List.of(DBType.values()));
    }

    /**
     * Returns the available database size options as a list of value-label pairs.
     */
    public List<Map<String, String>> getSizeOptions() {
        return getEnumOptions(List.of(DBSize.values()));
    }

    /**
     * Returns the available index type options as a list of value-label pairs.
     */
    public List<Map<String, String>> getIndexOptions() {
        return getEnumOptions(List.of(IndexType.values()));
    }

    /**
     * Returns the available workload type options as a list of value-label pairs.
     */
    public List<Map<String, String>> getWorkloadOptions() {
        return getEnumOptions(List.of(WorkloadType.values()));
    }
}