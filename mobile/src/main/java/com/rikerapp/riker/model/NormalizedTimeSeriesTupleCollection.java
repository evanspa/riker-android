package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Parcel
public final class NormalizedTimeSeriesTupleCollection {
    public Map<Integer, NormalizedTimeSeriesTuple> normalizedTimeSeriesTupleMap;
    public BigDecimal maxAggregateSummedValue;
    public BigDecimal maxAvgAggregateValue;
    public BigDecimal maxDistributionValue;

    public NormalizedTimeSeriesTupleCollection() {
        normalizedTimeSeriesTupleMap = new HashMap<>();
        maxAggregateSummedValue = BigDecimal.ZERO;
        maxAvgAggregateValue = BigDecimal.ZERO;
        maxDistributionValue = BigDecimal.ZERO;
    }

    public final void putNormalizedTimeSeriesTuple(final Integer forLocalMasterIdentifier,
                                                   final NormalizedTimeSeriesTuple normalizedTimeSeriesTuple) {
        normalizedTimeSeriesTupleMap.put(forLocalMasterIdentifier, normalizedTimeSeriesTuple);
    }
}
