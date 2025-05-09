package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.writer.Writers;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ZipReaderTest implements
        CommonReaderTest<String, ZipStrategy>,
        ElnFileFormatTest<String, ZipStrategy>
{
    @Override
    public void saveCrate(Crate crate, Path target) throws IOException {
        Writers.newZipPathWriter().save(crate, target.toAbsolutePath().toString());
        assertTrue(target.toFile().isFile());
    }

    @Override
    public Crate readCrate(Path source) throws IOException {
        return Readers.newZipPathReader().readCrate(source.toAbsolutePath().toString());
    }

    @Override
    public ZipStrategy newReaderStrategyWithTmp(Path tmpDirectory, boolean useUuidSubfolder) {
        ZipStrategy strategy = new ZipStrategy(tmpDirectory, useUuidSubfolder);
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
    public Crate readCrate(ZipStrategy strategy, Path source) throws IOException {
        Crate importedCrate = new CrateReader<>(strategy)
                .readCrate(source.toAbsolutePath().toString());
        assertTrue(strategy.isExtracted());
        return importedCrate;
    }
}
