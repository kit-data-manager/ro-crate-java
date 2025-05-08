package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.writer.Writers;

import java.io.*;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class ZipStreamReaderTest implements
        CommonReaderTest<InputStream, ZipStreamStrategy>,
        ElnFileFormatTest<InputStream, ZipStreamStrategy>
{
    /**
     * At the point of writing this test,
     *  these files are in a zip format which cannot be read in streaming mode
     */
    @Override
    public boolean isInBlacklist(String input) {
        return Set.of(
                "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/kadi4mat/records-example.eln",
                "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/kadi4mat/collections-example.eln"
        )
                .contains(input);
    }

    @Override
    public void saveCrate(Crate crate, Path target) throws IOException {
        Writers.newZipStreamWriter().save(crate, new FileOutputStream(target.toFile()));
        assertTrue(target.toFile().isFile());
    }

    @Override
    public Crate readCrate(Path source) throws IOException {
        return Readers.newZipStreamReader().readCrate(new FileInputStream(source.toFile()));
    }

    @Override
    public ZipStreamStrategy newReaderStrategyWithTmp(Path tmpDirectory, boolean useUuidSubfolder) {
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
    public Crate readCrate(ZipStreamStrategy strategy, Path source) throws IOException {
        Crate importedCrate = new CrateReader<>(strategy)
                .readCrate(new FileInputStream(source.toFile()));
        assertTrue(strategy.isExtracted());
        return importedCrate;
    }
}
