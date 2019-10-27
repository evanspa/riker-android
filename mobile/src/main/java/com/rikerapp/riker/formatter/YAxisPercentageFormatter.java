package com.rikerapp.riker.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;

public final class YAxisPercentageFormatter extends ValueFormatter {

    private final NumberFormat numberFormat;

    public YAxisPercentageFormatter() {
        this.numberFormat = NumberFormat.getPercentInstance();
    }

    @Override
    public final String getAxisLabel(final float val, final AxisBase axisBase) {
        return this.numberFormat.format(val);
    }
}
