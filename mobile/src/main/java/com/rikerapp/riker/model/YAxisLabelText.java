package com.rikerapp.riker.model;

import java.math.BigDecimal;

public enum YAxisLabelText {
    MILLIONS("in millions of %s", "in millions of %s"),
    HUNDREDS_OF_THOUS("in hundreds of thousands of %s", "in hund. of thous. of %s"),
    TENS_OF_THOUS("in tens of thousands of %s", "in tens of thous. of %s"),
    THOUS("in thousands of %s", "in thousands of %s"),
    ONES("%s", "%s")
    ;

    public final String textTemplate;
    public final String abbrevTextTemplate;

    YAxisLabelText(final String textTemplate, final String abbrevTextTemplate) {
        this.textTemplate = textTemplate;
        this.abbrevTextTemplate = abbrevTextTemplate;
    }

    public static LabelPair strengthValueForMaxWeight(final UserSettings userSettings, final BigDecimal maxWeight) {
        final WeightUnit weightUnit = WeightUnit.weightUnitById(userSettings.weightUom);
        return YAxisLabelText.valueForMaxValue(maxWeight, weightUnit.name);
    }

    public static LabelPair valueForMaxValue(final BigDecimal maxValue, final String type) {
        final float max = maxValue.floatValue();
        YAxisLabelText yAxisLabelText;
        if (max > 999999) {
            yAxisLabelText = MILLIONS;
        } else if (max > 99999) {
            yAxisLabelText = HUNDREDS_OF_THOUS;
        } else if (max > 9999) {
            yAxisLabelText = TENS_OF_THOUS;
        } else if (max > 999) {
            yAxisLabelText = THOUS;
        } else {
            yAxisLabelText = ONES;
        }
        return new LabelPair(String.format(yAxisLabelText.textTemplate, type), String.format(yAxisLabelText.abbrevTextTemplate, type));
    }

    public static final class LabelPair {
        public final String text;
        public final String abbrevText;

        public LabelPair(final String text, final String abbrevText) {
            this.text = text;
            this.abbrevText = abbrevText;
        }
    }
}
