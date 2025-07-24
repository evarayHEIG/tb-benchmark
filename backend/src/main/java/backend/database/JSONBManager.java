package backend.database;

import backend.model.options.DBType;
import backend.model.options.IndexType;
import backend.model.request.Index;

import java.util.List;

/**
 * The {@code JSONBManager} class is responsible for managing PostgreSQL databases with JSONB support.
 * It extends the {@code PGManager} class and provides methods to create indexes on JSONB fields.
 * This class implements the Singleton design pattern to ensure only one instance exists.
 *
 * It is expected that the PostgreSQL JSONB databse is running on the default port 5432.
 *
 * @author Eva Ray
 */
public class JSONBManager extends PGManager{

    // Singleton instance
    private static JSONBManager instance;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the PostgreSQL manager with the specified port.
     *
     * @param port the port number for the PostgreSQL database connection
     */
    private JSONBManager(int port) {
        super(port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DBType getType() {
        return DBType.POSTGRESQL_JSONB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIndexCreationString(Index index) {
        List<String> fieldsList = index.getFields();

        String jsonbExpressions = fieldsList.stream()
                .map(field -> "(data " + (index.getType().equals(IndexType.GIN) ? "->" : "->>") +" '" + field + "')")
                .collect(java.util.stream.Collectors.joining(", "));

        return String.format(
                "CREATE INDEX IF NOT EXISTS \"%s\" ON \"%s\" USING %s (%s)",
                index.getName(), index.getTable(), index.getType(), jsonbExpressions);
    }

    /**
     * Returns the singleton instance of {@code JSONBManager}.
     * If the instance is null, it creates a new instance with the default port 5432.
     *
     * @return the singleton instance of {@code JSONBManager}
     */
    public static JSONBManager getInstance() {
        if (instance == null) {
            instance = new JSONBManager(5432);
        }
        return instance;
    }
}
