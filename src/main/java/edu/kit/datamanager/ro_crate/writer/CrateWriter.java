package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;

import java.io.IOException;

/**
 * The class used for writing (exporting) crates. The class uses a strategy
 * pattern for writing crates as different formats. (zip, folders, etc.)
 *
 * @param <DESTINATION_TYPE> the type which determines the destination of the result
 */
public class CrateWriter<DESTINATION_TYPE> {

    private final GenericWriterStrategy<DESTINATION_TYPE> strategy;

    public CrateWriter(GenericWriterStrategy<DESTINATION_TYPE> strategy) {
        this.strategy = strategy;
    }

    /**
     * This method saves the crate to a destination provided.
     *
     * @param crate the crate to write.
     * @param destination the location where the crate should be written.
     */
    public void save(Crate crate, DESTINATION_TYPE destination) throws IOException {
        Validator defaultValidation = new Validator(new JsonSchemaValidation());
        defaultValidation.validate(crate);
        this.strategy.save(crate, destination);
    }
}
