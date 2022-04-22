package edu.kit.crate.preview;

import java.io.File;
import net.lingala.zip4j.ZipFile;

/**
 * Interface for the ROCrate preview.
 * This manages the human-readable representation of a crate.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface CratePreview {

  void saveAllToZip(ZipFile zipFile);

  void saveAllToFolder(File folder);

}
