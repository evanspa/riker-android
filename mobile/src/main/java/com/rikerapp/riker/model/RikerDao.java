package com.rikerapp.riker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.rikerapp.riker.Constants;
import com.rikerapp.riker.Function;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.mpandroidchart.RLineDataSet;
import com.rikerapp.riker.sql.SqliteUtil;
import com.rikerapp.riker.sql.columns.BmlColumn;
import com.rikerapp.riker.sql.columns.BodySegmentColumn;
import com.rikerapp.riker.sql.columns.ChartColumn;
import com.rikerapp.riker.sql.columns.ChartConfigColumn;
import com.rikerapp.riker.sql.columns.ChartPieSliceColumn;
import com.rikerapp.riker.sql.columns.ChartTimeSeriesColumn;
import com.rikerapp.riker.sql.columns.ChartTimeSeriesDataPointColumn;
import com.rikerapp.riker.sql.columns.CommonColumn;
import com.rikerapp.riker.sql.columns.CommonEntityColumn;
import com.rikerapp.riker.sql.columns.CommonMainColumn;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.rikerapp.riker.model.RikerSQLiteOpenHelper.setDefaultCreatedAtUpdatedAtDates;

public final class RikerDao {

    private static final String EMPTY_SELECTION_ARGS[] = new String[] {};
    private static final int SECONDS_IN_HOUR = 3600;

    public RikerSQLiteOpenHelper dbHelper;

