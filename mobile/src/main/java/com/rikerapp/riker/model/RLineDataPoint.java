package com.rikerapp.riker.model;

import org.parceler.Parcel;

@Parcel
public final class RLineDataPoint {

    public long date;
    public float value;

    public RLineDataPoint() {}

    public RLineDataPoint(final long date, final float value) {
        this.date = date;
        this.value = value;
    }
}
