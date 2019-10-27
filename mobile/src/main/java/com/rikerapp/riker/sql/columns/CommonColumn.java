package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum CommonColumn {
    LOCAL_ID("id", ColumnType.INTEGER),
    GLOBAL_ID("global_identifier", ColumnType.TEXT),
    USER_ID("user_id", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    CommonColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
