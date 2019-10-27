package com.rikerapp.riker.importexport;

import android.os.AsyncTask;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.eventbus.AppEvent;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.User;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class ExportSetsTask extends AsyncTask<Void, Void, ExportTaskResult> {

    private final RikerApp rikerApp;

    public ExportSetsTask(final RikerApp rikerApp) {
        this.rikerApp = rikerApp;
    }

    @Override
    protected final ExportTaskResult doInBackground(final Void... noArgs) {
        final User user = rikerApp.dao.user();
        final List<Set> sets = rikerApp.dao.descendingSets(user);
        final Map<Integer, Movement> allMovements = Utils.toMap(rikerApp.dao.movementsWithNullMuscleIds());
        final Map<Integer, MovementVariant> allMovementVariants = Utils.toMap(rikerApp.dao.movementVariants());
        final String fileName = String.format("riker-sets-%s.csv", new SimpleDateFormat(Constants.DATE_FORMAT_HYPHENS).format(new Date()));
        if (sets.size() > 0) {
            Utils.exportSets(rikerApp,
                    fileName,
                    sets,
                    allMovements,
                    allMovementVariants);
        }
        return new ExportTaskResult(fileName, sets.size());
    }

    @Override
    protected final void onPostExecute(final ExportTaskResult exportResult) {
        EventBus.getDefault().post(new AppEvent.SetsExportCompleteEvent(exportResult));
    }
}
