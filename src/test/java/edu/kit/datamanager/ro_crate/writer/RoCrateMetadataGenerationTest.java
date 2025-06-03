package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class RoCrateMetadataGenerationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_ContainRoCrateJavaEntities_When_WritingEmptyCrate(@TempDir Path tempDir) throws IOException {
        // Create and write crate
        RoCrate crate = new RoCrate.RoCrateBuilder().build();
        Path outputPath = tempDir.resolve("test-crate");
        Writers.newFolderWriter().save(crate, outputPath.toString());

        // Read and print metadata for debugging
        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));
        HelpFunctions.prettyPrintJsonString(metadata);

        // Parse metadata file
        JsonNode rootNode = objectMapper.readTree(metadata);
        JsonNode graph = rootNode.get("@graph");

        // Find ro-crate-java entity
        JsonNode roCrateJavaEntity = findEntityById(graph, "#ro-crate-java");
        assertNotNull(roCrateJavaEntity, "ro-crate-java entity should exist");
        assertEquals("SoftwareApplication", roCrateJavaEntity.get("@type").asText(),
            "ro-crate-java should be of type SoftwareApplication");

        // Find CreateAction entity
        JsonNode createActionEntity = findEntityByType(graph, "CreateAction");
        assertNotNull(createActionEntity, "CreateAction entity should exist");
        assertNotNull(createActionEntity.get("startTime"), "CreateAction should have startTime");
        assertEquals("#ro-crate-java", createActionEntity.get("agent").get("@id").asText(),
            "CreateAction should reference ro-crate-java as agent");
    }

    @Test
    void should_HaveRequiredPropertiesInRoCrateJavaEntity_When_WritingCrate(@TempDir Path tempDir) throws IOException {
        // Create and write crate
        RoCrate crate = new RoCrate.RoCrateBuilder().build();
        Path outputPath = tempDir.resolve("test-crate");
        Writers.newFolderWriter().save(crate, outputPath.toString());

        // Read and print metadata for debugging
        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));
        HelpFunctions.prettyPrintJsonString(metadata);

        // Parse metadata file
        JsonNode rootNode = objectMapper.readTree(metadata);
        JsonNode roCrateJavaEntity = findEntityById(rootNode.get("@graph"), "#ro-crate-java");

        assertNotNull(roCrateJavaEntity, "ro-crate-java entity should exist");
        assertEquals("ro-crate-java", roCrateJavaEntity.get("name").asText(),
            "should have correct name");
        assertEquals("https://github.com/kit-data-manager/ro-crate-java",
            roCrateJavaEntity.get("url").asText(),
            "should have correct repository URL");
        assertNotNull(roCrateJavaEntity.get("version"),
            "should have version property");
    }

    @Test
    void should_HaveBidirectionalRelation_Between_RoCrateJavaAndItsAction(@TempDir Path tempDir) throws IOException {
        // Create and write crate
        RoCrate crate = new RoCrate.RoCrateBuilder().build();
        Path outputPath = tempDir.resolve("test-crate");
        Writers.newFolderWriter().save(crate, outputPath.toString());

        // Read and print metadata for debugging
        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));
        HelpFunctions.prettyPrintJsonString(metadata);

        // Parse metadata file
        JsonNode rootNode = objectMapper.readTree(metadata);
        JsonNode graph = rootNode.get("@graph");

        // Get both entities
        JsonNode roCrateJavaEntity = findEntityById(graph, "#ro-crate-java");
        JsonNode createActionEntity = findEntityByType(graph, "CreateAction");

        assertNotNull(roCrateJavaEntity, "ro-crate-java entity should exist");
        assertNotNull(createActionEntity, "CreateAction entity should exist");

        // Test CreateAction -> ro-crate-java reference
        JsonNode agentRef = createActionEntity.get("agent");
        assertNotNull(agentRef, "CreateAction should have agent property");
        assertEquals("#ro-crate-java", agentRef.get("@id").asText(),
            "CreateAction's agent should reference ro-crate-java");

        // Test ro-crate-java -> CreateAction reference
        JsonNode actionRef = roCrateJavaEntity.get("action");
        assertNotNull(actionRef, "ro-crate-java should have action property");
        assertEquals(createActionEntity.get("@id").asText(), actionRef.get("@id").asText(),
            "ro-crate-java's action should reference the CreateAction");
    }

    @Test
    void should_AccumulateActions_When_WritingMultipleTimes(@TempDir Path tempDir) throws IOException {
        // Create and write crate first time
        RoCrate crate = new RoCrate.RoCrateBuilder().build();
        Path outputPath = tempDir.resolve("test-crate");
        Writers.newFolderWriter().save(crate, outputPath.toString());

        // Write same crate two more times to simulate updates
        Writers.newFolderWriter().save(crate, outputPath.toString());
        Writers.newFolderWriter().save(crate, outputPath.toString());

        // Read and print metadata for debugging
        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));
        HelpFunctions.prettyPrintJsonString(metadata);

        // Parse metadata file
        JsonNode rootNode = objectMapper.readTree(metadata);
        JsonNode graph = rootNode.get("@graph");

        // Get ro-crate-java entity
        JsonNode roCrateJavaEntity = findEntityById(graph, "#ro-crate-java");
        assertNotNull(roCrateJavaEntity, "ro-crate-java entity should exist");

        // Verify actions array exists and has three entries
        assertTrue(roCrateJavaEntity.get("action").isArray(),
            "ro-crate-java should have an array of actions");
        assertEquals(3, roCrateJavaEntity.get("action").size(),
            "should have three actions after three writes");

        // Find all action entities
        JsonNode createAction = findEntityByType(graph, "CreateAction");
        assertNotNull(createAction, "should have one CreateAction");

        JsonNode[] createActions = findEntitiesByType(graph, "UpdateAction");
        assertEquals(1, createActions.length, "should have exactly one CreateAction");

        JsonNode[] updateActions = findEntitiesByType(graph, "UpdateAction");
        assertEquals(2, updateActions.length, "should have two UpdateActions");

        // Verify CreateAction properties
        assertNotNull(createAction.get("startTime"), "CreateAction should have startTime");
        assertEquals("#ro-crate-java", createAction.get("agent").get("@id").asText(),
            "CreateAction should reference ro-crate-java as agent");

        // Verify UpdateAction properties
        for (JsonNode updateAction : updateActions) {
            assertNotNull(updateAction.get("startTime"),
                "UpdateAction should have startTime");
            assertEquals("#ro-crate-java", updateAction.get("agent").get("@id").asText(),
                "UpdateAction should reference ro-crate-java as agent");
        }

        // Verify chronological order of timestamps
        String createTime = createAction.get("startTime").asText();
        String updateTime1 = updateActions[0].get("startTime").asText();
        String updateTime2 = updateActions[1].get("startTime").asText();

        assertTrue(createTime.compareTo(updateTime1) < 0,
            "First update should be after creation");
        assertTrue(updateTime1.compareTo(updateTime2) < 0,
            "Second update should be after first update");
    }

    @Test
    void should_HaveValidVersionFormat_When_WritingCrate(@TempDir Path tempDir) throws IOException {
        // Create and write crate
        RoCrate crate = new RoCrate.RoCrateBuilder().build();
        Path outputPath = tempDir.resolve("test-crate");
        Writers.newFolderWriter().save(crate, outputPath.toString());

        // Read and print metadata for debugging
        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));
        HelpFunctions.prettyPrintJsonString(metadata);

        // Parse metadata file
        JsonNode rootNode = objectMapper.readTree(metadata);
        JsonNode roCrateJavaEntity = findEntityById(rootNode.get("@graph"), "#ro-crate-java");

        // Version format validation
        @SuppressWarnings("DataFlowIssue")
        String version = roCrateJavaEntity.get("version").asText();

        // Semantic versioning regex pattern that allows:
        // - Required: major.minor.patch (e.g., 1.2.3)
        // - Optional: pre-release identifier (e.g., -rc1, -RC1, -beta.1, -SNAPSHOT)
        // - Optional: build metadata (e.g., +build.123)
        String semverPattern = "(?i)^\\d+\\.\\d+\\.\\d+(?:-(?:rc\\d+|alpha|beta|snapshot)(?:\\.\\d+)?)?(?:\\+[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*)?$";

        assertTrue(version.matches(semverPattern),
            String.format("Version '%s' should match semantic versioning format: major.minor.patch[-prerelease][+build]%n" +
                        "Examples: 1.2.3, 1.2.3-rc1, 1.2.3-SNAPSHOT, 1.2.3-beta.1, 1.2.3+build.123", version));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void should_HaveCompleteMetadata_When_WritingCrate(@TempDir Path tempDir) throws IOException {
        // Create and write crate
        RoCrate crate = new RoCrate.RoCrateBuilder().build();
        Path outputPath = tempDir.resolve("test-crate");
        Writers.newFolderWriter().save(crate, outputPath.toString());

        // Read and print metadata for debugging
        String metadata = Files.readString(outputPath.resolve("ro-crate-metadata.json"));
        HelpFunctions.prettyPrintJsonString(metadata);

        // Parse metadata file
        JsonNode rootNode = objectMapper.readTree(metadata);
        JsonNode roCrateJavaEntity = findEntityById(rootNode.get("@graph"), "#ro-crate-java");

        // Required properties with specific values
        assertEquals("ro-crate-java", roCrateJavaEntity.get("name").asText(),
            "should have correct name");
        assertEquals("https://github.com/kit-data-manager/ro-crate-java",
            roCrateJavaEntity.get("url").asText(),
            "should have correct repository URL");
        assertEquals("SoftwareApplication", roCrateJavaEntity.get("@type").asText(),
            "should have correct type");

        // Optional but recommended properties
        assertNotNull(roCrateJavaEntity.get("description"),
            "should have a description");
        assertFalse(roCrateJavaEntity.get("description").asText().isEmpty(),
                "description should not be empty");

        assertNotNull(roCrateJavaEntity.get("license"),
            "should have a license");
        assertTrue(roCrateJavaEntity.has("softwareVersion"),
            "should have softwareVersion as an alias for version");
        assertEquals(roCrateJavaEntity.get("version").asText(),
            roCrateJavaEntity.get("softwareVersion").asText(),
            "version and softwareVersion should match");
    }

    private JsonNode findEntityById(JsonNode graph, String id) {
        for (JsonNode entity : graph) {
            if (entity.has("@id") && entity.get("@id").asText().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    private JsonNode findEntityByType(JsonNode graph, String type) {
        for (JsonNode entity : graph) {
            if (entity.has("@type") && entity.get("@type").asText().equals(type)) {
                return entity;
            }
        }
        return null;
    }

    private JsonNode[] findEntitiesByType(JsonNode graph, String type) {
        return StreamSupport.stream(graph.spliterator(), false)
            .filter(entity -> entity.has("@type") && entity.get("@type").asText().equals(type))
            .toArray(JsonNode[]::new);
    }
}
