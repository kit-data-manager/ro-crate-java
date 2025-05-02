package edu.kit.datamanager.ro_crate.examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;

import org.junit.jupiter.api.Test;

import static edu.kit.datamanager.ro_crate.HelpFunctions.printAndAssertEquals;

/**
 * This class contains examples of the RO-Crate specification version 1.1.
 * <p>
 * This is supposed to serve both as a user guide and as a test for the implementation.
 * Executing a test may also print some interesting information to the console.
 */
public class ExamplesOfSpecificationV1p1Test {

    /**
     * From: <a href="https://www.researchobject.org/ro-crate/specification/1.1/root-data-entity.html#minimal-example-of-ro-crate">
     *     Minimal Example
     * </a> (<a href="src/test/resources/spec-v1.1-example-json-files/minimal.json">location in repo</a>)
     * <p>
     * This is equivalent to {@link #testMinimalCrateWithoutCrateBuilder()}, but using more convenient APIs.
     */
    @Test
    void testMinimalCrateConvenient() {
        String licenseID = "https://creativecommons.org/licenses/by-nc-sa/3.0/au/";
        RoCrate minimal = new RoCrate.RoCrateBuilder(
                "Data files associated with the manuscript:Effects of facilitated family case conferencing for ...",
                "Palliative care planning for nursing home residents with advanced dementia ...",
                "2017",
                licenseID
        )
                // We already had to set the license ID in the builder,
                // but we can override it with more details to fit the example:
                .setLicense( new ContextualEntity.ContextualEntityBuilder()
                        .addType("CreativeWork")
                        .setId(licenseID)
                        .addProperty("description", "This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/au/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.")
                        .addProperty("identifier", licenseID)
                        .addProperty("name", "Attribution-NonCommercial-ShareAlike 3.0 Australia (CC BY-NC-SA 3.0 AU)")
                        .build()
                )
                .addIdentifier("https://doi.org/10.4225/59/59672c09f4a4b")
                .build();

        printAndAssertEquals(minimal, "/spec-v1.1-example-json-files/minimal.json");
    }

    /**
     * From: <a href="https://www.researchobject.org/ro-crate/specification/1.1/root-data-entity.html#minimal-example-of-ro-crate">
     *     Minimal Example
     * </a> (<a href="src/test/resources/spec-v1.1-example-json-files/minimal.json">location in repo</a>)
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

        // This is pretty low-level. We are considering hiding/replacing this detailed API in major versions,
        // so tell us (for example, open an issue) if you have a use case for it!
        minimal.setJsonDescriptor(new ContextualEntity.ContextualEntityBuilder()
                .setId("ro-crate-metadata.json")
                .addType("CreativeWork")
                .addIdProperty("about", "./")
                .addIdProperty("conformsTo", "https://w3id.org/ro/crate/1.1")
                .build()
        );
        minimal.addContextualEntity(license);

        printAndAssertEquals(minimal, "/spec-v1.1-example-json-files/minimal.json");
    }
}
