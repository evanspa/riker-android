package com.rikerapp.riker.importexport;

import com.rikerapp.riker.model.MainSupport;

import java.util.List;

public abstract class ImportPrepResult {

    public final List<? extends MainSupport> entitiesToImport;
    public final List<ImportError> errors;
    public final boolean anyReferenceErrors;
    public final String fileName;
    public final Throwable throwable;

    public ImportPrepResult(final List<? extends MainSupport> entitiesToImport,
                            final List<ImportError> errors,
                            final boolean anyReferenceErrors,
                            final String fileName) {
        this.entitiesToImport = entitiesToImport;
        this.errors = errors;
        this.anyReferenceErrors = anyReferenceErrors;
        this.fileName = fileName;
        this.throwable = null;
    }

    public ImportPrepResult(final Throwable throwable) {
        this.entitiesToImport = null;
        this.errors = null;
        anyReferenceErrors = false;
        this.fileName = null;
        this.throwable = throwable;
    }
}
