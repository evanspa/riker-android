package com.rikerapp.riker.importexport;

import android.os.AsyncTask;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.eventbus.AppEvent;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.User;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class ExportBmlsTask extends AsyncTask<Void, Void, ExportTaskResult> {

    private final RikerApp rikerApp;

    public ExportBmlsTask(final RikerApp rikerApp) {
        this.rikerApp = rikerApp;
    }

    @Override
    protected final ExportTaskResult doInBackground(final Void... noArgs) {
        final User user = rikerApp.dao.user();
        final List<BodyMeasurementLog> bmls = rikerApp.dao.descendingBmls(user);
        final String fileName = String.format("riker-body-measurement-logs-%s.csv", new SimpleDateFormat(Constants.DATE_FORMAT_HYPHENS).format(new Date()));
        if (bmls.size() > 0) {
            Utils.exportBmls(rikerApp, fileName, bmls);
        }
        return new ExportTaskResult(fileName, bmls.size());
    }

    @Override
    protected final void onPostExecute(final ExportTaskResult exportResult) {
        EventBus.getDefault().post(new AppEvent.BmlsExportCompleteEvent(exportResult));
    }
}
