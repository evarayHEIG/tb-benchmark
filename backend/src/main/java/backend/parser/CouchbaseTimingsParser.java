package backend.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parser for Couchbase profile timings (N1QL).
 * Extracts the execution tree with node name, execution time, used indexes, and its children.
 *
 * LLMs have been used to help write this parser, but it has been manually verified and tested.
 *
 * @author Eva Ray
 */
public class CouchbaseTimingsParser {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse the Couchbase profile timings from a JSON string.
     * This method expects the JSON to contain execution timings, which can be found either at the root level or under "executionTimings".
     *
     * @param json the JSON string containing the Couchbase profile timings
     * @return a ProfileNode representing the root of the execution tree
     * @throws Exception if there is an error parsing the JSON
     */
    public ProfileNode parseProfile(String json) throws Exception {
        JsonNode root = mapper.readTree(json);
        // The profile is in executionTimings or at the root for the main operator
        JsonNode timings = root.has("executionTimings") ? root.get("executionTimings") : root;
        // Usually, the root node is an Authorize, or it has a ~child node
        JsonNode mainNode = timings;
        if (timings.has("~child")) {
            mainNode = timings.get("~child");
        }
        return parseProfileNode(mainNode);
    }

    /**
     * Parse a Couchbase profile node from a JsonNode.
     */
    public ProfileNode parseProfileNode(JsonNode node) {
        if (node == null || !node.has("#operator")) {
            return null;
        }
        String operator = node.get("#operator").asText();

        // Execution time and service time
        String execTime = node.has("#stats") && node.get("#stats").has("execTime")
                ? node.get("#stats").get("execTime").asText()
                : null;

        String servTime = node.has("#stats") && node.get("#stats").has("servTime")
                ? node.get("#stats").get("servTime").asText()
                : null;

        // Number of items in and out
        Long itemsIn = node.has("#stats") && node.get("#stats").has("#itemsIn")
                ? node.get("#stats").get("#itemsIn").asLong()
                : null;

        Long itemsOut = node.has("#stats") && node.get("#stats").has("#itemsOut")
                ? node.get("#stats").get("#itemsOut").asLong()
                : null;

        // Index used
        String index = node.has("index") ? node.get("index").asText() : null;
        String using = node.has("using") ? node.get("using").asText() : null;

        List<ProfileNode> children = new ArrayList<>();

        // Couchbase profile timings : ~children (liste) or ~child (unique)
        if (node.has("~children")) {
            for (JsonNode child : node.get("~children")) {
                ProfileNode childNode = parseProfileNode(child);
                if (childNode != null) {
                    children.add(childNode);
                }
            }
        }
        if (node.has("~child")) {
            ProfileNode childNode = parseProfileNode(node.get("~child"));
            if (childNode != null) {
                children.add(childNode);
            }
        }

        // Add child nodes that are not in ~child/~children
        for (String fieldName : List.of("scan", "input", "subquery", "expr", "plan")) {
            if (node.has(fieldName)) {
                JsonNode possibleChild = node.get(fieldName);
                if (possibleChild.has("#operator")) {
                    ProfileNode childNode = parseProfileNode(possibleChild);
                    if (childNode != null) {
                        children.add(childNode);
                    }
                }
            }
        }

        // Generic traversal: all sub-fields with a #operator and not already processed
        // This is to catch any additional operators that might not be in the expected fields
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String field = it.next();
            JsonNode value = node.get(field);
            if (value.isObject() && value.has("#operator")) {
                ProfileNode childNode = parseProfileNode(value);
                if (childNode != null && !children.contains(childNode)) {
                    children.add(childNode);
                }
            }
        }
        return new ProfileNode(operator, execTime, servTime, itemsIn, itemsOut, index, using, children);
    }

    /**
     * Record representing a node in the Couchbase profile execution tree.
     */
    public record ProfileNode(
            String operator,
            String execTime,
            String servTime,
            Long itemsIn,
            Long itemsOut,
            String index,
            String using,
            List<ProfileNode> children
    ) {
        /**
         * Add execTime and servTime, convert to milliseconds.
         *
         * @param duration the duration string (e.g., "4.573ms", "2s", "1.5µs")
         */
        private static double parseDurationToMs(String duration) {
            if (duration == null) return 0.0;
            duration = duration.trim();
            try {
                if (duration.endsWith("ms")) {
                    return Double.parseDouble(duration.replace("ms", ""));
                } else if (duration.endsWith("s")) {
                    // caution: test "ms" before "s"!
                    return Double.parseDouble(duration.replace("s", "")) * 1000.0;
                } else if (duration.endsWith("µs") || duration.endsWith("us")) {
                    return Double.parseDouble(duration.replace("µs", "").replace("us", "")) / 1000.0;
                } else if (duration.endsWith("µ") || duration.endsWith("u")) {
                    return Double.parseDouble(duration.replace("µ", "").replace("u", "")) / 1000.0;
                } else if (duration.endsWith("ns")) {
                    return Double.parseDouble(duration.replace("ns", "")) / 1_000_000.0;
                }
            } catch (NumberFormatException e) {
                return 0.0;
            }
            return 0.0;
        }

        /**
         * Create a string representation of the node with indentation. It containes the operator name,
         * total execution time, number of items in and out, and index if available.
         * @param indent the level of indentation
         */
        public String toIndentedString(int indent) {
            StringBuilder sb = new StringBuilder();

            // If the operator is "Sequence", we ignore its display and just show its children at the same level since
            // it is just an indication of the order of execution.
            if ("Sequence".equals(operator)) {
                for (ProfileNode child : children) {
                    sb.append(child.toIndentedString(indent));
                }
                return sb.toString();
            }

            String prefix = "  ".repeat(indent);
            double exec = parseDurationToMs(execTime);
            double serv = parseDurationToMs(servTime);
            double total = exec + serv;

            sb.append(prefix).append(operator);
            sb.append(String.format(" - %.3f ms", total));

            // Items
            if (itemsIn != null && itemsOut != null) {
                sb.append(String.format(" [in: %d, out: %d]", itemsIn, itemsOut));
            } else if (itemsIn != null) {
                sb.append(String.format(" [in: %d]", itemsIn));
            } else if (itemsOut != null) {
                sb.append(String.format(" [out: %d]", itemsOut));
            }

            // Index
            if (index != null) sb.append(" [index: ").append(index).append("]");

            sb.append("\n");

            for (ProfileNode child : children) {
                sb.append(child.toIndentedString(indent + 1));
            }

            return sb.toString();
        }

    }
}