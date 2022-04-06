package edu.kit.crate.preview;

import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;

/**
 * @author Nikola Tzotchev on 12.2.2022 г.
 * @version 1
 */
public class CustomPreview implements IROCratePreview {

  private final File metadataHtml;
  private final File otherFiles;

  public CustomPreview(File metadataHtml, File otherFiles) {
    this.metadataHtml = metadataHtml;
    this.otherFiles = otherFiles;
  }

  public CustomPreview(File metadataHtml) {
    this.metadataHtml = metadataHtml;
    this.otherFiles = null;
  }

  @Override
  public void saveALLToZip(ZipFile zipFile) {
    if (this.metadataHtml != null) {
      try {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip("ro-crate-preview.html");
        zipFile.addFile(this.metadataHtml, zipParameters);
      } catch (ZipException e) {
        System.err.println("Exception writing preview html to zip");
      }
    }
    if (this.otherFiles != null) {
      try {
        zipFile.addFolder(this.otherFiles);
        zipFile.renameFile(this.otherFiles.getName()+"/", "ro-crate-preview_files/");
      } catch (ZipException e) {
        System.err.println("Exception writing preview files to zip");
      }
    }
  }

  @Override
  public void saveALLToFolder(File folder) {
    try {
      if (this.metadataHtml != null) {
        File fileInCrate = folder.toPath().resolve("ro-crate-preview.html").toFile();
        FileUtils.copyFile(this.metadataHtml, fileInCrate);
      }
      if (this.otherFiles != null) {
        File folderName = folder.toPath().resolve("ro-crate-preview_files").toFile();
        FileUtils.copyDirectory(this.otherFiles, folderName);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
