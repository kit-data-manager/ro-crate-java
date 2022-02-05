package edu.kit.rocrate.entities.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.entities.data.WorkflowEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class WorkflowEntityTest {

  @Test
  void testSerialization() throws IOException {


    WorkflowEntity entity = new WorkflowEntity.WorkflowEntityBuilder()
        .setId("workflow/alignment.knime")
        .addIdProperty("conformsTo", "https://bioschemas.org/profiles/ComputationalWorkflow/0.5-DRAFT-2020_07_21/")
        .addProperty("name", "Sequence alignment workflow")
        .addIdProperty("programmingLanguage", "#knime")
        .addAuthor("#alice")
        .addProperty("dateCreated", "2020-05-23")
        .setLicense("https://spdx.org/licenses/CC-BY-NC-SA-4.0")
        .addInput("#36aadbd4-4a2d-4e33-83b4-0cbf6a6a8c5b")
        .addOutput("#6c703fee-6af7-4fdb-a57d-9e8bc4486044")
        .addOutput("#2f32b861-e43c-401f-8c42-04fd84273bdf")
        .addProperty("url", "http://example.com/workflows/alignment")
        .addProperty("version", "0.5.0")
        .addIdProperty("sdPublisher", "#workflow-hub")
        .build();

    InputStream inputStream =
        WorkflowEntityTest.class.getResourceAsStream("/json/entities/data/workflow.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.convertValue(entity, JsonNode.class);

    assertEquals(node, expectedJson);
  }
}
