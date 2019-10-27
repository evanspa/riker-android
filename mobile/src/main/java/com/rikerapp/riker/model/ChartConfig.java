package com.rikerapp.riker.model;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;

import com.rikerapp.riker.R;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public final class ChartConfig {

    public enum EntityType {
        BML,
        SET
    }

    public enum GlobalChartId {

        WEIGHT_LIFTED("global-weight-lifted", R.string.weight_lifted_global_config_title),
        REPS("global-reps", R.string.reps_global_config_title),
        REST_TIME("global-rest-time", R.string.rest_time_global_config_title),
        BODY("global-body", R.string.bml_global_config_title)
        ;

        public final String idVal;
        @StringRes public final int configTitleResId;

        GlobalChartId(final String idVal, @StringRes final int configTitleResId) {
            this.idVal = idVal;
            this.configTitleResId = configTitleResId;
        }
    }

    public enum Category {
        WEIGHT (1, EntityType.SET),
        REPS   (2, EntityType.SET),
        REST_TIME (3, EntityType.SET),
        BODY   (4, EntityType.BML)
        ;
        public final EntityType entityType;
        public final int val;

        Category(final int val, final EntityType entityType) {
            this.val = val;
            this.entityType = entityType;
        }

        public static Category categoryForVal(final Integer val) {
            if (val != null) {
                final Category[] categories = Category.values();
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].val == val) {
                        return categories[i];
                    }
                }
            }
            return null;
        }
    }

    public enum AggregateBy {
        DAY       (1,   "by day",       "d MMM"),
        WEEK      (7,   "by week",      "d MMM"),
        MONTH     (30,  "by month",     "MMM"),
        QUARTER   (90,  "by quarter",   "MMM ''yy"),
        HALF_YEAR (180, "by half-year", "MMM ''yy"),
        YEAR      (365, "by year",      "yyyy")
        ;
        public final int val;
        public final String name;
        public final String xaxisDateFormat;

        AggregateBy(final int val, final String name, final String xaxisDateFormat) {
            this.val = val;
            this.name = name;
            this.xaxisDateFormat = xaxisDateFormat;
        }

        public static AggregateBy aggregateByForVal(final Integer val) {
            if (val != null) {
                final AggregateBy[] aggregateBys = AggregateBy.values();
                for (int i = 0; i < aggregateBys.length; i++) {
                    if (aggregateBys[i].val == val) {
                        return aggregateBys[i];
                    }
                }
            }
            return null;
        }

        public static AggregateBy aggregateByForOrdinal(final int ordinal) {
            final AggregateBy[] aggregateBys = AggregateBy.values();
            for (int i = 0; i < aggregateBys.length; i++) {
                if (aggregateBys[i].ordinal() == ordinal) {
                    return aggregateBys[i];
                }
            }
            return null;
        }

        @Override
        public final String toString() {
            return this.name;
        }
    }

    public Integer localIdentifier;
    public Category category;
    public String chartId;
    public Date startDate;
    public Date endDate;
    public boolean boundedEndDate;
    public AggregateBy aggregateBy;
    public boolean suppressPieSliceLabels;
    public boolean isGlobal;
    public Integer loaderId;

    public static final ChartConfig createFromTemplate(final ChartConfig templateConfig, final Chart chart) {
        final ChartConfig chartConfig = new ChartConfig();
        chartConfig.category = templateConfig.category;
        chartConfig.chartId = chart.id;
        chartConfig.startDate = templateConfig.startDate;
        chartConfig.endDate = templateConfig.endDate;
        chartConfig.isGlobal = false;
        chartConfig.localIdentifier = null;
        chartConfig.aggregateBy = templateConfig.aggregateBy;
        chartConfig.suppressPieSliceLabels = templateConfig.suppressPieSliceLabels;
        chartConfig.boundedEndDate = templateConfig.boundedEndDate;
        chartConfig.loaderId = chart.loaderId;
        return chartConfig;
    }

    @Override
    public final String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("chartId: ").append(chartId).append(", loaderId: ").append(loaderId);
        return stringBuilder.toString();
    }
}
