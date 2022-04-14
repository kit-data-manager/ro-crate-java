package edu.kit.crate.singlecratebenchmarks;

import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.DataEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

public class MixtureOfEntitiesPerformance {

  public static void main(String[] args) throws IOException {
    int times = Integer.parseInt(args[0]);
    String baseLocation = args[1];
    mixEntitiesTest(times, baseLocation);
  }

  public static void mixEntitiesTest(int numEntities, String baseLocation) throws IOException {

    Instant start = Instant.now();
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description").build();
    for (int i = 0; i < numEntities; i++) {
      PersonEntity person = new PersonEntity.PersonEntityBuilder()
          .setId("#id"+i)
          .addProperty("name", "Joe")
          .build();
      DataEntity file = new DataEntity.DataEntityBuilder()
          .setSource(new File(baseLocation + "file" + i))
          .addType("File")
          .addIdProperty("author", person)
          .build();
      crate.addDataEntity(file, true);
    }
    Instant end = Instant.now();
    String duration = String.valueOf(Duration.between(start, end).toMillis() / 1000.f);
    System.out.println("everything added in: " + duration + " seconds");
    FileUtils.writeStringToFile(new File("mix_java.txt"), duration + '\n', Charset.defaultCharset(), true);
  }
}
