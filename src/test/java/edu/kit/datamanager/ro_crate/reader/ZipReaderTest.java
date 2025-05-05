package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.writer.Writers;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ZipReaderTest extends CrateReaderTest<String, ZipStrategy> {

    /**
     * ELN Crates are zip files not fully compatible with the Ro-Crate standard
     * in the sense that they must contain a single subfolder in the zip file
     * which then contain a crate as specified by the Ro-Crate standard.
     * <p>
     * Here we test if we can read them using out ZipReader.
     *
     * @see <a href="https://github.com/TheELNConsortium/TheELNFileFormat"></a>
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/AI4Green/Export%20workbook-2024-08-27-export.eln",
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/OpenSemanticLab/MinimalExample.osl.eln",
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/PASTA/PASTA.eln",
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/RSpace/RSpace-2023-12-08-14-44-xml-SELECTION-c0bEtpHcnNe-HA.eln",
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/SampleDB/sampledb_export.eln",
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/elabftw/export.eln",
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/kadi4mat/records-example.eln",
            "https://github.com/TheELNConsortium/TheELNFileFormat/raw/refs/heads/master/examples/kadi4mat/collections-example.eln"
    })
    void testReadElnCrates(String urlStr, @TempDir Path tmp) throws IOException {
        // Download the ELN file
        URL url = new URL(urlStr);
        Path elnFile = tmp.resolve("downloaded.eln");
        FileUtils.copyURLToFile(url, elnFile.toFile(), 10000, 10000);
        assertTrue(elnFile.toFile().exists());

        // Read the crate from the downloaded file
        Crate read = this.readCrate(elnFile);
        assertNotNull(read);
        assertFalse(read.getAllDataEntities().isEmpty());
    }

    @Override
    protected void saveCrate(Crate crate, Path target) {
        Writers.newZipPathWriter().save(crate, target.toAbsolutePath().toString());
        assertTrue(target.toFile().isFile());
    }

    @Override
    protected Crate readCrate(Path source) throws IOException {
        return Readers.newZipPathReader().readCrate(source.toAbsolutePath().toString());
    }

    @Override
    protected ZipStrategy newReaderStrategyWithTmp(Path tmpDirectory, boolean useUuidSubfolder) {
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
    protected Crate readCrate(ZipStrategy strategy, Path source) throws IOException {
        Crate importedCrate = new CrateReader<>(strategy)
                .readCrate(source.toAbsolutePath().toString());
        assertTrue(strategy.isExtracted());
        return importedCrate;
    }
}
