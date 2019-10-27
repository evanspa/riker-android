package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum CommonMainColumn {

    MASTER_UPDATED_AT("master_updated_at", ColumnType.INTEGER),
    DATE_COPIED_DOWN_FROM_MASTER("date_copied_down_from_master", ColumnType.INTEGER),
    EDIT_IN_PROGRESS("edit_in_progress", ColumnType.INTEGER),
    SYNC_IN_PROGRESS("sync_in_progress", ColumnType.INTEGER),
    SYNCED("synced", ColumnType.INTEGER),
    EDIT_COUNT("edit_count", ColumnType.INTEGER),
    SYNC_HTTP_RESP_CODE("sync_http_resp_code", ColumnType.INTEGER),
    SYNC_HTTP_RESP_ERR_MASK("sync_http_resp_err_mask", ColumnType.INTEGER),
    SYNC_RETRY_AT("sync_http_resp_retry_at", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    CommonMainColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
