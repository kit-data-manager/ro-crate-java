package edu.kit.crate.singlecratebenchmarks;

import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.DataEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

public class RemoteDataEntitiesPerformance {

  public static void main(String[] args) throws IOException {
    int times = Integer.parseInt(args[0]);
    String baseId = args[1];
    remoteDataEntitiesTest(times, baseId);
  }

  public static void remoteDataEntitiesTest(int numEntities, String baseId) throws IOException {

    Instant start = Instant.now();
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description").build();
    for (int i = 0; i < numEntities; i++) {
      DataEntity person = new DataEntity.DataEntityBuilder()
          .setId(baseId + i)
          .addType("File")
          .build();
      crate.addDataEntity(person, true);
    }
    Instant end = Instant.now();
    String duration = String.valueOf(Duration.between(start, end).toMillis() / 1000.f);
    System.out.println(numEntities + " number of Data Remote Entities added in: " + duration + " seconds");
    FileUtils.writeStringToFile(new File("data_remote_java.txt"), duration + '\n', Charset.defaultCharset(), true);
  }

}
