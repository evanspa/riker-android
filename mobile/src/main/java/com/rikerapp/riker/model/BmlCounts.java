package com.rikerapp.riker.model;

public final class BmlCounts {

    public final int overallCount;
    public final int bodyWeightCount;
    public final int armSizeCount;
    public final int chestSizeCount;
    public final int calfSizeCount;
    public final int thighSizeCount;
    public final int forearmSizeCount;
    public final int waistSizeCount;
    public final int neckSizeCount;

    public BmlCounts(final int overallCount,
            final int bodyWeightCount,
            final int armSizeCount,
            final int chestSizeCount,
            final int calfSizeCount,
            final int thighSizeCount,
            final int forearmSizeCount,
            final int waistSizeCount,
            final int neckSizeCount) {
        this.overallCount = overallCount;
        this.bodyWeightCount = bodyWeightCount;
        this.armSizeCount = armSizeCount;
        this.chestSizeCount = chestSizeCount;
        this.calfSizeCount = calfSizeCount;
        this.thighSizeCount = thighSizeCount;
        this.forearmSizeCount = forearmSizeCount;
        this.waistSizeCount = waistSizeCount;
        this.neckSizeCount = neckSizeCount;
    }
}
