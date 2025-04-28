package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.preview.AutomaticPreview;
import edu.kit.datamanager.ro_crate.preview.PreviewGenerator;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class CrateWriterTest {

    /**
     * Saves the crate with the writer fitting to this test class.
     *
     * @param crate the crate to save
     * @param target the target path to the save location
     * @throws IOException if an error occurs while saving the crate
     */
    abstract protected void saveCrate(Crate crate, Path target) throws IOException;

    @Test
    void testFilesBeingAdjusted(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("input.txt");
        FileUtils.writeStringToFile(file1.toFile(), "content of Local File", Charset.defaultCharset());
        Path dirInCrate = tempDir.resolve("dir");
        FileUtils.forceMkdir(dirInCrate.toFile());
        Path dirInDirInCrate = dirInCrate.resolve("last_dir");
        FileUtils.writeStringToFile(
                dirInCrate.resolve("first.txt").toFile(),
                "content of first file in dir",
                Charset.defaultCharset());
        FileUtils.writeStringToFile(
                dirInDirInCrate.resolve("second.txt").toFile(),
                "content of second file in dir",
                Charset.defaultCharset());
        FileUtils.writeStringToFile(
                dirInCrate.resolve("third.txt").toFile(),
                "content of third file in dir",
                Charset.defaultCharset());

        // create the RO_Crate including the files that should be present in it
        RoCrate roCrate = new RoCrate.RoCrateBuilder(
                "Example RO-Crate",
                "The RO-Crate Root Data Entity",
                "2024",
                "https://creativecommons.org/licenses/by-nc-sa/3.0/au/"
        )
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
                                .addProperty("name", "Many files")
                                .addProperty("description",
                                        "This directory contains many small files, that we're not going to describe in detail.")
                                .setLocationWithExceptions(dirInCrate)
                                .setId("lots_of_little_files/")
                                .build()
                )
                .setPreview(new AutomaticPreview())
                .build();

        Path writtenCrate = tempDir.resolve("written-crate");
        this.saveCrate(roCrate, writtenCrate);

        Path folderToMakeAssertionsOn = tempDir.resolve("checkMe");
        this.ensureCrateIsExtractedIn(writtenCrate, folderToMakeAssertionsOn);

        // test if the names of the files in the crate are correct,
        // when there is an ID the file should be called the same as the entity.
        assertTrue(
                Files.isRegularFile(folderToMakeAssertionsOn.resolve("cp7glop.ai")),
                "The file should be present and have the name adjusted to the ID"
        );
        assertTrue(
                Files.isDirectory(folderToMakeAssertionsOn.resolve("lots_of_little_files/")),
                "The directory should be present and have the name adjusted to the ID"
        );
    }

    @Test
    void testWriting(@TempDir Path tempDir) throws IOException {
        // We need a correct directory to compare with.
        // It is built manually to ensure we meet our expectations.
        // Reader-writer-consistency is tested at {@link CrateReaderTest}
        // TODO test javadoc
        Path correctCrate = tempDir.resolve("compare_with_me");
        Path pathToFile = correctCrate.resolve("cp7glop.ai");
        Path pathToDir = correctCrate.resolve("lots_of_little_files");

        this.createManualCrateStructure(correctCrate, pathToFile, pathToDir);

        // Now use the builder to build the same crate independently.
        // The files will be reused (we need a place to take a copy from)
        RoCrate builtCrate = getCrateWithFileAndDir(pathToFile, pathToDir).build();

        Path pathToZip = tempDir.resolve("written-needs_testing.zip");
        this.saveCrate(builtCrate, pathToZip);

        // extract the zip file to a temporary directory
        Path extractionPath = tempDir.resolve("extracted_for_testing");
        this.ensureCrateIsExtractedIn(pathToZip, extractionPath);
        // compare the extracted directory with the correct one
        assertTrue(HelpFunctions.compareTwoDir(
                correctCrate.toFile(),
                extractionPath.toFile()));
        HelpFunctions.compareCrateJsonToFileInResources(
                builtCrate,
                "/json/crate/fileAndDir.json");
    }

    /**
     * Ensures the crate is in extracted form in the given path.
     *
     * @param pathToCrate       the path to the crate, may not be a folder yet
     * @param expectedPath      the path where the crate should be in extracted form
     * @throws IOException if an error occurs while extracting the crate
     */
    protected void ensureCrateIsExtractedIn(Path pathToCrate, Path expectedPath) throws IOException {
        try (ZipFile zf = new ZipFile(pathToCrate.toFile())) {
            zf.extractAll(expectedPath.toFile().getAbsolutePath());
        }
    }

    /**
     * Creates a crate structure manually.
     *
     * @param correctCrate the path to the crate
     * @param pathToFile   the path to the file
     * @param pathToDir    the path to the directory
     * @throws IOException if an error occurs while creating the crate structure
     */
    protected void createManualCrateStructure(Path correctCrate, Path pathToFile, Path pathToDir) throws IOException {
        FileUtils.forceMkdir(correctCrate.toFile());
        InputStream fileJson = ZipStreamStrategyTest.class
                .getResourceAsStream("/json/crate/fileAndDir.json");
        Assertions.assertNotNull(fileJson);
        // fill the directory with expected files and dirs
        // starting with the .json of our crate
        Path json = correctCrate.resolve("ro-crate-metadata.json");
        FileUtils.copyInputStreamToFile(fileJson, json.toFile());
        // create preview
        PreviewGenerator.generatePreview(correctCrate.toFile().getAbsolutePath());
        // create the files and directories
        FileUtils.writeStringToFile(pathToFile.toFile(), "content of Local File", Charset.defaultCharset());
        FileUtils.forceMkdir(pathToDir.toFile());
        FileUtils.writeStringToFile(
                pathToDir.resolve("first.txt").toFile(),
                "content of first file in dir",
                Charset.defaultCharset());
        FileUtils.writeStringToFile(
                pathToDir.resolve("second.txt").toFile(),
                "content of second file in dir",
                Charset.defaultCharset());
        FileUtils.writeStringToFile(
                pathToDir.resolve("third.txt").toFile(),
                "content of third file in dir",
                Charset.defaultCharset());
    }

    /**
     * Creates a crate resembling the one we manually create in these tests.
     *
     * @param pathToFile      the file to add
     * @param pathToSubdir the directory to add
     * @return the crate builder
     */
    protected RoCrate.RoCrateBuilder getCrateWithFileAndDir(Path pathToFile, Path pathToSubdir) {
        return new RoCrate.RoCrateBuilder(
                "Example RO-Crate",
                "The RO-Crate Root Data Entity",
                "2024",
                "https://creativecommons.org/licenses/by-nc-sa/3.0/au/"
        )
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                .addProperty("name", "Diagram showing trend to increase")
                                .addProperty("contentSize", "383766")
                                .addProperty("description", "Illustrator file for Glop Pot")
                                .setEncodingFormat("application/pdf")
                                .setLocationWithExceptions(pathToFile)
                                .setId("cp7glop.ai")
                                .build()
                )
                .addDataEntity(
                        new DataSetEntity.DataSetBuilder()
                                .addProperty("name", "Too many files")
                                .addProperty("description",
                                        "This directory contains many small files, that we're not going to describe in detail.")
                                .setLocationWithExceptions(pathToSubdir)
                                .setId("lots_of_little_files/")
                                .build()
                )
                .setPreview(new AutomaticPreview());
    }

    @Test
    void testWritingFail(@TempDir Path tempDir) throws IOException {
        Path correctCrate = tempDir.resolve("compare_with_me");
        Path pathToFile = correctCrate.resolve("input.txt");
        Path pathToDir = correctCrate.resolve("dir");

        this.createManualCrateStructure(correctCrate, pathToFile, pathToDir);
        {
            // another file, which we will forget in our definition
            Path falseFile = tempDir.resolve("new");
            FileUtils.writeStringToFile(
                    falseFile.toFile(),
                    "this file contains something else",
                    Charset.defaultCharset());
        }

        // create the RO_Crate including the files that should be present in it
        RoCrate roCrate = getCrateWithFileAndDir(pathToFile, pathToDir).build();

        Path pathToZip = tempDir.resolve("writing-needs_testing.zip");
        this.saveCrate(roCrate, pathToZip);

        // extract and compare
        Path extractionPath = tempDir.resolve("extracted_for_testing");
        ensureCrateIsExtractedIn(pathToZip, extractionPath);
        assertFalse(HelpFunctions.compareTwoDir(
                correctCrate.toFile(),
                extractionPath.toFile()));
        HelpFunctions.compareCrateJsonToFileInResources(
                roCrate,
                "/json/crate/fileAndDir.json");
    }
}
