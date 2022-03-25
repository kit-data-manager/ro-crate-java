package edu.kit.rocrate.crate.realexamples;

import edu.kit.crate.IROCrate;
import edu.kit.crate.reader.ROCrateReader;
import edu.kit.crate.reader.ZipReader;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;
import edu.kit.rocrate.HelpFunctions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class WorkflowHubTest {

  @Test
  void testImportZip(@TempDir Path temp) throws IOException {
    ROCrateReader reader = new ROCrateReader(new ZipReader());
    IROCrate crate = reader.readCrate(WorkflowHubTest.class.getResource("/crates/workflowhub/workflow-109-5.crate.zip").getPath());

    HelpFunctions.compareCrateJsonToFileInResources(crate, "/crates/workflowhub/workflow1/ro-crate-metadata.json");
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(crate, temp.toString());
    HelpFunctions.compareTwoDir(temp.toFile(), new File(WorkflowHubTest.class.getResource("/crates/workflowhub/workflow1/").getPath()));
  }
}
