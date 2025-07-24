package backend.model.request;

import backend.model.options.DBSize;
import backend.model.options.DBType;

import java.util.List;
import java.util.Map;

/**
 * The {@code BenchmarkRequest} class represents a request for a benchmark test.
 * It contains the number of executions, selected databases, database size, and indexes.
 * This class is used to configure benchmark tests on different database types.
 * The attributes of this class match the attributes of the benchmark configuration files, so that
 * Javalin can automatically convert the JSON files into instances of this class.
 * Getters and setters are mandatory for this conversion to work correctly.
 *
 * @author Eva Ray
 */
public abstract class BenchmarkRequest {

    private int numberOfExecutions;
    private List<DBType> selectedDatabases;
    private DBSize selectedSize;
    private Map<DBType, List<Index>> indexes = null;

    public BenchmarkRequest() {
        // Constructeur par défaut requis par Jackson
    }

    public int getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(int numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    public Map<DBType, List<Index>> getIndexes() {
        return indexes;
    }

    public void setIndexes(Map<DBType, List<Index>> indexes) {
        this.indexes = indexes;
    }

    public List<DBType> getSelectedDatabases() {
        return selectedDatabases;
    }

    public void setSelectedDatabases(List<DBType> selectedDatabases) {
        this.selectedDatabases = selectedDatabases;
    }

    public DBSize getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(DBSize selectedSize) {
        this.selectedSize = selectedSize;
    }

}
