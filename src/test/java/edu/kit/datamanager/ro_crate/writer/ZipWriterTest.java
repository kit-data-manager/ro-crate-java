package edu.kit.datamanager.ro_crate.writer;

import java.io.IOException;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.Crate;

class ZipWriterTest implements
        CommonWriterTest,
        ElnFileFormatTest
{
    @Override
    public void saveCrate(Crate crate, Path target) throws IOException {
        Writers.newZipPathWriter()
                .save(crate, target.toAbsolutePath().toString());
    }

    @Override
    public void saveCrateElnStyle(Crate crate, Path target) throws IOException {
        new CrateWriter<>(new ZipStrategy().usingElnStyle())
                .save(crate, target.toAbsolutePath().toString());
    }
}
