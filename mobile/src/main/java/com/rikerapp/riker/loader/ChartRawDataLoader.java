package com.rikerapp.riker.loader;

import android.content.Context;

import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.ChartDataFetchMode;
import com.rikerapp.riker.model.ChartRawData;
import com.rikerapp.riker.model.ChartRawDataContainer;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.Muscle;
import com.rikerapp.riker.model.MuscleGroup;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public final class ChartRawDataLoader extends AsyncLoader {

    private final RikerDao rikerDao;
    private final ChartDataFetchMode fetchMode;
    private final boolean calcPercentages;
    private final boolean calcAverages;

    public ChartRawDataLoader(final Context context,
                              final ChartDataFetchMode fetchMode,
                              final boolean calcPercentages,
                              final boolean calcAverages,
                              final RikerDao rikerDao) {
        super(context);
        this.fetchMode = fetchMode;
        this.calcPercentages = calcPercentages;
        this.calcAverages = calcAverages;
        this.rikerDao = rikerDao;
    }

    @Override
    public final ChartRawDataContainer loadInBackground() {
        Timber.d("yo, inside ChartRawDataloader.loadInBackground, fetch mode: [%s]", fetchMode);
        final User user = rikerDao.user();
        final UserSettings userSettings = rikerDao.userSettings(user); // we always need the user settings
        List<BodySegment> bodySegmentList = null;
        Map<Integer, BodySegment> bodySegmentMap = null;
        List<MuscleGroup> muscleGroupList = null;
        Map<Integer, MuscleGroup> muscleGroupMap = null;
        List<Muscle> muscleList = null;
        Map<Integer, Muscle> muscleMap = null;
        List<Movement> movementList = null;
        Map<Integer, Movement> movementMap = null;
        List<MovementVariant> movementVariantList = null;
        Map<Integer, MovementVariant> movementVariantMap = null;
        List sets = null;
        List bmls = null;
        final ChartRawDataContainer fetchData = new ChartRawDataContainer();
        if (fetchMode == ChartDataFetchMode.BODY) {
            bmls = rikerDao.ascendingBmls(user);
            fetchData.entities = bmls;
        } else {
            bodySegmentList = rikerDao.bodySegments();
            bodySegmentMap = Utils.toMap(bodySegmentList);
            muscleGroupList = rikerDao.muscleGroups();
            muscleGroupMap = Utils.toMap(muscleGroupList);
            muscleList = rikerDao.muscles();
            muscleMap = Utils.toMap(muscleList);
            movementList = rikerDao.movements();
            movementMap = Utils.toMap(movementList);
            movementVariantList = rikerDao.movementVariants();
            movementVariantMap = Utils.toMap(movementVariantList);
            sets = rikerDao.ascendingSets(user);
            fetchData.entities = sets;
        }
        ChartRawData chartRawData = null;
        switch (fetchMode) {
            case WEIGHT_LIFTED_CROSS_SECTION:
                chartRawData = ChartUtils.weightLiftedChartDataCrossSection(
                        userSettings,
                        muscleGroupList,
                        muscleGroupMap,
                        muscleMap,
                        movementMap,
                        sets);
                break;
            case WEIGHT_LIFTED_LINE:
                chartRawData = ChartUtils.weightLiftedLineChartStrengthRawDataForUser(
                        userSettings,
                        bodySegmentList,
                        bodySegmentMap,
                        muscleGroupList,
                        muscleGroupMap,
                        muscleList,
                        muscleMap,
                        movementMap,
                        movementVariantList,
                        movementVariantMap,
                        sets,
                        calcPercentages,
                        calcAverages);
                break;
            case WEIGHT_LIFTED_PIE:
                chartRawData = ChartUtils.weightLiftedDistChartStrengthRawDataForUser(
                        userSettings,
                        bodySegmentList,
                        bodySegmentMap,
                        muscleGroupList,
                        muscleGroupMap,
                        muscleList,
                        muscleMap,
                        movementMap,
                        movementVariantList,
                        movementVariantMap,
                        sets);
                break;
            case REPS_CROSS_SECTION:
                chartRawData = ChartUtils.repsChartDataCrossSection(
                        muscleGroupList,
                        muscleGroupMap,
                        muscleMap,
                        movementMap,
                        sets);
                break;
            case REPS_LINE:
                chartRawData = ChartUtils.repsLineChartStrengthRawDataForUser(
                        bodySegmentList,
                        bodySegmentMap,
                        muscleGroupList,
                        muscleGroupMap,
                        muscleList,
                        muscleMap,
                        movementMap,
                        movementVariantList,
                        movementVariantMap,
                        sets,
                        calcPercentages,
                        calcAverages);
                break;
            case REPS_PIE:
                chartRawData = ChartUtils.repsDistChartStrengthRawDataForUser(
                        bodySegmentList,
                        bodySegmentMap,
                        muscleGroupList,
                        muscleGroupMap,
                        muscleList,
                        muscleMap,
                        movementMap,
                        movementVariantList,
                        movementVariantMap,
                        sets);
                break;
            case REST_TIME_CROSS_SECTION:
                chartRawData = ChartUtils.restTimeChartDataCrossSection(
                        muscleGroupList,
                        muscleGroupMap,
                        muscleMap,
                        movementMap,
                        sets);
                break;
            case REST_TIME_LINE:
                chartRawData = ChartUtils.restTimeLineChartStrengthRawDataForUser(
                        bodySegmentList,
                        bodySegmentMap,
                        muscleGroupList,
                        muscleGroupMap,
                        muscleList,
                        muscleMap,
                        movementMap,
                        movementVariantList,
                        movementVariantMap,
                        sets,
                        calcPercentages,
                        calcAverages);
                break;
            case REST_TIME_PIE:
                chartRawData = ChartUtils.restTimeDistChartStrengthRawDataForUser(
                        bodySegmentList,
                        bodySegmentMap,
                        muscleGroupList,
                        muscleGroupMap,
                        muscleList,
                        muscleMap,
                        movementMap,
                        movementVariantList,
                        movementVariantMap,
                        sets);
                break;
            case BODY:
                chartRawData = ChartUtils.bodyLineChartRawDataForUser(userSettings, bmls);
                break;
        }
        fetchData.user = user;
        fetchData.userSettings = userSettings;
        fetchData.bodySegmentList = bodySegmentList;
        fetchData.bodySegmentMap = bodySegmentMap;
        fetchData.muscleGroupList = muscleGroupList;
        fetchData.muscleGroupMap = muscleGroupMap;
        fetchData.muscleList = muscleList;
        fetchData.muscleMap = muscleMap;
        fetchData.movementList = movementList;
        fetchData.movementMap = movementMap;
        fetchData.movementVariantList = movementVariantList;
        fetchData.movementVariantMap = movementVariantMap;
        fetchData.chartRawData = chartRawData;
        return fetchData;
    }
}