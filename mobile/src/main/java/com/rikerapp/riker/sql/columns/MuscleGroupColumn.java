package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum MuscleGroupColumn {

    NAME("getName", ColumnType.TEXT),
    ABBREV_NAME("abbrev_name", ColumnType.TEXT),
    BODY_SEGMENT_ID("body_segment_id", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    MuscleGroupColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
