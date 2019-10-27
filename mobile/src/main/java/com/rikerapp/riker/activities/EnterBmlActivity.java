package com.rikerapp.riker.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rikerapp.riker.AnalyticsEvent;
import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class EnterBmlActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, SimpleDialogFragment.Callbacks {

    // fragment tags
    private static final String TAG_DATE_PICKER = "TAG_DATE_PICKER";

    // local state keys
    private static final String LSTATE_UNITS_CONTENT_EXPANDED = "LSTATE_UNITS_CONTENT_EXPANDED";
    private static final String LSTATE_VALUE_UNIT = "LSTATE_VALUE_UNIT";
    private static final String LSTATE_MANUAL_DATE = "LSTATE_MANUAL_DATE";

    // dialog request codes
    private static final int DIALOG_REQUESTCODE_BML_SAVED_SUCCESS = 1;

    private boolean unitsContentExpanded;
    public TextView titleTextView;
    public EditText valueEditText;
    public Serializable valueUnit;
    public Date loggedAtDate;
    private SimpleDateFormat dateFormat;
    private Button changeDateButton;

    @Override
    public final void onDateSet(final DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
        this.loggedAtDate = Utils.adjustDate(this.loggedAtDate, year, monthOfYear, dayOfMonth);
        this.changeDateButton.setText(dateFormat.format(this.loggedAtDate));
    }

    public abstract Serializable defaultValueUnit(final UserSettings userSettings);

    public abstract String bmlType();

    public abstract String unitName(final Serializable unit);

    public abstract void configureRadioButtons(final RadioButton topUnitRadioButton, final RadioButton bottomUnitRadioButton);

    public abstract void bindToBml(final BodyMeasurementLog bml, final BigDecimal value, final UserSettings userSettings);

    public void bmlSavedPostFinishHook() {}

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putBoolean(LSTATE_UNITS_CONTENT_EXPANDED, this.unitsContentExpanded);
        outState.putSerializable(LSTATE_VALUE_UNIT, this.valueUnit);
        outState.putSerializable(LSTATE_MANUAL_DATE, this.loggedAtDate);
        super.onSaveInstanceState(outState);
    }

    @Override
    public final void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        changeDateButton.setText(dateFormat.format(this.loggedAtDate));
    }

    @Override
    public final void onResume() {
        super.onResume();
        final DatePickerDialog datePickerDialog = (DatePickerDialog) getFragmentManager().findFragmentByTag(TAG_DATE_PICKER);
        if (datePickerDialog != null) { datePickerDialog.setOnDateSetListener(this); }
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_bml);
        configureAppBar();
        logScreen(getTitle());

        final RikerApp rikerApp = (RikerApp)getApplication();
        final User user = rikerApp.dao.user();
        final UserSettings userSettings = rikerApp.dao.userSettings(user);
        this.valueUnit = defaultValueUnit(userSettings);
        titleTextView = (TextView)findViewById(R.id.bmlTitleTextView);
        titleTextView.setText(String.format("%s - %s", bmlType(), unitName(this.valueUnit)));
        this.valueEditText = (EditText)findViewById(R.id.valueEditText);
        this.valueEditText.setHint(String.format("%s (%s)", bmlType(), unitName(this.valueUnit)));
        this.loggedAtDate = new Date();

        if (savedInstanceState != null) {
            this.unitsContentExpanded = savedInstanceState.getBoolean(LSTATE_UNITS_CONTENT_EXPANDED, false);
            this.valueUnit = savedInstanceState.getSerializable(LSTATE_VALUE_UNIT);
            this.loggedAtDate = (Date)savedInstanceState.getSerializable(LSTATE_MANUAL_DATE);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Weight unit default msg
        ////////////////////////////////////////////////////////////////////////////////////////////
        final TextView valueUnitDefaultMsgTextView = (TextView)findViewById(R.id.valueUnitDefaultMsgTextView);
        valueUnitDefaultMsgTextView.setText(Utils.fromHtml("Weight unit default can be set in your <strong>Profile and Settings</strong>."));

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Handle date/time content
        ////////////////////////////////////////////////////////////////////////////////////////////
        this.dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        changeDateButton = (Button)findViewById(R.id.changeDateButton);
        changeDateButton.setText(dateFormat.format(loggedAtDate));
        changeDateButton.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.loggedAtDate);
            final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show(getFragmentManager(), TAG_DATE_PICKER);
        });

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
        final RadioButton topUnitRadioButton= (RadioButton)findViewById(R.id.topUnitRadioButton);
        final RadioButton bottomUnitRadioButton = (RadioButton)findViewById(R.id.bottomUnitRadioButton);
        configureRadioButtons(topUnitRadioButton, bottomUnitRadioButton);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Save set handler
        ////////////////////////////////////////////////////////////////////////////////////////////
        final Button topSaveButton = (Button)findViewById(R.id.topSaveButton);
        final Button bottomSaveButton = (Button)findViewById(R.id.bottomSaveButton);
        final View.OnClickListener saveButtonListener = view -> {
            Utils.clearFocusAndDismissKeyboard(this, valueEditText);
            new Handler().postDelayed(() -> {
                final List<String> errorMessagesList = new ArrayList<>();
                final BigDecimal value = Utils.validatePositiveNumberEditText(
                        false,
                        errorMessagesList,
                        valueEditText,
                        "Value cannot be empty.",
                        "Value must be a number.",
                        "Value must be a positive number.");
                if (errorMessagesList.size() == 0) {
                    final BodyMeasurementLog bml = new BodyMeasurementLog();
                    bml.loggedAt = this.loggedAtDate;
                    bindToBml(bml, value, userSettings);
                    bml.originationDeviceId = OriginationDevice.Id.ANDROID.id;
                    rikerApp.dao.saveNewBml(user, bml);
                    setResult(Codes.RESULT_CODE_ENTITY_ADDED);
                    if (offlineMode()) {

                    } else if (isUserLoggedIn() && !doesUserHaveValidAuthToken()) {

                    } else if (isUserLoggedIn() && user.isBadAccount()) {

                    } else {
                        logEvent(AnalyticsEvent.BML_SAVED_LOCAL_WHILE_ANONYMOUS);
                        final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.successInstance(
                                DIALOG_REQUESTCODE_BML_SAVED_SUCCESS,
                                "Body Log Saved",
                                "Your body measurement log has been saved.");
                        showDialog(simpleDialogFragment, "dialog_fragment_bml_saved_success");
                    }
                } else {
                    final SimpleDialogFragment simpleDialogFragment =
                            SimpleDialogFragment.validationErrorsInstance(null, errorMessagesList);
                    showDialog(simpleDialogFragment, "dialog_fragment_enter_bml_validation_errors");
                }
            }, 50); // I don't remember why I delay this
        };
        topSaveButton.setOnClickListener(saveButtonListener);
        bottomSaveButton.setOnClickListener(saveButtonListener);
    }

    @Override
    public final void dialogPositiveClicked(final int requestCode) {
        switch (requestCode) {
            case DIALOG_REQUESTCODE_BML_SAVED_SUCCESS:
                finish();
                bmlSavedPostFinishHook();
                break;
        }
    }

    @Override
    public final void dialogNegativeClicked(final int requestCode) {}

    @Override
    public final void dialogNeutralClicked(final int requestCode) {}
}
