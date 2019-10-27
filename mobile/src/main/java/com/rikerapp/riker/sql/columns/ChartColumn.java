package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum ChartColumn {
    CHART_ID("chart_id", ColumnType.TEXT), // alpha-numeric chart identifier string (not its PK column...PK column is LOCAL_ID)
    CATEGORY("category", ColumnType.INTEGER),
    CONFIG_ID("config_id", ColumnType.INTEGER), // FK to chart_config's 'LOCAL_ID' column (not its 'chart_id' column)
    AGGREGATE_BY("aggregate_by", ColumnType.INTEGER),
    XAXIS_LABEL_COUNT("xaxis_label_count", ColumnType.INTEGER),
    MAX_VALUE("max_value", ColumnType.REAL)
    ;

    public final String name;
    public final ColumnType type;

    ChartColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
