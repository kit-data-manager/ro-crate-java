package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import java.io.OutputStream;

/**
 * Strategy for writing of crates to streams.
 *
 * @author jejkal
 */
public interface StreamWriterStrategy {

    void save(Crate crate, OutputStream destination);
}
