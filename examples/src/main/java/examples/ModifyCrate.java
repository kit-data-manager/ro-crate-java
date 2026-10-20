package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.reader.Readers;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads a fixture crate from disk and modifies it: adds and removes entities,
 * adds and removes properties. Writes the canonical metadata after each step
 * (see {@link CanonicalJson}), so the comparison covers the mutation paths of
 * the library.
 */
public class ModifyCrate {

  static final Path FIXTURE = Path.of(
    "..",
    "core",
    "src",
    "test",
    "resources",
    "crates",
    "simple_crate"
  );

  public static void main(String[] args) throws Exception {
    RoCrate crate = Readers.newFolderReader().readCrate(FIXTURE.toString());
    StringBuilder report = new StringBuilder();

    report
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()))
      .append("\n\n");

    // add a data entity, then delete it again
    crate.addDataEntity(
      new DataEntity.DataEntityBuilder()
        .setId("data.txt")
        .addType("File")
        .addProperty("name", "Some data")
        .build()
    );
    report
      .append("after addDataEntity(data.txt)\n")
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()))
      .append("\n\n");
    crate.deleteEntityById("data.txt");
    report
      .append("after deleteEntityById(data.txt)\n")
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()))
      .append("\n\n");

    // add it back with different properties
    crate.addDataEntity(
      new DataEntity.DataEntityBuilder()
        .setId("data.txt")
        .addType("File")
        .addProperty("name", "Some other data")
        .addProperty("encodingFormat", "text/plain")
        .build()
    );
    report
      .append("after re-adding data.txt with different properties\n")
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()))
      .append("\n\n");

    // modify properties of the root data entity
    crate.getRootDataEntity().removeProperty("datePublished");
    crate.getRootDataEntity().addProperty("keywords", "testing, parity");
    report
      .append("after root data entity property changes\n")
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()))
      .append("\n\n");

    // add and remove a contextual entity
    ContextualEntity organization =
      new ContextualEntity.ContextualEntityBuilder()
        .setId("https://ror.org/04jsmm180")
        .addType("Organization")
        .addProperty("name", "Karlsruhe Institute of Technology")
        .build();
    crate.addContextualEntity(organization);
    report
      .append("after addContextualEntity(organization)\n")
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()))
      .append("\n\n");
    crate.deleteEntityById("https://ror.org/04jsmm180");
    report
      .append("after deleteEntityById(organization)\n")
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()));

    Files.writeString(Path.of(args[0]), report.toString());
  }
}
