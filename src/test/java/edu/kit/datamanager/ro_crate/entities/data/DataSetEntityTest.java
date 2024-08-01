package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.net.URI;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class DataSetEntityTest {

    @Test
    void testSimpleDirDeserialization() throws IOException {

        String id = "lots_of_little_files/";
        DataSetEntity dir = new DataSetEntity.DataSetBuilder()
                // this does not change anything, but it shows that the entity contains a physical file
                .addContent(Paths.get("invalid_file"), id)
                .addProperty("name", "Too many files")
                .addProperty("description",
                        "This directory contains many small files, that we're not going to describe in detail.")
                .build();

        assertEquals(id, dir.getId());
        HelpFunctions.compareEntityWithFile(dir, "/json/entities/data/directory.json");
    }

    /**
     * This is a test with a directory that is located on the web. It is
     * recommended that such a dir list all of its files in the hasPart property
     * https://www.researchobject.org/ro-crate/1.1/data-entities.html#directories-on-the-web-dataset-distributions
     */
    @Test
    void testDirWithHasPartDeserialization() throws IOException {

        String id = "second_content";
        DataEntity second_content = new DataEntity.DataEntityBuilder()
                .addContent(Paths.get("does_not_matter"), id)
                .addProperty("description", "This entity just describes one of the contents in the Dir")
                .build();

        // we can add to hasPart using directly the id, or passing the entity to it
        DataSetEntity dir = new DataSetEntity.DataSetBuilder()
                .addContent(URI.create("https://www.example.com/urltodir"))
                .addProperty("name", "Directory that is located on the web")
                .addToHasPart("first_content")
                .addToHasPart(second_content)
                .build();

        assertTrue(dir.hasInHasPart(id));
        HelpFunctions.compareEntityWithFile(dir, "/json/entities/data/directoryWeb.json");
    }

    @Test
    void testWebDir() {
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        ObjectNode propertyValue = objectMapper.createObjectNode();
        propertyValue.put("@id", "http://example.com/downloads/2020/lots_of_little_files.zip");

        DataSetEntity dir = new DataSetEntity.DataSetBuilder()
                .addContent(Paths.get("lots_of_little_files/"), "lots_of_little_files/")
                .addProperty("name", "Too many files")
                .addProperty("description", "This directory contains many small files, that we're not going to describe in detail.")
                .addProperty("distribution", propertyValue)
                .build();

        assertEquals(dir.getProperty("distribution"), propertyValue);

        DataEntity webDir = new DataEntity.DataEntityBuilder()
                .addContent(URI.create("http://example.com/downloads/2020/lots_of_little_files.zip"))
                .addType("DataDownload")
                .addProperty("encodingFormat", "application/zip")
                .addProperty("contentSize", "82818928")
                .build();
        
        assertNotNull(webDir);
    }
}
