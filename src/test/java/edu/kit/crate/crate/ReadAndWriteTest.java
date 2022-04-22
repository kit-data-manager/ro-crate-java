package edu.kit.crate.crate;

import edu.kit.crate.Crate;
import edu.kit.crate.RoCrate;
import edu.kit.crate.preview.CustomPreview;
import edu.kit.crate.reader.FolderReader;
import edu.kit.crate.reader.RoCrateReader;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.RoCrateWriter;
import edu.kit.crate.HelpFunctions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReadAndWriteTest {

  @Test
  void testReadingAndWriting(@TempDir Path path) throws IOException {
    Path htmlFile = path.resolve("htmlFile.html");
    FileUtils.writeStringToFile(htmlFile.toFile(), "useful file", Charset.defaultCharset());
    Path htmlDir = path.resolve("dir");
    Path fileInDir = htmlDir.resolve("file.html");
    FileUtils.writeStringToFile(fileInDir.toFile(), "fileN2", Charset.defaultCharset());

    RoCrate crate = new RoCrate.RoCrateBuilder("name", "description")
        .setPreview(new CustomPreview(htmlFile.toFile(), htmlDir.toFile()))
        .build();

    Path writeDir = path.resolve("crate");

    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());
    writer.save(crate, writeDir.toAbsolutePath().toString());

    RoCrateReader reader = new RoCrateReader(new FolderReader());
    Crate newCrate = reader.readCrate(writeDir.toAbsolutePath().toString());

    // the preview files as well as the metadata file should not be included here
    assertEquals(0, newCrate.getUntrackedFiles().size());

    HelpFunctions.compareTwoCrateJson(newCrate, crate);
  }
}
