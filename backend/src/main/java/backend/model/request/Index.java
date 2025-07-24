package backend.model.request;

import backend.model.options.IndexType;

import java.util.List;

/**
 * The {@code Index} class represents an index in a database.
 * It encapsulates the table name, fields, index name, and index type.
 * This class is used to define and manage indexes in the database.
 *
 * The attributes of this class match some attributes of the benchmark configuration files, so that
 * Javalin can automatically convert the JSON files into instances of this class.
 * Getters and setters are mandatory for this conversion to work correctly.
 *
 * @author Eva Ray
 */
public class Index {
    private String table;
    private List<String> fields;
    private String name;
    private IndexType type;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IndexType getType() {
        if ((type == null)) {
            return IndexType.GIN;
        }
        return type;
    }

    public void setType(IndexType type) {
        this.type = type;
    }

    public IndexType getIndexType() {
        if (type == null) {
            return null;
        }

        return type;
    }
}
