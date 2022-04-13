package edu.kit.crate.entities.data;

import java.io.File;
import java.io.IOException;

import edu.kit.crate.HelpFunctions;
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
        .setSource(new File("does_not_matter"))
        .build();

    FileEntity file2 = new FileEntity.FileEntityBuilder()
        .setId("file2_id")
        .addIdProperty("name", "dsklfajs")
        .addIdProperty("name", "11111111")
        .setSource(new File("does_not_matter"))
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

    HelpFunctions.compareEntityWithFile(rootDataEntity, "/json/entities/data/root.json");
  }
}
