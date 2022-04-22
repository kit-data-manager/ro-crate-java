package edu.kit.crate.crate.realexamples;

import edu.kit.crate.Crate;
import edu.kit.crate.reader.RoCrateReader;
import edu.kit.crate.reader.ZipReader;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.RoCrateWriter;
import edu.kit.crate.HelpFunctions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class WorkflowHubTest {

  @Test
  void testImportZip(@TempDir Path temp) throws IOException {
    RoCrateReader reader = new RoCrateReader(new ZipReader());
    Crate crate = reader.readCrate(WorkflowHubTest.class.getResource("/crates/workflowhub/workflow-109-5.crate.zip").getPath());

    HelpFunctions.compareCrateJsonToFileInResources(crate, "/crates/workflowhub/workflow1/ro-crate-metadata.json");
    RoCrateWriter writer = new RoCrateWriter(new FolderWriter());
    writer.save(crate, temp.toString());
    HelpFunctions.compareTwoDir(temp.toFile(), new File(WorkflowHubTest.class.getResource("/crates/workflowhub/workflow1/").getPath()));
  }
}
