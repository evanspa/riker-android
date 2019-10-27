package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum ChartPieSliceColumn {
    CHART_LOCAL_ID("chart_id", ColumnType.INTEGER), // FK to "chart" cache table/LOCAL_ID column
    VALUE("value", ColumnType.REAL)
    ;

    public final String name;
    public final ColumnType type;

    ChartPieSliceColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
