package backend.model.metadata;

/**
 * The {@code IndexInfo} class encapsulates information about a database index.
 * It includes the index name, the table it belongs to, the size of the index,
 * the size of the table, and the ratio of index size to table size.
 *
 * @author Eva Ray
 */
public class IndexInfo {

    String indexName;
    String table;
    String indexSize;
    String tableSize;
    double sizeRatio;

    /**
     * Constructs a new {@code IndexInfo} instance with the specified index name,
     * table name, table size, index size, and size ratio.
     *
     * @param indexName  the name of the index
     * @param table      the name of the table
     * @param tableSize  the size of the table
     * @param indexSize  the size of the index
     * @param sizeRatio  the ratio of index size to table size
     */
    public IndexInfo(String indexName, String table, String tableSize, String indexSize, double sizeRatio) {
        this.indexName = indexName;
        this.table = table;
        this.tableSize = tableSize;
        this.indexSize = indexSize;
        this.sizeRatio = sizeRatio;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTable() {
        return table;
    }

    public String getIndexSize() {
        return indexSize;
    }

    public String getTableSize() {
        return tableSize;
    }

    public double getSizeRatio() {
        return sizeRatio;
    }
}
