package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class used for writing (exporting) crates. The class uses a strategy
 * pattern for writing crates as different formats. (zip, folders, etc.)
 */
public class RoCrateWriter {

    private static Logger logger = LoggerFactory.getLogger(RoCrateWriter.class);

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

    /**
     * This method saves the crate to a destination provided.
     *
     * @param crate the crate to write.
     * @param destination the location where the crate should be written.
     */
    public void save(Crate crate, OutputStream destination) {
        Validator defaultValidation = new Validator(new JsonSchemaValidation());
        defaultValidation.validate(crate);
        if (writer instanceof StreamWriterStrategy streamWriterStrategy) {
            streamWriterStrategy.save(crate, destination);
        } else {
            logger.error("Provided writer does not implement StreamWriterStrategy. Please use 'save(Crate crate, String destination)'.");
        }
    }
}
