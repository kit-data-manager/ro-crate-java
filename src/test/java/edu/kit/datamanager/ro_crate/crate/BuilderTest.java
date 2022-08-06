package edu.kit.datamanager.ro_crate.crate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;

public class BuilderTest {
  @Test
  void testReadBuilder() throws JsonProcessingException {
    ContextualEntity license = new ContextualEntity.ContextualEntityBuilder()
        .addType("CreativeWork")
        .setId("https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addProperty("description",
            "This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/au/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.")
        .addProperty("identifier", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addProperty("name", "Attribution-NonCommercial-ShareAlike 3.0 Australia (CC BY-NC-SA 3.0 AU)")
        .build();

    RoCrate crate = new RoCrate.RoCrateBuilder().addContextualEntity(license).build();

    RoCrate crate1 = new RoCrate.RoCrateBuilder(crate).build();

    HelpFunctions.compareTwoCrateJson(crate1, crate);

  }
}
