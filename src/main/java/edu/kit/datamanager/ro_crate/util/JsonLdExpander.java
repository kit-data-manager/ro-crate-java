package edu.kit.datamanager.ro_crate.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import java.util.*;

import java.io.File;

/**
 * Utility class for expanding and pruning JSON-LD documents.
 * <p>
 * This class provides functionality to resolve references in JSON-LD based on "@id" values,
 * expanding the data structure by replacing references with their full content.
 * It also handles circular references to prevent infinite recursion.
 *
 * @author jejkal
 */
public class JsonLdExpander {

    public static JsonNode expandAndPrune(File jsonLdFile) throws Exception {
        if (jsonLdFile == null) {
            throw new IllegalArgumentException("JSON-LD file must not be null.");
        }
        if (!jsonLdFile.exists() || !jsonLdFile.canRead()) {
            throw new IllegalArgumentException("JSON-LD file does not exist or cannot be read: " + jsonLdFile.getAbsolutePath());
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonLdFile);
        return expandAndPrune(root);
    }

    public static JsonNode expandAndPrune(JsonNode root) throws IllegalArgumentException {
        if (root == null) {
            throw new IllegalArgumentException("Root node must not be null.");
        }
        JsonNode graph = root.path("@graph");
        ObjectMapper mapper = new ObjectMapper();
        // Index all items by @id
        Map<String, JsonNode> idMap = new HashMap<>();
        for (JsonNode node : graph) {
            if (node.has("@id")) {
                idMap.put(node.path("@id").asText(), node);
            }
        }

        // Track which ids were directly referenced and expanded
        Set<String> expandedIds = new HashSet<>();

        ArrayNode expandedGraph = mapper.createArrayNode();
        for (JsonNode node : graph) {
            // Include only if it's NOT a referenced/expanded object
            if (node.has("@id") && expandedIds.contains(node.path("@id").asText())) {
                continue; // skip referenced/expanded nodes
            }

            JsonNode expandedNode = expandNode(node, idMap, expandedIds, mapper, new HashSet<>());
            expandedGraph.add(expandedNode);
        }

        // Rebuild root
        ObjectNode newRoot = mapper.createObjectNode();
        newRoot.set("@context", root.path("@context"));
        newRoot.set("@graph", expandedGraph);
        return newRoot;
    }

    private static JsonNode expandNode(JsonNode node, Map<String, JsonNode> idMap, Set<String> expandedIds, ObjectMapper mapper, Set<String> visited) {
        if (!node.isObject()) {
            return node;
        }

        ObjectNode result = mapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (value.isObject() && value.has("@id")) {
                String refId = value.path("@id").asText();
                if (!visited.contains(refId) && idMap.containsKey(refId)) {
                    visited.add(refId);
                    expandedIds.add(refId);
                    result.set(key, expandNode(idMap.getOrDefault(refId, NullNode.getInstance()), idMap, expandedIds, mapper, new HashSet<>(visited)));
                } else {
                    result.set(key, value);
                }
            } else if (value.isArray()) {
                ArrayNode newArray = mapper.createArrayNode();
                for (JsonNode element : value) {
                    if (element.isObject() && element.has("@id")) {
                        String refId = element.path("@id").asText();
                        if (!visited.contains(refId) && idMap.containsKey(refId)) {
                            visited.add(refId);
                            expandedIds.add(refId);
                            newArray.add(expandNode(idMap.getOrDefault(refId, NullNode.getInstance()), idMap, expandedIds, mapper, new HashSet<>(visited)));
                        } else {
                            newArray.add(element);
                        }
                    } else {
                        newArray.add(expandNode(element, idMap, expandedIds, mapper, visited));
                    }
                }
                result.set(key, newArray);
            } else if (value.isObject()) {
                result.set(key, expandNode(value, idMap, expandedIds, mapper, visited));
            } else {
                result.set(key, value);
            }
        }

        return result;
    }
}
