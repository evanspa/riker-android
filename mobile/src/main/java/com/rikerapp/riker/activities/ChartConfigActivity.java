package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.eventbus.AppEvent;
import com.rikerapp.riker.model.Chart;
import com.rikerapp.riker.model.ChartConfig;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.tasks.SaveAllChartConfigTask;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class ChartConfigActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    private static final SimpleDateFormat SDF = new SimpleDateFormat(Constants.DATE_FORMAT);

    // fragment tags
    private static final String TAG_RANGE_START_DATE_PICKER = "TAG_RANGE_START_DATE_PICKER";
    private static final String TAG_RANGE_END_DATE_PICKER = "TAG_RANGE_END_DATE_PICKER";

    // local state keys
    private static final String LSTATE_RANGE_START_DATE = "LSTATE_RANGE_START_DATE";
    private static final String LSTATE_RANGE_END_DATE = "LSTATE_RANGE_END_DATE";
    private static final String LSTATE_BOUNDED_END_DATE = "LSTATE_BOUNDED_END_DATE";
    private static final String LSTATE_AGGREGATE_BY = "LSTATE_AGGREGATE_BY";
    private static final String LSTATE_SUPPRESS_PIE_SLICE_LABELS = "LSTATE_SUPPRESS_PIE_SLICE_LABELS";

    public enum ActiveDateButton {
        START_DATE_BUTTON,
        END_DATE_BUTTON
    }

    private Button rangeStartDateButton;
    private Button rangeEndDateButton;

    private ActiveDateButton activeDateButton;
    private Date rangeStartDate;
    private Date rangeEndDate;
    private boolean boundedEndDate;
    private boolean suppressPieSliceLabels;
    private ChartConfig.AggregateBy aggregateBy;
    private ChartConfig.GlobalChartId globalChartId;

    public static Intent makeIntentForGlobalChartConfig(final Context context,
                                                        final User user,
                                                        final UserSettings userSettings,
                                                        final ChartConfig.GlobalChartId globalChartId,
                                                        final ChartConfig chartConfig,
                                                        final Date firstEntityDate,
                                                        final Date lastEntityDate) {
        final Intent intent = new Intent(context, ChartConfigActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.User.name(), Parcels.wrap(user));
        bundle.putParcelable(CommonBundleKey.UserSettings.name(), Parcels.wrap(userSettings));
        bundle.putParcelable(CommonBundleKey.ChartConfig.name(), Parcels.wrap(chartConfig));
        bundle.putSerializable(CommonBundleKey.FirstEntityDate.name(), firstEntityDate);
        bundle.putSerializable(CommonBundleKey.LastEntityDate.name(), lastEntityDate);
        bundle.putSerializable(CommonBundleKey.GlobalChartId.name(), globalChartId);
        intent.putExtras(bundle);
        return intent;
    }

    public static Intent makeIntentForChartConfig(final Context context,
                                                  final User user,
                                                  final UserSettings userSettings,
                                                  final ChartConfig chartConfig,
                                                  final Chart chart,
                                                  final Date firstEntityDate,
                                                  final Date lastEntityDate) {
        final Intent intent = new Intent(context, ChartConfigActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.User.name(), Parcels.wrap(user));
        bundle.putParcelable(CommonBundleKey.UserSettings.name(), Parcels.wrap(userSettings));
        bundle.putParcelable(CommonBundleKey.ChartConfig.name(), Parcels.wrap(chartConfig));
        bundle.putSerializable(CommonBundleKey.Chart.name(), chart);
        bundle.putSerializable(CommonBundleKey.FirstEntityDate.name(), firstEntityDate);
        bundle.putSerializable(CommonBundleKey.LastEntityDate.name(), lastEntityDate);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable(LSTATE_RANGE_START_DATE, this.rangeStartDate);
        outState.putSerializable(LSTATE_RANGE_END_DATE, this.rangeEndDate);
        outState.putBoolean(LSTATE_BOUNDED_END_DATE, this.boundedEndDate);
        outState.putSerializable(LSTATE_AGGREGATE_BY, this.aggregateBy);
        outState.putBoolean(LSTATE_SUPPRESS_PIE_SLICE_LABELS, this.suppressPieSliceLabels);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_chart_config);
        configureAppBar();
        logScreen(getTitle());

        final Intent intent = getIntent();
        final Date firstEntityDate = (Date)intent.getSerializableExtra(CommonBundleKey.FirstEntityDate.name());
        final Date lastEntityDate = (Date)intent.getSerializableExtra(CommonBundleKey.LastEntityDate.name());
        final Chart chart = (Chart)intent.getSerializableExtra(CommonBundleKey.Chart.name());
        final ChartConfig chartConfig = Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.ChartConfig.name()));
        this.globalChartId = (ChartConfig.GlobalChartId)intent.getSerializableExtra(CommonBundleKey.GlobalChartId.name());

        this.rangeStartDate = chartConfig.startDate;
        this.rangeEndDate = chartConfig.endDate;
        this.boundedEndDate = chartConfig.boundedEndDate;
        this.aggregateBy = chartConfig.aggregateBy;
        this.suppressPieSliceLabels = chartConfig.suppressPieSliceLabels;

        if (savedInstanceState != null) {
            this.rangeStartDate = (Date)savedInstanceState.getSerializable(LSTATE_RANGE_START_DATE);
            this.rangeEndDate = (Date)savedInstanceState.getSerializable(LSTATE_RANGE_END_DATE);
            this.boundedEndDate = savedInstanceState.getBoolean(LSTATE_BOUNDED_END_DATE);
            this.aggregateBy = (ChartConfig.AggregateBy)savedInstanceState.getSerializable(LSTATE_AGGREGATE_BY);
            this.suppressPieSliceLabels = savedInstanceState.getBoolean(LSTATE_SUPPRESS_PIE_SLICE_LABELS);
        }

        final TextView firstEntityLoggedAtTextView = findViewById(R.id.firstEntityLoggedAtTextView);
        firstEntityLoggedAtTextView.setText(String.format("Your first set was logged on %s", SDF.format(firstEntityDate)));
        final TextView lastEntityLoggedAtTextView = findViewById(R.id.lastEntityLoggedAtTextView);
        lastEntityLoggedAtTextView.setText(String.format("Your last set was logged on %s", SDF.format(lastEntityDate)));
        final SwitchCompat boundedEndDateSwitch = findViewById(R.id.boundedEndDateSwitch);
        final ViewGroup rangeEndDateContainer = findViewById(R.id.rangeEndDateContainer);
        rangeEndDateContainer.setVisibility(boundedEndDateSwitch.isChecked() ? View.VISIBLE : View.GONE);
        boundedEndDateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rangeEndDateContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            this.boundedEndDate = isChecked;
        });
        final ViewGroup aggregateByContainer = findViewById(R.id.aggregateByContainer);
        final ViewGroup suppressPieSliceLabelsContainer = findViewById(R.id.suppressPieSliceLabelsContainer);
        final SwitchCompat suppressPieSliceLablesSwitch = findViewById(R.id.suppressPieSliceLabelsSwitch);
        suppressPieSliceLablesSwitch.setChecked(this.suppressPieSliceLabels);
        suppressPieSliceLablesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.suppressPieSliceLabels = isChecked;
        });
        final AppCompatSpinner aggregateBySpinner = findViewById(R.id.aggregateBySpinner);
        aggregateBySpinner.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                ChartConfig.AggregateBy.values()));
        aggregateBySpinner.setSelection(this.aggregateBy.ordinal());
        aggregateBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public final void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                ChartConfigActivity.this.aggregateBy = ChartConfig.AggregateBy.aggregateByForOrdinal(position);
            }

            @Override public final void onNothingSelected(final AdapterView<?> parent) { /* not used */ }
        });
        final TextView chartConfigTitleTextView = findViewById(R.id.chartConfigTitleTextView);
        if (chartConfig.isGlobal) {
            chartConfigTitleTextView.setText(getResources().getString(globalChartId.configTitleResId));
        } else {
            chartConfigTitleTextView.setText(getResources().getString(chart.titleResId));
            if (chart.isPie) {
                aggregateByContainer.setVisibility(View.GONE);

            } else {
                suppressPieSliceLabelsContainer.setVisibility(View.GONE);
            }
        }
        rangeStartDateButton = findViewById(R.id.rangeStartDateButton);
        rangeStartDateButton.setOnClickListener(view -> {
            activeDateButton = ActiveDateButton.START_DATE_BUTTON;
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.rangeStartDate);
            final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show(getFragmentManager(), TAG_RANGE_START_DATE_PICKER);
        });
        this.rangeStartDateButton.setText(SDF.format(this.rangeStartDate));
        boundedEndDateSwitch.setChecked(this.boundedEndDate);
        rangeEndDateButton = findViewById(R.id.rangeEndDateButton);
        rangeEndDateButton.setOnClickListener(view -> {
            activeDateButton = ActiveDateButton.END_DATE_BUTTON;
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.rangeEndDate);
            final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show(getFragmentManager(), TAG_RANGE_END_DATE_PICKER);
        });
        this.rangeEndDateButton.setText(SDF.format(this.rangeEndDate));
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final Intent intent = getIntent();
        final ChartConfig chartConfig = Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.ChartConfig.name()));
        if (chartConfig.isGlobal) {
            getMenuInflater().inflate(R.menu.menu_global_chart_config, menu);
        } else {
            if (chartConfig.localIdentifier != null) {
                getMenuInflater().inflate(R.menu.menu_chart_config, menu);
            } else {
                getMenuInflater().inflate(R.menu.menu_new_chart_config, menu);
            }
        }
        return true;
    }

    public static final Intent resultIntent(final ChartConfig chartConfig) {
        final Intent resultIntent = new Intent();
        final Bundle resultBundle = new Bundle();
        resultBundle.putParcelable(CommonBundleKey.ChartConfig.name(), Parcels.wrap(chartConfig));
        resultIntent.putExtras(resultBundle);
        return resultIntent;
    }

    private final ChartConfig chartConfigFromForm() {
        final Intent intent = getIntent();
        final ChartConfig chartConfig = Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.ChartConfig.name()));
        chartConfig.boundedEndDate = this.boundedEndDate;
        chartConfig.aggregateBy = this.aggregateBy;
        chartConfig.startDate = this.rangeStartDate;
        chartConfig.endDate = this.rangeEndDate;
        chartConfig.isGlobal = this.globalChartId != null;
        chartConfig.suppressPieSliceLabels = this.suppressPieSliceLabels;
        return chartConfig;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        final RikerApp rikerApp = (RikerApp) getApplication();
        final Intent intent = getIntent();
        final User user = Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.User.name()));
        final ChartConfig chartConfig = chartConfigFromForm();
        if (id == R.id.menu_action_chart_config_done) {
            if (chartConfig.isGlobal) {
                Utils.displayProgressDialog(this, "Saving chart config...");
                new SaveAllChartConfigTask((RikerApp)getApplication(),
                        chartConfig,
                        Chart.chartsByCategory(chartConfig.category),
                        user).execute();
            } else {
                // just do here on ui thread
                rikerApp.dao.saveNewOrExisting(chartConfig, user);
                setResult(Constants.RESULTCODE_CHART_CONFIG_SAVED, resultIntent(chartConfig));
                finish();
            }
            return true;
        } else if (id == R.id.menu_action_chart_config_clear) {
            if (chartConfig.isGlobal) {
                rikerApp.dao.deleteChartConfigs(chartConfig.category, user);
            } else {
                rikerApp.dao.deleteChartConfig(chartConfig.chartId, user);
            }
            setResult(Constants.RESULTCODE_CHART_CONFIG_CLEARED, resultIntent(chartConfig));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onDateSet(final DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
        if (activeDateButton == ActiveDateButton.START_DATE_BUTTON) {
            this.rangeStartDate = Utils.adjustDate(this.rangeStartDate, year, monthOfYear, dayOfMonth);
            this.rangeStartDateButton.setText(SDF.format(this.rangeStartDate));
        } else {
            this.rangeEndDate = Utils.adjustDate(this.rangeEndDate, year, monthOfYear, dayOfMonth);
            this.rangeEndDateButton.setText(SDF.format(this.rangeEndDate));
        }
    }

    @Subscribe
    public final void onMessageEvent(final AppEvent.SaveAllChartConfigsCompleteEvent saveAllChartConfigsCompleteEvent) {
        Utils.dismissProgressDialog(this);
        final ChartConfig chartConfig = chartConfigFromForm();
        setResult(Constants.RESULTCODE_CHART_CONFIG_SAVED, resultIntent(chartConfig));
        finish();
    }
}
