package edu.kit.rocrate.crate.realexamples;

import edu.kit.crate.IROCrate;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.externalproviders.personprovider.ORCIDProvider;
import edu.kit.crate.reader.FolderReader;
import edu.kit.crate.reader.ROCrateReader;
import edu.kit.rocrate.HelpFunctions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;


public class RealTest {

  @Test
  void testWithParadisecExample(@TempDir Path temp) throws IOException {

    ROCrateReader reader = new ROCrateReader(new FolderReader());
    IROCrate crate = reader.readCrate(RealTest.class.getResource("/crates/paradisec/test123").getPath());

    HelpFunctions.compareTwoMetadataJsonEqual(crate, "/crates/paradisec/test123/ro-crate-metadata.json");

    Path newFile = temp.resolve("new_file.txt");

    FileUtils.writeStringToFile(newFile.toFile(), "blablablalblalblabla", Charset.defaultCharset());
    crate.addDataEntity(
        new FileEntity.FileEntityBuilder()
            .setId("new_file.txt")
            .setLocation(newFile.toAbsolutePath().toFile())
            .addProperty("description", "my new file that I added")
            .build()
        , true);

    PersonEntity person = ORCIDProvider.getPerson("https://orcid.org/0000-0001-9842-9718");
    crate.addContextualEntity(person);

    ContextualEntity en = crate.getContextualEntityById("http://nla.gov.au/nla.party-593909");
    en.addIdProperty("custom", "new_file.txt");

    HelpFunctions.compareTwoMetadataJsonNotEqual(crate,"/crates/paradisec/test123/ro-crate-metadata.json");
    crate.deleteEntityById("new_file.txt");
    crate.deleteEntityById("https://orcid.org/0000-0001-9842-9718");
    HelpFunctions.compareTwoMetadataJsonEqual(crate, "/crates/paradisec/test123/ro-crate-metadata.json");
    //  uncomment if want to see crate in local folder structure
    /*  FolderWriter folderWriter = new FolderWriter();
      ROCrateWriter roCrateWriter = new ROCrateWriter(folderWriter);
      roCrateWriter.save(crate, "test");
     */
  }
}
