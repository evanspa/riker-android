package com.rikerapp.riker.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.rikerapp.riker.ChartUtils;
import com.rikerapp.riker.Constants;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.loader.ChartRawDataLoader;
import com.rikerapp.riker.loader.LineChartLoader;
import com.rikerapp.riker.loader.LoaderArg;
import com.rikerapp.riker.loader.PieChartLoader;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartColorsContainer;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.ChartDataFetchMode;
import com.rikerapp.riker.model.ChartRawData;
import com.rikerapp.riker.model.ChartRawDataContainer;
import com.rikerapp.riker.model.LineChartDataContainer;
import com.rikerapp.riker.model.PieChartDataContainer;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;

import org.parceler.Parcels;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import timber.log.Timber;

import static com.rikerapp.riker.ChartUtils.NO_RES_ID;
import static com.rikerapp.riker.ChartUtils.suggestedAggregateBy;

public abstract class BaseChartActivity extends BaseActivity {

    // local state keys
    public static final String LSTATE_CHART_COLORS_CONTAINER = "LSTATE_CHART_COLORS_CONTAINER";

    public ChartRawDataContainer defaultChartRawDataContainer;
    public ChartColorsContainer chartColorsContainer;
    public ExecutorService executorService;
    public SwipeRefreshLayout swipeRefreshLayout;
    public ChartContainerTuples chartContainerTuples;

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        if (chartColorsContainer != null) {
            outState.putParcelable(LSTATE_CHART_COLORS_CONTAINER, Parcels.wrap(this.chartColorsContainer));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            chartColorsContainer = Parcels.unwrap(savedInstanceState.getParcelable(LSTATE_CHART_COLORS_CONTAINER));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (doChartsNeedUpdating()) {
            final RikerApp rikerApp = (RikerApp)getApplication();
            rikerApp.dao.deleteChartCache(rikerApp.dao.user());
            if (rikerApp.setCache != null) {
                rikerApp.setCache.evictAll();
            }
            if (rikerApp.chartRawDataCache != null) {
                rikerApp.chartRawDataCache.evictAll();
            }
            Timber.d("charts NEED updating");
            reloadCharts();
        } else {
            Timber.d("charts DO NOT NEED updating");
        }
    }

    public final boolean doChartsNeedUpdating() {
        final Date chartsUpdatedAt = chartsUpdatedAt();
        if (chartsUpdatedAt != null) {
            final Date entitySavedOrDeletedAt = entitySavedOrDeletedAt();
            if (entitySavedOrDeletedAt != null) {
                return chartsUpdatedAt.compareTo(entitySavedOrDeletedAt) < 0;
            } else {
                // TODO - check additional event types that should result in chart-reloading
            }
            return false;
        }
        return true;
    }

    private final ChartConfig defaultChartConfig(final ChartConfig.Category category,
                                                 final Date firstEntityDate,
                                                 final Date lastEntityDate,
                                                 final Chart chart,
                                                 final ChartConfig.GlobalChartId globalChartId,
                                                 final Integer loaderId) {
        final ChartConfig chartConfig = new ChartConfig();
        chartConfig.localIdentifier = null;
        chartConfig.startDate = firstEntityDate;
        chartConfig.endDate = lastEntityDate;
        chartConfig.suppressPieSliceLabels = false;
        chartConfig.aggregateBy = ChartUtils.suggestedAggregateBy(firstEntityDate, lastEntityDate);
        if (globalChartId != null) {
            chartConfig.isGlobal = true;
            chartConfig.chartId = globalChartId.idVal;
        } else {
            chartConfig.isGlobal = false;
            chartConfig.chartId = chart.id;
        }
        chartConfig.category = category;
        chartConfig.boundedEndDate = false;
        chartConfig.loaderId = loaderId;
        return chartConfig;
    }

