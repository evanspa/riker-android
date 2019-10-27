package com.rikerapp.riker.importexport;

public abstract class ImportResult {
    public final int numEntitiesImported;
    public final Throwable error;

    public ImportResult(final int numEntitiesImported, final Throwable error) {
        this.numEntitiesImported = numEntitiesImported;
        this.error = error;
    }
}
