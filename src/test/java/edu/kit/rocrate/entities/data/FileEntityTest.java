package edu.kit.rocrate.entities.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.entities.data.FileEntity.FileEntityBuilder;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class FileEntityTest {

  /**
   * The FileEntity class provides a few methods that help to create an entity
   * For any Data Entity (File, Dir, Workflow) it is also possible to just use the
   * DataEntity class (check DataEntityTest for examples) the only difference is it will be more
   * "work"
   * @throws IOException
   */
  @Test
  void testSerialization() throws IOException {
    FileEntity file = new FileEntityBuilder()
        .setId("https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf")
        .addProperty("name", "RO-Crate specification")
        .setEncodingFormat("application/pdf")
        .addProperty("url", "https://zenodo.org/record/3541888")
        .build();
    InputStream inputStream =
        FileEntityTest.class.getResourceAsStream("/json/entities/data/fileEntity.json");
    JsonNode expectedJson = MyObjectMapper.getMapper().readTree(inputStream);
    JsonNode node = MyObjectMapper.getMapper().convertValue(file, JsonNode.class);
    assertEquals(node, expectedJson);
  }
}
