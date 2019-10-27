package com.rikerapp.riker.importexport;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;

import com.crashlytics.android.Crashlytics;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import timber.log.Timber;

public final class PrepareSetsImportTask extends AsyncTask<Void, Void, SetImportPrepResult> {

    public static final int NUM_FIELDS = 15;

    private final RikerApp rikerApp;
    private final Uri fileUri;

    public PrepareSetsImportTask(final RikerApp rikerApp, final Uri fileUri) {
        super();
        this.rikerApp = rikerApp;
        this.fileUri = fileUri;
    }

    @Override
    protected final SetImportPrepResult doInBackground(final Void... noArgs) {
        try {
            final Cursor cursor = this.rikerApp.getContentResolver().query(fileUri, null, null, null, null, null);
            cursor.moveToFirst();
            final String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
            final InputStream inputStream = this.rikerApp.getContentResolver().openInputStream(fileUri);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final Utils.ParseSetsCsvResult parseSetsCsvResult = Utils.parseSetsCsv(rikerApp.dao, reader);
            reader.close();
            return new SetImportPrepResult(parseSetsCsvResult.sets, parseSetsCsvResult.errors, parseSetsCsvResult.anyReferenceErrors, displayName);
        } catch (final Throwable throwable) {
            Timber.e(throwable);
            Crashlytics.logException(throwable);
            return new SetImportPrepResult(throwable);
        }
    }

    @Override
    protected final void onPostExecute(final SetImportPrepResult importPrepResult) {
        EventBus.getDefault().post(importPrepResult);
    }
}
