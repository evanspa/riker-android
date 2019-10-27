package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rikerapp.riker.Constants;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.adapters.MuscleGroupsAndMovementsAdapter;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.WeightUnit;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.parceler.Parcels;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class SetEditDetailsActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // fragment tags
    private static final String TAG_DATE_PICKER = "TAG_DATE_PICKER";
    private static final String TAG_TIME_PICKER = "TAG_TIME_PICKER";

    private static final int REQUEST_CODE_SELECT_MOVEMENT = 0;

    // local state keys
    private static final String LSTATE_MANUAL_DATE = "LSTATE_MANUAL_DATE";
    private static final String LSTATE_MOVEMENT_ID = "LSTATE_MOVEMENT_ID";
    private static final String LSTATE_MOVEMENT_VARIANT_ID = "LSTATE_MOVEMENT_VARIANT_ID";
    private static final String LSTATE_IGNORE_TIME = "LSTATE_IGNORE_TIME";
    private static final String LSTATE_WEIGHT = "LSTATE_WEIGHT";
    private static final String LSTATE_WEIGHT_UNIT = "LSTATE_WEIGHT_UNIT";
    private static final String LSTATE_REPS = "LSTATE_REPS";
    private static final String LSTATE_TO_FAILURE = "LSTATE_TO_FAILURE";
    private static final String LSTATE_NEGATIVES = "LSTATE_NEGATIVES";

    private Date manualDate;
    private Button changeDateButton;
    private Button changeTimeButton;
    private Button movementButton;
    private Movement movement;
    private TextView movementVariantTextView;
    private AppCompatSpinner movementVariantSpinner;
    private ArrayAdapter movementVariantsAdapter;
    private SwitchCompat ignoreTimeSwitch;
    private boolean ignoreTime;
    private EditText weightEditText;
    private AppCompatSpinner weightUnitsSpinner;
    private WeightUnit weightUnit;
    private EditText repsEditText;
    private SwitchCompat toFailureSwitch;
    private SwitchCompat negativesSwitch;
    private List<MovementVariant> movementVariantList;
    private Map<Integer, Movement> allMovements;
    private Set set;

    public static Intent makeIntent(final Context context,
                                    final Set set,
                                    final Map<Integer, Movement> allMovements) {
        final Intent intent = new Intent(context, SetEditDetailsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.Set.name(), Parcels.wrap(set));
        bundle.putParcelable(CommonBundleKey.Movements.name(), Parcels.wrap(allMovements));
        intent.putExtras(bundle);
        return intent;
    }

    private final WeightUnit selectedWeightUnit() {
        return weightUnitsSpinner.getSelectedItemPosition() == 0 ? WeightUnit.LBS : WeightUnit.KG;
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable(LSTATE_MANUAL_DATE, this.manualDate);
        outState.putBoolean(LSTATE_IGNORE_TIME, this.ignoreTime);
        outState.putSerializable(LSTATE_MOVEMENT_ID, this.movement.localIdentifier);
        if (movement.variantMask != null && movement.variantMask != 0) {
            final MovementVariant movementVariant = this.movementVariantList.get(movementVariantSpinner.getSelectedItemPosition());
            outState.putSerializable(LSTATE_MOVEMENT_VARIANT_ID, movementVariant.localIdentifier);
        }
        outState.putSerializable(LSTATE_WEIGHT, new BigDecimal(weightEditText.getText().toString()));
        outState.putSerializable(LSTATE_WEIGHT_UNIT, selectedWeightUnit());
        outState.putSerializable(LSTATE_REPS, new Integer(repsEditText.getText().toString()));
        outState.putBoolean(LSTATE_TO_FAILURE, toFailureSwitch.isChecked());
        outState.putBoolean(LSTATE_NEGATIVES, negativesSwitch.isChecked());
        outState.putParcelable(CommonBundleKey.Set.name(), Parcels.wrap(set));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final RikerApp rikerApp = (RikerApp)getApplication();
        switch (requestCode) {
            case REQUEST_CODE_SELECT_MOVEMENT:
                if (data != null) {
                    final Integer movementId = data.getIntExtra(MuscleGroupsAndMovementsAdapter.INTENT_DATA_TAPPED_MOVEMENT_ID, 0);
                    movement = allMovements.get(movementId);
                    movementButton.setText(movement.canonicalName);
                    configureMovementVariant(movement.variantMask != null && movement.variantMask != 0);
                    if (movement.variantMask != null && movement.variantMask != 0) {
                        this.movementVariantsAdapter.clear();
                        this.movementVariantList = rikerApp.dao.movementVariants(movement.variantMask);
                        this.movementVariantsAdapter.addAll(Utils.toStringList(movementVariantList,
                                obj -> ((MovementVariant)obj).name));
                        this.movementVariantsAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_edit_details);
        configureAppBar();
        logScreen(getTitle());
        final RikerApp rikerApp = (RikerApp) getApplication();
        set = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Set.name()));
        allMovements = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.Movements.name()));
        this.manualDate = set.loggedAt;
        Integer movementId = set.movementId;
        Integer movementVariantId = set.movementVariantId;
        this.ignoreTime = set.ignoreTime;
        BigDecimal weight = set.weight;
        this.weightUnit = WeightUnit.weightUnitById(set.weightUom);
        Integer reps = set.numReps;
        boolean toFailure = set.toFailure;
        boolean negatives = set.negatives;
        if (savedInstanceState != null) {
            this.manualDate = (Date)savedInstanceState.getSerializable(LSTATE_MANUAL_DATE);
            this.ignoreTime = savedInstanceState.getBoolean(LSTATE_IGNORE_TIME);
            movementId = (Integer)savedInstanceState.getSerializable(LSTATE_MOVEMENT_ID);
            movementVariantId = (Integer)savedInstanceState.getSerializable(LSTATE_MOVEMENT_VARIANT_ID);
            weight = (BigDecimal)savedInstanceState.getSerializable(LSTATE_WEIGHT);
            weightUnit = (WeightUnit)savedInstanceState.getSerializable(LSTATE_WEIGHT_UNIT);
            reps = (Integer)savedInstanceState.getSerializable(LSTATE_REPS);
            toFailure = savedInstanceState.getBoolean(LSTATE_TO_FAILURE);
            negatives = savedInstanceState.getBoolean(LSTATE_NEGATIVES);
            set = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.Set.name()));
        }
        this.movement = allMovements.get(movementId);
        this.changeDateButton = (Button)findViewById(R.id.loggedAtDateButton);
        changeDateButton.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.manualDate);
            final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show(getFragmentManager(), TAG_DATE_PICKER);
        });
        setChangeDateButtonText();
        changeTimeButton = (Button)findViewById(R.id.loggedAtTimeButton);
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
        setChangeTimeButtonText();
        movementButton = (Button)findViewById(R.id.movementButton);
        movementButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, MuscleGroupsAndMovementsActivity.class);
            intent.putExtra(MuscleGroupsAndMovementsActivity.INTENT_DATA_SELECTED_MOVEMENT_ID, this.movement.localIdentifier);
            intent.putExtra(MuscleGroupsAndMovementsActivity.INTENT_DATA_FINISH_ON_MOVEMENT_TAP, true);
            startActivityForResult(intent, 0);
        });
        ignoreTimeSwitch = (SwitchCompat)findViewById(R.id.ignoreTimeSwitch);
        ignoreTimeSwitch.setChecked(this.ignoreTime);
        configureChangeTimeButton();
        ignoreTimeSwitch.setOnClickListener(view -> {
            this.ignoreTime = ignoreTimeSwitch.isChecked();
            configureChangeTimeButton();
        });
        movementButton.setText(movement.canonicalName);
        movementVariantTextView = (TextView)findViewById(R.id.movementVariantTextView);
        movementVariantSpinner = (AppCompatSpinner)findViewById(R.id.movementVariantSpinner);
        configureMovementVariant(movementVariantId != null);
        this.movementVariantList = rikerApp.dao.movementVariants(movement.variantMask);
        this.movementVariantsAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                Utils.toStringList(movementVariantList, obj -> ((MovementVariant)obj).name));
        final int numVariants = movementVariantList.size();
        int selectedVariantIndex = 0;
        for (int i = 0; i < numVariants; i++) {
            if (movementVariantList.get(i).localIdentifier.equals(movementVariantId)) {
                selectedVariantIndex = i;
                break;
            }
        }
        movementVariantSpinner.setAdapter(this.movementVariantsAdapter);
        movementVariantSpinner.setSelection(selectedVariantIndex);
        this.weightEditText = Utils.bindWeightSizeEditText(this, R.id.weightEditText, weight);
        this.weightUnitsSpinner = (AppCompatSpinner)findViewById(R.id.weightUnitsSpinner);
        this.weightUnitsSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                new String[] { WeightUnit.LBS.name, WeightUnit.KG.name }));
        this.weightUnitsSpinner.setSelection(weightUnit.equals(WeightUnit.LBS) ? 0 : 1);
        this.weightUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public final void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                final WeightUnit selectedWeightUnit = position == 0 ? WeightUnit.LBS : WeightUnit.KG;
                Utils.convert(weightEditText, SetEditDetailsActivity.this.weightUnit, selectedWeightUnit);
                SetEditDetailsActivity.this.weightUnit = selectedWeightUnit;
            }

            @Override public final void onNothingSelected(final AdapterView<?> parent) { /* not used */ }
        });
        this.repsEditText = (EditText)findViewById(R.id.repsEditText);
        this.repsEditText.setText(reps.toString());
        this.toFailureSwitch = (SwitchCompat)findViewById(R.id.toFailureSwitch);
        this.toFailureSwitch.setChecked(toFailure);
        this.negativesSwitch = (SwitchCompat)findViewById(R.id.negativesSwitch);
        this.negativesSwitch.setChecked(negatives);
    }

    private final void configureChangeTimeButton() {
        if (ignoreTimeSwitch.isChecked()) {
            changeTimeButton.setEnabled(false);
            changeTimeButton.setText("---");
        } else {
            changeTimeButton.setEnabled(true);
            setChangeTimeButtonText();
        }
    }

    private final void configureMovementVariant(final boolean enableSpinner) {
        if (enableSpinner) {
            movementVariantTextView.setVisibility(View.GONE);
            movementVariantSpinner.setVisibility(View.VISIBLE);
        } else {
            movementVariantTextView.setVisibility(View.VISIBLE);
            movementVariantSpinner.setVisibility(View.GONE);
        }
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
        bundle.putParcelable(CommonBundleKey.Set.name(), Parcels.wrap(set));
        resultIntent.putExtras(bundle);
        setResult(Codes.RESULT_CODE_ENTITY_UPDATED, resultIntent);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        final RikerApp rikerApp = (RikerApp) getApplication();
        if (id == R.id.menu_action_edit_entity_cancel) {
            finish();
            Toast.makeText(this, "Edit cancelled.", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_action_edit_entity_done) {
            final List<String> errorMessagesList = new ArrayList<>();
            final Resources resources = getResources();
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
                set.loggedAt = this.manualDate;
                set.ignoreTime = this.ignoreTimeSwitch.isChecked();
                set.movementId = this.movement.localIdentifier;
                set.movementVariantId = null;
                if (movement.variantMask != null && movement.variantMask != 0) {
                    final MovementVariant movementVariant = this.movementVariantList.get(movementVariantSpinner.getSelectedItemPosition());
                    set.movementVariantId = movementVariant.localIdentifier;
                }
                set.weight = weightValue;
                set.weightUom = selectedWeightUnit().id;
                set.numReps = repsValue.intValue();
                set.toFailure = this.toFailureSwitch.isChecked();
                set.negatives = this.negativesSwitch.isChecked();
                final User user = rikerApp.dao.user();
                RikerDao.markAsDoneEditing(set);
                rikerApp.dao.saveSet(user, set);
                setResultEntityUpdated();
                indicateEntitySavedOrDeleted();
                finish();
                Toast.makeText(this, "Set saved.", Toast.LENGTH_SHORT).show();
            } else {
                final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.validationErrorsInstance(null, errorMessagesList);
                showDialog(simpleDialogFragment, "dialog_fragment_edit_set_validation_errors");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onTimeSet(final TimePickerDialog view, final int hourOfDay, final int minute, final int second) {
        this.manualDate = Utils.adjustTime(this.manualDate, hourOfDay, minute, second);
        setChangeTimeButtonText();
    }

    @Override
    public final void onDateSet(final DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
        this.manualDate = Utils.adjustDate(this.manualDate, year, monthOfYear, dayOfMonth);
        setChangeDateButtonText();
    }

    private final void setChangeDateButtonText() {
        this.changeDateButton.setText(new SimpleDateFormat(Constants.DATE_FORMAT).format(this.manualDate));
    }

    private final void setChangeTimeButtonText() {
        this.changeTimeButton.setText(new SimpleDateFormat(Constants.TIME_FORMAT).format(this.manualDate));
    }
}
