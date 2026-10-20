package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Legacy variant of {@code CreateMinimalCrate}, written against the released
 * 2.1.2 API. The example code is identical to the current variant; only
 * {@link CanonicalJson} uses the Jackson API instead of the neutral JSON
 * model.
 */
public class CreateMinimalCrate {

  public static void main(String[] args) throws Exception {
    RoCrate crate = new RoCrate.RoCrateBuilder(
      "Example crate",
      "An example crate built with ro-crate-java",
      "2024",
      "https://creativecommons.org/licenses/by/4.0/"
    )
      .addDataEntity(
        new DataEntity.DataEntityBuilder()
          .setId("survey-responses-2019.csv")
          .addType("File")
          .addProperty("name", "Survey responses")
          .addProperty("contentSize", "26452")
          .addProperty("encodingFormat", "text/csv")
          .build()
      )
      .addContextualEntity(
        new ContextualEntity.ContextualEntityBuilder()
          .setId("https://orcid.org/0000-0002-1825-0097")
          .addType("Person")
          .addProperty("name", "Josiah Carberry")
          .build()
      )
      .build();

    Files.writeString(
      Path.of(args[0]),
      CanonicalJson.canonicalize(crate.getJsonMetadata())
    );
  }
}
