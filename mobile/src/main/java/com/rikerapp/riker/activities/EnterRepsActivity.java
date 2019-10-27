package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.rikerapp.riker.AnalyticsEvent;
import com.rikerapp.riker.Constants;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.WeightUnit;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnterRepsActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, SimpleDialogFragment.Callbacks {

    // fragment tags
    private static final String TAG_DATE_PICKER = "TAG_DATE_PICKER";
    private static final String TAG_TIME_PICKER = "TAG_TIME_PICKER";

    // local state keys
    private static final String LSTATE_UNITS_CONTENT_EXPANDED = "LSTATE_UNITS_CONTENT_EXPANDED";
    private static final String LSTATE_MORE_CONTENT_EXPANDED = "LSTATE_MORE_CONTENT_EXPANDED";
    private static final String LSTATE_WEIGHT_UNIT = "LSTATE_WEIGHT_UNIT";
    private static final String LSTATE_REALTIME_CONTENT_EXPANDED = "LSTATE_REALTIME_CONTENT_EXPANDED";
    private static final String LSTATE_MANUAL_DATE = "LSTATE_MANUAL_DATE";
    private static final String LSTATE_HAVE_SHOWN_DEFAULTED_WEIGHTTF_DIALOG = "LSTATE_HAVE_SHOWN_DEFAULTED_WEIGHTTF_DIALOG";

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_SET_SAVED_SUCCESS = 1;
    private static final int DIALOG_REQUESTCODE_TO_FAILURE_INFO = 2;
    private static final int DIALOG_REQUESTCODE_NEGATIVES_INFO = 3;
    private static final int DIALOG_REQUESTCODE_REAL_TIME_INFO = 4;
    private static final int DIALOG_REQUESTCODE_IGNORE_TIME_INFO = 5;
    private static final int DIALOG_REQUESTCODE_SUPPRESS_WEIGHTTF_DEFAULTED_TO_BODY_WEIGHT_POPUP = 6;

    private static final int LONG_PRESS_INITIAL_DELAY = 500;
    private static final int LONG_PRESS_DELAY = 100;

    private TextView hyphensTextView;
    private TextView nextSetButton;
    private EditText weightEditText;
    private EditText repsEditText;
    private boolean didIncDecViaLongPress;
    private Handler incrementWeightHandler;
    private Handler decrementWeightHandler;
    private BigDecimal weightIncDecAmount;
    private Handler incrementRepsHandler;
    private Handler decrementRepsHandler;
    private WeightUnit weightUnit;
    private Date manualDate;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private SwitchCompat ignoreTimeSwitch;
    private Button changeDateButton;
    private Button changeTimeButton;
    private FlexboxLayout setsFlexboxLayout;
    private Map<Integer, Movement> allMovements;
    private Map<Integer, MovementVariant> allMovementVariants;
    private boolean unitsContentExpanded;
    private boolean moreContentExpanded;
    private boolean realTimeContentExpanded;
    private boolean haveShownDefaultedWeightTfDialog;
    private Map<Integer, View> setButtonViewsMap = new HashMap<>();

    private Set savedSet;
    public Map<String, List<Set>> setsMap;

    public static Intent makeIntent(final Context context,
                                    final Movement movement,
                                    final MovementVariant movementVariant,
                                    final Map<String, List<Set>> setsMap) {
        final Intent intent = new Intent(context, EnterRepsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Movement.name(), Parcels.wrap(movement));
        bundle.putParcelable(CommonBundleKey.SetsMap.name(), Parcels.wrap(setsMap));
        if (movementVariant != null) {
            bundle.putParcelable(CommonBundleKey.MovementVariant.name(), Parcels.wrap(movementVariant));
        }
        intent.putExtras(bundle);
        return intent;
    }

    private final Movement selectedMovement() {
        return Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Movement.name()));
    }

    private final MovementVariant selectedMovementVariant() {
        return Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.MovementVariant.name()));
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Codes.RESULT_CODE_ENTITY_DELETED:
                handleDeletedSet(data);
                break;
            case Codes.RESULT_CODE_ENTITY_UPDATED:
                handleUpdatedSet(data);
                break;
        }
    }

    private final void handleUpdatedSet(final Intent intent) {
        final Set updatedSet = Parcels.unwrap(intent.getParcelableExtra(CommonBundleKey.Set.name()));
        final List<Set> sets = setsMap.get(Utils.setKey(selectedMovement(), selectedMovementVariant()));
        final int numSets = sets.size();
        for (int i = 0; i < numSets; i++) {
            if (sets.get(i).localIdentifier.equals(updatedSet.localIdentifier)) {
                sets.remove(i);
                sets.add(i, updatedSet);
                break;
            }
        }
        updateSetButtonClickListeners(sets);
    }

    private final void handleDeletedSet(final Intent intent) {
        final Integer deletedSetId = intent.getIntExtra(CommonBundleKey.SetId.name(), -1);
        final List<Set> sets = setsMap.get(Utils.setKey(selectedMovement(), selectedMovementVariant()));
        final int numSets = sets.size();
        for (int i = 0; i < numSets; i++) {
            if (sets.get(i).localIdentifier.equals(deletedSetId)) {
                sets.remove(i);
                break;
            }
        }
        final View setButtonView = setButtonViewsMap.remove(deletedSetId);
        new Handler().postDelayed(() -> setsFlexboxLayout.removeView(setButtonView), 250);
        if (sets.size() > 0) {
            new Handler().postDelayed(() -> {
                for (int i = 0; i < sets.size(); i++) {
                    final TextView buttonView = (TextView)setsFlexboxLayout.getChildAt(i + 1);
                    buttonView.setText(Integer.toString(i + 1));
                }
                nextSetButton.setText(Integer.toString(sets.size() + 1));
                updateSetButtonClickListeners(sets);
            }, 350);
        } else {
            new Handler().postDelayed(() -> {
                hyphensTextView.setVisibility(View.VISIBLE);
                nextSetButton.setText("1");
            }, 350);
        }
    }

    private final Runnable decrementWeightAction = new Runnable() {
        @Override public final void run() {
            didIncDecViaLongPress = true;
            Utils.decrement(EnterRepsActivity.this, weightEditText, weightIncDecAmount);
            decrementWeightHandler.postDelayed(this, LONG_PRESS_DELAY);
        }
    };

    private final Runnable incrementWeightAction = new Runnable() {
        @Override public final void run() {
            didIncDecViaLongPress = true;
            Utils.increment(EnterRepsActivity.this, weightEditText, weightIncDecAmount);
            incrementWeightHandler.postDelayed(this, LONG_PRESS_DELAY);
        }
    };

    private final Runnable decrementRepsAction = new Runnable() {
        @Override public final void run() {
            didIncDecViaLongPress = true;
            Utils.decrement(EnterRepsActivity.this, repsEditText, BigDecimal.ONE);
            decrementRepsHandler.postDelayed(this, LONG_PRESS_DELAY);
        }
    };

    private final Runnable incrementRepsAction = new Runnable() {
        @Override public final void run() {
            didIncDecViaLongPress = true;
            Utils.increment(EnterRepsActivity.this, repsEditText, BigDecimal.ONE);
            incrementRepsHandler.postDelayed(this, LONG_PRESS_DELAY);
        }
    };

    @Override
    public final void onTimeSet(final TimePickerDialog view, final int hourOfDay, final int minute, final int second) {
        this.manualDate = Utils.adjustTime(this.manualDate, hourOfDay, minute, second);
        changeTimeButton.setText(timeFormat.format(this.manualDate));
    }

    @Override
    public final void onDateSet(final DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
        this.manualDate = Utils.adjustDate(this.manualDate, year, monthOfYear, dayOfMonth);
        changeDateButton.setText(dateFormat.format(this.manualDate));
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putBoolean(LSTATE_UNITS_CONTENT_EXPANDED, this.unitsContentExpanded);
        outState.putBoolean(LSTATE_MORE_CONTENT_EXPANDED, this.moreContentExpanded);
        outState.putSerializable(LSTATE_WEIGHT_UNIT, this.weightUnit);
        outState.putBoolean(LSTATE_REALTIME_CONTENT_EXPANDED, this.realTimeContentExpanded);
        outState.putBoolean(LSTATE_HAVE_SHOWN_DEFAULTED_WEIGHTTF_DIALOG, this.haveShownDefaultedWeightTfDialog);
        outState.putParcelable(CommonBundleKey.Movements.name(), Parcels.wrap(allMovements));
        outState.putParcelable(CommonBundleKey.MovementVariants.name(), Parcels.wrap(allMovementVariants));
        if (this.manualDate != null) {
            outState.putSerializable(LSTATE_MANUAL_DATE, this.manualDate);
        }
        if (this.savedSet != null) {
            outState.putParcelable(CommonBundleKey.Set.name(), Parcels.wrap(savedSet));
        }
        if (this.setsMap != null) {
            outState.putParcelable(CommonBundleKey.SetsMap.name(), Parcels.wrap(setsMap));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public final void onResume() {
        super.onResume();
        final DatePickerDialog datePickerDialog = (DatePickerDialog) getFragmentManager().findFragmentByTag(TAG_DATE_PICKER);
        final TimePickerDialog timePickerDialog = (TimePickerDialog) getFragmentManager().findFragmentByTag(TAG_TIME_PICKER);
        if (timePickerDialog != null) { timePickerDialog.setOnTimeSetListener(this); }
        if (datePickerDialog != null) { datePickerDialog.setOnDateSetListener(this); }
    }

    private final FlexboxLayout.LayoutParams makeSetButtonLayoutParams() {
        final Resources resources = getResources();
        final FlexboxLayout.LayoutParams setButtonLayoutParams = new FlexboxLayout.LayoutParams(
                (int)resources.getDimension(R.dimen.enter_reps_screen_button_round_set_size),
                (int)resources.getDimension(R.dimen.enter_reps_screen_button_round_set_size));
        setButtonLayoutParams.setMargins(
                (int)resources.getDimension(R.dimen.enter_reps_screen_set_buttons_left_margin), // left
                (int)resources.getDimension(R.dimen.enter_reps_screen_set_buttons_top_margin), // top
                0, // right
                0); // bottom
        return setButtonLayoutParams;
    }

    private final Function.SetOnTouch makeSetOnTouchFn() {
        return set -> {
            final RikerApp rikerApp = (RikerApp)getApplication();
            if (allMovements == null) {
                allMovements = Utils.toMap(rikerApp.dao.movementsWithNullMuscleIds());
            }
            if (allMovementVariants == null) {
                allMovementVariants = Utils.toMap(rikerApp.dao.movementVariants());
            }
            this.startActivityForResult(SetViewDetailsActivity.makeIntent(this,
                    set,
                    allMovements,
                    allMovementVariants), 0);
        };
    }

    @Override
    public final void onBackPressed() {
        final RikerApp rikerApp = (RikerApp)getApplication();
        rikerApp.setsMap = this.setsMap;
        super.onBackPressed();
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_reps);
        configureAppBar();
        logScreen(getTitle());
        this.setsMap = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.SetsMap.name()));
        final RikerApp rikerApp = (RikerApp)getApplication();
        final User user = rikerApp.dao.user();
        final UserSettings userSettings = rikerApp.dao.userSettings(user);
        this.weightUnit = WeightUnit.weightUnitById(userSettings.weightUom);
        final TextView movementLabel = findViewById(R.id.movementLabel);
        final TextView movementHyphenLabel = findViewById(R.id.movementHypenLabel);
        final TextView movementVariantLabel = findViewById(R.id.movementVariantLabel);
        final Movement selectedMovement = selectedMovement();
        movementLabel.setText(selectedMovement.canonicalName);
        final MovementVariant selectedMovementVariant = selectedMovementVariant();
        if (selectedMovementVariant != null) {
            movementVariantLabel.setText(selectedMovementVariant.name);
        } else {
            movementHyphenLabel.setVisibility(View.GONE);
            movementVariantLabel.setVisibility(View.GONE);
        }
        final Button movementInfoButton = findViewById(R.id.movementInfoButton);
        movementInfoButton.setOnClickListener(view -> startActivity(MovementDetailActivity.makeIntent(this, selectedMovement, false)));
        setsFlexboxLayout = findViewById(R.id.setsFlexboxLayout);
        final Resources resources = getResources();
        final FlexboxLayout.LayoutParams setButtonLayoutParams = makeSetButtonLayoutParams();
        if (savedInstanceState != null) {
            this.unitsContentExpanded = savedInstanceState.getBoolean(LSTATE_UNITS_CONTENT_EXPANDED, false);
            this.moreContentExpanded = savedInstanceState.getBoolean(LSTATE_MORE_CONTENT_EXPANDED, false);
            this.weightUnit = (WeightUnit)savedInstanceState.getSerializable(LSTATE_WEIGHT_UNIT);
            this.realTimeContentExpanded = savedInstanceState.getBoolean(LSTATE_REALTIME_CONTENT_EXPANDED, false);
            this.manualDate = (Date)savedInstanceState.getSerializable(LSTATE_MANUAL_DATE);
            this.haveShownDefaultedWeightTfDialog = savedInstanceState.getBoolean(LSTATE_HAVE_SHOWN_DEFAULTED_WEIGHTTF_DIALOG, false);
            this.savedSet = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.Set.name()));
            this.setsMap = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.SetsMap.name()));
            this.allMovements = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.Movements.name()));
            this.allMovementVariants = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.MovementVariants.name()));
        }
        if (this.setsMap == null) {
            this.setsMap = new HashMap<>();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle the display of the set circle buttons
        ////////////////////////////////////////////////////////////////////////////////////////////
        nextSetButton = findViewById(R.id.nextSetButton);
        hyphensTextView = findViewById(R.id.hyphensTextView);
        final Function.SetOnTouch setOnTouchFn = makeSetOnTouchFn();
        final String setKey = Utils.setKey(selectedMovement, selectedMovementVariant);
        final List<Set> currentSets = setsMap.get(setKey);
        final int numSets = currentSets != null ? currentSets.size() : 0;
        if (numSets > 0) {
            hyphensTextView.setVisibility(View.GONE);
            for (int i = 0; i < numSets; i++) {
                final Set set = currentSets.get(i);
                final Button setButton = (Button) LayoutInflater.from(this).inflate(R.layout.set_button, null);
                setButton.setOnClickListener(view -> setOnTouchFn.invoke(set));
                setButton.setLayoutParams(setButtonLayoutParams);
                setButton.setText(String.format("%d", i + 1));
                setsFlexboxLayout.addView(setButton, i + 1); // because index 0 is the "sets" text view
                this.setButtonViewsMap.put(set.localIdentifier, setButton);
            }
            nextSetButton.setText(String.format("%d", numSets + 1));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle the little preset weight buttons
        ////////////////////////////////////////////////////////////////////////////////////////////
        final FlexboxLayout presetWeightsFlexboxLayout = (FlexboxLayout)findViewById(R.id.presetWeightsFlexboxLayout);
        final int numPresetButtons = presetWeightsFlexboxLayout.getChildCount();
        for (int i = 0; i < numPresetButtons; i++) {
            final Button presetWeightButton = (Button)presetWeightsFlexboxLayout.getChildAt(i);
            final String weightStr = presetWeightButton.getText().toString();
            presetWeightButton.setOnClickListener(view -> {
                weightEditText.setText(weightStr);
                Utils.clearFocusAndDismissKeyboard(this, weightEditText);
            });
        }
        if (selectedMovementVariant == null ||
                this.weightUnit == WeightUnit.KG ||
                (selectedMovementVariant.localIdentifier != MovementVariant.Id.BARBELL.id &&
                        selectedMovementVariant.localIdentifier != MovementVariant.Id.SMITH_MACHINE.id)) {
            presetWeightsFlexboxLayout.setVisibility(View.INVISIBLE);
        } else {
            presetWeightsFlexboxLayout.setVisibility(View.VISIBLE);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Weight unit default msg
        ////////////////////////////////////////////////////////////////////////////////////////////
        final TextView weightUnitDefaultMsgTextView = (TextView)findViewById(R.id.weightUnitDefaultMsgTextView);
        weightUnitDefaultMsgTextView.setText(Utils.fromHtml("Weight unit default can be set in your <strong>Profile and Settings</strong>."));

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Info button click handlers
        ////////////////////////////////////////////////////////////////////////////////////////////
        findViewById(R.id.toFailureInfoButton).setOnClickListener(
                view -> {
                    final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                            DIALOG_REQUESTCODE_TO_FAILURE_INFO,
                            resources.getString(R.string.to_failure_info_title),
                            resources.getString(R.string.to_failure_info_msg));
                    showDialog(simpleDialogFragment, "dialog_fragment_to_failure_info");
                });
        findViewById(R.id.negativesInfoButton).setOnClickListener(
                view -> {
                    final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                            DIALOG_REQUESTCODE_NEGATIVES_INFO,
                            resources.getString(R.string.negatives_info_title),
                            resources.getString(R.string.negatives_info_msg));
                    showDialog(simpleDialogFragment, "dialog_fragment_negatives_info");
                });
        findViewById(R.id.realTimeInfoButton).setOnClickListener(
                view -> {
                    final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                            DIALOG_REQUESTCODE_REAL_TIME_INFO,
                            resources.getString(R.string.realtime_info_title),
                            resources.getString(R.string.realtime_info_msg));
                    showDialog(simpleDialogFragment, "dialog_fragment_real_time_info");
                });
        findViewById(R.id.ignoreTimeInfoButton).setOnClickListener(
                view -> {
                    final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.infoInstance(
                            DIALOG_REQUESTCODE_IGNORE_TIME_INFO,
                            resources.getString(R.string.ignoretime_info_title),
                            resources.getString(R.string.ignoretime_info_msg));
                    showDialog(simpleDialogFragment, "dialog_fragment_ignore_time_info");
                });

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle display/toggle of the 'more' content
        ////////////////////////////////////////////////////////////////////////////////////////////
        final ViewGroup moreContentViewGroup = (ViewGroup)findViewById(R.id.moreContentViewGroup);
        moreContentViewGroup.setVisibility(View.GONE);
        final Button moreLessButton = (Button)findViewById(R.id.moreLessButton);
        if (this.moreContentExpanded) {
            moreContentViewGroup.setVisibility(View.VISIBLE);
            moreLessButton.setText("less");
        } else {
            moreContentViewGroup.setVisibility(View.GONE);
            moreLessButton.setText("more");
        }
        moreLessButton.setOnClickListener(view -> {
            if (moreContentViewGroup.getVisibility() == View.VISIBLE) {
                moreContentViewGroup.setVisibility(View.GONE);
                moreLessButton.setText("more");
                this.moreContentExpanded = false;
            } else {
                moreContentViewGroup.setVisibility(View.VISIBLE);
                moreLessButton.setText("less");
                this.moreContentExpanded = true;
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle date/time content
        ////////////////////////////////////////////////////////////////////////////////////////////
        final ViewGroup dateContentContainer = (ViewGroup)findViewById(R.id.dateContentContainer);
        dateContentContainer.setVisibility(View.GONE);
        this.dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        this.timeFormat = new SimpleDateFormat(Constants.TIME_FORMAT);
        changeDateButton = (Button)findViewById(R.id.changeDateButton);
        changeDateButton.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.manualDate);
            final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show(getFragmentManager(), TAG_DATE_PICKER);
        });
        this.changeTimeButton = (Button)findViewById(R.id.changeTimeButton);
        changeTimeButton.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.manualDate);
            final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this,
                    calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND),
                    false); // 24-hour time (FYI, f you ever decide to change this to true, make sure to also change the hour format string from "hh" to "HH"
            timePickerDialog.enableSeconds(true);
            timePickerDialog.show(getFragmentManager(), TAG_TIME_PICKER);
        });
        final SwitchCompat realTimeSwitch = (SwitchCompat)findViewById(R.id.realTimeSwitch);
        realTimeSwitch.setChecked(!this.realTimeContentExpanded);
        if (this.realTimeContentExpanded) {
            changeDateButton.setText(dateFormat.format(this.manualDate));
            changeTimeButton.setText(timeFormat.format(this.manualDate));
            dateContentContainer.setVisibility(View.VISIBLE);
        } else {
            dateContentContainer.setVisibility(View.GONE);
        }
        realTimeSwitch.setOnClickListener(view -> {
            if (realTimeSwitch.isChecked()) {
                this.realTimeContentExpanded = false;
                dateContentContainer.setVisibility(View.GONE);
            } else {
                this.realTimeContentExpanded = true;
                this.manualDate = new Date();
                changeDateButton.setText(dateFormat.format(this.manualDate));
                changeTimeButton.setText(timeFormat.format(this.manualDate));
                dateContentContainer.setVisibility(View.VISIBLE);
            }
        });
        this.ignoreTimeSwitch = (SwitchCompat) findViewById(R.id.ignoreTimeSwitch);
        ignoreTimeSwitch.setOnClickListener(view -> {
            if (ignoreTimeSwitch.isChecked()) {
                changeDateButton.setText(dateFormat.format(this.manualDate));
                changeTimeButton.setText("---");
                changeTimeButton.setEnabled(false);
            } else {
                changeDateButton.setText(dateFormat.format(this.manualDate));
                changeTimeButton.setText(timeFormat.format(this.manualDate));
                changeTimeButton.setEnabled(true);
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle increment/decrement weight
        ////////////////////////////////////////////////////////////////////////////////////////////
        final Button decrementWeightButton = (Button)findViewById(R.id.decrementWeightButton);
        final Button incrementWeightButton = (Button)findViewById(R.id.incrementWeightButton);
        this.weightEditText = (EditText)findViewById(R.id.weightEditText);
        this.weightEditText.setHint(String.format("Weight (%s)", weightUnit.name));
        this.weightIncDecAmount = new BigDecimal(userSettings.weightIncDecAmount);
        decrementWeightButton.setText(String.format("- %s %s", this.weightIncDecAmount, this.weightUnit.name));
        incrementWeightButton.setText(String.format("+ %s %s", this.weightIncDecAmount, this.weightUnit.name));
        decrementWeightButton.setOnTouchListener((view, motionEvent) -> { // https://stackoverflow.com/a/10511800/1034895
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (this.decrementWeightHandler != null) { return true; }
                    this.decrementWeightHandler = new Handler();
                    this.decrementWeightHandler.postDelayed(this.decrementWeightAction, LONG_PRESS_INITIAL_DELAY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (this.decrementWeightHandler == null) { return true; }
                    if (!this.didIncDecViaLongPress) {
                        Utils.decrement(this, this.weightEditText, this.weightIncDecAmount);
                    }
                    this.decrementWeightHandler.removeCallbacks(this.decrementWeightAction);
                    this.decrementWeightHandler = null;
                    this.didIncDecViaLongPress = false;
                    break;
            }
            return false;
        });
        incrementWeightButton.setOnTouchListener((view, motionEvent) -> { // https://stackoverflow.com/a/10511800/1034895
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (this.incrementWeightHandler != null) { return true; }
                    this.incrementWeightHandler = new Handler();
                    this.incrementWeightHandler.postDelayed(this.incrementWeightAction, LONG_PRESS_INITIAL_DELAY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (this.incrementWeightHandler == null) { return true; }
                    if (!this.didIncDecViaLongPress) {
                        Utils.increment(this, this.weightEditText, this.weightIncDecAmount);
                    }
                    this.incrementWeightHandler.removeCallbacks(this.incrementWeightAction);
                    this.incrementWeightHandler = null;
                    this.didIncDecViaLongPress = false;
                    break;
            }
            return false;
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle if body lift movement
        ////////////////////////////////////////////////////////////////////////////////////////////
        boolean isBodyLift = false;
        if (selectedMovementVariant != null && selectedMovementVariant.localIdentifier == MovementVariant.Id.BODY.id) {
            isBodyLift = true;
        } else {
            if (selectedMovement.isBodyLift && selectedMovementVariant == null) {
                isBodyLift = true;
            }
        }
        final TextView bodyLiftInfoTextView = findViewById(R.id.bodyLiftInfoTextView);
        if (isBodyLift) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("The selected movement, <strong>%s</strong>, is a body-lift movement Riker estimates to use <strong>%s</strong> of your body weight.",
                    selectedMovement.canonicalName,
                    Utils.percentageText(selectedMovement.percentageOfBodyWeight)));
            final BodyMeasurementLog nearestNonNilWeightBml = rikerApp.dao.mostRecentBmlWithNonNilWeight(user);
            if (nearestNonNilWeightBml != null) {
                final WeightUnit bmlBodyWeightUnit = WeightUnit.weightUnitById(nearestNonNilWeightBml.bodyWeightUom);
                final BigDecimal nearestBmlBodyWeight = Utils.weightValue(nearestNonNilWeightBml.bodyWeight, bmlBodyWeightUnit, this.weightUnit);
                final BigDecimal defaultWeight = selectedMovement.percentageOfBodyWeight.multiply(nearestBmlBodyWeight);
                weightEditText.setText(Utils.formatWeightSizeValue(defaultWeight));
                stringBuilder.append(String.format("<p>Based on your most recent body measurement log, your body weight is <strong>%s %s</strong>.  Therefore we've defaulted the weight field to <strong>%s %s</strong>.</p>",
                        Utils.formatWeightSizeValue(nearestNonNilWeightBml.bodyWeight),
                        bmlBodyWeightUnit.name,
                        Utils.formatWeightSizeValue(defaultWeight),
                        weightUnit.name));
            } else {
                stringBuilder.append("<p>Keep this in mind when entering the weight value.</p>");
            }
            final Date suppressedWeightTfDefaultedToBodyWeightPopupAt = rikerApp.suppressedWeightTfDefaultedToBodyWeightPopupAt();
            if (!haveShownDefaultedWeightTfDialog &&
                    (suppressedWeightTfDefaultedToBodyWeightPopupAt == null ||
                            new DateTime(suppressedWeightTfDefaultedToBodyWeightPopupAt).plusMonths(4).isBeforeNow())) {
                            //new DateTime(suppressedWeightTfDefaultedToBodyWeightPopupAt).plusSeconds(4).isBeforeNow())) {
                final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.confirmInstance(
                        DIALOG_REQUESTCODE_SUPPRESS_WEIGHTTF_DEFAULTED_TO_BODY_WEIGHT_POPUP,
                        "Body Lift Movement",
                        stringBuilder.toString(),
                        "Remind me again later",
                        "Okay");
                showDialog(simpleDialogFragment, "dialog_fragment_weighttf_defaulted_notice");
                haveShownDefaultedWeightTfDialog = true;
            }
            decrementWeightButton.setVisibility(View.GONE);
            incrementWeightButton.setVisibility(View.GONE);
            final String bodyLiftMsg = String.format("<strong>%s</strong> is a body-lift movement Riker estimates to use <strong>%s</strong> of your body weight.",
                    StringUtils.capitalize(selectedMovement.canonicalName),
                    Utils.percentageText(selectedMovement.percentageOfBodyWeight));
            bodyLiftInfoTextView.setText(Utils.fromHtml(bodyLiftMsg));
        } else {
            bodyLiftInfoTextView.setVisibility(View.GONE);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle display/toggle of units
        ////////////////////////////////////////////////////////////////////////////////////////////
        final ViewGroup unitsContainer = (ViewGroup)findViewById(R.id.unitsContainer);
        unitsContainer.setVisibility(View.GONE);
        final Button changeHideUnitsButton = (Button)findViewById(R.id.changeHideUnitsButton);
        final Button hideButton = (Button)findViewById(R.id.hideUnitsButton);
        if (this.unitsContentExpanded) {
            unitsContainer.setVisibility(View.VISIBLE);
            changeHideUnitsButton.setText("hide\nunits");
        } else {
            unitsContainer.setVisibility(View.GONE);
            changeHideUnitsButton.setText("change\nunits");
        }
        hideButton.setOnClickListener(view -> {
            this.unitsContentExpanded = false;
            unitsContainer.setVisibility(View.GONE);
            changeHideUnitsButton.setText("change\nunits");
        });
        changeHideUnitsButton.setOnClickListener(view -> {
            if (unitsContainer.getVisibility() == View.VISIBLE) {
                this.unitsContentExpanded = false;
                unitsContainer.setVisibility(View.GONE);
                changeHideUnitsButton.setText("change\nunits");
            } else {
                this.unitsContentExpanded = true;
                unitsContainer.setVisibility(View.VISIBLE);
                changeHideUnitsButton.setText("hide\nunits");
            }
        });
        final RadioButton lbsRadioButton= (RadioButton)findViewById(R.id.lbsRadioButton);
        final RadioButton kgRadioButton= (RadioButton)findViewById(R.id.kgRadioButton);
        lbsRadioButton.setChecked(weightUnit == WeightUnit.LBS);
        lbsRadioButton.setOnClickListener(view -> {
            final BigDecimal currentWeightValue = Utils.editTextValueOrZero(this.weightEditText);
            if (currentWeightValue.compareTo(BigDecimal.ZERO) > 0) {
                this.weightEditText.setText(Utils.formatWeightSizeValue(Utils.weightValue(currentWeightValue, this.weightUnit, WeightUnit.LBS)));
            }
            this.weightUnit = WeightUnit.LBS;
            decrementWeightButton.setText(String.format("- %s %s", this.weightIncDecAmount, this.weightUnit.name));
            incrementWeightButton.setText(String.format("+ %s %s", this.weightIncDecAmount, this.weightUnit.name));
            presetWeightsFlexboxLayout.setVisibility(View.VISIBLE);
            this.weightEditText.setHint(String.format("Weight (%s)", weightUnit.name));
        });
        kgRadioButton.setChecked(weightUnit == WeightUnit.KG);
        kgRadioButton.setOnClickListener(view -> {
            final BigDecimal currentWeightValue = Utils.editTextValueOrZero(this.weightEditText);
            if (currentWeightValue.compareTo(BigDecimal.ZERO) > 0) {
                this.weightEditText.setText(Utils.formatWeightSizeValue(Utils.weightValue(currentWeightValue, this.weightUnit, WeightUnit.KG)));
            }
            this.weightUnit = WeightUnit.KG;
            decrementWeightButton.setText(String.format("- %s %s", this.weightIncDecAmount, this.weightUnit.name));
            incrementWeightButton.setText(String.format("+ %s %s", this.weightIncDecAmount, this.weightUnit.name));
            presetWeightsFlexboxLayout.setVisibility(View.INVISIBLE);
            this.weightEditText.setHint(String.format("Weight (%s)", weightUnit.name));
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle reps increment/decrement
        ////////////////////////////////////////////////////////////////////////////////////////////
        final Button decrementRepsButton = (Button)findViewById(R.id.decrementRepsButton);
        final Button incrementRepsButton = (Button)findViewById(R.id.incrementRepsButton);
        this.repsEditText = (EditText)findViewById(R.id.repsEditText);
        decrementRepsButton.setOnTouchListener((view, motionEvent) -> { // https://stackoverflow.com/a/10511800/1034895
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (this.decrementRepsHandler != null) { return true; }
                    this.decrementRepsHandler = new Handler();
                    this.decrementRepsHandler.postDelayed(this.decrementRepsAction, LONG_PRESS_INITIAL_DELAY);
                    break;
                case MotionEvent.ACTION_UP:
                    if (this.decrementRepsHandler == null) { return true; }
                    if (!this.didIncDecViaLongPress) {
                        Utils.decrement(this, this.repsEditText, BigDecimal.ONE);
                    }
                    this.decrementRepsHandler.removeCallbacks(this.decrementRepsAction);
                    this.decrementRepsHandler = null;
                    this.didIncDecViaLongPress = false;
                    break;
            }
            return false;
        });
        incrementRepsButton.setOnTouchListener((view, motionEvent) -> { // https://stackoverflow.com/a/10511800/1034895
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (this.incrementRepsHandler != null) { return true; }
                    this.incrementRepsHandler = new Handler();
                    this.incrementRepsHandler.postDelayed(this.incrementRepsAction, LONG_PRESS_INITIAL_DELAY);
                    break;
                case MotionEvent.ACTION_UP:
                    if (this.incrementRepsHandler == null) { return true; }
                    if (!this.didIncDecViaLongPress) {
                        Utils.increment(this, this.repsEditText, BigDecimal.ONE);
                    }
                    this.incrementRepsHandler.removeCallbacks(this.incrementRepsAction);
                    this.incrementRepsHandler = null;
                    this.didIncDecViaLongPress = false;
                    break;
            }
            return false;
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Save set handler
        ////////////////////////////////////////////////////////////////////////////////////////////
        final Button topSaveButton = (Button)findViewById(R.id.topSaveButton);
        final Button bottomSaveButton = (Button)findViewById(R.id.bottomSaveButton);
        final View.OnClickListener saveButtonListener = view -> {
            Utils.clearFocusAndDismissKeyboard(this, weightEditText);
            Utils.clearFocusAndDismissKeyboard(this, repsEditText);
            new Handler().postDelayed(() -> {
                final List<String> errorMessagesList = new ArrayList<>();
                final BigDecimal weightValue = Utils.validatePositiveNumberEditText(
                        false,
                        errorMessagesList,
                        weightEditText,
                        resources.getString(R.string.set_weight_empty),
                        resources.getString(R.string.set_weight_number),
                        resources.getString(R.string.set_weight_positive));
                final BigDecimal repsValue = Utils.validatePositiveNumberEditText(
                        false,
                        errorMessagesList,
                        repsEditText,
                        resources.getString(R.string.set_reps_empty),
                        resources.getString(R.string.set_reps_number),
                        resources.getString(R.string.set_reps_positive));
                if (errorMessagesList.size() == 0) {
                    final Set set = new Set();
                    set.realTime = realTimeSwitch.isChecked();
                    if (realTimeSwitch.isChecked()) {
                        set.loggedAt = new Date();
                    } else {
                        set.loggedAt = this.manualDate;
                    }
                    set.toFailure = ((SwitchCompat)findViewById(R.id.toFailureSwitch)).isChecked();
                    set.ignoreTime = ignoreTimeSwitch.isChecked();
                    set.negatives = ((SwitchCompat)findViewById(R.id.negativesSwitch)).isChecked();
                    set.movementId = selectedMovement.localIdentifier;
                    if (selectedMovementVariant != null) {
                        set.movementVariantId = selectedMovementVariant.localIdentifier;
                    }
                    set.weightUom = this.weightUnit.id;
                    set.weight = weightValue;
                    set.numReps = repsValue.intValue();
                    set.originationDeviceId = OriginationDevice.Id.ANDROID.id;
                    rikerApp.dao.saveNewSet(user, set);
                    logNewSetEvent(set);
                    indicateEntitySavedOrDeleted();
                    setResult(Codes.RESULT_CODE_ENTITY_ADDED);
                    this.savedSet = set;
                    if (offlineMode()) {

                    } else if (isUserLoggedIn() && !doesUserHaveValidAuthToken()) {

                    } else if (isUserLoggedIn() && user.isBadAccount()) {

                    } else {
                        logEvent(AnalyticsEvent.SET_SAVED_LOCAL_WHILE_ANONYMOUS);
                        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                                DIALOG_REQUESTCODE_SET_SAVED_SUCCESS,
                                "Set Saved",
                                "Your set has been saved.");
                        showDialog(simpleDialogFragment, "dialog_fragment_set_saved_success");
                    }
                } else {
                    final SimpleDialogFragment simpleDialogFragment =
                            SimpleDialogFragment.validationErrorsInstance(null, errorMessagesList);
                    showDialog(simpleDialogFragment, "dialog_fragment_enter_reps_validation_errors");
                }
            }, 50); // I don't remember why I delay this
        };
        topSaveButton.setOnClickListener(saveButtonListener);
        bottomSaveButton.setOnClickListener(saveButtonListener);
    }

    private final void updateSetButtonClickListeners(final List<Set> sets) {
        for (int i = 0; i < sets.size(); i++) {
            final Set set = sets.get(i);
            final Button button = (Button)setsFlexboxLayout.getChildAt(i + 1); // index 0 is the "sets" text view
            button.setOnClickListener(v -> makeSetOnTouchFn().invoke(set));
        }
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        final RikerApp rikerApp = (RikerApp)getApplication();
        switch (requestCode) {
            case DIALOG_REQUESTCODE_SET_SAVED_SUCCESS:
                final String setKey = Utils.setKey(selectedMovement(), selectedMovementVariant());
                List<Set> sets = setsMap.get(setKey);
                if (sets == null) {
                    setsMap.clear();
                    sets = new ArrayList<>();
                    setsMap.put(setKey, sets);
                }
                if (sets.size() == 0) {
                    hyphensTextView.setVisibility(View.GONE);
                }
                sets.add(savedSet);
                nextSetButton.setText(String.format("%d", sets.size() + 1));
                final Button setButton = (Button) LayoutInflater.from(this).inflate(R.layout.set_button, null);
                final FlexboxLayout.LayoutParams setButtonLayoutParams = makeSetButtonLayoutParams();
                setButton.setLayoutParams(setButtonLayoutParams);
                setButton.setText(String.format("%d", sets.size()));
                setsFlexboxLayout.addView(setButton, sets.size());
                this.setButtonViewsMap.put(savedSet.localIdentifier, setButton);
                updateSetButtonClickListeners(sets);
                break;
            case DIALOG_REQUESTCODE_TO_FAILURE_INFO:
                logHelpInfoPopupContentViewed("enter_reps_to_failure");
                break;
            case DIALOG_REQUESTCODE_NEGATIVES_INFO:
                logHelpInfoPopupContentViewed("enter_reps_negatives");
                break;
            case DIALOG_REQUESTCODE_REAL_TIME_INFO:
                logHelpInfoPopupContentViewed("enter_reps_real_time");
                break;
            case DIALOG_REQUESTCODE_IGNORE_TIME_INFO:
                logHelpInfoPopupContentViewed("enter_reps_time_of_day");
                break;
            case DIALOG_REQUESTCODE_SUPPRESS_WEIGHTTF_DEFAULTED_TO_BODY_WEIGHT_POPUP:
                rikerApp.setSuppressedWeightTfDefaultedToBodyWeightPopupAt(new Date());
                break;
        }
    }

    @Override
    public final void dialogNegativeClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_SUPPRESS_WEIGHTTF_DEFAULTED_TO_BODY_WEIGHT_POPUP:
                break;
        }
    }

    @Override
    public final void dialogNeutralClicked(final int requestCode) {}
}
