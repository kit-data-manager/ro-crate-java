/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.kit.datamanager.ro_crate.util;

/**
 *
 * @author jejkal
 */
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import java.util.*;

import java.io.File;

public class JsonLdExpander {

    public static JsonNode expandAndPrune(File jsonLdFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonLdFile);
        return expandAndPrune(root);
    }

    public static JsonNode expandAndPrune(JsonNode root) throws Exception {
        ArrayNode graph = (ArrayNode) root.get("@graph");
        ObjectMapper mapper = new ObjectMapper();
        // Index all items by @id
        Map<String, JsonNode> idMap = new HashMap<>();
        for (JsonNode node : graph) {
            if (node.has("@id")) {
                idMap.put(node.get("@id").asText(), node);
            }
        }

        // Track which ids were directly referenced and expanded
        Set<String> expandedIds = new HashSet<>();

        ArrayNode expandedGraph = mapper.createArrayNode();
        for (JsonNode node : graph) {
            // Include only if it's NOT a referenced/expanded object
            if (node.has("@id") && expandedIds.contains(node.get("@id").asText())) {
                continue; // skip referenced/expanded nodes
            }

            JsonNode expandedNode = expandNode(node, idMap, expandedIds, mapper, new HashSet<>());
            expandedGraph.add(expandedNode);
        }

        // Rebuild root
        ObjectNode newRoot = mapper.createObjectNode();
        newRoot.set("@context", root.get("@context"));
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
                String refId = value.get("@id").asText();
                if (!visited.contains(refId) && idMap.containsKey(refId)) {
                    visited.add(refId);
                    expandedIds.add(refId);
                    result.set(key, expandNode(idMap.get(refId), idMap, expandedIds, mapper, new HashSet<>(visited)));
                } else {
                    result.set(key, value);
                }
            } else if (value.isArray()) {
                ArrayNode newArray = mapper.createArrayNode();
                for (JsonNode element : value) {
                    if (element.isObject() && element.has("@id")) {
                        String refId = element.get("@id").asText();
                        if (!visited.contains(refId) && idMap.containsKey(refId)) {
                            visited.add(refId);
                            expandedIds.add(refId);
                            newArray.add(expandNode(idMap.get(refId), idMap, expandedIds, mapper, new HashSet<>(visited)));
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

    // Example usage
    public static void main(String[] args) throws Exception {
        File file = new File("E:\\Software\\NetbeansProjects\\ro-crate-java\\src\\test\\resources\\crates\\workflowhub\\workflow1\\ro-crate-metadata.json");
        JsonNode expanded = expandAndPrune(file);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, expanded);
    }
}
