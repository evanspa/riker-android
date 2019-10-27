package com.rikerapp.riker.model.mpandroidchart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

public final class RLineDataSet extends LineDataSet {

    public final Integer entityLocalIdentifier;

    public RLineDataSet(final List<Entry> yVals, final String label, final Integer entityLocalIdentifier) {
        super(yVals, label);
        this.entityLocalIdentifier = entityLocalIdentifier;
    }
}
