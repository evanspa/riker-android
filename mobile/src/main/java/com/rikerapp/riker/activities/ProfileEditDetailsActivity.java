package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.RikerDao;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.User;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.WeightUnit;

import org.parceler.Parcels;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProfileEditDetailsActivity extends BaseActivity {

    private static final String LSTATE_WEIGHT = "LSTATE_WEIGHT";
    private static final String LSTATE_WEIGHT_UNIT = "LSTATE_WEIGHT_UNIT";
    private static final String LSTATE_SIZE_UNIT = "LSTATE_SIZE_UNIT";

    private EditText weightAdjustEditText;
    private AppCompatSpinner weightUnitsSpinner;
    private AppCompatSpinner sizeUnitsSpinner;
    private UserSettings userSettings;

    public static Intent makeIntent(final Context context, final UserSettings userSettings) {
        final Intent intent = new Intent(context, ProfileEditDetailsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(CommonBundleKey.UserSettings.name(), Parcels.wrap(userSettings));
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public final void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable(LSTATE_WEIGHT, new Integer(weightAdjustEditText.getText().toString()));
        outState.putSerializable(LSTATE_WEIGHT_UNIT, selectedWeightUnit());
        outState.putSerializable(LSTATE_SIZE_UNIT, selectedSizeUnit());
        outState.putParcelable(CommonBundleKey.UserSettings.name(), Parcels.wrap(userSettings));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_details);
        configureAppBar();
        logScreen(getTitle());
        this.userSettings = Parcels.unwrap(getIntent().getParcelableExtra(CommonBundleKey.UserSettings.name()));
        Integer weight = userSettings.weightIncDecAmount;
        WeightUnit weightUnit = WeightUnit.weightUnitById(userSettings.weightUom);
        SizeUnit sizeUnit = SizeUnit.sizeUnitById(userSettings.sizeUom);
        if (savedInstanceState != null) {
            weight = (Integer) savedInstanceState.getSerializable(LSTATE_WEIGHT);
            weightUnit = (WeightUnit)savedInstanceState.getSerializable(LSTATE_WEIGHT_UNIT);
            sizeUnit = (SizeUnit)savedInstanceState.getSerializable(LSTATE_SIZE_UNIT);
            this.userSettings = Parcels.unwrap(savedInstanceState.getParcelable(CommonBundleKey.UserSettings.name()));
        }
        weightAdjustEditText = Utils.bindWeightSizeEditText(this, R.id.weightAdjustEditText, weight);
        weightUnitsSpinner = (AppCompatSpinner)findViewById(R.id.weightUnitsSpinner);
        weightUnitsSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                new String[] { WeightUnit.LBS.name, WeightUnit.KG.name }));
        weightUnitsSpinner.setSelection(weightUnit.equals(WeightUnit.LBS) ? 0 : 1);
        sizeUnitsSpinner = (AppCompatSpinner)findViewById(R.id.sizeUnitsSpinner);
        sizeUnitsSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                new String[] { SizeUnit.INCHES.name, SizeUnit.CM.name }));
        sizeUnitsSpinner.setSelection(sizeUnit.equals(SizeUnit.INCHES) ? 0 : 1);
    }

    private final WeightUnit selectedWeightUnit() {
        return weightUnitsSpinner.getSelectedItemPosition() == 0 ? WeightUnit.LBS : WeightUnit.KG;
    }

    private final SizeUnit selectedSizeUnit() {
        return sizeUnitsSpinner.getSelectedItemPosition() == 0 ? SizeUnit.INCHES : SizeUnit.CM;
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
        bundle.putParcelable(CommonBundleKey.UserSettings.name(), Parcels.wrap(userSettings));
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
            finish();
            Toast.makeText(this, "Edit cancelled.", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_action_edit_entity_done) {
            final List<String> errorMessagesList = new ArrayList<>();
            final Resources resources = getResources();
            final BigDecimal weightValue = Utils.validatePositiveNumberEditText(
                    false,
                    errorMessagesList,
                    weightAdjustEditText,
                    resources.getString(R.string.profile_weight_adjust_empty),
                    resources.getString(R.string.profile_weight_adjust_number),
                    resources.getString(R.string.profile_weight_adjust_positive));
            if (errorMessagesList.size() == 0) {
                userSettings.weightIncDecAmount = weightValue.intValue();
                userSettings.weightUom = selectedWeightUnit().id;
                userSettings.sizeUom = selectedSizeUnit().id;
                final RikerApp rikerApp = (RikerApp) getApplication();
                final User user = rikerApp.dao.user();
                RikerDao.markAsDoneEditing(userSettings);
                rikerApp.dao.saveUserSettings(user, userSettings);
                indicateEntitySavedOrDeleted();
                setResultEntityUpdated();
                finish();
                Toast.makeText(this, "Profile and settings saved.", Toast.LENGTH_SHORT).show();
            } else {
                final SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.validationErrorsInstance(null, errorMessagesList);
                showDialog(simpleDialogFragment, "dialog_fragment_edit_profile_validation_errors");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
