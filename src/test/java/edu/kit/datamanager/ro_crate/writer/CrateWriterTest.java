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

    /**
     * Test where the writer needs to rename files or folders in order to make a valid crate.
     * The content will therefore not be equal to its source!
     *
     * @param tempDir the temporary directory given by junit for our test
     * @throws IOException if an error occurs while writing the crate
     */
    @Test
    void testFilesBeingAdjusted(@TempDir Path tempDir) throws IOException {
        Path correctCrate = tempDir.resolve("compare_with_me");
        Path pathToFile = correctCrate.resolve("you-will-need-to-rename-this-file.ai");
        Path pathToDir = correctCrate.resolve("you-will-need-to-rename-this-dir");
        this.createManualCrateStructure(correctCrate, pathToFile, pathToDir);

        Path writtenCrate = tempDir.resolve("written-crate");
        Path extractionPath = tempDir.resolve("checkMe");
        {
            RoCrate builtCrate = getCrateWithFileAndDir(pathToFile, pathToDir).build();
            this.saveCrate(builtCrate, writtenCrate);
            this.ensureCrateIsExtractedIn(writtenCrate, extractionPath);
        }

        printFileTree(correctCrate);
        printFileTree(extractionPath);

        // The actual file name should **not** appear in the crate
        String fileName = pathToFile.getFileName().toString();
        assertFalse(
                Files.isRegularFile(extractionPath.resolve(fileName)),
                "The directory should not be present, because '%s' is a file in the crate".formatted(fileName)
        );
        // Instead, the file should be present with the name of the ID
        assertTrue(
                Files.isRegularFile(extractionPath.resolve("cp7glop.ai")),
                "The file 'cp7glop.ai' should be present and have the name adjusted to the ID"
        );
        // The actual directory name should **not** appear in the crate
        String dirName = pathToDir.getFileName().toString();
        assertFalse(
                Files.isDirectory(extractionPath.resolve(dirName)),
                "The directory should not be present, because '%s' is a file in the crate".formatted(dirName)
        );
        // Instead, the directory should be present with the name of the ID
        assertTrue(
                Files.isDirectory(extractionPath.resolve("lots_of_little_files/")),
                "The directory 'lots_of_little_files' should be present"
        );
    }

    /**
     * Test where the writer should make an exact copy of our defined folder.
     *
     * @param tempDir the temporary directory given by junit for our test
     * @throws IOException if an error occurs while writing the crate
     */
    @Test
    void testWritingMakesCopy(@TempDir Path tempDir) throws IOException {
        // We need a correct directory to compare with.
        // It is built manually to ensure we meet our expectations.
        // Reader-writer-consistency is tested at {@link CrateReaderTest}
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
        printFileTree(correctCrate);
        printFileTree(extractionPath);

        // compare the extracted directory with the correct one
        assertTrue(HelpFunctions.compareTwoDir(
                correctCrate.toFile(),
                extractionPath.toFile()));
        HelpFunctions.compareCrateJsonToFileInResources(
                builtCrate,
                "/json/crate/fileAndDir.json");
    }

    /**
     * Test where the writer should only consider the files that are added to the metadata json.
     * The crate should not contain any files that are not part of it.
     *
     * @param tempDir the temporary directory given by junit for our test
     * @throws IOException if an error occurs while writing the crate
     */
    @Test
    void testWritingOnlyConsidersAddedFiles(@TempDir Path tempDir) throws IOException {
        Path correctCrate = tempDir.resolve("compare_with_me");
        Path pathToFile = correctCrate.resolve("cp7glop.ai");
        Path pathToDir = correctCrate.resolve("lots_of_little_files");

        this.createManualCrateStructure(correctCrate, pathToFile, pathToDir);
        {
            // This file is not part of the crate, and should therefore not be present
            Path falseFile = correctCrate.resolve("new");
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
        printFileTree(correctCrate);
        printFileTree(extractionPath);

        assertFalse(HelpFunctions.compareTwoDir(
                correctCrate.toFile(),
                extractionPath.toFile()),
                "The crate should not contain the file that was not part of the metadata");
        HelpFunctions.compareCrateJsonToFileInResources(
                roCrate,
                "/json/crate/fileAndDir.json");
    }

    /**
     * Prints the file tree of the given directory for debugging and understanding
     * a test more quickly.
     *
     * @param directoryToPrint the directory to print
     * @throws IOException if an error occurs while printing the file tree
     */
    @SuppressWarnings("resource")
    protected static void printFileTree(Path directoryToPrint) throws IOException {
        // Print all files recursively in a tree structure for debugging
        System.out.printf("Files in %s:%n", directoryToPrint.getFileName().toString());
        Files.walk(directoryToPrint)
                .forEach(path -> {
                    if (!path.toAbsolutePath().equals(directoryToPrint.toAbsolutePath())) {
                        int depth = path.relativize(directoryToPrint).getNameCount();
                        String prefix = "  ".repeat(depth);
                        System.out.printf("%s%s%s%n", prefix, "└── ", path.getFileName());
                    }
                });
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
        // creates the directory and a subdirectory
        Path subdir = pathToDir.resolve("subdir");
        FileUtils.forceMkdir(subdir.toFile());
        FileUtils.writeStringToFile(
                subdir.resolve("subsubfirst.txt").toFile(),
                "content of subsub file in subsubdir",
                Charset.defaultCharset());
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
}
