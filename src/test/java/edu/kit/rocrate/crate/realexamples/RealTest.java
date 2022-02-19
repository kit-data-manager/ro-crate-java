package edu.kit.rocrate.crate.realexamples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.IROCrate;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.externalproviders.personprovider.ORCIDProvider;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.reader.FolderReader;
import edu.kit.crate.reader.ROCrateReader;
import edu.kit.crate.special.JsonHelpFunctions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ro.skyah.comparator.JSONCompare;
import ro.skyah.comparator.JsonComparator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;


public class RealTest {

  @Test
  void testWithParadisecExample(@TempDir Path temp) throws IOException {

    ObjectMapper mapper = MyObjectMapper.getMapper();

    ROCrateReader reader = new ROCrateReader(new FolderReader());
    IROCrate crate = reader.readCrate(RealTest.class.getResource("/crates/paradisec/test123").getPath());
    JsonNode has = mapper.readTree(crate.getJsonMetadata());

    InputStream inputStream =
        RealTest.class.getResourceAsStream("/crates/paradisec/test123/ro-crate-metadata.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = JsonHelpFunctions.unwrapSingleArray(objectMapper.readTree(inputStream));

    // new library that can take into account out of order arrays
    JSONCompare.assertEquals(expectedJson, has, new JsonComparator() {
      public boolean compareValues(Object expected, Object actual) {
        return expected.equals(actual);
      }

      public boolean compareFields(String expected, String actual) {
        return expected.equals(actual);
      }
    });

    Path newFile = temp.resolve("new_file.txt");

    FileUtils.writeStringToFile(newFile.toFile(), "blablablalblalblabla", Charset.defaultCharset());
    crate.addDataEntity(
        new DataEntity.DataEntityBuilder()
            .setId("new_file.txt")
            .setLocation(newFile.toAbsolutePath().toFile())
            .addProperty("description", "my new file that I added")
            .build()
        , true);

    ORCIDProvider orcidProvider = new ORCIDProvider();
    PersonEntity person = orcidProvider.getPerson("https://orcid.org/0000-0001-9842-9718");
    crate.addContextualEntity(person);

    ContextualEntity en = crate.getContextualEntityById("http://nla.gov.au/nla.party-593909");
    en.addIdProperty("custom", "new_file.txt");

    crate.deleteEntityById("new_file.txt");
    crate.deleteEntityById("https://orcid.org/0000-0001-9842-9718");
    JsonNode afterTwoDeletion = objectMapper.readTree(crate.getJsonMetadata());
    JSONCompare.assertEquals(expectedJson, afterTwoDeletion, new JsonComparator() {
      public boolean compareValues(Object expected, Object actual) {
        return expected.equals(actual);
      }

      public boolean compareFields(String expected, String actual) {
        return expected.equals(actual);
      }
    });
    //  uncomment if want to see crate in local folder structure
    /*  FolderWriter folderWriter = new FolderWriter();
      ROCrateWriter roCrateWriter = new ROCrateWriter(folderWriter);
      roCrateWriter.save(crate, "test");
     */
  }
}
