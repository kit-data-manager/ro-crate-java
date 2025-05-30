package edu.kit.datamanager.ro_crate.writer;

import java.io.*;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.RoCrate;

/**
 * @author jejkal
 */
class ZipStreamWriterTest implements
        CommonWriterTest,
        ElnFileWriterTest
{

  @Override
  public void saveCrate(Crate crate, Path target) throws IOException {
    try (FileOutputStream stream = new FileOutputStream(target.toFile())) {
      Writers.newZipStreamWriter().save(crate, stream);
    }
  }

  @Override
  public void saveCrateElnStyle(Crate crate, Path target) throws IOException {
    try (FileOutputStream stream = new FileOutputStream(target.toFile())) {
      new CrateWriter<>(new WriteZipStreamStrategy().usingElnStyle())
              .save(crate, stream);
    }
  }

  @Override
  public void saveCrateSubdirectoryStyle(RoCrate crate, Path target) throws IOException {
    try (FileOutputStream stream = new FileOutputStream(target.toFile())) {
      new CrateWriter<>(new WriteZipStreamStrategy().withRootSubdirectory())
              .save(crate, stream);
    }
  }
}
