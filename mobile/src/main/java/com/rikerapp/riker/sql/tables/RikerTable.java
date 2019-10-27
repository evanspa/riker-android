package com.rikerapp.riker.sql.tables;

public enum RikerTable {
    USER("user"),
    USER_SETTINGS("user_settings"),
    BODY_SEGMENT("body_segment"),
    MUSCLE_GROUP("muscle_group"),
    MUSCLE("muscle"),
    MUSCLE_ALIAS("muscle_alias"),
    MOVEMENT("movement"),
    MOVEMENT_VARIANT("movement_variant"),
    MOVEMENT_PRIMARY_MUSCLE("movement_primary_muscle"),
    MOVEMENT_SECONDARY_MUSCLE("movement_secondary_muscle"),
    MOVEMENT_ALIAS("movement_alias"),
    ORIGINATION_DEVICE("origination_device"),
    SET("rset"), // "set" is reserved word in Sqlite
    BML("bml"),
    CHART_CONFIG("chart_config"),
    CHART("chart"),
    CHART_PIE_SLICE("chart_pie_slice"),
    CHART_TIME_SERIES("chart_time_series"),
    CHART_TIME_SERIES_DATA_POINT("chart_time_series_data_point")
    ;

    public final String tableName;

    RikerTable(final String tableName) {
        this.tableName = tableName;
    }
}
