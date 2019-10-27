package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum MovementAliasColumn {

    ALIAS("alias", ColumnType.TEXT),
    MOVEMENT_ID("movement_id", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    MovementAliasColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
