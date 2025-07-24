package backend.model.request;

import backend.model.options.DBType;

import java.util.Map;

/**
 * The {@code CustomRequest} class represents a custom request for benchmark queries.
 * It extends the {@code BenchmarkRequest} class and allows for the inclusion of custom queries
 * mapped to different database types.
 *
 * The attributes of this class match the attributes of the benchmark configuration files, so that
 * Javalin can automatically convert the JSON files into instances of this class.
 * Getters and setters are mandatory for this conversion to work correctly.
 *
 * @author Eva Ray
 */
public class CustomRequest extends BenchmarkRequest {

    private Map<DBType, String> customQueries;

    public Map<DBType, String> getCustomQueries() {
        return customQueries;
    }

    public void setCustomQueries(Map<DBType, String> customQueries) {
        this.customQueries = customQueries;
    }
}
