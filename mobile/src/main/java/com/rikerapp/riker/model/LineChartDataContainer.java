package com.rikerapp.riker.model;

import android.support.annotation.DrawableRes;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.math.BigDecimal;
import java.util.Date;

public final class LineChartDataContainer {

    public ChartConfig chartConfig;
    public RLineChartData lineChartData;
    public LineData uiLineChartData;
    public int xaxisLabelCount;
    public BigDecimal yaxisMaximum;
    public BigDecimal yaxisMinimum;
    public ValueFormatter yaxisValueFormatter;
    public ValueFormatter xaxisFormatter;
    public YAxisLabelText.LabelPair yAxisLabelText;
    @DrawableRes public int settingsButtonIconImageName;
    public boolean wasConfigSet;
    public UserSettings userSettings;
    public User user;
    public Chart chart;
    public Integer chartConfigLocalIdentifier;
    public ChartConfig.AggregateBy aggregateBy;
    public BigDecimal maxyValue;
    public ChartConfig.Category category;
    public boolean cacheHit;
    public Date firstEntityDate;
    public Date lastEntityDate;
}
