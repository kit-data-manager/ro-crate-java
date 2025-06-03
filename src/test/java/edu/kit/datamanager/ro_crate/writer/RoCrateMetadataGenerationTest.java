package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.RoCrate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class RoCrateMetadataGenerationTest {

    @Test
    void should_ContainRoCrateJavaEntities_When_WritingEmptyCrate(@TempDir Path tempDir) throws IOException {
        // Create an empty RO-Crate
        RoCrate crate = new RoCrate.RoCrateBuilder().build();

        // Write it to a temporary directory
        Path outputPath = tempDir.resolve("test-crate");
        FolderWriter writer = new FolderWriter();
        writer.write(crate, outputPath);

        // Read the metadata file
        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));

        // Verify ro-crate-java entity exists
        assertTrue(metadata.contains("\"@id\": \"#ro-crate-java\""));
        assertTrue(metadata.contains("\"@type\": \"SoftwareApplication\""));

        // Verify write action exists
        assertTrue(metadata.contains("\"@type\": \"CreateAction\""));
        assertTrue(metadata.contains("startTime"));
        assertTrue(metadata.contains("agent"));
    }

    @Test
    void should_HaveRequiredPropertiesInRoCrateJavaEntity_When_WritingCrate(@TempDir Path tempDir) throws IOException {
        RoCrate crate = new RoCrate.RoCrateBuilder().build();
        Path outputPath = tempDir.resolve("test-crate");
        FolderWriter writer = new FolderWriter();
        writer.write(crate, outputPath);

        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));

        // Test essential properties of the ro-crate-java entity
        assertTrue(metadata.contains("\"name\": \"ro-crate-java\""));
        assertTrue(metadata.contains("\"url\": \"https://github.com/kit-data-manager/ro-crate-java\""));
        assertTrue(metadata.contains("\"version\""));
    }
}
