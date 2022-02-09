package edu.kit.crate.writer;

import edu.kit.crate.IROCrate;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public interface IWriterStrategy {
  void save(IROCrate crate, String destination);
}
