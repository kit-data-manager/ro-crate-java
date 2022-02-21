package edu.kit.rocrate.entities.data;

import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.DataEntity.DataEntityBuilder;
import java.io.IOException;

import edu.kit.rocrate.HelpFunctions;
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

    HelpFunctions.compareEntityWithFile(file, "/json/entities/data/fileEntity.json");
  }
}
