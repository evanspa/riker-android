package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

import static com.rikerapp.riker.Constants.DIVIDE_SCALE;

@Parcel
public final class NormalizedLineChartDataEntry {
    public Date date;
    public int count;
    public BigDecimal aggregateSummedValue;
    public BigDecimal avgAggregateValue;
    public BigDecimal distribution;
    public Integer groupIndex;

    public NormalizedLineChartDataEntry() {
        count = 0;
        aggregateSummedValue = BigDecimal.ZERO;
        avgAggregateValue = BigDecimal.ZERO;
        distribution = BigDecimal.ZERO;
    }

    public final void calculateAvgAggregateValue() {
        if (count > 0) {
            avgAggregateValue = aggregateSummedValue.divide(BigDecimal.valueOf(count), DIVIDE_SCALE, RoundingMode.HALF_UP);
        }
    }

    public final void calculateDistribution(final Map<Integer, BigDecimal> groupIndexTotals) {
        final BigDecimal groupIndexTotal = groupIndexTotals.get(groupIndex);
        if (groupIndexTotal.compareTo(BigDecimal.ZERO) != 0) {
            distribution = aggregateSummedValue.divide(groupIndexTotal, DIVIDE_SCALE, RoundingMode.HALF_UP);
        }
    }

    @Override
    public final String toString() {
        return String.format("date: %s, count: %d, aggregateSummedValue: %s, avgAggegateValue: %s, distribution: %s, groupIndex: %d",
                date, aggregateSummedValue, avgAggregateValue, distribution, groupIndex);
    }
}
