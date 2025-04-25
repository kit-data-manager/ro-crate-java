package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.writer.Writers;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class ZipStreamStrategyTest extends CrateReaderTest<InputStream, ZipStreamStrategy> {
    @Override
    protected void saveCrate(Crate crate, Path target) throws IOException {
        Writers.newZipStreamWriter().save(crate, new FileOutputStream(target.toFile()));
        assertTrue(target.toFile().isFile());
    }

    @Override
    protected Crate readCrate(Path source) throws IOException {
        return Readers.newZipStreamReader().readCrate(new FileInputStream(source.toFile()));
    }

    @Override
    protected ZipStreamStrategy newReaderStrategyWithTmp(Path tmpDirectory, boolean useUuidSubfolder) {
        ZipStreamStrategy strategy = new ZipStreamStrategy(tmpDirectory, useUuidSubfolder);
        assertFalse(strategy.isExtracted());
        if (useUuidSubfolder) {
            assertEquals(strategy.getTemporaryFolder().getFileName().toString(), strategy.getID());
        } else {
            assertEquals(strategy.getTemporaryFolder().getFileName().toString(), tmpDirectory.getFileName().toString());
        }
        assertTrue(strategy.getTemporaryFolder().startsWith(tmpDirectory));
        return strategy;
    }

    @Override
    protected Crate readCrate(ZipStreamStrategy strategy, Path source) throws IOException {
        Crate importedCrate = new CrateReader<>(strategy)
                .readCrate(new FileInputStream(source.toFile()));
        assertTrue(strategy.isExtracted());
        return importedCrate;
    }
}
