package edu.kit.datamanager.ro_crate.entities.data;

import java.io.File;
import java.io.IOException;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;

import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class DataSetEntityTest {

  @Test
  void testSimpleDirDeserialization() throws IOException {

    DataSetEntity dir = new DataSetEntity.DataSetBuilder()
        .setId("lots_of_little_files/")
        // this does not change anything, but it shows that the entity contains a physical file
        .setSource(new File("invalid_file"))
        .addProperty("name", "Too many files")
        .addProperty("description",
            "This directory contains many small files, that we're not going to describe in detail.")
        .build();

    HelpFunctions.compareEntityWithFile(dir, "/json/entities/data/directory.json");
  }

  /**
   * This is a test with a directory that is located on the web. It is recommended that such a dir
   * list all of its files in the hasPart property https://www.researchobject.org/ro-crate/1.1/data-entities.html#directories-on-the-web-dataset-distributions
   */
  @Test
  void testDirWithHasPartDeserialization() throws IOException {

    DataEntity second_content = new DataEntity.DataEntityBuilder()
        .setId("second_content")
        .setSource(new File("does_not_matter"))
        .addProperty("description", "This entity just describes one of the contents in the Dir")
        .build();

    // we can add to hasPart using directly the id, or passing the entity to it
    DataSetEntity dir = new DataSetEntity.DataSetBuilder()
        .setId("https://www.example.com/urltodir")
        .addProperty("name", "Directory that is located on the web")
        .addToHasPart("first_content")
        .addToHasPart(second_content)
        .build();

    HelpFunctions.compareEntityWithFile(dir, "/json/entities/data/directoryWeb.json");
  }
}
