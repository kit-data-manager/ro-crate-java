package edu.kit.datamanager.ro_crate.context;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class ContextTest {
    private Context context;
    private static final String SCHEMA_URL = "https://schema.org/";
    private static final String EXAMPLE_URL = "http://example.org/terms/";

    @BeforeEach
    void setUp() {
        context = new Context();
    }

    @Nested
    class CreationAndBasics {
        @Test
        void defaultConstructor_shouldCreateEmptyContext() {
            ObjectNode json = context.toJsonLd();
            assertNotNull(json.get("@context"));
            assertTrue(json.get("@context").isEmpty());
        }

        @Test
        void fromJson_withValidContext_shouldCreateEquivalentContext() {
            ObjectMapper mapper = MyObjectMapper.getMapper();
            ObjectNode input = mapper.createObjectNode();
            input.put("schema", SCHEMA_URL);

            Context newContext = Context.fromJson(input);
            assertEquals(SCHEMA_URL, newContext.getDefinition("schema"));
        }

        @Test
        void fromJson_withInvalidJson_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                Context.fromJson(null)
            );
        }
    }

    @Nested
    class TermDefinitions {
        @Test
        void defineTerm_withValidTuple_shouldAddDefinition() {
            context.define("name", "https://schema.org/name");
            assertTrue(context.isValidTerm("name"));
            assertEquals("https://schema.org/name", context.getDefinition("name"));
        }

        @Test
        void defineTerm_withNull_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                context.define(null, "https://example.org")
            );
            assertThrows(IllegalArgumentException.class, () ->
                context.define("term", null)
            );
        }

        @Test
        void defineTerm_withInvalidIRI_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                context.define("term", "not a valid IRI")
            );
        }
    }

    @Nested
    class PrefixHandling {
        @Test
        void definePrefix_withValidIRI_shouldAllowPrefixedTerms() {
            context.define("schema", SCHEMA_URL);
            assertTrue(context.isValidTerm("schema:name"));
        }

        @Test
        void validateTerm_withUndefinedPrefix_shouldReturnFalse() {
            assertFalse(context.isValidTerm("undefined:term"));
        }

        @Test
        void validateTerm_withDefinedPrefixButInvalidTerm_shouldReturnFalse() {
            context.define("ex", EXAMPLE_URL);
            assertFalse(context.isValidTerm("ex:nonexistentTerm"));
        }
    }

    @Nested
    class ContextResolution {
        @Test
        void addContext_withResolvableUrl_shouldExpandContext() {
            context.addContext(URI.create(SCHEMA_URL));
            assertTrue(context.isValidTerm("Person"));
            assertTrue(context.isValidTerm("name"));
        }

        @Test
        void addContext_withUnresolvableUrl_shouldThrow() {
            URI unreachableUri = URI.create("http://nonexistent.example.org/");
            assertThrows(ContextLoadException.class, () ->
                context.addContext(unreachableUri)
            );
        }

        @Test
        void addContext_withLocalFile_shouldLoadFromFileSystem(@TempDir Path tempDir) throws IOException {
            Path contextFile = tempDir.resolve("test-context.jsonld");
            Files.writeString(contextFile, """
                {
                    "@context": {
                        "test": "http://example.org/test#",
                        "LocalTerm": "test:LocalTerm"
                    }
                }
                """);

            context.addContext(contextFile.toUri());
            assertTrue(context.isValidTerm("LocalTerm"));
        }

        @Test
        void addContext_withNonexistentLocalFile_shouldThrow(@TempDir Path tempDir) {
            Path nonExistentFile = tempDir.resolve("nonexistent.jsonld");
            URI fileUri = nonExistentFile.toUri();

            assertThrows(ContextLoadException.class, () ->
                context.addContext(fileUri)
            );
        }

        @Test
        void addContext_withInvalidContent_shouldThrow(@TempDir Path tempDir) throws IOException {
            Path invalidFile = tempDir.resolve("invalid.jsonld");
            Files.writeString(invalidFile, "{ invalid json");

            assertThrows(ContextLoadException.class, () ->
                context.addContext(invalidFile.toUri())
            );
        }
    }

    @Nested
    class TermValidation {
        @Test
        void isValidTerm_withAbsoluteIRI_shouldReturnTrue() {
            assertTrue(context.isValidTerm("https://schema.org/Person"));
        }

        @Test
        void isValidTerm_withRelativeIRI_shouldReturnFalse() {
            assertFalse(context.isValidTerm("Person"));
        }

        @Test
        void isValidTerm_withDefinedTerm_shouldReturnTrue() {
            context.define("person", "https://schema.org/Person");
            assertTrue(context.isValidTerm("person"));
        }

        @Test
        void isValidTerm_withPrefixedTerm_shouldValidateAgainstPrefix() {
            context.define("schema", SCHEMA_URL);
            assertTrue(context.isValidTerm("schema:Person"));
            assertFalse(context.isValidTerm("schema:NonexistentType"));
        }
    }

    @Nested
    class JsonSerialization {
        @Test
        void toJsonLd_withEmptyContext_shouldReturnMinimalStructure() {
            ObjectNode json = context.toJsonLd();
            assertNotNull(json.get("@context"));
            assertTrue(json.get("@context").isEmpty());
        }

        @Test
        void toJsonLd_withDefinitions_shouldIncludeAllDefinitions() {
            context.define("schema", SCHEMA_URL);
            context.define("name", "https://schema.org/name");

            ObjectNode json = context.toJsonLd();
            JsonNode contextNode = json.get("@context");
            assertEquals(SCHEMA_URL, contextNode.get("schema").asText());
            assertEquals("https://schema.org/name", contextNode.get("name").asText());
        }
    }

    @Nested
    class ContextModification {
        @Test
        void remove_existingDefinition_shouldRemoveDefinition() {
            context.define("term", "https://example.org/term");
            assertTrue(context.isValidTerm("term"));

            context.remove("term");
            assertFalse(context.isValidTerm("term"));
        }

        @Test
        void remove_nonexistentDefinition_shouldNotThrowException() {
            assertDoesNotThrow(() -> context.remove("nonexistent"));
        }

        @Test
        void getDefinitions_shouldReturnUnmodifiableMap() {
            context.define("term", "https://example.org/term");
            Map<String, String> definitions = context.getDefinitions();

            assertTrue(definitions.containsKey("term"));
            assertTrue(definitions.containsValue("https://example.org/term"));

            assertThrows(UnsupportedOperationException.class, () ->
                definitions.put("new", "value")
            );
        }

        @Test
        void removeContext_shouldRemoveAllAssociatedTerms() throws IOException {
            URI contextUri = URI.create(SCHEMA_URL);
            context.addContext(contextUri);
            assertTrue(context.isValidTerm("Person"));

            context.removeContext(contextUri);
            assertFalse(context.isValidTerm("Person"));
        }

        @Test
        void getDefinitions_shouldIncludeContextTerms() throws IOException {
            URI contextUri = URI.create(SCHEMA_URL);
            context.addContext(contextUri);
            Map<String, String> definitions = context.getDefinitions();

            assertTrue(definitions.containsKey("Person"));
            assertTrue(definitions.containsValue(SCHEMA_URL + "Person"));
        }
    }
}
