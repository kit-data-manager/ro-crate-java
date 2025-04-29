package edu.kit.datamanager.ro_crate.writer;

import java.io.IOException;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.Crate;

class ZipWriterTest extends CrateWriterTest {
    @Override
    protected void saveCrate(Crate crate, Path target) throws IOException {
        Writers.newZipPathWriter()
                .save(crate, target.toAbsolutePath().toString());
    }
}
