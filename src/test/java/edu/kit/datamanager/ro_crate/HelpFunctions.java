package edu.kit.datamanager.ro_crate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.special.JsonUtilFunctions;

import org.apache.commons.io.FileUtils;
import io.json.compare.JSONCompare;
import io.json.compare.JsonComparator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelpFunctions {

    public static void compareEntityWithFile(AbstractEntity entity, String string) throws IOException {
        InputStream inputStream
                = HelpFunctions.class.getResourceAsStream(string);
        JsonNode expectedJson = MyObjectMapper.getMapper().readTree(inputStream);
        JsonNode node = MyObjectMapper.getMapper().convertValue(entity, JsonNode.class);
        //compare the size of the expected and actual node. Both nodes should have the same number of properties.
        assertEquals(expectedJson.size(), node.size());
        compare(expectedJson, node, true);
    }
    
    public static void compare(JsonNode node1, JsonNode node2, Boolean equals) {
        var comparator = new JsonComparator() {
            public boolean compareValues(Object expected, Object actual) {
                return expected.equals(actual);
            }

            public boolean compareFields(String expected, String actual) {
                return expected.equals(actual);
            }
        };

        if (equals) {
            JSONCompare.assertMatches(node1, node2, comparator);
        } else {
            JSONCompare.assertNotMatches(node1, node2, comparator);
        }
    }

    public static void compareTwoMetadataJsonNotEqual(Crate crate1, Crate crate2) throws JsonProcessingException {
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
        JsonNode node2 = objectMapper.readTree(crate2.getJsonMetadata());
        compare(node1, node2, false);
    }

    public static void compareTwoMetadataJsonNotEqual(Crate crate1, String jsonFileString) throws IOException {
        InputStream inputStream = HelpFunctions.class.getResourceAsStream(
                jsonFileString);
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
        JsonNode node2 = JsonUtilFunctions.unwrapSingleArray(objectMapper.readTree(inputStream));
        compare(node1, node2, false);
    }

    public static void compareTwoCrateJson(Crate crate1, Crate crate2) throws JsonProcessingException {
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
        JsonNode node2 = objectMapper.readTree(crate2.getJsonMetadata());
        compare(node1, node2, true);
    }

    public static void compareCrateJsonToFileInResources(File file1, File file2) throws IOException {
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode node1 = JsonUtilFunctions.unwrapSingleArray(objectMapper.readTree(file1));
        JsonNode node2 = JsonUtilFunctions.unwrapSingleArray(objectMapper.readTree(file2));
        compare(node1, node2, true);
    }

    /**
     * Compares the JSON metadata of a Crate object with a JSON file in the resources directory.
     *
     * @param crate1        The Crate object to compare.
     * @param jsonFileString The path to the JSON file in the resources directory.
     * @throws IOException If an error occurs while reading the JSON file.
     */
    public static void compareCrateJsonToFileInResources(Crate crate1, String jsonFileString) throws IOException {
        InputStream inputStream = HelpFunctions.class.getResourceAsStream(
                jsonFileString);
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
        JsonNode node2 = JsonUtilFunctions.unwrapSingleArray(objectMapper.readTree(inputStream));
        compare(node1, node2, true);
    }

    public static boolean compareTwoDir(File dir1, File dir2) throws IOException {
        // compare the content of the two directories
        Map<String, File> compareWithMe = FileUtils.listFiles(dir1, null, true)
                .stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));

        Map<String, java.io.File> testMe = FileUtils.listFiles(dir2, null, true)
                .stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));


        if (compareWithMe.size() != testMe.size()) {
            return false;
        }
        for (String filename : testMe.keySet()) {
            // we do that because the ro-crate-metadata.json can be differently formatted,
            // or the order of the entities may be different
            // the same holds for the html file
            if (filename.equals("ro-crate-preview.html") || filename.equals("ro-crate-metadata.json")) {
                if (!compareWithMe.containsKey(filename)) {
                    return false;
                }
            } else if (!FileUtils.contentEqualsIgnoreEOL(testMe.get(filename), compareWithMe.get(filename), null)) {
                return false;
            }
        }
        return true;
    }
}
