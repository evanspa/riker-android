package com.rikerapp.riker.model;

import org.parceler.Parcel;

import java.util.Date;
import java.util.HashMap;

@Parcel
public final class ChartRawData {

    public enum Metric {
        WEIGHT,
        REPS,
        REST_TIME
    }

    public Date startDate;
    public Date endDate;

    // Pie chart data sources
    public HashMap<Integer, PieSliceDataTuple> byBodySegment;
    public HashMap<Integer, PieSliceDataTuple> byMuscleGroup;
    public HashMap<Integer, PieSliceDataTuple> byUpperBodySegment;
    public HashMap<Integer, PieSliceDataTuple> byLowerBodySegment;
    public HashMap<Integer, PieSliceDataTuple> byMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> byShoulderMg;
    public HashMap<Integer, PieSliceDataTuple> byBackMg;
    public HashMap<Integer, PieSliceDataTuple> byCoreMg;
    public HashMap<Integer, PieSliceDataTuple> byChestMg;
    public HashMap<Integer, PieSliceDataTuple> byTricepsMg;
    public HashMap<Integer, PieSliceDataTuple> upperBodyByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> lowerBodyByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> shoulderByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> backByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> tricepsByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> bicepsByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> forearmsByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> coreByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> chestByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> hamstringsByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> quadsByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> calfsByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> glutesByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> hipAbductorsByMovementVariant;
    public HashMap<Integer, PieSliceDataTuple> hipFlexorsByMovementVariant;

    // Line chart data sources
    public HashMap<Integer, RawLineDataPointsByDateTuple> timeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byBodySegmentTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byMuscleGroupTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byUpperBodySegmentTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byLowerBodySegmentTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byShoulderMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byBackMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byCoreMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byChestMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byTricepsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byBicepsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byForearmsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byHamstringsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byQuadsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byCalfsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byGlutesMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byHipAbductorsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byHipFlexorsMgTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> byMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> upperBodyByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> lowerBodyByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> shoulderByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> backByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> tricepsByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> bicepsByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> forearmsByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> coreByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> chestByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> hamstringsByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> quadsByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> calfsByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> glutesByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> hipAbductorsByMovementVariantTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> hipFlexorsByMovementVariantTimeSeries;

    // Body line chart data sources
    public HashMap<Integer, RawLineDataPointsByDateTuple> bodyWeightTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> armSizeTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> chestSizeTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> calfSizeTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> thighSizeTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> forearmSizeTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> waistSizeTimeSeries;
    public HashMap<Integer, RawLineDataPointsByDateTuple> neckSizeTimeSeries;

    public ChartRawData() {}

    public static ChartRawData makeBodyChartData() {
        final ChartRawData chartBodyData = new ChartRawData();
        chartBodyData.bodyWeightTimeSeries = new HashMap<>();
        chartBodyData.armSizeTimeSeries = new HashMap<>();
        chartBodyData.chestSizeTimeSeries = new HashMap<>();
        chartBodyData.calfSizeTimeSeries = new HashMap<>();
        chartBodyData.thighSizeTimeSeries = new HashMap<>();
        chartBodyData.forearmSizeTimeSeries = new HashMap<>();
        chartBodyData.waistSizeTimeSeries = new HashMap<>();
        chartBodyData.neckSizeTimeSeries = new HashMap<>();
        return chartBodyData;
    }

    public static ChartRawData makeCrossSectionChartData() {
        final ChartRawData chartStrengthData = new ChartRawData();
        chartStrengthData.byMuscleGroup = new HashMap<>();
        chartStrengthData.byMuscleGroupTimeSeries = new HashMap<>();
        return chartStrengthData;
    }

