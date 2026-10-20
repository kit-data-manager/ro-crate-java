package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.writer.Writers;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Legacy variant of {@code ReadCrate}, written against the released 2.1.2
 * API. The example code is identical to the current variant; only
 * {@link CanonicalJson} uses the Jackson API instead of the neutral JSON
 * model.
 */
public class ReadCrate {

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
    Path output = Path.of(args[0]);
    Path zip = output.getParent().resolve("simple_crate.zip");
    RoCrate folderCrate = Readers.newFolderReader().readCrate(
      FIXTURE.toString()
    );
    // suppress provenance: it would stamp library version and creation time
    // into the metadata and is not part of the comparison
    Writers.newZipPathWriter()
      .withAutomaticProvenance(null)
      .save(folderCrate, zip.toAbsolutePath().toString());
    RoCrate zipCrate = Readers.newZipPathReader().readCrate(
      zip.toAbsolutePath().toString()
    );

    StringBuilder report = new StringBuilder();
    report
      .append("from folder\n")
      .append(CanonicalJson.canonicalize(folderCrate.getJsonMetadata()))
      .append("\n\nfrom zip\n")
      .append(CanonicalJson.canonicalize(zipCrate.getJsonMetadata()))
      .append("\n");
    Files.writeString(output, report.toString());
  }
}
