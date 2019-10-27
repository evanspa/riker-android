package com.rikerapp.riker.activities;

import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.UserSettings;

import java.math.BigDecimal;

public final class EnterNeckSizeActivity extends EnterBmlSizeActivity {

    @Override
    public final String bmlType() {
        return "Neck Size";
    }

    @Override
    public final void bindToBml(final BodyMeasurementLog bml, final BigDecimal value, final UserSettings userSettings) {
        super.bindToBml(bml, value, userSettings);
        bml.neckSize = value;
    }
}
