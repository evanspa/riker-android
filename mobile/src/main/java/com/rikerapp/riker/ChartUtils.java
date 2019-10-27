package com.rikerapp.riker;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.rikerapp.riker.formatter.DateValueFormatter;
import com.rikerapp.riker.model.AbstractChartEntityDataTuple;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartColorsContainer;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartDataFetchMode;
import com.rikerapp.riker.model.ChartRawData;
import com.rikerapp.riker.model.ChartRawDataContainer;
import com.rikerapp.riker.model.LineChartDataContainer;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.Muscle;
import com.rikerapp.riker.model.MuscleGroup;
import com.rikerapp.riker.model.NormalizedLineChartDataEntry;
import com.rikerapp.riker.model.NormalizedTimeSeriesTuple;
import com.rikerapp.riker.model.NormalizedTimeSeriesTupleCollection;
import com.rikerapp.riker.model.PieChartDataContainer;
import com.rikerapp.riker.model.PieSliceDataTuple;
import com.rikerapp.riker.model.RLineChartData;
import com.rikerapp.riker.model.RLineChartDataSeries;
import com.rikerapp.riker.model.RLineDataPoint;
import com.rikerapp.riker.model.RawLineDataPointTuple;
import com.rikerapp.riker.model.RawLineDataPointsByDateTuple;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.WeightUnit;
import com.rikerapp.riker.model.mpandroidchart.RLineDataSet;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.rikerapp.riker.Constants.DIVIDE_SCALE;
import static java.lang.Math.ceil;

public final class ChartUtils {

    public static final int NO_RES_ID = -1;
    public static final int LMID_KEY_FOR_SINGLE_VALUE_CONTAINER = 0;

    public static final BigDecimal PRIMARY_MUSCLE_PERCENTAGE = new BigDecimal("0.80");

    public interface TallyWeight { void invoke(final Integer localMasterIdentifier, final BigDecimal weightToAdd); }
    public interface TallyReps   { void invoke(final Integer localMasterIdentifier, final BigDecimal repsToAdd); }
    public interface TallyRestTime { void invoke(final Integer localMasterIdentifier, final BigDecimal restTimeToAdd); }

    private static String abbreviate(final MuscleGroup muscleGroup) {
        final String abbreviatedName = muscleGroup.abbrevName;
        if (abbreviatedName != null && abbreviatedName.trim().length() > 0) {
            return abbreviatedName;
        }
        return muscleGroup.name;
    }

    private static String abbreviate(final Muscle muscle) {
        final String abbreviatedName = muscle.abbrevCanonicalName;
        if (abbreviatedName != null && abbreviatedName.trim().length() > 0) {
            return abbreviatedName;
        }
        return muscle.canonicalName;
    }

    private static String abbreviate(final MovementVariant movementVariant) {
        final String abbreviatedName = movementVariant.abbrevName;
        if (abbreviatedName != null && abbreviatedName.trim().length() > 0) {
            return abbreviatedName;
        }
        return movementVariant.name;
    }

    private static void initBodySegmentPieData(final Map<Integer, PieSliceDataTuple> dataMap, final BodySegment bodySegment) {
        final PieSliceDataTuple pieDataTuple = new PieSliceDataTuple();
        pieDataTuple.aggregateValue = BigDecimal.ZERO;
        pieDataTuple.name = bodySegment.name;
        pieDataTuple.localIdentifier = bodySegment.localIdentifier;
        dataMap.put(pieDataTuple.localIdentifier, pieDataTuple);
    }

    private static void initMuscleGroupPieData(final Map<Integer, PieSliceDataTuple> dataMap, final MuscleGroup muscleGroup) {
        final PieSliceDataTuple pieDataTuple = new PieSliceDataTuple();
        pieDataTuple.aggregateValue = BigDecimal.ZERO;
        pieDataTuple.name = abbreviate(muscleGroup);
        pieDataTuple.localIdentifier = muscleGroup.localIdentifier;
        dataMap.put(pieDataTuple.localIdentifier, pieDataTuple);
    }

    private static void initMusclePieData(final Map<Integer, PieSliceDataTuple> dataMap, final Muscle muscle) {
        final PieSliceDataTuple pieDataTuple = new PieSliceDataTuple();
        pieDataTuple.aggregateValue = BigDecimal.ZERO;
        pieDataTuple.name = abbreviate(muscle);
        pieDataTuple.localIdentifier = muscle.localIdentifier;
        dataMap.put(pieDataTuple.localIdentifier, pieDataTuple);
    }

    private static void initMovementVariantPieData(final Map<Integer, PieSliceDataTuple> dataMap, final MovementVariant movementVariant) {
        final PieSliceDataTuple pieDataTuple = new PieSliceDataTuple();
        pieDataTuple.aggregateValue = BigDecimal.ZERO;
        pieDataTuple.name = abbreviate(movementVariant);
        pieDataTuple.localIdentifier = movementVariant.localIdentifier;
        dataMap.put(pieDataTuple.localIdentifier, pieDataTuple);
    }

    private static void initSingleEntityTimeSeriesData(final Map<Integer, RawLineDataPointsByDateTuple> dataMap, final String name) {
        final RawLineDataPointsByDateTuple timeSeriesDataTuple = new RawLineDataPointsByDateTuple();
        timeSeriesDataTuple.name = name;
        timeSeriesDataTuple.localIdentifier = LMID_KEY_FOR_SINGLE_VALUE_CONTAINER;
        timeSeriesDataTuple.dataPointsByDate = new HashMap();
        dataMap.put(timeSeriesDataTuple.localIdentifier, timeSeriesDataTuple);
    }

    private static void initBodySegmentTimeSeriesData(final Map<Integer, RawLineDataPointsByDateTuple> dataMap, final BodySegment bodySegment) {
        final RawLineDataPointsByDateTuple timeSeriesDataTuple = new RawLineDataPointsByDateTuple();
        timeSeriesDataTuple.name = bodySegment.name;
        timeSeriesDataTuple.localIdentifier = bodySegment.localIdentifier;
        timeSeriesDataTuple.dataPointsByDate = new HashMap();
        dataMap.put(timeSeriesDataTuple.localIdentifier, timeSeriesDataTuple);
    }

    private static void initMuscleGroupTimeSeriesData(final Map<Integer, RawLineDataPointsByDateTuple> dataMap, final MuscleGroup muscleGroup) {
        final RawLineDataPointsByDateTuple timeSeriesDataTuple = new RawLineDataPointsByDateTuple();
        timeSeriesDataTuple.name = abbreviate(muscleGroup);
        timeSeriesDataTuple.localIdentifier = muscleGroup.localIdentifier;
        timeSeriesDataTuple.dataPointsByDate = new HashMap();
        dataMap.put(timeSeriesDataTuple.localIdentifier, timeSeriesDataTuple);
    }

    private static void initMuscleTimeSeriesData(final Map<Integer, RawLineDataPointsByDateTuple> dataMap, final Muscle muscle) {
        final RawLineDataPointsByDateTuple timeSeriesDataTuple = new RawLineDataPointsByDateTuple();
        timeSeriesDataTuple.name = abbreviate(muscle);
        timeSeriesDataTuple.localIdentifier = muscle.localIdentifier;
        timeSeriesDataTuple.dataPointsByDate = new HashMap();
        dataMap.put(timeSeriesDataTuple.localIdentifier, timeSeriesDataTuple);
    }

    private static void initMovementVariantTimeSeriesData(final Map<Integer, RawLineDataPointsByDateTuple> dataMap, final MovementVariant movementVariant) {
        final RawLineDataPointsByDateTuple timeSeriesDataTuple = new RawLineDataPointsByDateTuple();
        timeSeriesDataTuple.name = abbreviate(movementVariant);
        timeSeriesDataTuple.localIdentifier = movementVariant.localIdentifier;
        timeSeriesDataTuple.dataPointsByDate = new HashMap();
        dataMap.put(timeSeriesDataTuple.localIdentifier, timeSeriesDataTuple);
    }

    private static void addTo(final Map<Integer, ? extends AbstractChartEntityDataTuple> dataTuples, final Integer localMasterIdentifier, final BigDecimal valueToAdd, final Date loggedAt) {
        if (valueToAdd != null) {
            final AbstractChartEntityDataTuple chartDataTuple = dataTuples.get(localMasterIdentifier);
            if (chartDataTuple != null) {
                if (loggedAt != null) { // if loggedAt is provided, then chartDataTuple is assumed to be for a line chart
                    final Map<Date, RawLineDataPointTuple> timeSeries = ((RawLineDataPointsByDateTuple)chartDataTuple).dataPointsByDate;
                    RawLineDataPointTuple tuple = timeSeries.get(loggedAt);
                    if (tuple != null) {
                        tuple.sum = tuple.sum.add(valueToAdd);
                        tuple.count++;
                    } else {
                        tuple = new RawLineDataPointTuple();
                        tuple.sum = valueToAdd;
                        tuple.percentage = BigDecimal.ZERO;
                        tuple.count = 1;
                        timeSeries.put(loggedAt, tuple);
                    }
                } else { // is a pie chart data tuple
                    final PieSliceDataTuple tuple = (PieSliceDataTuple)chartDataTuple;
                    tuple.aggregateValue = tuple.aggregateValue.add(valueToAdd);
                }
            }
        }
    }

    private static void holePluggerAndPercentageCalculator(final Map<Integer, RawLineDataPointsByDateTuple> dataTuples,
                                                           final Date loggedAt,
                                                           final boolean calcPercentages,
                                                           final boolean calcAverages) {
        // collect all the time series tuples into a list
        java.util.Set<Integer> localMasterIdentifiers = dataTuples.keySet();
        final List<RawLineDataPointsByDateTuple> dataTuplesList = new ArrayList<>();
        for (final Integer localMasterIdentifier : localMasterIdentifiers) {
            final RawLineDataPointsByDateTuple dataTuple = dataTuples.get(localMasterIdentifier);
            dataTuplesList.add(dataTuple);
        }
        // fill empty holes with "zeros" and calculate total val
        BigDecimal totalVal = BigDecimal.ZERO;
        for (final RawLineDataPointsByDateTuple dataTuple : dataTuplesList) {
            RawLineDataPointTuple timeSeriesDataPointTuple = dataTuple.dataPointsByDate.get(loggedAt);
            if (timeSeriesDataPointTuple != null) {
                totalVal = totalVal.add(timeSeriesDataPointTuple.sum);
            } else {
                timeSeriesDataPointTuple = new RawLineDataPointTuple();
                timeSeriesDataPointTuple.sum = BigDecimal.ZERO;
                timeSeriesDataPointTuple.percentage = BigDecimal.ZERO;
                timeSeriesDataPointTuple.count = 0;
                timeSeriesDataPointTuple.average = BigDecimal.ZERO;
                dataTuple.dataPointsByDate.put(loggedAt, timeSeriesDataPointTuple);
            }
        }
        // 2nd pass to fill percentages and average values
        if (calcPercentages || calcAverages) {
            if (totalVal.compareTo(BigDecimal.ZERO) == 1) {
                for (final RawLineDataPointsByDateTuple dataTuple : dataTuplesList) {
                    final RawLineDataPointTuple timeSeriesDataPointTuple = dataTuple.dataPointsByDate.get(loggedAt);
                    if (calcPercentages) {
                        timeSeriesDataPointTuple.percentage= timeSeriesDataPointTuple.sum.divide(totalVal, DIVIDE_SCALE, RoundingMode.HALF_UP);
                    }
                    if (calcAverages) {
                        if (timeSeriesDataPointTuple.count > 0) {
                            timeSeriesDataPointTuple.average= timeSeriesDataPointTuple.sum.divide(BigDecimal.valueOf(timeSeriesDataPointTuple.count), DIVIDE_SCALE, RoundingMode.HALF_UP);
                        }
                    }
                }
            }
        }
    }

    public static final ChartConfig.AggregateBy suggestedAggregateBy(final ChartRawDataContainer defaultChartRawDataContainer) {
        if (defaultChartRawDataContainer.entities != null && defaultChartRawDataContainer.entities.size() > 0) {
            return ChartUtils.suggestedAggregateBy(defaultChartRawDataContainer.entities.get(0).loggedAt,
                    defaultChartRawDataContainer.entities.get(defaultChartRawDataContainer.entities.size() - 1).loggedAt);
        }
        return ChartConfig.AggregateBy.DAY;
    }

