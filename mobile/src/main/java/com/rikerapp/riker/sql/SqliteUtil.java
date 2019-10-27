package com.rikerapp.riker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigDecimal;
import java.util.Date;

public class SqliteUtil {

    private SqliteUtil() {}

    public static final void put(final ContentValues cv, final String columnName, final Date date) {
        if (date != null) {
            cv.put(columnName, date.getTime());
        } else {
            cv.put(columnName, (String)null);
        }
    }

    public static final String[] toArgs(final long value) {
        return new String[] { Long.toString(value) };
    }

    public static final boolean toBoolean(final Cursor cursor, final String columnName) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex) == 1;
    }

    public static final boolean toBoolean(final Cursor cursor, final String columnName, final boolean defaultValue) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(columnIndex)) {
            return defaultValue;
        }
        return cursor.getInt(columnIndex) == 1;
    }

    public static final Date toDateFromLongColType(final Cursor cursor, final String columnName) {
        final int colIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(colIndex)) {
            return null;
        }
        return new Date(cursor.getLong(colIndex));
    }

    public static final Integer toInteger(final Cursor cursor, final String columnName) {
        final int colIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(colIndex)) {
            return null;
        }
        return cursor.getInt(colIndex);
    }

    public static final int toInt(final Cursor cursor, final String columnName, final int defaultValue) {
        final int colIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(colIndex)) {
            return defaultValue;
        }
        return cursor.getInt(colIndex);
    }

    public static final float toFloat(final Cursor cursor, final String columnName, final float defaultValue) {
        final int colIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(colIndex)) {
            return defaultValue;
        }
        return cursor.getFloat(colIndex);
    }

    public static final String toString(final Cursor cursor, final String columnName) {
        final int colIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(colIndex)) {
            return null;
        }
        return cursor.getString(colIndex);
    }

    public static final BigDecimal toBigDecimal(final Cursor cursor, final String columnName) {
        final int colIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(colIndex)) {
            return null;
        }
        return new BigDecimal(cursor.getString(colIndex));
    }

    public static final void enableForeignKeys(final SQLiteDatabase database) {
        database.execSQL("PRAGMA foreign_keys = ON");
    }
}
