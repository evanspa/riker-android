package com.rikerapp.riker.activities;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartDataFetchMode;

public final class AvgWeightLiftedChartsListActivity extends BaseStrengthChartListActivity {

    @Override
    public final void initializeChartContainerTuples() {
        this.chartContainerTuples = new ChartContainerTuples()
                .addTuple(totalChartContainer, R.id.totalChartContainer, Chart.AVG_WEIGHT_LIFTED_TOTAL)
                .addTuple(bodySegmentsChartContainer, R.id.bodySegmentsChartContainer, Chart.AVG_WEIGHT_LIFTED_BODY_SEGMENTS)
                .addTuple(allMgsChartContainer, R.id.allMgsChartContainer, Chart.AVG_WEIGHT_LIFTED_ALL_MGS)
                .addTuple(upperBodyMgsChartContainer, R.id.upperBodyMgsChartContainer, Chart.AVG_WEIGHT_LIFTED_UPPER_BODY)
                .addTuple(shouldersChartContainer, R.id.shouldersChartContainer, Chart.AVG_WEIGHT_LIFTED_SHOULDERS)
                .addTuple(chestChartContainer, R.id.chestChartContainer, Chart.AVG_WEIGHT_LIFTED_CHEST)
                .addTuple(backChartContainer, R.id.backChartContainer, Chart.AVG_WEIGHT_LIFTED_BACK)
                .addTuple(bicepsChartContainer, R.id.bicepsChartContainer, Chart.AVG_WEIGHT_LIFTED_BICEPS)
                .addTuple(tricepsChartContainer, R.id.tricepsChartContainer, Chart.AVG_WEIGHT_LIFTED_TRICEPS)
                .addTuple(forearmsChartContainer, R.id.forearmsChartContainer, Chart.AVG_WEIGHT_LIFTED_FOREARMS)
                .addTuple(coreChartContainer, R.id.coreChartContainer, Chart.AVG_WEIGHT_LIFTED_CORE)
                .addTuple(lowerBodyMgsChartContainer, R.id.lowerBodyMgsChartContainer, Chart.AVG_WEIGHT_LIFTED_LOWER_BODY)
                .addTuple(quadsChartContainer, R.id.quadsChartContainer, Chart.AVG_WEIGHT_LIFTED_QUADS)
                .addTuple(hamstringsChartContainer, R.id.hamstringsChartContainer, Chart.AVG_WEIGHT_LIFTED_HAMS)
                .addTuple(calfsChartContainer, R.id.calfsChartContainer, Chart.AVG_WEIGHT_LIFTED_CALFS)
                .addTuple(glutesChartContainer, R.id.glutesChartContainer, Chart.AVG_WEIGHT_LIFTED_GLUTES)
                .addTuple(hipAbductorsChartContainer, R.id.hipAbductorsChartContainer, Chart.AVG_WEIGHT_LIFTED_HIP_ABDUCTORS)
                .addTuple(hipFlexorsChartContainer, R.id.hipFlexorsChartContainer, Chart.AVG_WEIGHT_LIFTED_HIP_FLEXORS)

                .addTuple(allMgsMvChartContainer, R.id.allMgsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_ALL_MGS_MV)
                .addTuple(upperBodyMgsMvChartContainer, R.id.upperBodyMgsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_UPPER_BODY_MV)
                .addTuple(shouldersMvChartContainer, R.id.shouldersMvChartContainer, Chart.AVG_WEIGHT_LIFTED_SHOULDERS_MV)
                .addTuple(chestMvChartContainer, R.id.chestMvChartContainer, Chart.AVG_WEIGHT_LIFTED_CHEST_MV)
                .addTuple(backMvChartContainer, R.id.backMvChartContainer, Chart.AVG_WEIGHT_LIFTED_BACK_MV)
                .addTuple(bicepsMvChartContainer, R.id.bicepsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_BICEPS_MV)
                .addTuple(tricepsMvChartContainer, R.id.tricepsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_TRICEPS_MV)
                .addTuple(forearmsMvChartContainer, R.id.forearmsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_FOREARMS_MV)
                .addTuple(absMvChartContainer, R.id.coreMvChartContainer, Chart.AVG_WEIGHT_LIFTED_CORE_MV)
                .addTuple(lowerBodyMgsMvChartContainer, R.id.lowerBodyMgsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_LOWER_BODY_MV)
                .addTuple(quadsMvChartContainer, R.id.quadsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_QUADS_MV)
                .addTuple(hamstringsMvChartContainer, R.id.hamstringsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_HAMS_MV)
                .addTuple(calfsMvChartContainer, R.id.calfsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_CALFS_MV)
                .addTuple(glutesMvChartContainer, R.id.glutesMvChartContainer, Chart.AVG_WEIGHT_LIFTED_GLUTES_MV)
                .addTuple(hipAbductorsMvChartContainer, R.id.hipAbductorsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_HIP_ABDUCTORS_MV)
                .addTuple(hipFlexorsMvChartContainer, R.id.hipFlexorsMvChartContainer, Chart.AVG_WEIGHT_LIFTED_HIP_FLEXORS_MV);
    }

