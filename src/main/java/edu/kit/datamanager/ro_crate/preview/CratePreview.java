package edu.kit.datamanager.ro_crate.preview;

import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;

/**
 * Interface for the ROCrate preview. This manages the human-readable
 * representation of a crate.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @author jejkal
 * @version 2
 */
public interface CratePreview {

    void saveAllToZip(ZipFile zipFile) throws IOException;

    void saveAllToFolder(File folder) throws IOException;
    
    void saveAllToStream(String metadata, ZipOutputStream stream) throws IOException;

}
