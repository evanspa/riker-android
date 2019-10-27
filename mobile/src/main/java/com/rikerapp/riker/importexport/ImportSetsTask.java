package com.rikerapp.riker.importexport;

import android.os.AsyncTask;

import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.MainSupport;
import com.rikerapp.riker.model.User;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public final class ImportSetsTask extends AsyncTask<Void, Void, SetImportResult> {

    private List<? extends MainSupport> setsToImport;
    private final RikerDao rikerDao;

    public ImportSetsTask(final List<? extends MainSupport> setsToImport,
                          final RikerDao rikerDao) {
        super();
        this.setsToImport = setsToImport;
        this.rikerDao = rikerDao;
    }

    @Override
    protected final SetImportResult doInBackground(final Void... noArgs) {
        Throwable error = null;
        int numSavedSets = 0;
        try {
            final User user = rikerDao.user();
            numSavedSets = rikerDao.saveAllNewImportedSets(user, setsToImport);
        } catch (Throwable any) {
            error = any;
        }
        return new SetImportResult(numSavedSets, error);
    }

    @Override
    protected final void onPostExecute(final SetImportResult importResult) {
        EventBus.getDefault().post(importResult);
    }
}
