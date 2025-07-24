package backend.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of JsonParser for PostgreSQL's EXPLAIN (ANALYZE, FORMAT JSON).
 *
 * LLMs have been used to help write this parser, but it has been manually verified and tested.
 *
 * @author Eva Ray
 */
public class PostgresExplainJsonParser {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses a JSON string representing a PostgreSQL EXPLAIN (ANALYZE, FORMAT JSON) output
     * and returns the root PlanNode.
     *
     * @param json the JSON string to parse
     * @return the root PlanNode of the parsed execution plan
     * @throws Exception if parsing fails
     */
    public PlanNode parsePlan(String json) throws Exception {
        JsonNode root = mapper.readTree(json);
        JsonNode planRoot = root.get(0).get("Plan");
        return parsePlanNode(planRoot);
    }

    /**
     * Parses a JsonNode representing a PostgreSQL execution plan node.
     *
     * @param node the JsonNode to parse
     * @return a PlanNode representing the parsed execution plan node
     */
    public PlanNode parsePlanNode(JsonNode node) {
        String nodeType = node.has("Node Type") ? node.get("Node Type").asText() : "Unknown";
        double actualTotalTime = node.has("Actual Total Time") ? node.get("Actual Total Time").asDouble() : 0;
        double actualLoops = node.has("Actual Loops") ? node.get("Actual Loops").asDouble() : 1;
        int sharedHitBlocks = node.has("Shared Hit Blocks") ? node.get("Shared Hit Blocks").asInt() : 0;
        int sharedReadBlocks = node.has("Shared Read Blocks") ? node.get("Shared Read Blocks").asInt() : 0;
        long planRows = node.has("Plan Rows") ? node.get("Plan Rows").asLong() : 0;
        long actualRows = node.has("Actual Rows") ? node.get("Actual Rows").asLong() : 0;
        double totalCost = node.has("Total Cost") ? node.get("Total Cost").asDouble() : 0;
        String indexName = node.has("Index Name") ? node.get("Index Name").asText() : null;

        List<PlanNode> children = new ArrayList<>();
        if (node.has("Plans")) {
            for (JsonNode child : node.get("Plans")) {
                children.add(parsePlanNode(child));
            }
        }
        return new PlanNode(
                nodeType, actualTotalTime, actualLoops,
                sharedHitBlocks, sharedReadBlocks, planRows, actualRows,
                totalCost, indexName, children);
    }

    /**
     * Represents a node in the execution plan of a PostgreSQL query.
     * Contains information about the node type, execution time, rows processed,
     * and child nodes.
     */
    public record PlanNode(
            String nodeType,
            double actualTotalTime,
            double actualLoops,
            int sharedHitBlocks,
            int sharedReadBlocks,
            long planRows,
            long actualRows,
            double totalCost,
            String indexName,
            List<PlanNode> children) {

        /**
         * Compute the exclusive time for this node. The exclusive time is the time that this node
         * spent executing, minus the time spent in its children. By default, PostgreSQL counts the children's time.
         */
        public double getExclusiveTime() {
            double exclusive = (actualTotalTime);
            for (PlanNode child : children) {
                exclusive -= child.actualTotalTime;
            }
            return exclusive;
        }

        /**
         * Return a string representation of this PlanNode with indentation.
         *
         * @param indent the level of indentation for this node
         */
        public String toIndentedString(int indent) {
            StringBuilder sb = new StringBuilder();
            String prefix = "  ".repeat(indent);

            sb.append(String.format("%s%s - %.3f ms (exclusive: %.3f ms)",
                    prefix, nodeType, actualTotalTime, getExclusiveTime()));

            sb.append(String.format(" [rows: %d/%d]", planRows, actualRows));

            if (sharedHitBlocks > 0 || sharedReadBlocks > 0) {
                sb.append(String.format(" [blocks: hit=%d, read=%d]", sharedHitBlocks, sharedReadBlocks));
            }

            sb.append(String.format(" [cost: %.2f]", totalCost));

            if (indexName != null && !indexName.isEmpty()) {
                sb.append(String.format(" [index: %s]", indexName));
            }

            sb.append("\n");

            for (PlanNode child : children) {
                sb.append(child.toIndentedString(indent + 1));
            }
            return sb.toString();
        }
    }
}