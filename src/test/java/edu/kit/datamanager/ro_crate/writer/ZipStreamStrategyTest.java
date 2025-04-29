package edu.kit.datamanager.ro_crate.writer;

import java.io.*;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.Crate;

/**
 * @author jejkal
 */
class ZipStreamStrategyTest extends CrateWriterTest {

  @Override
  protected void saveCrate(Crate crate, Path target) throws IOException {
    try (FileOutputStream stream = new FileOutputStream(target.toFile())) {
      Writers.newZipStreamWriter().save(crate, stream);
    }
  }
}