    public static ChartRawData makePieChartData() {
        final ChartRawData chartStrengthData = new ChartRawData();
        chartStrengthData.byBodySegment = new HashMap<>();
        chartStrengthData.byMuscleGroup = new HashMap<>();
        chartStrengthData.byUpperBodySegment = new HashMap<>();
        chartStrengthData.byLowerBodySegment = new HashMap<>();
        chartStrengthData.byMovementVariant = new HashMap<>();
        chartStrengthData.byShoulderMg = new HashMap<>();
        chartStrengthData.byBackMg = new HashMap<>();
        chartStrengthData.byCoreMg = new HashMap<>();
        chartStrengthData.byChestMg = new HashMap<>();
        chartStrengthData.byTricepsMg = new HashMap<>();
        chartStrengthData.upperBodyByMovementVariant = new HashMap<>();
        chartStrengthData.lowerBodyByMovementVariant = new HashMap<>();
        chartStrengthData.shoulderByMovementVariant = new HashMap<>();
        chartStrengthData.backByMovementVariant = new HashMap<>();
        chartStrengthData.tricepsByMovementVariant = new HashMap<>();
        chartStrengthData.bicepsByMovementVariant = new HashMap<>();
        chartStrengthData.forearmsByMovementVariant = new HashMap<>();
        chartStrengthData.coreByMovementVariant = new HashMap<>();
        chartStrengthData.chestByMovementVariant = new HashMap<>();
        chartStrengthData.hamstringsByMovementVariant = new HashMap<>();
        chartStrengthData.quadsByMovementVariant = new HashMap<>();
        chartStrengthData.calfsByMovementVariant = new HashMap<>();
        chartStrengthData.glutesByMovementVariant = new HashMap<>();
        chartStrengthData.hipAbductorsByMovementVariant = new HashMap<>();
        chartStrengthData.hipFlexorsByMovementVariant = new HashMap<>();
        return chartStrengthData;
    }

    public static ChartRawData makeLineChartData() {
        final ChartRawData chartStrengthData = new ChartRawData();
        chartStrengthData.timeSeries = new HashMap<>();
        chartStrengthData.byBodySegmentTimeSeries = new HashMap<>();
        chartStrengthData.byMuscleGroupTimeSeries = new HashMap<>();
        chartStrengthData.byUpperBodySegmentTimeSeries = new HashMap<>();
        chartStrengthData.byLowerBodySegmentTimeSeries = new HashMap<>();
        chartStrengthData.byMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.byShoulderMgTimeSeries = new HashMap<>();
        chartStrengthData.byBackMgTimeSeries = new HashMap<>();
        chartStrengthData.byCoreMgTimeSeries = new HashMap<>();
        chartStrengthData.byChestMgTimeSeries = new HashMap<>();
        chartStrengthData.byTricepsMgTimeSeries = new HashMap<>();
        chartStrengthData.byBicepsMgTimeSeries = new HashMap<>();
        chartStrengthData.byForearmsMgTimeSeries = new HashMap<>();
        chartStrengthData.byHamstringsMgTimeSeries = new HashMap<>();
        chartStrengthData.byQuadsMgTimeSeries = new HashMap<>();
        chartStrengthData.byCalfsMgTimeSeries = new HashMap<>();
        chartStrengthData.byGlutesMgTimeSeries = new HashMap<>();
        chartStrengthData.byHipAbductorsMgTimeSeries = new HashMap<>();
        chartStrengthData.byHipFlexorsMgTimeSeries = new HashMap<>();
        chartStrengthData.byGlutesMgTimeSeries = new HashMap<>();
        chartStrengthData.upperBodyByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.lowerBodyByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.shoulderByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.backByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.tricepsByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.bicepsByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.forearmsByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.coreByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.chestByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.hamstringsByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.quadsByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.calfsByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.glutesByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.hipAbductorsByMovementVariantTimeSeries = new HashMap<>();
        chartStrengthData.hipFlexorsByMovementVariantTimeSeries = new HashMap<>();
        return chartStrengthData;
    }

    @Override
    public final String toString() {
        return String.format("byMuscleGroupTimeSeries: %s", byMuscleGroupTimeSeries);
    }
}
