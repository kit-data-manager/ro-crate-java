package edu.kit.rocrate.entities.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class RootDataEntityTest {


  @Test
  void testSerialization() throws IOException {
    FileEntity file1 = new FileEntity.FileEntityBuilder()
        .setId("file1_id")
        .build();

    FileEntity file2 = new FileEntity.FileEntityBuilder()
        .setId("file2_id")
        .build();

    RootDataEntity rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
        .addProperty("identifier", "https://doi.org/10.4225/59/59672c09f4a4b")
        .addProperty("datePublished", "2000-02-01T00:00:00Z")
        .addProperty("name",
            "Data files associated with the manuscript:Effects of facilitated family case conferencing for ...")
        .addProperty("description",
            "Palliative care planning for nursing home residents with advanced dementia ...")
        .addToHasPart(file1)
        .addToHasPart(file2)
        .addAuthor("a1")
        .addAuthor("a2")
        .build();

    InputStream inputStream =
        RootDataEntity.class.getResourceAsStream("/json/entities/data/root.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.convertValue(rootDataEntity, JsonNode.class);
    assertEquals(node, expectedJson);
  }
}
