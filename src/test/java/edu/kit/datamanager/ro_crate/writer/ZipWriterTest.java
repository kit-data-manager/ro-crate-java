package edu.kit.datamanager.ro_crate.writer;

import java.io.IOException;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.CommonReaderTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipWriterTest implements
        CommonWriterTest,
        ElnFileWriterTest
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

    @Override
    public void saveCrateSubdirectoryStyle(RoCrate crate, Path target) throws IOException {
        new CrateWriter<>(new ZipStrategy().withRootSubdirectory())
                .save(crate, target.toString());
    }
}
