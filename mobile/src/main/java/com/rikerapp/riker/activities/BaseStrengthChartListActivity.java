package com.rikerapp.riker.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartRawDataContainer;

import org.parceler.Parcels;

import java.util.concurrent.Executors;

import static com.rikerapp.riker.Constants.RESULTCODE_DASHBOARD_CHARTS_NEED_RELOAD;

public abstract class BaseStrengthChartListActivity extends BaseChartActivity implements LoaderManager.LoaderCallbacks {

    public static final int DIALOG_REQUESTCODE_INFO_ACK = 1;

    private View scrollUpTarget;

    public ViewGroup totalChartContainer;
    public ViewGroup bodySegmentsChartContainer;
    public ViewGroup allMgsChartContainer;
    public ViewGroup upperBodyMgsChartContainer;
    public ViewGroup shouldersChartContainer;
    public ViewGroup chestChartContainer;
    public ViewGroup backChartContainer;
    public ViewGroup bicepsChartContainer;
    public ViewGroup tricepsChartContainer;
    public ViewGroup forearmsChartContainer;
    public ViewGroup coreChartContainer;
    public ViewGroup lowerBodyMgsChartContainer;
    public ViewGroup quadsChartContainer;
    public ViewGroup hamstringsChartContainer;
    public ViewGroup calfsChartContainer;
    public ViewGroup glutesChartContainer;
    public ViewGroup hipAbductorsChartContainer;
    public ViewGroup hipFlexorsChartContainer;
    public ViewGroup allMgsMvChartContainer;
    public ViewGroup upperBodyMgsMvChartContainer;
    public ViewGroup shouldersMvChartContainer;
    public ViewGroup chestMvChartContainer;
    public ViewGroup backMvChartContainer;
    public ViewGroup bicepsMvChartContainer;
    public ViewGroup tricepsMvChartContainer;
    public ViewGroup forearmsMvChartContainer;
    public ViewGroup absMvChartContainer;
    public ViewGroup lowerBodyMgsMvChartContainer;
    public ViewGroup quadsMvChartContainer;
    public ViewGroup hamstringsMvChartContainer;
    public ViewGroup calfsMvChartContainer;
    public ViewGroup glutesMvChartContainer;
    public ViewGroup hipAbductorsMvChartContainer;
    public ViewGroup hipFlexorsMvChartContainer;

    public abstract @ColorRes int chartSectionBarColor();

    public abstract @StringRes int chartSectionHeaderTextRes();
    public abstract @StringRes int infoTextRes();

    public abstract boolean areDistCharts();

    public abstract boolean arePieCharts();

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus.getDefault().register(this);
        if (arePieCharts()) {
            setContentView(R.layout.activity_pie_charts_list);
        } else {
            setContentView(R.layout.activity_line_charts_list);
        }
        configureAppBar();
        setTitle(screenTitle());

