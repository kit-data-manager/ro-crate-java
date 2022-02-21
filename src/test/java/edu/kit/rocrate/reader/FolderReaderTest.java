package edu.kit.rocrate.reader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.kit.crate.IROCrate;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.reader.FolderReader;
import edu.kit.crate.reader.ROCrateReader;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;
import edu.kit.rocrate.HelpFunctions;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class FolderReaderTest {

  @Test
  void testReadingBasicCrate(@TempDir Path temp) throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .build();
    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new FolderReader());
    IROCrate res = roCrateFolderReader.readCrate(temp.toFile().toString());

    Path r = temp.resolve("output.txt");
    FileUtils.touch(r.toFile());
    FileUtils.writeStringToFile(r.toFile(), res.getJsonMetadata(), Charset.defaultCharset());
    assertTrue(FileUtils.contentEquals(f.toFile(), r.toFile()));
  }


  @Test
  void testWithFile(@TempDir Path temp) throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();

    Path f = temp.resolve("ro-crate-metadata.json");
    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new FolderReader());
    IROCrate res = roCrateFolderReader.readCrate(temp.toFile().toString());

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, res);
  }

  @Test
  void TestWithFileWithLocation(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("survey-responses-2019.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setLocation(file.toFile())
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    Path f = temp.resolve("ro-crate-metadata.json");

    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new FolderReader());

    IROCrate res = roCrateFolderReader.readCrate(locationSource.toFile().toString());

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());

    writer.save(res, destinationDir.toFile().toString());

    // that copies the directory locally to see its content
    //FileUtils.copyDirectory(locationSource.toFile(), new File("test"));
    assertTrue(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, res);
  }


  @Test
  void TestWithFileWithLocationAddEntity(@TempDir Path temp) throws IOException {
    Path file = temp.resolve("file.csv");
    FileUtils.writeStringToFile(file.toFile(), "fakecsv.1", Charset.defaultCharset());
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .setLocation(file.toFile())
                .build()
        )
        .build();
    Path locationSource = temp.resolve("src");
    FileUtils.forceMkdir(locationSource.toFile());
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());

    writer.save(roCrate, locationSource.toFile().toString());

    Path f = temp.resolve("ro-crate-metadata.json");

    FileUtils.touch(f.toFile());
    FileUtils.writeStringToFile(f.toFile(), roCrate.getJsonMetadata(), Charset.defaultCharset());

    ROCrateReader roCrateFolderReader = new ROCrateReader(new FolderReader());

    Path newFile = temp.resolve("new_file");
    FileUtils.writeStringToFile(newFile.toFile(), "fkladjsl;fjasd;lfjda;lkf", Charset.defaultCharset());

    IROCrate res = roCrateFolderReader.readCrate(locationSource.toFile().toString());
    res.addDataEntity(new FileEntity.FileEntityBuilder()
        .setId("new_file")
        .setEncodingFormat("setnew")
        .setLocation(newFile.toFile())
        .build(), true);

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());

    writer.save(res, destinationDir.toFile().toString());

    assertFalse(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
    HelpFunctions.compareTwoMetadataJsonNotEqual(roCrate, res);
  }
}
