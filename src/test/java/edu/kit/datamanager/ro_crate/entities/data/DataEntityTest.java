package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity.DataEntityBuilder;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.net.MalformedURLException;

import java.net.URISyntaxException;
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

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
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

        });
        assertEquals("This Data Entity remote ID does not resolve to a valid URL.", exception.getMessage());

    }

    @Test
    void testUrlCheck() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            // create data entity with random ID that is not a remote URI
        // this should create a warning in the console
        new DataEntity.DataEntityBuilder()
                .addType("File")
                .setId("dfkjdfkj")
                .build();

        });

        assertEquals("This Data Entity remote ID does not resolve to a valid URL.", exception.getMessage());

        //this entity id is a valid URL so there should not be any console information
        DataEntity dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .setId("https://www.example.com/19")
                .build();

        assertNotNull(dataEntity);
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
        new DataEntity.DataEntityBuilder()
                    .addType("File")
                    .setSource(new File("/json/crate/results and diagrams.json"))
                    .build();

        });

        assertEquals("The ID is not correctly encoded.", exception.getMessage());
    }

    @Test
    void testEncodedUrl() throws MalformedURLException, URISyntaxException {
        //this entity id is a valid URL so there should not be any console information
        DataEntity dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .setId("https://github.com/kit-data-manager/ro-crate-java/issues/5")
                .build();

        assertEquals("https://github.com/kit-data-manager/ro-crate-java/issues/5", dataEntity.getId());

        URL url
                = HelpFunctions.class.getResource("/json/crate/simple2.json");
        assert url != null;
        // this data entity is not a remote one, so there should not be any messages
        dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .setSource(new File(url.getFile()))
                .build();

        assertNotNull(dataEntity);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DataEntity.DataEntityBuilder()
                    .addType("File")
                    .setId("/json/crate/Results%20and%20Diagrams/almost-50%25.json")
                    .build();

        });

        assertEquals("This Data Entity remote ID does not resolve to a valid URL.", exception.getMessage());

        dataEntity = new DataEntity.DataEntityBuilder()
                .addType("File")
                .setSource(new File("/json/crate/Results%20and%20Diagrams/almost-50%25.json"))
                .build();

        assertTrue(dataEntity.getTypes().contains("File"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            new DataEntity.DataEntityBuilder()
                    .addType("File")
                    .setSource(new File("/json/crate/Results and Diagrams/almost-50%.json"))
                    .build();

        });

        assertEquals("The ID is not correctly encoded.", exception.getMessage());
    }
}
