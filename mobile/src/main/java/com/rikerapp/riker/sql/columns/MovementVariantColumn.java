package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum MovementVariantColumn {

    NAME("getName", ColumnType.TEXT),
    ABBREV_NAME("abbrev_name", ColumnType.TEXT),
    DESCRIPTION("description", ColumnType.TEXT),
    SORT_ORDER("sort_order", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    MovementVariantColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
