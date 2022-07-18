package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;

/**
 * Strategy for writing of crates.
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public interface WriterStrategy {
  void save(Crate crate, String destination);
}
