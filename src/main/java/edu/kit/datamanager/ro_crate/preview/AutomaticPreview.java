package edu.kit.datamanager.ro_crate.preview;

import edu.kit.datamanager.ro_crate.util.ZipUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * The default preview should use the rochtml tool
 * (https://www.npmjs.com/package/ro-crate-html-js) for creating a simple
 * preview file.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class AutomaticPreview implements CratePreview {

    public AutomaticPreview() {
    }

    @Override
    public void saveAllToZip(ZipFile zipFile) throws IOException {
        if (PreviewGenerator.isRochtmlAvailable()) {
            // extract the .json file so we can run the "rochtml" tool on it"
            try {
                try {
                    zipFile.extractFile("ro-crate-metadata.json", "temp");
                } catch (ZipException ex) {
                    throw new IOException("ro-crate-metadata.json not found in provided ZIP.", ex);
                }
                if (PreviewGenerator.isRochtmlAvailable()) {
                    PreviewGenerator.generatePreview("temp");
                    zipFile.addFile("temp/ro-crate-preview.html");
                }
            } finally {
                FileUtils.deleteDirectory(new File("temp"));
            }
        } else {
            new CustomPreview().saveAllToZip(zipFile);
        }
    }

    @Override
    public void saveAllToFolder(File folder) throws IOException {
        if (folder == null || !folder.exists()) {
            throw new IOException("Preview target folder " + folder + " does not exist.");
        }

        if (PreviewGenerator.isRochtmlAvailable()) {
            PreviewGenerator.generatePreview(folder.getAbsolutePath());
        } else {
            new CustomPreview().saveAllToFolder(folder);
        }
    }

    @Override
    public void saveAllToStream(String metadata, ZipOutputStream stream) throws IOException {
        if (PreviewGenerator.isRochtmlAvailable()) {
            try {
                FileUtils.forceMkdir(new File("temp"));
                FileWriter writer = new FileWriter(new File("temp/ro-crate-metadata.json"));
                writer.write(metadata);
                writer.flush();
                writer.close();
                if (PreviewGenerator.isRochtmlAvailable()) {
                    PreviewGenerator.generatePreview("temp");
                    ZipUtil.addFileToZipStream(stream, new File("temp/ro-crate-preview.html"), "ro-crate-preview.html");
                }
            } finally {
                FileUtils.deleteDirectory(new File("temp"));
            }
        } else {
            new CustomPreview().saveAllToStream(metadata, stream);
        }
    }

}
