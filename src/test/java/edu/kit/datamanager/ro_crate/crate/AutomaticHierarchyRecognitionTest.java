package edu.kit.datamanager.ro_crate.crate;

import static org.junit.jupiter.api.Assertions.*;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Automatic Hierarchy Recognition - API Tests & Usage Examples
 */
public class AutomaticHierarchyRecognitionTest {

    private RoCrate crate;

    @BeforeEach
    void setUp() {
        crate = new RoCrate.RoCrateBuilder(
            "Test Crate",
            "A crate for testing hierarchy recognition",
            "2024",
            "https://creativecommons.org/licenses/by/4.0/"
        ).build();
    }

    /**
     * One-directional recognition in simple hierarchy.
     */
    @Test
    void givenFilesInFolderHierarchy_whenRecognizeStructure_thenEstablishesParentChildRelations() {
        // Given: A crate with files and folders in a hierarchy
        FileEntity file1 = new FileEntity.FileEntityBuilder()
            .setId("data/raw/experiment1.csv")
            .setLocationWithExceptions(Paths.get("test1.csv"))
            .build();

        FileEntity file2 = new FileEntity.FileEntityBuilder()
            .setId("data/processed/results.txt")
            .setLocationWithExceptions(Paths.get("test2.txt"))
            .build();

        DataSetEntity dataFolder = new DataSetEntity.DataSetBuilder()
            .setId("data/")
            .addProperty("name", "Data Directory")
            .build();

        DataSetEntity rawFolder = new DataSetEntity.DataSetBuilder()
            .setId("data/raw/")
            .addProperty("name", "Raw Data")
            .build();

        DataSetEntity processedFolder = new DataSetEntity.DataSetBuilder()
            .setId("data/processed/")
            .addProperty("name", "Processed Data")
            .build();

        crate.addDataEntity(file1);
        crate.addDataEntity(file2);
        crate.addDataEntity(dataFolder);
        crate.addDataEntity(rawFolder);
        crate.addDataEntity(processedFolder);

        // When: We automatically recognize hierarchy
        crate.createDataEntityFileStructure(false);

        // Then: Hierarchy should be established
        assertTrue(dataFolder.hasPart("data/raw/"));
        assertTrue(dataFolder.hasPart("data/processed/"));
        assertTrue(rawFolder.hasPart("data/raw/experiment1.csv"));
        assertTrue(processedFolder.hasPart("data/processed/results.txt"));

        // Root should only contain top-level entities
        var root = crate.getRootDataEntity();
        assertTrue(root.hasPart("data/"));
        assertEquals(1, root.hasPart.size());
    }

    /**
     * Adding bidirectional relationships.
     */
    @Test
    void givenFileInFolder_whenRecognizeWithIsPartOf_thenCreatesBidirectionalRelations() {
        FileEntity file = new FileEntity.FileEntityBuilder()
            .setId("folder/file.txt")
            .setLocationWithExceptions(Paths.get("test.txt"))
            .build();

        DataSetEntity folder = new DataSetEntity.DataSetBuilder()
            .setId("folder/")
            .build();

        crate.addDataEntity(file);
        crate.addDataEntity(folder);

        // When: We enable isPartOf relationships
        crate.createDataEntityFileStructure(true);

        // Then: Both hasPart and isPartOf should be set
        assertTrue(folder.hasPart("folder/file.txt"));
        assertEquals("folder/", file.getProperties().get("isPartOf").asText());
        // same for root!
        var root = crate.getRootDataEntity();
        assertTrue(root.hasPart("folder/"));
        assertEquals(1, root.hasPart.size());
        assertEquals(root.getId(), folder.getProperties().get("isPartOf").asText(""));
    }

