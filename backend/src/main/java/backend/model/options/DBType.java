package backend.model.options;

/**
 * The {@code DBType} enum represents different types of databases.
 * It is used to categorize databases into Couchbase, PostgreSQL, and PostgreSQL JSONB.
 * This enum is used in the benchmark configuration files to specify the type of database being used for testing.
 *
 * @author Eva Ray
 */
public enum DBType {
    COUCHBASE("Couchbase"),
    POSTGRESQL("PostgreSQL"),
    POSTGRESQL_JSONB("PostgreSQL JSONB");

    private final String name;

    /**
     * Constructs a new {@code DBType} instance with the specified name.
     *
     * @param name the name of the database type
     */
    DBType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}