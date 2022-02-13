import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.io.FileUtils;

/**
 * @author Nikola Tzotchev on 31.1.2022 г.
 * @version 1
 */
public class Main {

  public static void main(String[] args) throws ScriptException {
//    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
//    engine.eval("print('Hello World!');");
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("cmd.exe", "/c", "rochtml", "test/ro-crate-metadata.json");
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
        System.out.println("Success!");
        System.out.println(output);
        System.exit(0);
      } else {
        //abnormal...
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

  }
}
