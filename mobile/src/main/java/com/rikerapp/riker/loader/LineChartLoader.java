package com.rikerapp.riker.loader;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartColorsContainer;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartDataFetchMode;
import com.rikerapp.riker.model.ChartRawData;
import com.rikerapp.riker.model.ChartRawDataContainer;
import com.rikerapp.riker.model.LineChartDataContainer;
import com.rikerapp.riker.model.RikerDao;

import java.util.List;
import java.util.concurrent.ExecutorService;

import timber.log.Timber;

public final class LineChartLoader extends AsyncLoader {

    private Chart chart;
    private ChartRawDataContainer defaultChartRawDataContainer;
    private RikerDao rikerDao;
    private Function.ChartRawDataMaker chartRawDataMaker;
    private ChartDataFetchMode fetchMode;
    private LruCache<String, List> entitiesCache;
    private LruCache<String, ChartRawData> chartRawDataCache;
    private ChartConfig.AggregateBy defaultAggregateBy;
    private ChartColorsContainer chartColorsContainer;
    private ExecutorService executorService;
    private boolean calcPercentages;
    private boolean calcAverages;

    public LineChartLoader(final Context context,
                           final Chart chart,
                           final ChartRawDataContainer defaultChartRawDataContainer,
                           final RikerDao rikerDao,
                           final Function.ChartRawDataMaker chartStrengthRawDataMaker,
                           final boolean calcPercentages,
                           final boolean calcAverages,
                           final ChartDataFetchMode fetchMode,
                           final LruCache<String, List> entitiesCache,
                           final LruCache<String, ChartRawData> chartRawDataCache,
                           final ChartConfig.AggregateBy defaultAggregateBy,
                           final ChartColorsContainer chartColorsContainer,
                           final ExecutorService executorService) {
        super(context);
        this.chart = chart;
        this.defaultChartRawDataContainer = defaultChartRawDataContainer;
        this.rikerDao = rikerDao;
        this.chartRawDataMaker = chartStrengthRawDataMaker;
        this.calcPercentages = calcPercentages;
        this.calcAverages = calcAverages;
        this.fetchMode = fetchMode;
        this.entitiesCache = entitiesCache;
        this.chartRawDataCache = chartRawDataCache;
        this.defaultAggregateBy = defaultAggregateBy;
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
        this.defaultAggregateBy = null;
        this.chartColorsContainer = null;
        this.executorService = null;
    }

    @Override
    public final LineChartDataContainer loadInBackground() {
        LineChartDataContainer lineChartDataContainer = null;
        try {
            lineChartDataContainer =
                    ChartUtils.lineChartDataContainer(getContext().getResources(),
                            chart,
                            defaultChartRawDataContainer,
                            rikerDao,
                            chartRawDataMaker,
                            fetchMode,
                            entitiesCache,
                            chartRawDataCache,
                            defaultAggregateBy,
                            chartColorsContainer,
                            calcPercentages,
                            calcAverages,
                            false); // loaders are always invoked in the context of an activity (which is not headless)
            final LineChartDataContainer lineChartDataContainerFinal = lineChartDataContainer;
            executorService.execute(() -> {
                if (lineChartDataContainerFinal != null && !lineChartDataContainerFinal.cacheHit) {
                    rikerDao.saveLineChartDataCache(lineChartDataContainerFinal.lineChartData,
                            lineChartDataContainerFinal.chart.id,
                            lineChartDataContainerFinal.chartConfigLocalIdentifier,
                            lineChartDataContainerFinal.category,
                            lineChartDataContainerFinal.aggregateBy,
                            lineChartDataContainerFinal.xaxisLabelCount,
                            lineChartDataContainerFinal.maxyValue,
                            lineChartDataContainerFinal.user);
                }
            });
        } catch (Throwable t) {
            Timber.e(t);
        }
        return lineChartDataContainer;
    }
}
