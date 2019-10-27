package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum SetColumn {

    MOVEMENT_ID("movement_id", ColumnType.INTEGER),
    MOVEMENT_VARIANT_ID("movement_variant_id", ColumnType.INTEGER),
    NUM_REPS("num_reps", ColumnType.INTEGER),
    WEIGHT("weight", ColumnType.REAL),
    WEIGHT_UOM("weight_uom", ColumnType.INTEGER),
    NEGATIVES("negatives", ColumnType.INTEGER),
    TO_FAILURE("to_failure", ColumnType.INTEGER),
    LOGGED_AT("logged_at", ColumnType.INTEGER),
    IGNORE_TIME("ignore_time", ColumnType.INTEGER),
    ORIGINATION_DEVICE_ID("origination_device_id", ColumnType.INTEGER),
    IMPORTED_AT("imported_at", ColumnType.INTEGER),
    CORRELATION_GUID("correlation_guid", ColumnType.TEXT)
    ;

    public final String name;
    public final ColumnType type;

    SetColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
