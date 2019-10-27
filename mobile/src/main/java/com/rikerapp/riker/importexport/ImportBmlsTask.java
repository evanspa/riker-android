package com.rikerapp.riker.importexport;

import android.os.AsyncTask;

import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.MainSupport;
import com.rikerapp.riker.model.User;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ImportBmlsTask extends AsyncTask<Void, Void, BmlImportResult> {

    private List<? extends MainSupport> bmlsToImport;
    private final RikerDao rikerDao;

    public ImportBmlsTask(final List<? extends MainSupport> setsToImport,
                          final RikerDao rikerDao) {
        super();
        this.bmlsToImport = setsToImport;
        this.rikerDao = rikerDao;
    }

    @Override
    protected final BmlImportResult doInBackground(final Void... noArgs) {
        final User user = rikerDao.user();
        Throwable error = null;
        int numSavedBmls = 0;
        try {
            numSavedBmls = rikerDao.saveAllNewImportedBmls(user, bmlsToImport);
        } catch (Throwable any) {
            error = any;
        }
        return new BmlImportResult(numSavedBmls, error);
    }

    @Override
    protected final void onPostExecute(final BmlImportResult importResult) {
        EventBus.getDefault().post(importResult);
    }
}

