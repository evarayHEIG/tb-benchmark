package backend.database;

import backend.model.metadata.CacheInfo;
import backend.model.metadata.IndexInfo;
import backend.model.result.Result;
import backend.model.query.Query;
import backend.model.request.Index;
import backend.parser.PostgresExplainJsonParser;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The {@code PGManager} class is an abstract class that extends {@code DatabaseManager} and provides
 * methods to manage PostgreSQL database connections, execute queries, create and drop indexes,
 * and retrieve index information. It uses a connection pool to manage database connections efficiently.
 * This class is designed to be extended by specific PostgreSQL database managers that implement
 * the abstract methods defined in this class.
 *
 * @author Eva Ray
 */
public abstract class PGManager extends DatabaseManager {

    // Connection parameters
    private static final String[] SERVER_NAMES = new String[]{"localhost"};
    private static final String DB_NAME = "postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private long initialConnectionTime;
    private Block blocks = new Block(0, 0);
    private final PostgresExplainJsonParser explainJsonParser;

    // Data source cache to avoid creating multiple connections for the same port
    private static final Map<Integer, PGSimpleDataSource> dataSources = new HashMap<>();
    // Port for the PostgreSQL server
    private final int port;

    /**
     * Constructs a new {@code PGManager} instance with the specified port.
     * Initializes the data source for the PostgreSQL connection and measures
     * the initial connection time.
     *
     * @param port the port number for the PostgreSQL server
     */
    public PGManager(int port) {
        super();
        this.port = port;
        this.explainJsonParser = new PostgresExplainJsonParser();

        // Initialize the data source for the PostgreSQL connection
        dataSources.computeIfAbsent(port, p -> {
            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setServerNames(SERVER_NAMES);
            ds.setPortNumbers(new int[]{p});
            ds.setDatabaseName(DB_NAME);
            ds.setUser(USER);
            ds.setPassword(PASSWORD);

            // Only to measure the initial connection time
            long start = System.currentTimeMillis();
            try (Connection conn = ds.getConnection()) {
                long end = System.currentTimeMillis();
                initialConnectionTime = end - start;
            } catch (Exception e) {
                System.out.println("Error during first connection : " + e.getMessage());
            }
            return ds;
        });

    }

    /**
     * Private inner class to hold shared hit and read block counts.
     */
    private class Block {
        private final int sharedHit;
        private final int sharedRead;

        public Block(int sharedHit, int sharedRead) {
            this.sharedHit = sharedHit;
            this.sharedRead = sharedRead;
        }

        public int getSharedHit() {
            return sharedHit;
        }

        public int getSharedRead() {
            return sharedRead;
        }
    }

