package edu.kit.crate.writer;

import edu.kit.crate.IROCrate;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class ROCrateWriter {

  private final IWriterStrategy writer;

  public ROCrateWriter(IWriterStrategy writer) {
    this.writer = writer;
  }

  public void save(IROCrate crate, String destination) {
    this.writer.save(crate, destination);
  }
}
