package com.rikerapp.riker.activities;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartDataFetchMode;

public final class RepsDashboardActivity extends BaseChartDashboardActivity
        implements LoaderManager.LoaderCallbacks {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_charts_dashboard);
        super.onCreate(savedInstanceState);
    }

    @Override
    public final @StringRes int headingStringRes() {
        return R.string.heading_panel_reps;
    }

    @Override
    public final @StringRes int headingInfoStringRes() {
        return R.string.reps_info;
    }

    @Override public final Loader onCreateLoader(final int id, final Bundle args) { return handleOnCreateLoader(id, args, ((RikerApp)getApplication()).setCache, ((RikerApp)getApplication()).chartRawDataCache); }
    @Override public final void onLoadFinished(final Loader loader, final Object fetchData) { handleOnLoadFinished(loader, fetchData); }
    @Override public final void onLoaderReset(final Loader loader) { handleOnLoaderReset(loader); }

    @Override
    public final void initializeChartContainerTuples() {
        this.chartContainerTuples = new ChartContainerTuples()
                .addTuple(totalContainer, R.id.totalContainer, Chart.TOTAL_REPS_ALL_MGS)
                .addTuple(avgContainer, R.id.avgContainer, Chart.AVG_REPS_ALL_MGS)
                .addTuple(distContainer, R.id.distContainer, Chart.DIST_TOTAL_REPS_ALL_MGS)
                .addTuple(distTimeContainer, R.id.distTimeContainer, Chart.DIST_TIME_REPS_ALL_MGS);
    }

    @Override
    public final int[] loaderIds() {
        return new int[] {
                Chart.TOTAL_REPS_ALL_MGS.loaderId,
                Chart.AVG_REPS_ALL_MGS.loaderId,
                Chart.DIST_TOTAL_REPS_ALL_MGS.loaderId,
                Chart.DIST_TIME_REPS_ALL_MGS.loaderId
        };
    }

    @Override
    public final @NonNull LoaderManager.LoaderCallbacks loaderCallbacks() { return this; }

    @Override
    public final Function.ChartRawDataMaker newChartRawDataMaker() {
        return (userSettings, bodySegments, bodySegmentsDict, muscleGroups, muscleGroupsDict,
                muscles, musclesDict, movementsDict, movementVariants, movementVariantsDict, sets,
                calcPercentages, calcAverages) ->
                ChartUtils.repsChartDataCrossSection(muscleGroups,
                        muscleGroupsDict,
                        musclesDict,
                        movementsDict,
                        sets);
    }

    @Override
    public final ChartDataFetchMode chartDataFetchMode() {
        return ChartDataFetchMode.REPS_CROSS_SECTION;
    }

    @Override
    public final ChartConfig.Category chartConfigCategory() {
        return ChartConfig.Category.REPS;
    }

    @Override
    public final ChartConfig.GlobalChartId globalChartId() {
        return ChartConfig.GlobalChartId.REPS;
    }

    @Override
    public final @ColorRes int chartSectionBarColor() {
        return R.color.repsSubSection;
    }

    @Override public final @DrawableRes int sectionBarButtonBg() { return R.drawable.section_bar_button_bg_reps; }

    @Override public final @StringRes int totalHeaderTextRes() { return R.string.chart_section_title_reps_total; }
    @Override public final @StringRes int totalInfoTextRes() { return R.string.total_reps_info; }
    @Override public final Class totalChartsListClass() { return TotalRepsChartsListActivity.class; }

    @Override public final @StringRes int avgHeaderTextRes() { return R.string.chart_section_title_reps_per_set; }
    @Override public final @StringRes int avgInfoTextRes() { return R.string.avg_reps_info; }
    @Override public final Class avgChartsListClass() { return AvgRepsChartsListActivity.class; }

    @Override public final @StringRes int distHeaderTextRes() { return R.string.chart_section_title_reps_dist; }
    @Override public final @StringRes int distInfoTextRes() { return R.string.dist_reps_info; }
    @Override public final Class distChartsListClass() { return DistRepsChartsListActivity.class; }

    @Override public final @StringRes int distTimeHeaderTextRes() { return R.string.chart_section_title_reps_dist_time; }
    @Override public final @StringRes int distTimeInfoTextRes() { return R.string.dist_time_reps_info; }
    @Override public final Class distTimeChartsListClass() { return DistTimeRepsChartsListActivity.class; }
}
