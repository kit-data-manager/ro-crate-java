package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.slf4j.LoggerFactory;

/**
 * Strategy for writing of crates to streams.
 *
 * @author jejkal
 */
public interface StreamWriterStrategy extends WriterStrategy {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(StreamWriterStrategy.class);

    /**
     * Default override of save interface from WriterStrategy. The override
     * assumes, that destination is a file, which is used as output stream. If
     * this assumption is not true, this call will fail.
     *
     * @param crate The crate to write.
     * @param destination The destination, which is supposed to be a file.
     */
    default void save(Crate crate, String destination) {
        try {
            save(crate, new FileOutputStream(new File(destination)));
        } catch (FileNotFoundException ex) {
            logger.error("Failed save crate to destination " + destination, ex);
        }
    }

    void save(Crate crate, OutputStream destination);
}