    /**
     * Advanced configuration with missing-folder-creation enabled.
     */
    @Test
    void givenDeepNestedPathWithMissingIntermediates_whenRecognizeWithCreateMissing_thenCreatesAllIntermediateEntities() {
        FileEntity file = new FileEntity.FileEntityBuilder()
            .setId("data/deep/nested/file.txt")
            .setLocationWithExceptions(Paths.get("test.txt"))
            .build();

        crate.addDataEntity(file);

        // When: We configure to create missing intermediate entities
        HierarchyRecognitionConfig config = new HierarchyRecognitionConfig()
            .createMissingIntermediateEntities(true)
            .setInverseRelationships(true)
            .removeExistingConnections(true);

        HierarchyRecognitionResult result = crate.createDataEntityFileStructure(
            config
        );

        // Then: Missing intermediate entities should be created
        assertTrue(result.isSuccessful());
        assertNotNull(crate.getDataEntityById("data/"));
        assertNotNull(crate.getDataEntityById("data/deep/"));
        assertNotNull(crate.getDataEntityById("data/deep/nested/"));

        // And hierarchy should be established
        assertTrue(
            ((DataSetEntity) crate.getDataEntityById("data/")).hasPart(
                "data/deep/"
            )
        );
        assertTrue(
            ((DataSetEntity) crate.getDataEntityById("data/deep/")).hasPart(
                "data/deep/nested/"
            )
        );
        assertTrue(
            ((DataSetEntity) crate.getDataEntityById(
                    "data/deep/nested/"
                )).hasPart("data/deep/nested/file.txt")
        );
    }

    /**
     * Removing existing manual relationships.
     */
    @Test
    void givenFolderWithExistingRelations_whenRecognizeWithRemoveExisting_thenKeepsOnlyNewRelations() {
        FileEntity file1 = new FileEntity.FileEntityBuilder()
            .setId("folder/file1.txt")
            .setLocationWithExceptions(Paths.get("test1.txt"))
            .build();

        FileEntity file2 = new FileEntity.FileEntityBuilder()
            .setId("folder/file2.txt")
            .setLocationWithExceptions(Paths.get("test2.txt"))
            .build();

        DataSetEntity folder = new DataSetEntity.DataSetBuilder()
            .setId("folder/")
            .addToHasPart("manually-added-entity")
            .build();

        crate.addDataEntity(file1);
        crate.addDataEntity(file2);
        crate.addDataEntity(folder);

        // When: We merge with existing relationships
        HierarchyRecognitionConfig config =
            new HierarchyRecognitionConfig().removeExistingConnections(true);

        crate.createDataEntityFileStructure(config);

        // Then: Both existing and new relationships should exist
        assertFalse(folder.hasPart("manually-added-entity"));
        assertTrue(folder.hasPart("folder/file1.txt"));
        assertTrue(folder.hasPart("folder/file2.txt"));
    }

    /**
     * Default behavior keeps existing relationships.
     */
    @Test
    void givenFolderWithExistingRelations_whenRecognizeWithDefaultBehavior_thenKeepsExistingRelations() {
        FileEntity file = new FileEntity.FileEntityBuilder()
            .setId("folder/file.txt")
            .setLocationWithExceptions(Paths.get("test.txt"))
            .build();

        DataSetEntity folder = new DataSetEntity.DataSetBuilder()
            .setId("folder/")
            .addToHasPart("manually-added-entity")
            .build();

        crate.addDataEntity(file);
        crate.addDataEntity(folder);

        // When: We use default behavior (keep existing)
        crate.createDataEntityFileStructure(false);

        // Then: Only new relationships should exist
        assertTrue(folder.hasPart("manually-added-entity"));
        assertTrue(folder.hasPart("folder/file.txt"));
    }

    /**
     * Test skipping non-file-path IDs
     */
    @Test
    void givenMixOfFilePathsUrlsAndDois_whenRecognizeStructure_thenProcessesOnlyFilePaths() {
        FileEntity localFile = new FileEntity.FileEntityBuilder()
            .setId("folder/file.txt")
            .setLocationWithExceptions(Paths.get("test.txt"))
            .build();

        DataEntity remoteEntity = new DataEntity.DataEntityBuilder()
            .setId("https://example.com/remote-file.txt")
            .addType("File")
            .build();

        DataEntity doiEntity = new DataEntity.DataEntityBuilder()
            .setId("doi:10.1234/example")
            .addType("CreativeWork")
            .build();

        DataSetEntity folder = new DataSetEntity.DataSetBuilder()
            .setId("folder/")
            .build();

        crate.addDataEntity(localFile);
        crate.addDataEntity(remoteEntity);
        crate.addDataEntity(doiEntity);
        crate.addDataEntity(folder);

        // When: We recognize hierarchy
        crate.createDataEntityFileStructure(false);

        // Then: Only local file paths should be processed
        assertTrue(folder.hasPart("folder/file.txt"));
        assertFalse(folder.hasPart("https://example.com/remote-file.txt"));
        assertFalse(folder.hasPart("doi:10.1234/example"));

        // Remote and DOI entities should remain in root
        assertTrue(
            crate
                .getRootDataEntity()
                .hasPart("https://example.com/remote-file.txt")
        );
        assertTrue(crate.getRootDataEntity().hasPart("doi:10.1234/example"));
    }

