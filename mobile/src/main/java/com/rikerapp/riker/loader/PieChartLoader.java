package com.rikerapp.riker.loader;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartColorsContainer;
import com.rikerapp.riker.model.ChartDataFetchMode;
import com.rikerapp.riker.model.ChartRawData;
import com.rikerapp.riker.model.ChartRawDataContainer;
import com.rikerapp.riker.model.PieChartDataContainer;
import com.rikerapp.riker.model.RikerDao;

import java.util.List;
import java.util.concurrent.ExecutorService;

public final class PieChartLoader extends AsyncLoader {

    private Chart chart;
    private ChartRawDataContainer defaultChartRawDataContainer;
    private RikerDao rikerDao;
    private Function.ChartRawDataMaker chartRawDataMaker;
    private ChartDataFetchMode fetchMode;
    private LruCache<String, List> entitiesCache;
    private LruCache<String, ChartRawData> chartRawDataCache;
    private ChartColorsContainer chartColorsContainer;
    private ExecutorService executorService;

    public PieChartLoader(final Context context,
                          final Chart chart,
                          final ChartRawDataContainer defaultChartRawDataContainer,
                          final RikerDao rikerDao,
                          final Function.ChartRawDataMaker chartStrengthRawDataMaker,
                          final ChartDataFetchMode fetchMode,
                          final LruCache<String, List> entitiesCache,
                          final LruCache<String, ChartRawData> chartRawDataCache,
                          final ChartColorsContainer chartColorsContainer,
                          final ExecutorService executorService) {
        super(context);
        this.chart = chart;
        this.defaultChartRawDataContainer = defaultChartRawDataContainer;
        this.rikerDao = rikerDao;
        this.chartRawDataMaker = chartStrengthRawDataMaker;
        this.fetchMode = fetchMode;
        this.entitiesCache = entitiesCache;
        this.chartRawDataCache = chartRawDataCache;
        this.chartColorsContainer = chartColorsContainer;
        this.executorService = executorService;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.chart = null;
        this.defaultChartRawDataContainer = null;
        this.rikerDao = null;
        this.chartRawDataMaker = null;
        this.fetchMode = null;
        this.entitiesCache = null;
        this.chartRawDataCache = null;
        this.chartColorsContainer = null;
        this.executorService = null;
    }

    @Override
    public final PieChartDataContainer loadInBackground() {
        return ChartUtils.pieChartDataContainer(getContext().getResources(),
                this.chart,
                this.defaultChartRawDataContainer,
                this.rikerDao,
                this.chartRawDataMaker,
                this.fetchMode,
                this.entitiesCache,
                this.chartRawDataCache,
                this.chartColorsContainer,
                false); // loaders are always invoked in the context of an activity (which is not headless)
    }
}
