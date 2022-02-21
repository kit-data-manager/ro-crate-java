package edu.kit.crate.preview;

import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

/**
 * @author Nikola Tzotchev on 12.2.2022 г.
 * @version 1
 */
public class CustomPreview implements IROCratePreview {

  private File metadataHtml;
  private File otherFiles;

  public CustomPreview() {
  }

  public CustomPreview(File metadataHtml, File otherFiles) {
    if (!this.metadataHtml.getName().equals("ro-crate-preview.html")) {
      System.err.println("rename file");
      // TODO: throw an exception
    } else {
      this.metadataHtml = metadataHtml;
    }
    if (!this.otherFiles.getName().equals("ro-crate-preview")) {
      System.err.println("rename file");
      // TODO: throw an exception
    } else {
      this.otherFiles = otherFiles;
    }
  }

  @Override
  public void saveALLToZip(ZipFile zipFile) {
    if (this.metadataHtml != null) {
      try {
        zipFile.addFile(this.metadataHtml);
      } catch (ZipException e) {
        System.out.println("Exception writing preview html to zip");
      }
    }
    if (this.otherFiles != null) {
      try {
        zipFile.addFolder(this.otherFiles);
      } catch (ZipException e) {
        System.out.println("Exception writing preview files to zip");
      }
    }
  }

  @Override
  public void saveALLToFolder(File folder) {
    try {
      if (this.metadataHtml != null) {
        FileUtils.copyFileToDirectory(this.metadataHtml, folder);
      }
      if (this.otherFiles != null) {
        FileUtils.copyDirectoryToDirectory(this.otherFiles, folder);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
