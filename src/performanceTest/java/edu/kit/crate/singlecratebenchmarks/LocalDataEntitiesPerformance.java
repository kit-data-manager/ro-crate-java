package edu.kit.crate.singlecratebenchmarks;

import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.DataEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

public class LocalDataEntitiesPerformance {

  public static void main(String[] args) throws IOException {
    int times = Integer.parseInt(args[0]);
    String baseLocation = args[1];
    localDataEntitiesTest(times, baseLocation);
  }

  public static void localDataEntitiesTest(int numEntities, String baseLocation) throws IOException {

    Instant start = Instant.now();
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description").build();
    for (int i = 0; i < numEntities; i++) {
      DataEntity person = new DataEntity.DataEntityBuilder()
          .setSource(new File(baseLocation + "file" + i))
          .addType("File")
          .build();
      crate.addDataEntity(person, true);
    }
    Instant end = Instant.now();
    String duration = String.valueOf(Duration.between(start, end).toMillis() / 1000.f);
    System.out.println(numEntities + " number of Data local Entities added in: " + duration + " seconds");
    FileUtils.writeStringToFile(new File("data_local_java.txt"), duration + '\n', Charset.defaultCharset(), true);
  }
}
