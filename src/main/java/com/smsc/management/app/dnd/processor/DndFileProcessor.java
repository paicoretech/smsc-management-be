package com.smsc.management.app.dnd.processor;

import com.smsc.management.app.dnd.model.entity.DndEntryList;

import java.io.InputStream;

public interface DndFileProcessor {

    /**
     * Indicates whether this processor supports the given file type.
     * This can be determined by file extension or content type.
     *
     * @param fileType The type or name of the file (extension or MIME type).
     * @return true if the processor supports this file type.
     */
    boolean supports(String fileType);

    /**
     * Process the uploaded file to extract MSISDNs and save DndEntry entities.
     *
     * @param inputStream The uploaded file input stream.
     * @param dndEntryList     The associated DND Name entity.
     */
    void process(InputStream inputStream, DndEntryList dndEntryList);
}
