package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum ChartTimeSeriesColumn {
    CHART_LOCAL_ID("chart_id", ColumnType.INTEGER), // FK to "chart" cache table/LOCAL_ID column
    ENTITY_LMID("entity_lmid", ColumnType.INTEGER),
    LABEL("label", ColumnType.TEXT)
    ;

    public final String name;
    public final ColumnType type;

    ChartTimeSeriesColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
