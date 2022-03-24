package edu.kit.rocrate.writer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.DataSetEntity;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.preview.AutomaticPreview;
import edu.kit.crate.preview.PreviewGenerator;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import edu.kit.rocrate.HelpFunctions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class FolderWriterTest {


  @Test
  void writeToFolderTest(@TempDir Path tempDir) throws IOException {
    ROCrateWriter folderRoCrateWriter = new ROCrateWriter(new FolderWriter());
    Path roDir = tempDir.resolve("ro_dir");
    FileUtils.forceMkdir(roDir.toFile());

    // the .json of our crate
    InputStream fileJson =
        FolderWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

    // fill the expected directory with files and dirs

    Path json = roDir.resolve("ro-crate-metadata.json");
    FileUtils.copyInputStreamToFile(fileJson, json.toFile());

    PreviewGenerator.generatePreview(roDir.toString());

    Path file1 = roDir.resolve("input.txt");
    FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
    Path dirInCrate = roDir.resolve("dir");
    FileUtils.forceMkdir(dirInCrate.toFile());
    Path dirInDirInCrate = dirInCrate.resolve("last_dir");
    FileUtils.writeStringToFile(dirInCrate.resolve("first.txt").toFile(),
        "content of first file in dir", Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInDirInCrate.resolve("second.txt").toFile(),
        "content of second file in dir",
        Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("third.txt").toFile(),
        "content of third file in dir",
        Charset.defaultCharset());

    // create the RO_Crate including the files that should be present in it
    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("cp7glop.ai")
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .setLocation(file1.toFile())
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .setId("lots_of_little_files/")
                .addProperty("name", "Too many files")
                .addProperty("description",
                    "This directory contains many small files, that we're not going to describe in detail.")
                .setLocation(dirInCrate.toFile())
                .build()
        )
        .setPreview(new AutomaticPreview())
        .build();

    Path result = tempDir.resolve("dest");
    folderRoCrateWriter.save(roCrate, result.toFile().toString());
    assertTrue(HelpFunctions.compareTwoDir(result.toFile(), roDir.toFile()));

    // just so we know the metadata is still valid
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/fileAndDir.json");
  }

  @Test
  void writeToFolderWrongTest(@TempDir Path tempDir) throws IOException {
    ROCrateWriter folderRoCrateWriter = new ROCrateWriter(new FolderWriter());
    Path roDir = tempDir.resolve("ro_dir");
    FileUtils.forceMkdir(roDir.toFile());

    // the .json of our crate
    InputStream fileJson =
        FolderWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

    // fill the expected directory with files and dirs

    Path json = roDir.resolve("ro-crate-metadata.json");
    FileUtils.copyInputStreamToFile(fileJson, json.toFile());

    PreviewGenerator.generatePreview(roDir.toString());

    Path file1 = roDir.resolve("input.txt");
    FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
    Path dirInCrate = roDir.resolve("dir");
    FileUtils.forceMkdir(dirInCrate.toFile());
    Path dirInDirInCrate = dirInCrate.resolve("last_dir");
    FileUtils.writeStringToFile(dirInCrate.resolve("first.txt").toFile(),
        "content of first file in dir", Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInDirInCrate.resolve("second.txt").toFile(),
        "content of second file in dir",
        Charset.defaultCharset());
    FileUtils.writeStringToFile(dirInCrate.resolve("third.txt").toFile(),
        "content of third file in dir",
        Charset.defaultCharset());
    // false file, this test case should fal
    Path falseFile = tempDir.resolve("new");
    FileUtils.writeStringToFile(falseFile.toFile(), "this file contains something else", Charset.defaultCharset());
    // create the RO_Crate including the files that should be present in it
    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("cp7glop.ai")
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .setLocation(falseFile.toFile())
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .setId("lots_of_little_files/")
                .addProperty("name", "Too many files")
                .addProperty("description",
                    "This directory contains many small files, that we're not going to describe in detail.")
                .setLocation(dirInCrate.toFile())
                .build()
        )
        .build();

    Path result = tempDir.resolve("dest");
    folderRoCrateWriter.save(roCrate, result.toFile().toString());
    //FileUtils.copyDirectory(result.toFile(), new File("test"));
 //   assertFalse(HelpFunctions.compareTwoDir(result.toFile(), roDir.toFile()));

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/fileAndDir.json");
  }
}
