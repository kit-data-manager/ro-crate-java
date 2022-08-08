package edu.kit.datamanager.ro_crate.crate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;

public class TestRemoveAddContext {
  @Test
  void testAddRemoveValuePair() throws JsonProcessingException {
    RoCrate crate = new RoCrate.RoCrateBuilder().addValuePairToContext("key", "value").build();
    RoCrate defaultCrate = new RoCrate.RoCrateBuilder().build();

    crate.deleteValuePairFromContext("key");

    HelpFunctions.compareTwoCrateJson(crate, defaultCrate);

  }
}
