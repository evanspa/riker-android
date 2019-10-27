package com.rikerapp.riker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;

import org.parceler.Parcels;

import java.util.concurrent.Executors;

import static com.rikerapp.riker.ChartUtils.NO_RES_ID;

public abstract class BaseChartDashboardActivity extends BaseChartActivity {

    public ViewGroup totalContainer;
    public ViewGroup avgContainer;
    public ViewGroup distContainer;
    public ViewGroup distTimeContainer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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
        final int numSets = rikerApp.dao.numSets(rikerApp.dao.user());

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize heading panel
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeHeadingPanel(numSets);

        final ScrollView scrollView = findViewById(R.id.scrollView);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Set containers
        ////////////////////////////////////////////////////////////////////////////////////////////
        totalContainer = findViewById(R.id.totalContainer);
        avgContainer = findViewById(R.id.avgContainer);
        distContainer = findViewById(R.id.distContainer);
        distTimeContainer = findViewById(R.id.distTimeContainer);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize chart container tuples
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeChartContainerTuples();

        ////////////////////////////////////////////////////////////////////////////////////////////
        // 'Total' chart section
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeTotalChartSectionBar(scrollView, totalContainer);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.totalContainer).chart, totalContainer, numSets, R.id.lineChart);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // 'Avg' chart section
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeAvgChartSectionBar(scrollView, avgContainer);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.avgContainer).chart, avgContainer, numSets, R.id.lineChart);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // 'Dist' chart section
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeDistChartSectionBar(scrollView, distContainer);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.distContainer).chart, distContainer, numSets, R.id.pieChart);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // 'Dist/Time' chart section
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeDistTimeChartSectionBar(scrollView, distTimeContainer);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.distTimeContainer).chart, distTimeContainer, numSets, R.id.lineChart);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Jump-To buttons panel
        ////////////////////////////////////////////////////////////////////////////////////////////
        final ViewGroup jumpToButtonsContainer = findViewById(R.id.jumpToButtonsDashboardContainer);
        final Button jumpToTotalButton = jumpToButtonsContainer.findViewById(R.id.jumpToTotalButton);
        final Button jumpToAvgButton = jumpToButtonsContainer.findViewById(R.id.jumpToAvgButton);
        final Button jumpToDistButton = jumpToButtonsContainer.findViewById(R.id.jumpToDistButton);
        final Button jumpToDistTimeButton = jumpToButtonsContainer.findViewById(R.id.jumpToDistTimeButton);
        setOnClickScrollTo(jumpToTotalButton, totalContainer, scrollView);
        setOnClickScrollTo(jumpToAvgButton, avgContainer, scrollView);
        setOnClickScrollTo(jumpToDistButton, distContainer, scrollView);
        setOnClickScrollTo(jumpToDistTimeButton, distTimeContainer, scrollView);

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
                //initChartLoaders();
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
                totalContainer,
                avgContainer,
                distContainer,
                distTimeContainer
        };
    }

    public abstract @ColorRes int chartSectionBarColor();

    public abstract @DrawableRes int sectionBarButtonBg();

    public abstract @StringRes int totalHeaderTextRes();
    public abstract @StringRes int totalInfoTextRes();
    public abstract Class totalChartsListClass();

    private final void initializeTotalChartSectionBar(final ScrollView scrollView, final ViewGroup totalContainer) {
        initializeChartSectionBar(totalContainer,
                scrollView,
                DIALOG_REQUESTCODE_TOTAL_INFO_ACK,
                totalHeaderTextRes(),
                totalInfoTextRes(),
                chartSectionBarColor(),
                sectionBarButtonBg(),
                "dialog_fragment_total_info",
                false,
                true,
                totalChartsListClass());
    }

    public abstract @StringRes int avgHeaderTextRes();
    public abstract @StringRes int avgInfoTextRes();
    public abstract Class avgChartsListClass();

    private final void initializeAvgChartSectionBar(final ScrollView scrollView, final ViewGroup avgContainer) {
        initializeChartSectionBar(avgContainer,
                scrollView,
                DIALOG_REQUESTCODE_AVG_INFO_ACK,
                avgHeaderTextRes(),
                avgInfoTextRes(),
                chartSectionBarColor(),
                sectionBarButtonBg(),
                "dialog_fragment_avg_info",
                false,
                false,
                avgChartsListClass());
    }

    public abstract @StringRes int distHeaderTextRes();
    public abstract @StringRes int distInfoTextRes();
    public abstract Class distChartsListClass();

    private final void initializeDistChartSectionBar(final ScrollView scrollView, final ViewGroup distContainer) {
        initializeChartSectionBar(distContainer,
                scrollView,
                DIALOG_REQUESTCODE_DIST_INFO_ACK,
                distHeaderTextRes(),
                distInfoTextRes(),
                chartSectionBarColor(),
                sectionBarButtonBg(),
                "dialog_fragment_dist_info",
                false,
                false,
                distChartsListClass());
    }

    public abstract @StringRes int distTimeHeaderTextRes();
    public abstract @StringRes int distTimeInfoTextRes();
    public abstract Class distTimeChartsListClass();

    private final void initializeDistTimeChartSectionBar(final ScrollView scrollView, final ViewGroup distTimeContainer) {
        initializeChartSectionBar(distTimeContainer,
                scrollView,
                DIALOG_REQUESTCODE_DIST_TIME_INFO_ACK,
                distTimeHeaderTextRes(),
                distTimeInfoTextRes(),
                chartSectionBarColor(),
                sectionBarButtonBg(),
                "dialog_fragment_dist_time_info",
                false,
                false,
                distTimeChartsListClass());
    }

    @Override
    public final @IdRes int containerResId(final ChartConfig chartConfig) {
        return Chart.chartByLoaderId(chartConfig.loaderId).chartDashboardContainerResId;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CHART_CONFIG:
                switch (resultCode) {
                    case Constants.RESULTCODE_DASHBOARD_CHARTS_NEED_RELOAD:
                        final ChartConfig chartConfig = Parcels.unwrap(data.getParcelableExtra(CommonBundleKey.ChartConfig.name()));
                        final Bundle args = defaultChartRawDataBundle(defaultChartRawDataContainer);
                        if (chartConfig.isGlobal) {
                            handleCategoryChartConfigSavedOrCleared(null, args, loaderCallbacks());
                        } else {
                            final Chart chart = Chart.chartByLoaderId(chartConfig.loaderId);
                            if (chart.chartDashboardContainerResId != NO_RES_ID) {
                                handleSingleChartConfigSavedOrCleared(chartConfig, null, args, loaderCallbacks(), chart.chartDashboardContainerResId);
                            }
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public final boolean calcPercentages() {
        return true;
    }

    @Override
    public final boolean calcAverages() {
        return true;
    }
}
