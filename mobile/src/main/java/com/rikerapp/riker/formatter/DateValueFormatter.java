package com.rikerapp.riker.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateValueFormatter extends ValueFormatter {

    private final SimpleDateFormat simpleDateFormat;

    public DateValueFormatter(final String pattern) {
        this.simpleDateFormat = new SimpleDateFormat(pattern);
    }

    @Override
    public final String getAxisLabel(final float val, final AxisBase axisBase) {
        return this.simpleDateFormat.format(new Date(Math.round((double)val)));
    }
}
