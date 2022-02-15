package edu.kit.crate.preview;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PreviewGenerator {
    private static final String command = "rochtml";

    public static void generatePreview(String location) {
        ProcessBuilder builder = new ProcessBuilder();
        // this is the equivalent of "rochtml dir/ro-crate-metadata.json"
        // TODO: add solution for windows, this works only with linux machines
        builder.command("sh", "-c", command + " " + location + "/ro-crate-metadata.json");
        Process process = null;
        try {
            process = builder.start();
            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                // success
            } else {
                //abnormal...
                throw new Exception("failed command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
