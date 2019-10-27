package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.math.BigDecimal;

@Parcel
public class PieSliceDataTuple extends AbstractChartEntityDataTuple {
    public BigDecimal aggregateValue;
}
