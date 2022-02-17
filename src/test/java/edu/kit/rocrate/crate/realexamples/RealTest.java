package edu.kit.rocrate.crate.realexamples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.IROCrate;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.externalproviders.personprovider.ORCIDProvider;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.reader.FolderReader;
import edu.kit.crate.reader.ROCrateReader;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ro.skyah.comparator.CompareMode;
import ro.skyah.comparator.JSONCompare;

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
    JsonNode expectedJson = objectMapper.readTree(inputStream);

    // new library that can take into account out of order arrays
    JSONCompare.assertNotEquals(expectedJson, has, CompareMode.JSON_ARRAY_NON_EXTENSIBLE);

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

    crate.deleteEntityById("new_file.txt");
    FolderWriter folderWriter = new FolderWriter();
    ROCrateWriter roCrateWriter = new ROCrateWriter(folderWriter);
    roCrateWriter.save(crate, "test");
  }
}
