package com.rikerapp.riker.tasks;

import android.os.AsyncTask;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.eventbus.AppEvent;

import org.greenrobot.eventbus.EventBus;

public final class DeleteAllDataTask extends AsyncTask<Void, Void, Void> {

    private final RikerApp rikerApp;

    public DeleteAllDataTask(final RikerApp rikerApp) {
        this.rikerApp = rikerApp;
    }

    @Override
    protected final Void doInBackground(final Void... noArgs) {
        rikerApp.dao.deleteAllDataAndResetLocalUser();
        return null;
    }

    @Override
    protected final void onPostExecute(final Void noArg) {
        EventBus.getDefault().post(new AppEvent.DeleteAllDataCompleteEvent());
    }
}
