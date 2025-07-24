package backend.model.options;

/**
 * The {@code QueryType} enum represents different types of queries that can be executed in a database.
 * It is used to categorize queries into various types such as FILTER_IS_MISSING, FILTER4, ARRAY, NEST, etc.
 * This enum is used in the benchmark configuration files to specify the type of query being executed for testing.
 *
 * @author Eva Ray
 */
public enum QueryType {
    FILTER_IS_MISSING("WHERE with IS MISSING clause"),
    FILTER4("WHERE with 4 tests"),
    ARRAY("WHERE on ARRAY"),
    NEST("NEST"),
    NEST_AGG("NEST with aggregation function"),
    UNNEST("UNNEST"),
    UNNEST_GROUP_BY("UNNEST followed by GROUP BY"),
    AGG("Aggregation"),
    SELECT("SELECT"),
    FILTER("WHERE"),
    JOIN1("JOIN"),
    JOIN_FILTER("JOIN with filter"),
    CUSTOM("Custom query"),
    IMBRICATION_FILTER("WHERE on nested fields (3 layers deep)"),;

    private final String name;

    /**
     * Constructs a new {@code QueryType} instance with the specified name.
     *
     * @param name the name of the query type
     */
    QueryType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}