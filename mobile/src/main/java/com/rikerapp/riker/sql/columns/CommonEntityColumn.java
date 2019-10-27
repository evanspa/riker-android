package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum CommonEntityColumn {
    CREATED_AT("created_at", ColumnType.INTEGER),
    UPDATED_AT("updated_at", ColumnType.INTEGER),
    DELETED_AT("deleted_at", ColumnType.INTEGER),
    SYNC_IN_PROGRESS("sync_in_progress", ColumnType.INTEGER),
    SYNCED("synced", ColumnType.INTEGER),
    SYNC_HTTP_RESP_CODE("sync_http_resp_code", ColumnType.INTEGER),
    SYNC_HTTP_RESP_ERR_MASK("sync_http_resp_err_mask", ColumnType.INTEGER),
    SYNC_RETRY_AT("sync_http_resp_retry_at", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    CommonEntityColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
