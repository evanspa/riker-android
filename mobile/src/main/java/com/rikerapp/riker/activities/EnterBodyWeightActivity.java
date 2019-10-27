package com.rikerapp.riker.activities;

import android.widget.RadioButton;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.UserSettings;
import com.rikerapp.riker.model.WeightUnit;

import java.io.Serializable;
import java.math.BigDecimal;

public final class EnterBodyWeightActivity extends EnterBmlActivity {

    @Override
    public final Serializable defaultValueUnit(final UserSettings userSettings) {
        return WeightUnit.weightUnitById(userSettings.weightUom);
    }

    @Override
    public final String bmlType() {
        return "Body Weight";
    }

    @Override
    public final String unitName(final Serializable unit) {
        return ((WeightUnit)unit).name;
    }

    @Override
    public final void bmlSavedPostFinishHook() {
        // bml syncing will get done by BaseActivity.onResume
    }

    @Override
    public final void configureRadioButtons(final RadioButton topUnitRadioButton, final RadioButton bottomUnitRadioButton) {
        final WeightUnit weightUnit = (WeightUnit)this.valueUnit;
        topUnitRadioButton.setChecked(weightUnit == WeightUnit.LBS);
        topUnitRadioButton.setOnClickListener(view -> {
            final WeightUnit currentWeightUnit = (WeightUnit)this.valueUnit;
            final BigDecimal currentValue = Utils.editTextValueOrZero(this.valueEditText);
            if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
                this.valueEditText.setText(Utils.formatWeightSizeValue(Utils.weightValue(currentValue, currentWeightUnit, WeightUnit.LBS)));
            }
            this.valueUnit = WeightUnit.LBS;
            this.titleTextView.setText(String.format("%s - %s", bmlType(), unitName(this.valueUnit)));
            this.valueEditText.setHint(String.format("%s (%s)", bmlType(), unitName(this.valueUnit)));
        });
        bottomUnitRadioButton.setChecked(weightUnit == WeightUnit.KG);
        bottomUnitRadioButton.setOnClickListener(view -> {
            final WeightUnit currentWeightUnit = (WeightUnit)this.valueUnit;
            final BigDecimal currentValue = Utils.editTextValueOrZero(this.valueEditText);
            if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
                this.valueEditText.setText(Utils.formatWeightSizeValue(Utils.weightValue(currentValue, currentWeightUnit, WeightUnit.KG)));
            }
            this.valueUnit = WeightUnit.KG;
            this.titleTextView.setText(String.format("%s - %s", bmlType(), unitName(this.valueUnit)));
            this.valueEditText.setHint(String.format("%s (%s)", bmlType(), unitName(this.valueUnit)));
        });
    }

    @Override
    public final void bindToBml(final BodyMeasurementLog bml, final BigDecimal value, final UserSettings userSettings) {
        bml.bodyWeight = value;
        bml.sizeUom = userSettings.sizeUom;
        bml.bodyWeightUom = ((WeightUnit)this.valueUnit).id;
    }
}
