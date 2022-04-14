package edu.kit.crate.singlecratebenchmarks;

import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.contextual.PersonEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

public class ContextualEntitiesPerformance {

  public static void main(String[] args) throws IOException {
    int times =  Integer.parseInt(args[0]);
    String baseId =  args[1];
    contextualEntitiesTest(times, baseId);
  }

  public static void contextualEntitiesTest(int numEntities, String baseId) throws IOException {

    Instant start = Instant.now();
    ROCrate crate = new ROCrate.ROCrateBuilder("name", "description").build();
    for (int i = 0; i < numEntities; i++) {
      PersonEntity person = new PersonEntity.PersonEntityBuilder()
          .setId(baseId + i)
          .addProperty("name", "Joe Bloggs")
          .build();
      crate.addContextualEntity(person);
    }
    Instant end = Instant.now();
    String duration = String.valueOf(Duration.between(start,end).toMillis()/1000.f);
    System.out.println(numEntities + " number of Contextual Entities added in: " + duration + " seconds");
    FileUtils.writeStringToFile(new File("contextual_java.txt"), duration+'\n', Charset.defaultCharset(), true);
  }
}