    public static ChartRawData weightLiftedChartDataCrossSection(final UserSettings userSettings,
                                                                 final List<MuscleGroup> muscleGroupList,
                                                                 final Map<Integer, MuscleGroup> muscleGroupMap,
                                                                 final Map<Integer, Muscle> muscleMap,
                                                                 final Map<Integer, Movement> movementMap,
                                                                 final List<Set> setList) {
        final ChartRawData cd = ChartRawData.makeCrossSectionChartData();
        final int numSets = setList.size();
        if (numSets > 0) {
            final Set firstSet = setList.get(0);
            final Set lastSet = setList.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
            for (final MuscleGroup muscleGroup : muscleGroupList) {
                initMuscleGroupPieData(cd.byMuscleGroup, muscleGroup);
                initMuscleGroupTimeSeriesData(cd.byMuscleGroupTimeSeries, muscleGroup);
            }
            for (int i = 0; i < numSets; i++) {
                final Set set = setList.get(i);
                final Date loggedAt = set.loggedAt;
                final BigDecimal weight = Utils.weightValue(set.weight, WeightUnit.weightUnitById(set.weightUom), WeightUnit.weightUnitById(userSettings.weightUom));
                final int numReps = set.numReps;
                final BigDecimal totalWeight = weight.multiply(BigDecimal.valueOf(numReps));
                final Movement movement = movementMap.get(set.movementId);
                final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
                final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
                final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
                final BigDecimal primaryMusclesTotalWeight = secondaryMuscleIdsCount > 0 ? totalWeight.multiply(PRIMARY_MUSCLE_PERCENTAGE) : totalWeight;
                final BigDecimal secondaryMusclesTotalWeight = totalWeight.subtract(primaryMusclesTotalWeight);
                final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
                final BigDecimal perPrimaryMuscleWeight = primaryMusclesTotalWeight.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
                final BigDecimal perSecondaryMuscleWeight = secondaryMuscleIdsCount > 0 ? secondaryMusclesTotalWeight.divide(BigDecimal.valueOf(secondaryMuscleIdsCount), DIVIDE_SCALE, RoundingMode.HALF_UP) : null;
                final TallyWeight tallyWeight = (localMasterIdentifier, weightToAdd) -> {
                    final Muscle muscle = muscleMap.get(localMasterIdentifier);
                    final MuscleGroup muscleGroup = muscleGroupMap.get(muscle.muscleGroupId);
                    final Integer muscleGroupLocalMasterIdentifier = muscleGroup.localIdentifier;
                    addTo(cd.byMuscleGroup, muscleGroupLocalMasterIdentifier, weightToAdd, null);
                    addTo(cd.byMuscleGroupTimeSeries, muscleGroupLocalMasterIdentifier, weightToAdd, loggedAt);
                };
                for (final Integer primaryMuscleId : primaryMuscleIds) {
                    tallyWeight.invoke(primaryMuscleId, perPrimaryMuscleWeight);
                }
                if (perSecondaryMuscleWeight != null) {
                    for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                        tallyWeight.invoke(secondaryMuscleId, perSecondaryMuscleWeight);
                    }
                }
            }
            for (final Set set : setList) {
                holePluggerAndPercentageCalculator(cd.byMuscleGroupTimeSeries, set.loggedAt, true, true);
            }
        }
        return cd;
    }

    public static ChartRawData weightLiftedLineChartStrengthRawDataForUser(final UserSettings userSettings,
                                                                           final List<BodySegment> bodySegments,
                                                                           final Map<Integer, BodySegment> bodySegmentsDict,
                                                                           final List<MuscleGroup> muscleGroups,
                                                                           final Map<Integer, MuscleGroup> muscleGroupsDict,
                                                                           final List<Muscle> muscles,
                                                                           final Map<Integer, Muscle> musclesDict,
                                                                           final Map<Integer, Movement> movementsDict,
                                                                           final List<MovementVariant> movementVariants,
                                                                           final Map<Integer, MovementVariant> movementVariantsDict,
                                                                           final List<Set> sets,
                                                                           final boolean calcPercentages,
                                                                           final boolean calcAverages) {
        final ChartRawData cd = ChartRawData.makeLineChartData();
        final int numSets = sets.size();
        if (numSets > 0) {
            final Set firstSet = sets.get(0);
            final Set lastSet = sets.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
        }
        initSingleEntityTimeSeriesData(cd.timeSeries, "Total Weight");
        for (final BodySegment bodySegment : bodySegments) {
            initBodySegmentTimeSeriesData(cd.byBodySegmentTimeSeries, bodySegment);
        }
        for (final MuscleGroup muscleGroup : muscleGroups) {
            initMuscleGroupTimeSeriesData(cd.byMuscleGroupTimeSeries, muscleGroup);
            final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
            switch (bodySegmentId) {
                case UPPER_BODY:
                    initMuscleGroupTimeSeriesData(cd.byUpperBodySegmentTimeSeries, muscleGroup);
                    break;
                case LOWER_BODY:
                    initMuscleGroupTimeSeriesData(cd.byLowerBodySegmentTimeSeries, muscleGroup);
                    break;
            }
        }
        for (final Muscle muscle : muscles) {
            final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscle.muscleGroupId);
            switch (muscleGroupId) {
                case CORE:
                    initMuscleTimeSeriesData(cd.byCoreMgTimeSeries, muscle);
                    break;
                case BACK:
                    initMuscleTimeSeriesData(cd.byBackMgTimeSeries, muscle);
                    break;
                case CALF:
                    initMuscleTimeSeriesData(cd.byCalfsMgTimeSeries, muscle);
                    break;
                case HAMS:
                    initMuscleTimeSeriesData(cd.byHamstringsMgTimeSeries, muscle);
                    break;
                case CHEST:
                    initMuscleTimeSeriesData(cd.byChestMgTimeSeries, muscle);
                    break;
                case QUADS:
                    initMuscleTimeSeriesData(cd.byQuadsMgTimeSeries, muscle);
                    break;
                case BICEPS:
                    initMuscleTimeSeriesData(cd.byBicepsMgTimeSeries, muscle);
                    break;
                case GLUTES:
                    initMuscleTimeSeriesData(cd.byGlutesMgTimeSeries, muscle);
                    break;
                case TRICEPS:
                    initMuscleTimeSeriesData(cd.byTricepsMgTimeSeries, muscle);
                    break;
                case FOREARMS:
                    initMuscleTimeSeriesData(cd.byForearmsMgTimeSeries, muscle);
                    break;
                case SHOULDERS:
                    initMuscleTimeSeriesData(cd.byShoulderMgTimeSeries, muscle);
                    break;
                case HIP_ABDUCTORS:
                    initMuscleTimeSeriesData(cd.byHipAbductorsMgTimeSeries, muscle);
                    break;
                case HIP_FLEXORS:
                    initMuscleTimeSeriesData(cd.byHipFlexorsMgTimeSeries, muscle);
                    break;
            }
        }
        for (final MovementVariant movementVariant : movementVariants) {
            initMovementVariantTimeSeriesData(cd.byMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.upperBodyByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.lowerBodyByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.shoulderByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.tricepsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.bicepsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.forearmsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.chestByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.backByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.coreByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hamstringsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.quadsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.calfsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.glutesByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hipAbductorsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hipFlexorsByMovementVariantTimeSeries, movementVariant);
        }
        for (int i = 0; i < numSets; i++) {
            final Set set = sets.get(i);
            final Date loggedAt = set.loggedAt;
            final BigDecimal weight = Utils.weightValue(set.weight, WeightUnit.weightUnitById(set.weightUom), WeightUnit.weightUnitById(userSettings.weightUom));
            final int numReps = set.numReps;
            final BigDecimal totalWeight = weight.multiply(BigDecimal.valueOf(numReps));
            final Movement movement = movementsDict.get(set.movementId);
            final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
            final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
            final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
            final BigDecimal primaryMusclesTotalWeight = secondaryMuscleIdsCount > 0 ? totalWeight.multiply(PRIMARY_MUSCLE_PERCENTAGE) : totalWeight;
            final BigDecimal secondaryMusclesTotalWeight = totalWeight.subtract(primaryMusclesTotalWeight);
            MovementVariant movementVariant = movementVariantsDict.get(MovementVariant.Id.BODY.id);
            if (set.movementVariantId != null) {
                movementVariant = movementVariantsDict.get(set.movementVariantId);
            }
            final MovementVariant movementVariantFinal = movementVariant;
            addTo(cd.timeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, totalWeight, loggedAt);
            addTo(cd.byMovementVariantTimeSeries, movementVariant.localIdentifier, totalWeight, loggedAt);
            final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
            final BigDecimal perPrimaryMuscleWeight = primaryMusclesTotalWeight.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
            final BigDecimal perSecondaryMuscleWeight = secondaryMuscleIdsCount > 0 ? secondaryMusclesTotalWeight.divide(BigDecimal.valueOf(secondaryMuscleIdsCount), DIVIDE_SCALE, RoundingMode.HALF_UP) : null;
            final TallyWeight tallyWeight = (muscleLocalIdentifier, weightToAdd) -> {
                final Muscle muscle = musclesDict.get(muscleLocalIdentifier);
                final MuscleGroup muscleGroup = muscleGroupsDict.get(muscle.muscleGroupId);
                final BodySegment bodySegment = bodySegmentsDict.get(muscleGroup.bodySegmentId);
                addTo(cd.byBodySegmentTimeSeries, bodySegment.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byMuscleGroupTimeSeries, muscleGroup.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byShoulderMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byChestMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byTricepsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byBicepsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byForearmsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byHamstringsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byQuadsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byCalfsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byGlutesMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byHipAbductorsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byHipFlexorsMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byCoreMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byBackMgTimeSeries, muscle.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byUpperBodySegmentTimeSeries, muscleGroup.localIdentifier, weightToAdd, loggedAt);
                addTo(cd.byLowerBodySegmentTimeSeries, muscleGroup.localIdentifier, weightToAdd, loggedAt);
                final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
                switch (bodySegmentId) {
                    case UPPER_BODY:
                        addTo(cd.upperBodyByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case LOWER_BODY:
                        addTo(cd.lowerBodyByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                }
                final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscleGroup.localIdentifier);
                switch (muscleGroupId) {
                    case CORE:
                        addTo(cd.coreByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case BACK:
                        addTo(cd.backByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case CALF:
                        addTo(cd.calfsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case HAMS:
                        addTo(cd.hamstringsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case CHEST:
                        addTo(cd.chestByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case QUADS:
                        addTo(cd.quadsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case BICEPS:
                        addTo(cd.bicepsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case GLUTES:
                        addTo(cd.glutesByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case HIP_ABDUCTORS:
                        addTo(cd.hipAbductorsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case HIP_FLEXORS:
                        addTo(cd.hipFlexorsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case TRICEPS:
                        addTo(cd.tricepsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case FOREARMS:
                        addTo(cd.forearmsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                    case SHOULDERS:
                        addTo(cd.shoulderByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, weightToAdd, loggedAt);
                        break;
                }
            };
            for (final Integer primaryMuscleId : primaryMuscleIds) {
                tallyWeight.invoke(primaryMuscleId, perPrimaryMuscleWeight);
            }
            if (perSecondaryMuscleWeight != null) {
                for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                    tallyWeight.invoke(secondaryMuscleId, perSecondaryMuscleWeight);
                }
            }
        }
        for (final Set set : sets) {
            holePluggerAndPercentageCalculator(cd.timeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byMuscleGroupTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byUpperBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byLowerBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byShoulderMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBackMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byCoreMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byChestMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byTricepsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBicepsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byForearmsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHamstringsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byQuadsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byCalfsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byGlutesMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHipAbductorsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHipFlexorsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.upperBodyByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.lowerBodyByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.shoulderByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.backByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.tricepsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.bicepsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.forearmsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.coreByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.chestByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hamstringsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.quadsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.calfsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.glutesByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hipAbductorsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hipFlexorsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
        }
        return cd;
    }

    public static ChartRawData weightLiftedDistChartStrengthRawDataForUser(final UserSettings userSettings,
                                                                           final List<BodySegment> bodySegments,
                                                                           final Map<Integer, BodySegment> bodySegmentsDict,
                                                                           final List<MuscleGroup> muscleGroups,
                                                                           final Map<Integer, MuscleGroup> muscleGroupsDict,
                                                                           final List<Muscle> muscles,
                                                                           final Map<Integer, Muscle> musclesDict,
                                                                           final Map<Integer, Movement> movementsDict,
                                                                           final List<MovementVariant> movementVariants,
                                                                           final Map<Integer, MovementVariant> movementVariantsDict,
                                                                           final List<Set> sets) {
        final ChartRawData cd = ChartRawData.makePieChartData();
        final int numSets = sets.size();
        if (numSets > 0) {
            final Set firstSet = sets.get(0);
            final Set lastSet = sets.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
        }
        for (final BodySegment bodySegment : bodySegments) {
            initBodySegmentPieData(cd.byBodySegment, bodySegment);
        }
        for (final MuscleGroup muscleGroup : muscleGroups) {
            initMuscleGroupPieData(cd.byMuscleGroup, muscleGroup);
            final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
            switch (bodySegmentId) {
                case UPPER_BODY:
                    initMuscleGroupPieData(cd.byUpperBodySegment, muscleGroup);
                    break;
                case LOWER_BODY:
                    initMuscleGroupPieData(cd.byLowerBodySegment, muscleGroup);
                    break;
            }
        }
        for (final Muscle muscle : muscles) {
            final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscle.muscleGroupId);
            switch (muscleGroupId) {
                case CORE:
                    initMusclePieData(cd.byCoreMg, muscle);
                    break;
                case BACK:
                    initMusclePieData(cd.byBackMg, muscle);
                    break;
                case CHEST:
                    initMusclePieData(cd.byChestMg, muscle);
                    break;
                case TRICEPS:
                    initMusclePieData(cd.byTricepsMg, muscle);
                    break;
                case SHOULDERS:
                    initMusclePieData(cd.byShoulderMg, muscle);
                    break;
            }
        }
        for (final MovementVariant movementVariant : movementVariants) {
            initMovementVariantPieData(cd.byMovementVariant, movementVariant);
            initMovementVariantPieData(cd.upperBodyByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.lowerBodyByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.shoulderByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.tricepsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.bicepsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.forearmsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.chestByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.backByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.coreByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hamstringsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.quadsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.calfsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.glutesByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hipAbductorsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hipFlexorsByMovementVariant, movementVariant);
        }
        for (int i = 0; i < numSets; i++) {
            final Set set = sets.get(i);
            final BigDecimal weight = Utils.weightValue(set.weight, WeightUnit.weightUnitById(set.weightUom), WeightUnit.weightUnitById(userSettings.weightUom));
            final int numReps = set.numReps;
            final BigDecimal totalWeight = weight.multiply(BigDecimal.valueOf(numReps));
            final Movement movement = movementsDict.get(set.movementId);
            final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
            final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
            final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
            final BigDecimal primaryMusclesTotalWeight = secondaryMuscleIdsCount > 0 ? totalWeight.multiply(PRIMARY_MUSCLE_PERCENTAGE) : totalWeight;
            final BigDecimal secondaryMusclesTotalWeight = totalWeight.subtract(primaryMusclesTotalWeight);
            MovementVariant movementVariant = movementVariantsDict.get(MovementVariant.Id.BODY.id);
            if (set.movementVariantId != null) {
                movementVariant = movementVariantsDict.get(set.movementVariantId);
            }
            final MovementVariant movementVariantFinal = movementVariant;
            addTo(cd.byMovementVariant, movementVariant.localIdentifier, totalWeight, null);
            final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
            final BigDecimal perPrimaryMuscleWeight = primaryMusclesTotalWeight.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
            final BigDecimal perSecondaryMuscleWeight = secondaryMuscleIdsCount > 0 ? secondaryMusclesTotalWeight.divide(BigDecimal.valueOf(secondaryMuscleIdsCount), DIVIDE_SCALE, RoundingMode.HALF_UP) : null;
            final TallyWeight tallyWeight = (muscleLocalIdentifier, weightToAdd) -> {
                final Muscle muscle = musclesDict.get(muscleLocalIdentifier);
                final MuscleGroup muscleGroup = muscleGroupsDict.get(muscle.muscleGroupId);
                final BodySegment bodySegment = bodySegmentsDict.get(muscleGroup.bodySegmentId);
                addTo(cd.byBodySegment, bodySegment.localIdentifier, weightToAdd, null);
                addTo(cd.byMuscleGroup, muscleGroup.localIdentifier, weightToAdd, null);
                addTo(cd.byShoulderMg, muscle.localIdentifier, weightToAdd, null);
                addTo(cd.byChestMg, muscle.localIdentifier, weightToAdd, null);
                addTo(cd.byTricepsMg, muscle.localIdentifier, weightToAdd, null);
                addTo(cd.byCoreMg, muscle.localIdentifier, weightToAdd, null);
                addTo(cd.byBackMg, muscle.localIdentifier, weightToAdd, null);
                addTo(cd.byUpperBodySegment, muscleGroup.localIdentifier, weightToAdd, null);
                addTo(cd.byLowerBodySegment, muscleGroup.localIdentifier, weightToAdd, null);
                final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
                switch (bodySegmentId) {
                    case UPPER_BODY:
                        addTo(cd.upperBodyByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case LOWER_BODY:
                        addTo(cd.lowerBodyByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                }
                final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscleGroup.localIdentifier);
                switch (muscleGroupId) {
                    case CORE:
                        addTo(cd.coreByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case BACK:
                        addTo(cd.backByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case CALF:
                        addTo(cd.calfsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case HAMS:
                        addTo(cd.hamstringsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case CHEST:
                        addTo(cd.chestByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case QUADS:
                        addTo(cd.quadsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case BICEPS:
                        addTo(cd.bicepsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case GLUTES:
                        addTo(cd.glutesByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case HIP_ABDUCTORS:
                        addTo(cd.hipAbductorsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case HIP_FLEXORS:
                        addTo(cd.hipFlexorsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case TRICEPS:
                        addTo(cd.tricepsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case FOREARMS:
                        addTo(cd.forearmsByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                    case SHOULDERS:
                        addTo(cd.shoulderByMovementVariant, movementVariantFinal.localIdentifier, weightToAdd, null);
                        break;
                }
            };
            for (final Integer primaryMuscleId : primaryMuscleIds) {
                tallyWeight.invoke(primaryMuscleId, perPrimaryMuscleWeight);
            }
            if (perSecondaryMuscleWeight != null) {
                for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                    tallyWeight.invoke(secondaryMuscleId, perSecondaryMuscleWeight);
                }
            }
        }
        return cd;
    }

    public static ChartRawData repsChartDataCrossSection(final List<MuscleGroup> muscleGroupList,
                                                         final Map<Integer, MuscleGroup> muscleGroupMap,
                                                         final Map<Integer, Muscle> muscleMap,
                                                         final Map<Integer, Movement> movementMap,
                                                         final List<Set> setList) {
        final ChartRawData cd = ChartRawData.makeCrossSectionChartData();
        final int numSets = setList.size();
        if (numSets > 0) {
            final Set firstSet = setList.get(0);
            final Set lastSet = setList.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
            for (final MuscleGroup muscleGroup : muscleGroupList) {
                initMuscleGroupPieData(cd.byMuscleGroup, muscleGroup);
                initMuscleGroupTimeSeriesData(cd.byMuscleGroupTimeSeries, muscleGroup);
            }
            for (int i = 0; i < numSets; i++) {
                final Set set = setList.get(i);
                final Date loggedAt = set.loggedAt;
                final BigDecimal numReps = BigDecimal.valueOf(set.numReps);
                final Movement movement = movementMap.get(set.movementId);
                final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
                final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
                final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
                final BigDecimal primaryMusclesTotalReps = PRIMARY_MUSCLE_PERCENTAGE.multiply(numReps);
                final BigDecimal secondaryMusclesTotalReps = numReps.subtract(primaryMusclesTotalReps);
                final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
                final BigDecimal perPrimaryMuscleReps = primaryMusclesTotalReps.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
                final BigDecimal perSecondaryMuscleReps = secondaryMuscleIdsCount > 0 ? secondaryMusclesTotalReps.divide(BigDecimal.valueOf(secondaryMuscleIdsCount), DIVIDE_SCALE, RoundingMode.HALF_UP) : null;
                final TallyReps tallyReps = (localMasterIdentifier, repsToAdd) -> {
                    final Muscle muscle = muscleMap.get(localMasterIdentifier);
                    final MuscleGroup muscleGroup = muscleGroupMap.get(muscle.muscleGroupId);
                    final Integer muscleGroupLocalMasterIdentifier = muscleGroup.localIdentifier;
                    addTo(cd.byMuscleGroup, muscleGroupLocalMasterIdentifier, repsToAdd, null);
                    addTo(cd.byMuscleGroupTimeSeries, muscleGroupLocalMasterIdentifier, repsToAdd, loggedAt);
                };
                for (final Integer primaryMuscleId : primaryMuscleIds) {
                    tallyReps.invoke(primaryMuscleId, perPrimaryMuscleReps);
                }
                if (perSecondaryMuscleReps != null) {
                    for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                        tallyReps.invoke(secondaryMuscleId, perSecondaryMuscleReps);
                    }
                }
            }
            for (final Set set : setList) {
                holePluggerAndPercentageCalculator(cd.byMuscleGroupTimeSeries, set.loggedAt, true, true);
            }
        }
        return cd;
    }

    public static ChartRawData repsLineChartStrengthRawDataForUser(final List<BodySegment> bodySegments,
                                                                   final Map<Integer, BodySegment> bodySegmentsDict,
                                                                   final List<MuscleGroup> muscleGroups,
                                                                   final Map<Integer, MuscleGroup> muscleGroupsDict,
                                                                   final List<Muscle> muscles,
                                                                   final Map<Integer, Muscle> musclesDict,
                                                                   final Map<Integer, Movement> movementsDict,
                                                                   final List<MovementVariant> movementVariants,
                                                                   final Map<Integer, MovementVariant> movementVariantsDict,
                                                                   final List<Set> sets,
                                                                   final boolean calcPercentages,
                                                                   final boolean calcAverages) {
        final ChartRawData cd = ChartRawData.makeLineChartData();
        final int numSets = sets.size();
        if (numSets > 0) {
            final Set firstSet = sets.get(0);
            final Set lastSet = sets.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
        }
        initSingleEntityTimeSeriesData(cd.timeSeries, "Total Reps");
        for (final BodySegment bodySegment : bodySegments) {
            initBodySegmentTimeSeriesData(cd.byBodySegmentTimeSeries, bodySegment);
        }
        for (final MuscleGroup muscleGroup : muscleGroups) {
            initMuscleGroupTimeSeriesData(cd.byMuscleGroupTimeSeries, muscleGroup);
            final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
            switch (bodySegmentId) {
                case UPPER_BODY:
                    initMuscleGroupTimeSeriesData(cd.byUpperBodySegmentTimeSeries, muscleGroup);
                    break;
                case LOWER_BODY:
                    initMuscleGroupTimeSeriesData(cd.byLowerBodySegmentTimeSeries, muscleGroup);
                    break;
            }
        }
        for (final Muscle muscle : muscles) {
            final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscle.muscleGroupId);
            switch (muscleGroupId) {
                case CORE:
                    initMuscleTimeSeriesData(cd.byCoreMgTimeSeries, muscle);
                    break;
                case BACK:
                    initMuscleTimeSeriesData(cd.byBackMgTimeSeries, muscle);
                    break;
                case CALF:
                    initMuscleTimeSeriesData(cd.byCalfsMgTimeSeries, muscle);
                    break;
                case HAMS:
                    initMuscleTimeSeriesData(cd.byHamstringsMgTimeSeries, muscle);
                    break;
                case CHEST:
                    initMuscleTimeSeriesData(cd.byChestMgTimeSeries, muscle);
                    break;
                case QUADS:
                    initMuscleTimeSeriesData(cd.byQuadsMgTimeSeries, muscle);
                    break;
                case BICEPS:
                    initMuscleTimeSeriesData(cd.byBicepsMgTimeSeries, muscle);
                    break;
                case GLUTES:
                    initMuscleTimeSeriesData(cd.byGlutesMgTimeSeries, muscle);
                    break;
                case HIP_ABDUCTORS:
                    initMuscleTimeSeriesData(cd.byHipAbductorsMgTimeSeries, muscle);
                    break;
                case HIP_FLEXORS:
                    initMuscleTimeSeriesData(cd.byHipFlexorsMgTimeSeries, muscle);
                    break;
                case TRICEPS:
                    initMuscleTimeSeriesData(cd.byTricepsMgTimeSeries, muscle);
                    break;
                case FOREARMS:
                    initMuscleTimeSeriesData(cd.byForearmsMgTimeSeries, muscle);
                    break;
                case SHOULDERS:
                    initMuscleTimeSeriesData(cd.byShoulderMgTimeSeries, muscle);
                    break;
            }
        }
        for (final MovementVariant movementVariant : movementVariants) {
            initMovementVariantTimeSeriesData(cd.byMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.upperBodyByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.lowerBodyByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.shoulderByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.tricepsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.bicepsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.forearmsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.chestByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.backByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.coreByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hamstringsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.quadsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.calfsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.glutesByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hipAbductorsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hipFlexorsByMovementVariantTimeSeries, movementVariant);
        }
        for (int i = 0; i < numSets; i++) {
            final Set set = sets.get(i);
            final Date loggedAt = set.loggedAt;
            final BigDecimal numReps = BigDecimal.valueOf(set.numReps);
            final Movement movement = movementsDict.get(set.movementId);
            final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
            final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
            final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
            final BigDecimal primaryMusclesTotalReps = PRIMARY_MUSCLE_PERCENTAGE.multiply(numReps);
            final BigDecimal secondaryMusclesTotalReps = numReps.subtract(primaryMusclesTotalReps);
            final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
            final BigDecimal perPrimaryMuscleReps = primaryMusclesTotalReps.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
            final BigDecimal perSecondaryMuscleReps = secondaryMuscleIdsCount > 0 ? secondaryMusclesTotalReps.divide(BigDecimal.valueOf(secondaryMuscleIdsCount), DIVIDE_SCALE, RoundingMode.HALF_UP) : null;
            MovementVariant movementVariant = movementVariantsDict.get(MovementVariant.Id.BODY.id);
            if (set.movementVariantId != null) {
                movementVariant = movementVariantsDict.get(set.movementVariantId);
            }
            final MovementVariant movementVariantFinal = movementVariant;
            addTo(cd.timeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, numReps, loggedAt);
            addTo(cd.byMovementVariantTimeSeries, movementVariant.localIdentifier, numReps, loggedAt);
            final TallyWeight tallyReps = (muscleLocalIdentifier, repsToAdd) -> {
                final Muscle muscle = musclesDict.get(muscleLocalIdentifier);
                final MuscleGroup muscleGroup = muscleGroupsDict.get(muscle.muscleGroupId);
                final BodySegment bodySegment = bodySegmentsDict.get(muscleGroup.bodySegmentId);
                addTo(cd.byBodySegmentTimeSeries, bodySegment.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byMuscleGroupTimeSeries, muscleGroup.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byShoulderMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byChestMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byTricepsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byBicepsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byForearmsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byHamstringsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byQuadsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byCalfsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byGlutesMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byHipAbductorsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byHipFlexorsMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byCoreMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byBackMgTimeSeries, muscle.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byUpperBodySegmentTimeSeries, muscleGroup.localIdentifier, repsToAdd, loggedAt);
                addTo(cd.byLowerBodySegmentTimeSeries, muscleGroup.localIdentifier, repsToAdd, loggedAt);
                final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
                switch (bodySegmentId) {
                    case UPPER_BODY:
                        addTo(cd.upperBodyByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case LOWER_BODY:
                        addTo(cd.lowerBodyByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                }
                final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscleGroup.localIdentifier);
                switch (muscleGroupId) {
                    case CORE:
                        addTo(cd.coreByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case BACK:
                        addTo(cd.backByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case CALF:
                        addTo(cd.calfsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case HAMS:
                        addTo(cd.hamstringsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case CHEST:
                        addTo(cd.chestByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case QUADS:
                        addTo(cd.quadsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case BICEPS:
                        addTo(cd.bicepsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case GLUTES:
                        addTo(cd.glutesByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case HIP_ABDUCTORS:
                        addTo(cd.hipAbductorsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case HIP_FLEXORS:
                        addTo(cd.hipFlexorsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case TRICEPS:
                        addTo(cd.tricepsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case FOREARMS:
                        addTo(cd.forearmsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                    case SHOULDERS:
                        addTo(cd.shoulderByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, repsToAdd, loggedAt);
                        break;
                }
            };
            for (final Integer primaryMuscleId : primaryMuscleIds) {
                tallyReps.invoke(primaryMuscleId, perPrimaryMuscleReps);
            }
            if (perSecondaryMuscleReps != null) {
                for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                    tallyReps.invoke(secondaryMuscleId, perSecondaryMuscleReps);
                }
            }
        }
        for (final Set set : sets) {
            holePluggerAndPercentageCalculator(cd.timeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byMuscleGroupTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byUpperBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byLowerBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byShoulderMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBackMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byCoreMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byChestMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byTricepsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBicepsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byForearmsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHamstringsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byQuadsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byCalfsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byGlutesMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHipAbductorsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHipFlexorsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.upperBodyByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.lowerBodyByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.shoulderByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.backByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.tricepsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.bicepsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.forearmsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.coreByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.chestByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hamstringsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.quadsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.calfsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.glutesByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hipAbductorsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hipFlexorsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
        }
        return cd;
    }

    public static ChartRawData repsDistChartStrengthRawDataForUser(final List<BodySegment> bodySegments,
                                                                   final Map<Integer, BodySegment> bodySegmentsDict,
                                                                   final List<MuscleGroup> muscleGroups,
                                                                   final Map<Integer, MuscleGroup> muscleGroupsDict,
                                                                   final List<Muscle> muscles,
                                                                   final Map<Integer, Muscle> musclesDict,
                                                                   final Map<Integer, Movement> movementsDict,
                                                                   final List<MovementVariant> movementVariants,
                                                                   final Map<Integer, MovementVariant> movementVariantsDict,
                                                                   final List<Set> sets) {
        final ChartRawData cd = ChartRawData.makePieChartData();
        final int numSets = sets.size();
        if (numSets > 0) {
            final Set firstSet = sets.get(0);
            final Set lastSet = sets.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
        }
        for (final BodySegment bodySegment : bodySegments) {
            initBodySegmentPieData(cd.byBodySegment, bodySegment);
        }
        for (final MuscleGroup muscleGroup : muscleGroups) {
            initMuscleGroupPieData(cd.byMuscleGroup, muscleGroup);
            final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
            switch (bodySegmentId) {
                case UPPER_BODY:
                    initMuscleGroupPieData(cd.byUpperBodySegment, muscleGroup);
                    break;
                case LOWER_BODY:
                    initMuscleGroupPieData(cd.byLowerBodySegment, muscleGroup);
                    break;
            }
        }
        for (final Muscle muscle : muscles) {
            final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscle.muscleGroupId);
            switch (muscleGroupId) {
                case CORE:
                    initMusclePieData(cd.byCoreMg, muscle);
                    break;
                case BACK:
                    initMusclePieData(cd.byBackMg, muscle);
                    break;
                case CHEST:
                    initMusclePieData(cd.byChestMg, muscle);
                    break;
                case TRICEPS:
                    initMusclePieData(cd.byTricepsMg, muscle);
                    break;
                case SHOULDERS:
                    initMusclePieData(cd.byShoulderMg, muscle);
                    break;
            }
        }
        for (final MovementVariant movementVariant : movementVariants) {
            initMovementVariantPieData(cd.byMovementVariant, movementVariant);
            initMovementVariantPieData(cd.upperBodyByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.lowerBodyByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.shoulderByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.tricepsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.bicepsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.forearmsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.chestByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.backByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.coreByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hamstringsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.quadsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.calfsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.glutesByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hipAbductorsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hipFlexorsByMovementVariant, movementVariant);
        }
        for (int i = 0; i < numSets; i++) {
            final Set set = sets.get(i);
            final BigDecimal numReps = BigDecimal.valueOf(set.numReps);
            final Movement movement = movementsDict.get(set.movementId);
            final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
            final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
            final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
            final BigDecimal primaryMusclesTotalReps = PRIMARY_MUSCLE_PERCENTAGE.multiply(numReps);
            final BigDecimal secondaryMusclesTotalReps = numReps.subtract(primaryMusclesTotalReps);
            final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
            final BigDecimal perPrimaryMuscleReps = primaryMusclesTotalReps.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
            final BigDecimal perSecondaryMuscleReps = secondaryMuscleIdsCount > 0 ? secondaryMusclesTotalReps.divide(BigDecimal.valueOf(secondaryMuscleIdsCount), DIVIDE_SCALE, RoundingMode.HALF_UP) : null;
            MovementVariant movementVariant = movementVariantsDict.get(MovementVariant.Id.BODY.id);
            if (set.movementVariantId != null) {
                movementVariant = movementVariantsDict.get(set.movementVariantId);
            }
            final MovementVariant movementVariantFinal = movementVariant;
            addTo(cd.byMovementVariant, movementVariant.localIdentifier, numReps, null);
            final TallyWeight tallyReps = (muscleLocalIdentifier, repsToAdd) -> {
                final Muscle muscle = musclesDict.get(muscleLocalIdentifier);
                final MuscleGroup muscleGroup = muscleGroupsDict.get(muscle.muscleGroupId);
                final BodySegment bodySegment = bodySegmentsDict.get(muscleGroup.bodySegmentId);
                addTo(cd.byBodySegment, bodySegment.localIdentifier, repsToAdd, null);
                addTo(cd.byMuscleGroup, muscleGroup.localIdentifier, repsToAdd, null);
                addTo(cd.byShoulderMg, muscle.localIdentifier, repsToAdd, null);
                addTo(cd.byChestMg, muscle.localIdentifier, repsToAdd, null);
                addTo(cd.byTricepsMg, muscle.localIdentifier, repsToAdd, null);
                addTo(cd.byCoreMg, muscle.localIdentifier, repsToAdd, null);
                addTo(cd.byBackMg, muscle.localIdentifier, repsToAdd, null);
                addTo(cd.byUpperBodySegment, muscleGroup.localIdentifier, repsToAdd, null);
                addTo(cd.byLowerBodySegment, muscleGroup.localIdentifier, repsToAdd, null);
                final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
                switch (bodySegmentId) {
                    case UPPER_BODY:
                        addTo(cd.upperBodyByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case LOWER_BODY:
                        addTo(cd.lowerBodyByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                }
                final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscleGroup.localIdentifier);
                switch (muscleGroupId) {
                    case CORE:
                        addTo(cd.coreByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case BACK:
                        addTo(cd.backByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case CALF:
                        addTo(cd.calfsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case HAMS:
                        addTo(cd.hamstringsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case CHEST:
                        addTo(cd.chestByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case QUADS:
                        addTo(cd.quadsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case BICEPS:
                        addTo(cd.bicepsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case GLUTES:
                        addTo(cd.glutesByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case HIP_ABDUCTORS:
                        addTo(cd.hipAbductorsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case HIP_FLEXORS:
                        addTo(cd.hipFlexorsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case TRICEPS:
                        addTo(cd.tricepsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case FOREARMS:
                        addTo(cd.forearmsByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                    case SHOULDERS:
                        addTo(cd.shoulderByMovementVariant, movementVariantFinal.localIdentifier, repsToAdd, null);
                        break;
                }
            };
            for (final Integer primaryMuscleId : primaryMuscleIds) {
                tallyReps.invoke(primaryMuscleId, perPrimaryMuscleReps);
            }
            if (perSecondaryMuscleReps!= null) {
                for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                    tallyReps.invoke(secondaryMuscleId, perSecondaryMuscleReps);
                }
            }
        }
        return cd;
    }

    private static final class LmidNormalizedTimeSeriesPair {
        public final Integer localIdentifier;
        public final List<NormalizedLineChartDataEntry> normalizedTimeSeries;

        public LmidNormalizedTimeSeriesPair(final Integer localIdentifier,
                final List<NormalizedLineChartDataEntry> normalizedTimeSeries) {
            this.localIdentifier = localIdentifier;
            this.normalizedTimeSeries = normalizedTimeSeries;
        }
    }

    public static NormalizedTimeSeriesTupleCollection normalizeUsingGroupInterval(final int groupSizeInDays,
                                                                                  final Date firstDate,
                                                                                  final Date lastDate,
                                                                                  final Map<Integer, RawLineDataPointsByDateTuple> rawContainer,
                                                                                  final boolean calculateAverages,
                                                                                  final boolean calculateDistributions,
                                                                                  final boolean logging) {
        if (firstDate == null) {
            return null;
        }
        final NormalizedTimeSeriesTupleCollection dataEntries = new NormalizedTimeSeriesTupleCollection();
        final java.util.Set<Integer> localMasterIdentifiers = rawContainer.keySet();
        final List<Map<Date, RawLineDataPointTuple>> rawDataPointsByDateDicts = new ArrayList();
        final List<LmidNormalizedTimeSeriesPair> lmidNormalizedTimeSeriesPairs = new ArrayList();
        for (final Integer localIdentifier : localMasterIdentifiers) {
            final RawLineDataPointsByDateTuple rawLineDataPointsByDateTuple = rawContainer.get(localIdentifier);
            final Map<Date, RawLineDataPointTuple> rawDataPointsByDate = rawLineDataPointsByDateTuple.dataPointsByDate;
            rawDataPointsByDateDicts.add(rawDataPointsByDate);
            final List<NormalizedLineChartDataEntry> normalizedTimeSeries = new ArrayList();
            final NormalizedTimeSeriesTuple normalizedTimeSeriesTuple = new NormalizedTimeSeriesTuple();
            normalizedTimeSeriesTuple.normalizedTimeSeries = normalizedTimeSeries;
            normalizedTimeSeriesTuple.name = rawLineDataPointsByDateTuple.name;
            normalizedTimeSeriesTuple.localIdentifier = rawLineDataPointsByDateTuple.localIdentifier;
            dataEntries.putNormalizedTimeSeriesTuple(localIdentifier, normalizedTimeSeriesTuple);
            lmidNormalizedTimeSeriesPairs.add(new LmidNormalizedTimeSeriesPair(localIdentifier, normalizedTimeSeries));
        }
        final int numRawDataPointsByDateDicts = rawDataPointsByDateDicts.size();
        if (numRawDataPointsByDateDicts > 0) {
            Map<Date, RawLineDataPointTuple> rawDataPointsByDate = rawDataPointsByDateDicts.get(0);
            final java.util.Set<Date> datesSet = rawDataPointsByDate.keySet();
            final Date firstDateInclusive = Utils.dateWithoutTime(firstDate);
            final DateTime firstDateInclusiveDateTime = new DateTime(firstDateInclusive);
            final Date lastDateInclusive = Utils.addSeconds(Utils.dateWithoutTime(lastDate), 1);
            final int numDaysBetweenDateEdges = Days.daysBetween(new DateTime(firstDateInclusive.getTime()),
                    new DateTime(lastDateInclusive.getTime())).getDays();
            final int numIntervals = (int)ceil(numDaysBetweenDateEdges / groupSizeInDays) + 1;
            final Map<Integer, BigDecimal> groupIndexTotals = new HashMap();
            for (int i = 0; i < numRawDataPointsByDateDicts; i++) {
                rawDataPointsByDate = rawDataPointsByDateDicts.get(i);
                final LmidNormalizedTimeSeriesPair lmidNormalizedTimeSeriesPair = lmidNormalizedTimeSeriesPairs.get(i);
                final List<NormalizedLineChartDataEntry> normalizedTimeSeries = lmidNormalizedTimeSeriesPair.normalizedTimeSeries;
                for (int j = 0; j < numIntervals; j++) {
                    final int daysForNextGroup = j * groupSizeInDays;
                    final Date group = Utils.addDays(firstDateInclusive, daysForNextGroup);
                    final NormalizedLineChartDataEntry dataEntry = new NormalizedLineChartDataEntry();
                    dataEntry.date = group;
                    normalizedTimeSeries.add(dataEntry);
                }
                for (final Date date : datesSet) {
                    final int daysSinceFirstDate = Days.daysBetween(firstDateInclusiveDateTime, new DateTime(date)).getDays();
                    final int groupIndex = (int)Math.floor(daysSinceFirstDate / groupSizeInDays);
                    final RawLineDataPointTuple rawDataPointTuple = rawDataPointsByDate.get(date);
                    final BigDecimal aggregateSum = rawDataPointTuple.sum;
                    final NormalizedLineChartDataEntry dataEntry = normalizedTimeSeries.get(groupIndex);
                    dataEntry.groupIndex = groupIndex;
                    if (aggregateSum.compareTo(BigDecimal.ZERO) != 0) {
                        dataEntry.aggregateSummedValue = dataEntry.aggregateSummedValue.add(aggregateSum);
                        dataEntry.count++;
                        BigDecimal groupIndexTotal = groupIndexTotals.get(groupIndex);
                        if (groupIndexTotal == null) {
                            groupIndexTotal = BigDecimal.ZERO;
                        }
                        groupIndexTotal = groupIndexTotal.add(aggregateSum);
                        groupIndexTotals.put(groupIndex, groupIndexTotal);
                        if (dataEntry.aggregateSummedValue.compareTo(dataEntries.maxAggregateSummedValue) > 0) {
                            dataEntries.maxAggregateSummedValue = dataEntry.aggregateSummedValue;
                        }
                    }
                }
            }
            // 2nd pass to calculate averages / percentages
            if (calculateAverages || calculateDistributions) {
                for (int i = 0; i < numRawDataPointsByDateDicts; i++) {
                    final LmidNormalizedTimeSeriesPair localMasterIdentifierTimeSeriesPair = lmidNormalizedTimeSeriesPairs.get(i);
                    for (NormalizedLineChartDataEntry dataEntry : localMasterIdentifierTimeSeriesPair.normalizedTimeSeries) {
                        if (dataEntry.count > 0) {
                            if (calculateAverages) {
                                dataEntry.calculateAvgAggregateValue();
                                final BigDecimal avgAggregateVal = dataEntry.avgAggregateValue;
                                if (avgAggregateVal.compareTo(dataEntries.maxAvgAggregateValue) > 0) {
                                    dataEntries.maxAvgAggregateValue = avgAggregateVal;
                                }
                            }
                            if (calculateDistributions) {
                                dataEntry.calculateDistribution(groupIndexTotals);
                                final BigDecimal distributionValue = dataEntry.distribution;
                                if (distributionValue.compareTo(dataEntries.maxDistributionValue) > 0) {
                                    dataEntries.maxDistributionValue = distributionValue;
                                }
                            }
                        }
                    }
                }
            }
        }
        return dataEntries;
    }

    public static ChartConfig.AggregateBy suggestedAggregateBy(final Date firstDate, final Date lastDate) {
        final int numDaysBetweenDateEdges = Days.daysBetween(new DateTime(firstDate.getTime()),
                new DateTime(lastDate.getTime())).getDays();
        if (numDaysBetweenDateEdges >= 4380) { // 12 years worth of days
            return ChartConfig.AggregateBy.YEAR;
        }
        if (numDaysBetweenDateEdges >= 2190) { // 6 years worth of days
            return ChartConfig.AggregateBy.HALF_YEAR;
        }
        if (numDaysBetweenDateEdges >= 1095) { // 3 years worth of days
            return ChartConfig.AggregateBy.QUARTER;
        }
        if (numDaysBetweenDateEdges >= 540) { // 1.5 years worth of days
            return ChartConfig.AggregateBy.MONTH;
        }
        if (numDaysBetweenDateEdges >= 93) { // 3 months worth of days
            return ChartConfig.AggregateBy.WEEK;
        }
        return ChartConfig.AggregateBy.DAY;
    }

    public static final void configureUiDataSet(final LineDataSet lineDataSet, final @ColorRes int lineColor, final float lineWidth) {
        final int dataEntriesCount = lineDataSet.getEntryCount();
        if (dataEntriesCount > 10) {
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setCircleRadius(2.0f);
        } else if (dataEntriesCount > 5) {
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setCircleRadius(3.0f);
        } else if (dataEntriesCount > 4) {
            lineDataSet.setDrawCircleHole(true);
            lineDataSet.setCircleRadius(4.5f);
        } else if (dataEntriesCount > 3) {
            lineDataSet.setDrawCircleHole(true);
            lineDataSet.setCircleRadius(5.0f);
        } else if (dataEntriesCount > 2) {
            lineDataSet.setDrawCircleHole(true);
            lineDataSet.setCircleRadius(5.25f);
        } else if (dataEntriesCount > 1) {
            lineDataSet.setDrawCircleHole(true);
            lineDataSet.setCircleRadius(5.5f);
        } else if (dataEntriesCount > 0) { // just one
            final Entry entry = lineDataSet.getEntryForIndex(0);
            if (entry.getY() > 0.0f) {
                lineDataSet.setDrawCircleHole(true);
                lineDataSet.setCircleRadius(6.0f);
            } else { // hide the point
                lineDataSet.setDrawCircleHole(false);
                lineDataSet.setCircleRadius(0.0f);
            }
        } else {
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setCircleRadius(0.0f);
        }
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setColor(lineColor);
        lineDataSet.setCircleColor(lineColor);
        lineDataSet.setLineWidth(lineWidth);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
    }

    public static final float yaxisScalingFactor(final BigDecimal maxValue) {
        final float max = maxValue.floatValue();
        if (max > 999999f) {
            return 0.000001f;
        } else if (max > 99999) {
            return 0.00001f;
        } else if (max > 9999) {
            return 0.0001f;
        } else if (max > 999) {
            return 0.001f;
        } else {
            return 1.0f;
        }
    }

    public static final String xaxisDateFormatPattern(final ChartConfig.AggregateBy aggregateBy) {
        switch (aggregateBy) {
            case DAY:
                return "d MMM";
            case WEEK:
                return "d MMM";
            case MONTH:
                return "MMM";
            case QUARTER:
                return "MMM ''yy";
            case HALF_YEAR:
                return "MMM ''yy";
            case YEAR:
                return "yyyy";
        }
        return null;
    }

    public static final LineChartDataContainer lineChartDataContainer(final Resources resources,
                                                                      final Chart chart,
                                                                      final ChartRawDataContainer defaultChartRawDataContainer,
                                                                      final RikerDao rikerDao,
                                                                      final Function.ChartRawDataMaker chartRawDataMaker,
                                                                      final ChartDataFetchMode fetchMode,
                                                                      final LruCache<String, List> entitiesCache,
                                                                      final LruCache<String, ChartRawData> chartRawDataCache,
                                                                      final ChartConfig.AggregateBy defaultAggregateBy,
                                                                      final ChartColorsContainer chartColorsContainer,
                                                                      final boolean calcPercentages,
                                                                      final boolean calcAverages,
                                                                      final boolean headless) {
        boolean cacheHit = false;
        Integer chartConfigLocalIdentifier = null;
        boolean wasConfigSet = false;
        @DrawableRes int settingsButtonIconImageName = R.drawable.riker_semi_black_settings;
        final ChartConfig chartConfig = rikerDao.chartConfig(chart, defaultChartRawDataContainer.user);
        if (chartConfig != null) {
            settingsButtonIconImageName = R.drawable.riker_blue_settings;
            chartConfigLocalIdentifier = chartConfig.localIdentifier;
            wasConfigSet = true;
        }
        LineChartDataContainer lineChartDataContainer = rikerDao.lineChartDataContainerCache(chart.id, chartConfigLocalIdentifier, defaultChartRawDataContainer.user);
        if (lineChartDataContainer != null) {
            //Timber.d("inside lineChartDataContainer(), lineChartDataContainer sqlite cache HIT for chart id: [%s]", chart.id);
            cacheHit = true;
            if (headless) {
                return null; // in headless mode, the return value of this function is not used
            }
            lineChartDataContainer.wasConfigSet = wasConfigSet;
            lineChartDataContainer.category = fetchMode.category;
            lineChartDataContainer.chart = chart;
            lineChartDataContainer.chartConfig = chartConfig;
            lineChartDataContainer.user = defaultChartRawDataContainer.user;
            lineChartDataContainer.chartConfigLocalIdentifier = chartConfigLocalIdentifier;
            lineChartDataContainer.cacheHit = cacheHit;
            lineChartDataContainer.userSettings = defaultChartRawDataContainer.userSettings;
            if (chart.yAxisValueLabelMaker != null) {
                lineChartDataContainer.yAxisLabelText = chart.yAxisValueLabelMaker.invoke(defaultChartRawDataContainer.userSettings, lineChartDataContainer.maxyValue);
            }
            lineChartDataContainer.yaxisValueFormatter = chart.yaxisValueFormatterMaker.invoke(lineChartDataContainer.maxyValue);
            lineChartDataContainer.xaxisFormatter = new DateValueFormatter(ChartUtils.xaxisDateFormatPattern(lineChartDataContainer.aggregateBy));
            lineChartDataContainer.yaxisMaximum = chart.yaxisMaximumFn.invoke(lineChartDataContainer.maxyValue);
            lineChartDataContainer.yaxisMinimum = chart.yaxisMinimum;
            lineChartDataContainer.settingsButtonIconImageName = settingsButtonIconImageName;
            final int numEntities = defaultChartRawDataContainer.entities.size();
            if (numEntities > 0) {
                lineChartDataContainer.firstEntityDate = defaultChartRawDataContainer.entities.get(0).loggedAt;
                lineChartDataContainer.lastEntityDate = defaultChartRawDataContainer.entities.get(numEntities - 1).loggedAt;
            }
            final List<ILineDataSet> uiDataSets = lineChartDataContainer.uiLineChartData.getDataSets();
            for (final ILineDataSet dataSet : uiDataSets) {
                final RLineDataSet rLineDataSet = (RLineDataSet)dataSet;
                final Integer colorInteger = chart.chartColorsFn.invoke(chartColorsContainer).get(rLineDataSet.entityLocalIdentifier);
                final @ColorRes int color = colorInteger.intValue();
                configureUiDataSet(rLineDataSet, color, chart.lineWidthFn.invoke(resources));
            }
            return lineChartDataContainer;
        } else {
            lineChartDataContainer = new LineChartDataContainer();
            if (!headless) {
                lineChartDataContainer.settingsButtonIconImageName = settingsButtonIconImageName;
                lineChartDataContainer.wasConfigSet = wasConfigSet;
                lineChartDataContainer.userSettings = defaultChartRawDataContainer.userSettings;
            }
            lineChartDataContainer.chartConfig = chartConfig;
            lineChartDataContainer.yaxisMinimum = chart.yaxisMinimum;
            lineChartDataContainer.chartConfigLocalIdentifier = chartConfigLocalIdentifier;
            lineChartDataContainer.user = defaultChartRawDataContainer.user;
            lineChartDataContainer.chart = chart;
            lineChartDataContainer.category = fetchMode.category;
            lineChartDataContainer.cacheHit = cacheHit;
            int numEntities = defaultChartRawDataContainer.entities.size();
            if (numEntities > 0) {
                lineChartDataContainer.firstEntityDate = defaultChartRawDataContainer.entities.get(0).loggedAt;
                lineChartDataContainer.lastEntityDate = defaultChartRawDataContainer.entities.get(numEntities - 1).loggedAt;
            }
            //Timber.d("inside lineChartDataContainer(), lineChartDataContainer sqlite cache MISS for chart id: [%s]", chart.id);
            NormalizedTimeSeriesTupleCollection normalizedTimeSeriesCollection = null;
            ValueFormatter xaxisFormatter;
            ChartConfig.AggregateBy aggregateBy = defaultAggregateBy;
            lineChartDataContainer.aggregateBy = aggregateBy;
            if (chartConfig != null) {
                aggregateBy = chartConfig.aggregateBy;
                // make a cache key, attempt to get sets from cache; if null, then really fetch them, add to cache; then do the same thing for the chart raw data
                List filteredEntities;
                String cacheKeySubPart;
                String entitiesCacheKey;
                xaxisFormatter = headless ? null : new DateValueFormatter(ChartUtils.xaxisDateFormatPattern(aggregateBy));
                if (chartConfig.boundedEndDate) {
                    cacheKeySubPart = String.format("_%d_%d", chartConfig.startDate.getTime(), chartConfig.endDate.getTime());
                    entitiesCacheKey = String.format("entities_%s", cacheKeySubPart);
                    filteredEntities = entitiesCache != null ? entitiesCache.get(entitiesCacheKey) : null;
                    if (filteredEntities == null) {
                        //Timber.d("inside lineChartDataContainer(), Cache MISS for filtered, bounded entities, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                        filteredEntities = chart.boundedAscendingEntitiesFn.invoke(rikerDao, defaultChartRawDataContainer.user, chartConfig.startDate, chartConfig.endDate);
                        if (entitiesCache != null) {
                            entitiesCache.put(entitiesCacheKey, filteredEntities);
                        }
                    } else {
                        //Timber.d("inside lineChartDataContainer(), Cache HIT for filtered, bounded entities, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                    }
                } else {
                    cacheKeySubPart = String.format("_%d", chartConfig.startDate.getTime());
                    entitiesCacheKey = String.format("entities_%s", cacheKeySubPart);
                    filteredEntities = entitiesCache != null ? entitiesCache.get(entitiesCacheKey) : null;
                    if (filteredEntities == null) {
                        //Timber.d("inside lineChartDataContainer(), Cache MISS for filtered, unbounded entities, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                        filteredEntities = chart.ascendingEntitiesFn.invoke(rikerDao, defaultChartRawDataContainer.user, chartConfig.startDate);
                        if (entitiesCache != null) {
                            entitiesCache.put(entitiesCacheKey, filteredEntities);
                        }
                    } else {
                        //Timber.d("inside lineChartDataContainer(), Cache HIT for filtered, unbounded entities, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                    }
                }
                //Timber.d("inside lineChartDataContainer(), filtered entities size: [%d] for chart id: [%s]", filteredEntities.size(), chart.id);
                if (filteredEntities.size() > 0) {
                    final String rawDataCacheKey = String.format("raw_data_%s_fetch_mode_%s", cacheKeySubPart, fetchMode);
                    ChartRawData filteredChartRawData = chartRawDataCache != null ? chartRawDataCache.get(rawDataCacheKey) : null;
                    if (filteredChartRawData == null) {
                        //Timber.d("inside lineChartDataContainer(), Cache MISS for chart raw data, cache key: [%s], for chart id: [%s]", rawDataCacheKey, chart.id);
                        filteredChartRawData = chartRawDataMaker.invoke(defaultChartRawDataContainer.userSettings,
                                defaultChartRawDataContainer.bodySegmentList,
                                defaultChartRawDataContainer.bodySegmentMap,
                                defaultChartRawDataContainer.muscleGroupList,
                                defaultChartRawDataContainer.muscleGroupMap,
                                defaultChartRawDataContainer.muscleList,
                                defaultChartRawDataContainer.muscleMap,
                                defaultChartRawDataContainer.movementMap,
                                defaultChartRawDataContainer.movementVariantList,
                                defaultChartRawDataContainer.movementVariantMap,
                                filteredEntities,
                                calcPercentages,
                                calcAverages);
                        if (chartRawDataCache != null) {
                            chartRawDataCache.put(rawDataCacheKey, filteredChartRawData);
                        }
                    } else {
                        //Timber.d("inside lineChartDataContainer(), Cache HIT for chart raw data, cache key: [%s], for chart id: [%s]", rawDataCacheKey, chart.id);
                    }
                    normalizedTimeSeriesCollection = chart.normalizedTimeSeriesCollectionMaker.invoke(filteredChartRawData, aggregateBy);
                }
            } else {
                //Timber.d("inside lineChartDataContainer(), Null chart config, so using default chart raw data for chart id: [%s]", chart.id);
                xaxisFormatter = headless ? null : new DateValueFormatter(ChartUtils.xaxisDateFormatPattern(defaultAggregateBy));
                normalizedTimeSeriesCollection = chart.normalizedTimeSeriesCollectionMaker.invoke(defaultChartRawDataContainer.chartRawData, defaultAggregateBy);
            }
            //Timber.d("inside lineChartDataContainer() - 1 - [%s]", chart.id);
            if (normalizedTimeSeriesCollection != null) {
                //Timber.d("inside lineChartDataContainer() - 2 - [%s]", chart.id);
                final BigDecimal maxyValue = chart.maxValueMaker.invoke(normalizedTimeSeriesCollection);
                if (maxyValue.compareTo(BigDecimal.ZERO) > 0) {
                    //Timber.d("inside lineChartDataContainer() - 3 - [%s]", chart.id);
                    final List<NormalizedTimeSeriesTuple> normalizedTimeSeriesTuples = new ArrayList<>(normalizedTimeSeriesCollection.normalizedTimeSeriesTupleMap.values());
                    Collections.sort(normalizedTimeSeriesTuples, (tuple1, tuple2) -> tuple1.name.compareTo(tuple2.name));
                    final List<ILineDataSet> uiDataSets = headless ? null : new ArrayList<>();
                    final List<RLineChartDataSeries> dataSets = new ArrayList<>();
                    int xaxisLabelCount = 0;
                    final Iterator<NormalizedTimeSeriesTuple> normalizedTimeSeriesTuplesIterator = normalizedTimeSeriesTuples.iterator();
                    while (normalizedTimeSeriesTuplesIterator.hasNext()) {
                        final NormalizedTimeSeriesTuple normalizedTimeSeriesTuple = normalizedTimeSeriesTuplesIterator.next();
                        final @ColorRes int color = chart.chartColorsFn.invoke(chartColorsContainer).get(normalizedTimeSeriesTuple.localIdentifier);
                        final int normalizedTimeSeriesCount = normalizedTimeSeriesTuple.normalizedTimeSeries.size();
                        if (normalizedTimeSeriesCount > xaxisLabelCount) {
                            xaxisLabelCount = normalizedTimeSeriesCount;
                        }
                        final List<Entry> uiLineChartDataEntries = headless ? null : new ArrayList<>();
                        final List<RLineDataPoint> lineChartDataEntries = new ArrayList<>();
                        for (int i = 0; i < normalizedTimeSeriesCount; i++) {
                            final NormalizedLineChartDataEntry normalizedLineChartDataEntry = normalizedTimeSeriesTuple.normalizedTimeSeries.get(i);
                            final BigDecimal value = chart.yvalueMaker.invoke(normalizedLineChartDataEntry);
                            if (value.compareTo(BigDecimal.ZERO) > 0) {
                                if (!headless) {
                                    uiLineChartDataEntries.add(new Entry(normalizedLineChartDataEntry.date.getTime(), value.floatValue()));
                                }
                                lineChartDataEntries.add(new RLineDataPoint(normalizedLineChartDataEntry.date.getTime(), value.floatValue()));
                            }
                        }
                        if (!headless) {
                            if (uiLineChartDataEntries.size() > 0) {
                                final LineDataSet uiLineDataSet = new LineDataSet(uiLineChartDataEntries, normalizedTimeSeriesTuple.name);
                                configureUiDataSet(uiLineDataSet, color, chart.lineWidthFn.invoke(resources));
                                uiDataSets.add(uiLineDataSet);
                            }
                        }
                        if (lineChartDataEntries.size() > 0) {
                            dataSets.add(new RLineChartDataSeries(lineChartDataEntries,
                                    normalizedTimeSeriesTuple.name,
                                    null,
                                    normalizedTimeSeriesTuple.localIdentifier));
                        }
                    }
                    if (xaxisLabelCount > 6) {
                        xaxisLabelCount = 6;
                    }
                    if (!headless) {
                        lineChartDataContainer.uiLineChartData = new LineData(uiDataSets);
                        lineChartDataContainer.yaxisValueFormatter = chart.yaxisValueFormatterMaker.invoke(maxyValue);
                        lineChartDataContainer.xaxisFormatter = xaxisFormatter;
                        if (chart.yAxisValueLabelMaker != null) {
                            lineChartDataContainer.yAxisLabelText = chart.yAxisValueLabelMaker.invoke(defaultChartRawDataContainer.userSettings, maxyValue);
                        }
                    }
                    lineChartDataContainer.lineChartData = new RLineChartData(dataSets);
                    lineChartDataContainer.xaxisLabelCount = xaxisLabelCount;
                    lineChartDataContainer.yaxisMaximum = chart.yaxisMaximumFn.invoke(maxyValue);
                    lineChartDataContainer.aggregateBy = aggregateBy;
                    lineChartDataContainer.maxyValue = maxyValue;
                    numEntities = defaultChartRawDataContainer.entities.size();
                    if (numEntities > 0) {
                        lineChartDataContainer.firstEntityDate = defaultChartRawDataContainer.entities.get(0).loggedAt;
                        lineChartDataContainer.lastEntityDate = defaultChartRawDataContainer.entities.get(numEntities - 1).loggedAt;
                    }
                }
            }
        }
        return lineChartDataContainer;
    }

    private static final void setMuscleColors(final List<Muscle> muscles,
                                              final Integer muscleGroupId,
                                              final Map<Integer, Map<Integer, Integer>> muscleGroupMuscleColors,
                                              final int colors[]) {
        final Map<Integer, Integer> muscleColors = muscleGroupMuscleColors.get(muscleGroupId);
        int colorIndex = 0;
        for (final Muscle muscle : muscles) {
            if (muscle.muscleGroupId.equals(muscleGroupId)) {
                muscleColors.put(muscle.localIdentifier, colors[colorIndex]);
                colorIndex++;
            }
        }
    }

    public static final ChartColorsContainer chartColorsContainer(final Context context, final Resources resources, final List<Muscle> muscleList) {
        final int mainColors[] = resources.getIntArray(R.array.mainColors);
        final ChartColorsContainer fetchData = new ChartColorsContainer();

        if (muscleList != null) {
            fetchData.singleValueColor = new HashMap<>();
            fetchData.singleValueColor.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.bootstrapBlue));

            fetchData.bodySegmentColors = new HashMap<>();
            fetchData.bodySegmentColors.put(BodySegment.Id.UPPER_BODY.id, mainColors[0]);
            fetchData.bodySegmentColors.put(BodySegment.Id.LOWER_BODY.id, mainColors[1]);

            fetchData.muscleGroupColors = new HashMap<>();
            fetchData.muscleGroupColors.put(MuscleGroup.Id.SHOULDERS.id, mainColors[0]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.CHEST.id, mainColors[1]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.TRICEPS.id, mainColors[2]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.CORE.id, mainColors[3]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.FOREARMS.id, mainColors[4]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.BACK.id, mainColors[5]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.BICEPS.id, mainColors[6]);

            fetchData.muscleGroupColors.put(MuscleGroup.Id.GLUTES.id, mainColors[7]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.QUADS.id, mainColors[8]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.HAMS.id, mainColors[9]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.CALF.id, mainColors[10]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.HIP_ABDUCTORS.id, mainColors[11]);
            fetchData.muscleGroupColors.put(MuscleGroup.Id.HIP_FLEXORS.id, mainColors[12]);

            fetchData.lowerBodyMuscleGroupColors = new HashMap<>();
            fetchData.lowerBodyMuscleGroupColors.put(MuscleGroup.Id.GLUTES.id, mainColors[7]);
            fetchData.lowerBodyMuscleGroupColors.put(MuscleGroup.Id.QUADS.id, mainColors[8]);
            fetchData.lowerBodyMuscleGroupColors.put(MuscleGroup.Id.HAMS.id, mainColors[9]);
            fetchData.lowerBodyMuscleGroupColors.put(MuscleGroup.Id.CALF.id, mainColors[10]);
            fetchData.lowerBodyMuscleGroupColors.put(MuscleGroup.Id.HIP_ABDUCTORS.id, mainColors[11]);
            fetchData.lowerBodyMuscleGroupColors.put(MuscleGroup.Id.HIP_FLEXORS.id, mainColors[12]);

            fetchData.movementVariantColors = new HashMap<>();
            fetchData.movementVariantColors.put(MovementVariant.Id.BARBELL.id, mainColors[0]);
            fetchData.movementVariantColors.put(MovementVariant.Id.DUMBBELL.id, mainColors[1]);
            fetchData.movementVariantColors.put(MovementVariant.Id.MACHINE.id, mainColors[2]);
            fetchData.movementVariantColors.put(MovementVariant.Id.SMITH_MACHINE.id, mainColors[3]);
            fetchData.movementVariantColors.put(MovementVariant.Id.CABLE.id, mainColors[4]);
            fetchData.movementVariantColors.put(MovementVariant.Id.CURL_BAR.id, mainColors[5]);
            fetchData.movementVariantColors.put(MovementVariant.Id.SLED.id, mainColors[6]);
            fetchData.movementVariantColors.put(MovementVariant.Id.BODY.id, mainColors[7]);
            fetchData.movementVariantColors.put(MovementVariant.Id.KETTLEBELL.id, mainColors[8]);

            fetchData.muscleGroupMuscleColors = new HashMap<>();
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.SHOULDERS.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.CHEST.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.TRICEPS.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.CORE.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.FOREARMS.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.BACK.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.BICEPS.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.GLUTES.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.QUADS.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.HAMS.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.CALF.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.HIP_ABDUCTORS.id, new HashMap<>());
            fetchData.muscleGroupMuscleColors.put(MuscleGroup.Id.HIP_FLEXORS.id, new HashMap<>());

            setMuscleColors(muscleList, MuscleGroup.Id.SHOULDERS.id, fetchData.muscleGroupMuscleColors, mainColors);
            setMuscleColors(muscleList, MuscleGroup.Id.CHEST.id, fetchData.muscleGroupMuscleColors, mainColors);
            setMuscleColors(muscleList, MuscleGroup.Id.TRICEPS.id, fetchData.muscleGroupMuscleColors, mainColors);
            setMuscleColors(muscleList, MuscleGroup.Id.CORE.id, fetchData.muscleGroupMuscleColors, mainColors);
            setMuscleColors(muscleList, MuscleGroup.Id.BACK.id, fetchData.muscleGroupMuscleColors, mainColors);
            setMuscleColors(muscleList, MuscleGroup.Id.HAMS.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[9]});
            setMuscleColors(muscleList, MuscleGroup.Id.BICEPS.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[6]});
            setMuscleColors(muscleList, MuscleGroup.Id.FOREARMS.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[4]});
            setMuscleColors(muscleList, MuscleGroup.Id.QUADS.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[8]});
            setMuscleColors(muscleList, MuscleGroup.Id.CALF.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[10]});
            setMuscleColors(muscleList, MuscleGroup.Id.GLUTES.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[7]});
            setMuscleColors(muscleList, MuscleGroup.Id.HIP_ABDUCTORS.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[11]});
            setMuscleColors(muscleList, MuscleGroup.Id.HIP_FLEXORS.id, fetchData.muscleGroupMuscleColors, new int[]{mainColors[12]});
        } else {
            fetchData.bodyWeightColors = new HashMap<>();
            fetchData.bodyWeightColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.bodyWeightSection));
            fetchData.armSizeColors = new HashMap<>();
            fetchData.armSizeColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.armSizeSection));
            fetchData.chestSizeColors = new HashMap<>();
            fetchData.chestSizeColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.chestSizeSection));
            fetchData.calfSizeColors = new HashMap<>();
            fetchData.calfSizeColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.calfSizeSection));
            fetchData.thighSizeColors = new HashMap<>();
            fetchData.thighSizeColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.thighSizeSection));
            fetchData.forearmSizeColors = new HashMap<>();
            fetchData.forearmSizeColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.forearmSizeSection));
            fetchData.waistSizeColors = new HashMap<>();
            fetchData.waistSizeColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.waistSizeSection));
            fetchData.neckSizeColors = new HashMap<>();
            fetchData.neckSizeColors.put(LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, ContextCompat.getColor(context, R.color.neckSizeSection));
        }
        return fetchData;
    }

    public static void removeNoDataProgressBar(final ViewGroup container) {
        Utils.safeRemove(container, R.id.noDataProgressBar);
    }

    public static void removeChartProgressBar(final ViewGroup container) {
        Utils.safeRemove(container, R.id.chartProgressBar);
    }

    public static void showChartProgressBar(final ViewGroup container) {
        if (container != null) {
            final ViewGroup chartContainer = container.findViewById(R.id.chartContainer);
            if (chartContainer.getVisibility() == View.VISIBLE) {
                container.findViewById(R.id.chartProgressBar).setVisibility(View.VISIBLE);
            } else {
                container.findViewById(R.id.noDataProgressBar).setVisibility(View.VISIBLE);
            }
        }
    }

    public static void removeChartSettingsButton(final ViewGroup container) {
        Utils.safeMakeInvisible(container, R.id.settingsImageButton);
    }

    public static void showNoDataContainer(final ViewGroup container) {
        Utils.safeRemove(container, R.id.chartContainer);
        Utils.safeMakeVisible(container, R.id.noDataToChartYetContainer);
    }

    public static final PieChartDataContainer pieChartDataContainer(final Resources resources,
                                                                    final Chart chart,
                                                                    final ChartRawDataContainer defaultChartRawDataContainer,
                                                                    final RikerDao rikerDao,
                                                                    final Function.ChartRawDataMaker chartRawDataMaker,
                                                                    final ChartDataFetchMode fetchMode,
                                                                    final LruCache<String, List> entitiesCache,
                                                                    final LruCache<String, ChartRawData> chartRawDataCache,
                                                                    final ChartColorsContainer chartColorsContainer,
                                                                    final boolean headless) {
        Integer chartConfigLocalIdentifier = null;
        boolean wasConfigSet = false;
        @DrawableRes int settingsButtonIconImageName = R.drawable.riker_semi_black_settings;
        final ChartConfig chartConfig = rikerDao.chartConfig(chart, defaultChartRawDataContainer.user);
        if (chartConfig != null) {
            settingsButtonIconImageName = R.drawable.riker_blue_settings;
            chartConfigLocalIdentifier = chartConfig.localIdentifier;
            wasConfigSet = true;
        }
        ChartRawData chartRawData = defaultChartRawDataContainer.chartRawData;
        if (chartConfig != null) {
            chartConfigLocalIdentifier = chartConfig.localIdentifier;
            // make a cache key, attempt to get sets from cache; if null, then really fetch them, add to cache; then do the same thing for the chart raw data
            List filteredEntities;
            String cacheKeySubPart;
            String entitiesCacheKey;
            if (chartConfig.boundedEndDate) {
                cacheKeySubPart = String.format("_%d_%d", chartConfig.startDate.getTime(), chartConfig.endDate.getTime());
                entitiesCacheKey = String.format("entities_%s", cacheKeySubPart);
                filteredEntities = entitiesCache.get(entitiesCacheKey);
                if (filteredEntities == null) {
                    Timber.d("Cache MISS for filtered, bounded sets, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                    filteredEntities = chart.boundedAscendingEntitiesFn.invoke(rikerDao, defaultChartRawDataContainer.user, chartConfig.startDate, chartConfig.endDate);
                    entitiesCache.put(entitiesCacheKey, filteredEntities);
                } else {
                    Timber.d("Cache HIT for filtered, bounded sets, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                }
            } else {
                cacheKeySubPart = String.format("_%d", chartConfig.startDate.getTime());
                entitiesCacheKey = String.format("entities_%s", cacheKeySubPart);
                filteredEntities = entitiesCache != null ? entitiesCache.get(entitiesCacheKey) : null;
                if (filteredEntities == null) {
                    Timber.d("Cache MISS for filtered, unbounded sets, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                    filteredEntities = chart.ascendingEntitiesFn.invoke(rikerDao, defaultChartRawDataContainer.user, chartConfig.startDate);
                    if (entitiesCache != null) {
                        entitiesCache.put(entitiesCacheKey, filteredEntities);
                    }
                } else {
                    Timber.d("Cache HIT for filtered, unbounded sets, cache key: [%s], for chart id: [%s]", entitiesCacheKey, chart.id);
                }
            }
            if (filteredEntities.size() > 0) {
                final String rawDataCacheKey = String.format("raw_data_%s_fetch_mode_%s", cacheKeySubPart, fetchMode);
                chartRawData = chartRawDataCache != null ? chartRawDataCache.get(rawDataCacheKey) : null;
                if (chartRawData == null) {
                    Timber.d("Cache MISS for chart raw data, cache key: [%s], for chart id: [%s]", rawDataCacheKey, chart.id);
                    chartRawData = chartRawDataMaker.invoke(defaultChartRawDataContainer.userSettings,
                            defaultChartRawDataContainer.bodySegmentList,
                            defaultChartRawDataContainer.bodySegmentMap,
                            defaultChartRawDataContainer.muscleGroupList,
                            defaultChartRawDataContainer.muscleGroupMap,
                            defaultChartRawDataContainer.muscleList,
                            defaultChartRawDataContainer.muscleMap,
                            defaultChartRawDataContainer.movementMap,
                            defaultChartRawDataContainer.movementVariantList,
                            defaultChartRawDataContainer.movementVariantMap,
                            filteredEntities,
                            true,
                            false);
                    if (chartRawDataCache != null) {
                        chartRawDataCache.put(rawDataCacheKey, chartRawData);
                    }
                } else {
                    Timber.d("Cache HIT for chart raw data, cache key: [%s], for chart id: [%s]", rawDataCacheKey, chart.id);
                }
            }
        } else {
            Timber.d("Null chart config, so using default chart raw data for chart id: [%s]", chart.id);
        }
        final HashMap<Integer, PieSliceDataTuple> pieSliceDataTuplesMap = chart.pieSliceMaker.invoke(chartRawData);
        final List<PieSliceDataTuple> pieSliceDataTuples = new ArrayList<>(pieSliceDataTuplesMap.values());
        Collections.sort(pieSliceDataTuples, (tuple1, tuple2) -> tuple1.name.compareTo(tuple2.name));
        final List<PieEntry> pieEntries = new ArrayList<>();
        final List<Integer> colors = new ArrayList<>();
        final Map<Integer, Integer> chartColors = chart.chartColorsFn.invoke(chartColorsContainer);
        for (final PieSliceDataTuple pieSliceDataTuple : pieSliceDataTuples) {
            if (pieSliceDataTuple.aggregateValue != null && pieSliceDataTuple.aggregateValue.compareTo(BigDecimal.ZERO) > 0) {
                colors.add(chartColors.get(pieSliceDataTuple.localIdentifier));
                final PieEntry dataEntry = new PieEntry(pieSliceDataTuple.aggregateValue.floatValue(), pieSliceDataTuple.name);
                pieEntries.add(dataEntry);
            }
        }
        final PieDataSet pieDataSet = new PieDataSet(pieEntries, null);
        pieDataSet.setColors(colors);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        //pieDataSet.setValueLineVariableLength(true);
        pieDataSet.setValueLinePart1Length(0.6f);
        pieDataSet.setValueLinePart2Length(0.2f);
        pieDataSet.setValueLinePart1OffsetPercentage(95.0f);
        //pieDataSet.setSliceSpace(5.0f);
        pieDataSet.setValueLineColor(R.color.colorPrimaryDark);
        //pieDataSet.setValueLineWidth(1.0f);
        final PieChartDataContainer pieChartDataContainer = new PieChartDataContainer();
        pieChartDataContainer.pieData = new PieData(pieDataSet);
        //pieChartDataContainer.pieData.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.format("%.0f%%", value));
        pieChartDataContainer.pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public final String getFormattedValue(final float value) {
                return String.format("%.0f%%", value);
            }
        });
        pieDataSet.setValueTextSize(Utils.floatValue(resources, R.dimen.pie_chart_entry_label_text_size));
        pieDataSet.setValueTextColor(R.color.colorPrimaryDark);
        pieChartDataContainer.chartConfig = chartConfig;
        pieChartDataContainer.wasConfigSet = wasConfigSet;
        pieChartDataContainer.settingsButtonIconImageName = settingsButtonIconImageName;
        pieChartDataContainer.userSettings = defaultChartRawDataContainer.userSettings;
        pieChartDataContainer.chartConfigLocalIdentifier = chartConfigLocalIdentifier;
        pieChartDataContainer.user = defaultChartRawDataContainer.user;
        pieChartDataContainer.chart = chart;
        pieChartDataContainer.category = fetchMode.category;
        final int numEntities = defaultChartRawDataContainer.entities.size();
        if (numEntities > 0) {
            pieChartDataContainer.firstEntityDate = defaultChartRawDataContainer.entities.get(0).loggedAt;
            pieChartDataContainer.lastEntityDate = defaultChartRawDataContainer.entities.get(numEntities - 1).loggedAt;
        }
        return pieChartDataContainer;
    }

    public static ChartRawData restTimeChartDataCrossSection(final List<MuscleGroup> muscleGroupList,
                                                             final Map<Integer, MuscleGroup> muscleGroupMap,
                                                             final Map<Integer, Muscle> muscleMap,
                                                             final Map<Integer, Movement> movementMap,
                                                             final List<Set> setList) {
        final ChartRawData cd = ChartRawData.makeCrossSectionChartData();
        final int numSets = setList.size();
        if (numSets > 0) {
            final Set firstSet = setList.get(0);
            final Set lastSet = setList.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
            for (final MuscleGroup muscleGroup : muscleGroupList) {
                initMuscleGroupPieData(cd.byMuscleGroup, muscleGroup);
                initMuscleGroupTimeSeriesData(cd.byMuscleGroupTimeSeries, muscleGroup);
            }
            for (int i = 0; i < numSets; i++) {
                BigDecimal timeBetweenSetsSameMov = null;
                final Set set = setList.get(i);
                Set nextSet = null;
                if (i + 1 < numSets) {
                    nextSet = setList.get(i + 1);
                }
                final Date loggedAt = set.loggedAt;
                if (nextSet != null) {
                    final Date nextSetLoggedAt = nextSet.loggedAt;
                    if (!set.ignoreTime && !nextSet.ignoreTime) {
                        final long seconds = (nextSetLoggedAt.getTime() - loggedAt.getTime()) / 1000;
                        if (seconds < Constants.SECONDS_IN_HOUR && seconds > 0) {
                            if (set.movementId.equals(nextSet.movementId)) {
                                if (Utils.equalOrBothNull(set.movementVariantId, nextSet.movementVariantId)) {
                                    timeBetweenSetsSameMov = BigDecimal.valueOf(seconds);
                                }
                            }
                        }
                    }
                }
                final Movement movement = movementMap.get(set.movementId);
                final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
                final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
                final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
                final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
                BigDecimal primaryMusclesTimeBetweenSets = null;
                BigDecimal secondaryMusclesTimeBetweenSets = null;
                if (timeBetweenSetsSameMov != null) {
                    if (secondaryMuscleIdsCount > 0) {
                        primaryMusclesTimeBetweenSets = timeBetweenSetsSameMov.multiply(PRIMARY_MUSCLE_PERCENTAGE);
                    } else {
                        primaryMusclesTimeBetweenSets = timeBetweenSetsSameMov;
                    }
                    secondaryMusclesTimeBetweenSets = timeBetweenSetsSameMov.subtract(primaryMusclesTimeBetweenSets);
                }
                BigDecimal perPrimaryMuscleTimeBetweenSets = null;
                if (primaryMusclesTimeBetweenSets != null) {
                    perPrimaryMuscleTimeBetweenSets = primaryMusclesTimeBetweenSets.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
                }
                BigDecimal perSecondaryMuscleTimeBetweenSets = null;
                if (secondaryMuscleIdsCount > 0) {
                     BigDecimal secondaryMusclesCount = BigDecimal.valueOf(secondaryMuscleIdsCount);
                    if (secondaryMusclesTimeBetweenSets != null) {
                        perSecondaryMuscleTimeBetweenSets = secondaryMusclesTimeBetweenSets.divide(secondaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
                    }
                }
                final TallyReps tallyRestTime = (localMasterIdentifier, restTimeToAdd) -> {
                    final Muscle muscle = muscleMap.get(localMasterIdentifier);
                    final MuscleGroup muscleGroup = muscleGroupMap.get(muscle.muscleGroupId);
                    final Integer muscleGroupLocalMasterIdentifier = muscleGroup.localIdentifier;
                    addTo(cd.byMuscleGroup, muscleGroupLocalMasterIdentifier, restTimeToAdd, null);
                    addTo(cd.byMuscleGroupTimeSeries, muscleGroupLocalMasterIdentifier, restTimeToAdd, loggedAt);
                };
                for (final Integer primaryMuscleId : primaryMuscleIds) {
                    tallyRestTime.invoke(primaryMuscleId, perPrimaryMuscleTimeBetweenSets);
                }
                for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                    tallyRestTime.invoke(secondaryMuscleId, perSecondaryMuscleTimeBetweenSets);
                }
            }
            for (final Set set : setList) {
                holePluggerAndPercentageCalculator(cd.byMuscleGroupTimeSeries, set.loggedAt, true, true);
            }
        }
        return cd;
    }

    public static ChartRawData restTimeLineChartStrengthRawDataForUser(final List<BodySegment> bodySegments,
                                                                       final Map<Integer, BodySegment> bodySegmentsDict,
                                                                       final List<MuscleGroup> muscleGroups,
                                                                       final Map<Integer, MuscleGroup> muscleGroupsDict,
                                                                       final List<Muscle> muscles,
                                                                       final Map<Integer, Muscle> musclesDict,
                                                                       final Map<Integer, Movement> movementsDict,
                                                                       final List<MovementVariant> movementVariants,
                                                                       final Map<Integer, MovementVariant> movementVariantsDict,
                                                                       final List<Set> sets,
                                                                       final boolean calcPercentages,
                                                                       final boolean calcAverages) {
        final ChartRawData cd = ChartRawData.makeLineChartData();
        final int numSets = sets.size();
        if (numSets > 0) {
            final Set firstSet = sets.get(0);
            final Set lastSet = sets.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
        }
        initSingleEntityTimeSeriesData(cd.timeSeries, "Total Reps");
        for (final BodySegment bodySegment : bodySegments) {
            initBodySegmentTimeSeriesData(cd.byBodySegmentTimeSeries, bodySegment);
        }
        for (final MuscleGroup muscleGroup : muscleGroups) {
            initMuscleGroupTimeSeriesData(cd.byMuscleGroupTimeSeries, muscleGroup);
            final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
            switch (bodySegmentId) {
                case UPPER_BODY:
                    initMuscleGroupTimeSeriesData(cd.byUpperBodySegmentTimeSeries, muscleGroup);
                    break;
                case LOWER_BODY:
                    initMuscleGroupTimeSeriesData(cd.byLowerBodySegmentTimeSeries, muscleGroup);
                    break;
            }
        }
        for (final Muscle muscle : muscles) {
            final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscle.muscleGroupId);
            switch (muscleGroupId) {
                case CORE:
                    initMuscleTimeSeriesData(cd.byCoreMgTimeSeries, muscle);
                    break;
                case BACK:
                    initMuscleTimeSeriesData(cd.byBackMgTimeSeries, muscle);
                    break;
                case CALF:
                    initMuscleTimeSeriesData(cd.byCalfsMgTimeSeries, muscle);
                    break;
                case HAMS:
                    initMuscleTimeSeriesData(cd.byHamstringsMgTimeSeries, muscle);
                    break;
                case CHEST:
                    initMuscleTimeSeriesData(cd.byChestMgTimeSeries, muscle);
                    break;
                case QUADS:
                    initMuscleTimeSeriesData(cd.byQuadsMgTimeSeries, muscle);
                    break;
                case BICEPS:
                    initMuscleTimeSeriesData(cd.byBicepsMgTimeSeries, muscle);
                    break;
                case GLUTES:
                    initMuscleTimeSeriesData(cd.byGlutesMgTimeSeries, muscle);
                    break;
                case HIP_ABDUCTORS:
                    initMuscleTimeSeriesData(cd.byHipAbductorsMgTimeSeries, muscle);
                    break;
                case HIP_FLEXORS:
                    initMuscleTimeSeriesData(cd.byHipFlexorsMgTimeSeries, muscle);
                    break;
                case TRICEPS:
                    initMuscleTimeSeriesData(cd.byTricepsMgTimeSeries, muscle);
                    break;
                case FOREARMS:
                    initMuscleTimeSeriesData(cd.byForearmsMgTimeSeries, muscle);
                    break;
                case SHOULDERS:
                    initMuscleTimeSeriesData(cd.byShoulderMgTimeSeries, muscle);
                    break;
            }
        }
        for (final MovementVariant movementVariant : movementVariants) {
            initMovementVariantTimeSeriesData(cd.byMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.upperBodyByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.lowerBodyByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.shoulderByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.tricepsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.bicepsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.forearmsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.chestByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.backByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.coreByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hamstringsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.quadsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.calfsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.glutesByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hipAbductorsByMovementVariantTimeSeries, movementVariant);
            initMovementVariantTimeSeriesData(cd.hipFlexorsByMovementVariantTimeSeries, movementVariant);
        }
        for (int i = 0; i < numSets; i++) {
            BigDecimal timeBetweenSetsSameMov = null;
            final Set set = sets.get(i);
            Set nextSet = null;
            if (i + 1 < numSets) {
                nextSet = sets.get(i + 1);
            }
            final Date loggedAt = set.loggedAt;
            if (nextSet != null) {
                final Date nextSetLoggedAt = nextSet.loggedAt;
                if (!set.ignoreTime && !nextSet.ignoreTime) {
                    final long seconds = (nextSetLoggedAt.getTime() - loggedAt.getTime()) / 1000;
                    if (seconds < Constants.SECONDS_IN_HOUR && seconds > 0) {
                        if (set.movementId.equals(nextSet.movementId)) {
                            if (Utils.equalOrBothNull(set.movementVariantId, nextSet.movementVariantId)) {
                                timeBetweenSetsSameMov = BigDecimal.valueOf(seconds);
                            }
                        }
                    }
                }
            }
            final Movement movement = movementsDict.get(set.movementId);
            final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
            final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
            final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
            final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
            BigDecimal primaryMusclesTimeBetweenSets = null;
            BigDecimal secondaryMusclesTimeBetweenSets = null;
            MovementVariant movementVariant = movementVariantsDict.get(MovementVariant.Id.BODY.id);
            if (set.movementVariantId != null) {
                movementVariant = movementVariantsDict.get(set.movementVariantId);
            }
            if (timeBetweenSetsSameMov != null) {
                if (secondaryMuscleIdsCount > 0) {
                    primaryMusclesTimeBetweenSets = timeBetweenSetsSameMov.multiply(PRIMARY_MUSCLE_PERCENTAGE);
                } else {
                    primaryMusclesTimeBetweenSets = timeBetweenSetsSameMov;
                }
                secondaryMusclesTimeBetweenSets = timeBetweenSetsSameMov.subtract(primaryMusclesTimeBetweenSets);
                addTo(cd.timeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, timeBetweenSetsSameMov, loggedAt);
                addTo(cd.byMovementVariantTimeSeries, movementVariant.localIdentifier, timeBetweenSetsSameMov, loggedAt);
            }
            BigDecimal perPrimaryMuscleTimeBetweenSets = null;
            if (primaryMusclesTimeBetweenSets != null) {
                perPrimaryMuscleTimeBetweenSets = primaryMusclesTimeBetweenSets.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
            }
            BigDecimal perSecondaryMuscleTimeBetweenSets = null;
            if (secondaryMuscleIdsCount > 0) {
                BigDecimal secondaryMusclesCount = BigDecimal.valueOf(secondaryMuscleIdsCount);
                if (secondaryMusclesTimeBetweenSets != null) {
                    perSecondaryMuscleTimeBetweenSets = secondaryMusclesTimeBetweenSets.divide(secondaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
                }
            }
            final MovementVariant movementVariantFinal = movementVariant;
            final TallyRestTime tallyRestTime = (muscleLocalIdentifier, restTimeToAdd) -> {
                final Muscle muscle = musclesDict.get(muscleLocalIdentifier);
                final MuscleGroup muscleGroup = muscleGroupsDict.get(muscle.muscleGroupId);
                final BodySegment bodySegment = bodySegmentsDict.get(muscleGroup.bodySegmentId);
                addTo(cd.byBodySegmentTimeSeries, bodySegment.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byMuscleGroupTimeSeries, muscleGroup.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byShoulderMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byChestMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byTricepsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byBicepsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byForearmsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byHamstringsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byQuadsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byCalfsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byGlutesMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byHipAbductorsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byHipFlexorsMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byCoreMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byBackMgTimeSeries, muscle.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byUpperBodySegmentTimeSeries, muscleGroup.localIdentifier, restTimeToAdd, loggedAt);
                addTo(cd.byLowerBodySegmentTimeSeries, muscleGroup.localIdentifier, restTimeToAdd, loggedAt);
                final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
                switch (bodySegmentId) {
                    case UPPER_BODY:
                        addTo(cd.upperBodyByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case LOWER_BODY:
                        addTo(cd.lowerBodyByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                }
                final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscleGroup.localIdentifier);
                switch (muscleGroupId) {
                    case CORE:
                        addTo(cd.coreByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case BACK:
                        addTo(cd.backByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case CALF:
                        addTo(cd.calfsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case HAMS:
                        addTo(cd.hamstringsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case CHEST:
                        addTo(cd.chestByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case QUADS:
                        addTo(cd.quadsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case BICEPS:
                        addTo(cd.bicepsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case GLUTES:
                        addTo(cd.glutesByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case HIP_ABDUCTORS:
                        addTo(cd.hipAbductorsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case HIP_FLEXORS:
                        addTo(cd.hipFlexorsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case TRICEPS:
                        addTo(cd.tricepsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case FOREARMS:
                        addTo(cd.forearmsByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                    case SHOULDERS:
                        addTo(cd.shoulderByMovementVariantTimeSeries, movementVariantFinal.localIdentifier, restTimeToAdd, loggedAt);
                        break;
                }
            };
            for (final Integer primaryMuscleId : primaryMuscleIds) {
                tallyRestTime.invoke(primaryMuscleId, perPrimaryMuscleTimeBetweenSets);
            }
            for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                tallyRestTime.invoke(secondaryMuscleId, perSecondaryMuscleTimeBetweenSets);
            }
        }
        for (final Set set : sets) {
            holePluggerAndPercentageCalculator(cd.timeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byMuscleGroupTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byUpperBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byLowerBodySegmentTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byShoulderMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBackMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byCoreMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byChestMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byTricepsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byBicepsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byForearmsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHamstringsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byQuadsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byCalfsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byGlutesMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHipAbductorsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.byHipFlexorsMgTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.upperBodyByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.lowerBodyByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.shoulderByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.backByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.tricepsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.bicepsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.forearmsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.coreByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.chestByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hamstringsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.quadsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.calfsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.glutesByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hipAbductorsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
            holePluggerAndPercentageCalculator(cd.hipFlexorsByMovementVariantTimeSeries, set.loggedAt, calcPercentages, calcAverages);
        }
        return cd;
    }

    public static ChartRawData restTimeDistChartStrengthRawDataForUser(final List<BodySegment> bodySegments,
                                                                       final Map<Integer, BodySegment> bodySegmentsDict,
                                                                       final List<MuscleGroup> muscleGroups,
                                                                       final Map<Integer, MuscleGroup> muscleGroupsDict,
                                                                       final List<Muscle> muscles,
                                                                       final Map<Integer, Muscle> musclesDict,
                                                                       final Map<Integer, Movement> movementsDict,
                                                                       final List<MovementVariant> movementVariants,
                                                                       final Map<Integer, MovementVariant> movementVariantsDict,
                                                                       final List<Set> sets) {
        final ChartRawData cd = ChartRawData.makePieChartData();
        final int numSets = sets.size();
        if (numSets > 0) {
            final Set firstSet = sets.get(0);
            final Set lastSet = sets.get(numSets - 1);
            cd.startDate = firstSet.loggedAt;
            cd.endDate = lastSet.loggedAt;
        }
        for (final BodySegment bodySegment : bodySegments) {
            initBodySegmentPieData(cd.byBodySegment, bodySegment);
        }
        for (final MuscleGroup muscleGroup : muscleGroups) {
            initMuscleGroupPieData(cd.byMuscleGroup, muscleGroup);
            final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
            switch (bodySegmentId) {
                case UPPER_BODY:
                    initMuscleGroupPieData(cd.byUpperBodySegment, muscleGroup);
                    break;
                case LOWER_BODY:
                    initMuscleGroupPieData(cd.byLowerBodySegment, muscleGroup);
                    break;
            }
        }
        for (final Muscle muscle : muscles) {
            final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscle.muscleGroupId);
            switch (muscleGroupId) {
                case CORE:
                    initMusclePieData(cd.byCoreMg, muscle);
                    break;
                case BACK:
                    initMusclePieData(cd.byBackMg, muscle);
                    break;
                case CHEST:
                    initMusclePieData(cd.byChestMg, muscle);
                    break;
                case TRICEPS:
                    initMusclePieData(cd.byTricepsMg, muscle);
                    break;
                case SHOULDERS:
                    initMusclePieData(cd.byShoulderMg, muscle);
                    break;
            }
        }
        for (final MovementVariant movementVariant : movementVariants) {
            initMovementVariantPieData(cd.byMovementVariant, movementVariant);
            initMovementVariantPieData(cd.upperBodyByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.lowerBodyByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.shoulderByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.tricepsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.bicepsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.forearmsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.chestByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.backByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.coreByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hamstringsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.quadsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.calfsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.glutesByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hipAbductorsByMovementVariant, movementVariant);
            initMovementVariantPieData(cd.hipFlexorsByMovementVariant, movementVariant);
        }
        for (int i = 0; i < numSets; i++) {
            BigDecimal timeBetweenSetsSameMov = null;
            final Set set = sets.get(i);
            Set nextSet = null;
            if (i + 1 < numSets) {
                nextSet = sets.get(i + 1);
            }
            final Date loggedAt = set.loggedAt;
            if (nextSet != null) {
                final Date nextSetLoggedAt = nextSet.loggedAt;
                if (!set.ignoreTime && !nextSet.ignoreTime) {
                    final long seconds = (nextSetLoggedAt.getTime() - loggedAt.getTime()) / 1000;
                    if (seconds < Constants.SECONDS_IN_HOUR && seconds > 0) {
                        if (set.movementId.equals(nextSet.movementId)) {
                            if (Utils.equalOrBothNull(set.movementVariantId, nextSet.movementVariantId)) {
                                timeBetweenSetsSameMov = BigDecimal.valueOf(seconds);
                            }
                        }
                    }
                }
            }
            final Movement movement = movementsDict.get(set.movementId);
            final List<Integer> primaryMuscleIds = movement.primaryMuscleIdList;
            final List<Integer> secondaryMuscleIds = movement.secondaryMuscleIdList;
            final int secondaryMuscleIdsCount = secondaryMuscleIds != null ? secondaryMuscleIds.size() : 0;
            final BigDecimal primaryMusclesCount = BigDecimal.valueOf(primaryMuscleIds.size());
            BigDecimal primaryMusclesTimeBetweenSets = null;
            BigDecimal secondaryMusclesTimeBetweenSets = null;
            MovementVariant movementVariant = movementVariantsDict.get(MovementVariant.Id.BODY.id);
            if (set.movementVariantId != null) {
                movementVariant = movementVariantsDict.get(set.movementVariantId);
            }
            if (timeBetweenSetsSameMov != null) {
                if (secondaryMuscleIdsCount > 0) {
                    primaryMusclesTimeBetweenSets = timeBetweenSetsSameMov.multiply(PRIMARY_MUSCLE_PERCENTAGE);
                } else {
                    primaryMusclesTimeBetweenSets = timeBetweenSetsSameMov;
                }
                secondaryMusclesTimeBetweenSets = timeBetweenSetsSameMov.subtract(primaryMusclesTimeBetweenSets);
            }
            BigDecimal perPrimaryMuscleTimeBetweenSets = null;
            if (primaryMusclesTimeBetweenSets != null) {
                perPrimaryMuscleTimeBetweenSets = primaryMusclesTimeBetweenSets.divide(primaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
            }
            BigDecimal perSecondaryMuscleTimeBetweenSets = null;
            if (secondaryMuscleIdsCount > 0) {
                BigDecimal secondaryMusclesCount = BigDecimal.valueOf(secondaryMuscleIdsCount);
                if (secondaryMusclesTimeBetweenSets != null) {
                    perSecondaryMuscleTimeBetweenSets = secondaryMusclesTimeBetweenSets.divide(secondaryMusclesCount, DIVIDE_SCALE, RoundingMode.HALF_UP);
                }
            }
            final MovementVariant movementVariantFinal = movementVariant;
            addTo(cd.byMovementVariant, movementVariant.localIdentifier, timeBetweenSetsSameMov, null);
            final TallyWeight tallyRestTime = (muscleLocalIdentifier, restTimeToAdd) -> {
                final Muscle muscle = musclesDict.get(muscleLocalIdentifier);
                final MuscleGroup muscleGroup = muscleGroupsDict.get(muscle.muscleGroupId);
                final BodySegment bodySegment = bodySegmentsDict.get(muscleGroup.bodySegmentId);
                addTo(cd.byBodySegment, bodySegment.localIdentifier, restTimeToAdd, null);
                addTo(cd.byMuscleGroup, muscleGroup.localIdentifier, restTimeToAdd, null);
                addTo(cd.byShoulderMg, muscle.localIdentifier, restTimeToAdd, null);
                addTo(cd.byChestMg, muscle.localIdentifier, restTimeToAdd, null);
                addTo(cd.byTricepsMg, muscle.localIdentifier, restTimeToAdd, null);
                addTo(cd.byCoreMg, muscle.localIdentifier, restTimeToAdd, null);
                addTo(cd.byBackMg, muscle.localIdentifier, restTimeToAdd, null);
                addTo(cd.byUpperBodySegment, muscleGroup.localIdentifier, restTimeToAdd, null);
                addTo(cd.byLowerBodySegment, muscleGroup.localIdentifier, restTimeToAdd, null);
                final BodySegment.Id bodySegmentId = BodySegment.Id.bodySegmentIdById(muscleGroup.bodySegmentId);
                switch (bodySegmentId) {
                    case UPPER_BODY:
                        addTo(cd.upperBodyByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case LOWER_BODY:
                        addTo(cd.lowerBodyByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                }
                final MuscleGroup.Id muscleGroupId = MuscleGroup.Id.muscleGroupIdById(muscleGroup.localIdentifier);
                switch (muscleGroupId) {
                    case CORE:
                        addTo(cd.coreByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case BACK:
                        addTo(cd.backByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case CALF:
                        addTo(cd.calfsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case HAMS:
                        addTo(cd.hamstringsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case CHEST:
                        addTo(cd.chestByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case QUADS:
                        addTo(cd.quadsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case BICEPS:
                        addTo(cd.bicepsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case GLUTES:
                        addTo(cd.glutesByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case HIP_ABDUCTORS:
                        addTo(cd.hipAbductorsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case HIP_FLEXORS:
                        addTo(cd.hipFlexorsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case TRICEPS:
                        addTo(cd.tricepsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case FOREARMS:
                        addTo(cd.forearmsByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                    case SHOULDERS:
                        addTo(cd.shoulderByMovementVariant, movementVariantFinal.localIdentifier, restTimeToAdd, null);
                        break;
                }
            };
            for (final Integer primaryMuscleId : primaryMuscleIds) {
                tallyRestTime.invoke(primaryMuscleId, perPrimaryMuscleTimeBetweenSets);
            }
            for (final Integer secondaryMuscleId : secondaryMuscleIds) {
                tallyRestTime.invoke(secondaryMuscleId, perSecondaryMuscleTimeBetweenSets);
            }
        }
        return cd;
    }

    public static ChartRawData bodyLineChartRawDataForUser(final UserSettings userSettings, final List<BodyMeasurementLog> bmls) {
        final ChartRawData cd = ChartRawData.makeBodyChartData();
        final int numBmls = bmls.size();
        if (numBmls > 0) {
            final BodyMeasurementLog firstBml = bmls.get(0);
            final BodyMeasurementLog lastBml = bmls.get(numBmls - 1);
            cd.startDate = firstBml.loggedAt;
            cd.endDate = lastBml.loggedAt;
        }
        initSingleEntityTimeSeriesData(cd.bodyWeightTimeSeries, "Body Weight");
        initSingleEntityTimeSeriesData(cd.armSizeTimeSeries, "Arm Size");
        initSingleEntityTimeSeriesData(cd.chestSizeTimeSeries, "Chest Size");
        initSingleEntityTimeSeriesData(cd.calfSizeTimeSeries, "Calf Size");
        initSingleEntityTimeSeriesData(cd.thighSizeTimeSeries, "Thigh Size");
        initSingleEntityTimeSeriesData(cd.forearmSizeTimeSeries, "Forearm Size");
        initSingleEntityTimeSeriesData(cd.waistSizeTimeSeries, "Waist Size");
        initSingleEntityTimeSeriesData(cd.neckSizeTimeSeries, "Neck Size");
        for (int i = 0; i < numBmls; i++) {
            final BodyMeasurementLog bml = bmls.get(i);
            addTo(cd.bodyWeightTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.weightValue(bml.bodyWeight, WeightUnit.weightUnitById(bml.bodyWeightUom), WeightUnit.weightUnitById(userSettings.weightUom)), bml.loggedAt);
            final SizeUnit currentSizeUom = SizeUnit.sizeUnitById(bml.sizeUom);
            final SizeUnit targetSizeUom = SizeUnit.sizeUnitById(userSettings.sizeUom);
            addTo(cd.armSizeTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.sizeValue(bml.armSize, currentSizeUom, targetSizeUom), bml.loggedAt);
            addTo(cd.chestSizeTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.sizeValue(bml.chestSize, currentSizeUom, targetSizeUom), bml.loggedAt);
            addTo(cd.calfSizeTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.sizeValue(bml.calfSize, currentSizeUom, targetSizeUom), bml.loggedAt);
            addTo(cd.thighSizeTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.sizeValue(bml.thighSize, currentSizeUom, targetSizeUom), bml.loggedAt);
            addTo(cd.forearmSizeTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.sizeValue(bml.forearmSize, currentSizeUom, targetSizeUom), bml.loggedAt);
            addTo(cd.waistSizeTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.sizeValue(bml.waistSize, currentSizeUom, targetSizeUom), bml.loggedAt);
            addTo(cd.neckSizeTimeSeries, LMID_KEY_FOR_SINGLE_VALUE_CONTAINER, Utils.sizeValue(bml.neckSize, currentSizeUom, targetSizeUom), bml.loggedAt);
        }
        for (final BodyMeasurementLog bml : bmls) {
            holePluggerAndPercentageCalculator(cd.bodyWeightTimeSeries, bml.loggedAt, false, true);
            holePluggerAndPercentageCalculator(cd.armSizeTimeSeries, bml.loggedAt, false, true);
            holePluggerAndPercentageCalculator(cd.chestSizeTimeSeries, bml.loggedAt, false, true);
            holePluggerAndPercentageCalculator(cd.calfSizeTimeSeries, bml.loggedAt, false, true);
            holePluggerAndPercentageCalculator(cd.thighSizeTimeSeries, bml.loggedAt, false, true);
            holePluggerAndPercentageCalculator(cd.forearmSizeTimeSeries, bml.loggedAt, false, true);
            holePluggerAndPercentageCalculator(cd.waistSizeTimeSeries, bml.loggedAt, false, true);
            holePluggerAndPercentageCalculator(cd.neckSizeTimeSeries, bml.loggedAt, false, true);
        }
        return cd;
    }
}
