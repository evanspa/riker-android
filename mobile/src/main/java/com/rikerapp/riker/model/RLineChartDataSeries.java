package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public final class RLineChartDataSeries {

    public List<RLineDataPoint> dataPoints;
    public String label;
    public Integer localIdentifier;
    public Integer entityLocalIdentifier;

    public RLineChartDataSeries() {}

    public RLineChartDataSeries(final List<RLineDataPoint> dataPoints,
                                final String label,
                                final Integer localIdentifier,
                                final Integer entityLocalIdentifier) {
        this.dataPoints = dataPoints;
        this.label = label;
        this.localIdentifier = localIdentifier;
        this.entityLocalIdentifier = entityLocalIdentifier;
    }
}
