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

public class DeletionEntitiesPerformance {

  public static void main(String[] args) throws IOException {
    int times = Integer.parseInt(args[0]);
    String baseLocation = args[1];
    deletionEntitiesTest(times, baseLocation);
  }

  public static void deletionEntitiesTest(int numEntities, String baseLocation) throws IOException {

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

    for (int i = 0; i < numEntities; i++) {
      crate.deleteEntityById("#id"+i);
    }
    for (int i = 0; i < numEntities; i++) {
      crate.deleteEntityById("file"+i);
    }

    Instant end = Instant.now();
    String duration = String.valueOf(Duration.between(start, end).toMillis() / 1000.f);
    System.out.println("everything deleted in: " + duration + " seconds");
    FileUtils.writeStringToFile(new File("deletion_java.txt"), duration + '\n', Charset.defaultCharset(), true);
  }
}
