package edu.kit.datamanager.ro_crate.writer;

import static org.junit.jupiter.api.Assertions.*;
class ProvenanceManagerTest {

    private final String oldVersionId = new ProvenanceManager(() -> "1.0.0").getLibraryId();
    private final String newVersionId = new ProvenanceManager(() -> "2.5.3").getLibraryId();
  
}