        // quick check (yes on ui thread) to see if user has *any* sets
        final RikerApp rikerApp = (RikerApp)getApplication();
        final int numSets = rikerApp.dao.numSets(rikerApp.dao.user());
        final ScrollView scrollView = findViewById(R.id.scrollView);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Heading panel
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeHeadingPanel(numSets);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Chart section
        ////////////////////////////////////////////////////////////////////////////////////////////
        final ViewGroup chartSectionContainer = findViewById(R.id.chartSectionContainer);
        initializeChartSectionBar(chartSectionContainer,
                scrollView,
                DIALOG_REQUESTCODE_INFO_ACK,
                chartSectionHeaderTextRes(),
                infoTextRes(),
                chartSectionBarColor(),
                0,
                "dialog_fragment_total_info",
                true,
                true,
                null);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize mg / mv jump to buttons
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeMgMvJumpToButtons(scrollView);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Set chart containers
        ////////////////////////////////////////////////////////////////////////////////////////////
        totalChartContainer = findViewById(R.id.totalChartContainer);
        bodySegmentsChartContainer = findViewById( R.id.bodySegmentsChartContainer);
        allMgsChartContainer = findViewById(R.id.allMgsChartContainer);
        upperBodyMgsChartContainer = findViewById(R.id.upperBodyMgsChartContainer);
        shouldersChartContainer = findViewById(R.id.shouldersChartContainer);
        chestChartContainer = findViewById(R.id.chestChartContainer);
        backChartContainer = findViewById(R.id.backChartContainer);
        bicepsChartContainer = findViewById(R.id.bicepsChartContainer);
        tricepsChartContainer = findViewById(R.id.tricepsChartContainer);
        forearmsChartContainer = findViewById(R.id.forearmsChartContainer);
        coreChartContainer = findViewById(R.id.coreChartContainer);
        lowerBodyMgsChartContainer = findViewById(R.id.lowerBodyMgsChartContainer);
        quadsChartContainer = findViewById(R.id.quadsChartContainer);
        hamstringsChartContainer = findViewById(R.id.hamstringsChartContainer);
        calfsChartContainer = findViewById(R.id.calfsChartContainer);
        glutesChartContainer = findViewById(R.id.glutesChartContainer);
        hipAbductorsChartContainer = findViewById(R.id.hipAbductorsChartContainer);
        hipFlexorsChartContainer = findViewById(R.id.hipFlexorsChartContainer);
        allMgsMvChartContainer = findViewById(R.id.allMgsMvChartContainer);
        upperBodyMgsMvChartContainer = findViewById(R.id.upperBodyMgsMvChartContainer);
        shouldersMvChartContainer = findViewById(R.id.shouldersMvChartContainer);
        chestMvChartContainer = findViewById(R.id.chestMvChartContainer);
        backMvChartContainer = findViewById(R.id.backMvChartContainer);
        bicepsMvChartContainer = findViewById(R.id.bicepsMvChartContainer);
        tricepsMvChartContainer = findViewById(R.id.tricepsMvChartContainer);
        forearmsMvChartContainer = findViewById(R.id.forearmsMvChartContainer);
        absMvChartContainer = findViewById(R.id.coreMvChartContainer);
        lowerBodyMgsMvChartContainer = findViewById(R.id.lowerBodyMgsMvChartContainer);
        quadsMvChartContainer = findViewById(R.id.quadsMvChartContainer);
        hamstringsMvChartContainer = findViewById(R.id.hamstringsMvChartContainer);
        calfsMvChartContainer = findViewById(R.id.calfsMvChartContainer);
        glutesMvChartContainer = findViewById(R.id.glutesMvChartContainer);
        hipAbductorsMvChartContainer = findViewById(R.id.hipAbductorsMvChartContainer);
        hipFlexorsMvChartContainer = findViewById(R.id.hipFlexorsMvChartContainer);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize chart container tuples
        ////////////////////////////////////////////////////////////////////////////////////////////
        initializeChartContainerTuples();

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize charts
        ////////////////////////////////////////////////////////////////////////////////////////////
        @IdRes final int chartId = arePieCharts() ? R.id.pieChart : R.id.lineChart;
        if (areDistCharts()) {
            removeView(R.id.totalChartContainer);
            removeView(R.id.bicepsChartContainer);
            removeView(R.id.forearmsChartContainer);
            removeView(R.id.quadsChartContainer);
            removeView(R.id.hamstringsChartContainer);
            removeView(R.id.calfsChartContainer);
            removeView(R.id.glutesChartContainer);
            removeView(R.id.hipAbductorsChartContainer);
            removeView(R.id.hipFlexorsChartContainer);
        } else {
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.totalChartContainer).chart, totalChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.bicepsChartContainer).chart, bicepsChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.forearmsChartContainer).chart, forearmsChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.quadsChartContainer).chart, quadsChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.hamstringsChartContainer).chart, hamstringsChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.calfsChartContainer).chart, calfsChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.glutesChartContainer).chart, glutesChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.hipAbductorsChartContainer).chart, hipAbductorsChartContainer, numSets, chartId);
            initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.hipFlexorsChartContainer).chart, hipFlexorsChartContainer, numSets, chartId);
        }
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.bodySegmentsChartContainer).chart, bodySegmentsChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.allMgsChartContainer).chart, allMgsChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.upperBodyMgsChartContainer).chart, upperBodyMgsChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.shouldersChartContainer).chart, shouldersChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.chestChartContainer).chart, chestChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.backChartContainer).chart, backChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.tricepsChartContainer).chart, tricepsChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.coreChartContainer).chart, coreChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.lowerBodyMgsChartContainer).chart, lowerBodyMgsChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.allMgsMvChartContainer).chart, allMgsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.upperBodyMgsMvChartContainer).chart, upperBodyMgsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.shouldersMvChartContainer).chart, shouldersMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.chestMvChartContainer).chart, chestMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.backMvChartContainer).chart, backMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.bicepsMvChartContainer).chart, bicepsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.tricepsMvChartContainer).chart, tricepsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.forearmsMvChartContainer).chart, forearmsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.coreMvChartContainer).chart, absMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.lowerBodyMgsMvChartContainer).chart, lowerBodyMgsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.quadsMvChartContainer).chart, quadsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.hamstringsMvChartContainer).chart, hamstringsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.calfsMvChartContainer).chart, calfsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.glutesMvChartContainer).chart, glutesMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.hipAbductorsMvChartContainer).chart, hipAbductorsMvChartContainer, numSets, chartId);
        initializeChartView(chartContainerTuples.tupleByContainerResId(R.id.hipFlexorsMvChartContainer).chart, hipFlexorsMvChartContainer, numSets, chartId);

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

    private final void removeView(@IdRes final int viewId) {
        final View view = findViewById(viewId);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_charts, menu);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.menu_action_scroll_up) {
            final ScrollView scrollView = findViewById(R.id.scrollView);
            if (scrollUpTarget != null) {
                scrollView.smoothScrollTo(0, scrollUpTarget.getTop() - Utils.dpToPx(this, 5));
                scrollUpTarget = null;
            } else {
                scrollView.smoothScrollTo(0, 0);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public final Loader onCreateLoader(final int id, final Bundle args) { return handleOnCreateLoader(id, args, ((RikerApp)getApplication()).setCache, ((RikerApp)getApplication()).chartRawDataCache); }
    @Override public final void onLoadFinished(final Loader loader, final Object fetchData) { handleOnLoadFinished(loader, fetchData); }
    @Override public final void onLoaderReset(final Loader loader) { handleOnLoaderReset(loader); }

    public abstract @StringRes int byMuscleGroupJumpToTitle();
    public abstract @StringRes int byMuscleGroupJumpToDescription();

    public abstract @StringRes int byMovementVariantJumpToTitle();
    public abstract @StringRes int byMovementVariantJumpToDescription();

    private final void initializeMgMvJumpToButtons(final ScrollView scrollView) {
        final Resources resources = getResources();
        final Button jumpToMgButton = findViewById(R.id.jumpToMgButton);
        final Button jumpToMvButton = findViewById(R.id.jumpToMvButton);
        final View jumpToButtonsMgContainer = findViewById(R.id.jumpToButtonsMgContainer);
        final TextView jumpToMgPanelTitleTextView = jumpToButtonsMgContainer.findViewById(R.id.jumpToMgPanelTitleTextView);
        jumpToMgPanelTitleTextView.setText(Utils.fromHtml(resources.getString(byMuscleGroupJumpToTitle())));
        final TextView jumpToMgPanelInfoTextView = jumpToButtonsMgContainer.findViewById(R.id.jumpToMgPanelInfoTextView);
        jumpToMgPanelInfoTextView.setText(Utils.fromHtml(resources.getString(byMuscleGroupJumpToDescription())));
        final Function.VoidFunction assignUpTargetToMgContainer = () -> scrollUpTarget = jumpToButtonsMgContainer;
        setOnClickScrollTo(jumpToMgButton, jumpToButtonsMgContainer, scrollView);
        configureJumpToTopButton(scrollView, R.id.jumpToMgPanelTopButton, scrollView, null);
        if (areDistCharts()) {
            findViewById(R.id.jumpToBicepsButton).setVisibility(View.GONE);
            findViewById(R.id.jumpToForearmsButton).setVisibility(View.GONE);
            findViewById(R.id.jumpToQuadsButton).setVisibility(View.GONE);
            findViewById(R.id.jumpToHamstringsButton).setVisibility(View.GONE);
            findViewById(R.id.jumpToCalfsButton).setVisibility(View.GONE);
            findViewById(R.id.jumpToGlutesButton).setVisibility(View.GONE);
            findViewById(R.id.jumpToHipAbductorsButton).setVisibility(View.GONE);
            findViewById(R.id.jumpToHipFlexorsButton).setVisibility(View.GONE);
        } else {
            setOnClickScrollTo(findViewById(R.id.jumpToBicepsButton), findViewById(R.id.bicepsChartContainer), scrollView, assignUpTargetToMgContainer);
            setOnClickScrollTo(findViewById(R.id.jumpToForearmsButton), findViewById(R.id.forearmsChartContainer), scrollView, assignUpTargetToMgContainer);
            setOnClickScrollTo(findViewById(R.id.jumpToQuadsButton), findViewById(R.id.quadsChartContainer), scrollView, assignUpTargetToMgContainer);
            setOnClickScrollTo(findViewById(R.id.jumpToHamstringsButton), findViewById(R.id.hamstringsChartContainer), scrollView, assignUpTargetToMgContainer);
            setOnClickScrollTo(findViewById(R.id.jumpToCalfsButton), findViewById(R.id.calfsChartContainer), scrollView, assignUpTargetToMgContainer);
            setOnClickScrollTo(findViewById(R.id.jumpToGlutesButton), findViewById(R.id.glutesChartContainer), scrollView, assignUpTargetToMgContainer);
            setOnClickScrollTo(findViewById(R.id.jumpToHipAbductorsButton), findViewById(R.id.hipAbductorsChartContainer), scrollView, assignUpTargetToMgContainer);
            setOnClickScrollTo(findViewById(R.id.jumpToHipFlexorsButton), findViewById(R.id.hipFlexorsChartContainer), scrollView, assignUpTargetToMgContainer);
        }
        setOnClickScrollTo(findViewById(R.id.jumpToAllButton), findViewById(R.id.allMgsChartContainer), scrollView, assignUpTargetToMgContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToUpperBodyButton), findViewById(R.id.upperBodyMgsChartContainer), scrollView, assignUpTargetToMgContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToShouldersButton), findViewById(R.id.shouldersChartContainer), scrollView, assignUpTargetToMgContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToChestButton), findViewById(R.id.chestChartContainer), scrollView, assignUpTargetToMgContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToBackButton), findViewById(R.id.backChartContainer), scrollView, assignUpTargetToMgContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToTricepsButton), findViewById(R.id.tricepsChartContainer), scrollView, assignUpTargetToMgContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToCoreButton), findViewById(R.id.coreChartContainer), scrollView, assignUpTargetToMgContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToLowerBodyButton), findViewById(R.id.lowerBodyMgsChartContainer), scrollView, assignUpTargetToMgContainer);

        final View jumpToButtonsMvContainer = findViewById(R.id.jumpToButtonsMvContainer);
        final TextView jumpToMvPanelTitleTextView = jumpToButtonsMvContainer.findViewById(R.id.jumpToMvPanelTitleTextView);
        jumpToMvPanelTitleTextView.setText(Utils.fromHtml(resources.getString(byMovementVariantJumpToTitle())));
        final TextView jumpToMvPanelInfoTextView = jumpToButtonsMvContainer.findViewById(R.id.jumpToMvPanelInfoTextView);
        jumpToMvPanelInfoTextView.setText(Utils.fromHtml(resources.getString(byMovementVariantJumpToDescription())));
        setOnClickScrollTo(jumpToMvButton, jumpToButtonsMvContainer, scrollView);
        configureJumpToTopButton(scrollView, R.id.jumpToMvPanelTopButton, scrollView, null);
        final Function.VoidFunction assignUpTargetToMvContainer = () -> scrollUpTarget = jumpToButtonsMvContainer;
        setOnClickScrollTo(findViewById(R.id.jumpToAllMvButton), findViewById(R.id.allMgsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToUpperBodyMvButton), findViewById(R.id.upperBodyMgsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToShouldersMvButton), findViewById(R.id.shouldersMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToChestMvButton), findViewById(R.id.chestMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToBackMvButton), findViewById(R.id.backMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToBicepsMvButton), findViewById(R.id.bicepsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToTricepsMvButton), findViewById(R.id.tricepsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToForearmsMvButton), findViewById(R.id.forearmsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToAbsMvButton), findViewById(R.id.coreMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToLowerBodyMvButton), findViewById(R.id.lowerBodyMgsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToQuadsMvButton), findViewById(R.id.quadsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToHamstringsMvButton), findViewById(R.id.hamstringsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToCalfsMvButton), findViewById(R.id.calfsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToGlutesMvButton), findViewById(R.id.glutesMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToHipAbductorsMvButton), findViewById(R.id.hipAbductorsMvChartContainer), scrollView, assignUpTargetToMvContainer);
        setOnClickScrollTo(findViewById(R.id.jumpToHipFlexorsMvButton), findViewById(R.id.hipFlexorsMvChartContainer), scrollView, assignUpTargetToMvContainer);
    }

    public abstract String screenTitle();

    @Override
    public final @NonNull LoaderManager.LoaderCallbacks loaderCallbacks() {
        return this;
    }

    @Override
    public final ViewGroup[] chartContainers() {
        return new ViewGroup[] {
                totalChartContainer,
                bodySegmentsChartContainer,
                allMgsChartContainer,
                upperBodyMgsChartContainer,
                shouldersChartContainer,
                chestChartContainer,
                backChartContainer,
                bicepsChartContainer,
                tricepsChartContainer,
                forearmsChartContainer,
                coreChartContainer,
                lowerBodyMgsChartContainer,
                quadsChartContainer,
                hamstringsChartContainer,
                calfsChartContainer,
                glutesChartContainer,
                hipAbductorsChartContainer,
                hipFlexorsChartContainer,
                allMgsMvChartContainer,
                upperBodyMgsMvChartContainer,
                shouldersMvChartContainer,
                chestMvChartContainer,
                backMvChartContainer,
                bicepsMvChartContainer,
                tricepsMvChartContainer,
                forearmsMvChartContainer,
                absMvChartContainer,
                lowerBodyMgsMvChartContainer,
                quadsMvChartContainer,
                hamstringsMvChartContainer,
                calfsMvChartContainer,
                glutesMvChartContainer,
                hipAbductorsMvChartContainer,
                hipFlexorsMvChartContainer
        };
    }

    @Override
    public final void chartConfigSaved(final ChartConfig chartConfig, final ChartRawDataContainer chartRawDataContainer, @NonNull LoaderManager.LoaderCallbacks callback) {
        super.chartConfigSaved(chartConfig, chartRawDataContainer, callback);
        final Intent intent = new Intent();
        intent.putExtra(CommonBundleKey.ChartConfig.name(), Parcels.wrap(chartConfig));
        setResult(RESULTCODE_DASHBOARD_CHARTS_NEED_RELOAD, intent);
    }

    @Override
    public final void chartConfigCleared(final ChartConfig chartConfig, final ChartRawDataContainer chartRawDataContainer, @NonNull LoaderManager.LoaderCallbacks callback) {
        super.chartConfigCleared(chartConfig, chartRawDataContainer, callback);
        final Intent intent = new Intent();
        intent.putExtra(CommonBundleKey.ChartConfig.name(), Parcels.wrap(chartConfig));
        setResult(RESULTCODE_DASHBOARD_CHARTS_NEED_RELOAD, intent);
    }

    @Override
    public final @IdRes int containerResId(final ChartConfig chartConfig) {
        return Chart.chartByLoaderId(chartConfig.loaderId).chartListContainerResId;
    }
}
