package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum MuscleColumn {

    CANONICAL_NAME("canonical_name", ColumnType.TEXT),
    ABBREV_CANONICAL_NAME("abbrev_canonical_name", ColumnType.TEXT),
    MUSCLE_GROUP_ID("muscle_group_id", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    MuscleColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
