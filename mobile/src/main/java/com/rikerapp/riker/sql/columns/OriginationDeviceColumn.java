package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum OriginationDeviceColumn {

    NAME("getName", ColumnType.TEXT),
    HAS_LOCAL_IMAGE("has_local_image", ColumnType.INTEGER),
    ICON_IMAGE_NAME("icon_image_name", ColumnType.TEXT)
    ;

    public final String name;
    public final ColumnType type;

    OriginationDeviceColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
