package com.rikerapp.riker.importexport;

import java.io.Serializable;

public final class ExportTaskResult implements Serializable {

    public final String fileName;
    public final int numRecords;

    public ExportTaskResult(final String fileName, final int numRecords) {
        this.fileName = fileName;
        this.numRecords = numRecords;
    }
}
