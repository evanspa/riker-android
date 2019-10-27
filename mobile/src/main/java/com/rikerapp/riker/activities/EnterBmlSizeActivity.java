package com.rikerapp.riker.activities;

import android.widget.RadioButton;

import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.UserSettings;

import java.io.Serializable;
import java.math.BigDecimal;

public abstract class EnterBmlSizeActivity extends EnterBmlActivity {

    @Override
    public final Serializable defaultValueUnit(final UserSettings userSettings) {
        return SizeUnit.sizeUnitById(userSettings.sizeUom);
    }

    @Override
    public final String unitName(final Serializable unit) {
        return ((SizeUnit)unit).name;
    }

    @Override
    public final void configureRadioButtons(final RadioButton topUnitRadioButton, final RadioButton bottomUnitRadioButton) {
        final SizeUnit sizeUnit = (SizeUnit)this.valueUnit;
        topUnitRadioButton.setChecked(sizeUnit == SizeUnit.INCHES);
        topUnitRadioButton.setOnClickListener(view -> {
            final SizeUnit currentSizeUnit = (SizeUnit)this.valueUnit;
            final BigDecimal currentValue = Utils.editTextValueOrZero(this.valueEditText);
            if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
                this.valueEditText.setText(Utils.formatWeightSizeValue(Utils.sizeValue(currentValue, currentSizeUnit, SizeUnit.INCHES)));
            }
            this.valueUnit = SizeUnit.INCHES;
            this.titleTextView.setText(String.format("%s - %s", bmlType(), unitName(this.valueUnit)));
            this.valueEditText.setHint(String.format("%s (%s)", bmlType(), unitName(this.valueUnit)));
        });
        bottomUnitRadioButton.setChecked(sizeUnit == SizeUnit.CM);
        bottomUnitRadioButton.setOnClickListener(view -> {
            final SizeUnit currentSizeUnit = (SizeUnit)this.valueUnit;
            final BigDecimal currentValue = Utils.editTextValueOrZero(this.valueEditText);
            if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
                this.valueEditText.setText(Utils.formatWeightSizeValue(Utils.sizeValue(currentValue, currentSizeUnit, SizeUnit.CM)));
            }
            this.valueUnit = SizeUnit.CM;
            this.titleTextView.setText(String.format("%s - %s", bmlType(), unitName(this.valueUnit)));
            this.valueEditText.setHint(String.format("%s (%s)", bmlType(), unitName(this.valueUnit)));
        });
    }

    @Override
    public void bindToBml(final BodyMeasurementLog bml, final BigDecimal value, final UserSettings userSettings) {
        bml.sizeUom = ((SizeUnit)this.valueUnit).id;
        bml.bodyWeightUom = userSettings.weightUom;
    }
}
