package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum BmlColumn {

    LOGGED_AT("logged_at", ColumnType.INTEGER),
    BODY_WEIGHT("body_weight", ColumnType.REAL),
    BODY_WEIGHT_UOM("body_weight_uom", ColumnType.INTEGER),
    SIZE_UOM("size_uom", ColumnType.INTEGER),
    ARM_SIZE("arm_size", ColumnType.REAL),
    CALF_SIZE("calf_size", ColumnType.REAL),
    CHEST_SIZE("chest_size", ColumnType.REAL),
    NECK_SIZE("neck_size", ColumnType.REAL),
    WAIST_SIZE("waist_size", ColumnType.REAL),
    FOREARM_SIZE("forearm_size", ColumnType.REAL),
    THIGH_SIZE("thigh_size", ColumnType.REAL),
    ORIGINATION_DEVICE_ID("origination_device_id", ColumnType.INTEGER),
    IMPORTED_AT("imported_at", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    BmlColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
