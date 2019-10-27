package com.rikerapp.riker.model;

public enum ChartDataFetchMode {

    WEIGHT_LIFTED_CROSS_SECTION (ChartConfig.Category.WEIGHT),
    WEIGHT_LIFTED_LINE          (ChartConfig.Category.WEIGHT),
    WEIGHT_LIFTED_PIE           (ChartConfig.Category.WEIGHT),
    REPS_CROSS_SECTION          (ChartConfig.Category.REPS),
    REPS_LINE                   (ChartConfig.Category.REPS),
    REPS_PIE                    (ChartConfig.Category.REPS),
    REST_TIME_CROSS_SECTION     (ChartConfig.Category.REST_TIME),
    REST_TIME_LINE              (ChartConfig.Category.REST_TIME),
    REST_TIME_PIE               (ChartConfig.Category.REST_TIME),
    BODY                        (ChartConfig.Category.BODY),

    ;
    public final ChartConfig.Category category;

    ChartDataFetchMode(final ChartConfig.Category category) {
        this.category = category;
    }
}