    public final void handleLineChartDataContainer(final LineChartDataContainer lineChartDataContainer,
                                                   final ViewGroup container,
                                                   final int loaderId) {
        final ImageButton settingsButton = container.findViewById(R.id.settingsImageButton);
        settingsButton.setOnClickListener(view -> {
            ChartConfig chartConfig = lineChartDataContainer.chartConfig;
            if (chartConfig == null) {
                chartConfig = defaultChartConfig(lineChartDataContainer.category,
                        lineChartDataContainer.firstEntityDate,
                        lineChartDataContainer.lastEntityDate,
                        lineChartDataContainer.chart,
                        null,
                        loaderId);
            }
            final Intent intent = ChartConfigActivity.makeIntentForChartConfig(this,
                    lineChartDataContainer.user,
                    lineChartDataContainer.userSettings,
                    chartConfig,
                    lineChartDataContainer.chart,
                    lineChartDataContainer.firstEntityDate,
                    lineChartDataContainer.lastEntityDate);
            startActivityForResult(intent, REQUEST_CODE_CHART_CONFIG);
        });
        final TextView yAxisTextView = container.findViewById(R.id.yaxisTextView);
        final LineChart lineChart = container.findViewById(R.id.lineChart);
        final ViewGroup noDataToChartYetContainer = container.findViewById(R.id.noDataToChartYetContainer);
        final TextView noDataToChartTextView = noDataToChartYetContainer.findViewById(R.id.noDataToChartTextView);
        final ViewGroup lineChartContainer = container.findViewById(R.id.chartContainer);
        final ProgressBar progressBar = container.findViewById(R.id.chartProgressBar);
        if (lineChartDataContainer != null) {
            if (lineChartDataContainer.uiLineChartData != null && lineChartDataContainer.uiLineChartData.getDataSetCount() > 0) {
                lineChart.setHardwareAccelerationEnabled(true);
                lineChart.setExtraBottomOffset(10.0f);
                final Resources resources = getResources();
                lineChart.getDescription().setText("");
                final XAxis topXaxis = lineChart.getXAxis();
                topXaxis.setPosition(XAxis.XAxisPosition.TOP);
                topXaxis.setLabelCount(lineChartDataContainer.xaxisLabelCount);
                topXaxis.setAvoidFirstLastClipping(true);
                topXaxis.setValueFormatter(lineChartDataContainer.xaxisFormatter);
                topXaxis.setDrawGridLines(true);
                topXaxis.setGridDashedLine(new DashPathEffect(new float[] { 10.0f, 5.0f }, 0));
                topXaxis.setGridLineWidth(Utils.floatValue(resources, R.dimen.line_chart_grid_line_width));
                topXaxis.setCenterAxisLabels(true);
                topXaxis.setGranularityEnabled(true);
                topXaxis.setDrawLabels(true);
                topXaxis.setCenterAxisLabels(true);
                topXaxis.setGridColor(ContextCompat.getColor(this, R.color.silverSandGrey));
                topXaxis.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                final YAxis leftAxis = lineChart.getAxisLeft();
                if (lineChartDataContainer.yaxisMaximum != null) {
                    leftAxis.setAxisMaximum(lineChartDataContainer.yaxisMaximum.floatValue());
                }
                if (lineChartDataContainer.yaxisMinimum != null) {
                    leftAxis.setAxisMinimum(lineChartDataContainer.yaxisMinimum.floatValue());
                }
                leftAxis.setValueFormatter(lineChartDataContainer.yaxisValueFormatter);
                leftAxis.setDrawGridLines(true);
                leftAxis.setGridDashedLine(new DashPathEffect(new float[] { 10.0f, 5.0f }, 0));
                leftAxis.setGridLineWidth(Utils.floatValue(resources, R.dimen.line_chart_grid_line_width));
                leftAxis.setGridColor(ContextCompat.getColor(this, R.color.silverSandGrey));
                leftAxis.setDrawZeroLine(false);
                leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                leftAxis.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                leftAxis.setXOffset(Utils.floatValue(resources, R.dimen.yaxis_x_offset));
                if (lineChartDataContainer.yaxisMaximum == null || lineChartDataContainer.yaxisMaximum.compareTo(BigDecimal.ZERO) <= 0) {
                    yAxisTextView.setVisibility(View.INVISIBLE);
                } else {
                    if (lineChartDataContainer.yAxisLabelText != null) {
                        yAxisTextView.setVisibility(View.VISIBLE);
                        yAxisTextView.setText(isPortrait() ? lineChartDataContainer.yAxisLabelText.text : lineChartDataContainer.yAxisLabelText.abbrevText);
                    } else {
                        yAxisTextView.setVisibility(View.INVISIBLE);
                    }
                }
                if (progressBar != null) { progressBar.setVisibility(View.GONE); }
                final Legend legend = lineChart.getLegend();
                legend.setForm(Legend.LegendForm.LINE);
                legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
                legend.setDrawInside(false);
                legend.setXOffset(Utils.floatValue(resources, R.dimen.line_chart_legend_x_offset));
                legend.setWordWrapEnabled(true);
                legend.setTextSize(Utils.floatValue(resources, R.dimen.legend_text_size));
                settingsButton.setVisibility(View.VISIBLE);
                settingsButton.setImageResource(lineChartDataContainer.settingsButtonIconImageName);
                settingsButton.setEnabled(true);
                lineChart.getAxisRight().setEnabled(false);
                lineChart.setData(lineChartDataContainer.uiLineChartData);
                noDataToChartYetContainer.setVisibility(View.GONE);
                lineChartContainer.setVisibility(View.VISIBLE);
                lineChart.animateX(1500);
            } else {
                if (progressBar != null) { progressBar.setVisibility(View.GONE); }
                final ProgressBar noDataProgressBar = container.findViewById(R.id.noDataProgressBar);
                if (noDataProgressBar != null) { noDataProgressBar.setVisibility(View.GONE); }
                lineChartContainer.setVisibility(View.GONE);
                noDataToChartYetContainer.setVisibility(View.VISIBLE);
                if (lineChartDataContainer.wasConfigSet) {
                    noDataToChartTextView.setText("No data to chart for the configured date range.");
                    settingsButton.setVisibility(View.VISIBLE);
                    settingsButton.setImageResource(lineChartDataContainer.settingsButtonIconImageName);
                    settingsButton.setEnabled(true);
                } else {
                    noDataToChartTextView.setText("No data to chart yet.");
                    settingsButton.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (progressBar != null) { progressBar.setVisibility(View.GONE); }
            final ProgressBar noDataProgressBar = container.findViewById(R.id.noDataProgressBar);
            if (noDataProgressBar != null) { noDataProgressBar.setVisibility(View.GONE); }
            lineChartContainer.setVisibility(View.GONE);
            noDataToChartYetContainer.setVisibility(View.VISIBLE);
            noDataToChartTextView.setText("No data to chart yet.");
            settingsButton.setVisibility(View.INVISIBLE);
        }
    }

    public final void handlePieChartDataContainer(final PieChartDataContainer pieChartDataContainer,
                                                  final ViewGroup container,
                                                  final int loaderId) {
        final ImageButton settingsButton = container.findViewById(R.id.settingsImageButton);
        settingsButton.setOnClickListener(view -> {
            ChartConfig chartConfig = pieChartDataContainer.chartConfig;
            if (chartConfig == null) {
                chartConfig = defaultChartConfig(pieChartDataContainer.category,
                        pieChartDataContainer.firstEntityDate,
                        pieChartDataContainer.lastEntityDate,
                        pieChartDataContainer.chart,
                        null,
                        loaderId);
            }
            final Intent intent = ChartConfigActivity.makeIntentForChartConfig(this,
                    pieChartDataContainer.user,
                    pieChartDataContainer.userSettings,
                    chartConfig,
                    pieChartDataContainer.chart,
                    pieChartDataContainer.firstEntityDate,
                    pieChartDataContainer.lastEntityDate);
            startActivityForResult(intent, REQUEST_CODE_CHART_CONFIG);
        });
        final PieChart pieChart = container.findViewById(R.id.pieChart);
        final ViewGroup noDataToChartYetContainer = container.findViewById(R.id.noDataToChartYetContainer);
        final TextView noDataToChartTextView = noDataToChartYetContainer.findViewById(R.id.noDataToChartTextView);
        final ViewGroup pieChartContainer = container.findViewById(R.id.chartContainer);
        final ProgressBar progressBar = container.findViewById(R.id.chartProgressBar);
        if (pieChartDataContainer != null) {
            pieChart.getDescription().setText("");
            if (progressBar != null) { progressBar.setVisibility(View.GONE); }
            if (pieChartDataContainer.pieData != null) {
                pieChart.setHardwareAccelerationEnabled(true);
                final IPieDataSet pieDataSet = pieChartDataContainer.pieData.getDataSet();
                if (pieDataSet != null && pieDataSet.getEntryCount() > 0) {
                    final Resources resources = getResources();
                    final Legend legend = pieChart.getLegend();
                    legend.setForm(Legend.LegendForm.LINE);
                    legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
                    legend.setWordWrapEnabled(true);
                    legend.setTextSize(Utils.floatValue(resources, R.dimen.legend_text_size));
                    legend.setXOffset(Utils.floatValue(resources, R.dimen.pie_chart_legend_x_offset));
                    noDataToChartYetContainer.setVisibility(View.GONE);
                    pieChartContainer.setVisibility(View.VISIBLE);
                    settingsButton.setVisibility(View.VISIBLE);
                    settingsButton.setImageResource(pieChartDataContainer.settingsButtonIconImageName);
                    settingsButton.setEnabled(true);
                    pieChart.setDrawHoleEnabled(false);
                    pieChart.setUsePercentValues(true);
                    pieChart.setEntryLabelTextSize(Utils.floatValue(resources, R.dimen.pie_chart_entry_label_text_size));
                    pieChart.setEntryLabelColor(R.color.colorPrimaryDark);
                    if (pieChartDataContainer.chartConfig != null && pieChartDataContainer.chartConfig.suppressPieSliceLabels) {
                        pieChartDataContainer.pieData.setDrawValues(false);
                        pieChart.setDrawEntryLabels(false);
                    } else {
                        pieChartDataContainer.pieData.setDrawValues(true);
                        pieChart.setDrawEntryLabels(true);
                    }
                    pieChart.setData(pieChartDataContainer.pieData);
                    pieChart.animateX(1500);
                } else {
                    if (progressBar != null) { progressBar.setVisibility(View.GONE); }
                    final ProgressBar noDataProgressBar = container.findViewById(R.id.noDataProgressBar);
                    if (noDataProgressBar != null) { noDataProgressBar.setVisibility(View.GONE); }
                    pieChartContainer.setVisibility(View.GONE);
                    noDataToChartYetContainer.setVisibility(View.VISIBLE);
                    if (pieChartDataContainer.wasConfigSet) {
                        noDataToChartTextView.setText("No data to chart for the configured date range.");
                        settingsButton.setVisibility(View.VISIBLE);
                        settingsButton.setImageResource(pieChartDataContainer.settingsButtonIconImageName);
                        settingsButton.setEnabled(true);
                    } else {
                        pieChartContainer.setVisibility(View.GONE);
                        noDataToChartYetContainer.setVisibility(View.VISIBLE);
                        noDataToChartTextView.setText("No data to chart yet.");
                        settingsButton.setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                if (progressBar != null) { progressBar.setVisibility(View.GONE); }
                final ProgressBar noDataProgressBar = container.findViewById(R.id.noDataProgressBar);
                if (noDataProgressBar != null) { noDataProgressBar.setVisibility(View.GONE); }
                pieChartContainer.setVisibility(View.GONE);
                noDataToChartYetContainer.setVisibility(View.VISIBLE);
                noDataToChartTextView.setText("No data to chart yet.");
                settingsButton.setVisibility(View.INVISIBLE);
            }
        } else {
            if (progressBar != null) { progressBar.setVisibility(View.GONE); }
        }
    }

    public final void initializeHeadingPanel(final ViewGroup container,
                                             final int infoDialogRequestCode,
                                             @StringRes final int headerTextRes,
                                             @StringRes final int infoTextRes,
                                             final String dialogFragmentTag,
                                             final int totalNumEntities,
                                             final int noChartsToConfigureInfoDialogRequestCode,
                                             @StringRes final int noChartsToConfigureHeaderTextRes,
                                             @StringRes final int noChartsToConfigureInfoTextRes,
                                             final String noChartsToConfigureDialogFragmentTag) {
        final TextView headingTextView = container.findViewById(R.id.headingTextView);
        headingTextView.setText(headerTextRes);
        final Button infoButton = container.findViewById(R.id.headingInfoButton);
        final Resources resources = getResources();
        if (infoButton != null) {
            infoButton.setOnClickListener(view -> {
                final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                        infoDialogRequestCode,
                        resources.getString(headerTextRes),
                        resources.getString(infoTextRes));
                showDialog(simpleDialogFragment, dialogFragmentTag);
            });
        }
        final ImageButton chartsReloadButton = container.findViewById(R.id.chartsReloadButton);
        chartsReloadButton.setOnClickListener(view -> reloadCharts());
        if (totalNumEntities == 0) {
            configureGlobalSettingsButtonNoCharts(container,
                    noChartsToConfigureInfoDialogRequestCode,
                    noChartsToConfigureHeaderTextRes,
                    noChartsToConfigureInfoTextRes,
                    noChartsToConfigureDialogFragmentTag);
        }
    }

    public final void configureGlobalSettingsButtonNoCharts(final ViewGroup container,
                                                            final int noChartsToConfigureInfoDialogRequestCode,
                                                            @StringRes final int noChartsToConfigureHeaderTextRes,
                                                            @StringRes final int noChartsToConfigureInfoTextRes,
                                                            final String noChartsToConfigureDialogFragmentTag) {
        final Resources resources = getResources();
        final ImageButton globalSettingsButton = container.findViewById(R.id.globalSettingsImageButton);
        globalSettingsButton.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                    noChartsToConfigureInfoDialogRequestCode,
                    resources.getString(noChartsToConfigureHeaderTextRes),
                    resources.getString(noChartsToConfigureInfoTextRes));
            showDialog(simpleDialogFragment, noChartsToConfigureDialogFragmentTag);
        });
    }