    /**
     * Returns the type of database managed by this manager.
     *
     * @return the database type, which is PostgreSQL in this case
     */
    protected PGSimpleDataSource getDataSource() {
        return dataSources.get(port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getInitialConnectionTime() {
        return initialConnectionTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result run(Query query, int nbExecutions, String scope, List<Index> indexes) {

        warmup(query, scope);

        System.out.println("Running " + getType().getName() + " query " + nbExecutions + " times in scope: " + scope + ": \n" + query.getQuery() );

        // Call to the benchmark query function that executes the query a certain number of times
        // and returns the average execution time, standard deviation, variance, 95th percentile, cache info, and explain plan.
        String benchmarkSQL = "SELECT * FROM benchmark_query3(?, ?);";

        // Get the data source for the specified port. Use try with resources to ensure the connection is closed properly.
        try (Connection conn = getDataSource().getConnection()) {

            // Set the schema to the specified scope
            conn.setSchema(scope);

            // Use a prepared statement to execute the benchmark query
            PreparedStatement pstmt = conn.prepareStatement(benchmarkSQL);
            pstmt.setInt(1, nbExecutions);
            pstmt.setString(2, query.getQuery());

            ResultSet rs = pstmt.executeQuery();
            double avgTimeMs = 0.0;
            double stddev = 0.0;
            double variance = 0.0;
            double percentile95 = 0.0;
            String explainPlan = "";

            while (rs.next()) {
                avgTimeMs = Double.parseDouble(df.format(Double.parseDouble(rs.getString("avg_time_ms"))).replace(',', '.'));
                stddev = Double.parseDouble(df.format(Double.parseDouble(rs.getString("stddev_time_ms"))).replace(',', '.'));
                variance = Double.parseDouble(df.format(Double.parseDouble(rs.getString("variance_time_ms"))).replace(',', '.'));
                percentile95 = Double.parseDouble(df.format(Double.parseDouble(rs.getString("percentile_95_ms"))).replace(',', '.'));
                explainPlan = rs.getString("explain_json");
                blocks = new Block(rs.getInt("total_shared_hit_blocks"), rs.getInt("total_shared_read_blocks"));
            }

            System.out.println("Query benchmarking completed. Average latency: " + avgTimeMs + " ms");

            return new Result(query.getQuery(), avgTimeMs, TPS(avgTimeMs), explainJsonParser.parsePlan(explainPlan).toIndentedString(3), initialConnectionTime,
                    stddev, variance, percentile95, getCacheInfo(nbExecutions));

        } catch (Exception e) {
            System.err.println("Error while running query " + e.getClass() + e.getMessage());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warmup(Query query, String scope) {
        System.out.println("Warming up " + getType().getName() + " query in scope: " + scope);

        String warmupSQL = "SELECT * FROM benchmark_query3(?, ?);";

        try (Connection conn = getDataSource().getConnection()) {
            conn.setSchema(scope);
            PreparedStatement pstmt = conn.prepareStatement(warmupSQL);
            pstmt.setInt(1, WARMUP_EXECUTIONS);
            pstmt.setString(2, query.getQuery());

            ResultSet rs = pstmt.executeQuery();
            double avgTimeMs = 0;
            while (rs.next()) {
                // Just consume the result to warm up the cache
                avgTimeMs = Double.parseDouble(df.format(Double.parseDouble(rs.getString("avg_time_ms"))).replace(',', '.'));
            }

            System.out.println("Warming up completed. Average latency: " + avgTimeMs + " ms");

        } catch (Exception e) {
            System.err.println("Error during warmup: " + e.getMessage());
        }
    }

    /**
     * Returns the SQL string to create an index for the specified index object.
     * This method must be implemented by subclasses to provide the specific SQL syntax
     * for creating indexes in PostgreSQL.
     *
     * @param index the index object containing the details of the index to be created
     * @return the SQL string to create the index
     */
    public abstract String getIndexCreationString(Index index);

    /**
     * {@inheritDoc}
     */
    @Override
    public void createIndexes(String scope, List<Index> indexes) {

        if (indexes == null || indexes.isEmpty()) {
            System.out.println("No indexes to create in scope: " + scope);
            return;
        }

        System.out.println("Creating indexes in scope: " + scope);

        // Get the data source for the specified port. Use try with resources to ensure the connection is closed properly.
        try (Connection conn = getDataSource().getConnection()) {
            conn.setSchema(scope);
            for (Index index : indexes) {

                try (PreparedStatement pstmt = conn.prepareStatement(getIndexCreationString(index))) {
                    // An enhancement would be to make sure no SQL injection is possible here
                    System.out.println("Creating index: " + pstmt.toString());
                    // Use executeUpdate to create the index because it modifies the database
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.err.println("Error creating index: " + e.getMessage());
                }
            }
            System.out.println(indexes.size() + " indexes created in scope: " + scope);

        } catch (Exception e) {
            System.err.println("Error creating indexes: " + e.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropIndexes(String scope, List<Index> indexes) {

        if (indexes == null || indexes.isEmpty()) {
            System.out.println("No indexes to drop in scope: " + scope);
            return;
        }

        System.out.println("Dropping indexes in scope: " + scope);


        // Get the data source for the specified port. Use try with resources to ensure the connection is closed properly.
        try (Connection conn = getDataSource().getConnection()) {
            conn.setSchema(scope);
            for (Index index : indexes) {
                String dropIndexSQL = String.format("DROP INDEX IF EXISTS %s", index.getName());

                try (PreparedStatement pstmt = conn.prepareStatement(dropIndexSQL)) {
                    // An enhancement would be to make sure no SQL injection is possible here
                    // Use executeUpdate to drop the index because it modifies the database
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.err.println("Error dropping index: " + e.getMessage());
                }
            }

            System.out.println(indexes.size() + " indexes dropped in scope: " + scope);

        } catch (Exception e) {
            System.err.println("Error dropping indexes: " + e.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IndexInfo> getIndexesInfo(String scope) {

        // SQL query to retrieve index information for the specified schema
        // - pg_stat_all_indexes: provides statistics about all indexes in the database
        // - pg_stat_all_tables: provides statistics about all tables in the database
        // - pg_relation_size: returns the size of a relation (table or index)
        // - pg_table_size: returns the size of a table, including all its indexes
        String indexInfoQuery = """
                SELECT
                    i.indexrelname AS index_name,
                    t.relname AS table_name,
                    pg_size_pretty(pg_relation_size(i.indexrelid)) AS index_size,
                    pg_size_pretty(pg_table_size(quote_ident(t.schemaname) || '.' || quote_ident(t.relname))) AS table_size,
                    ROUND((pg_relation_size(i.indexrelid)::numeric /
                           NULLIF(pg_table_size(quote_ident(t.schemaname) || '.' || quote_ident(t.relname)), 0)::numeric) * 100, 2)
                        AS index_ratio_percent
                FROM
                    pg_stat_all_indexes i
                        JOIN
                    pg_stat_all_tables t ON i.relid = t.relid
                WHERE
                    t.schemaname = ?
                ORDER BY
                    t.relname;
                """;

        List<IndexInfo> indexes = new ArrayList<>();

        // Get the data source for the specified port. Use try with resources to ensure the connection is closed properly.
        try (Connection conn = getDataSource().getConnection()) {
            conn.setSchema(scope);
            PreparedStatement pstmt = conn.prepareStatement(indexInfoQuery);

            pstmt.setString(1, scope);

            ResultSet rs = pstmt.executeQuery();
            String indexName = "";
            String tableName = "";
            String indexSize = "";
            String tableSize = "";
            double indexRatioPercent = 0.0;

            while (rs.next()) {
                indexName = rs.getString("index_name");
                tableName = rs.getString("table_name");
                indexSize = rs.getString("index_size");
                tableSize = rs.getString("table_size");
                indexRatioPercent = rs.getDouble("index_ratio_percent");

                indexes.add(new IndexInfo(indexName, tableName, tableSize, indexSize, indexRatioPercent));
            }

            return indexes;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheInfo getCacheInfo(int nbExecutions) {

        int sharedHit = blocks.getSharedHit() / nbExecutions;
        int sharedRead = blocks.getSharedRead() / nbExecutions;
        int hitsRatio = sharedHit > 0 ? (int) ((double) sharedHit / (sharedHit + sharedRead) * 100) : 0;

        return new CacheInfo(sharedHit, sharedRead, hitsRatio);
    }

}
