package backend.database;

import backend.model.options.DBType;
import backend.model.request.Index;

/**
 * The {@code RelManager} class is a singleton that manages the connection to a PostgreSQL relational database.
 * It extends the {@code PGManager} class and provides methods for creating indexes and retrieving the database type.
 * This class is specifically designed for managing relational databases using PostgreSQL.
 *
 * It is expected that the PostgreSQL server is running on port 5433.
 *
 * @author Eva Ray
 */
public class RelManager extends PGManager{

    // Singleton instance
    private static RelManager instance;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the PostgreSQL manager with the specified port.
     *
     * @param port the port number for the PostgreSQL server
     */
    private RelManager(int port) {
        super(port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DBType getType() {
        return DBType.POSTGRESQL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIndexCreationString(Index index) {
        String fields = String.join(", ", index.getFields());
        return String.format(
                "CREATE INDEX IF NOT EXISTS %s ON %s USING %s (%s)",
                index.getName(), index.getTable(), index.getType(), fields);
    }

    /**
     * Returns the singleton instance of the {@code RelManager}.
     * If the instance is not created yet, it initializes a new instance.
     *
     * @return the singleton instance of {@code RelManager}
     */
    public static RelManager getInstance() {
        if (instance == null) {
            instance = new RelManager(5433);
        }
        return instance;
    }
}
