package edu.kit.crate.multiplecrates;

import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.DataEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

public class MultipleCratesBenchmark {

  public static void main(String[] args) throws IOException {
    int crates = Integer.parseInt(args[0]);
    int entities = Integer.parseInt(args[1]);
    String baseLocation = args[2];
    multipleCratesCreation(crates, entities, baseLocation);
  }

  public static void multipleCratesCreation(int numCrates, int numEntitiesProCrate, String baseLocation) throws IOException {

    Instant start = Instant.now();
    for (int i = 0; i < numCrates; i++) {
      ROCrate crate = new ROCrate.ROCrateBuilder("name", "description").build();
      for (int j = 0; j < numEntitiesProCrate; j++) {
        PersonEntity person = new PersonEntity.PersonEntityBuilder()
            .setId("#id"+i+j)
            .addProperty("name", "Joe")
            .build();
        DataEntity file = new DataEntity.DataEntityBuilder()
            .setSource(new File(baseLocation + "file" + j))
            .addType("File")
            .addIdProperty("author", person)
            .build();
        crate.addContextualEntity(person);
        crate.addDataEntity(file, true);
      }
    }
    Instant end = Instant.now();
    String duration = String.valueOf(Duration.between(start, end).toMillis() / 1000.f);
    System.out.println("time taken: " + duration + " seconds");
    FileUtils.writeStringToFile(new File("mul_mix_java.txt"), duration + '\n', Charset.defaultCharset(), true);
  }
}
