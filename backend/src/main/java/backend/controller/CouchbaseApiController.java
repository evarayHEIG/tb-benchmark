package backend.controller;

import backend.http.HttpClient;
import backend.http.JavaHttpClient;
import backend.model.metadata.CouchbaseCacheStats;
import backend.model.metadata.CouchbaseIndexStats;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * The {@code CouchbaseApiController} class provides methods to interact with the Couchbase API
 * for retrieving index and cache statistics.
 * It uses a Java HTTP client to make requests to the Couchbase server.
 * The class supports basic authentication using a username and password.
 *
 * It is a bridge between the backend application and the Couchbase server, allowing for
 * retrieval of performance metrics and statistics related to Couchbase indexes and cache that cannot be obtained
 * through the Couchbase SDK.
 *
 * @author Eva Ray
 */
public class CouchbaseApiController {

    private final HttpClient httpClient;
    private final String host;
    private final String username;
    private final String password;
    private static final int API_PORT = 9102;
    private static final int MANAGEMENT_PORT = 8091;

    /**
     * Constructs a new {@code CouchbaseApiController} instance with the specified host, username, and password.
     *
     * @param host     the Couchbase server host (e.g., "couchbase://localhost")
     * @param username the username for authentication
     * @param password the password for authentication
     */
    public CouchbaseApiController(String host, String username, String password) {
        this.httpClient = new JavaHttpClient();
        this.host = host.replace("couchbase://", "");
        this.username = username;
        this.password = password;
    }

    /**
     * Retrieves index statistics for a specified Couchbase bucket.
     *
     * @param bucketName the name of the Couchbase bucket
     * @return a list of {@link CouchbaseIndexStats} containing index statistics
     * @throws Exception if an error occurs while retrieving or parsing the statistics
     */
    public List<CouchbaseIndexStats> getIndexStats(String bucketName) throws Exception {
        String url = "http://" + host + ":" + API_PORT + "/api/v1/stats/" + bucketName;

        Map<String, String> headers = new HashMap<>();
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.put("Authorization", "Basic " + encodedAuth);

        String response = httpClient.get(url, headers);

        return parseIndexStatsResponse(response);
    }

    /**
     * Retrieves cache statistics for a specified Couchbase bucket.
     *
     * @param bucketName the name of the Couchbase bucket
     * @return a {@link CouchbaseCacheStats} object containing cache statistics
     * @throws Exception if an error occurs while retrieving or parsing the statistics
     */
    public CouchbaseCacheStats getCacheStats(String bucketName) throws Exception {
        String url = "http://" + host + ":" + MANAGEMENT_PORT + "/pools/default/buckets/" + bucketName + "/stats";

        Map<String, String> headers = new HashMap<>();
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.put("Authorization", "Basic " + encodedAuth);

        String response = httpClient.get(url, headers);

        return parseCacheStatsResponse(response);
    }

    /**
     * Parses the JSON response from the Couchbase API to extract cache statistics. Since it is very minimalistic,
     * it is only a function and not a separate class like the other parsers of the application.
     *
     * LLMs have been used to help write this parser, but it has been manually verified and tested.
     *
     * @param jsonResponse the JSON response string from the Couchbase API
     * @return a {@link CouchbaseCacheStats} object containing the parsed cache statistics
     */
    private CouchbaseCacheStats parseCacheStatsResponse(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);

            // Access the samples
            JsonNode samplesNode = rootNode.path("op").path("samples");

            double[] residentItemsRate = extractDoubleArray(samplesNode, "ep_resident_items_rate");
            double[] cacheMissRate = extractDoubleArray(samplesNode, "ep_cache_miss_rate");
            double[] bgFetches = extractLongArray(samplesNode, "ep_bg_fetched");
            double[] ops = extractDoubleArray(samplesNode, "ops");

            CouchbaseCacheStats stats = new CouchbaseCacheStats(
                    residentItemsRate,
                    cacheMissRate,
                    bgFetches,
                    ops
            );

