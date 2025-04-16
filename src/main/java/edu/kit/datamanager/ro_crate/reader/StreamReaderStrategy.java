package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.writer.StreamWriterStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public interface StreamReaderStrategy extends ReaderStrategy {

    org.slf4j.Logger logger = LoggerFactory.getLogger(StreamReaderStrategy.class);

    /**
     * Default override of readMetadataJson interface from ReaderStrategy. The
     * override assumes, that location is a file, which is used as input stream.
     * If this assumption is not true, this call will fail.
     *
     * @param location The source, which is supposed to be a file.
     *
     * @return the RO-Crate metadata as ObjectNode
     */
    @Override
    default ObjectNode readMetadataJson(String location) {
        ObjectNode result = null;
        try {
            result = readMetadataJson(new FileInputStream(new File(location)));
        } catch (FileNotFoundException ex) {
            logger.error("Failed read crate from source " + location, ex);
        }
        return result;
    }

    /**
     * Default override of readContent interface from ReaderStrategy. The
     * override assumes, that location is a file, which is used as input stream.
     * If this assumption is not true, this call will fail.
     *
     * @param location The source, which is supposed to be a file.
     *
     * @return the RO-Crate content as file, i.e., a folder
     */
    @Override
    default File readContent(String location) {
        File result = null;
        try {
            result = readContent(new FileInputStream(new File(location)));
        } catch (FileNotFoundException ex) {
            logger.error("Failed read crate from source " + location, ex);
        }
        return result;
    }

    ObjectNode readMetadataJson(InputStream source);

    File readContent(InputStream source);

}
