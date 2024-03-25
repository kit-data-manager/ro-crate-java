package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.PersonEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;

import edu.kit.datamanager.ro_crate.writer.FolderWriter;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class FolderReaderTest {

  private Path writeMetadataToFile(Path temp, RoCrate c1) throws IOException {
    // Write metadata to file
    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), c1.getJsonMetadata(), Charset.defaultCharset());
    return f;
  }

  @Test
  void testReadingBasicCrate(@TempDir Path temp) throws IOException {
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .build();
    Path f = writeMetadataToFile(temp, roCrate);
    // Read from written file
    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    RoCrate res = roCrateFolderReader.readCrate(temp.toFile().toString());
    // Write metadata again
    Path r = temp.resolve("output.txt");
    FileUtils.touch(r.toFile());
    FileUtils.writeStringToFile(r.toFile(), res.getJsonMetadata(), Charset.defaultCharset());
    // See if it is the same
    assertTrue(FileUtils.contentEquals(f.toFile(), r.toFile()));
  }

  @Test
  void testMultipleReads(@TempDir Path temp1, @TempDir Path temp2) throws IOException {
    String id = "https://orcid.org/0000-0001-6121-5409";
    PersonEntity person = new PersonEntity.PersonEntityBuilder()
        .addId(id)
        .setContactPoint("mailto:tim.luckett@uts.edu.au")
        .setAffiliation("https://ror.org/03f0f6041")
        .setFamilyName("Luckett")
        .setGivenName("Tim")
        .addProperty("name", "Tim Luckett")
        .build();
    RoCrate c1 = new RoCrate.RoCrateBuilder("mini", "test", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/").build();
    RoCrate c2 = new RoCrate.RoCrateBuilder("other", "with file", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addContextualEntity(person)
        .build();
    writeMetadataToFile(temp1, c1);
    writeMetadataToFile(temp2, c2);
    // some first checks...
    assertEquals(0, c1.getAllContextualEntities().size());
    assertEquals(1, c2.getAllContextualEntities().size());
    // read both with the same reader
    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    RoCrate c1_read = roCrateFolderReader.readCrate(temp1.toFile().toString());
    RoCrate c2_read = roCrateFolderReader.readCrate(temp2.toFile().toString());
    // check that the reference is not the same
    assertNotEquals(c1, c1_read);
    assertNotEquals(c2, c2_read);
    assertNotEquals(c1_read, c2_read);
    assertEquals(0, c1_read.getAllContextualEntities().size());
    assertEquals(1, c2_read.getAllContextualEntities().size());
    HelpFunctions.compareTwoMetadataJsonNotEqual(c1_read, c2_read);
  }

  @Test
  void testWithFile(@TempDir Path temp) throws IOException {
    Path cvs = temp.resolve("survey-responses-2019.csv");
    FileUtils.touch(cvs.toFile());
    FileUtils.writeStringToFile(cvs.toFile(), "fkdjaflkjfla", Charset.defaultCharset());

    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .addContent(cvs, cvs.toFile().getName())
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();

    assertEquals(1, roCrate.getAllDataEntities().size());

    writeMetadataToFile(temp, roCrate);

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    RoCrate res = roCrateFolderReader.readCrate(temp.toFile().toString());
    HelpFunctions.compareTwoCrateJson(roCrate, res);
  }

  @Test
  void testWithFileUrlEncoded(@TempDir Path temp) throws IOException {

    // get the std output redirected, so we can see if there is something written
    PrintStream standardOut = System.out;
    ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStreamCaptor));

    Path csv = temp.resolve("survey responses 2019.csv"); // This URL will be encoded because of whitespaces
    FileUtils.touch(csv.toFile());
    FileUtils.writeStringToFile(csv.toFile(), "fkdjaflkjfla", Charset.defaultCharset());

    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
            .addDataEntity(
                    new FileEntity.FileEntityBuilder()
                            .addContent(csv, csv.toFile().getName())
                            .addProperty("name", "Survey responses")
                            .addProperty("contentSize", "26452")
                            .addProperty("encodingFormat", "text/csv")
                            .build()
            )
            .build();

    writeMetadataToFile(temp, roCrate);

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    Crate res = roCrateFolderReader.readCrate(temp.toFile().toString());
    HelpFunctions.compareTwoCrateJson(roCrate, res);

    // Make sure we did not print any errors
    assertEquals("", outputStreamCaptor.toString().trim());
    System.setOut(standardOut);
  }

  @Test
  void TestWithFileWithLocation(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("survey-responses-2019.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .addContent(file, "survey-responses-2019.csv")
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    writeMetadataToFile(temp, roCrate);

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());

    Crate res = roCrateFolderReader.readCrate(locationSource.toFile().toString());

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());

    writer.save(res, destinationDir.toFile().toString());

    // that copies the directory locally to see its content
    //FileUtils.copyDirectory(locationSource.toFile(), new File("test"));
    assertTrue(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoCrateJson(roCrate, res);
  }

  @Test
  void TestWithFileWithLocationAddEntity(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("file.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .addContent(file, "survey-responses-2019.csv")
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    writeMetadataToFile(temp, roCrate);

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());

    Path newFile = temp.resolve("new_file");
    FileUtils.writeStringToFile(newFile.toFile(), "fkladjsl;fjasd;lfjda;lkf", Charset.defaultCharset());

    Crate res = roCrateFolderReader.readCrate(locationSource.toFile().toString());
    res.addDataEntity(new FileEntity.FileEntityBuilder()
        .setEncodingFormat("setnew")
        .addContent(newFile, "new_file")
        .build(), true);

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());

    writer.save(res, destinationDir.toFile().toString());

    assertFalse(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoMetadataJsonNotEqual(roCrate, res);
  }
}
