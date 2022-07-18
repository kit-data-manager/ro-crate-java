package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.reader.FolderReader;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

  @Test
  void testReadingBasicCrate(@TempDir Path temp) throws IOException {
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .build();
    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    Crate res = roCrateFolderReader.readCrate(temp.toFile().toString());

    Path r = temp.resolve("output.txt");
    FileUtils.touch(r.toFile());
    FileUtils.writeStringToFile(r.toFile(), res.getJsonMetadata(), Charset.defaultCharset());
    assertTrue(FileUtils.contentEquals(f.toFile(), r.toFile()));
  }


  @Test
  void testWithFile(@TempDir Path temp) throws IOException {
    Path cvs = temp.resolve("survey-responses-2019.csv");
    FileUtils.touch(cvs.toFile());
    FileUtils.writeStringToFile(cvs.toFile(), "fkdjaflkjfla", Charset.defaultCharset());

    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setSource(cvs.toFile())
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();

    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    Crate res = roCrateFolderReader.readCrate(temp.toFile().toString());
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

    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
            .addDataEntity(
                    new FileEntity.FileEntityBuilder()
                            .setSource(csv.toFile())
                            .addProperty("name", "Survey responses")
                            .addProperty("contentSize", "26452")
                            .addProperty("encodingFormat", "text/csv")
                            .build()
            )
            .build();

    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
    Crate res = roCrateFolderReader.readCrate(temp.toFile().toString());
    HelpFunctions.compareTwoCrateJson(roCrate, res);

    // Make sure we did not print any errors
    assertEquals(outputStreamCaptor.toString().trim(), "");
    System.setOut(standardOut);
  }

  @Test
  void TestWithFileWithLocation(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("survey-responses-2019.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setSource(file.toFile())
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    Path f = temp.resolve("ro-crate-metadata.json");

    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

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
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setSource(file.toFile())
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    Path f = temp.resolve("ro-crate-metadata.json");

    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());

    Path newFile = temp.resolve("new_file");
    FileUtils.writeStringToFile(newFile.toFile(), "fkladjsl;fjasd;lfjda;lkf", Charset.defaultCharset());

    Crate res = roCrateFolderReader.readCrate(locationSource.toFile().toString());
    res.addDataEntity(new FileEntity.FileEntityBuilder()
        .setId("new_file")
        .setEncodingFormat("setnew")
        .setSource(newFile.toFile())
        .build(), true);

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());

    writer.save(res, destinationDir.toFile().toString());

    assertFalse(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoMetadataJsonNotEqual(roCrate, res);
  }
}
