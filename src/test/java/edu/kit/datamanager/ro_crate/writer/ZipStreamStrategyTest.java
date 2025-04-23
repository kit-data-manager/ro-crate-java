package edu.kit.datamanager.ro_crate.writer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.preview.AutomaticPreview;
import edu.kit.datamanager.ro_crate.preview.PreviewGenerator;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author jejkal
 */
class ZipStreamStrategyTest {

  @Test
  void testWritingToZipStream(@TempDir Path tempDir) throws IOException {
    // create the RO_crate directory in the tempDir
    Path roDir = tempDir.resolve("ro_dir");
    FileUtils.forceMkdir(roDir.toFile());

    // the .json of our crate
    InputStream fileJson=
        ZipStreamStrategyTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

    // fill the expected directory with files and dirs

    Path json = roDir.resolve("ro-crate-metadata.json");
    FileUtils.copyInputStreamToFile(fileJson, json.toFile());

    PreviewGenerator.generatePreview(roDir.toFile().getAbsolutePath());

    Path file1 = roDir.resolve("cp7glop.ai");
    FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
    Path dirInCrate = roDir.resolve("dir");
    FileUtils.forceMkdir(dirInCrate.toFile());
    FileUtils.writeStringToFile(dirInCrate.resolve("first.txt").toFile(),
        "content of first file in dir", Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("second.txt").toFile(),
        "content of second file in dir",
        Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("third.txt").toFile(),
        "content of third file in dir",
        Charset.defaultCharset());
    // create the RO_Crate including the files that should be present in it
    RoCrate roCrate = new RoCrate.RoCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .setLocationWithExceptions(file1)
                .setId("cp7glop.ai")
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .addProperty("name", "Too many files")
                .addProperty("description",
                    "This directory contains many small files, that we're not going to describe in detail.")
                .setLocationWithExceptions(dirInCrate)
                .setId("lots_of_little_files/")
                .build()
        )
        .setPreview(new AutomaticPreview())
        .build();

    // create a Writer for writing RoCrates to zip
    CrateWriter<OutputStream> writer = Writers.newZipStreamWriter();
    // write into destination path
    Path destination_path = tempDir.resolve("test.zip");
    OutputStream destination = new FileOutputStream(destination_path.toFile());
    writer.save(roCrate, destination);

    // extract and compare
    Path res = tempDir.resolve("dest");
    try (ZipFile zf = new ZipFile(destination_path.toFile())) {
        zf.extractAll(res.toFile().getAbsolutePath());
    }
    assertTrue(HelpFunctions.compareTwoDir(roDir.toFile(), res.toFile()));

    // just so we know the metadata is still valid
    HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/fileAndDir.json");
  }


  @Test
  void testWritingToZipFail(@TempDir Path tempDir) throws IOException {
    // create the RO_crate directory in the tempDir
    Path roDir = tempDir.resolve("ro_dir");
    FileUtils.forceMkdir(roDir.toFile());

    // the .json of our crate
    InputStream fileJson=
        ZipStreamStrategyTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

    // fill the expected directory with files and dirs

    Path json = roDir.resolve("ro-crate-metadata.json");
    FileUtils.copyInputStreamToFile(fileJson, json.toFile());

    PreviewGenerator.generatePreview(roDir.toFile().getAbsolutePath());

    Path file1 = roDir.resolve("input.txt");
    FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
    Path dirInCrate = roDir.resolve("dir");
    FileUtils.forceMkdir(dirInCrate.toFile());
    FileUtils.writeStringToFile(dirInCrate.resolve("first.txt").toFile(),
        "content of first file in dir", Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("second.txt").toFile(),
        "content of second file in dir",
        Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("third.txt").toFile(),
        "content of third file in dir",
        Charset.defaultCharset());
    // false file, this test case should fal
    Path falseFile = tempDir.resolve("new");
    FileUtils.writeStringToFile(falseFile.toFile(), "this file contains something else", Charset.defaultCharset());
    // create the RO_Crate including the files that should be present in it
    RoCrate roCrate = new RoCrate.RoCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .setLocationWithExceptions(falseFile)
                .setId("cp7glop.ai")
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .addProperty("name", "Too many files")
                .addProperty("description",
                    "This directory contains many small files, that we're not going to describe in detail.")
                .setLocationWithExceptions(dirInCrate)
                .setId("lots_of_little_files/")
                .build()
        )
        .setPreview(new AutomaticPreview())
        .build();

    // create a Writer for writing RoCrates to zip
    CrateWriter<OutputStream> writer = Writers.newZipStreamWriter();
    // write into destination path
    Path destination_path = tempDir.resolve("test.zip");
    OutputStream destination = new FileOutputStream(destination_path.toFile());
    writer.save(roCrate, destination);

    // extract and compare
    Path res = tempDir.resolve("dest");
    try (ZipFile zf = new ZipFile(destination_path.toFile())) {
        zf.extractAll(res.toFile().getAbsolutePath());
    }
    assertFalse(HelpFunctions.compareTwoDir(roDir.toFile(), res.toFile()));

    // just so we know the metadata is still valid
    HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/fileAndDir.json");
  }
}
