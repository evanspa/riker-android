package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum ChartTimeSeriesDataPointColumn {
    CHART_LOCAL_ID("chart_id", ColumnType.INTEGER), // FK to "chart" cache table/LOCAL_ID column
    CHART_TIME_SERIES_ID("chart_time_series_id", ColumnType.INTEGER), // FK to "chart_time_series" cache table
    DATE("date", ColumnType.INTEGER),
    VALUE("value", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    ChartTimeSeriesDataPointColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
