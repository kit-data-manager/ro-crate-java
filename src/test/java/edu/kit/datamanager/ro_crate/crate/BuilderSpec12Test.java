package edu.kit.datamanager.ro_crate.crate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.RoCrate;

class BuilderSpec12Test {
    @Test
    void testAppendConformsTo() throws URISyntaxException {
        Crate crate = new RoCrate.BuilderV1p2Draft()
            .alsoConformsTo(new URI("https://w3id.org/ro/wfrun/process/0.1"))
            .alsoConformsTo(new URI("https://example.com/myprofile/1.0"))
            .build();
        JsonNode conformsTo = crate.getJsonDescriptor().getProperty("conformsTo");
        assertTrue(conformsTo.isArray());
        // one version and two profiles
        assertEquals(1 + 2, conformsTo.size());
    }
}
