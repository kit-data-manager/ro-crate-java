package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.writer.Writers;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Legacy variant of {@code WriteCrate}, written against the released 2.1.2
 * API. The example code is identical to the current variant; only
 * {@link CanonicalJson} uses the Jackson API instead of the neutral JSON
 * model.
 */
public class WriteCrate {

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
    Path workDir = Path.of(args[0]).getParent();
    Path folder = workDir.resolve("written-crate");
    Path zip = workDir.resolve("written-crate.zip");

    RoCrate crate = Readers.newFolderReader().readCrate(FIXTURE.toString());
    Writers.newFolderWriter()
      .withAutomaticProvenance(null)
      .save(crate, folder.toAbsolutePath().toString());
    Writers.newZipPathWriter()
      .withAutomaticProvenance(null)
      .save(crate, zip.toAbsolutePath().toString());

    StringBuilder report = new StringBuilder();
    report
      .append("metadata\n")
      .append(CanonicalJson.canonicalize(crate.getJsonMetadata()))
      .append("\n\nfolder files (name: sha256)\n")
      .append(folderFileDigests(folder))
      .append("\nzip entries (name: sha256)\n")
      .append(zipEntryDigests(zip))
      .append("\n");
    Files.writeString(Path.of(args[0]), report.toString());
  }

  static String folderFileDigests(Path folder) throws Exception {
    List<Path> files;
    try (Stream<Path> walk = Files.walk(folder)) {
      files = walk
        .filter(Files::isRegularFile)
        .sorted(Comparator.comparing(Path::toString))
        .toList();
    }
    StringBuilder report = new StringBuilder();
    for (Path file : files) {
      report
        .append(folder.relativize(file).toString())
        .append(": ")
        .append(digest(file))
        .append("\n");
    }
    return report.toString();
  }

  static String zipEntryDigests(Path zip) throws Exception {
    StringBuilder report = new StringBuilder();
    try (ZipFile zipFile = new ZipFile(zip.toFile())) {
      List<? extends ZipEntry> entries = zipFile
        .stream()
        .sorted(Comparator.comparing(ZipEntry::getName))
        .toList();
      for (ZipEntry entry : entries) {
        report
          .append(entry.getName())
          .append(": ")
          .append(digest(() -> zipFile.getInputStream(entry)))
          .append("\n");
      }
    }
    return report.toString();
  }

  static String digest(Path file) throws Exception {
    return digest(() -> Files.newInputStream(file));
  }

  static String digest(InputStreamSupplier supplier) throws Exception {
    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    try (InputStream stream = supplier.open()) {
      byte[] buffer = new byte[8192];
      int read;
      while ((read = stream.read(buffer)) != -1) {
        sha256.update(buffer, 0, read);
      }
    }
    StringBuilder hex = new StringBuilder();
    for (byte b : sha256.digest()) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }

  interface InputStreamSupplier {
    InputStream open() throws Exception;
  }
}
