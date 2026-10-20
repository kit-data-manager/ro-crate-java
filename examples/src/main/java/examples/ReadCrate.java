package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.writer.Writers;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads a crate from a folder and from a zip archive (the archive is created
 * from the fixture with the library's own zip writer) and writes the
 * canonical metadata for both paths (see {@link CanonicalJson}).
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
