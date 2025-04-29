package edu.kit.datamanager.ro_crate.writer;

import java.io.IOException;
import java.nio.file.Path;

import edu.kit.datamanager.ro_crate.Crate;

import org.apache.commons.io.FileUtils;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
class FolderWriterTest extends CrateWriterTest {

    @Override
    protected void saveCrate(Crate crate, Path target) throws IOException {
        Writers.newFolderWriter()
                .save(crate, target.toAbsolutePath().toString());
    }

    @Override
    protected void ensureCrateIsExtractedIn(Path pathToCrate, Path expectedPath) throws IOException {
        FileUtils.copyDirectory(pathToCrate.toFile(), expectedPath.toFile());
    }
}