            return stats;

        } catch (Exception e) {
            System.err.println("Error parsing cache statistics: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract a double array from a JSON node.
     *
     * @param samplesNode the JSON node containing the samples
     * @param key the key to extract the double array from
     * @return a double array containing the values from the specified key
     */
    private double[] extractDoubleArray(JsonNode samplesNode, String key) {
        JsonNode arrayNode = samplesNode.path(key);
        if (arrayNode.isArray()) {
            double[] result = new double[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                result[i] = arrayNode.get(i).asDouble();
            }
            return result;
        }
        return new double[0];
    }

    /**
     * Extract a long array from a JSON node.
     *
     * @param samplesNode the JSON node containing the samples
     * @param key the key to extract the long array from
     * @return a double array containing the values from the specified key, converted to long
     */
    private double[] extractLongArray(JsonNode samplesNode, String key) {
        JsonNode arrayNode = samplesNode.path(key);
        if (arrayNode.isArray()) {
            double[] result = new double[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                result[i] = arrayNode.get(i).asLong();
            }
            return result;
        }
        return new double[0];
    }

    /**
     * Parses the JSON response from the Couchbase API to extract index statistics.
     * It handles different formats of index names and extracts relevant statistics. Since it is very minimalistic,
     * it is only a function and not a separate class like the other parsers of the application.
     *
     * LLMs have been used to help write this parser, but it has been manually verified and tested.
     *
     * @param jsonResponse the JSON response string from the Couchbase API
     * @return a list of {@link CouchbaseIndexStats} containing the parsed index statistics
     */
    private List<CouchbaseIndexStats> parseIndexStatsResponse(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);

            List<CouchbaseIndexStats> statsList = new ArrayList<>();

            // Iterate on all indexes in the response
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> indexEntry = fields.next();
                String fullIndexName = indexEntry.getKey();
                JsonNode indexStats = indexEntry.getValue();

                // Get the disk size in bytes, defaulting to 0 if not present
                long diskSizeBytes = indexStats.has("disk_size") ? indexStats.get("disk_size").asLong() : 0;

                // Ceate a CouchbaseIndexStats object
                CouchbaseIndexStats stats = getCouchbaseIndexStats(fullIndexName, diskSizeBytes);

                statsList.add(stats);
            }

            return statsList;

        } catch (Exception e) {
            System.err.println("Error parsing index statistics: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Creates a {@link CouchbaseIndexStats} object from the full index name and disk size.
     * It extracts the bucket, scope, collection, and index names from the full index name.
     *
     * @param fullIndexName the full index name in the format "bucket:scope:collection:index"
     * @param diskSizeBytes the disk size in bytes for the index
     * @return a {@link CouchbaseIndexStats} object containing the parsed information
     */
    private static CouchbaseIndexStats getCouchbaseIndexStats(String fullIndexName, long diskSizeBytes) {
        CouchbaseIndexStats stats;

        // Extraction of parts of the index name (bucket:scope:collection:index)
        String[] parts = fullIndexName.split(":");

        if (parts.length >= 4) {
            // Complete format: bucket:scope:collection:index
            stats = new CouchbaseIndexStats(parts[1], parts[2], parts[3], diskSizeBytes);
        } else if (parts.length == 3) {
            // Format without explicit collection name : bucket:scope:index
            stats = new CouchbaseIndexStats(parts[1], "_default", parts[2], diskSizeBytes);
        } else {
            // Fallback if format is unexpected
            stats = new CouchbaseIndexStats("unknown", "unknown", fullIndexName, diskSizeBytes);
        }
        return stats;
    }

    /**
     * Formats a byte size into a human-readable string.
     *
     * @param bytes the size in bytes
     * @return a formatted string representing the byte size
     */
    public String formatByteSize(long bytes) {
        final long KB = 1024;
        final long MB = KB * 1024;
        final long GB = MB * 1024;

        if (bytes < KB) {
            return bytes + " B";
        } else if (bytes < MB) {
            return String.format("%.2f KB", (double) bytes / KB);
        } else if (bytes < GB) {
            return String.format("%.2f MB", (double) bytes / MB);
        } else {
            return String.format("%.2f GB", (double) bytes / GB);
        }
    }

}
