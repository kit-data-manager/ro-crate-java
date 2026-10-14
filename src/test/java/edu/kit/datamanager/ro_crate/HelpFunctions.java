package edu.kit.datamanager.ro_crate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.special.JsonUtilFunctions;
import io.json.compare.JSONCompare;
import io.json.compare.JsonComparator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.opentest4j.AssertionFailedError;

public class HelpFunctions {

  public static void compareEntityWithFile(AbstractEntity entity, String string)
    throws IOException {
    InputStream inputStream = HelpFunctions.class.getResourceAsStream(string);
    JsonValue expectedJson = Json.parse(inputStream);
    JsonValue node = entity.toJson();
    //compare the size of the expected and actual node. Both nodes should have the same number of properties.
    assertEquals(expectedJson.size(), node.size());
    compare(expectedJson, node, true);
  }

  public static void compare(JsonValue node1, JsonValue node2, Boolean equals) {
    var comparator = new JsonComparator() {
      public boolean compareValues(Object expected, Object actual) {
        return expected.equals(actual);
      }

      public boolean compareFields(String expected, String actual) {
        return expected.equals(actual);
      }
    };

    // json-compare works on JSON documents; hand it the string form of the
    // neutral trees (produced by the active adapter)
    if (equals) {
      JSONCompare.assertMatches(node1.toString(), node2.toString(), comparator);
    } else {
      JSONCompare.assertNotMatches(
        node1.toString(),
        node2.toString(),
        comparator
      );
    }
  }

  public static void compareTwoMetadataJsonNotEqual(Crate crate1, Crate crate2)
    throws IOException {
    JsonValue node1 = Json.parse(crate1.getJsonMetadata());
    JsonValue node2 = Json.parse(crate2.getJsonMetadata());
    compare(node1, node2, false);
  }

  public static void compareTwoMetadataJsonNotEqual(
    Crate crate1,
    String jsonFileString
  ) throws IOException {
    InputStream inputStream = HelpFunctions.class.getResourceAsStream(
      jsonFileString
    );
    JsonValue node1 = Json.parse(crate1.getJsonMetadata());
    JsonValue node2 = JsonUtilFunctions.unwrapSingleArray(
      Json.parse(inputStream)
    );
    compare(node1, node2, false);
  }

  public static void compareTwoCrateJson(Crate crate1, Crate crate2)
    throws IOException {
    JsonValue node1 = Json.parse(crate1.getJsonMetadata());
    JsonValue node2 = Json.parse(crate2.getJsonMetadata());
    compare(node1, node2, true);
  }

  public static void prettyPrintJsonString(String minimalJsonMetadata) {
    try {
      JsonValue jsonNode = Json.parse(minimalJsonMetadata);
      System.out.println(Json.writePretty(jsonNode));
    } catch (IOException e) {
      throw new AssertionFailedError("Not able to process string as JSON!", e);
    }
  }

  public static void printAndAssertEquals(
    RoCrate crate,
    String pathToResource
  ) {
    // So you get something to see
    prettyPrintJsonString(crate.getJsonMetadata());
    // Compare with the example from the specification
    try {
      HelpFunctions.compareCrateJsonToFileInResources(crate, pathToResource);
    } catch (IOException e) {
      throw new AssertionFailedError("Missing resources file!", e);
    }
  }

  public static void compareCrateJsonToFileInResources(File file1, File file2)
    throws IOException {
    JsonValue node1 = JsonUtilFunctions.unwrapSingleArray(Json.parse(file1));
    JsonValue node2 = JsonUtilFunctions.unwrapSingleArray(Json.parse(file2));
    compare(node1, node2, true);
  }

  /**
   * Compares the JSON metadata of a Crate object with a JSON file in the resources directory.
   *
   * @param crate1        The Crate object to compare.
   * @param jsonFileString The path to the JSON file in the resources directory.
   * @throws IOException If an error occurs while reading the JSON file.
   */
  public static void compareCrateJsonToFileInResources(
    Crate crate1,
    String jsonFileString
  ) throws IOException {
    InputStream inputStream = HelpFunctions.class.getResourceAsStream(
      jsonFileString
    );
    JsonValue node1 = Json.parse(crate1.getJsonMetadata());
    JsonValue node2 = JsonUtilFunctions.unwrapSingleArray(
      Json.parse(inputStream)
    );
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
      if (
        filename.equals("ro-crate-preview.html") ||
        filename.equals("ro-crate-metadata.json")
      ) {
        if (!compareWithMe.containsKey(filename)) {
          return false;
        }
      } else if (
        !FileUtils.contentEqualsIgnoreEOL(
          testMe.get(filename),
          compareWithMe.get(filename),
          null
        )
      ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Prints the file tree of the given directory for debugging and understanding
   * a test more quickly.
   *
   * @param directoryToPrint the directory to print
   * @throws IOException if an error occurs while printing the file tree
   */
  @SuppressWarnings("resource")
  public static void printFileTree(Path directoryToPrint) throws IOException {
    // Print all files recursively in a tree structure for debugging
    System.out.printf(
      "Files in %s:%n",
      directoryToPrint.getFileName().toString()
    );
    Files.walk(directoryToPrint).forEach(path -> {
      if (!path.toAbsolutePath().equals(directoryToPrint.toAbsolutePath())) {
        int depth = path.relativize(directoryToPrint).getNameCount();
        String prefix = "  ".repeat(depth);
        System.out.printf("%s%s%s%n", prefix, "└── ", path.getFileName());
      }
    });
  }
}
