package edu.kit.datamanager.ro_crate.writer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.preview.AutomaticPreview;
import edu.kit.datamanager.ro_crate.preview.CustomPreview;
import edu.kit.datamanager.ro_crate.preview.PreviewGenerator;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
class FolderWriterTest {
    
    @Test
    void writeToFolderCorrectNames(@TempDir Path tempDir) throws IOException {
        Path fileWithoutID = tempDir.resolve("spo.txt");
        FileUtils.writeStringToFile(fileWithoutID.toFile(), "content", Charset.defaultCharset());
        Path file1 = tempDir.resolve("input.txt");
        FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
        Path dirInCrate = tempDir.resolve("dir");
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
        
        Path result = tempDir.resolve("dest");
        
        RoCrateWriter folderRoCrateWriter = new RoCrateWriter(new FolderWriter());
        folderRoCrateWriter.save(roCrate, result.toFile().toString());
        // test if the names of the files in the crate are correct,
        // when there is an ID the file should be called the same as the entity.
        assertTrue(Files.isRegularFile(result.resolve("cp7glop.ai")));
        assertTrue(Files.isDirectory(result.resolve("lots_of_little_files/")));
        // assertTrue(Files.isRegularFile(result.resolve(fileWithoutID.getFileName())));
    }
    
    @Test
    void writeToFolderTest(@TempDir Path tempDir) throws IOException {
        RoCrateWriter folderRoCrateWriter = new RoCrateWriter(new FolderWriter());
        Path roDir = tempDir.resolve("ro_dir");
        FileUtils.forceMkdir(roDir.toFile());

        // the .json of our crate
        InputStream fileJson
                = FolderWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

        // fill the expected directory with files and dirs
        Path json = roDir.resolve("ro-crate-metadata.json");
        FileUtils.copyInputStreamToFile(fileJson, json.toFile());
        
        //create preview via rochtml or custom as fallback
        if (PreviewGenerator.isRochtmlAvailable()) {
            PreviewGenerator.generatePreview(roDir.toString());
        } else {
            new CustomPreview().saveAllToFolder(roDir.toFile());
        }
        Path file1 = roDir.resolve("cp7glop.ai");
        FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
        Path dirInCrate = roDir.resolve("lots_of_little_files");
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
        
        Path result = tempDir.resolve("dest");
        folderRoCrateWriter.save(roCrate, result.toFile().toString());
        assertTrue(HelpFunctions.compareTwoDir(result.toFile(), roDir.toFile()));

        // just so we know the metadata is still valid
        HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/fileAndDir.json");
    }
    
    @Test
    void writeToFolderWrongTest(@TempDir Path tempDir) throws IOException {
        RoCrateWriter folderRoCrateWriter = new RoCrateWriter(new FolderWriter());
        Path roDir = tempDir.resolve("ro_dir");
        FileUtils.forceMkdir(roDir.toFile());

        // the .json of our crate
        InputStream fileJson
                = FolderWriterTest.class.getResourceAsStream("/json/crate/fileAndDir.json");

        // fill the expected directory with files and dirs
        Path json = roDir.resolve("ro-crate-metadata.json");
        FileUtils.copyInputStreamToFile(fileJson, json.toFile());
        
        PreviewGenerator.generatePreview(roDir.toString());
        
        Path file1 = roDir.resolve("cp7glop.ai");
        FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
        Path dirInCrate = roDir.resolve("lots_of_little_files");
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
                                .setId("lots_of_little_files/")
                                .addProperty("name", "Too many files")
                                .addProperty("description",
                                        "This directory contains many small files, that we're not going to describe in detail.")
                                .setLocationWithExceptions(dirInCrate)
                                .setId("lots_of_little_files/")
                                .build()
                )
                .build();
        
        Path result = tempDir.resolve("dest");
        folderRoCrateWriter.save(roCrate, result.toFile().toString());
        assertFalse(HelpFunctions.compareTwoDir(result.toFile(), roDir.toFile()));
        
        HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/fileAndDir.json");
    }
}