    /**
     * Test error handling with circular references
     */
    @Test
    void givenEntitiesWithCircularPathReferences_whenRecognizeStructure_thenHandlesGracefullyWithoutException() {
        // This would be a malformed crate, but we should handle it gracefully
        DataSetEntity folder1 = new DataSetEntity.DataSetBuilder()
            .setId("folder1/")
            .build();

        DataSetEntity folder2 = new DataSetEntity.DataSetBuilder()
            .setId("folder1/folder2/")
            .build();

        // Manually create circular reference in IDs (this is contrived but tests the logic)
        DataEntity circularEntity = new DataEntity.DataEntityBuilder()
            .setId("folder1/folder2/../../../folder1/file.txt") // resolves to folder1/file.txt
            .addType("File")
            .build();

        crate.addDataEntity(folder1);
        crate.addDataEntity(folder2);
        crate.addDataEntity(circularEntity);

        // When/Then: Should handle gracefully
        assertDoesNotThrow(() -> {
            // When: Default configuration for hierarchy recognition
            HierarchyRecognitionResult result =
                crate.createDataEntityFileStructure(
                    new HierarchyRecognitionConfig()
                );
            // Then: Does not throw exception or error.
            assertTrue(result.isSuccessful());
        });
    }

    /**
     * Test validation before any changes are made
     */
    @Test
    void givenInvalidEntityData_whenRecognizeStructure_thenFailsWithoutMakingChanges() {
        // Given: A file appears to be inside another file (invalid hierarchy)
        FileEntity parentFile = new FileEntity.FileEntityBuilder()
            .setId("document.pdf")
            .setLocationWithExceptions(Paths.get("document.pdf"))
            .build();

        FileEntity childFile = new FileEntity.FileEntityBuilder()
            .setId("document.pdf/embedded_data.txt") // Invalid: file inside a file
            .setLocationWithExceptions(Paths.get("embedded.txt"))
            .build();

        crate.addDataEntity(parentFile);
        crate.addDataEntity(childFile);

        // When: We try to recognize hierarchy
        HierarchyRecognitionConfig config = new HierarchyRecognitionConfig();
        HierarchyRecognitionResult result = crate.createDataEntityFileStructure(
            config
        );

        // Then: Should fail without making any changes
        assertFalse(result.isSuccessful());

        // Original state should be preserved
        assertTrue(crate.getRootDataEntity().hasPart("document.pdf"));
        assertTrue(
            crate.getRootDataEntity().hasPart("document.pdf/embedded_data.txt")
        );
    }

    /**
     * Test result object provides useful information
     */
    @Test
    void givenFileRequiringIntermediateCreation_whenRecognizeStructure_thenReturnsDetailedOperationInfo() {
        FileEntity file = new FileEntity.FileEntityBuilder()
            .setId("folder/file.txt")
            .setLocationWithExceptions(Paths.get("test.txt"))
            .build();

        crate.addDataEntity(file);

        HierarchyRecognitionConfig config =
            new HierarchyRecognitionConfig().createMissingIntermediateEntities(
                true
            );

        // When: We recognize hierarchy
        HierarchyRecognitionResult result = crate.createDataEntityFileStructure(
            config
        );

        // Then: Result should provide useful information
        assertTrue(result.isSuccessful());
        HierarchyRecognitionResult info = result;

        assertEquals(1, info.createdEntities().size()); // "folder/" was created
        assertEquals(2, info.processedRelationships().size()); // root -> folder -> file relationship
        assertTrue(info.skippedEntities().isEmpty()); // no entities skipped
        assertTrue(info.warnings().isEmpty()); // no warnings
    }
}
