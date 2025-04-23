package edu.kit.datamanager.ro_crate.writer;

import java.io.OutputStream;

/**
 * Utility class for creating instances of different crate writers.
 * This class is not meant to be instantiated.
 */
public class Writers {

    /**
     * Prevents instantiation of this utility class.
     */
    private Writers() {}

    public static CrateWriter<OutputStream> newZipStreamWriter() {
        return new CrateWriter<>(new ZipStreamStrategy());
    }
}
