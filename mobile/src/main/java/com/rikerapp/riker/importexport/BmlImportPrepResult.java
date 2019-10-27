package com.rikerapp.riker.importexport;

import com.rikerapp.riker.model.MainSupport;

import java.util.List;

public final class BmlImportPrepResult extends ImportPrepResult {
    public BmlImportPrepResult(final List<? extends MainSupport> bmlsToImport,
                               final List<ImportError> errors,
                               final boolean anyReferenceErrors,
                               final String fileName) {
        super(bmlsToImport, errors, anyReferenceErrors, fileName);
    }

    public BmlImportPrepResult(final Throwable throwable) {
        super(throwable);
    }
}
