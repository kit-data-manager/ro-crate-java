package edu.kit.rocrate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.IROCrate;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.special.JsonUtilFunctions;
import org.apache.commons.io.FileUtils;
import ro.skyah.comparator.JSONCompare;
import ro.skyah.comparator.JsonComparator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HelpFunctions {

  public static void compareEntityWithFile(AbstractEntity entity, String string) throws IOException {
    InputStream inputStream =
        HelpFunctions.class.getResourceAsStream(string);
    JsonNode expectedJson = MyObjectMapper.getMapper().readTree(inputStream);
    JsonNode node = MyObjectMapper.getMapper().convertValue(entity, JsonNode.class);
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
      JSONCompare.assertEquals(node1, node2, comparator);
    } else {
      JSONCompare.assertNotEquals(node1, node2, comparator);
    }
  }

  public static void compareTwoMetadataJsonNotEqual(IROCrate crate1, IROCrate crate2) throws JsonProcessingException {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
    JsonNode node2 = objectMapper.readTree(crate2.getJsonMetadata());
    compare(node1, node2, false);
  }

  public static void compareTwoMetadataJsonNotEqual(IROCrate crate1, String jsonFileString) throws IOException {
    InputStream inputStream = HelpFunctions.class.getResourceAsStream(
        jsonFileString);
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
    JsonNode node2 = JsonUtilFunctions.unwrapSingleArray(objectMapper.readTree(inputStream));
    compare(node1, node2, false);
  }

  public static void compareCrateJsonToFileInResources(IROCrate crate1, IROCrate crate2) throws JsonProcessingException {
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

  public static void compareCrateJsonToFileInResources(IROCrate crate1, String jsonFileString) throws IOException {
    InputStream inputStream = HelpFunctions.class.getResourceAsStream(
        jsonFileString);
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode node1 = objectMapper.readTree(crate1.getJsonMetadata());
    JsonNode node2 = JsonUtilFunctions.unwrapSingleArray(objectMapper.readTree(inputStream));
    compare(node1, node2, true);
  }

  public static boolean compareTwoDir(File dir1, File dir2) throws IOException {
    // compare the content of the two directories
    List<File> a = (List<java.io.File>) FileUtils.listFiles(dir1, null, true);
    Map<String, File> result_map = a.stream()
        .collect(Collectors.toMap(java.io.File::getName, Function.identity()));

    List<java.io.File> b = (List<java.io.File>) FileUtils.listFiles(dir2, null, true);
    Map<String, java.io.File> input_map = b.stream()
        .collect(Collectors.toMap(java.io.File::getName, Function.identity()));

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
}
