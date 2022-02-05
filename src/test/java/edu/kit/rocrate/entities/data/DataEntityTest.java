package edu.kit.rocrate.entities.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.DataEntity.DataEntityBuilder;
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
public class DataEntityTest {

  @Test
  void testSerialization() throws IOException {
    DataEntity file = new DataEntityBuilder()
        .addType("File")
        .setId("https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf")
        .addProperty("name", "RO-Crate specification")
        .addProperty("encodingFormat", "application/pdf")
        .addProperty("url", "https://zenodo.org/record/3541888")
        .build();
    InputStream inputStream =
        FileEntityTest.class.getResourceAsStream("/json/entities/data/fileEntity.json");
    JsonNode expectedJson = MyObjectMapper.getMapper().readTree(inputStream);
    JsonNode node = MyObjectMapper.getMapper().convertValue(file, JsonNode.class);
    assertEquals(node, expectedJson);
  }
}
