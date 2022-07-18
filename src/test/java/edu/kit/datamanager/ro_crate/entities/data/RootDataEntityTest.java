package edu.kit.datamanager.ro_crate.entities.data;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import edu.kit.datamanager.ro_crate.HelpFunctions;

import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class RootDataEntityTest {


  @Test
  void testSerialization() throws IOException {
    String id1 = "file1_id";
    FileEntity file1 = new FileEntity.FileEntityBuilder()
        .setId(id1)
        .setSource(new File("does_not_matter"))
        .build();

    String id2 = "file2_id";
    FileEntity file2 = new FileEntity.FileEntityBuilder()
        .setId(id2)
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

    assertTrue(rootDataEntity.hasInHasPart(id1));
    assertTrue(rootDataEntity.hasInHasPart(id2));
    HelpFunctions.compareEntityWithFile(rootDataEntity, "/json/entities/data/root.json");
  }
}
