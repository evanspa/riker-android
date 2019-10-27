package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum MuscleAliasColumn {

    ALIAS("alias", ColumnType.TEXT),
    MUSCLE_ID("muscle_id", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    MuscleAliasColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
