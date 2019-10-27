package com.rikerapp.riker.activities;

import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.UserSettings;

import java.math.BigDecimal;

public final class EnterThighSizeActivity extends EnterBmlSizeActivity {

    @Override
    public final String bmlType() {
        return "Thigh Size";
    }

    @Override
    public final void bindToBml(final BodyMeasurementLog bml, final BigDecimal value, final UserSettings userSettings) {
        super.bindToBml(bml, value, userSettings);
        bml.thighSize = value;
    }
}