    public final void configureGlobalSettingsButton(final ViewGroup container,
                                                    final User user,
                                                    final UserSettings userSettings,
                                                    final ChartConfig.Category category,
                                                    final ChartConfig.GlobalChartId globalChartId,
                                                    final Date firstEntityDate,
                                                    final Date lastEntityDate) {
        final ImageButton globalSettingsButton = container.findViewById(R.id.globalSettingsImageButton);
        globalSettingsButton.setOnClickListener(view -> {
            final RikerApp rikerApp = (RikerApp)getApplication();
            ChartConfig globalChartConfig = rikerApp.dao.chartConfig(globalChartId, user);
            if (globalChartConfig == null) {
                globalChartConfig = defaultChartConfig(category,
                        firstEntityDate,
                        lastEntityDate,
                        null,
                        globalChartId,
                        null);
            }
            final Intent intent = ChartConfigActivity.makeIntentForGlobalChartConfig(this,
                    user,
                    userSettings,
                    globalChartId,
                    globalChartConfig,
                    firstEntityDate,
                    lastEntityDate);
            startActivityForResult(intent, REQUEST_CODE_CHART_CONFIG);
        });
    }

    public final void configureJumpToTopButton(final ViewGroup container, final @IdRes int jumpButtonId, final ScrollView scrollView, final @DrawableRes Integer background) {
        final Button jumpToTopButton = container.findViewById(jumpButtonId);
        jumpToTopButton.setOnClickListener(view -> scrollView.smoothScrollTo(0, 0));
        if (background != null) {
            jumpToTopButton.setBackgroundResource(background);
        }
    }

