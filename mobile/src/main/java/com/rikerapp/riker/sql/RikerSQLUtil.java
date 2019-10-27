package com.rikerapp.riker.sql;

import com.rikerapp.riker.sql.columns.BmlColumn;
import com.rikerapp.riker.sql.columns.BodySegmentColumn;
import com.rikerapp.riker.sql.columns.ChartColumn;
import com.rikerapp.riker.sql.columns.ChartConfigColumn;
import com.rikerapp.riker.sql.columns.ChartPieSliceColumn;
import com.rikerapp.riker.sql.columns.ChartTimeSeriesColumn;
import com.rikerapp.riker.sql.columns.ChartTimeSeriesDataPointColumn;
import com.rikerapp.riker.sql.columns.CommonColumn;
import com.rikerapp.riker.sql.columns.CommonMainColumn;
import com.rikerapp.riker.sql.columns.CommonEntityColumn;
import com.rikerapp.riker.sql.columns.MovementAliasColumn;
import com.rikerapp.riker.sql.columns.MovementColumn;
import com.rikerapp.riker.sql.columns.MovementMuscleColumn;
import com.rikerapp.riker.sql.columns.MovementVariantColumn;
import com.rikerapp.riker.sql.columns.MuscleAliasColumn;
import com.rikerapp.riker.sql.columns.MuscleColumn;
import com.rikerapp.riker.sql.columns.MuscleGroupColumn;
import com.rikerapp.riker.sql.columns.OriginationDeviceColumn;
import com.rikerapp.riker.sql.columns.SetColumn;
import com.rikerapp.riker.sql.columns.UserColumn;
import com.rikerapp.riker.sql.columns.UserSettingsColumn;
import com.rikerapp.riker.sql.tables.RikerTable;

public final class RikerSQLUtil {

    private RikerSQLUtil() {}

    //public static final int RIKER_CURRENT_DATABASE_VERSION = 1; // released to Google Play on 12/05/2018
    public static final int RIKER_CURRENT_DATABASE_VERSION = 2; // released to Google Play on TBD

    public static final String RIKER_DATABASE_FILE_NAME = "riker.sqlite";

