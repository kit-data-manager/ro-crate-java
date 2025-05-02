package edu.kit.datamanager.ro_crate.examples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;

/**
 * This class contains examples of the RO-Crate specification version 1.1.
 */
public class ExamplesOfSpecificationV1p1Test {

    /**
     * From: <a href="https://www.researchobject.org/ro-crate/specification/1.1/root-data-entity.html#minimal-example-of-ro-crate">
     *     Minimal Example
     * </a>
     * <p>
     * This is equivalent to {@link #testMinimalCrateWithoutCrateBuilder()}, but using more convenient APIs.
     */
    @Test
    void testMinimalCrateConvenient() {
        // Example 1: Basic RO-Crate
        RoCrate minimal = new RoCrate.RoCrateBuilder(
                "Data files associated with the manuscript:Effects of facilitated family case conferencing for ...",
                "Palliative care planning for nursing home residents with advanced dementia ...",
                "2017",
                "https://creativecommons.org/licenses/by-nc-sa/3.0/au/"
        )
                .setLicense( new ContextualEntity.ContextualEntityBuilder()
                        .addType("CreativeWork")
                        .setId("https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                        .addProperty("description", "This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/au/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.")
                        .addProperty("identifier", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                        .addProperty("name", "Attribution-NonCommercial-ShareAlike 3.0 Australia (CC BY-NC-SA 3.0 AU)")
                        .build()
                )
                .addIdentifier("https://doi.org/10.4225/59/59672c09f4a4b")
                .build();

        // So you get something to see
        prettyPrintJsonString(minimal.getJsonMetadata());
        // Compare with the example from the specification
        try {
            HelpFunctions.compareCrateJsonToFileInResources(minimal, "/spec-v1.1-example-json-files/minimal.json");
        } catch (IOException e) {
            throw new AssertionFailedError("Missing resources file!", e);
        }
    }

    /**
     * From: <a href="https://www.researchobject.org/ro-crate/specification/1.1/root-data-entity.html#minimal-example-of-ro-crate">
     *     Minimal Example
     * </a>
     * <p>
     * In this example, the crate is created without the builder.
     * Otherwise, the example is the same as {@link #testMinimalCrateConvenient()}.
     */
    @Test
    void testMinimalCrateWithoutCrateBuilder() {
        RoCrate minimal = new RoCrate();

        ContextualEntity license = new ContextualEntity.ContextualEntityBuilder()
                .addType("CreativeWork")
                .setId("https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                .addProperty("description", "This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/au/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.")
                .addProperty("identifier", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
                .addProperty("name", "Attribution-NonCommercial-ShareAlike 3.0 Australia (CC BY-NC-SA 3.0 AU)")
                .build();

        minimal.setRootDataEntity(new RootDataEntity.RootDataEntityBuilder()
                .addProperty("identifier", "https://doi.org/10.4225/59/59672c09f4a4b")
                .addProperty("datePublished", "2017")
                .addProperty("name", "Data files associated with the manuscript:Effects of facilitated family case conferencing for ...")
                .addProperty("description", "Palliative care planning for nursing home residents with advanced dementia ...")
                .setLicense(license)
                .build());

        minimal.setJsonDescriptor(new ContextualEntity.ContextualEntityBuilder()
                .setId("ro-crate-metadata.json")
                .addType("CreativeWork")
                .addIdProperty("about", "./")
                .addIdProperty("conformsTo", "https://w3id.org/ro/crate/1.1")
                .build()
        );
        minimal.addContextualEntity(license);

        // Print resulting json to console
        prettyPrintJsonString(minimal.getJsonMetadata());
        // Compare with the example from the specification
        try {
            HelpFunctions.compareCrateJsonToFileInResources(minimal, "/spec-v1.1-example-json-files/minimal.json");
        } catch (IOException e) {
            throw new AssertionFailedError("Missing resources file!", e);
        }
    }

    protected static void prettyPrintJsonString(String minimalJsonMetadata) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(minimalJsonMetadata);
            // Enable pretty printing
            String prettyJson = objectMapper
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(jsonNode);
            // Print the pretty JSON
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            throw new AssertionFailedError("Not able to process string as JSON!", e);
        }
    }
}
