package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.util.Map;

@Parcel
public final class ChartColorsContainer {

    public Map<Integer, Integer> singleValueColor;
    public Map<Integer, Integer> bodySegmentColors;
    public Map<Integer, Integer> muscleGroupColors;
    public Map<Integer, Integer> lowerBodyMuscleGroupColors;
    public Map<Integer, Integer> movementVariantColors;
    public Map<Integer, Map<Integer, Integer>> muscleGroupMuscleColors;

    public Map<Integer, Integer> bodyWeightColors;
    public Map<Integer, Integer> armSizeColors;
    public Map<Integer, Integer> chestSizeColors;
    public Map<Integer, Integer> calfSizeColors;
    public Map<Integer, Integer> thighSizeColors;
    public Map<Integer, Integer> forearmSizeColors;
    public Map<Integer, Integer> waistSizeColors;
    public Map<Integer, Integer> neckSizeColors;
}