    /* ==========================================================================================
     *   Helpers
     * ========================================================================================== */
    private static void appendTableCreate(final StringBuilder ddl, final String tableName) {
        ddl.append(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));
    }

    private static void appendColumn(final StringBuilder ddl, final String name, final ColumnType type, final boolean nil, final boolean last) {
        ddl.append(String.format("%s %s%s%s", name, type, nil ? "" : " NOT NULL", last ? "" : ", "));
    }

    private static void appendNilColumn(final StringBuilder ddl, final String name, final ColumnType type, final boolean last) {
        appendColumn(ddl, name, type, true, last);
    }

    private static void appendNotNilColumn(final StringBuilder ddl, final String name, final ColumnType type, final boolean last) {
        appendColumn(ddl, name, type, false, last);
    }

    private static void appendNilColumn(final StringBuilder ddl, final UserColumn userColumn) { appendNilColumn(ddl, userColumn, false); }
    private static void appendNilColumn(final StringBuilder ddl, final UserColumn userColumn, final boolean last) { appendNilColumn(ddl, userColumn.name, userColumn.type, last); }
    private static void appendNilColumn(final StringBuilder ddl, final CommonColumn column, final boolean last) { appendNilColumn(ddl, column.name, column.type, last); }
    private static void appendNilColumn(final StringBuilder ddl, final CommonMainColumn mainColumn) { appendNilColumn(ddl, mainColumn, false); }
    private static void appendNilColumn(final StringBuilder ddl, final CommonMainColumn mainColumn, final boolean last) { appendNilColumn(ddl, mainColumn.name, mainColumn.type, last); }
    private static void appendNilColumn(final StringBuilder ddl, final CommonEntityColumn masterColumn, final boolean last) { appendNilColumn(ddl, masterColumn.name, masterColumn.type, last); }
    private static void appendNilColumn(final StringBuilder ddl, final SetColumn setColumn) { appendNilColumn(ddl, setColumn, false); }
    private static void appendNilColumn(final StringBuilder ddl, final SetColumn setColumn, final boolean last) { appendNilColumn(ddl, setColumn.name, setColumn.type, last); }
    private static void appendNotNilColumn(final StringBuilder ddl, final SetColumn setColumn) { appendNotNilColumn(ddl, setColumn, false); }
    private static void appendNotNilColumn(final StringBuilder ddl, final SetColumn setColumn, final boolean last) { appendNotNilColumn(ddl, setColumn.name, setColumn.type, last); }
    private static void appendNilColumn(final StringBuilder ddl, final BmlColumn bmlColumn) { appendNilColumn(ddl, bmlColumn, false); }
    private static void appendNilColumn(final StringBuilder ddl, final BmlColumn bmlColumn, final boolean last) { appendNilColumn(ddl, bmlColumn.name, bmlColumn.type, last); }
    private static void appendNotNilColumn(final StringBuilder ddl, final BmlColumn bmlColumn) { appendNotNilColumn(ddl, bmlColumn, false); }
    private static void appendNotNilColumn(final StringBuilder ddl, final BmlColumn bmlColumn, final boolean last) { appendNotNilColumn(ddl, bmlColumn.name, bmlColumn.type, last); }
    private static void appendNilColumn(final StringBuilder ddl, final ChartConfigColumn chartConfigColumn) { appendNilColumn(ddl, chartConfigColumn, false); }
    private static void appendNilColumn(final StringBuilder ddl, final ChartConfigColumn chartConfigColumn, final boolean last) { appendNilColumn(ddl, chartConfigColumn.name, chartConfigColumn.type, last); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartConfigColumn chartConfigColumn) { appendNotNilColumn(ddl, chartConfigColumn, false); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartConfigColumn chartConfigColumn, final boolean last) { appendNotNilColumn(ddl, chartConfigColumn.name, chartConfigColumn.type, last); }
    private static void appendNilColumn(final StringBuilder ddl, final ChartColumn chartConfigColumn) { appendNilColumn(ddl, chartConfigColumn, false); }
    private static void appendNilColumn(final StringBuilder ddl, final ChartColumn chartConfigColumn, final boolean last) { appendNilColumn(ddl, chartConfigColumn.name, chartConfigColumn.type, last); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartColumn chartConfigColumn) { appendNotNilColumn(ddl, chartConfigColumn, false); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartColumn chartConfigColumn, final boolean last) { appendNotNilColumn(ddl, chartConfigColumn.name, chartConfigColumn.type, last); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartPieSliceColumn chartPieSliceColumn) { appendNotNilColumn(ddl, chartPieSliceColumn, false); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartPieSliceColumn chartPieSliceColumn, final boolean last) { appendNotNilColumn(ddl, chartPieSliceColumn.name, chartPieSliceColumn.type, last); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartTimeSeriesColumn chartTimeSeriesColumn) { appendNotNilColumn(ddl, chartTimeSeriesColumn, false); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartTimeSeriesColumn chartTimeSeriesColumn, final boolean last) { appendNotNilColumn(ddl, chartTimeSeriesColumn.name, chartTimeSeriesColumn.type, last); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartTimeSeriesDataPointColumn chartTimeSeriesDataPointColumn) { appendNotNilColumn(ddl, chartTimeSeriesDataPointColumn, false); }
    private static void appendNotNilColumn(final StringBuilder ddl, final ChartTimeSeriesDataPointColumn chartTimeSeriesDataPointColumn, final boolean last) { appendNotNilColumn(ddl, chartTimeSeriesDataPointColumn.name, chartTimeSeriesDataPointColumn.type, last); }

    private static void appendCommonMasterColumns(final StringBuilder ddl, final boolean last, final boolean nullable) {
        ddl.append(String.format("%s %s%s, ", CommonEntityColumn.CREATED_AT.name, CommonEntityColumn.CREATED_AT.type, nullable ? "" : " NOT NULL"));
        ddl.append(String.format("%s %s%s, ", CommonEntityColumn.UPDATED_AT.name, CommonEntityColumn.UPDATED_AT.type, nullable ? "" : " NOT NULL"));
        appendNilColumn(ddl, CommonMainColumn.SYNC_IN_PROGRESS);
        appendNilColumn(ddl, CommonMainColumn.SYNCED);
        appendNilColumn(ddl, CommonMainColumn.SYNC_HTTP_RESP_CODE);
        appendNilColumn(ddl, CommonMainColumn.SYNC_HTTP_RESP_ERR_MASK);
        appendNilColumn(ddl, CommonMainColumn.SYNC_RETRY_AT, last);
        appendNilColumn(ddl, CommonEntityColumn.DELETED_AT, last);
    }

    private static void appendLocalAndGlobalIds(final StringBuilder ddl, final boolean nullGlobalId) {
        ddl.append(String.format("%s %s PRIMARY KEY, ", CommonColumn.LOCAL_ID.name, CommonColumn.LOCAL_ID.type));
        ddl.append(String.format("%s %s UNIQUE%s, ", CommonColumn.GLOBAL_ID.name, CommonColumn.GLOBAL_ID.type, nullGlobalId ? "" : " NOT NULL"));
    }

    private static void appendNonNilMasterUserId(final StringBuilder ddl) {
        appendColumn(ddl, CommonColumn.USER_ID.name, CommonColumn.USER_ID.type, false, false);
    }

    private static void appendFkToMasterUser(final StringBuilder ddl, final boolean last) {
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)%s",
                CommonColumn.USER_ID.name,
                RikerTable.USER.tableName,
                CommonColumn.LOCAL_ID.name,
                last ? "" : ", "));
    }

    /* ==========================================================================================
     *   Version 1 DDL
     * ========================================================================================== */
    private static void appendChartDomainColumns(final StringBuilder ddl) {
        ddl.append(String.format("%s %s UNIQUE NOT NULL, ", ChartColumn.CHART_ID.name, ChartColumn.CHART_ID.type));
        appendNotNilColumn(ddl, ChartColumn.CATEGORY);
        appendNilColumn(ddl, ChartColumn.CONFIG_ID);
        appendNotNilColumn(ddl, ChartColumn.AGGREGATE_BY);
        appendNotNilColumn(ddl, ChartColumn.XAXIS_LABEL_COUNT);
        appendNotNilColumn(ddl, ChartColumn.MAX_VALUE);
    }

    public static String v1_chartDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.CHART.tableName);
        ddl.append(String.format("%s %s PRIMARY KEY, ", CommonColumn.LOCAL_ID.name, CommonColumn.LOCAL_ID.type));
        appendNonNilMasterUserId(ddl);
        appendChartDomainColumns(ddl);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s),",
                ChartColumn.CONFIG_ID.name,
                RikerTable.CHART_CONFIG.tableName,
                CommonColumn.LOCAL_ID.name));
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendChartPieSliceDomainColumns(final StringBuilder ddl) {
        appendNotNilColumn(ddl, ChartPieSliceColumn.CHART_LOCAL_ID);
        appendNotNilColumn(ddl, ChartPieSliceColumn.VALUE);
    }

    public static String v1_chartPieSliceDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.CHART_PIE_SLICE.tableName);
        ddl.append(String.format("%s %s PRIMARY KEY, ", CommonColumn.LOCAL_ID.name, CommonColumn.LOCAL_ID.type));
        appendNonNilMasterUserId(ddl);
        appendChartPieSliceDomainColumns(ddl);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s),",
                ChartPieSliceColumn.CHART_LOCAL_ID.name,
                RikerTable.CHART.tableName,
                CommonColumn.LOCAL_ID.name));
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendChartTimeSeriesDomain(final StringBuilder ddl) {
        appendNotNilColumn(ddl, ChartTimeSeriesColumn.CHART_LOCAL_ID);
        appendNotNilColumn(ddl, ChartTimeSeriesColumn.ENTITY_LMID);
        appendNotNilColumn(ddl, ChartTimeSeriesColumn.LABEL);
    }

    public static String v1_chartTimeSeriesDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.CHART_TIME_SERIES.tableName);
        ddl.append(String.format("%s %s PRIMARY KEY, ", CommonColumn.LOCAL_ID.name, CommonColumn.LOCAL_ID.type));
        appendNonNilMasterUserId(ddl);
        appendChartTimeSeriesDomain(ddl);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s),",
                ChartTimeSeriesColumn.CHART_LOCAL_ID.name,
                RikerTable.CHART.tableName,
                CommonColumn.LOCAL_ID.name));
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendChartTimeSeriesDataPointDomain(final StringBuilder ddl) {
        appendNotNilColumn(ddl, ChartTimeSeriesDataPointColumn.CHART_LOCAL_ID);
        appendNotNilColumn(ddl, ChartTimeSeriesDataPointColumn.CHART_TIME_SERIES_ID);
        appendNotNilColumn(ddl, ChartTimeSeriesDataPointColumn.DATE);
        appendNotNilColumn(ddl, ChartTimeSeriesDataPointColumn.VALUE);
    }

    public static String v1_chartTimeSeriesDataPointDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName);
        ddl.append(String.format("%s %s PRIMARY KEY, ", CommonColumn.LOCAL_ID.name, CommonColumn.LOCAL_ID.type));
        appendNonNilMasterUserId(ddl);
        appendChartTimeSeriesDataPointDomain(ddl);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s),",
                ChartTimeSeriesDataPointColumn.CHART_LOCAL_ID.name,
                RikerTable.CHART.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s),",
                ChartTimeSeriesDataPointColumn.CHART_TIME_SERIES_ID.name,
                RikerTable.CHART_TIME_SERIES.tableName,
                CommonColumn.LOCAL_ID.name));
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendChartConfigDomainColumns(final StringBuilder ddl) {
        ddl.append(String.format("%s %s UNIQUE NOT NULL, ", ChartConfigColumn.CHART_ID.name, ChartConfigColumn.CHART_ID.type));
        appendNotNilColumn(ddl, ChartConfigColumn.CATEGORY);
        appendNotNilColumn(ddl, ChartConfigColumn.START_DATE);
        appendNilColumn(ddl, ChartConfigColumn.END_DATE);
        appendNotNilColumn(ddl, ChartConfigColumn.BOUNDED_END_DATE);
        appendNotNilColumn(ddl, ChartConfigColumn.AGGREGATE_BY);
        appendNotNilColumn(ddl, ChartConfigColumn.SUPPRESS_PIE_SLICE_LABELS);
        appendNotNilColumn(ddl, ChartConfigColumn.IS_GLOBAL);
        appendNilColumn(ddl, ChartConfigColumn.LOADER_ID);
    }

    public static String v1_chartConfigDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.CHART_CONFIG.tableName);
        ddl.append(String.format("%s %s PRIMARY KEY, ", CommonColumn.LOCAL_ID.name, CommonColumn.LOCAL_ID.type));
        appendNonNilMasterUserId(ddl);
        appendChartConfigDomainColumns(ddl);
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendBmlDomainColumns(final StringBuilder ddl, final boolean last) {
        appendNotNilColumn(ddl, BmlColumn.LOGGED_AT);
        appendNilColumn(ddl, BmlColumn.BODY_WEIGHT);
        appendNilColumn(ddl, BmlColumn.BODY_WEIGHT_UOM);
        appendNilColumn(ddl, BmlColumn.ARM_SIZE);
        appendNilColumn(ddl, BmlColumn.CALF_SIZE);
        appendNilColumn(ddl, BmlColumn.CHEST_SIZE);
        appendNilColumn(ddl, BmlColumn.NECK_SIZE);
        appendNilColumn(ddl, BmlColumn.WAIST_SIZE);
        appendNilColumn(ddl, BmlColumn.THIGH_SIZE);
        appendNilColumn(ddl, BmlColumn.FOREARM_SIZE);
        appendNilColumn(ddl, BmlColumn.SIZE_UOM);
        appendNotNilColumn(ddl, BmlColumn.ORIGINATION_DEVICE_ID);
        appendNilColumn(ddl, BmlColumn.IMPORTED_AT);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)%s",
                BmlColumn.ORIGINATION_DEVICE_ID.name,
                RikerTable.ORIGINATION_DEVICE.tableName,
                CommonColumn.LOCAL_ID.name,
                last ? "" : ", "));
    }

    public static String v1_masterBmlDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.BML.tableName);
        appendLocalAndGlobalIds(ddl, true); // 'true' here means null global ID column
        appendNonNilMasterUserId(ddl);
        appendCommonMasterColumns(ddl, false, true);
        appendBmlDomainColumns(ddl, false);
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendSetDomainColumns(final StringBuilder ddl, final boolean last) {
        appendNotNilColumn(ddl, SetColumn.MOVEMENT_ID);
        appendNilColumn(ddl, SetColumn.MOVEMENT_VARIANT_ID);
        appendNotNilColumn(ddl, SetColumn.NUM_REPS);
        appendNotNilColumn(ddl, SetColumn.WEIGHT);
        appendNotNilColumn(ddl, SetColumn.WEIGHT_UOM);
        appendNotNilColumn(ddl, SetColumn.NEGATIVES);
        appendNotNilColumn(ddl, SetColumn.TO_FAILURE);
        appendNotNilColumn(ddl, SetColumn.LOGGED_AT);
        appendNotNilColumn(ddl, SetColumn.IGNORE_TIME);
        appendNotNilColumn(ddl, SetColumn.ORIGINATION_DEVICE_ID);
        appendNilColumn(ddl, SetColumn.IMPORTED_AT);
        appendNilColumn(ddl, SetColumn.CORRELATION_GUID);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s), ",
                SetColumn.MOVEMENT_ID.name,
                RikerTable.MOVEMENT.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s), ",
                SetColumn.ORIGINATION_DEVICE_ID.name,
                RikerTable.ORIGINATION_DEVICE.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)%s",
                SetColumn.MOVEMENT_VARIANT_ID.name,
                RikerTable.MOVEMENT_VARIANT.tableName,
                CommonColumn.LOCAL_ID.name,
                last ? "" : ", "));
    }

    public static String v1_masterSetDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.SET.tableName);
        appendLocalAndGlobalIds(ddl, true); // 'true' here means null global ID column
        appendNonNilMasterUserId(ddl);
        appendCommonMasterColumns(ddl, false, true);
        appendSetDomainColumns(ddl, false);
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterOriginationDeviceDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.ORIGINATION_DEVICE.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, OriginationDeviceColumn.NAME.name, OriginationDeviceColumn.NAME.type, false, false); // non null and not last
        appendColumn(ddl, OriginationDeviceColumn.HAS_LOCAL_IMAGE.name, OriginationDeviceColumn.HAS_LOCAL_IMAGE.type, false, false);
        appendColumn(ddl, OriginationDeviceColumn.ICON_IMAGE_NAME.name, OriginationDeviceColumn.ICON_IMAGE_NAME.type, false, true);
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMovementAliasDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MOVEMENT_ALIAS.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, MovementAliasColumn.ALIAS.name, MovementAliasColumn.ALIAS.type, false, false); // non null and not last
        appendColumn(ddl, MovementAliasColumn.MOVEMENT_ID.name, MovementAliasColumn.MOVEMENT_ID.type, false, false);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)",
                MovementAliasColumn.MOVEMENT_ID.name,
                RikerTable.MOVEMENT.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMovementSecondaryMuscleDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MOVEMENT_SECONDARY_MUSCLE.tableName);
        appendColumn(ddl, MovementMuscleColumn.MUSCLE_ID.name, MovementMuscleColumn.MUSCLE_ID.type, false, false); // non null and not last
        appendColumn(ddl, MovementMuscleColumn.MOVEMENT_ID.name, MovementMuscleColumn.MOVEMENT_ID.type, false, false);
        ddl.append(String.format("PRIMARY KEY (%s, %s), ", MovementMuscleColumn.MUSCLE_ID.name, MovementMuscleColumn.MOVEMENT_ID.name));
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s), ",
                MovementMuscleColumn.MUSCLE_ID.name,
                RikerTable.MUSCLE.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)",
                MovementMuscleColumn.MOVEMENT_ID.name,
                RikerTable.MOVEMENT.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMovementPrimaryMuscleDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MOVEMENT_PRIMARY_MUSCLE.tableName);
        appendColumn(ddl, MovementMuscleColumn.MUSCLE_ID.name, MovementMuscleColumn.MUSCLE_ID.type, false, false); // non null and not last
        appendColumn(ddl, MovementMuscleColumn.MOVEMENT_ID.name, MovementMuscleColumn.MOVEMENT_ID.type, false, false);
        ddl.append(String.format("PRIMARY KEY (%s, %s), ", MovementMuscleColumn.MUSCLE_ID.name, MovementMuscleColumn.MOVEMENT_ID.name));
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s), ",
                MovementMuscleColumn.MUSCLE_ID.name,
                RikerTable.MUSCLE.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)",
                MovementMuscleColumn.MOVEMENT_ID.name,
                RikerTable.MOVEMENT.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMovementVariantDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MOVEMENT_VARIANT.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, MovementVariantColumn.NAME.name, MovementVariantColumn.NAME.type, false, false); // non null and not last
        appendColumn(ddl, MovementVariantColumn.ABBREV_NAME.name, MovementVariantColumn.ABBREV_NAME.type, true, false); // null and not last
        appendColumn(ddl, MovementVariantColumn.DESCRIPTION.name, MovementVariantColumn.DESCRIPTION.type, true, false);
        appendColumn(ddl, MovementVariantColumn.SORT_ORDER.name, MovementVariantColumn.SORT_ORDER.type, false, true);
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMovementDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MOVEMENT.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, MovementColumn.CANONICAL_NAME.name, MovementColumn.CANONICAL_NAME.type, false, false); // non null and not last
        appendColumn(ddl, MovementColumn.IS_BODY_LIFT.name, MovementColumn.IS_BODY_LIFT.type, false, false);
        appendColumn(ddl, MovementColumn.PERCENTAGE_OF_BODY_WEIGHT.name, MovementColumn.PERCENTAGE_OF_BODY_WEIGHT.type, true, false);
        appendColumn(ddl, MovementColumn.VARIANT_MASK.name, MovementColumn.VARIANT_MASK.type, false, false);
        appendColumn(ddl, MovementColumn.SORT_ORDER.name, MovementColumn.SORT_ORDER.type, false, true);
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMuscleAliasDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MUSCLE_ALIAS.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, MuscleAliasColumn.ALIAS.name, MuscleAliasColumn.ALIAS.type, false, false); // non null and not last
        appendColumn(ddl, MuscleAliasColumn.MUSCLE_ID.name, MuscleAliasColumn.MUSCLE_ID.type, false, false);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)",
                MuscleAliasColumn.MUSCLE_ID.name,
                RikerTable.MUSCLE.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMuscleDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MUSCLE.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, MuscleColumn.CANONICAL_NAME.name, MuscleColumn.CANONICAL_NAME.type, false, false); // non null and not last
        appendColumn(ddl, MuscleColumn.ABBREV_CANONICAL_NAME.name, MuscleColumn.ABBREV_CANONICAL_NAME.type, true, false);
        appendColumn(ddl, MuscleColumn.MUSCLE_GROUP_ID.name, MuscleColumn.MUSCLE_GROUP_ID.type, false, false);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)",
                MuscleColumn.MUSCLE_GROUP_ID.name,
                RikerTable.MUSCLE_GROUP.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterMuscleGroupDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.MUSCLE_GROUP.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, MuscleGroupColumn.NAME.name, MuscleGroupColumn.NAME.type, false, false); // non null and not last
        appendColumn(ddl, MuscleGroupColumn.ABBREV_NAME.name, MuscleGroupColumn.ABBREV_NAME.type, true, false);
        appendColumn(ddl, MuscleGroupColumn.BODY_SEGMENT_ID.name, MuscleGroupColumn.BODY_SEGMENT_ID.type, false, false);
        ddl.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s)",
                MuscleGroupColumn.BODY_SEGMENT_ID.name,
                RikerTable.BODY_SEGMENT.tableName,
                CommonColumn.LOCAL_ID.name));
        ddl.append(")");
        return ddl.toString();
    }

    public static String v1_masterBodySegmentDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.BODY_SEGMENT.tableName);
        appendLocalAndGlobalIds(ddl, false);
        appendCommonMasterColumns(ddl, false, false);
        appendColumn(ddl, BodySegmentColumn.NAME.name, BodySegmentColumn.NAME.type, false, true); // non null and last
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendUserSettingsDomainColumns(final StringBuilder ddl, final boolean last) {
        appendColumn(ddl, UserSettingsColumn.WEIGHT_UOM.name, UserSettingsColumn.WEIGHT_UOM.type, false, false);
        appendColumn(ddl, UserSettingsColumn.SIZE_UOM.name, UserSettingsColumn.SIZE_UOM.type, false, false);
        appendColumn(ddl, UserSettingsColumn.WEIGHT_INC_DEC_AMOUNT.name, UserSettingsColumn.WEIGHT_INC_DEC_AMOUNT.type, false, last);
    }

    public static String v1_masterUserSettingsDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.USER_SETTINGS.tableName);
        appendLocalAndGlobalIds(ddl, true);
        appendNonNilMasterUserId(ddl);
        appendCommonMasterColumns(ddl, false, true);
        appendUserSettingsDomainColumns(ddl, false);
        appendFkToMasterUser(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }

    private static void appendUserDomainColumns(final StringBuilder ddl, final boolean last) {
        appendNilColumn(ddl, UserColumn.NAME);
        appendNilColumn(ddl, UserColumn.EMAIL);
        appendNilColumn(ddl, UserColumn.PASSWORD_HASH);
        appendNilColumn(ddl, UserColumn.VERIFIED_AT);
        appendNilColumn(ddl, UserColumn.LAST_CHARGE_ID);
        appendNilColumn(ddl, UserColumn.TRIAL_ALMOST_EXPIRED_NOTICE_SENT_AT);
        appendNilColumn(ddl, UserColumn.LATEST_STRIPE_TOKEN_ID);
        appendNilColumn(ddl, UserColumn.NEXT_INVOICE_AT);
        appendNilColumn(ddl, UserColumn.NEXT_INVOICE_AMOUNT);
        appendNilColumn(ddl, UserColumn.LAST_INVOICE_AT);
        appendNilColumn(ddl, UserColumn.LAST_INVOICE_AMOUNT);
        appendNilColumn(ddl, UserColumn.CURRENT_CARD_LAST4);
        appendNilColumn(ddl, UserColumn.CURRENT_CARD_BRAND);
        appendNilColumn(ddl, UserColumn.CURRENT_CARD_EXP_MONTH);
        appendNilColumn(ddl, UserColumn.CURRENT_CARD_EXP_YEAR);
        appendNilColumn(ddl, UserColumn.TRIAL_ENDS_AT);
        appendNilColumn(ddl, UserColumn.STRIPE_CUSTOMER_ID);
        appendNilColumn(ddl, UserColumn.PAID_ENROLLMENT_ESTABLISHED_AT);
        appendNilColumn(ddl, UserColumn.NEW_MOVEMENTS_ADDED_AT);
        appendNilColumn(ddl, UserColumn.INFORMED_OF_MAINTENANCE_AT);
        appendNilColumn(ddl, UserColumn.MAINTENANCE_STARTS_AT);
        appendNilColumn(ddl, UserColumn.MAINTENANCE_DURATION);
        appendNilColumn(ddl, UserColumn.IS_PAYMENT_PAST_DUE);
        appendNilColumn(ddl, UserColumn.PAID_ENROLLMENT_CANCELLED_AT);
        appendNilColumn(ddl, UserColumn.FINAL_FAILED_PAYMENT_ATTEMPT_OCCURRED_AT);
        appendNilColumn(ddl, UserColumn.VALIDATE_APP_STORE_RECEIPT_AT);
        appendNilColumn(ddl, UserColumn.MAX_ALLOWED_SET_IMPORT);
        appendNilColumn(ddl, UserColumn.MAX_ALLOWED_BML_IMPORT, last);
    }

    public static String v1_masterUserDDL() {
        final StringBuilder ddl = new StringBuilder();
        appendTableCreate(ddl, RikerTable.USER.tableName);
        appendLocalAndGlobalIds(ddl, true);
        appendCommonMasterColumns(ddl, false, true);
        appendUserDomainColumns(ddl, true);
        ddl.append(")");
        return ddl.toString();
    }
}
