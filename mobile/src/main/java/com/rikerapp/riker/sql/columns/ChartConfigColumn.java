package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum ChartConfigColumn {
    CHART_ID("chart_id", ColumnType.TEXT), // alpha numeric chart identifier (not FK to "chart" cache table)
    CATEGORY("category", ColumnType.INTEGER),
    START_DATE("start_date", ColumnType.INTEGER),
    END_DATE("end_date", ColumnType.INTEGER),
    BOUNDED_END_DATE("bounded_end_date", ColumnType.INTEGER),
    AGGREGATE_BY("aggregate_by", ColumnType.INTEGER),
    SUPPRESS_PIE_SLICE_LABELS("suppress_pie_slice_labels", ColumnType.INTEGER),
    IS_GLOBAL("is_global", ColumnType.INTEGER),
    LOADER_ID("loader_id", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    ChartConfigColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
