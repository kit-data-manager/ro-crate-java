package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;
import edu.kit.datamanager.ro_crate.reader.ZipReader;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;
import edu.kit.datamanager.ro_crate.writer.ZipWriter;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ZipReaderTest {

  @Test
  void testReadingBasicCrate(@TempDir Path temp) throws IOException {
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .build();

    Path zipPath = temp.resolve("result.zip");

    RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toString());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipReader());
    Crate res = roCrateFolderReader.readCrate(zipPath.toFile().getAbsolutePath());
    HelpFunctions.compareTwoCrateJson(roCrate, res);
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

    Path zipPath = temp.resolve("result.zip");

    RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toFile().getAbsolutePath());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipReader());
    Crate res = roCrateFolderReader.readCrate(zipPath.toFile().getAbsolutePath());

    HelpFunctions.compareTwoCrateJson(roCrate, res);
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

    Path zipPath = temp.resolve("result.zip");

    RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toString());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipReader());
    Crate res = roCrateFolderReader.readCrate(zipPath.toString());


    Path locationSource = temp.resolve("expected");
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());
    writer.save(roCrate, locationSource.toString());

    Path destinationDir = temp.resolve("result");
    FileUtils.forceMkdir(destinationDir.toFile());
    writer.save(res, destinationDir.toString());

    // that copies the directory locally to see its content
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

    Path zipPath = temp.resolve("result.zip");

    RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
    // save the content of the roCrate to the dest zip
    roCrateZipWriter.save(roCrate, zipPath.toString());

    RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipReader());
    Crate res = roCrateFolderReader.readCrate(zipPath.toFile().getAbsolutePath());


    Path locationSource = temp.resolve("expected");
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());
    writer.save(roCrate, locationSource.toString());


    Path newFile = temp.resolve("new_file");
    FileUtils.writeStringToFile(newFile.toFile(), "fkladjsl;fjasd;lfjda;lkf", Charset.defaultCharset());

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
