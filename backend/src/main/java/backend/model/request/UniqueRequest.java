package backend.model.request;

import backend.model.options.QueryType;

/**
 * The {@code UniqueRequest} class represents a unique request in the benchmark system.
 * It extends the {@code BenchmarkRequest} class and includes a selected query type.
 * This class is used to encapsulate the details of a unique request, including the query type to be executed.
 *
 * The attributes of this class match the attributes of the benchmark configuration files, so that
 * Javalin can automatically convert the JSON files into instances of this class.
 * Getters and setters are mandatory for this conversion to work correctly.
 *
 * @author Eva Ray
 */
public class UniqueRequest extends BenchmarkRequest{
    private QueryType selectedQuery;

    public QueryType getSelectedQuery() {
        return selectedQuery;
    }

    public void setSelectedQuery(QueryType selectedQuery) {
        this.selectedQuery = selectedQuery;
    }
}