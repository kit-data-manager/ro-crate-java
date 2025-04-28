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
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    public static void compareCrateJsonToFileInResources(Crate crate1, String jsonFileString) throws IOException {
        InputStream inputStream = HelpFunctions.class.getResourceAsStream(
                jsonFileString);
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
        JsonNode node2 = JsonUtilFunctions.unwrapSingleArray(objectMapper.readTree(inputStream));
        compare(node1, node2, true);
    }

    /**
     * Compare two directories. The method will compare the content of the two and return true if they are equal.
     * 
     * @param dir1 the first directory
     * @param dir2 the second directory
     * @return true if the two directories are equal, false otherwise.
     * @throws IOException If something goes wrong with the file system.
     * 
     */
    //@Deprecated(since = "2.1.0", forRemoval = true)
    public static boolean compareTwoDir(File dir1, File dir2) throws IOException {
        // compare the content of the two directories
        Map<String, File> result_map = FileUtils.listFiles(dir1, null, true)
                .stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));

        Map<String, java.io.File> input_map = FileUtils.listFiles(dir2, null, true)
                .stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));;


        if (result_map.size() != input_map.size()) {
            return false;
        }
        for (String s : input_map.keySet()) {
            // we do that because the ro-crate-metadata.json can be differently formatted,
            // or the order of the entities may be different
            // the same holds for the html file
            if (s.equals("ro-crate-preview.html") || s.equals("ro-crate-metadata.json")) {
                if (!result_map.containsKey(s)) {
                    return false;
                }
            } else if (!FileUtils.contentEqualsIgnoreEOL(input_map.get(s), result_map.get(s), null)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Asserts that the two directories are equal. Throws an exception otherwise.
     *
     * @param originalDirectory the original directory to compare to
     * @param testingDirectory the testing directory to test
     * @throws IOException If something goes wrong with the file system.
     */
    public static void assertEqualDirectories(File originalDirectory, File testingDirectory) throws IOException, AssertionFailedError {
        assertTrue(originalDirectory.isDirectory(), "The original directory is not a directory.");
        assertTrue(testingDirectory.isDirectory(), "The testing directory is not a directory.");
        // compare the content of the two directories
        Map<String, File> referenceFiles = FileUtils.listFiles(originalDirectory, null, true)
                .stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));

        Map<String, java.io.File> compareThoseFiles = FileUtils.listFiles(testingDirectory, null, true)
                .stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));;

        for (String filename : compareThoseFiles.keySet()) {
            // we do that because the ro-crate-metadata.json can be differently formatted,
            // or the order of the entities may be different
            // the same holds for the html file
            if (filename.equals("ro-crate-preview.html") || filename.equals("ro-crate-metadata.json")) {
                if (!referenceFiles.containsKey(filename)) {
                    throw new AssertionFailedError("The file %s is not present in the reference directory.".formatted(filename));
                }
            } else if (!FileUtils.contentEqualsIgnoreEOL(compareThoseFiles.get(filename), referenceFiles.get(filename), null)) {
                throw new AssertionFailedError("The content of the file %s is not equal.".formatted(filename));
            }
        }

        referenceFiles.keySet().forEach(filename -> {
                    if (!compareThoseFiles.containsKey(filename)) {
                        throw new AssertionFailedError("The file %s is not present in the testing directory.".formatted(filename));
                    }
                });

        compareThoseFiles.keySet().forEach(filename -> {
                    if (!referenceFiles.containsKey(filename)) {
                        throw new AssertionFailedError("The file %s is not present in the reference directory.".formatted(filename));
                    }
                });

        if (referenceFiles.size() != compareThoseFiles.size()) {
            throw new AssertionFailedError("The number of files in the two directories is not equal.");
        }
    }
}
