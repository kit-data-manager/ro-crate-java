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

    @Test
    public void testAlias(@TempDir Path tmpDir) throws IOException {
        Path zip = tmpDir.resolve("test.eln").toAbsolutePath();
        RoCrate crate = CommonReaderTest.newBaseCrate().build();

        new CrateWriter<>(new ZipStrategy().withRootSubdirectory())
                .save(crate, zip.toString());

        assertTrue(zip.toFile().exists(), "The zip file should exist");
        Path extractedPath = tmpDir.resolve("extracted");
        ensureCrateIsExtractedIn(zip, extractedPath);
    }
}
