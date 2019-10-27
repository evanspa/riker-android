package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum MovementColumn {

    CANONICAL_NAME("canonical_name", ColumnType.TEXT),
    IS_BODY_LIFT("is_body_lift", ColumnType.INTEGER),
    PERCENTAGE_OF_BODY_WEIGHT("percentage_of_body_weight", ColumnType.TEXT),
    VARIANT_MASK("variant_mask", ColumnType.INTEGER),
    SORT_ORDER("sort_order", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    MovementColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
