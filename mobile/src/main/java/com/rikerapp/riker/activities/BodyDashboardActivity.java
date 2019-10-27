package com.rikerapp.riker.activities;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Constants;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.BmlCounts;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartDataFetchMode;

import java.util.concurrent.Executors;

public final class BodyDashboardActivity extends BaseChartActivity
        implements LoaderManager.LoaderCallbacks {

    // dialog request codes
    public static final int DIALOG_REQUEST_CODE_SECTION_INFO = 298;

    @Override
    public final Function.ChartRawDataMaker newChartRawDataMaker() {
        return (userSettings, bodySegments, bodySegmentsDict, muscleGroups, muscleGroupsDict,
                muscles, musclesDict, movementsDict, movementVariants, movementVariantsDict, bmls,
                calcPercentages, calcAverages) ->
                ChartUtils.bodyLineChartRawDataForUser(userSettings, bmls);
    }

    @Override
    public final void initializeChartContainerTuples() {
        this.chartContainerTuples = new ChartContainerTuples()
                .addTuple(bodyWeightContainer, R.id.bodyWeightContainer, Chart.AVG_BODY_WEIGHT)
                .addTuple(armsContainer, R.id.armSizeContainer, Chart.AVG_ARM_SIZE)
                .addTuple(chestContainer, R.id.chestSizeContainer, Chart.AVG_CHEST_SIZE)
                .addTuple(calfsContainer, R.id.calfSizeContainer, Chart.AVG_CALF_SIZE)
                .addTuple(thighsContainer, R.id.thighSizeContainer, Chart.AVG_THIGH_SIZE)
                .addTuple(forearmsContainer, R.id.forearmSizeContainer, Chart.AVG_FOREARM_SIZE)
                .addTuple(waistContainer, R.id.waistSizeContainer, Chart.AVG_WAIST_SIZE)
                .addTuple(neckContainer, R.id.neckSizeContainer, Chart.AVG_NECK_SIZE);
    }

    @Override
    public final ChartConfig.Category chartConfigCategory() {
        return ChartConfig.Category.BODY;
    }

    @Override
    public final ChartConfig.GlobalChartId globalChartId() {
        return ChartConfig.GlobalChartId.BODY;
    }

    public ViewGroup bodyWeightContainer;
    public ViewGroup armsContainer;
    public ViewGroup chestContainer;
    public ViewGroup calfsContainer;
    public ViewGroup thighsContainer;
    public ViewGroup forearmsContainer;
    public ViewGroup waistContainer;
    public ViewGroup neckContainer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_charts_body);
        super.onCreate(savedInstanceState);
        configureAppBar();
        logScreen(getTitle());
        configureMovementSearch();
        searchMovementsEditText.setVisibility(View.GONE);
        cancelMovementSearchEditTextButton.setVisibility(View.GONE);
        configureFloatingActionsMenu();
        final View contentContainer = findViewById(R.id.contentContainer);
        contentContainer.setOnClickListener(view -> floatingActionsMenu.collapse());

        // quick check (yes on ui thread) to see if user has *any* sets
        final RikerApp rikerApp = (RikerApp)getApplication();
        final BmlCounts bmlCounts = rikerApp.dao.bmlCounts(rikerApp.dao.user());

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize heading panel
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeHeadingPanel(bmlCounts.overallCount);

        final ScrollView scrollView = findViewById(R.id.scrollView);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Set containers
        ////////////////////////////////////////////////////////////////////////////////////////////
        bodyWeightContainer = findViewById(R.id.bodyWeightContainer);
        armsContainer = findViewById(R.id.armSizeContainer);
        chestContainer = findViewById(R.id.chestSizeContainer);
        calfsContainer = findViewById(R.id.calfSizeContainer);
        thighsContainer = findViewById(R.id.thighSizeContainer);
        forearmsContainer = findViewById(R.id.forearmSizeContainer);
        waistContainer = findViewById(R.id.waistSizeContainer);
        neckContainer = findViewById(R.id.neckSizeContainer);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize chart container tuples
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeChartContainerTuples();

        ////////////////////////////////////////////////////////////////////////////////////////////
        // All chart sections
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeChartSectionBar(scrollView, bodyWeightContainer, R.string.body_weight_title, R.string.body_weight_section_info, R.color.bodyWeightSection, R.drawable.section_bar_button_bg_body_weight, true);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.bodyWeightContainer).chart, bodyWeightContainer, bmlCounts.bodyWeightCount, R.id.lineChart);

        initializeChartSectionBar(scrollView, armsContainer, R.string.arm_size_title, R.string.arm_size_section_info, R.color.armSizeSection, R.drawable.section_bar_button_bg_arm_size, false);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.armSizeContainer).chart, armsContainer, bmlCounts.armSizeCount, R.id.lineChart);

        initializeChartSectionBar(scrollView, chestContainer, R.string.chest_size_title, R.string.chest_size_section_info, R.color.chestSizeSection, R.drawable.section_bar_button_bg_chest_size, false);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.chestSizeContainer).chart, chestContainer, bmlCounts.chestSizeCount, R.id.lineChart);

        initializeChartSectionBar(scrollView, calfsContainer, R.string.calfs_size_title, R.string.calfs_size_section_info, R.color.calfSizeSection, R.drawable.section_bar_button_bg_calf_size, false);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.calfSizeContainer).chart, calfsContainer, bmlCounts.calfSizeCount, R.id.lineChart);

        initializeChartSectionBar(scrollView, thighsContainer, R.string.thigh_size_title, R.string.thigh_size_section_info, R.color.thighSizeSection, R.drawable.section_bar_button_bg_thigh_size, false);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.thighSizeContainer).chart, thighsContainer, bmlCounts.thighSizeCount, R.id.lineChart);

        initializeChartSectionBar(scrollView, forearmsContainer, R.string.forearm_size_title, R.string.forearm_size_section_info, R.color.forearmSizeSection, R.drawable.section_bar_button_bg_forearm_size, false);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.forearmSizeContainer).chart, forearmsContainer, bmlCounts.forearmSizeCount, R.id.lineChart);

        initializeChartSectionBar(scrollView, waistContainer, R.string.waist_size_title, R.string.waist_size_section_info, R.color.waistSizeSection, R.drawable.section_bar_button_bg_waist_size, false);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.waistSizeContainer).chart, waistContainer, bmlCounts.waistSizeCount, R.id.lineChart);

        initializeChartSectionBar(scrollView, neckContainer, R.string.neck_size_title, R.string.neck_size_section_info, R.color.neckSizeSection, R.drawable.section_bar_button_bg_neck_size, false);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.neckSizeContainer).chart, neckContainer, bmlCounts.neckSizeCount, R.id.lineChart);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Jump-To buttons panel
        ////////////////////////////////////////////////////////////////////////////////////////////
        final ViewGroup jumpToButtonsContainer = findViewById(R.id.jumpToButtonsContainer);
        final Button jumpToBodyWeightButton = jumpToButtonsContainer.findViewById(R.id.jumpToBodyWeightButton);
        final Button jumpToArmSizeButton = jumpToButtonsContainer.findViewById(R.id.jumpToArmSizeButton);
        final Button jumpToChestSizeButton = jumpToButtonsContainer.findViewById(R.id.jumpToChestSizeButton);
        final Button jumpToCalfSizeButton = jumpToButtonsContainer.findViewById(R.id.jumpToCalfSizeButton);
        final Button jumpToThighSizeButton = jumpToButtonsContainer.findViewById(R.id.jumpToThighSizeButton);
        final Button jumpToForearmSizeButton = jumpToButtonsContainer.findViewById(R.id.jumpToForearmsSizeButton);
        final Button jumpToWaistSizeButton = jumpToButtonsContainer.findViewById(R.id.jumpToWaistSizeButton);
        final Button jumpToNeckSizeButton = jumpToButtonsContainer.findViewById(R.id.jumpToNeckSizeButton);

        setOnClickScrollTo(jumpToBodyWeightButton, bodyWeightContainer, scrollView);
        setOnClickScrollTo(jumpToArmSizeButton, armsContainer, scrollView);
        setOnClickScrollTo(jumpToChestSizeButton, chestContainer, scrollView);
        setOnClickScrollTo(jumpToCalfSizeButton, calfsContainer, scrollView);
        setOnClickScrollTo(jumpToThighSizeButton, thighsContainer, scrollView);
        setOnClickScrollTo(jumpToForearmSizeButton, forearmsContainer, scrollView);
        setOnClickScrollTo(jumpToWaistSizeButton, waistContainer, scrollView);
        setOnClickScrollTo(jumpToNeckSizeButton, neckContainer, scrollView);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize swipe refresh layout
        ////////////////////////////////////////////////////////////////////////////////////////////
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> reloadCharts());

        // for saving chart cache in background threads
        executorService = Executors.newCachedThreadPool();

        indicateChartsUpdated();
        if (this.defaultChartRawDataContainer != null) {
            if (this.defaultChartRawDataContainer.entities.size() > 0) {
                initChartLoader(0);
            }
        } else {
            getSupportLoaderManager().initLoader(Constants.CHART_INITIAL_DATA_LOADER_ID, null, loaderCallbacks());
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_charts_dashboard, menu);
        return true;
    }

    @Override
    public final ViewGroup[] chartContainers() {
        return new ViewGroup[] {
                bodyWeightContainer,
                armsContainer,
                chestContainer,
                calfsContainer,
                thighsContainer,
                forearmsContainer,
                waistContainer,
                neckContainer
        };
    }

    @NonNull
    @Override
    public final LoaderManager.LoaderCallbacks loaderCallbacks() {
        return this;
    }

    private final void initializeChartSectionBar(final ScrollView scrollView,
                                                 final ViewGroup container,
                                                 final @StringRes int headerTextRes,
                                                 final @StringRes int infoTextRes,
                                                 final @ColorRes int chartSectionBarColor,
                                                 final @DrawableRes int sectionBarButtonBg,
                                                 final boolean suppressTopButton) {
        initializeChartSectionBar(container,
                scrollView,
                DIALOG_REQUEST_CODE_SECTION_INFO,
                headerTextRes,
                infoTextRes,
                chartSectionBarColor,
                sectionBarButtonBg,
                "dialog_fragment_section_info",
                true,
                suppressTopButton,
                null);
    }


    @Override
    public final @IdRes int containerResId(final ChartConfig chartConfig) {
        return Chart.chartByLoaderId(chartConfig.loaderId).chartListContainerResId;
    }

    @Override
    public final @StringRes int headingStringRes() {
        return R.string.heading_panel_body;
    }

    @Override
    public final @StringRes int headingInfoStringRes() {
        return R.string.body_info;
    }

    @Override
    public final boolean calcPercentages() {
        return false;
    }

    @Override
    public final boolean calcAverages() {
        return true;
    }

    @Override
    public final ChartDataFetchMode chartDataFetchMode() {
        return ChartDataFetchMode.BODY;
    }

    @Override
    public final int[] loaderIds() {
        return new int[] {
                Chart.AVG_BODY_WEIGHT.loaderId,
                Chart.AVG_ARM_SIZE.loaderId,
                Chart.AVG_CALF_SIZE.loaderId,
                Chart.AVG_CHEST_SIZE.loaderId,
                Chart.AVG_FOREARM_SIZE.loaderId,
                Chart.AVG_WAIST_SIZE.loaderId,
                Chart.AVG_NECK_SIZE.loaderId,
                Chart.AVG_THIGH_SIZE.loaderId
        };
    }

    @Override public final Loader onCreateLoader(final int id, final Bundle args) { return handleOnCreateLoader(id, args, null, null); }
    @Override public final void onLoadFinished(final Loader loader, final Object data) { handleOnLoadFinished(loader, data); }
    @Override public final void onLoaderReset(final Loader loader) { handleOnLoaderReset(loader); }
}
