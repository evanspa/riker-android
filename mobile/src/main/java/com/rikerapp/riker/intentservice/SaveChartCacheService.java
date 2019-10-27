package com.rikerapp.riker.intentservice;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.LineChartDataContainer;
import com.rikerapp.riker.model.RLineChartData;
import com.rikerapp.riker.model.User;

import org.parceler.Parcels;

import java.math.BigDecimal;

public final class SaveChartCacheService extends IntentService {

    private static final String INTENTDATA_LINE_CHART_DATA = "INTENTDATA_LINE_CHART_DATA";
    private static final String INTENTDATA_CHART_ID = "INTENTDATA_CHART_ID";
    private static final String INTENTDATA_CHART_CONFIG_LOCAL_IDENTIFIER = "INTENTDATA_CHART_CONFIG_LOCAL_IDENTIFIER";
    private static final String INTENTDATA_CATEGORY = "INTENTDATA_CATEGORY";
    private static final String INTENTDATA_AGGREGATE_BY = "INTENTDATA_AGGREGATE_BY";
    private static final String INTENTDATA_XAXIS_LABEL_COUNT = "INTENTDATA_XAXIS_LABEL_COUNT";
    private static final String INTENTDATA_MAX_Y_VALUE = "INTENTDATA_MAX_Y_VALUE";
    private static final String INTENTDATA_USER = "INTENTDATA_USER";

    public SaveChartCacheService() {
        super("SaveChartCacheService");
    }

    public static final Intent makeIntent(final Context context, final LineChartDataContainer lineChartDataContainer) {
        final Intent intent = new Intent(context, SaveChartCacheService.class);
        intent.putExtra(INTENTDATA_LINE_CHART_DATA, Parcels.wrap(lineChartDataContainer.lineChartData));
        intent.putExtra(INTENTDATA_CHART_ID, lineChartDataContainer.chart.id);
        intent.putExtra(INTENTDATA_CHART_CONFIG_LOCAL_IDENTIFIER, lineChartDataContainer.chartConfigLocalIdentifier);
        intent.putExtra(INTENTDATA_CATEGORY, lineChartDataContainer.category);
        intent.putExtra(INTENTDATA_AGGREGATE_BY, lineChartDataContainer.aggregateBy);
        intent.putExtra(INTENTDATA_XAXIS_LABEL_COUNT, lineChartDataContainer.xaxisLabelCount);
        intent.putExtra(INTENTDATA_MAX_Y_VALUE, lineChartDataContainer.maxyValue);
        intent.putExtra(INTENTDATA_USER, Parcels.wrap(lineChartDataContainer.user));
        return intent;
    }

    @Override
    protected final void onHandleIntent(final Intent intent) {
        if (intent != null) {
            final RikerApp rikerApp = (RikerApp)getApplication();
            final RLineChartData lineChartData = Parcels.unwrap(intent.getParcelableExtra(INTENTDATA_LINE_CHART_DATA));
            final String chartId = intent.getStringExtra(INTENTDATA_CHART_ID);
            final Integer chartConfigLocalIdentifier = (Integer)intent.getSerializableExtra(INTENTDATA_CHART_CONFIG_LOCAL_IDENTIFIER);
            final ChartConfig.Category category = (ChartConfig.Category)intent.getSerializableExtra(INTENTDATA_CATEGORY);
            final ChartConfig.AggregateBy aggregateBy = (ChartConfig.AggregateBy)intent.getSerializableExtra(INTENTDATA_AGGREGATE_BY);
            final int xaxisLabelCount = intent.getIntExtra(INTENTDATA_XAXIS_LABEL_COUNT, 0);
            final BigDecimal maxyValue = (BigDecimal)intent.getSerializableExtra(INTENTDATA_MAX_Y_VALUE);
            final User user = Parcels.unwrap(intent.getParcelableExtra(INTENTDATA_USER));
            rikerApp.dao.saveLineChartDataCache(lineChartData,
                    chartId,
                    chartConfigLocalIdentifier,
                    category,
                    aggregateBy,
                    xaxisLabelCount,
                    maxyValue,
                    user);
        }
    }
}
