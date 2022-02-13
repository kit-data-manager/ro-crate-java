package edu.kit.crate.preview;

import java.io.File;
import net.lingala.zip4j.ZipFile;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface IROCratePreview {

  void saveALLToZip(ZipFile zipFile);
  void saveALLToFolder(File folder);

}
