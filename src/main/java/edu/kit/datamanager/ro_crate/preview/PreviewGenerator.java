package edu.kit.datamanager.ro_crate.preview;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Class responsible for the generation of the human-readable representation of the metadata.
 * For this to work the nodejs library rochtml has to be installed.
 * This can be done using:
 * npm install -g ro-crate-html-js
 * or
 * npm install ro-crate-html-js
 */
public class PreviewGenerator {

  private static final String command = "rochtml";

  /**
   * The method that from the location of the crate generates the html file.
   *
   * @param location the location of the crate in the filesystem.
   */
  public static void generatePreview(String location) {
    ProcessBuilder builder = new ProcessBuilder();
    // this is the equivalent of "rochtml dir/ro-crate-metadata.json"
    // check if we are running on windows or unix
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      builder.command("cmd.exe", "/c", command + " " + location + "/ro-crate-metadata.json");

    } else {
      builder.command("sh", "-c", command + " " + location + "/ro-crate-metadata.json");
    }

    Process process;
    try {
      process = builder.start();
      StringBuilder output = new StringBuilder();

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
      reader.close();
      int exitVal = process.waitFor();
      if (exitVal != 0) {
        //abnormal...
        throw new Exception("failed command");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
