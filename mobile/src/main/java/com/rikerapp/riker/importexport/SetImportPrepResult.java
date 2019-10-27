package com.rikerapp.riker.importexport;

import com.rikerapp.riker.model.MainSupport;

import java.util.List;

public final class SetImportPrepResult extends ImportPrepResult {
    public SetImportPrepResult(final List<? extends MainSupport> setsToImport,
                               final List<ImportError> errors,
                               final boolean anyReferenceErrors,
                               final String fileName) {
        super(setsToImport, errors, anyReferenceErrors, fileName);
    }

    public SetImportPrepResult(final Throwable throwable) {
        super(throwable);
    }
}
