package com.rikerapp.riker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;

import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartDataFetchMode;

public final class HomeActivity extends BaseChartDashboardActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_home);
        super.onCreate(savedInstanceState);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        final @ColorInt int colorIntWhite = ContextCompat.getColor(this, android.R.color.white);
        toggle.getDrawerArrowDrawable().setColor(colorIntWhite);
        toolbar.setTitleTextColor(colorIntWhite);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /*AppRater.app_launched(this,
                Constants.RATE_DAYS_SINCE_INSTALL,
                Constants.RATE_LAUNCHES_SINCE_INSTALL,
                Constants.RATE_DAYS_REMIND,
                Constants.RATE_LAUNCHES_REMIND);*/
        final Button workoutsButton = findViewById(R.id.workoutsButton);
        workoutsButton.setOnClickListener(view -> startActivity(new Intent(this, WorkoutsActivity.class)));
        final Button repsButton = findViewById(R.id.repsButton);
        repsButton.setOnClickListener(view -> startActivity(new Intent(this, RepsDashboardActivity.class)));
        final Button restTimeButton = findViewById(R.id.restTimeButton);
        restTimeButton.setOnClickListener(view -> startActivity(new Intent(this, RestTimeDashboardActivity.class)));
        final Button bodyCompositionButton = findViewById(R.id.bodyCompositionButton);
        bodyCompositionButton.setOnClickListener(view -> startActivity(new Intent(this, BodyDashboardActivity.class)));
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
    public final void onBackPressed() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public final boolean onNavigationItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.nav_records) {
            startActivity(new Intent(this, RecordsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_share) {
            Utils.shareRiker(this, getApplication());
        } else if (id == R.id.nav_rate) {
            rateNow();
        }
        new Handler().postDelayed(() -> {
            final DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }, 500);
        return true;
    }

    @Override public final Loader onCreateLoader(final int id, final Bundle args) { return handleOnCreateLoader(id, args, ((RikerApp)getApplication()).setCache, ((RikerApp)getApplication()).chartRawDataCache); }
    @Override public final void onLoadFinished(final Loader loader, final Object fetchData) { handleOnLoadFinished(loader, fetchData); }
    @Override public final void onLoaderReset(final Loader loader) { handleOnLoaderReset(loader); }

    @Override
    public final void initializeChartContainerTuples() {
        this.chartContainerTuples = new ChartContainerTuples()
                .addTuple(totalContainer, R.id.totalContainer, Chart.TOTAL_WEIGHT_LIFTED_ALL_MGS)
                .addTuple(avgContainer, R.id.avgContainer, Chart.AVG_WEIGHT_LIFTED_ALL_MGS)
                .addTuple(distContainer, R.id.distContainer, Chart.DIST_TOTAL_WEIGHT_LIFTED_ALL_MGS)
                .addTuple(distTimeContainer, R.id.distTimeContainer, Chart.DIST_TIME_WEIGHT_LIFTED_ALL_MGS);
    }

    @Override
    public final int[] loaderIds() {
        return new int[] {
                Chart.TOTAL_WEIGHT_LIFTED_ALL_MGS.loaderId,
                Chart.AVG_WEIGHT_LIFTED_ALL_MGS.loaderId,
                Chart.DIST_TOTAL_WEIGHT_LIFTED_ALL_MGS.loaderId,
                Chart.DIST_TIME_WEIGHT_LIFTED_ALL_MGS.loaderId
        };
    }

    @Override
    public final @NonNull LoaderManager.LoaderCallbacks loaderCallbacks() {
        return this;
    }

    @Override
    public final Function.ChartRawDataMaker newChartRawDataMaker() {
        return (userSettings, bodySegments, bodySegmentsDict, muscleGroups, muscleGroupsDict,
                muscles, musclesDict, movementsDict, movementVariants, movementVariantsDict, sets,
                calcPercentages, calcAverages) ->
                ChartUtils.weightLiftedChartDataCrossSection(userSettings,
                        muscleGroups,
                        muscleGroupsDict,
                        musclesDict,
                        movementsDict,
                        sets);
    }

    @Override
    public final ChartDataFetchMode chartDataFetchMode() {
        return ChartDataFetchMode.WEIGHT_LIFTED_CROSS_SECTION;
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
    public final @ColorRes int chartSectionBarColor() {
        return R.color.weightLiftedSubSection;
    }

    @Override public final @DrawableRes int sectionBarButtonBg() { return R.drawable.section_bar_button_bg_weight_lifted; }

    @Override public final @StringRes int totalHeaderTextRes() { return R.string.chart_section_title_weight_lifted_total; }
    @Override public final @StringRes int totalInfoTextRes() { return R.string.total_weight_lifted_info; }
    @Override public final Class totalChartsListClass() { return TotalWeightLiftedChartsListActivity.class; }

    @Override public final @StringRes int avgHeaderTextRes() { return R.string.chart_section_title_weight_lifted_per_set; }
    @Override public final @StringRes int avgInfoTextRes() { return R.string.avg_weight_lifted_info; }
    @Override public final Class avgChartsListClass() { return AvgWeightLiftedChartsListActivity.class; }

    @Override public final @StringRes int distHeaderTextRes() { return R.string.chart_section_title_weight_lifted_dist; }
    @Override public final @StringRes int distInfoTextRes() { return R.string.dist_weight_lifted_info; }
    @Override public final Class distChartsListClass() { return DistWeightLiftedChartsListActivity.class; }

    @Override public final @StringRes int distTimeHeaderTextRes() { return R.string.chart_section_title_weight_lifted_dist_time; }
    @Override public final @StringRes int distTimeInfoTextRes() { return R.string.dist_time_weight_lifted_info; }
    @Override public final Class distTimeChartsListClass() { return DistTimeWeightLiftedChartsListActivity.class; }
}
