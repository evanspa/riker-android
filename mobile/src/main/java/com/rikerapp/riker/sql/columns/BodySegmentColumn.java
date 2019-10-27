package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum BodySegmentColumn {

    NAME("getName", ColumnType.TEXT)
    ;

    public final String name;
    public final ColumnType type;

    BodySegmentColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
