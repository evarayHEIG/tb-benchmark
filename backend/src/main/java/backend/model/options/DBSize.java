package backend.model.options;

/**
 * The {@code DBSize} enum represents different sizes of databases.
 * It is used to categorize databases into small, medium, and large sizes.
 * It is used in the benchmark configuration files to specify the size of the database being used for testing.
 *
 * @author Eva Ray
 */
public enum DBSize {
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large");

    private final String name;

    /**
     * Constructs a new {@code DBSize} instance with the specified name.
     *
     * @param name the name of the database size
     */
    DBSize(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
