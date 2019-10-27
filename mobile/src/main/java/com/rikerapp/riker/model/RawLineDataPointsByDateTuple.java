package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.util.Date;
import java.util.Map;

@Parcel
public class RawLineDataPointsByDateTuple extends AbstractChartEntityDataTuple {
    public Map<Date, RawLineDataPointTuple> dataPointsByDate;

    @Override
    public final String toString() {
        return String.format("dataPointsByDate: %s", dataPointsByDate);
    }
}