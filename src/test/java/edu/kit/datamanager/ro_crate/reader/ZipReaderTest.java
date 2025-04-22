package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;
import edu.kit.datamanager.ro_crate.writer.ZipWriter;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ZipReaderTest {

    @Test
    void testReadingBasicCrate(@TempDir Path temp) throws IOException {
        RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                .build();

        Path zipPath = temp.resolve("result.zip");

        RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
        // save the content of the roCrate to the dest zip
        roCrateZipWriter.save(roCrate, zipPath.toString());

        File zipFile = zipPath.toFile();
        assertTrue(zipFile.isFile());

        CrateReader<String> reader = Readers.newZipPathReader();
        Crate res = reader.readCrate(zipFile.getAbsolutePath());
        HelpFunctions.compareTwoCrateJson(roCrate, res);
    }

    @Test
    void testWithFile(@TempDir Path temp) throws IOException {
        Path cvs = temp.resolve("survey-responses-2019.csv");
        FileUtils.touch(cvs.toFile());
        FileUtils.writeStringToFile(cvs.toFile(), "fkdjaflkjfla", Charset.defaultCharset());
        RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                .setLocationWithExceptions(cvs)
                                .setId(cvs.toFile().getName())
                                .addProperty("name", "Survey responses")
                                .addProperty("contentSize", "26452")
                                .addProperty("encodingFormat", "text/csv")
                                .build()
                )
                .build();

        assertEquals(1, roCrate.getAllDataEntities().size());

        Path zipPath = temp.resolve("result.zip");

        RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
        // save the content of the roCrate to the dest zip
        roCrateZipWriter.save(roCrate, zipPath.toFile().getAbsolutePath());

        CrateReader<String> reader = Readers.newZipPathReader();
        Crate res = reader.readCrate(zipPath.toFile().getAbsolutePath());

        HelpFunctions.compareTwoCrateJson(roCrate, res);
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
                                .setLocationWithExceptions(file)
                                .setId("survey-responses-2019.csv")
                                .build()
                )
                .setPreview(null)//disable preview to allow to compare folders before and after
                .build();

        Path zipPath = temp.resolve("result.zip");

        RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
        // save the content of the roCrate to the dest zip
        roCrateZipWriter.save(roCrate, zipPath.toString());

        CrateReader<String> reader = Readers.newZipPathReader();
        Crate res = reader.readCrate(zipPath.toString());

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
        RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                .addProperty("name", "Survey responses")
                                .addProperty("contentSize", "26452")
                                .addProperty("encodingFormat", "text/csv")
                                .setLocationWithExceptions(file)
                                .setId("survey-responses-2019.csv")
                                .build()
                )
                .build();

        Path zipPath = temp.resolve("result.zip");

        RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
        // save the content of the roCrate to the dest zip
        roCrateZipWriter.save(roCrate, zipPath.toString());

        CrateReader<String> reader = Readers.newZipPathReader();
        Crate res = reader.readCrate(zipPath.toFile().getAbsolutePath());

        Path locationSource = temp.resolve("expected");
        RoCrateWriter writer = new RoCrateWriter(new FolderWriter());
        writer.save(roCrate, locationSource.toString());

        Path newFile = temp.resolve("new_file");
        FileUtils.writeStringToFile(newFile.toFile(), "fkladjsl;fjasd;lfjda;lkf", Charset.defaultCharset());

        res.addDataEntity(new FileEntity.FileEntityBuilder()
                .setEncodingFormat("setnew")
                .setLocationWithExceptions(newFile)
                .setId("new_file")
                .build());

        Path destinationDir = temp.resolve("result");
        FileUtils.forceMkdir(destinationDir.toFile());
        writer.save(res, destinationDir.toFile().toString());

        assertFalse(HelpFunctions.compareTwoDir(locationSource.toFile(), destinationDir.toFile()));
        HelpFunctions.compareTwoMetadataJsonNotEqual(roCrate, res);
    }

    @Test
    void testReadingBasicCrateWithCustomPath(@TempDir Path temp) throws IOException {
        RoCrate roCrate = new RoCrate.RoCrateBuilder(
                "minimal",
                "minimal RO_crate",
                "2024",
                "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                .build();

        Path zipPath = temp.resolve("result.zip");

        RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
        roCrateZipWriter.save(roCrate, zipPath.toString());

        File zipFile = zipPath.toFile();
        assertTrue(zipFile.isFile());

        Path differentFolder = temp.resolve("differentFolder");
        ZipStrategy readerType = new ZipStrategy(differentFolder, true);
        assertFalse(readerType.isExtracted());
        assertEquals(readerType.getTemporaryFolder().getFileName().toString(), readerType.getID());
        assertTrue(readerType.getTemporaryFolder().startsWith(differentFolder));

        CrateReader<String> roCrateFolderReader = new CrateReader<>(readerType);
        Crate crate = roCrateFolderReader.readCrate(zipFile.getAbsolutePath());
        assertTrue(readerType.isExtracted());
        HelpFunctions.compareTwoCrateJson(roCrate, crate);

        {
            // try it again without the UUID subfolder and test if the directory is being cleaned up (using coverage).
            ZipStrategy newReaderType = new ZipStrategy(differentFolder, false);
            assertFalse(newReaderType.isExtracted());
            assertNotEquals(newReaderType.getTemporaryFolder().getFileName().toString(), newReaderType.getID());
            assertTrue(newReaderType.getTemporaryFolder().startsWith(differentFolder));

            CrateReader<String> newRoCrateFolderReader = new CrateReader<>(newReaderType);
            Crate crate2 = newRoCrateFolderReader.readCrate(zipFile.getAbsolutePath());
            assertTrue(newReaderType.isExtracted());
            HelpFunctions.compareTwoCrateJson(roCrate, crate2);
        }
    }
}
