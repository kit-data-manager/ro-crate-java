package edu.kit.rocrate.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.DataSetEntity;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.preview.PreviewGenerator;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                .build();

        Path result = tempDir.resolve("dest");
        folderRoCrateWriter.save(roCrate, result.toFile().toString());

        assertTrue(compareTwoDir(result.toFile(), roDir.toFile()));

        // just so we know the metadata is still valid
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode jsonROCrate = objectMapper.readTree(roCrate.getJsonMetadata());
        InputStream inputStream = FolderWriterTest.class.getResourceAsStream(
                "/json/crate/fileAndDir.json");
        JsonNode expectedJson = objectMapper.readTree(inputStream);

        assertEquals(jsonROCrate, expectedJson);
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
        assertFalse(compareTwoDir(result.toFile(), roDir.toFile()));

        // just so we know the metadata is still valid
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        JsonNode jsonROCrate = objectMapper.readTree(roCrate.getJsonMetadata());
        InputStream inputStream = FolderWriterTest.class.getResourceAsStream(
                "/json/crate/fileAndDir.json");
        JsonNode expectedJson = objectMapper.readTree(inputStream);

        assertEquals(jsonROCrate, expectedJson);
    }

    // TODO: maybe export to different class
    public static boolean compareTwoDir(File dir1, File dir2) throws IOException {
        // compare the content of the two directories
        List<File> a = (List<java.io.File>) FileUtils.listFiles(dir1, null, true);
        Map<String, File> result_map = a.stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));

        List<java.io.File> b = (List<java.io.File>) FileUtils.listFiles(dir2, null, true);
        Map<String, java.io.File> input_map = b.stream()
                .collect(Collectors.toMap(java.io.File::getName, Function.identity()));

        if (result_map.size() != input_map.size()) {
            return false;
        }
        for (String s : input_map.keySet()) {
            // we do that because the ro-crate-metadata.json can be differently formatted,
            // or the order of the entities may be different
            // the same holds for the html file
            if (s.equals("ro-crate-metadata.json") || s.equals("ro-crate-preview.html")) {
                if (!result_map.containsKey(s)) {
                    return false;
                }
                continue;
            }
            if (!FileUtils.contentEqualsIgnoreEOL(input_map.get(s), result_map.get(s), null)) {
                return false;
            }
        }
        return true;
    }

}
