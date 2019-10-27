package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum UserSettingsColumn {
    WEIGHT_UOM("weight_uom", ColumnType.INTEGER),
    SIZE_UOM("size_uom", ColumnType.INTEGER),
    WEIGHT_INC_DEC_AMOUNT("weight_inc_dec_amount", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    UserSettingsColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
