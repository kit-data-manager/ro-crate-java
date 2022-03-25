package edu.kit.rocrate.crate;

import edu.kit.crate.ROCrate;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;
import edu.kit.rocrate.HelpFunctions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OtherFilesTest {

  /**
   * Test if adding untracked files to the crate a being included inside it
   */
  @Test
  void testOtherFiles(@TempDir Path tempDir) throws IOException, URISyntaxException {
    Path crate = tempDir.resolve("crate");
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");
    FileUtils.touch(file1.toFile());
    FileUtils.touch(file2.toFile());
    FileUtils.writeStringToFile(file1.toFile(), "content of file 1", Charset.defaultCharset());
    FileUtils.writeStringToFile(file2.toFile(), "content of file 2", Charset.defaultCharset());
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addUntrackedFile(file1.toFile())
        .addUntrackedFile(file2.toFile())
        .build();

    HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/simple.json");

    // write the crate in the temp dir
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(roCrate, crate.toFile().getAbsolutePath());

    HelpFunctions.compareCrateJsonToFileInResources(new File(Objects.requireNonNull(OtherFilesTest.class.getResource("/json/crate/simple.json")).toURI()), crate.resolve("ro-crate-metadata.json").toFile());

    assertTrue(FileUtils.contentEqualsIgnoreEOL(file1.toFile(), crate.resolve("file1.txt").toFile(), null));
    assertTrue(FileUtils.contentEqualsIgnoreEOL(file2.toFile(), crate.resolve("file2.txt").toFile(), null));
    assertFalse(FileUtils.contentEqualsIgnoreEOL(file1.toFile(), crate.resolve("file2.txt").toFile(), null));
  }
}
