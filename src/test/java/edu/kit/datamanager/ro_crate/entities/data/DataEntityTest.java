package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URL;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity.DataEntityBuilder;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.net.MalformedURLException;
import java.net.URI;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class DataEntityTest {

    @Test
    void testSerialization() throws IOException {
        ObjectMapper objectMapper = MyObjectMapper.getMapper();

        ObjectNode bigJson = objectMapper.createObjectNode();
        bigJson.put("onefield", "dalkfa");
        ObjectNode address = objectMapper.createObjectNode();
        address.put("street", "x");
        address.put("city", "other");
        address.put("state", "this");
        address.put("zipCode", 66439);
        bigJson.set("nestedObject", address);

        DataEntity file = new DataEntityBuilder()
                .addType("File")
                .setId("https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf")
                .addProperty("name", "RO-Crate specification")
                .addProperty("encodingFormat", "application/pdf")
                .addProperty("url", "https://zenodo.org/record/3541888")
                // this should not be included since it does not follow the json flatten form of the RO-Crate
                .addProperty("test", bigJson)
                .build();
        assertNull(file.getProperty("test"));
        HelpFunctions.compareEntityWithFile(file, "/json/entities/data/fileEntity.json");
    }

    @Test
    void testSerializationOutside() throws IOException {
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        ObjectNode json = objectMapper.createObjectNode();
        json.put("name", "RO-Crate specification");
        json.put("encodingFormat", "application/pdf");
        json.put("url", "https://zenodo.org/record/3541888");
        json.put("@id", "https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf");
        json.put("@type", "File");

        DataEntity data = new DataEntity.DataEntityBuilder().build();
        data.setProperties(json);
        HelpFunctions.compareEntityWithFile(data, "/json/entities/data/fileEntity.json");

        ObjectNode address = objectMapper.createObjectNode();
        address.put("street", "x");
        address.put("city", "other");
        address.put("state", "this");
        address.put("zipCode", 66439);
        json.set("nestedObject", address);
        // this should not be included into the entity since it does not follow the basic RO-Crate structure
        data.setProperties(json);
        assertNotEquals("", data.getId());
        HelpFunctions.compareEntityWithFile(data, "/json/entities/data/fileEntity.json");
    }

    @Test
    void testUriCheck() {
        //this entity id is a valid URL so there should not be any console information
        DataEntity dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .addContent(URI.create("https://www.example.com/19"))
                .build();

        assertNotNull(dataEntity);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DataEntity.DataEntityBuilder()
                    .addType("File")
                    .addContent(URI.create("zzz://wrong/url"))
                    .build();
        });

        assertEquals("This Data Entity remote ID does not resolve to a valid URL.", exception.getMessage());

        assertNotNull(dataEntity);

    }

    @Test
    void testPathCheck() throws IOException {
        FileEntity dataEntity = new FileEntity.FileEntityBuilder()
                .addContent(Paths.get("example.json"), "example.json")
                .addProperty("name", "RO-Crate specification")
                .setEncodingFormat("application/json")
                .build();
        assertEquals(dataEntity.getId(), "example.json");
        HelpFunctions.compareEntityWithFile(dataEntity, "/json/entities/data/localFile.json");

        dataEntity = new FileEntity.FileEntityBuilder()
                .addContent(Paths.get("cp7glop.ai"), "cp7glop.ai")
                .addProperty("name", "Diagram showing trend to increase")
                .setEncodingFormat("application/pdf")
                .build();
        assertEquals(dataEntity.getId(), "cp7glop.ai");
        assertEquals(dataEntity.getContent(), Paths.get("cp7glop.ai"));
        assertEquals(dataEntity.getProperty("encodingFormat").asText(), "application/pdf");
    }

    @Test
    void testEncodedUrl() throws MalformedURLException, URISyntaxException {
        //this entity id is a valid URL so there should not be any console information
        DataEntity dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .addContent(URI.create("https://github.com/kit-data-manager/ro-crate-java/issues/5"))
                .build();

        assertEquals("https://github.com/kit-data-manager/ro-crate-java/issues/5", dataEntity.getId());

        URL url
                = HelpFunctions.class.getResource("/json/crate/simple2.json");
        assert url != null;
        dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .addContent(Paths.get(url.toURI()), "simple2.json")
                .build();

        assertNotNull(dataEntity);

        dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .addContent(Paths.get("/json/crate/Results%20and%20Diagrams/almost-50%25.json"), "almost-50%25.json")
                .build();

        assertTrue(dataEntity.getTypes().contains("File"));
        assertEquals(dataEntity.getId(), "almost-50%25.json");

        // even if the Path is not correctly encoded, the data entity will be added with an encoded Path.
        dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .addContent(Paths.get("/json/crate/Results and Diagrams/almost-50%.json"), "almost-50%.json")
                .build();
        assertEquals(dataEntity.getId(), "almost-50%25.json");

    }

    @Test
    void testRemoveProperty() {
        FileEntity dataEntity = new FileEntity.FileEntityBuilder()
                .addProperty("name", "RO-Crate specification")
                .setEncodingFormat("application/json")
                .addProperty("url", "https://zenodo.org/record/3541888")
                .addProperty("@id", "https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf")
                .build();

        dataEntity.removeProperty("url");
        assertNull(dataEntity.getProperty("url"));
        assertEquals(dataEntity.getProperties().size(), 4);

        List<String> keyList = Arrays.asList("encodingFormat", "name");
        dataEntity.removeProperties(keyList);
        assertEquals(dataEntity.getProperties().size(), 2);
    }
}
