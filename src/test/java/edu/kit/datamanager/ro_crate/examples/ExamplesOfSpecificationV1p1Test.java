package edu.kit.datamanager.ro_crate.examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.PersonEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.PlaceEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;

import edu.kit.datamanager.ro_crate.writer.CrateWriter;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Paths;

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
     * This example produces a minimal crate with a
     * name, description, date, license and identifier.
     * <p>
     * This example produces the same result as
     * {@link #testMinimalCrateWithoutCrateBuilder()}, but using more convenient APIs.
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
     * In this example, the minimal crate is created without the builder.
     * This should only be done if necessary: Use the builder if possible.
     * This example produces the same result as {@link #testMinimalCrateConvenient()}.
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

    // https://www.researchobject.org/ro-crate/specification/1.1/data-entities.html#example-linking-to-a-file-and-folders

    /**
     * From: <a href="https://www.researchobject.org/ro-crate/specification/1.1/data-entities.html#example-linking-to-a-file-and-folders">
     *     "Example linking to a file and folders"
     * </a> (<a href="src/test/resources/spec-v1.1-example-json-files/files-and-folders.json.json">location in repo</a>)
     * <p>
     * This example adds a File(Entity) and a DataSet(Entity) to the crate.
     * The file and the folder are referenced by their location. This way
     * they will be copied to the crate when writing it using a
     * {@link CrateWriter}.
     * The name of the file and the folder will be implicitly set to the
     * ID of the respective entity in order to conform to the specification.
     * <p>
     * Here we use the inner builder classes for the construction of the
     * crate. In contrast to {@link #testMinimalCrateWithoutCrateBuilder()},
     * we do not have to care about specification details.
     */
    @Test
    void testLinkingToFileAndFolders() {
        RoCrate crate = new RoCrate.RoCrateBuilder()
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                // This will tell us where the file is located. It will be copied to the crate.
                                .setLocation(Paths.get("path to file"))
                                // If no ID is given explicitly, the ID will be set to the filename.
                                // Changing the ID means also to set the file name within the crate!
                                .setId("cp7glop.ai")
                                .addProperty("name", "Diagram showing trend to increase")
                                .addProperty("contentSize", "383766")
                                .addProperty("description", "Illustrator file for Glop Pot")
                                .setEncodingFormat("application/pdf")
                                .build()
                )
                .addDataEntity(
                        new DataSetEntity.DataSetBuilder()
                                .setLocation(Paths.get("path_to_files"))
                                .setId("lots_of_little_files/")
                                .addProperty("name", "Too many files")
                                .addProperty("description", "This directory contains many small files, that we're not going to describe in detail.")
                                .build()
                )
                .build();

        printAndAssertEquals(crate, "/spec-v1.1-example-json-files/files-and-folders.json");
    }

    /**
     * From: <a href="https://www.researchobject.org/ro-crate/specification/1.1/data-entities.html#web-based-data-entities">
     *     Example with web-based data entities
     * </a> (<a href="src/test/resources/spec-v1.1-example-json-files/web-based-data-entities.json">location in repo</a>)
     * <p>
     * This example adds twp FileEntities to the crate.
     * One is a local file, the other one is located in the web
     * and will not be copied to the crate.
     */
    @Test
    void testWebBasedDataEntities() {
        RoCrate crate = new RoCrate.RoCrateBuilder()
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                .setLocation(Paths.get("README.md"))
                                .setId("survey-responses-2019.csv")
                                .addProperty("name", "Survey responses")
                                .addProperty("contentSize", "26452")
                                .setEncodingFormat("text/csv")
                                .build()
                )
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                .setLocation(URI.create("https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf"))
                                .addProperty("name", "RO-Crate specification")
                                .addProperty("contentSize", "310691")
                                .addProperty("description", "RO-Crate specification")
                                .setEncodingFormat("application/pdf")
                                .build()
                )
                .build();

        printAndAssertEquals(crate, "/spec-v1.1-example-json-files/web-based-data-entities.json");
    }

    /**
     * From: <a href="https://www.researchobject.org/ro-crate/specification/1.1/appendix/jsonld.html">
     *     Example with file, author, and location
     * </a> (<a href="src/test/resources/spec-v1.1-example-json-files/file-author-location.json">location in repo</a>)
     */
    @Test
    void testWithFileAuthorLocation() {
        PersonEntity alice = new PersonEntity.PersonEntityBuilder()
                .setId("#alice")
                .addProperty("name", "Alice")
                .addProperty("description", "One of hopefully many Contextual Entities")
                .build();
        PlaceEntity park = new PlaceEntity.PlaceEntityBuilder()
                .setId(URI.create("http://sws.geonames.org/8152662/").toString())
                .addProperty("name", "Catalina Park")
                .build();

        RoCrate crate = new RoCrate.RoCrateBuilder(
                "Example RO-Crate",
                "The RO-Crate Root Data Entity",
                "2020",
                "https://spdx.org/licenses/CC-BY-NC-SA-4.0"
        )
                .addContextualEntity(park)
                .addContextualEntity(alice)
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                .setLocation(Paths.get("......."))
                                .setId("data2.txt")
                                .build()
                )
                .addDataEntity(
                        new FileEntity.FileEntityBuilder()
                                .setLocation(Paths.get("......."))
                                .setId("data1.txt")
                                .addProperty("description", "One of hopefully many Data Entities")
                                .addAuthor(alice.getId())
                                .addIdProperty("contentLocation", park)
                                .build()
                )
                .build();

        printAndAssertEquals(crate, "/spec-v1.1-example-json-files/file-author-location.json");
    }
}
