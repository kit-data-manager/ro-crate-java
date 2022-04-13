package edu.kit.crate.preview;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreviewTest {

  @Test
  void customPreviewSaveToFolderTest(@TempDir Path dir) throws IOException {
    var file1 = dir.resolve("file.html");
    FileUtils.writeStringToFile(file1.toFile(), "random html it is not important that it is valid for know", Charset.defaultCharset());

    var file2 = dir.resolve("directory");
    var fileInDir = file2.resolve("fileInDir.html");
    FileUtils.writeStringToFile(fileInDir.toFile(), "dajkdlfjdsklafj alksfjdalk fjl", Charset.defaultCharset());
    CustomPreview customPreview = new CustomPreview(file1.toFile(), file2.toFile());

    customPreview.saveALLToFolder(dir.resolve("result").toFile());

    var e = dir.resolve("result");

    var roPreview = e.resolve("ro-crate-preview.html");
    var roDir = e.resolve("ro-crate-preview_files");
    var roDirFile = roDir.resolve("fileInDir.html");
    assertTrue(Files.isRegularFile(roPreview));
    assertTrue(Files.isDirectory(roDir));
    assertTrue(Files.isRegularFile(roDirFile));

    assertTrue(FileUtils.contentEqualsIgnoreEOL(roPreview.toFile(), file1.toFile(), String.valueOf(Charset.defaultCharset())));
    assertFalse(FileUtils.contentEqualsIgnoreEOL(roPreview.toFile(), fileInDir.toFile(), String.valueOf(Charset.defaultCharset())));

    assertTrue(FileUtils.contentEqualsIgnoreEOL(roDirFile.toFile(), fileInDir.toFile(), String.valueOf(Charset.defaultCharset())));
  }

  @Test
  void customPreviewSaveToZip(@TempDir Path dir) throws IOException {
    var file1 = dir.resolve("file.html");
    FileUtils.writeStringToFile(file1.toFile(), "random html it is not important that it is valid for know", Charset.defaultCharset());

    var file2 = dir.resolve("directory");
    var fileInDir = file2.resolve("fileInDir.html");
    FileUtils.writeStringToFile(fileInDir.toFile(), "dajkdlfjdsklafj alksfjdalk fjl", Charset.defaultCharset());
    CustomPreview customPreview = new CustomPreview(file1.toFile(), file2.toFile());

    customPreview.saveALLToZip(new ZipFile(dir.resolve("destination.zip").toFile()));
    new ZipFile(dir.resolve("destination.zip").toFile()).extractAll(dir.resolve("extracted").toAbsolutePath().toString());

    var e = dir.resolve("extracted");
    var roPreview = e.resolve("ro-crate-preview.html");
    var roDir = e.resolve("ro-crate-preview_files");
    var roDirFile = roDir.resolve("fileInDir.html");
    assertTrue(Files.isRegularFile(roPreview));
    assertTrue(Files.isDirectory(roDir));
    assertTrue(Files.isRegularFile(roDirFile));

    assertTrue(FileUtils.contentEqualsIgnoreEOL(roPreview.toFile(), file1.toFile(), String.valueOf(Charset.defaultCharset())));
    assertFalse(FileUtils.contentEqualsIgnoreEOL(roPreview.toFile(), fileInDir.toFile(), String.valueOf(Charset.defaultCharset())));

    assertTrue(FileUtils.contentEqualsIgnoreEOL(roDirFile.toFile(), fileInDir.toFile(), String.valueOf(Charset.defaultCharset())));

  }

  @Test
  void testAutomaticPreviewAddToFolder(@TempDir Path dir) throws IOException {
    AutomaticPreview automaticPreview = new AutomaticPreview();

    InputStream crateJson = PreviewTest.class.getResourceAsStream("/crates/simple_crate/ro-crate-metadata.json");
    Path crate = dir.resolve("crate");
    // this crate will not have a json file
    Path fakeCrate = dir.resolve("fakeCrate");
    FileUtils.forceMkdir(crate.toFile());
    FileUtils.copyInputStreamToFile(crateJson, crate.resolve("ro-crate-metadata.json").toFile());

    automaticPreview.saveALLToFolder(crate.toFile());

    // the program should not crash
    automaticPreview.saveALLToFolder(fakeCrate.toFile());

    // there should be a html file generated
    assertTrue(Files.isRegularFile(crate.resolve("ro-crate-preview.html")));
  }

  @Test
  void testAutomaticPreviewZip(@TempDir Path dir) throws IOException {
    AutomaticPreview automaticPreview = new AutomaticPreview();
    InputStream crateJson = PreviewTest.class.getResourceAsStream("/crates/simple_crate/ro-crate-metadata.json");
    Path crate = dir.resolve("crate");
    ZipParameters zipParameters = new ZipParameters();
    zipParameters.setFileNameInZip("ro-crate-metadata.json");

    ZipFile zipFile = new ZipFile(dir.resolve("test.zip").toFile());
    zipFile.addStream(crateJson, zipParameters);
    crateJson.close();

    automaticPreview.saveALLToZip(zipFile);

    // this should trow an exception but not stop the execution
    ZipFile randomZipFile = new ZipFile(dir.resolve("dddd.zip").toFile());
    automaticPreview.saveALLToZip(randomZipFile);

    zipFile.extractAll(crate.toString());
    assertTrue(Files.isRegularFile(crate.resolve("ro-crate-preview.html")));
  }
}