    @Override
    public final int[] loaderIds() {
        return new int[] {
                Chart.AVG_WEIGHT_LIFTED_TOTAL.loaderId,
                Chart.AVG_WEIGHT_LIFTED_BODY_SEGMENTS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_ALL_MGS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_UPPER_BODY.loaderId,
                Chart.AVG_WEIGHT_LIFTED_SHOULDERS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_CHEST.loaderId,
                Chart.AVG_WEIGHT_LIFTED_BACK.loaderId,
                Chart.AVG_WEIGHT_LIFTED_BICEPS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_TRICEPS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_FOREARMS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_CORE.loaderId,
                Chart.AVG_WEIGHT_LIFTED_LOWER_BODY.loaderId,
                Chart.AVG_WEIGHT_LIFTED_QUADS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_HAMS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_CALFS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_GLUTES.loaderId,
                Chart.AVG_WEIGHT_LIFTED_HIP_ABDUCTORS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_HIP_FLEXORS.loaderId,

                Chart.AVG_WEIGHT_LIFTED_ALL_MGS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_UPPER_BODY_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_SHOULDERS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_CHEST_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_BACK_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_BICEPS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_TRICEPS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_FOREARMS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_CORE_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_LOWER_BODY_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_QUADS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_HAMS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_CALFS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_GLUTES_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_HIP_ABDUCTORS_MV.loaderId,
                Chart.AVG_WEIGHT_LIFTED_HIP_FLEXORS_MV.loaderId
        };
    }

    @Override
    public final boolean areDistCharts() {
        return false;
    }

    @Override
    public final boolean arePieCharts() {
        return false;
    }

    @Override
    public final @StringRes int headingStringRes() {
        return R.string.heading_panel_weight_lifted;
    }

    @Override
    public final @StringRes int headingInfoStringRes() {
        return R.string.weight_lifted_info;
    }

    @Override
    public final @StringRes int chartSectionHeaderTextRes() {
        return R.string.chart_section_title_weight_lifted_per_set;
    }

    @Override
    public final @StringRes int infoTextRes() {
        return R.string.avg_weight_lifted_info;
    }

    @Override
    public final @ColorRes int chartSectionBarColor() {
        return R.color.weightLiftedSubSection;
    }

    @Override
    public final Function.ChartRawDataMaker newChartRawDataMaker() {
        return (userSettings, bodySegments, bodySegmentsDict, muscleGroups, muscleGroupsDict,
                muscles, musclesDict, movementsDict, movementVariants, movementVariantsDict, sets,
                calcPercentages, calcAverages) ->
                ChartUtils.weightLiftedLineChartStrengthRawDataForUser(userSettings, bodySegments,
                        bodySegmentsDict, muscleGroups, muscleGroupsDict, muscles, musclesDict,
                        movementsDict, movementVariants, movementVariantsDict, sets, calcPercentages,
                        calcAverages);
    }

    @Override
    public final ChartDataFetchMode chartDataFetchMode() {
        return ChartDataFetchMode.WEIGHT_LIFTED_LINE;
    }

    @Override
    public final ChartConfig.Category chartConfigCategory() {
        return ChartConfig.Category.WEIGHT;
    }

    @Override
    public final ChartConfig.GlobalChartId globalChartId() {
        return ChartConfig.GlobalChartId.WEIGHT_LIFTED;
    }

    @Override
    public final String screenTitle() {
        return "Avg Weight Lifted Trend";
    }

    @Override
    public final @StringRes int byMuscleGroupJumpToTitle() {
        return R.string.avg_weight_lifted_jump_to_mg_section_title;
    }

    @Override
    public final @StringRes int byMuscleGroupJumpToDescription() {
        return R.string.avg_weight_lifted_jump_to_mg_section_description;
    }

    @Override
    public final @StringRes int byMovementVariantJumpToTitle() {
        return R.string.avg_weight_lifted_jump_to_mv_section_title;
    }

    @Override
    public final @StringRes int byMovementVariantJumpToDescription() {
        return R.string.avg_weight_lifted_jump_to_mv_section_description;
    }

    @Override
    public final boolean calcPercentages() {
        return false;
    }

    @Override
    public final boolean calcAverages() {
        return true;
    }
}
