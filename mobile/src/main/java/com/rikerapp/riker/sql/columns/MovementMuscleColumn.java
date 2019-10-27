package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum MovementMuscleColumn {

    MOVEMENT_ID("movement_id", ColumnType.INTEGER),
    MUSCLE_ID("muscle_id", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    MovementMuscleColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
