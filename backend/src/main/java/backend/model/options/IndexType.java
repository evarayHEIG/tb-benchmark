package backend.model.options;

/**
 * The {@code IndexType} enum represents different types of indexes used in databases.
 * It is used to categorize indexes into GIN, BTREE, HASH, SPGIST, BRIN, and GIST.
 * This enum is used in the benchmark configuration files to specify the type of index being used for testing.
 *
 * @author Eva Ray
 */
public enum IndexType {
    GIN("gin"),
    BTREE("btree"),
    HASH("hash"),
    SPGIST("spgist"),
    BRIN("brin"),
    GIST("gist");

    private final String name;

    /**
     * Constructs a new {@code IndexType} instance with the specified name.
     *
     * @param name the name of the index type
     */
    IndexType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