    public final void initializeChartSectionBar(final ViewGroup container,
                                                final ScrollView scrollViewContainer,
                                                final int infoDialogRequestCode,
                                                @StringRes final int headerTextRes,
                                                @StringRes final int infoTextRes,
                                                @ColorRes final int barColor,
                                                @DrawableRes final Integer sectionBarButtonBg,
                                                final String dialogFragmentTag,
                                                final boolean suppressMoreChartsButton,
                                                final boolean suppressTopButton,
                                                final Class chartsListActivityClass) {
        final Button infoButton = container.findViewById(R.id.chartSectionBarInfoButton);
        if (suppressTopButton) {
            container.findViewById(R.id.chartSectionBarJumpToTopButton).setVisibility(View.GONE);
        } else {
            configureJumpToTopButton(container, R.id.chartSectionBarJumpToTopButton, scrollViewContainer, sectionBarButtonBg);
        }
        final Resources resources = getResources();
        final String headerText = resources.getString(headerTextRes);
        container.findViewById(R.id.chartSectionBarFlexboxLayout).setBackgroundColor(ContextCompat.getColor(this, barColor));
        ((TextView)container.findViewById(R.id.chartSectionBarTextView)).setText(headerText);
        final Button moreChartsButton = container.findViewById(R.id.chartSectionBarMoreChartsButton);
        if (suppressMoreChartsButton) {
            moreChartsButton.setVisibility(View.GONE);
        } else {
            moreChartsButton.setOnClickListener(view -> {
                final Intent intent = new Intent(this, chartsListActivityClass);
                startActivityForResult(intent, REQUEST_CODE_CHART_CONFIG);
            });
            setDrawable(sectionBarButtonBg, moreChartsButton);
        }
        infoButton.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                    infoDialogRequestCode,
                    headerText,
                    resources.getString(infoTextRes));
            showDialog(simpleDialogFragment, dialogFragmentTag);
        });
    }

    public final void initializeChartView(final Chart chart, final ViewGroup container, final int numEntities, @IdRes final int chartResId) {
        final Resources resources = getResources();
        final TextView titleTextView = container.findViewById(R.id.chartTitleTextView);
        titleTextView.setText(resources.getString(chart.titleResId));
        final TextView subTitleTextView = container.findViewById(R.id.chartSubTitleTextView);
        if (chart.subTitleResId != NO_RES_ID) {
            subTitleTextView.setText(resources.getString(chart.subTitleResId));
        } else {
            subTitleTextView.setVisibility(View.GONE);
        }
        final Button infoButton = container.findViewById(R.id.infoButton);
        infoButton.setOnClickListener(view -> {
            final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                    DIALOG_REQUESTCODE_CHART_INFO,
                    resources.getString(chart.titleResId),
                    resources.getString(chart.infoResId));
            showDialog(simpleDialogFragment, "dialog_fragment_chart_info");
        });
        final View noDataToChartYetContainer = container.findViewById(R.id.noDataToChartYetContainer);
        final View lineChartContainer = container.findViewById(R.id.chartContainer);
        final com.github.mikephil.charting.charts.Chart uiChart = container.findViewById(chartResId);
        if (numEntities == 0) {
            lineChartContainer.setVisibility(View.GONE);
            noDataToChartYetContainer.setVisibility(View.VISIBLE);
        } else {
            noDataToChartYetContainer.setVisibility(View.GONE);
            lineChartContainer.setVisibility(View.VISIBLE);
            uiChart.setNoDataText(""); // empty string because we're showing a progress bar as the loading indicator
        }
    }

    public final void handleNoData(final ViewGroup container) {
        ChartUtils.removeNoDataProgressBar(container);
        ChartUtils.removeChartProgressBar(container);
        ChartUtils.showNoDataContainer(container);
        ChartUtils.removeChartSettingsButton(container);
    }

    public final Bundle defaultChartRawDataBundle(final ChartRawDataContainer chartRawDataContainer) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(LoaderArg.DEFAULT_CHART_RAW_DATA_CONTAINER.name(), Parcels.wrap(chartRawDataContainer));
        return bundle;
    }

    public abstract @IdRes int containerResId(final ChartConfig chartConfig);

    public void chartConfigCleared(final ChartConfig chartConfig, final ChartRawDataContainer chartRawDataContainer, @NonNull LoaderManager.LoaderCallbacks callback) {
        final Bundle args = defaultChartRawDataBundle(chartRawDataContainer);
        if (chartConfig.isGlobal) {
            handleCategoryChartConfigSavedOrCleared("All chart config cleared", args, callback);
        } else {
            handleSingleChartConfigSavedOrCleared(chartConfig, "Chart config cleared", args, callback, containerResId(chartConfig));
        }
    }

    public void chartConfigSaved(final ChartConfig chartConfig, final ChartRawDataContainer chartRawDataContainer, @NonNull LoaderManager.LoaderCallbacks callback) {
        final Bundle args = defaultChartRawDataBundle(chartRawDataContainer);
        if (chartConfig.isGlobal) {
            handleCategoryChartConfigSavedOrCleared("All chart config saved", args, callback);
        } else {
            handleSingleChartConfigSavedOrCleared(chartConfig, "Chart config saved", args, callback, containerResId(chartConfig));
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("inside BaseChartActivity.onActivityResult [%d], result: [%d]", requestCode, resultCode);
        switch (requestCode) {
            case REQUEST_CODE_CHART_CONFIG:
                ChartConfig chartConfig;
                switch (resultCode) {
                    case Constants.RESULTCODE_CHART_CONFIG_SAVED:
                        chartConfig = Parcels.unwrap(data.getParcelableExtra(CommonBundleKey.ChartConfig.name()));
                        chartConfigSaved(chartConfig, defaultChartRawDataContainer, loaderCallbacks());
                        break;
                    case Constants.RESULTCODE_CHART_CONFIG_CLEARED:
                        chartConfig = Parcels.unwrap(data.getParcelableExtra(CommonBundleKey.ChartConfig.name()));
                        chartConfigCleared(chartConfig, defaultChartRawDataContainer, loaderCallbacks());
                        break;
                }
                break;
        }
    }

    public abstract @StringRes int headingStringRes();
    public abstract @StringRes int headingInfoStringRes();

    public final void initializeHeadingPanel(final int numEntities) {
        initializeHeadingPanel(findViewById(R.id.headingPanelContainer),
                DIALOG_REQUESTCODE_WEIGHT_LIFTED_INFO_ACK,
                headingStringRes(),
                headingInfoStringRes(),
                "dialog_fragment_heading_panel_info",
                numEntities,
                DIALOG_REQUESTCODE_NO_CHARTS_TO_CONFIGURE_ACK,
                R.string.no_charts_to_configure_title,
                R.string.no_charts_to_configure_message,
                "dialog_fragment_no_charts_to_configure");
    }

    public final void handleSingleChartConfigSavedOrCleared(final ChartConfig chartConfig,
                                                            final String toastText,
                                                            final Bundle loaderArgs,
                                                            @NonNull final LoaderManager.LoaderCallbacks callback,
                                                            final @IdRes int chartContainerResId) {
        final ViewGroup container = findViewById(chartContainerResId);
        if (container != null) {
            if (toastText != null) {
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
            }
            ChartUtils.showChartProgressBar(container);
            final LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.restartLoader(chartConfig.loaderId, loaderArgs, callback);
            indicateChartsUpdated();
        }
    }

    public final void handleCategoryChartConfigSavedOrCleared(final String toastText,
                                                              final Bundle loaderArgs,
                                                              @NonNull LoaderManager.LoaderCallbacks callback) {
        final LoaderManager loaderManager = getSupportLoaderManager();
        if (toastText != null) {
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
        final ViewGroup chartContainers[] = chartContainers();
        for (final ViewGroup chartContainer : chartContainers) {
            ChartUtils.showChartProgressBar(chartContainer);
        }
        final int loaderIds[] = loaderIds();
        for (final int loaderId : loaderIds) {
            loaderManager.destroyLoader(loaderId);
        }
        loaderManager.restartLoader(Constants.CHART_INITIAL_DATA_LOADER_ID, null, loaderCallbacks());
        indicateChartsUpdated();
    }

    public final void initChartLoader(final int loaderIdIndex) {
        final LoaderManager loaderManager = getSupportLoaderManager();
        final Bundle args = new Bundle();
        args.putParcelable(LoaderArg.DEFAULT_CHART_RAW_DATA_CONTAINER.name(), Parcels.wrap(defaultChartRawDataContainer));
        loaderManager.initLoader(loaderIds()[loaderIdIndex], args, loaderCallbacks());
    }

    public final void reloadCharts() {
        Toast.makeText(this, "Reloading charts", Toast.LENGTH_SHORT).show();
        final ViewGroup chartContainers[] = chartContainers();
        for (final ViewGroup chartContainer : chartContainers) {
            ChartUtils.showChartProgressBar(chartContainer);
        }
        indicateChartsUpdated();
        final LoaderManager loaderManager = getSupportLoaderManager();
        final int loaderIds[] = loaderIds();
        for (final int loaderId : loaderIds) {
            loaderManager.destroyLoader(loaderId);
        }
        loaderManager.restartLoader(Constants.CHART_INITIAL_DATA_LOADER_ID, null, loaderCallbacks());
    }

    public final void allChartsHandleNoData() {
        final ViewGroup chartContainers[] = chartContainers();
        for (final ViewGroup chartContainer : chartContainers) {
            handleNoData(chartContainer);
        }
    }

    public abstract boolean calcPercentages();
    public abstract boolean calcAverages();

    public final Loader newChartLoader(final Chart chart,
                                       final Bundle args,
                                       final LruCache<String, List> entitiesCache,
                                       final LruCache<String, ChartRawData> chartRawDataCache) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        if (chart.isPie) {
            return new PieChartLoader(this,
                    chart,
                    Parcels.unwrap(args.getParcelable(LoaderArg.DEFAULT_CHART_RAW_DATA_CONTAINER.name())),
                    rikerApp.dao,
                    newChartRawDataMaker(),
                    chartDataFetchMode(),
                    entitiesCache,
                    chartRawDataCache,
                    this.chartColorsContainer,
                    executorService);
        } else {
            return new LineChartLoader(this,
                    chart,
                    Parcels.unwrap(args.getParcelable(LoaderArg.DEFAULT_CHART_RAW_DATA_CONTAINER.name())),
                    rikerApp.dao,
                    newChartRawDataMaker(),
                    calcPercentages(),
                    calcAverages(),
                    chartDataFetchMode(),
                    entitiesCache,
                    chartRawDataCache,
                    suggestedAggregateBy(this.defaultChartRawDataContainer),
                    this.chartColorsContainer,
                    executorService);
        }
    }

    public final Loader handleOnCreateLoader(final int id, final Bundle args, final LruCache<String, List> entitiesCache, final LruCache<String, ChartRawData> chartRawDataCache) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        if (id == Constants.CHART_INITIAL_DATA_LOADER_ID) {
            return new ChartRawDataLoader(this, chartDataFetchMode(), calcPercentages(), calcAverages(), rikerApp.dao);
        } else {
            return newChartLoader(Chart.chartByLoaderId(id), args, entitiesCache, chartRawDataCache);
        }
    }

    private final Integer nextLoaderIndex(final int loaderId) {
        final int[] loaderIds = loaderIds();
        for (int i = 0; i < loaderIds.length; i++) {
            if (loaderId == loaderIds[i]) {
                if (i + 1 < loaderIds.length) {
                    return i + 1;
                }
                break;
            }
        }
        return null;
    }

    public final void handleOnLoadFinished(final Loader loader, final Object fetchData) {
        final int loaderId = loader.getId();
        if (loaderId == Constants.CHART_INITIAL_DATA_LOADER_ID) {
            swipeRefreshLayout.setRefreshing(false);
            this.defaultChartRawDataContainer = (ChartRawDataContainer)fetchData;
            final int numEntities = this.defaultChartRawDataContainer.entities.size();
            if (numEntities > 0) {
                chartColorsContainer = ChartUtils.chartColorsContainer(this, getResources(), defaultChartRawDataContainer.muscleList);
                configureGlobalSettingsButton(findViewById(R.id.headingPanelContainer),
                        defaultChartRawDataContainer.user,
                        defaultChartRawDataContainer.userSettings,
                        chartConfigCategory(),
                        globalChartId(),
                        this.defaultChartRawDataContainer.entities.get(0).loggedAt,
                        this.defaultChartRawDataContainer.entities.get(numEntities - 1).loggedAt);
                initChartLoader(0);
            } else {
                configureGlobalSettingsButtonNoCharts(
                        findViewById(R.id.headingPanelContainer),
                        DIALOG_REQUESTCODE_NO_CHARTS_TO_CONFIGURE_ACK,
                        R.string.no_charts_to_configure_title,
                        R.string.no_charts_to_configure_message,
                        "dialog_fragment_no_charts_to_configure");
                // right now, as the code is written, we're assuming that the 'no data' views
                // are currently displaying, and so we only need here to remove the progress bar
                // that is currently hovering over the no-data views.  This probably is not good
                // enough - i.e., it's possible that at Time T0, we had data and were displaying
                // charts, but, at Time T1, the user decided to delete all their data, and so
                // now were here...so we need to un-hide the no-data views (that are currently hidden)
                allChartsHandleNoData();
            }
        } else {
            final Integer nextLoaderIndex = nextLoaderIndex(loaderId);
            if (nextLoaderIndex != null) {
                initChartLoader(nextLoaderIndex);
            }
            final ChartContainerTuple chartContainerTuple = chartContainerTuples.tupleByLoaderId(loaderId);
            if (chartContainerTuple.chart.isPie) {
                handlePieChartDataContainer((PieChartDataContainer)fetchData, chartContainerTuple.container, loaderId);
            } else {
                handleLineChartDataContainer((LineChartDataContainer)fetchData, chartContainerTuple.container, loaderId);
            }
        }
    }

    public final void handleOnLoaderReset(final Loader loader) {
        this.defaultChartRawDataContainer = null;
    }

    public abstract ChartDataFetchMode chartDataFetchMode();

    public abstract int[] loaderIds();

    public abstract ViewGroup[] chartContainers();

    public abstract  @NonNull LoaderManager.LoaderCallbacks loaderCallbacks();

    public abstract Function.ChartRawDataMaker newChartRawDataMaker();

    public static final class ChartContainerTuple {
        public final ViewGroup container;
        public final Chart chart;

        public ChartContainerTuple(final ViewGroup container, final Chart chart) {
            this.container = container;
            this.chart = chart;
        }
    }

    public static final class ChartContainerTuples {
        private final Map<Integer, ChartContainerTuple> tuplesByLoaderId;
        private final Map<Integer, ChartContainerTuple> tuplesByContainerResId;

        public ChartContainerTuples() {
            this.tuplesByLoaderId = new HashMap<>();
            this.tuplesByContainerResId = new HashMap<>();
        }

        public final ChartContainerTuples addTuple(final ViewGroup container,
                                                   final @IdRes int containerResId,
                                                   final Chart chart) {
            final ChartContainerTuple chartContainerTuple = new ChartContainerTuple(container, chart);
            tuplesByLoaderId.put(chart.loaderId, chartContainerTuple);
            tuplesByContainerResId.put(containerResId, chartContainerTuple);
            return this;
        }

        public final ChartContainerTuple tupleByLoaderId(final int loaderId) {
            return tuplesByLoaderId.get(loaderId);
        }

        public final ChartContainerTuple tupleByContainerResId(final @IdRes int containerResId) {
            return tuplesByContainerResId.get(containerResId);
        }
    }

    public final void setOnClickScrollTo(final Button jumpToButton, final View destinationView, final ScrollView scrollView) {
        setOnClickScrollTo(jumpToButton, destinationView, scrollView, null);
    }

    public final void setOnClickScrollTo(final Button jumpToButton, final View destinationView, final ScrollView scrollView, final Function.VoidFunction hook) {
        jumpToButton.setOnClickListener(view -> {
            scrollView.smoothScrollTo(0,destinationView.getTop() - Utils.dpToPx(this, 5));
            if (hook != null) {
                hook.invoke();
            }
        });
    }

    public abstract void initializeChartContainerTuples();

    public abstract ChartConfig.Category chartConfigCategory();

    public abstract ChartConfig.GlobalChartId globalChartId();
}
