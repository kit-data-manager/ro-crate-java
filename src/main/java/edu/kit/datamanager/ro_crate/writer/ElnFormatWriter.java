package edu.kit.datamanager.ro_crate.writer;

import java.io.IOException;

public interface ElnFormatWriter<SOURCE_TYPE> extends GenericWriterStrategy<SOURCE_TYPE> {

    /**
     * Write in ELN format style, meaning with a root subfolder in the zip file.
     * Same as {@link #withRootSubdirectory()}.
     *
     * @throws IOException if an error occurs
     */
    ElnFormatWriter<SOURCE_TYPE> usingElnStyle();

    /**
     * Alias with more generic name for {@link #usingElnStyle()}.
     *
     * @throws IOException if an error occurs
     */
    default ElnFormatWriter<SOURCE_TYPE> withRootSubdirectory() {
        return this.usingElnStyle();
    }
}
