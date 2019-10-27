package com.rikerapp.riker.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.math.RoundingMode;
import java.text.NumberFormat;

public final class YAxisNumberFormatter extends ValueFormatter {

    private final NumberFormat numberFormat;
    private final float scalingFactor;

    public YAxisNumberFormatter(final float scalingFactor) {
        this.numberFormat = NumberFormat.getNumberInstance();
        this.numberFormat.setMaximumFractionDigits(1);
        this.numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        this.scalingFactor = scalingFactor;
    }

    @Override
    public final String getAxisLabel(final float val, final AxisBase axisBase) {
        return this.numberFormat.format(val * this.scalingFactor);
    }
}