    public RikerDao(final Context context) {
        dbHelper = new RikerSQLiteOpenHelper(context);
        // a very small number of users experience a crash for the dbHelper.getWritableDatabase() call here, so,
        // as a shot-in-the-dark, I'm going to delay it by 50 ms and see if that might help matters
        new Handler().postDelayed(() -> SqliteUtil.enableForeignKeys(dbHelper.getWritableDatabase()), 50);
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Helpers
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    public final void deleteEntity(final MainSupport entity, final RikerTable table) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            if (entity.localIdentifier != null) {
                deleteFromTable(table.tableName,
                        new String[] { CommonColumn.LOCAL_ID.name },
                        new String[] { entity.localIdentifier.toString() },
                        database);
            }
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    private static void deleteFromTable(final String table,
                                        final String whereColumns[],
                                        final String whereArgs[],
                                        final SQLiteDatabase database) {
        final StringBuilder stmt = new StringBuilder(String.format("delete from %s", table));
        if (whereColumns != null && whereColumns.length > 0) {
            stmt.append(" where ");
        }
        if (whereColumns != null) {
            for (int i = 0; i < whereColumns.length; i++) {
                stmt.append(String.format("%s = ?", whereColumns[i]));
                if (i + 1 < whereColumns.length) {
                    stmt.append(" and ");
                }
            }
        }
        database.execSQL(stmt.toString(), whereArgs != null ? whereArgs : EMPTY_SELECTION_ARGS);
    }

    private static Integer localIdentifierForEntity(final ModelSupport entity,
                                                    final RikerTable table,
                                                    final SQLiteDatabase database) {
        Integer localIdentifier = null;
        if (entity.globalIdentifier != null) {
            localIdentifier = integerFromTable(table.tableName,
                    CommonColumn.LOCAL_ID.name,
                    CommonColumn.GLOBAL_ID.name,
                    entity.globalIdentifier,
                    database);
        }
        return localIdentifier;
    }

    private static void close(final Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private static List objectsFromQuery(final String query,
                                         final String args[],
                                         final Integer numAllowed,
                                         final Function.RsObjConverter rsConverter,
                                         final SQLiteDatabase database) {
        final List objects = new ArrayList<>();
        Cursor cursor = null;
        final StringBuilder queryStringBuilder = new StringBuilder(query);
        if (numAllowed != null) {
            queryStringBuilder.append(" limit ").append(numAllowed.toString());
        }
        try {
            cursor = database.rawQuery(queryStringBuilder.toString(), args);
            while (cursor.moveToNext()) {
                objects.add(rsConverter.invoke(cursor));
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            close(cursor);
        }
        return objects;
    }

    private static Object objectFromQuery(final String query,
                                          final String args[],
                                          final Function.RsObjConverter rsConverter,
                                          final SQLiteDatabase database) {
        Object object = null;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, args);
            while (cursor.moveToNext()) {
                object = rsConverter.invoke(cursor);
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        return object;
    }

    private static int intFromQuery(final String query,
                                    final String args[],
                                    final SQLiteDatabase database) {
        int value = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, args);
            while (cursor.moveToNext()) {
                value = cursor.getInt(0);
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        return value;
    }

    private static Object objectFromTable(final String table,
                                          final String selectColumn,
                                          final String whereColumn,
                                          final String whereValue,
                                          final Function.RsObjConverter rsExtractor,
                                          final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select %s from %s where %s = ?", selectColumn, table, whereColumn),
                new String[] { whereValue },
                null,
                rsExtractor,
                database);
    }

    private static List<Integer> integersFromQuery(final String query, final String args[], final SQLiteDatabase database) {
        final List<Integer> integers = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, args);
            while (cursor.moveToNext()) {
                integers.add(cursor.getInt(0));
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        return integers;
    }

    private static Integer integerFromTable(final String table,
                                            final String selectColumn,
                                            final String whereColumn,
                                            final String whereValue,
                                            final SQLiteDatabase database) {
        return (Integer) objectFromTable(table, selectColumn, whereColumn, whereValue, cursor -> SqliteUtil.toInteger(cursor, selectColumn), database);
    }

    private static Date dateFromTable(final String table,
                                      final String selectColumn,
                                      final String whereColumn,
                                      final String whereValue,
                                      final SQLiteDatabase database) {
        return (Date) objectFromTable(table, selectColumn, whereColumn, whereValue, cursor -> SqliteUtil.toDateFromLongColType(cursor, selectColumn), database);
    }

    private static void populateModelSupportFieldsFromCursor(final ModelSupport modelSupport, final Cursor cursor) {
        modelSupport.globalIdentifier = SqliteUtil.toString(cursor, CommonColumn.GLOBAL_ID.name);
    }

    private static void populateModelSupportFieldsToContentValues(final ModelSupport modelSupport, final ContentValues contentValues) {
        contentValues.put(CommonColumn.GLOBAL_ID.name, modelSupport.globalIdentifier);
    }

    private static void populateSupportFieldsFromCursor(final MasterSupport support, final Cursor cursor) {
        populateModelSupportFieldsFromCursor(support, cursor);
        support.localIdentifier = SqliteUtil.toInteger(cursor, CommonColumn.LOCAL_ID.name);
        support.createdAt = SqliteUtil.toDateFromLongColType(cursor, CommonEntityColumn.CREATED_AT.name);
        support.updatedAt = SqliteUtil.toDateFromLongColType(cursor, CommonEntityColumn.UPDATED_AT.name);
        support.deletedAt = SqliteUtil.toDateFromLongColType(cursor, CommonEntityColumn.DELETED_AT.name);
        support.syncInProgress = SqliteUtil.toBoolean(cursor, CommonEntityColumn.SYNC_IN_PROGRESS.name);
        support.synced = SqliteUtil.toBoolean(cursor, CommonEntityColumn.SYNCED.name);
        support.syncHttpRespCode = SqliteUtil.toInteger(cursor, CommonEntityColumn.SYNC_HTTP_RESP_CODE.name);
        support.syncErrMask = SqliteUtil.toInteger(cursor, CommonEntityColumn.SYNC_HTTP_RESP_ERR_MASK.name);
        support.syncRetryAt = SqliteUtil.toDateFromLongColType(cursor, CommonEntityColumn.SYNC_RETRY_AT.name);
    }

    private static void populateSupportFieldsToContentValues(final MasterSupport masterSupport, final ContentValues contentValues) {
        populateModelSupportFieldsToContentValues(masterSupport, contentValues);
        SqliteUtil.put(contentValues, CommonEntityColumn.CREATED_AT.name, masterSupport.createdAt);
        SqliteUtil.put(contentValues, CommonEntityColumn.UPDATED_AT.name, masterSupport.updatedAt);
        SqliteUtil.put(contentValues, CommonEntityColumn.DELETED_AT.name, masterSupport.deletedAt);
        contentValues.put(CommonMainColumn.SYNC_IN_PROGRESS.name, masterSupport.syncInProgress);
        contentValues.put(CommonMainColumn.SYNCED.name, masterSupport.synced);
        contentValues.put(CommonMainColumn.SYNC_HTTP_RESP_CODE.name, masterSupport.syncHttpRespCode);
        contentValues.put(CommonMainColumn.SYNC_HTTP_RESP_ERR_MASK.name, masterSupport.syncErrMask);
        SqliteUtil.put(contentValues, CommonMainColumn.SYNC_RETRY_AT.name, masterSupport.syncRetryAt);
    }

    public static void markAsDoneEditing(final MainSupport mainSupport) {
        mainSupport.syncHttpRespCode = null;
        mainSupport.syncErrMask = null;
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Chart Config Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void populateChartConfigDomainFieldsFromCursor(final ChartConfig chartConfig, final Cursor cursor) {
        chartConfig.chartId = SqliteUtil.toString(cursor, ChartConfigColumn.CHART_ID.name);
        chartConfig.category = ChartConfig.Category.categoryForVal(SqliteUtil.toInteger(cursor, ChartConfigColumn.CATEGORY.name));
        chartConfig.aggregateBy = ChartConfig.AggregateBy.aggregateByForVal(SqliteUtil.toInteger(cursor, ChartConfigColumn.AGGREGATE_BY.name));
        chartConfig.startDate = SqliteUtil.toDateFromLongColType(cursor, ChartConfigColumn.START_DATE.name);
        chartConfig.endDate = SqliteUtil.toDateFromLongColType(cursor, ChartConfigColumn.END_DATE.name);
        chartConfig.boundedEndDate = SqliteUtil.toBoolean(cursor, ChartConfigColumn.BOUNDED_END_DATE.name);
        chartConfig.suppressPieSliceLabels= SqliteUtil.toBoolean(cursor, ChartConfigColumn.SUPPRESS_PIE_SLICE_LABELS.name);
        chartConfig.isGlobal = SqliteUtil.toBoolean(cursor, ChartConfigColumn.IS_GLOBAL.name);
        chartConfig.loaderId = SqliteUtil.toInteger(cursor, ChartConfigColumn.LOADER_ID.name);
    }

    private static ChartConfig chartConfigFromCursor(final Cursor cursor) {
        final ChartConfig chartConfig = new ChartConfig();
        chartConfig.localIdentifier = SqliteUtil.toInteger(cursor, CommonColumn.LOCAL_ID.name);
        populateChartConfigDomainFieldsFromCursor(chartConfig, cursor);
        return chartConfig;
    }

    private static void populateChartConfigDomainFieldsToContentValues(final ChartConfig chartConfig, final ContentValues contentValues) {
        contentValues.put(ChartConfigColumn.CHART_ID.name, chartConfig.chartId);
        contentValues.put(ChartConfigColumn.AGGREGATE_BY.name, chartConfig.aggregateBy.val);
        contentValues.put(ChartConfigColumn.CATEGORY.name, chartConfig.category.val);
        SqliteUtil.put(contentValues, ChartConfigColumn.START_DATE.name, chartConfig.startDate);
        SqliteUtil.put(contentValues, ChartConfigColumn.END_DATE.name, chartConfig.endDate);
        contentValues.put(ChartConfigColumn.SUPPRESS_PIE_SLICE_LABELS.name, chartConfig.suppressPieSliceLabels);
        contentValues.put(ChartConfigColumn.BOUNDED_END_DATE.name, chartConfig.boundedEndDate);
        contentValues.put(ChartConfigColumn.IS_GLOBAL.name, chartConfig.isGlobal);
        contentValues.put(ChartConfigColumn.LOADER_ID.name, chartConfig.loaderId);
    }

    private static void insertIntoChartConfig(final ChartConfig chartConfig, final User user, final SQLiteDatabase database) {
        final ContentValues chartConfigContentValues = new ContentValues();
        populateChartConfigDomainFieldsToContentValues(chartConfig, chartConfigContentValues);
        chartConfigContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        final long newLocalIdentifier = database.insert(RikerTable.CHART_CONFIG.tableName, null, chartConfigContentValues);
        chartConfig.localIdentifier = (int)newLocalIdentifier;
    }

    private static void updateChartConfig(final ChartConfig chartConfig, final User user, final SQLiteDatabase database) {
        final ContentValues chartConfigContentValues = new ContentValues();
        chartConfigContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        populateChartConfigDomainFieldsToContentValues(chartConfig, chartConfigContentValues);
        database.update(RikerTable.CHART_CONFIG.tableName,
                chartConfigContentValues,
                String.format("%s = ?", CommonColumn.LOCAL_ID.name),
                new String[] { chartConfig.localIdentifier.toString() });
    }

    public ChartConfig chartConfig(final Chart chart, final User user) {
        return chartConfig(chart, user, dbHelper.getReadableDatabase());
    }

    public static ChartConfig chartConfig(final Chart chart, final User user, final SQLiteDatabase database) {
        return chartConfig(chart.id, user, database);
    }

    public ChartConfig chartConfig(final ChartConfig.GlobalChartId globalChartId, final User user) {
        return chartConfig(globalChartId, user, dbHelper.getReadableDatabase());
    }

    public static ChartConfig chartConfig(final ChartConfig.GlobalChartId globalChartId, final User user, final SQLiteDatabase database) {
        return chartConfig(globalChartId.idVal, user, database);
    }

    public static ChartConfig chartConfig(final String chartId, final User user, final SQLiteDatabase database) {
        return (ChartConfig) objectFromQuery(String.format("select * from %s where %s = ? and %s = ?",
                RikerTable.CHART_CONFIG.tableName,
                CommonColumn.USER_ID.name,
                ChartConfigColumn.CHART_ID.name),
                new String[] { user.localIdentifier.toString(), chartId },
                cursor -> chartConfigFromCursor(cursor),
                database);
    }

    public final void deleteChartConfig(final String chartId, final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            deleteChartConfig(chartId, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public static void deleteChartConfig(final String chartId, final User user, final SQLiteDatabase database) {
        deleteChartCache(chartId, user, database);
        deleteFromTable(RikerTable.CHART_CONFIG.tableName,
                new String[] { ChartConfigColumn.CHART_ID.name, CommonColumn.USER_ID.name },
                new String[] { chartId, user.localIdentifier.toString() }, database);
    }

    public final void deleteChartConfigs(final ChartConfig.Category category, final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            deleteChartConfigs(category, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public static void deleteChartConfigs(final ChartConfig.Category category, final User user, final SQLiteDatabase database) {
        deleteChartCache(category, user, database);
        deleteFromTable(RikerTable.CHART_CONFIG.tableName,
                new String[] { ChartConfigColumn.CATEGORY.name, CommonColumn.USER_ID.name },
                new String[] { Integer.toString(category.val), user.localIdentifier.toString() }, database);
    }

    public final void deleteChartConfigs(final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            deleteChartConfigs(user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public static void deleteChartConfigs(final User user, final SQLiteDatabase database) {
        deleteChartCache(user, database);
        deleteFromTable(RikerTable.CHART_CONFIG.tableName,
                new String[] { CommonColumn.USER_ID.name },
                new String[] { user.localIdentifier.toString() }, database);
    }

    public void saveNewOrExisting(final ChartConfig chartConfig, final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            saveNewOrExisting(chartConfig, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    private static void saveNewOrExisting(final ChartConfig chartConfig, final User user, final SQLiteDatabase database) {
        deleteChartCache(chartConfig.chartId, user, database);
        if (chartConfig.localIdentifier != null) {
            updateChartConfig(chartConfig, user, database);
        } else {
            insertIntoChartConfig(chartConfig, user, database);
        }
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Chart Cache Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    public final void deleteChartCache(final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            deleteChartCache(user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    private static void deleteChartCache(final User user, final  SQLiteDatabase database) {
        final String userIdArg[] = new String[] { user.localIdentifier.toString() };
        deleteFromTable(RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName,
                new String[] { CommonColumn.USER_ID.name },
                userIdArg, database);
        deleteFromTable(RikerTable.CHART_TIME_SERIES.tableName,
                new String[] { CommonColumn.USER_ID.name },
                userIdArg, database);
        deleteFromTable(RikerTable.CHART_PIE_SLICE.tableName,
                new String[] { CommonColumn.USER_ID.name },
                userIdArg, database);
        deleteFromTable(RikerTable.CHART.tableName,
                new String[] { CommonColumn.USER_ID.name },
                userIdArg, database);
    }

    public final void deleteChartCache(final String chartId, final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            deleteChartCache(chartId, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    private static void deleteChartCache(final String chartId, final User user, final  SQLiteDatabase database) {
        final Integer chartLocalId = (Integer)objectFromQuery(
                String.format("select %s from %s where %s = ? and %s = ?",
                CommonColumn.LOCAL_ID.name, RikerTable.CHART.tableName, CommonColumn.USER_ID.name, ChartColumn.CHART_ID.name),
                new String[] { user.localIdentifier.toString(), chartId },
                cursor -> cursor.getInt(0),
                database);
        if (chartLocalId != null) {
            final String chartLocalIdStringArg[] = new String[] { chartLocalId.toString() };
            deleteFromTable(RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName,
                    new String[] { ChartTimeSeriesDataPointColumn.CHART_LOCAL_ID.name },
                    chartLocalIdStringArg,
                    database);
            deleteFromTable(RikerTable.CHART_TIME_SERIES.tableName,
                    new String[] { ChartTimeSeriesColumn.CHART_LOCAL_ID.name },
                    chartLocalIdStringArg,
                    database);
            deleteFromTable(RikerTable.CHART_PIE_SLICE.tableName,
                    new String[] { ChartPieSliceColumn.CHART_LOCAL_ID.name },
                    chartLocalIdStringArg,
                    database);
            deleteFromTable(RikerTable.CHART.tableName,
                    new String[] { CommonColumn.LOCAL_ID.name },
                    chartLocalIdStringArg,
                    database);
        }
    }

    private static void deleteChartCache(final ChartConfig.Category category, final User user, final  SQLiteDatabase database) {
        final List<Integer> localChartIds = integersFromQuery(
                String.format("select %s from %s where %s = ? and %s = ?", CommonColumn.LOCAL_ID.name, RikerTable.CHART.tableName, CommonColumn.USER_ID.name, ChartColumn.CATEGORY.name),
                new String[] { user.localIdentifier.toString(), Integer.toString(category.val) },
                database);
        final int numLocalChartIds = localChartIds.size();
        if (numLocalChartIds > 0) {
            final StringBuilder localChartIdsStringBuilder = new StringBuilder();
            for (int i = 0; i < numLocalChartIds; i++) {
                localChartIdsStringBuilder.append(localChartIds.get(i));
                if (i + 1 < numLocalChartIds) {
                    localChartIdsStringBuilder.append(", ");
                }
            }
            final String localChartIdsString = localChartIdsStringBuilder.toString();
            final String userIdArg[] = new String[] { user.localIdentifier.toString() };
            final String deleteStmtTemplate = "delete from %s where %s = ? and %s in (%s)";
            database.execSQL(String.format(deleteStmtTemplate,
                    RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName,
                    CommonColumn.USER_ID.name,
                    ChartTimeSeriesDataPointColumn.CHART_LOCAL_ID.name,
                    localChartIdsString),
                    userIdArg);
            database.execSQL(String.format(deleteStmtTemplate,
                    RikerTable.CHART_TIME_SERIES.tableName,
                    CommonColumn.USER_ID.name,
                    ChartTimeSeriesColumn.CHART_LOCAL_ID.name,
                    localChartIdsString),
                    userIdArg);
            database.execSQL(String.format(deleteStmtTemplate,
                    RikerTable.CHART_PIE_SLICE.tableName,
                    CommonColumn.USER_ID.name,
                    ChartPieSliceColumn.CHART_LOCAL_ID.name,
                    localChartIdsString),
                    userIdArg);
            database.execSQL(String.format(deleteStmtTemplate,
                    RikerTable.CHART.tableName,
                    CommonColumn.USER_ID.name,
                    CommonColumn.LOCAL_ID.name,
                    localChartIdsString),
                    userIdArg);
        }
    }

    public final void deleteChartCache(final ChartConfig.Category category, final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            deleteChartCache(category, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public final void saveLineChartDataCache(final RLineChartData lineData,
                                             final String chartId,
                                             final Integer chartConfigId,
                                             final ChartConfig.Category category,
                                             final ChartConfig.AggregateBy aggregateBy,
                                             final int xaxisLabelCount,
                                             final BigDecimal maxValue,
                                             final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            saveLineChartDataCache(lineData, chartId, chartConfigId, category, aggregateBy, xaxisLabelCount, maxValue, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Timber.e(t);
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    private static void saveLineChartDataCache(final RLineChartData lineData,
                                               final String chartId,
                                               final Integer chartConfigId,
                                               final ChartConfig.Category category,
                                               final ChartConfig.AggregateBy aggregateBy,
                                               final int xaxisLabelCount,
                                               final BigDecimal maxValue,
                                               final User user,
                                               final SQLiteDatabase database) {
        deleteChartCache(chartId, user, database);
        ContentValues contentValues = new ContentValues();
        contentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        contentValues.put(ChartColumn.CHART_ID.name, chartId);
        contentValues.put(ChartColumn.CONFIG_ID.name, chartConfigId);
        contentValues.put(ChartColumn.AGGREGATE_BY.name, aggregateBy.val);
        contentValues.put(ChartColumn.CATEGORY.name, category.val);
        contentValues.put(ChartColumn.MAX_VALUE.name, maxValue.toString());
        contentValues.put(ChartColumn.XAXIS_LABEL_COUNT.name, xaxisLabelCount);
        final long newChartLocalId = database.insert(RikerTable.CHART.tableName, null, contentValues);
        final List<RLineChartDataSeries> dataSets = lineData.dataSeriesList;
        for (final RLineChartDataSeries dataSet : dataSets) {
            if (dataSet.dataPoints.size() > 0) {
                contentValues = new ContentValues();
                contentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
                contentValues.put(ChartTimeSeriesColumn.CHART_LOCAL_ID.name, newChartLocalId);
                contentValues.put(ChartTimeSeriesColumn.ENTITY_LMID.name, dataSet.entityLocalIdentifier);
                contentValues.put(ChartTimeSeriesColumn.LABEL.name, dataSet.label);
                final long newChartTimeSeriesId = database.insert(RikerTable.CHART_TIME_SERIES.tableName, null, contentValues);
                for (final RLineDataPoint chartDataEntry : dataSet.dataPoints) {
                    contentValues = new ContentValues();
                    contentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
                    contentValues.put(ChartTimeSeriesDataPointColumn.CHART_LOCAL_ID.name, newChartLocalId);
                    contentValues.put(ChartTimeSeriesDataPointColumn.CHART_TIME_SERIES_ID.name, newChartTimeSeriesId);
                    contentValues.put(ChartTimeSeriesDataPointColumn.DATE.name, chartDataEntry.date);
                    contentValues.put(ChartTimeSeriesDataPointColumn.VALUE.name, chartDataEntry.value);
                    database.insert(RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName, null, contentValues);
                }
            }
        }
    }

    public final LineChartDataContainer lineChartDataContainerCache(final String chartId, final Integer chartConfigLocalId, final User user) {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        LineChartDataContainer lineChartDataContainer = null;
        final List<String> queryArgs = new ArrayList<>();
        final StringBuilder query = new StringBuilder(String.format("select * from %s where %s = ? and %s = ?", RikerTable.CHART.tableName, CommonColumn.USER_ID.name, ChartColumn.CHART_ID.name));
        queryArgs.add(user.localIdentifier.toString());
        queryArgs.add(chartId);
        if (chartConfigLocalId != null) {
            query.append(String.format(" and %s = ?", ChartColumn.CONFIG_ID.name));
            queryArgs.add(chartConfigLocalId.toString());
        }
        Cursor cursor = database.rawQuery(query.toString(), queryArgs.toArray(new String[queryArgs.size()]));
        Integer chartLocalId = null;
        while (cursor.moveToNext()) {
            lineChartDataContainer = new LineChartDataContainer();
            chartLocalId = SqliteUtil.toInteger(cursor, CommonColumn.LOCAL_ID.name);
            lineChartDataContainer.maxyValue = SqliteUtil.toBigDecimal(cursor, ChartColumn.MAX_VALUE.name);
            lineChartDataContainer.xaxisLabelCount = SqliteUtil.toInt(cursor, ChartColumn.XAXIS_LABEL_COUNT.name, 0);
            lineChartDataContainer.aggregateBy = ChartConfig.AggregateBy.aggregateByForVal(SqliteUtil.toInteger(cursor, ChartColumn.AGGREGATE_BY.name));
        }
        cursor.close();
        if (lineChartDataContainer != null) {
            final List<RLineChartDataSeries> rLineChartDataSeriesList = new ArrayList<>();
            cursor = database.rawQuery(String.format("select %s, %s, %s from %s where %s = ?",
                    CommonColumn.LOCAL_ID.name,
                    ChartTimeSeriesColumn.ENTITY_LMID.name,
                    ChartTimeSeriesColumn.LABEL.name,
                    RikerTable.CHART_TIME_SERIES.tableName,
                    ChartTimeSeriesColumn.CHART_LOCAL_ID.name),
                    new String[] { chartLocalId.toString() });
            while (cursor.moveToNext()) {
                rLineChartDataSeriesList.add(new RLineChartDataSeries(null,
                        SqliteUtil.toString(cursor, ChartTimeSeriesColumn.LABEL.name),
                        SqliteUtil.toInteger(cursor, CommonColumn.LOCAL_ID.name),
                        SqliteUtil.toInteger(cursor, ChartTimeSeriesColumn.ENTITY_LMID.name)));
            }
            cursor.close();
            final List<ILineDataSet> uiDataSets = new ArrayList<>(rLineChartDataSeriesList.size());
            for (final RLineChartDataSeries rLineChartDataSeries : rLineChartDataSeriesList) {
                final List<Entry> uiLineChartDataEntries = new ArrayList<>();
                cursor = database.rawQuery(String.format("select %s, %s from %s where %s = ? order by %s asc",
                        ChartTimeSeriesDataPointColumn.VALUE.name,
                        ChartTimeSeriesDataPointColumn.DATE.name,
                        RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName,
                        ChartTimeSeriesDataPointColumn.CHART_TIME_SERIES_ID.name,
                        ChartTimeSeriesDataPointColumn.DATE.name),
                        new String[] { rLineChartDataSeries.localIdentifier.toString() });
                while (cursor.moveToNext()) {
                    uiLineChartDataEntries.add(new Entry(
                            SqliteUtil.toFloat(cursor, ChartTimeSeriesDataPointColumn.DATE.name, 0.0f),
                            SqliteUtil.toFloat(cursor, ChartTimeSeriesDataPointColumn.VALUE.name, 0.0f)));
                }
                cursor.close();
                uiDataSets.add(new RLineDataSet(uiLineChartDataEntries, rLineChartDataSeries.label, rLineChartDataSeries.entityLocalIdentifier));
            }
            lineChartDataContainer.uiLineChartData = new LineData(uiDataSets);
        }
        return lineChartDataContainer;
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       User Settings Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void populateUserSettingsDomainFieldsFromCursor(final UserSettings userSettings, final Cursor cursor) {
        userSettings.weightUom = SqliteUtil.toInteger(cursor, UserSettingsColumn.WEIGHT_UOM.name);
        userSettings.sizeUom = SqliteUtil.toInteger(cursor, UserSettingsColumn.SIZE_UOM.name);
        userSettings.weightIncDecAmount= SqliteUtil.toInteger(cursor, UserSettingsColumn.WEIGHT_INC_DEC_AMOUNT.name);
    }

    private static UserSettings masterUserSettingsFromCursor(final Cursor cursor) {
        final UserSettings userSettings = new UserSettings();
        populateSupportFieldsFromCursor(userSettings, cursor);
        populateUserSettingsDomainFieldsFromCursor(userSettings, cursor);
        return userSettings;
    }

    private static void populateUserSettingsDomainFieldsToContentValues(final UserSettings userSettings, final ContentValues contentValues) {
        contentValues.put(UserSettingsColumn.WEIGHT_UOM.name, userSettings.weightUom);
        contentValues.put(UserSettingsColumn.SIZE_UOM.name, userSettings.sizeUom);
        contentValues.put(UserSettingsColumn.WEIGHT_INC_DEC_AMOUNT.name, userSettings.weightIncDecAmount);
    }

    private static void insertIntoUserSettings(final UserSettings userSettings, final User user, final SQLiteDatabase database) {
        final ContentValues userSettingsContentValues = new ContentValues();
        populateSupportFieldsToContentValues(userSettings, userSettingsContentValues);
        populateUserSettingsDomainFieldsToContentValues(userSettings, userSettingsContentValues);
        userSettingsContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        final long newLocalIdentifier = database.insert(RikerTable.USER_SETTINGS.tableName, null, userSettingsContentValues);
        userSettings.localIdentifier = (int)newLocalIdentifier;
    }

    private static void updateUserSettings(final UserSettings userSettings, final User user, final SQLiteDatabase database) {
        final ContentValues setContentValues = new ContentValues();
        setContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        populateSupportFieldsToContentValues(userSettings, setContentValues);
        populateUserSettingsDomainFieldsToContentValues(userSettings, setContentValues);
        database.update(RikerTable.USER_SETTINGS.tableName,
                setContentValues,
                String.format("%s = ?", CommonColumn.LOCAL_ID.name),
                new String[] { userSettings.localIdentifier.toString() });
    }

    public final UserSettings userSettings(final User user) {
        return userSettings(user, dbHelper.getReadableDatabase());
    }

    private static final UserSettings userSettings(final User user, final SQLiteDatabase database) {
        return (UserSettings) objectFromQuery(String.format("select * from %s where %s = ?", RikerTable.USER_SETTINGS.tableName, CommonColumn.USER_ID.name),
                new String[] { user.localIdentifier.toString() },
                cursor -> masterUserSettingsFromCursor(cursor),
                database);
    }

    public final void saveUserSettings(final User user, final UserSettings userSettings) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            updateUserSettings(userSettings, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Set Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void populateSetDomainFieldsToContentValues(final Set set, final ContentValues contentValues) {
        contentValues.put(SetColumn.WEIGHT.name, set.weight.toString());
        contentValues.put(SetColumn.NUM_REPS.name, set.numReps.toString());
        contentValues.put(SetColumn.TO_FAILURE.name, set.toFailure);
        contentValues.put(SetColumn.NEGATIVES.name, set.negatives);
        contentValues.put(SetColumn.IGNORE_TIME.name, set.ignoreTime);
        SqliteUtil.put(contentValues, SetColumn.LOGGED_AT.name, set.loggedAt);
        SqliteUtil.put(contentValues, SetColumn.IMPORTED_AT.name, set.importedAt);
        contentValues.put(SetColumn.WEIGHT_UOM.name, set.weightUom.toString());
        contentValues.put(SetColumn.ORIGINATION_DEVICE_ID.name, set.originationDeviceId.toString());
        contentValues.put(SetColumn.MOVEMENT_ID.name, set.movementId.toString());
        if (set.movementVariantId != null) {
            contentValues.put(SetColumn.MOVEMENT_VARIANT_ID.name, set.movementVariantId.toString());
        }
    }

    private static void populateSetDomainFieldsFromCursor(final Set set, final Cursor cursor) {
        set.loggedAt = SqliteUtil.toDateFromLongColType(cursor, SetColumn.LOGGED_AT.name);
        set.importedAt = SqliteUtil.toDateFromLongColType(cursor, SetColumn.IMPORTED_AT.name);
        set.weight = SqliteUtil.toBigDecimal(cursor, SetColumn.WEIGHT.name);
        set.weightUom = SqliteUtil.toInteger(cursor, SetColumn.WEIGHT_UOM.name);
        set.numReps = SqliteUtil.toInteger(cursor, SetColumn.NUM_REPS.name);
        set.toFailure = SqliteUtil.toBoolean(cursor, SetColumn.TO_FAILURE.name);
        set.negatives = SqliteUtil.toBoolean(cursor, SetColumn.NEGATIVES.name);
        set.ignoreTime = SqliteUtil.toBoolean(cursor, SetColumn.IGNORE_TIME.name);
        set.originationDeviceId = SqliteUtil.toInteger(cursor, SetColumn.ORIGINATION_DEVICE_ID.name);
        set.movementId = SqliteUtil.toInteger(cursor, SetColumn.MOVEMENT_ID.name);
        set.movementVariantId = SqliteUtil.toInteger(cursor, SetColumn.MOVEMENT_VARIANT_ID.name);
    }

    private static Set masterSetFromCursor(final Cursor cursor) {
        final Set set = new Set();
        populateSupportFieldsFromCursor(set, cursor);
        populateSetDomainFieldsFromCursor(set, cursor);
        return set;
    }

    private static void insertIntoSet(final Set set, final User user, final SQLiteDatabase database) {
        final ContentValues setContentValues = new ContentValues();
        setContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        populateSupportFieldsToContentValues(set, setContentValues);
        populateSetDomainFieldsToContentValues(set, setContentValues);
        final long newUserLocalIdentifier = database.insert(RikerTable.SET.tableName, null, setContentValues);
        set.localIdentifier = (int)newUserLocalIdentifier;
    }

    private static void updateSet(final Set set, final User user, final SQLiteDatabase database) {
        final ContentValues setContentValues = new ContentValues();
        setContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        populateSupportFieldsToContentValues(set, setContentValues);
        populateSetDomainFieldsToContentValues(set, setContentValues);
        database.update(RikerTable.SET.tableName,
                setContentValues,
                String.format("%s = ?", CommonColumn.LOCAL_ID.name),
                new String[] { set.localIdentifier.toString() });
    }

    public final void saveNewSet(final User user, final Set set) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            insertIntoSet(set, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public final int saveAllNewImportedSets(final User user,
                                            final List<? extends MainSupport> setsList) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        final int numSets = setsList.size();
        int savedSetCount = 0;
        try {
            for (int i = 0; i < numSets; i++) {
                insertIntoSet((Set)setsList.get(i), user, database);
                savedSetCount++;
            }
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
        return savedSetCount;
    }

    public final void saveSet(final User user, final Set set) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            updateSet(set, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public final List descendingSets(final User user) {
        return descendingSets(user, dbHelper.getReadableDatabase());
    }

    public final List descendingSets(final User user, final int limit) {
        return descendingSets(user, limit, dbHelper.getReadableDatabase());
    }

    public final List descendingSets(final Date beforeLoggedAt, final User user) {
        return  descendingSets(beforeLoggedAt, user, dbHelper.getReadableDatabase());
    }

    public final List descendingSets(final Date beforeLoggedAt, final User user, final int limit) {
        return descendingSets(beforeLoggedAt, user, limit, dbHelper.getReadableDatabase());
    }

    public final List descendingSetsOnOrAfter(final Date upTo, final User user) {
        return descendingSetsOnOrAfter(upTo, user, dbHelper.getReadableDatabase());
    }

    public final List descendingSetsAfter(final Date after, final User user) {
        return descendingSetsAfter(after, user, dbHelper.getReadableDatabase());
    }

    private static final List descendingSets(final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s desc, %s desc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name),
                new String[] { user.localIdentifier.toString() },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    private static final List descendingSets(final User user, final int limit, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s desc, %s desc limit %d",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name,
                limit),
                new String[] { user.localIdentifier.toString() },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    private static final List descendingSets(final Date beforeLoggedAt, final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s < ? order by %s desc, %s desc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(beforeLoggedAt.getTime()) },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    private static final List descendingSets(final Date beforeLoggedAt, final User user, final int limit, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s < ? order by %s desc, %s desc limit %d",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name,
                limit),
                new String[] { user.localIdentifier.toString(), Long.toString(beforeLoggedAt.getTime()) },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    private static final List descendingSetsOnOrAfter(final Date upTo, final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s >= ? order by %s desc, %s desc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(upTo.getTime()) },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    private static final List descendingSetsAfter(final Date after, final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s > ? order by %s desc, %s desc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(after.getTime()) },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    public final List ascendingSets(final User user) {
        return ascendingSets(user, dbHelper.getReadableDatabase());
    }

    private static final List ascendingSets(final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s asc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString() },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    public final List ascendingSetsSince(final User user, final Date loggedSince) {
        return ascendingSetsSince(user, loggedSince, dbHelper.getReadableDatabase());
    }

    private static final List ascendingSetsSince(final User user, final Date loggedSince, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s > ? order by %s asc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(loggedSince.getTime()) },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    public final List ascendingSets(final User user, final Date onOrAfterLoggedAt, final Date onOrBeforeLoggedAt) {
        return ascendingSets(user, onOrAfterLoggedAt, onOrBeforeLoggedAt, dbHelper.getReadableDatabase());
    }

    private static final List ascendingSets(final User user, final Date onOrAfterLoggedAt, final Date onOrBeforeLoggedAt, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s >= ? AND %s <= ? order by %s asc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(onOrAfterLoggedAt.getTime()), Long.toString(onOrBeforeLoggedAt.getTime()) },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    public final List ascendingSets(final User user, final Date onOrAfterLoggedAt) {
        return ascendingSets(user, onOrAfterLoggedAt, dbHelper.getReadableDatabase());
    }

    private static final List ascendingSets(final User user, final Date onOrAfterLoggedAt, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s >= ? order by %s asc",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name,
                SetColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(onOrAfterLoggedAt.getTime()) },
                null,
                cursor -> masterSetFromCursor(cursor),
                database);
    }

    public final int numSets(final User user) {
        return numSets(user, dbHelper.getReadableDatabase());
    }

    private static int numSets(final User user, final SQLiteDatabase database) {
        return intFromQuery(String.format("select count(*) from %s where %s = ?",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name),
                new String[] { user.localIdentifier.toString() },
                database);
    }

    public final int numSets(final User user, final Date loggedSince) {
        return numSets(user, loggedSince, dbHelper.getReadableDatabase());
    }

    private static int numSets(final User user, final Date loggedSince, final SQLiteDatabase database) {
        return intFromQuery(String.format("select count(*) from %s where %s = ? and %s > ?",
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name,
                SetColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(loggedSince.getTime()) },
                database);
    }

    private static Date mostRecentSetDate(final User user, final SQLiteDatabase database) {
        return (Date) objectFromQuery(String.format("select max(%s) as max_date from %s where %s = ?",
                SetColumn.LOGGED_AT.name,
                RikerTable.SET.tableName,
                CommonColumn.USER_ID.name),
                new String[] { user.localIdentifier.toString() },
                cursor -> SqliteUtil.toDateFromLongColType(cursor, "max_date"),
                database);
    }

    public final Date mostRecentSetDate(final User user) {
        return mostRecentSetDate(user, dbHelper.getReadableDatabase());
    }

    public final void deleteSet(final Set set) {
        deleteEntity(set, RikerTable.SET);
    }

    public final WorkoutsTuple workoutsTupleForDescendingSets(final List descendingSets,
                                                              final User user,
                                                              final UserSettings userSettings,
                                                              final Map<Integer, Movement> allMovements,
                                                              final Map<Integer, MuscleGroup> allMuscleGroups,
                                                              final Map<Integer, Muscle> allMuscles) {
        return workoutsTupleForDescendingSets(descendingSets,
                user,
                userSettings,
                allMovements,
                allMuscleGroups,
                allMuscles,
                dbHelper.getReadableDatabase());
    }

    private static final WorkoutsTuple workoutsTupleForDescendingSets(final List descendingSets,
                                                                      final User user,
                                                                      final UserSettings userSettings,
                                                                      final Map<Integer, Movement> allMovements,
                                                                      final Map<Integer, MuscleGroup> allMuscleGroups,
                                                                      final Map<Integer, Muscle> allMuscles,
                                                                      final SQLiteDatabase database) {
        Date previousSetLoggedAt = null;
        final int numSets = descendingSets.size();
        List workouts = null;
        Date latestSetLoggedAt = null;
        if (numSets > 0) {
            latestSetLoggedAt = ((Set)descendingSets.get(0)).loggedAt;
            workouts = new ArrayList();
            final List setsForWorkout = new ArrayList();
            for (int i = 0; i < numSets; i++) {
                final Set set = (Set) descendingSets.get(i);
                if (previousSetLoggedAt != null) {
                    final long seconds = (previousSetLoggedAt.getTime() - set.loggedAt.getTime()) / 1000l;
                    if (seconds < SECONDS_IN_HOUR) {
                        setsForWorkout.add(set);
                        previousSetLoggedAt = set.loggedAt;
                    } else {
                        final BodyMeasurementLog nearestBml = nearestBmlWithNonNilBodyWeight(user, previousSetLoggedAt, database);
                        workouts.add(Utils.workoutForDescendingSets(setsForWorkout,
                                nearestBml,
                                userSettings,
                                allMovements,
                                allMuscleGroups,
                                allMuscles));
                        setsForWorkout.clear();
                        setsForWorkout.add(set); // final set (because the sets are in descending order) of new workout
                        previousSetLoggedAt = set.loggedAt;
                    }
                } else {
                    setsForWorkout.add(set);
                    previousSetLoggedAt = set.loggedAt;
                }
            }
            final Date loggedAt = ((Set)setsForWorkout.get(setsForWorkout.size() - 1)).loggedAt;
            final BodyMeasurementLog nearestBml = nearestBmlWithNonNilBodyWeight(user, loggedAt, database);
            workouts.add(Utils.workoutForDescendingSets(setsForWorkout,
                    nearestBml,
                    userSettings,
                    allMovements,
                    allMuscleGroups,
                    allMuscles));
        }
        return new WorkoutsTuple(workouts, latestSetLoggedAt, descendingSets.size());
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Bml Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void populateBmlDomainFieldsToContentValues(final BodyMeasurementLog bml, final ContentValues contentValues) {
        SqliteUtil.put(contentValues, BmlColumn.LOGGED_AT.name, bml.loggedAt);
        SqliteUtil.put(contentValues, BmlColumn.IMPORTED_AT.name, bml.importedAt);
        contentValues.put(BmlColumn.ORIGINATION_DEVICE_ID.name, bml.originationDeviceId.toString());
        // the uom columns should always be non-null
        contentValues.put(BmlColumn.BODY_WEIGHT_UOM.name, bml.bodyWeightUom.toString());
        contentValues.put(BmlColumn.SIZE_UOM.name, bml.sizeUom.toString());
        // null checks needed for all nullable value columns
        contentValues.put(BmlColumn.BODY_WEIGHT.name, bml.bodyWeight != null ? bml.bodyWeight.toString() : null);
        contentValues.put(BmlColumn.ARM_SIZE.name, bml.armSize != null ? bml.armSize.toString() : null);
        contentValues.put(BmlColumn.CALF_SIZE.name, bml.calfSize != null ? bml.calfSize.toString() : null);
        contentValues.put(BmlColumn.CHEST_SIZE.name, bml.chestSize != null ? bml.chestSize.toString() : null);
        contentValues.put(BmlColumn.NECK_SIZE.name, bml.neckSize != null ? bml.neckSize.toString() : null);
        contentValues.put(BmlColumn.WAIST_SIZE.name, bml.waistSize != null ? bml.waistSize.toString() : null);
        contentValues.put(BmlColumn.FOREARM_SIZE.name, bml.forearmSize != null ? bml.forearmSize.toString() : null);
        contentValues.put(BmlColumn.THIGH_SIZE.name, bml.thighSize != null ? bml.thighSize.toString() : null);
    }

    private static void populateBmlDomainFieldsFromCursor(final BodyMeasurementLog bml, final Cursor cursor) {
        bml.loggedAt = SqliteUtil.toDateFromLongColType(cursor, BmlColumn.LOGGED_AT.name);
        bml.importedAt = SqliteUtil.toDateFromLongColType(cursor, BmlColumn.IMPORTED_AT.name);
        bml.bodyWeight = SqliteUtil.toBigDecimal(cursor, BmlColumn.BODY_WEIGHT.name);
        bml.bodyWeightUom = SqliteUtil.toInteger(cursor, BmlColumn.BODY_WEIGHT_UOM.name);
        bml.sizeUom = SqliteUtil.toInteger(cursor, BmlColumn.SIZE_UOM.name);
        bml.armSize = SqliteUtil.toBigDecimal(cursor, BmlColumn.ARM_SIZE.name);
        bml.calfSize = SqliteUtil.toBigDecimal(cursor, BmlColumn.CALF_SIZE.name);
        bml.chestSize = SqliteUtil.toBigDecimal(cursor, BmlColumn.CHEST_SIZE.name);
        bml.neckSize = SqliteUtil.toBigDecimal(cursor, BmlColumn.NECK_SIZE.name);
        bml.waistSize = SqliteUtil.toBigDecimal(cursor, BmlColumn.WAIST_SIZE.name);
        bml.forearmSize = SqliteUtil.toBigDecimal(cursor, BmlColumn.FOREARM_SIZE.name);
        bml.thighSize = SqliteUtil.toBigDecimal(cursor, BmlColumn.THIGH_SIZE.name);
        bml.originationDeviceId = SqliteUtil.toInteger(cursor, BmlColumn.ORIGINATION_DEVICE_ID.name);
    }

    private static BodyMeasurementLog masterBmlFromCursor(final Cursor cursor) {
        final BodyMeasurementLog bml = new BodyMeasurementLog();
        populateSupportFieldsFromCursor(bml, cursor);
        populateBmlDomainFieldsFromCursor(bml, cursor);
        return bml;
    }

    private static void insertIntoBml(final BodyMeasurementLog bml, final User user, final SQLiteDatabase database) {
        final ContentValues bmlContentValues = new ContentValues();
        bmlContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        populateSupportFieldsToContentValues(bml, bmlContentValues);
        populateBmlDomainFieldsToContentValues(bml, bmlContentValues);
        final long newUserLocalIdentifier = database.insert(RikerTable.BML.tableName, null, bmlContentValues);
        bml.localIdentifier = (int)newUserLocalIdentifier;
    }

    private static void updateBml(final BodyMeasurementLog bml, final User user, final SQLiteDatabase database) {
        final ContentValues bmlContentValues = new ContentValues();
        bmlContentValues.put(CommonColumn.USER_ID.name, user.localIdentifier);
        populateSupportFieldsToContentValues(bml, bmlContentValues);
        populateBmlDomainFieldsToContentValues(bml, bmlContentValues);
        database.update(RikerTable.BML.tableName,
                bmlContentValues,
                String.format("%s = ?", CommonColumn.LOCAL_ID.name),
                new String[] { bml.localIdentifier.toString() });
    }

    public final void saveNewBml(final User user, final BodyMeasurementLog bml) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            insertIntoBml(bml, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public final int saveAllNewImportedBmls(final User user,
                                            final List<? extends MainSupport> bmlsList) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        final int numBmls = bmlsList.size();
        int savedBmlCount = 0;
        try {
            for (int i = 0; i < numBmls; i++) {
                insertIntoBml((BodyMeasurementLog)bmlsList.get(i), user, database);
                savedBmlCount++;
            }
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
        return savedBmlCount;
    }

    public final void saveBml(final User user, final BodyMeasurementLog bml) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            updateBml(bml, user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public final List descendingBmls(final User user) {
        return descendingBmls(user, dbHelper.getReadableDatabase());
    }

    public final List descendingBmls(final User user, final int limit) {
        return descendingBmls(user, limit, dbHelper.getReadableDatabase());
    }

    public final List descendingBmls(final Date beforeLoggedAt, final User user, final int limit) {
        return descendingBmls(beforeLoggedAt, user, limit, dbHelper.getReadableDatabase());
    }

    public final List descendingBmlsUpTo(final Date upTo, final User user) {
        return descendingBmlsUpTo(upTo, user, dbHelper.getReadableDatabase());
    }

    private static final List descendingBmls(final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s desc, %s desc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name),
                new String[] { user.localIdentifier.toString() },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    private static final List descendingBmls(final User user, final int limit, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s desc, %s desc limit %d",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name,
                limit),
                new String[] { user.localIdentifier.toString() },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    private static final List descendingBmls(final Date beforeLoggedAt, final User user, final int limit, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s < ? order by %s desc, %s desc limit %d",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.LOGGED_AT.name,
                BmlColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name,
                limit),
                new String[] { user.localIdentifier.toString(), Long.toString(beforeLoggedAt.getTime()) },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    private static final List descendingBmlsUpTo(final Date upTo, final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s >= ? order by %s desc, %s desc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.LOGGED_AT.name,
                BmlColumn.LOGGED_AT.name,
                CommonEntityColumn.UPDATED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(upTo.getTime()) },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    public final BodyMeasurementLog mostRecentBmlWithNonNilWeight(final User user) {
        return mostRecentBmlWithNonNilWeight(user, dbHelper.getReadableDatabase());
    }

    private static BodyMeasurementLog mostRecentBmlWithNonNilWeight(final User user, final SQLiteDatabase database) {
        final List<BodyMeasurementLog> bmls = objectsFromQuery(String.format("select * from %s where %s = ? and %s is not null order by %s desc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.BODY_WEIGHT.name,
                BmlColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString() },
                1,
                cursor -> masterBmlFromCursor(cursor),
                database);
        if (bmls.size() >= 1) {
            return bmls.get(0);
        }
        return null;
    }

    private static final List ascendingBmlsWithNonNilColumn(final User user, final Date onOrAfterLoggedAt, final Date onOrBeforeLoggedAt, final BmlColumn nonNilColumn, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s >= ? AND %s <= ? AND %s is not null order by %s asc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.LOGGED_AT.name,
                BmlColumn.LOGGED_AT.name,
                BmlColumn.LOGGED_AT.name,
                nonNilColumn.name),
                new String[] { user.localIdentifier.toString(), Long.toString(onOrAfterLoggedAt.getTime()), Long.toString(onOrBeforeLoggedAt.getTime()) },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    private static final List ascendingBmlsWithNonNilColumn(final User user, final Date loggedSince, final BmlColumn nonNilColumn, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s is not null and %s > ? order by %s asc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                nonNilColumn.name,
                BmlColumn.LOGGED_AT.name,
                BmlColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(loggedSince.getTime()) },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    private static final List ascendingBmlsWithNonNilColumn(final User user, final BmlColumn nonNilColumn, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? and %s is not null order by %s asc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                nonNilColumn.name,
                BmlColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString() },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    public final List ascendingBmlsWithNonNilBodyWeight(final User user, final Date loggedSince) {
        return ascendingBmlsWithNonNilBodyWeight(user, loggedSince, dbHelper.getReadableDatabase());
    }

    private static final List ascendingBmlsWithNonNilBodyWeight(final User user, final Date loggedSince, final SQLiteDatabase database) {
        return ascendingBmlsWithNonNilColumn(user, loggedSince, BmlColumn.BODY_WEIGHT, database);
    }

    public final List ascendingBmlsWithNonNilBodyWeight(final User user) {
        return ascendingBmlsWithNonNilBodyWeight(user, dbHelper.getReadableDatabase());
    }

    private static final List ascendingBmlsWithNonNilBodyWeight(final User user, final SQLiteDatabase database) {
        return ascendingBmlsWithNonNilColumn(user, BmlColumn.BODY_WEIGHT, database);
    }

    public final List ascendingBmlsWithNonNilColumn(final User user, final BmlColumn nonNilColumn, final Date onOrAfterLoggedAt, final Date onOrBeforeLoggedAt) {
        return ascendingBmlsWithNonNilColumn(user, onOrAfterLoggedAt, onOrBeforeLoggedAt, nonNilColumn, dbHelper.getReadableDatabase());
    }

    public final List ascendingBmlsWithNonNilColumn(final User user, final BmlColumn nonNilColumn, final Date loggedSince) {
        return ascendingBmlsWithNonNilColumn(user, loggedSince, nonNilColumn, dbHelper.getReadableDatabase());
    }

    public final List ascendingBmlsWithNonNilColumn(final User user, final BmlColumn nonNilColumn) {
        return ascendingBmlsWithNonNilColumn(user, nonNilColumn, dbHelper.getReadableDatabase());
    }

    public final List ascendingBmls(final User user) {
        return ascendingBmls(user, dbHelper.getReadableDatabase());
    }

    private static final List ascendingBmls(final User user, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s asc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString() },
                null,
                cursor -> masterBmlFromCursor(cursor),
                database);
    }

    public final int numBmls(final User user) {
        return numBmls(user, dbHelper.getReadableDatabase());
    }

    private static int numBmls(final User user, final SQLiteDatabase database) {
        return intFromQuery(String.format("select count(*) from %s where %s = ?",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name),
                new String[] { user.localIdentifier.toString() },
                database);
    }

    public final int numBmlsWithNonNilBodyWeight(final User user) {
        return numBmlsWithNonNilBodyWeight(user, dbHelper.getReadableDatabase());
    }

    public final BmlCounts bmlCounts(final User user) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        return new BmlCounts(numBmls(user, sqLiteDatabase),
                numBmlsWithNonNilBodyWeight(user, sqLiteDatabase),
                numBmlsWithNonNilColumn(user, BmlColumn.ARM_SIZE, sqLiteDatabase),
                numBmlsWithNonNilColumn(user, BmlColumn.CHEST_SIZE, sqLiteDatabase),
                numBmlsWithNonNilColumn(user, BmlColumn.CALF_SIZE, sqLiteDatabase),
                numBmlsWithNonNilColumn(user, BmlColumn.THIGH_SIZE, sqLiteDatabase),
                numBmlsWithNonNilColumn(user, BmlColumn.FOREARM_SIZE, sqLiteDatabase),
                numBmlsWithNonNilColumn(user, BmlColumn.WAIST_SIZE, sqLiteDatabase),
                numBmlsWithNonNilColumn(user, BmlColumn.NECK_SIZE, sqLiteDatabase));
    }

    private static int numBmlsWithNonNilColumn(final User user, BmlColumn bmlColumn, final SQLiteDatabase database) {
        return intFromQuery(String.format("select count(*) from %s where %s = ? and %s is not null",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                bmlColumn.name),
                new String[] { user.localIdentifier.toString() },
                database);
    }

    private static int numBmlsWithNonNilBodyWeight(final User user, final SQLiteDatabase database) {
        return numBmlsWithNonNilColumn(user, BmlColumn.BODY_WEIGHT, database);
    }

    public final int numBmlsWithNonNilBodyWeight(final User user, final Date since) {
        return numBmlsWithNonNilBodyWeight(user, since, dbHelper.getReadableDatabase());
    }

    private static int numBmlsWithNonNilBodyWeight(final User user, final Date loggedSince, final SQLiteDatabase database) {
        return intFromQuery(String.format("select count(*) from %s where %s = ? and %s is not null and logged_at > ?",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.BODY_WEIGHT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(loggedSince.getTime()) },
                database);
    }

    private static BodyMeasurementLog nearestBmlWithNonNilBodyWeight(final User user, final Date nearestTo, final SQLiteDatabase database) {
        final long nearestToTime = nearestTo.getTime();
        final List bmlsLeadingUpTo = objectsFromQuery(String.format("select * from %s where %s = ? and %s is not null and %s <= ? order by %s desc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.BODY_WEIGHT.name,
                BmlColumn.LOGGED_AT.name,
                BmlColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(nearestToTime) },
                1,
                cursor -> masterBmlFromCursor(cursor),
                database);
        final List bmlsAfter = objectsFromQuery(String.format("select * from %s where %s = ? and %s is not null and %s >= ? order by %s desc",
                RikerTable.BML.tableName,
                CommonColumn.USER_ID.name,
                BmlColumn.BODY_WEIGHT.name,
                BmlColumn.LOGGED_AT.name,
                BmlColumn.LOGGED_AT.name),
                new String[] { user.localIdentifier.toString(), Long.toString(nearestToTime) },
                1,
                cursor -> masterBmlFromCursor(cursor),
                database);
        final int numBmlsLeadingUpTo = bmlsLeadingUpTo.size();
        final int numBmlsAfter = bmlsAfter.size();
        BodyMeasurementLog nearestBml = null;
        if (numBmlsLeadingUpTo > 0) {
            if (numBmlsAfter > 0) {
                final Date nearestLeadingUpToDate = ((BodyMeasurementLog)bmlsLeadingUpTo.get(numBmlsLeadingUpTo - 1)).loggedAt;
                final Date nearestAfterDate = ((BodyMeasurementLog)bmlsAfter.get(numBmlsAfter - 1)).loggedAt;
                if ((nearestToTime - nearestLeadingUpToDate.getTime()) <= (nearestAfterDate.getTime() - nearestToTime)) {
                    nearestBml = (BodyMeasurementLog)bmlsLeadingUpTo.get(numBmlsLeadingUpTo - 1);
                } else {
                    nearestBml = (BodyMeasurementLog)bmlsAfter.get(0);
                }
            } else {
                nearestBml = (BodyMeasurementLog)bmlsLeadingUpTo.get(numBmlsLeadingUpTo - 1);
            }
        } else if (numBmlsAfter > 0) {
            nearestBml = (BodyMeasurementLog)bmlsAfter.get(0);
        }
        return nearestBml;
    }

    public final void deleteBml(final BodyMeasurementLog bml) {
        deleteEntity(bml, RikerTable.BML);
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       User Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void populateUserDomainFieldsFromCursor(final User user, final Cursor cursor) {
        user.name = SqliteUtil.toString(cursor, UserColumn.NAME.name);
        user.email = SqliteUtil.toString(cursor, UserColumn.EMAIL.name);
        user.verifiedAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.VERIFIED_AT.name);
        user.newishMovementsAddedAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.NEW_MOVEMENTS_ADDED_AT.name);
        user.paidEnrollmentEstablishedAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.PAID_ENROLLMENT_ESTABLISHED_AT.name);
        user.isPaymentPastDue = SqliteUtil.toBoolean(cursor, UserColumn.IS_PAYMENT_PAST_DUE.name);
        user.paidEnrollmentCancelledAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.PAID_ENROLLMENT_CANCELLED_AT.name);
        user.finalFailedPaymentAttemptOccurredAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.FINAL_FAILED_PAYMENT_ATTEMPT_OCCURRED_AT.name);
        user.informedOfMaintenanceAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.INFORMED_OF_MAINTENANCE_AT.name);
        user.maintenanceStartsAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.MAINTENANCE_STARTS_AT.name);
        user.maintenanceDuration = SqliteUtil.toInteger(cursor, UserColumn.MAINTENANCE_DURATION.name);
        user.validateAppStoreReceiptAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.VALIDATE_APP_STORE_RECEIPT_AT.name);
        user.lastChargeId = SqliteUtil.toString(cursor, UserColumn.LAST_CHARGE_ID.name);
        user.trialAlmostExpiredNoticeSentAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.TRIAL_ALMOST_EXPIRED_NOTICE_SENT_AT.name);
        user.latestStripeTokenId = SqliteUtil.toString(cursor, UserColumn.LATEST_STRIPE_TOKEN_ID.name);
        user.nextInvoiceAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.NEXT_INVOICE_AT.name);
        user.lastInvoiceAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.LAST_INVOICE_AT.name);
        user.lastInvoiceAmount = SqliteUtil.toInteger(cursor, UserColumn.LAST_INVOICE_AMOUNT.name);
        user.currentCardLast4 = SqliteUtil.toString(cursor, UserColumn.CURRENT_CARD_LAST4.name);
        user.currentCardBrand = SqliteUtil.toString(cursor, UserColumn.CURRENT_CARD_BRAND.name);
        user.currentCardExpYear = SqliteUtil.toInteger(cursor, UserColumn.CURRENT_CARD_EXP_YEAR.name);
        user.currentCardExpMonth = SqliteUtil.toInteger(cursor, UserColumn.CURRENT_CARD_EXP_MONTH.name);
        user.trialEndsAt = SqliteUtil.toDateFromLongColType(cursor, UserColumn.TRIAL_ENDS_AT.name);
        user.stripeCustomerId = SqliteUtil.toString(cursor, UserColumn.STRIPE_CUSTOMER_ID.name);
        user.maxAllowedSetImport = SqliteUtil.toInteger(cursor, UserColumn.MAX_ALLOWED_SET_IMPORT.name);
        user.maxAllowedBmlImport = SqliteUtil.toInteger(cursor, UserColumn.MAX_ALLOWED_BML_IMPORT.name);
    }

    private static void populateUserDomainFieldsToContentValues(final User user, final ContentValues contentValues) {
        contentValues.put(UserColumn.NAME.name, user.name);
        contentValues.put(UserColumn.EMAIL.name, user.email);
        SqliteUtil.put(contentValues, UserColumn.VERIFIED_AT.name, user.verifiedAt);
        SqliteUtil.put(contentValues, UserColumn.NEW_MOVEMENTS_ADDED_AT.name, user.newishMovementsAddedAt);
        SqliteUtil.put(contentValues, UserColumn.PAID_ENROLLMENT_ESTABLISHED_AT.name, user.paidEnrollmentEstablishedAt);
        contentValues.put(UserColumn.IS_PAYMENT_PAST_DUE.name, user.isPaymentPastDue);
        SqliteUtil.put(contentValues, UserColumn.PAID_ENROLLMENT_CANCELLED_AT.name, user.paidEnrollmentCancelledAt);
        SqliteUtil.put(contentValues, UserColumn.FINAL_FAILED_PAYMENT_ATTEMPT_OCCURRED_AT.name, user.finalFailedPaymentAttemptOccurredAt);
        SqliteUtil.put(contentValues, UserColumn.INFORMED_OF_MAINTENANCE_AT.name, user.informedOfMaintenanceAt);
        SqliteUtil.put(contentValues, UserColumn.MAINTENANCE_STARTS_AT.name, user.maintenanceStartsAt);
        contentValues.put(UserColumn.MAINTENANCE_DURATION.name, user.maintenanceDuration);
        SqliteUtil.put(contentValues, UserColumn.VALIDATE_APP_STORE_RECEIPT_AT.name, user.validateAppStoreReceiptAt);
        contentValues.put(UserColumn.LAST_CHARGE_ID.name, user.lastChargeId);
        SqliteUtil.put(contentValues, UserColumn.TRIAL_ALMOST_EXPIRED_NOTICE_SENT_AT.name, user.trialAlmostExpiredNoticeSentAt);
        contentValues.put(UserColumn.LATEST_STRIPE_TOKEN_ID.name, user.latestStripeTokenId);
        SqliteUtil.put(contentValues, UserColumn.NEXT_INVOICE_AT.name, user.nextInvoiceAt);
        SqliteUtil.put(contentValues, UserColumn.LAST_INVOICE_AT.name, user.lastInvoiceAt);
        contentValues.put(UserColumn.LAST_INVOICE_AMOUNT.name, user.lastInvoiceAmount);
        contentValues.put(UserColumn.CURRENT_CARD_LAST4.name, user.currentCardLast4);
        contentValues.put(UserColumn.CURRENT_CARD_BRAND.name, user.currentCardBrand);
        contentValues.put(UserColumn.CURRENT_CARD_EXP_MONTH.name, user.currentCardExpMonth);
        contentValues.put(UserColumn.CURRENT_CARD_EXP_YEAR.name, user.currentCardExpYear);
        SqliteUtil.put(contentValues, UserColumn.TRIAL_ENDS_AT.name, user.trialEndsAt);
        contentValues.put(UserColumn.STRIPE_CUSTOMER_ID.name, user.stripeCustomerId);
        contentValues.put(UserColumn.MAX_ALLOWED_SET_IMPORT.name, user.maxAllowedSetImport);
        contentValues.put(UserColumn.MAX_ALLOWED_BML_IMPORT.name, user.maxAllowedBmlImport);
    }

    private static User masterUserFromCursor(final Cursor cursor) {
        final User user = new User();
        populateSupportFieldsFromCursor(user, cursor);
        user.localIdentifier = SqliteUtil.toInteger(cursor, CommonColumn.LOCAL_ID.name);
        populateUserDomainFieldsFromCursor(user, cursor);
        return user;
    }

    public final User user() {
        return user(dbHelper.getReadableDatabase());
    }

    private static User user(final SQLiteDatabase database) {
        User user = null;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(String.format("select * from %s", RikerTable.USER.tableName), EMPTY_SELECTION_ARGS);
            while (cursor.moveToNext()) {
                user = masterUserFromCursor(cursor);
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        return user;
    }

    private static void insertIntoUser(final User user, final SQLiteDatabase database) {
        final ContentValues userContentValues = new ContentValues();
        populateSupportFieldsToContentValues(user, userContentValues);
        populateUserDomainFieldsToContentValues(user, userContentValues);
        final long newUserLocalIdentifier = database.insert(RikerTable.USER.tableName, null, userContentValues);
        user.localIdentifier = (int)newUserLocalIdentifier;
    }

    public final void establishLocalUser() {
        // save new local user, and save new settings (with default values)
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            establishLocalUser(database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    private static void establishLocalUser(final SQLiteDatabase database) {
        final User user = new User();
        insertIntoUser(user, database);
        final UserSettings userSettings = new UserSettings();
        userSettings.weightUom = Constants.DEFAULT_WEIGHT_UOM_ID;
        userSettings.sizeUom = Constants.DEFAULT_SIZE_UOM_ID;
        userSettings.weightIncDecAmount = Constants.DEFAULT_WEIGHT_INC_DEC_AMOUNT;
        insertIntoUserSettings(userSettings, user, database);
    }

    public final void deleteAllDataAndResetLocalUser() {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            // delete chart config and chart cache
            deleteFromTable(RikerTable.CHART_TIME_SERIES_DATA_POINT.tableName, null, null, database);
            deleteFromTable(RikerTable.CHART_TIME_SERIES.tableName, null, null, database);
            deleteFromTable(RikerTable.CHART_PIE_SLICE.tableName, null, null, database);
            deleteFromTable(RikerTable.CHART.tableName, null, null, database);
            deleteFromTable(RikerTable.CHART_CONFIG.tableName, null, null, database);
            // delete sets
            deleteFromTable(RikerTable.SET.tableName, null, null, database);
            // delete bmls
            deleteFromTable(RikerTable.BML.tableName, null, null, database);
            // delete settings
            deleteFromTable(RikerTable.USER_SETTINGS.tableName, null, null, database);
            // delete user
            deleteFromTable(RikerTable.USER.tableName, null, null, database);
            // establish new local user
            establishLocalUser(database);
            database.setTransactionSuccessful(); // can't forget this!
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    private static int updateUser(final User user, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(user, contentValues);
        populateUserDomainFieldsToContentValues(user, contentValues);
        return database.update(RikerTable.USER.tableName,
                contentValues,
                String.format("%s = ?", CommonColumn.LOCAL_ID.name),
                new String[] { user.localIdentifier.toString() });
    }

    public final void saveUser(final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            updateUser(user, database);
            database.setTransactionSuccessful();
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally {
            database.endTransaction();
        }
    }

    public static final class DeepSaveUserResult {

        public int inserts = 0;
        public int updates = 0;
        public int deletes = 0;

        public final Map<RikerTable, Integer> insertCounts = new HashMap<>();
        public final Map<RikerTable, Integer> updateCounts = new HashMap<>();
        public final Map<RikerTable, Integer> deleteCounts = new HashMap<>();

        public DeepSaveUserResult() {
            final RikerTable allTables[] = RikerTable.values();
            for (final RikerTable rikerTable : allTables) {
                insertCounts.put(rikerTable, 0);
                updateCounts.put(rikerTable, 0);
                deleteCounts.put(rikerTable, 0);
            }
        }

        public final void insert(final RikerTable rikerTable) {
            inserts++;
            insertCounts.put(rikerTable, insertCounts.get(rikerTable) + 1);
        }

        public final void update(final RikerTable rikerTable) {
            updates++;
            updateCounts.put(rikerTable, updateCounts.get(rikerTable) + 1);
        }

        public final void delete(final RikerTable rikerTable) {
            deletes++;
            deleteCounts.put(rikerTable, deleteCounts.get(rikerTable) + 1);
        }
    }

    public enum SaveResult {
        DID_NOTHING,
        DID_UPDATE,
        DID_INSERT
    }

    private static Integer localIdFromGlobalId(final RikerTable rikerTable, final String globalIdentifier, final SQLiteDatabase database) {
        return integerFromTable(rikerTable.tableName,
                CommonColumn.LOCAL_ID.name,
                CommonColumn.GLOBAL_ID.name,
                globalIdentifier,
                database);
    }

    private static SaveResult saveNewOrExisting(final MasterSupport masterSupport,
                                                final RikerTable table,
                                                final Function.EntityDbOp updateFn,
                                                final Function.EntityDbOp insertFn,
                                                final DeepSaveUserResult deepSaveUserResult,
                                                final SQLiteDatabase database) {
        final Integer localId = localIdFromGlobalId(table, masterSupport.globalIdentifier, database);
        if (localId != null) {
            masterSupport.localIdentifier = localId;
            final Date localUpdatedAt = dateFromTable(table.tableName, CommonEntityColumn.UPDATED_AT.name, CommonColumn.LOCAL_ID.name, localId.toString(), database);
            if (masterSupport.updatedAt.getTime() > localUpdatedAt.getTime()) {
                updateFn.invoke(masterSupport, database);
                deepSaveUserResult.update(table);
                return SaveResult.DID_UPDATE;
            } else {
                return SaveResult.DID_NOTHING;
            }
        } else {
            insertFn.invoke(masterSupport, database);
            deepSaveUserResult.insert(table);
            return SaveResult.DID_INSERT;
        }
    }

    public final DeepSaveUserResult deepSaveUser(final User user) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            final DeepSaveUserResult deepSaveUserResult = new DeepSaveUserResult();
            final int updateCount = updateUser(user, database);
            if (updateCount > 0) {
                deepSaveUserResult.update(RikerTable.USER);
            }
            for (final BodySegment bodySegment : user.bodySegmentList) {
                saveNewOrExisting(bodySegment,
                        RikerTable.BODY_SEGMENT,
                        (bs, db) -> updateMasterBodySegment((BodySegment)bs, db),
                        (bs, db) -> insertMasterBodySegment((BodySegment)bs, database),
                        deepSaveUserResult,
                        database);
            }
            database.setTransactionSuccessful();
            return deepSaveUserResult;
        } catch (final Throwable t) {
            Crashlytics.logException(t);
            return null;
        } finally {
            database.endTransaction();
        }
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Movement Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void insertMovementMuscles(final List<Integer> muscleIdList,
                                              final int movementId,
                                              final String tableName,
                                              final SQLiteDatabase database) {
        if (muscleIdList != null) {
            for (final Integer muscleId : muscleIdList) {
                final ContentValues contentValues = new ContentValues();
                contentValues.put(MovementMuscleColumn.MOVEMENT_ID.name, movementId);
                contentValues.put(MovementMuscleColumn.MUSCLE_ID.name, muscleId);
                database.insert(tableName, null, contentValues);
            }
        }
    }

    public static void insertMasterMovement(final List<Object> movementAndAliases,
                                            final SQLiteDatabase database) {
        final Movement movement = (Movement)movementAndAliases.get(0);
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(movement, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, movement.localIdentifier);
        contentValues.put(MovementColumn.CANONICAL_NAME.name, movement.canonicalName);
        contentValues.put(MovementColumn.IS_BODY_LIFT.name, movement.isBodyLift);
        if (movement.percentageOfBodyWeight != null) {
            contentValues.put(MovementColumn.PERCENTAGE_OF_BODY_WEIGHT.name, movement.percentageOfBodyWeight.toString());
        }
        contentValues.put(MovementColumn.VARIANT_MASK.name, movement.variantMask);
        contentValues.put(MovementColumn.SORT_ORDER.name, movement.sortOrder);
        database.insert(RikerTable.MOVEMENT.tableName, null, contentValues);
        insertMovementMuscles(movement.primaryMuscleIdList, movement.localIdentifier, RikerTable.MOVEMENT_PRIMARY_MUSCLE.tableName, database);
        insertMovementMuscles(movement.secondaryMuscleIdList, movement.localIdentifier, RikerTable.MOVEMENT_SECONDARY_MUSCLE.tableName, database);
        final List<MovementAlias> movementAliasList = (List<MovementAlias>)movementAndAliases.get(1);
        for (final MovementAlias movementAlias : movementAliasList) {
            insertMasterMovementAlias(movementAlias, database);
        }
    }

    private static void populateMovementDomainFieldsFromCursor(final Movement movement, final Cursor cursor) {
        movement.canonicalName = SqliteUtil.toString(cursor, MovementColumn.CANONICAL_NAME.name);
        movement.variantMask = SqliteUtil.toInteger(cursor, MovementColumn.VARIANT_MASK.name);
        movement.isBodyLift = SqliteUtil.toBoolean(cursor, MovementColumn.IS_BODY_LIFT.name);
        movement.percentageOfBodyWeight = SqliteUtil.toBigDecimal(cursor, MovementColumn.PERCENTAGE_OF_BODY_WEIGHT.name);
        movement.sortOrder = SqliteUtil.toInteger(cursor, MovementColumn.SORT_ORDER.name);
    }

    private static Movement movementFromCursor(final Cursor cursor) {
        final Movement movement = new Movement();
        populateSupportFieldsFromCursor(movement, cursor);
        populateMovementDomainFieldsFromCursor(movement, cursor);
        return movement;
    }

    public final List<Movement> movementsWithNullMuscleIds() {
        return movementsWithNullMuscleIds(dbHelper.getReadableDatabase());
    }

    private static List<Movement> movementsWithNullMuscleIds(final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s", RikerTable.MOVEMENT.tableName),
                EMPTY_SELECTION_ARGS,
                null,
                cursor -> movementFromCursor(cursor),
                database);
    }

    private static List<Movement> movements(final SQLiteDatabase database) {
        final List<Movement> movements = movementsWithNullMuscleIds(database);
        for (final Movement movement : movements) {
            movement.primaryMuscleIdList = integersFromQuery(String.format("select %s from %s where %s = ?",
                    MovementMuscleColumn.MUSCLE_ID.name,
                    RikerTable.MOVEMENT_PRIMARY_MUSCLE.tableName,
                    MovementMuscleColumn.MOVEMENT_ID.name),
                    new String[] { movement.localIdentifier.toString() },
                    database);
            movement.secondaryMuscleIdList = integersFromQuery(String.format("select %s from %s where %s = ?",
                    MovementMuscleColumn.MUSCLE_ID.name,
                    RikerTable.MOVEMENT_SECONDARY_MUSCLE.tableName,
                    MovementMuscleColumn.MOVEMENT_ID.name),
                    new String[] { movement.localIdentifier.toString() },
                    database);
        }
        return movements;
    }

    public final List<Movement> movements() {
        return movements(dbHelper.getReadableDatabase());
    }

    public final List<Movement> movementsWithNullMuscleIds(final int muscleGroupId) {
        return movementsWithNullMuscleIds(muscleGroupId, dbHelper.getReadableDatabase());
    }

    private static List<Movement> movementsWithNullMuscleIds(final int muscleGroupId, final SQLiteDatabase database) {
        final List<Movement> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(String.format("select distinct mov.* from %s mov, %s m, %s mpm where mov.id = mpm.movement_id and m.id = mpm.muscle_id and m.muscle_group_id = ? order by mov.canonical_name collate nocase asc",
                    RikerTable.MOVEMENT.tableName,
                    RikerTable.MUSCLE.tableName,
                    RikerTable.MOVEMENT_PRIMARY_MUSCLE.tableName),
                    new String[]{Integer.toString(muscleGroupId)});
            while (cursor.moveToNext()) {
                final Movement movement = movementFromCursor(cursor);
                list.add(movement);
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        return list;
    }

    public final List<MovementSearchResult> searchMovements(final String searchText) {
        final List<MovementSearchResult> searchResultList = new ArrayList<>();
        Cursor cursor = null;
        try {
            final String searchQuery = String.format("select m.id, m.canonical_name, m.variant_mask, ma.alias, m.is_body_lift, m.sort_order, m.percentage_of_body_weight from %s m left outer join %s ma on ma.movement_id = m.id where m.canonical_name like '%%%s%%' or ma.alias like '%%%s%%'",
                    RikerTable.MOVEMENT.tableName,
                    RikerTable.MOVEMENT_ALIAS.tableName,
                    searchText,
                    searchText);
            cursor = dbHelper.getReadableDatabase().rawQuery(searchQuery, EMPTY_SELECTION_ARGS);
            final Map<Integer, MovementSearchResult> movementSearchResultMap = new HashMap<>();
            while (cursor.moveToNext()) {
                final Integer movementId = SqliteUtil.toInteger(cursor, CommonColumn.LOCAL_ID.name);
                MovementSearchResult movementSearchResult = movementSearchResultMap.get(movementId);
                if (movementSearchResult == null) {
                    movementSearchResult = new MovementSearchResult();
                    movementSearchResult.movementId = movementId;
                    movementSearchResult.canonicalName = SqliteUtil.toString(cursor, MovementColumn.CANONICAL_NAME.name);
                    movementSearchResult.variantMask = SqliteUtil.toInteger(cursor, MovementColumn.VARIANT_MASK.name);
                    movementSearchResult.isBodyLift = SqliteUtil.toBoolean(cursor, MovementColumn.IS_BODY_LIFT.name);
                    movementSearchResult.percentageOfBodyWeight = SqliteUtil.toBigDecimal(cursor, MovementColumn.PERCENTAGE_OF_BODY_WEIGHT.name);
                    movementSearchResult.sortOrder = SqliteUtil.toInteger(cursor, MovementColumn.SORT_ORDER.name);
                    movementSearchResultMap.put(movementId, movementSearchResult);
                    searchResultList.add(movementSearchResult);
                }
                final String alias = SqliteUtil.toString(cursor, "alias");
                if (alias != null) {
                    movementSearchResult.aliases.add(alias);
                }
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        return searchResultList;
    }

    public final List<Map<String, List<Movement>>> muscleGroupsAndMovements() {
        final Map<Integer, Map<String, Object>> mgsAndMovsDict = new HashMap<>();
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            final StringBuilder qry = new StringBuilder("select distinct mov.id as movement_id, mov.sort_order, mov.percentage_of_body_weight, ");
            qry.append("mov.global_identifier, mov.variant_mask, mov.canonical_name, mov.is_body_lift, ");
            qry.append("mg.id as muscle_group_id, mg.getName as muscle_group_name ");
            qry.append("from movement mov, movement_primary_muscle mpm, muscle m, ");
            qry.append("muscle_group mg where mov.id = mpm.movement_id and mpm.muscle_id = m.id and ");
            qry.append("m.muscle_group_id = mg.id order by mov.canonical_name collate nocase asc");
            cursor = database.rawQuery(qry.toString(), EMPTY_SELECTION_ARGS);
            while (cursor.moveToNext()) {
                final Integer mgId = SqliteUtil.toInteger(cursor, "muscle_group_id");
                Map<String, Object> mgNameAndMovs = mgsAndMovsDict.get(mgId);
                List<Movement> movements;
                if (mgNameAndMovs == null) {
                    mgNameAndMovs = new HashMap<>();
                    mgNameAndMovs.put("mg_name", SqliteUtil.toString(cursor, "muscle_group_name"));
                    movements = new ArrayList<>();
                    mgNameAndMovs.put("movs", movements);
                    mgsAndMovsDict.put(mgId, mgNameAndMovs);
                } else {
                    movements = (List<Movement>)mgNameAndMovs.get("movs");
                }
                final Movement movement = new Movement();
                movement.localIdentifier = SqliteUtil.toInteger(cursor, "movement_id");
                movement.globalIdentifier = SqliteUtil.toString(cursor, CommonColumn.GLOBAL_ID.name);
                populateMovementDomainFieldsFromCursor(movement, cursor);
                movements.add(movement);
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        final List mgsAndMovs = new ArrayList(mgsAndMovsDict.values());
        Collections.sort(mgsAndMovs, (o1, o2) -> {
            final Map<String, Object> obj1MgName = (Map<String, Object>)o1;
            final Map<String, Object> obj2MgName = (Map<String, Object>)o2;
            final String mgName1 = (String)obj1MgName.get("mg_name");
            final String mgName2 = (String)obj2MgName.get("mg_name");
            return mgName1.compareTo(mgName2);
        });
        // deviation from iOS impl (here in Android, we want a flat list of objects returned (not a list of maps))
        final List flattenedMgsAndMovs = new ArrayList();
        for (final Object mgNameAndMovsObj : mgsAndMovs) {
            final Map<String, Object> mgNameAndMovsMap = (Map<String, Object>)mgNameAndMovsObj;
            final String mgName = (String)mgNameAndMovsMap.get("mg_name");
            flattenedMgsAndMovs.add(mgName);
            final List<Movement> movements = (List<Movement>)mgNameAndMovsMap.get("movs");
            flattenedMgsAndMovs.addAll(movements);
        }
        return flattenedMgsAndMovs;
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Movement Alias Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    public static void insertMasterMovementAlias(final int movementId, final int movementAliasId, final String alias, final SQLiteDatabase database) {
        final MovementAlias movementAlias = new MovementAlias();
        movementAlias.movementId = movementId;
        movementAlias.localIdentifier = movementAliasId;
        movementAlias.alias = alias;
        movementAlias.globalIdentifier = Utils.globalIdentifier(UriPathPart.MOVEMENT_ALIASES, movementAlias.localIdentifier);
        setDefaultCreatedAtUpdatedAtDates(movementAlias);
        insertMasterMovementAlias(movementAlias, database);
    }

    public static void insertMasterMovementAlias(final MovementAlias movementAlias, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(movementAlias, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, movementAlias.localIdentifier);
        contentValues.put(MovementAliasColumn.MOVEMENT_ID.name, movementAlias.movementId);
        contentValues.put(MovementAliasColumn.ALIAS.name, movementAlias.alias);
        database.insert(RikerTable.MOVEMENT_ALIAS.tableName, null, contentValues);
    }

    private static MovementAlias movementAliasFromCursor(final Cursor cursor) {
        final MovementAlias movementAlias = new MovementAlias();
        populateSupportFieldsFromCursor(movementAlias, cursor);
        movementAlias.alias = SqliteUtil.toString(cursor, MovementAliasColumn.ALIAS.name);
        movementAlias.movementId = SqliteUtil.toInteger(cursor, MovementAliasColumn.MOVEMENT_ID.name);
        return movementAlias;
    }

    public final List<MovementAlias> movementAliases(final int movementId) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s asc",
                RikerTable.MOVEMENT_ALIAS.tableName,
                MovementAliasColumn.MOVEMENT_ID.name,
                MovementAliasColumn.ALIAS.name),
                new String[] { Integer.toString(movementId) },
                null,
                cursor -> movementAliasFromCursor(cursor),
                dbHelper.getReadableDatabase());
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Movement Variant Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    public static void insertMasterMovementVariant(final MovementVariant movementVariant, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(movementVariant, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, movementVariant.localIdentifier);
        contentValues.put(MovementVariantColumn.NAME.name, movementVariant.name);
        contentValues.put(MovementVariantColumn.ABBREV_NAME.name, movementVariant.abbrevName);
        contentValues.put(MovementVariantColumn.DESCRIPTION.name, movementVariant.variantDescription);
        contentValues.put(MovementVariantColumn.SORT_ORDER.name, movementVariant.sortOrder);
        database.insert(RikerTable.MOVEMENT_VARIANT.tableName, null, contentValues);
    }

    private static MovementVariant movementVariantFromCursor(final Cursor cursor) {
        final MovementVariant variant = new MovementVariant();
        populateSupportFieldsFromCursor(variant, cursor);
        variant.name = SqliteUtil.toString(cursor, MovementVariantColumn.NAME.name);
        variant.abbrevName = SqliteUtil.toString(cursor, MovementVariantColumn.ABBREV_NAME.name);
        variant.variantDescription = SqliteUtil.toString(cursor, MovementVariantColumn.DESCRIPTION.name);
        variant.sortOrder = SqliteUtil.toInteger(cursor, MovementVariantColumn.SORT_ORDER.name);
        return variant;
    }

    public final List<MovementVariant> movementVariants() {
        return movementVariants(dbHelper.getReadableDatabase());
    }

    private static List<MovementVariant> movementVariants(final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s order by %s asc",
                RikerTable.MOVEMENT_VARIANT.tableName,
                MovementVariantColumn.SORT_ORDER.name),
                EMPTY_SELECTION_ARGS,
                null,
                cursor -> movementVariantFromCursor(cursor),
                database);
    }

    public final List<MovementVariant> movementVariants(final int movementVariantMask) {
        return movementVariants(movementVariantMask, dbHelper.getReadableDatabase());
    }

    private static List<MovementVariant> movementVariants(final int movementVariantMask, final SQLiteDatabase database) {
        final List<MovementVariant> allVariants = movementVariants(database);
        final List<MovementVariant> variantList = new ArrayList<>();
        for (final MovementVariant variant : allVariants) {
            if ((variant.localIdentifier & movementVariantMask) >= 1) {
                variantList.add(variant);
            }
        }
        return variantList;
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Muscle Alias Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    public static void insertMasterMuscleAlias(final MuscleAlias muscleAlias, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(muscleAlias, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, muscleAlias.localIdentifier);
        contentValues.put(MuscleAliasColumn.MUSCLE_ID.name, muscleAlias.muscleId);
        contentValues.put(MuscleAliasColumn.ALIAS.name, muscleAlias.alias);
        database.insert(RikerTable.MUSCLE_ALIAS.tableName, null, contentValues);
    }

    private static MuscleAlias muscleAliasFromCursor(final Cursor cursor) {
        final MuscleAlias muscleAlias = new MuscleAlias();
        populateSupportFieldsFromCursor(muscleAlias, cursor);
        muscleAlias.alias = SqliteUtil.toString(cursor, MuscleAliasColumn.ALIAS.name);
        muscleAlias.muscleId = SqliteUtil.toInteger(cursor, MuscleAliasColumn.MUSCLE_ID.name);
        return muscleAlias;
    }

    public final List<MuscleAlias> muscleAliases(final int muscleId) {
        return objectsFromQuery(String.format("select * from %s where %s = ? order by %s asc",
                RikerTable.MUSCLE_ALIAS.tableName,
                MuscleAliasColumn.MUSCLE_ID.name,
                MuscleAliasColumn.ALIAS.name),
                new String[] { Integer.toString(muscleId) },
                null,
                cursor -> muscleAliasFromCursor(cursor),
                dbHelper.getReadableDatabase());
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Muscle Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static Muscle muscleFromCursor(final Cursor cursor) {
        final Muscle muscle = new Muscle();
        populateSupportFieldsFromCursor(muscle, cursor);
        muscle.canonicalName = SqliteUtil.toString(cursor, MuscleColumn.CANONICAL_NAME.name);
        muscle.abbrevCanonicalName = SqliteUtil.toString(cursor, MuscleColumn.ABBREV_CANONICAL_NAME.name);
        muscle.muscleGroupId = SqliteUtil.toInteger(cursor, MuscleColumn.MUSCLE_GROUP_ID.name);
        return muscle;
    }

    public static void insertMasterMuscle(final Muscle muscle, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(muscle, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, muscle.localIdentifier);
        contentValues.put(MuscleColumn.MUSCLE_GROUP_ID.name, muscle.muscleGroupId);
        contentValues.put(MuscleColumn.CANONICAL_NAME.name, muscle.canonicalName);
        contentValues.put(MuscleColumn.ABBREV_CANONICAL_NAME.name, muscle.abbrevCanonicalName);
        database.insert(RikerTable.MUSCLE.tableName, null, contentValues);
    }

    private static final List<Muscle> movementMuscles(final int movementId, final RikerTable movementMuscleTable, final SQLiteDatabase database) {
        return objectsFromQuery(String.format("SELECT * FROM %s where %s in (select %s from %s where %s = ?)",
                RikerTable.MUSCLE.tableName,
                CommonColumn.LOCAL_ID.name,
                MovementMuscleColumn.MUSCLE_ID.name,
                movementMuscleTable.tableName,
                MovementMuscleColumn.MOVEMENT_ID.name),
                new String[] { Integer.toString(movementId) },
                null,
                cursor -> muscleFromCursor(cursor),
                database);
    }

    public final List<Muscle> primaryMuscles(final int movementId) {
        return primaryMuscles(movementId, dbHelper.getReadableDatabase());
    }

    private static final List<Muscle> primaryMuscles(final int movementId, final SQLiteDatabase database) {
        return movementMuscles(movementId, RikerTable.MOVEMENT_PRIMARY_MUSCLE, database);
    }

    public final List<Muscle> secondaryMuscles(final int movementId) {
        return secondaryMuscles(movementId, dbHelper.getReadableDatabase());
    }

    private static final List<Muscle> secondaryMuscles(final int movementId, final SQLiteDatabase database) {
        return movementMuscles(movementId, RikerTable.MOVEMENT_SECONDARY_MUSCLE, database);
    }

    private static List<Muscle> muscles(final SQLiteDatabase database) {
        return objectsFromQuery(String.format("SELECT * FROM %s",
                RikerTable.MUSCLE.tableName),
                EMPTY_SELECTION_ARGS,
                null,
                cursor -> muscleFromCursor(cursor),
                database);
    }

    public final List<Muscle> muscles() {
        return muscles(dbHelper.getReadableDatabase());
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Muscle Group Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void populateMuscleGroupDomainFieldsFromCursor(final MuscleGroup muscleGroup, final Cursor cursor) {
        muscleGroup.name = SqliteUtil.toString(cursor, MuscleGroupColumn.NAME.name);
        muscleGroup.abbrevName = SqliteUtil.toString(cursor, MuscleGroupColumn.ABBREV_NAME.name);
        muscleGroup.bodySegmentId = SqliteUtil.toInteger(cursor, MuscleGroupColumn.BODY_SEGMENT_ID.name);
    }

    public static ContentValues contentValues(final MuscleGroup muscleGroup) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(muscleGroup, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, muscleGroup.localIdentifier);
        contentValues.put(MuscleGroupColumn.BODY_SEGMENT_ID.name, muscleGroup.bodySegmentId);
        contentValues.put(MuscleGroupColumn.NAME.name, muscleGroup.name);
        contentValues.put(MuscleGroupColumn.ABBREV_NAME.name, muscleGroup.abbrevName);
        return contentValues;
    }

    public static void insertMasterMuscleGroup(final MuscleGroup muscleGroup, final SQLiteDatabase database) {
        final ContentValues contentValues = contentValues(muscleGroup);
        database.insert(RikerTable.MUSCLE_GROUP.tableName, null, contentValues);
    }

    public final List<MuscleGroup> muscleGroups(final Integer bodySegmentId) {
        return muscleGroups(dbHelper.getReadableDatabase(), bodySegmentId);
    }

    public final List<MuscleGroup> muscleGroups() {
        return muscleGroups(dbHelper.getReadableDatabase());
    }

    private static List<MuscleGroup> muscleGroups(final SQLiteDatabase database) {
        return muscleGroups(database, null);
    }

    private static List<MuscleGroup> muscleGroups(final SQLiteDatabase database, final Integer bodySegmentId) {
        final List<MuscleGroup> list = new ArrayList<>();
        Cursor cursor = null;
        final StringBuilder query = new StringBuilder(String.format("select * from %s", RikerTable.MUSCLE_GROUP.tableName));
        String[] queryArgs;
        if (bodySegmentId != null) {
            query.append(String.format(" where %s = ?", MuscleGroupColumn.BODY_SEGMENT_ID.name));
            queryArgs = new String[] { Integer.toString(bodySegmentId) };
        } else {
            queryArgs = EMPTY_SELECTION_ARGS;
        }
        query.append(String.format(" order by %s asc", MuscleGroupColumn.NAME.name));
        try {
            cursor = database.rawQuery(query.toString(), queryArgs);
            while (cursor.moveToNext()) {
                final MuscleGroup muscleGroup = new MuscleGroup();
                populateSupportFieldsFromCursor(muscleGroup, cursor);
                populateMuscleGroupDomainFieldsFromCursor(muscleGroup, cursor);
                list.add(muscleGroup);
            }
        } catch (final Throwable t) {
            Crashlytics.logException(t);
        } finally { close(cursor); }
        return list;
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Origination Device Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    public static void insertMasterOriginationDevice(final OriginationDevice originationDevice, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(originationDevice, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, originationDevice.localIdentifier);
        contentValues.put(OriginationDeviceColumn.NAME.name, originationDevice.name);
        contentValues.put(OriginationDeviceColumn.HAS_LOCAL_IMAGE.name, originationDevice.hasLocalImage);
        contentValues.put(OriginationDeviceColumn.ICON_IMAGE_NAME.name, originationDevice.iconImageName);
        database.insert(RikerTable.ORIGINATION_DEVICE.tableName, null, contentValues);
    }

    private static OriginationDevice originationDeviceFromCursor(final Cursor cursor) {
        final OriginationDevice originationDevice = new OriginationDevice();
        populateSupportFieldsFromCursor(originationDevice, cursor);
        originationDevice.name = SqliteUtil.toString(cursor, OriginationDeviceColumn.NAME.name);
        originationDevice.hasLocalImage = SqliteUtil.toBoolean(cursor, OriginationDeviceColumn.HAS_LOCAL_IMAGE.name);
        originationDevice.iconImageName = SqliteUtil.toString(cursor, OriginationDeviceColumn.ICON_IMAGE_NAME.name);
        return originationDevice;
    }

    public final List<OriginationDevice> originationDevices() {
        return originationDevices(dbHelper.getReadableDatabase());
    }

    private static List<OriginationDevice> originationDevices(final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s order by %s asc",
                RikerTable.ORIGINATION_DEVICE.tableName,
                OriginationDeviceColumn.NAME.name),
                EMPTY_SELECTION_ARGS,
                null,
                cursor -> originationDeviceFromCursor(cursor),
                database);
    }

    /*==============================================================================================
    ************************************************************************************************
    ================================================================================================
       Body Segment Operations
    ================================================================================================
    ************************************************************************************************
    ==============================================================================================*/
    private static void populateDomainFieldsToContentValues(final BodySegment bodySegment, final ContentValues contentValues) {
        contentValues.put(BodySegmentColumn.NAME.name, bodySegment.name);
    }


    public static void insertMasterBodySegment(final BodySegment bodySegment, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(bodySegment, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, bodySegment.localIdentifier);
        populateDomainFieldsToContentValues(bodySegment, contentValues);
        database.insert(RikerTable.BODY_SEGMENT.tableName, null, contentValues);
    }

    public static void updateMasterBodySegment(final BodySegment bodySegment, final SQLiteDatabase database) {
        final ContentValues contentValues = new ContentValues();
        populateSupportFieldsToContentValues(bodySegment, contentValues);
        contentValues.put(CommonColumn.LOCAL_ID.name, bodySegment.localIdentifier);
        populateDomainFieldsToContentValues(bodySegment, contentValues);
        database.update(RikerTable.BODY_SEGMENT.tableName,
                contentValues,
                String.format("%s = ?", CommonColumn.LOCAL_ID.name),
                new String[] { bodySegment.localIdentifier.toString() });
    }

    private static BodySegment bodySegmentFromCursor(final Cursor cursor) {
        final BodySegment bodySegment = new BodySegment();
        populateSupportFieldsFromCursor(bodySegment, cursor);
        bodySegment.name = SqliteUtil.toString(cursor, BodySegmentColumn.NAME.name);
        return bodySegment;
    }

    public final List<BodySegment> bodySegments() {
        return bodySegments(dbHelper.getReadableDatabase());
    }

    private static List<BodySegment> bodySegments(final SQLiteDatabase database) {
        return objectsFromQuery(String.format("select * from %s order by %s desc",
                RikerTable.BODY_SEGMENT.tableName, BodySegmentColumn.NAME.name),
                EMPTY_SELECTION_ARGS,
                null,
                cursor -> bodySegmentFromCursor(cursor),
                database);
    }
}
