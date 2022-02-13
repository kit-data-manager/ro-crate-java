package edu.kit.crate.preview;

import java.io.File;
import net.lingala.zip4j.ZipFile;

/**
 * The default preview should use the rochtml tool (https://www.npmjs.com/package/ro-crate-html-js)
 * for creating a simple preview file.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class DefaultPreview implements IROCratePreview {

  private static final String command = "rochtml";
  private String scriptLocation;

  public DefaultPreview() {
    this.scriptLocation = null;
  }

  /**
   * if the rochtml script is installed in a specific place
   *
   * @param scriptLocation the location of the script
   */
  public DefaultPreview(String scriptLocation) {
    this.scriptLocation = scriptLocation;
  }

  @Override
  public void saveALLToZip(ZipFile zipFile) {

  }

  @Override
  public void saveALLToFolder(File folder) {

  }
}
