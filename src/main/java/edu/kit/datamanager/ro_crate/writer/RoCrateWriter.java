package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;

/**
 * The class used for writing (exporting) crates.
 * The class uses a strategy pattern for writing crates as different formats.
 * (zip, folders, etc.)
 */
public class RoCrateWriter {

  private final WriterStrategy writer;

  public RoCrateWriter(WriterStrategy writer) {
    this.writer = writer;
  }

  /**
   * This method saves the crate to a destination provided.
   *
   * @param crate the crate to write.
   * @param destination the location where the crate should be written.
   */
  public void save(Crate crate, String destination) {
    Validator defaultValidation = new Validator(new JsonSchemaValidation());
    defaultValidation.validate(crate);
    this.writer.save(crate, destination);
  }
}
