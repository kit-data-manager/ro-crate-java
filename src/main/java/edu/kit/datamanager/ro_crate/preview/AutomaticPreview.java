package edu.kit.datamanager.ro_crate.preview;

import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;
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
    public void saveAllToZip(ZipFile zipFile) {
        try {
            // extract the .json file so we can run the "rochtml" tool on it"
            zipFile.extractFile("ro-crate-metadata.json", "temp");
            if (PreviewGenerator.isRochtmlAvailable()) {
                PreviewGenerator.generatePreview("temp");
            } else {
                DefaultPreviewGenerator.generatePreview(FileUtils.readFileToString(new File("temp/ro-crate-metadata.json"), "UTF-8"), zipFile);
            }
            zipFile.addFile("temp/ro-crate-preview.html");
            FileUtils.deleteDirectory(new File("temp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAllToFolder(File folder) {

        if (PreviewGenerator.isRochtmlAvailable()) {
            PreviewGenerator.generatePreview(folder.getAbsolutePath());
        } else {
            try {
                DefaultPreviewGenerator.generatePreview(FileUtils.readFileToString(new File(folder, "ro-crate-metadata.json"), "UTF-8"), folder);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
