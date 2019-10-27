package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.WeightUnit;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.parceler.Parcels;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BmlEditDetailsActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    // fragment tags
    private static final String TAG_DATE_PICKER = "TAG_DATE_PICKER";

    // local state keys
    private static final String LSTATE_LOGGED_AT_DATE = "LSTATE_LOGGED_AT_DATE";
    private static final String LSTATE_WEIGHT_UNIT = "LSTATE_WEIGHT_UNIT";
    private static final String LSTATE_BODY_WEIGHT = "LSTATE_BODY_WEIGHT";
    private static final String LSTATE_SIZE_UNIT = "LSTATE_SIZE_UNIT";
    private static final String LSTATE_ARM_SIZE = "LSTATE_ARM_SIZE";
    private static final String LSTATE_CHEST_SIZE = "LSTATE_CHEST_SIZE";
    private static final String LSTATE_CALF_SIZE = "LSTATE_CALF_SIZE";
    private static final String LSTATE_THIGH_SIZE = "LSTATE_THIGH_SIZE";
    private static final String LSTATE_FOREARM_SIZE = "LSTATE_FOREARM_SIZE";
    private static final String LSTATE_WAIST_SIZE = "LSTATE_WAIST_SIZE";
    private static final String LSTATE_NECK_SIZE = "LSTATE_NECK_SIZE";

    private Date loggedAtDate;
    private SizeUnit sizeUnit;
    private Button changeLoggedAtDateButton;
    private AppCompatSpinner weightUnitsSpinner;
    private WeightUnit weightUnit;
    private EditText bodyWeightEditText;
    private AppCompatSpinner sizeUnitsSpinner;
    private EditText armSizeEditText;
    private EditText chestSizeEditText;
    private EditText calfSizeEditText;
    private EditText thighSizeEditText;
    private EditText forearmSizeEditText;
    private EditText waistSizeEditText;
    private EditText neckSizeEditText;
    private BodyMeasurementLog bml;

    public static Intent makeIntent(final Context context, final BodyMeasurementLog bml) {
        final Intent intent = new Intent(context, BmlEditDetailsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Bml.name(), Parcels.wrap(bml));
        intent.putExtras(bundle);
        return intent;
    }

    private final WeightUnit selectedWeightUnit() {
        return weightUnitsSpinner.getSelectedItemPosition() == 0 ? WeightUnit.LBS : WeightUnit.KG;
    }

    private final SizeUnit selectedSizeUnit() {
        return sizeUnitsSpinner.getSelectedItemPosition() == 0 ? SizeUnit.INCHES : SizeUnit.CM;
    }

    @Override
    public final void onDateSet(final DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
        this.loggedAtDate = Utils.adjustDate(this.loggedAtDate, year, monthOfYear, dayOfMonth);
        setChangeDateButtonText();
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable(LSTATE_LOGGED_AT_DATE, this.loggedAtDate);
        outState.putSerializable(LSTATE_WEIGHT_UNIT, selectedWeightUnit());
        Utils.putBigDecimalIfNotEmpty(outState, bodyWeightEditText, LSTATE_BODY_WEIGHT);
        outState.putSerializable(LSTATE_SIZE_UNIT, selectedSizeUnit());
        Utils.putBigDecimalIfNotEmpty(outState, armSizeEditText, LSTATE_ARM_SIZE);
        Utils.putBigDecimalIfNotEmpty(outState, chestSizeEditText, LSTATE_CHEST_SIZE);
        Utils.putBigDecimalIfNotEmpty(outState, calfSizeEditText, LSTATE_CALF_SIZE);
        Utils.putBigDecimalIfNotEmpty(outState, thighSizeEditText, LSTATE_THIGH_SIZE);
        Utils.putBigDecimalIfNotEmpty(outState, forearmSizeEditText, LSTATE_FOREARM_SIZE);
        Utils.putBigDecimalIfNotEmpty(outState, waistSizeEditText, LSTATE_WAIST_SIZE);
        Utils.putBigDecimalIfNotEmpty(outState, neckSizeEditText, LSTATE_NECK_SIZE);
        outState.putParcelable(CommonBundleKey.Bml.name(), Parcels.wrap(bml));
        super.onSaveInstanceState(outState);
    }

    public BodyMeasurementLog bml() {
        return Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Bml.name()));
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bml_edit_details);
        configureAppBar();
        logScreen(getTitle());
        this.bml = bml();
        this.loggedAtDate = bml.loggedAt;
        this.weightUnit = WeightUnit.weightUnitById(bml.bodyWeightUom);
        BigDecimal bodyWeight = bml.bodyWeight;
        this.sizeUnit = SizeUnit.sizeUnitById(bml.sizeUom);
        BigDecimal armSize = bml.armSize;
        BigDecimal chestSize = bml.chestSize;
        BigDecimal calfSize = bml.calfSize;
        BigDecimal thighSize = bml.thighSize;
        BigDecimal forearmSize = bml.forearmSize;
        BigDecimal waistSize = bml.waistSize;
        BigDecimal neckSize = bml.neckSize;
        if (savedInstanceState != null) {
            this.loggedAtDate = (Date)savedInstanceState.getSerializable(LSTATE_LOGGED_AT_DATE);
            weightUnit = (WeightUnit)savedInstanceState.getSerializable(LSTATE_WEIGHT_UNIT);
            bodyWeight = (BigDecimal)savedInstanceState.getSerializable(LSTATE_BODY_WEIGHT);
            sizeUnit = (SizeUnit)savedInstanceState.getSerializable(LSTATE_SIZE_UNIT);
            armSize = (BigDecimal)savedInstanceState.getSerializable(LSTATE_ARM_SIZE);
            chestSize = (BigDecimal)savedInstanceState.getSerializable(LSTATE_CHEST_SIZE);
            calfSize = (BigDecimal)savedInstanceState.getSerializable(LSTATE_CALF_SIZE);
            thighSize = (BigDecimal)savedInstanceState.getSerializable(LSTATE_THIGH_SIZE);
            forearmSize = (BigDecimal)savedInstanceState.getSerializable(LSTATE_FOREARM_SIZE);
            waistSize = (BigDecimal)savedInstanceState.getSerializable(LSTATE_WAIST_SIZE);
            neckSize = (BigDecimal)savedInstanceState.getSerializable(LSTATE_NECK_SIZE);
            this.bml = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.Bml.name()));
        }
        changeLoggedAtDateButton = (Button)findViewById(R.id.loggedAtDateButton);
        changeLoggedAtDateButton.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.loggedAtDate);
            final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show(getFragmentManager(), TAG_DATE_PICKER);
        });
        setChangeDateButtonText();
        this.weightUnitsSpinner = findViewById(R.id.weightUnitsSpinner);
        this.weightUnitsSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                new String[] { WeightUnit.LBS.name, WeightUnit.KG.name }));
        this.weightUnitsSpinner.setSelection(weightUnit.equals(WeightUnit.LBS) ? 0 : 1);
        this.bodyWeightEditText = Utils.bindWeightSizeEditText(this, R.id.bodyWeightEditText, bodyWeight);
        this.sizeUnitsSpinner = findViewById(R.id.sizeUnitsSpinner);
        this.sizeUnitsSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                new String[] { SizeUnit.INCHES.name, SizeUnit.CM.name }));
        this.sizeUnitsSpinner.setSelection(sizeUnit.equals(SizeUnit.INCHES) ? 0 : 1);
        this.armSizeEditText = Utils.bindWeightSizeEditText(this, R.id.armSizeEditText, armSize);
        this.chestSizeEditText = Utils.bindWeightSizeEditText(this, R.id.chestSizeEditText, chestSize);
        this.calfSizeEditText = Utils.bindWeightSizeEditText(this, R.id.calfSizeEditText, calfSize);
        this.thighSizeEditText = Utils.bindWeightSizeEditText(this, R.id.thighSizeEditText, thighSize);
        this.forearmSizeEditText = Utils.bindWeightSizeEditText(this, R.id.forearmSizeEditText, forearmSize);
        this.waistSizeEditText = Utils.bindWeightSizeEditText(this, R.id.waistSizeEditText, waistSize);
        this.neckSizeEditText = Utils.bindWeightSizeEditText(this, R.id.neckSizeEditText, neckSize);
        this.weightUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public final void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                final WeightUnit selectedWeightUnit = position == 0 ? WeightUnit.LBS : WeightUnit.KG;
                Utils.convert(bodyWeightEditText, BmlEditDetailsActivity.this.weightUnit, selectedWeightUnit);
                BmlEditDetailsActivity.this.weightUnit = selectedWeightUnit;
            }

            @Override public final void onNothingSelected(final AdapterView<?> parent) { /* not used */ }
        });
        this.sizeUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public final void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                final SizeUnit selectedSizeUnit = position == 0 ? SizeUnit.INCHES : SizeUnit.CM;
                Utils.convert(armSizeEditText, BmlEditDetailsActivity.this.sizeUnit, selectedSizeUnit);
                Utils.convert(chestSizeEditText, BmlEditDetailsActivity.this.sizeUnit, selectedSizeUnit);
                Utils.convert(calfSizeEditText, BmlEditDetailsActivity.this.sizeUnit, selectedSizeUnit);
                Utils.convert(thighSizeEditText, BmlEditDetailsActivity.this.sizeUnit, selectedSizeUnit);
                Utils.convert(forearmSizeEditText, BmlEditDetailsActivity.this.sizeUnit, selectedSizeUnit);
                Utils.convert(waistSizeEditText, BmlEditDetailsActivity.this.sizeUnit, selectedSizeUnit);
                Utils.convert(neckSizeEditText, BmlEditDetailsActivity.this.sizeUnit, selectedSizeUnit);
                BmlEditDetailsActivity.this.sizeUnit = selectedSizeUnit;
            }

            @Override public final void onNothingSelected(final AdapterView<?> parent) { /* not used */ }
        });
    }

    private final void setChangeDateButtonText() {
        this.changeLoggedAtDateButton.setText(new SimpleDateFormat(Constants.DATE_FORMAT).format(this.loggedAtDate));
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entity_edit_details, menu);
        return true;
    }

    private final void setResultEntityUpdated() {
        final Intent resultIntent = new Intent();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Bml.name(), Parcels.wrap(bml));
        resultIntent.putExtras(bundle);
        setResult(Codes.RESULT_CODE_ENTITY_UPDATED, resultIntent);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        if (id == R.id.menu_action_edit_entity_cancel) {
            cancel();
            return true;
        } else if (id == R.id.menu_action_edit_entity_done) {
            final List<String> errorMessagesList = new ArrayList<>();
            final Resources resources = getResources();
            final BigDecimal bodyWeightValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    bodyWeightEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_body_weight_number),
                    resources.getString(R.string.bml_body_weight_positive));
            final BigDecimal armSizeValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    armSizeEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_arm_size_number),
                    resources.getString(R.string.bml_arm_size_positive));
            final BigDecimal chestSizeValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    chestSizeEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_chest_size_number),
                    resources.getString(R.string.bml_chest_size_positive));
            final BigDecimal calfSizeValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    calfSizeEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_calf_size_number),
                    resources.getString(R.string.bml_calf_size_positive));
            final BigDecimal thighSizeValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    thighSizeEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_thigh_size_number),
                    resources.getString(R.string.bml_thigh_size_positive));
            final BigDecimal forearmSizeValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    forearmSizeEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_forearm_size_number),
                    resources.getString(R.string.bml_forearm_size_positive));
            final BigDecimal waistSizeValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    waistSizeEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_waist_size_number),
                    resources.getString(R.string.bml_waist_size_positive));
            final BigDecimal neckSizeValue = Utils.validatePositiveNumberEditText(
                    true,
                    errorMessagesList,
                    neckSizeEditText,
                    null, // is-null error msg irrelevant
                    resources.getString(R.string.bml_neck_size_number),
                    resources.getString(R.string.bml_neck_size_positive));
            if (errorMessagesList.size() == 0) {
                if (bodyWeightValue == null &&
                        armSizeValue == null &&
                        chestSizeValue == null &&
                        calfSizeValue == null &&
                        thighSizeValue == null &&
                        waistSizeValue == null &&
                        forearmSizeValue == null &&
                        neckSizeValue == null) {
                    final SimpleDialogFragment simpleDialogFragment =
                            SimpleDialogFragment.validationErrorsInstance(null, Arrays.asList("At least one value needs to be provided."));
                    showDialog(simpleDialogFragment, "dialog_fragment_edit_bml_validation_errors");
                } else {
                    this.bml.loggedAt = this.loggedAtDate;
                    this.bml.bodyWeightUom = selectedWeightUnit().id;
                    this.bml.sizeUom = selectedSizeUnit().id;
                    this.bml.bodyWeight = bodyWeightValue;
                    this.bml.armSize = armSizeValue;
                    this.bml.chestSize = chestSizeValue;
                    this.bml.calfSize = calfSizeValue;
                    this.bml.thighSize = thighSizeValue;
                    this.bml.forearmSize = forearmSizeValue;
                    this.bml.waistSize = waistSizeValue;
                    this.bml.neckSize = neckSizeValue;
                    final RikerApp rikerApp = (RikerApp) getApplication();
                    final User user = rikerApp.dao.user();
                    saveAndDone(user, bml);
                }
            } else {
                final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.validationErrorsInstance(null, errorMessagesList);
                showDialog(simpleDialogFragment, "dialog_fragment_edit_bml_validation_errors");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveAndDone(final User user, final BodyMeasurementLog bml) {
        RikerDao.markAsDoneEditing(bml);
        final RikerApp rikerApp = (RikerApp) getApplication();
        rikerApp.dao.saveBml(user, bml);
        indicateEntitySavedOrDeleted();
        setResultEntityUpdated();
        finish();
        Toast.makeText(this, "Body log saved.", Toast.LENGTH_SHORT).show();
    }

    public void cancel() {
        finish();
        Toast.makeText(this, "Edit cancelled.", Toast.LENGTH_SHORT).show();
    }
}
