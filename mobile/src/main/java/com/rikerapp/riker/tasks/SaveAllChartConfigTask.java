package com.rikerapp.riker.tasks;

import android.os.AsyncTask;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.eventbus.AppEvent;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.User;

import org.greenrobot.eventbus.EventBus;

public final class SaveAllChartConfigTask extends AsyncTask<Void, Void, Void> {

    private final RikerApp rikerApp;
    private final Chart charts[];
    private final ChartConfig globalChartConfig;
    private final User user;

    public SaveAllChartConfigTask(final RikerApp rikerApp, final ChartConfig globalChartConfig, final Chart charts[], final User user) {
        this.rikerApp = rikerApp;
        this.globalChartConfig = globalChartConfig;
        this.charts = charts;
        this.user = user;
    }

    @Override
    protected final Void doInBackground(final Void... noArgs) {
        rikerApp.dao.deleteChartConfigs(this.globalChartConfig.category, user);
        for (final Chart chart : charts) {
            rikerApp.dao.saveNewOrExisting(ChartConfig.createFromTemplate(this.globalChartConfig, chart), user);
        }
        rikerApp.dao.saveNewOrExisting(this.globalChartConfig, user);
        return null;
    }

    @Override
    protected final void onPostExecute(final Void noArg) {
        EventBus.getDefault().post(new AppEvent.SaveAllChartConfigsCompleteEvent());
    }
}
