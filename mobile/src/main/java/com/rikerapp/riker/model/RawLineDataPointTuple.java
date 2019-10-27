package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.math.BigDecimal;

@Parcel
public class RawLineDataPointTuple {

    public BigDecimal sum;
    public int count;
    public BigDecimal average;
    public BigDecimal percentage;

    @Override
    public final String toString() {
        return String.format("sum: %s, count: %d, average: %s, percentage: %s", sum, count, average, percentage);
    }
}
