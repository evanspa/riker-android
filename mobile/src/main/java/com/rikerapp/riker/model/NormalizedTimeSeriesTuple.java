package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public final class NormalizedTimeSeriesTuple extends AbstractChartEntityDataTuple {
    public List<NormalizedLineChartDataEntry> normalizedTimeSeries;
}
