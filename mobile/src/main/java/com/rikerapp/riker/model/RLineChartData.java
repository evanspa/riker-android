package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public final class RLineChartData {

    public List<RLineChartDataSeries> dataSeriesList;

    public RLineChartData() {}

    public RLineChartData(final List<RLineChartDataSeries> dataSeriesList) {
        this.dataSeriesList = dataSeriesList;
    }
}
