package edu.kit.datamanager.ro_crate.writer;

public interface ElnFormatWriter<SOURCE_TYPE> extends GenericWriterStrategy<SOURCE_TYPE> {

    /**
     * Write in ELN format style, meaning with a root subfolder in the zip file.
     * Same as {@link #withRootSubdirectory()}.
     */
    ElnFormatWriter<SOURCE_TYPE> usingElnStyle();

    /**
     * Alias with more generic name for {@link #usingElnStyle()}.
     */
    default ElnFormatWriter<SOURCE_TYPE> withRootSubdirectory() {
        return this.usingElnStyle();
    }
}
