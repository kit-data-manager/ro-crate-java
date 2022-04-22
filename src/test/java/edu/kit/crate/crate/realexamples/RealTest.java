package edu.kit.crate.crate.realexamples;

import edu.kit.crate.Crate;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.externalproviders.personprovider.OrcidProvider;
import edu.kit.crate.reader.FolderReader;
import edu.kit.crate.reader.RoCrateReader;
import edu.kit.crate.HelpFunctions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;


public class RealTest {

  @Test
  void testWithIDRCProject(@TempDir Path temp) throws IOException {

    RoCrateReader reader = new RoCrateReader(new FolderReader());
    final String locationMetadataFile = "/crates/other/idrc_project/ro-crate-metadata.json";
    Crate crate = reader.readCrate(RealTest.class.getResource("/crates/other/idrc_project").getPath());

    HelpFunctions.compareCrateJsonToFileInResources(crate, locationMetadataFile);

    Path newFile = temp.resolve("new_file.txt");

    FileUtils.writeStringToFile(newFile.toFile(), "blablablalblalblabla", Charset.defaultCharset());
    crate.addDataEntity(
        new FileEntity.FileEntityBuilder()
            .setId("new_file.txt")
            .setSource(newFile.toAbsolutePath().toFile())
            .addProperty("description", "my new file that I added")
            .build()
        , true);


    PersonEntity person = OrcidProvider.getPerson("https://orcid.org/0000-0001-9842-9718");
    crate.addContextualEntity(person);

    // problem
    ContextualEntity en = crate.getContextualEntityById("9a4e89e1-13bf-4d44-b5f7-ced40eb33cb2");
    en.addIdProperty("custom", "new_file.txt");

    HelpFunctions.compareTwoMetadataJsonNotEqual(crate,locationMetadataFile);
    crate.deleteEntityById("https://orcid.org/0000-0001-9842-9718");
    crate.deleteEntityById("new_file.txt");


    HelpFunctions.compareCrateJsonToFileInResources(crate, locationMetadataFile);
    //  uncomment if want to see crate in local folder structure
  }
}
