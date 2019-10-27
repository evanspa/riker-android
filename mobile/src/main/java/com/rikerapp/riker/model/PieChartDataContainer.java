package com.rikerapp.riker.model;

import android.support.annotation.DrawableRes;

import com.github.mikephil.charting.data.PieData;

import java.util.Date;

public final class PieChartDataContainer {

    public ChartConfig chartConfig;
    public PieData pieData;
    @DrawableRes public int settingsButtonIconImageName;
    public boolean wasConfigSet;
    public UserSettings userSettings;
    public User user;
    public Chart chart;
    public Integer chartConfigLocalIdentifier;
    public ChartConfig.Category category;
    public Date firstEntityDate;
    public Date lastEntityDate;
}
