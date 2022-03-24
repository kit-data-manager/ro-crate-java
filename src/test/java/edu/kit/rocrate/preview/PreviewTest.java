package edu.kit.rocrate.preview;

import edu.kit.crate.ROCrate;
import edu.kit.crate.preview.AutomaticPreview;
import edu.kit.crate.preview.CustomPreview;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreviewTest {

  @Test
  void testAutomaticPreview(@TempDir Path temp) {
    Path location = temp.resolve("ro_crate");
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description")
        .setPreview(new AutomaticPreview())
        .build();
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(crate, location.toFile().toString());
    assertTrue(location.resolve("ro-crate-preview.html").toFile().exists());
  }

  @Test
  void testAutomaticPreviewAddingLater(@TempDir Path temp) {
    Path location = temp.resolve("ro_crate");
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description").build();
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(crate, location.toFile().toString());
    assertFalse(location.resolve("ro-crate-preview.html").toFile().exists());
    crate.setRoCratePreview(new AutomaticPreview());
    writer.save(crate, location.toFile().toString());
    assertTrue(location.resolve("ro-crate-preview.html").toFile().exists());
  }

  @Test
  void testCustomPreviewOnlyHtmlFile(@TempDir Path temp) throws IOException {
    Path location = temp.resolve("ro_crate");
    Path previewFile = temp.resolve("random.html");
    FileUtils.writeStringToFile(previewFile.toFile(), "random html it is not important that it is valid for know", Charset.defaultCharset());
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description")
        .setPreview(new CustomPreview(previewFile.toFile()))
        .build();
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(crate, location.toFile().toString());
    assertTrue(location.resolve("ro-crate-preview.html").toFile().exists());
  }

  @Test
  void testCustomPreviewOnlyHtmlFileWithOtherFiles(@TempDir Path temp) throws IOException {
    Path location = temp.resolve("ro_crate");
    Path previewFile = temp.resolve("random.html");
    FileUtils.writeStringToFile(previewFile.toFile(), "random html it is not important that it is valid for know", Charset.defaultCharset());
    Path dirHtml = temp.resolve("html_dir");
    Path css_file = dirHtml.resolve("test.css");
    FileUtils.writeStringToFile(css_file.toFile(), "random css it is not important that it is valid for know", Charset.defaultCharset());
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description")
        .setPreview(new CustomPreview(previewFile.toFile(), dirHtml.toFile()))
        .build();
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(crate, location.toFile().toString());
    assertTrue(location.resolve("ro-crate-preview.html").toFile().exists());
    assertTrue(location.resolve("ro-crate-preview_files").toFile().exists());
    assertTrue(location.resolve("ro-crate-preview_files").resolve("test.css").toFile().exists());
  }

}
