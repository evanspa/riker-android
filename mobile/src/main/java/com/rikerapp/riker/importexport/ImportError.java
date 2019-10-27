package com.rikerapp.riker.importexport;

public final class ImportError {
    public final String message;
    public final Integer recordNumber;
    public final boolean missingRefData;

    public ImportError(final String message, final Integer recordNumber, final boolean missingRefData) {
        this.message = message;
        this.recordNumber = recordNumber;
        this.missingRefData = missingRefData;
    }
}
