package ml.puredark.hviewer.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper {
    private static DBHelper sDBHelper = null;
    private SQLiteHelper mSqliteHelper = null;

    private DBHelper() {
    }

    public synchronized static DBHelper instance() {
        if (sDBHelper == null) {
            sDBHelper = new DBHelper();
        }

        return sDBHelper;
    }

    public synchronized void open(Context context, String dbName) {
        close();
        mSqliteHelper = new SQLiteHelper(context, dbName, null, 1);
    }

    public synchronized void insert(String sql) {
        if (mSqliteHelper == null) {
            return;
        }
        mSqliteHelper.getWritableDatabase().execSQL(sql);
    }

    public synchronized long insert(String table, ContentValues values) {
        if (mSqliteHelper == null) {
            return -1;
        }
        return mSqliteHelper.getWritableDatabase().insert(table, null, values);
    }

    public synchronized int update(String table, ContentValues values,
                                   String whereClause, String[] whereArgs) {
        if (mSqliteHelper == null) {
            return -1;
        }
        return mSqliteHelper.getWritableDatabase().update(table, values,
                whereClause, whereArgs);
    }

    public synchronized Cursor query(String table, String[] columns,
                                     String selection, String[] selectionArgs, String groupBy,
                                     String having, String orderBy) {
        if (mSqliteHelper == null) {
            return null;
        }
        return mSqliteHelper.getReadableDatabase().query(table, columns,
                selection, selectionArgs, groupBy, having, orderBy);
    }

    public synchronized Cursor query(String sql) {
        if (mSqliteHelper == null) {
            return null;
        }
        return mSqliteHelper.getReadableDatabase().rawQuery(sql, null);
    }

    public synchronized int delete(String table, String whereClause,
                                   String[] whereArgs) {
        if (mSqliteHelper == null) {
            return -1;
        }
        return mSqliteHelper.getReadableDatabase().delete(table, whereClause,
                whereArgs);
    }

    public synchronized void close() {
        if (mSqliteHelper != null) {
            mSqliteHelper.close();
            mSqliteHelper = null;
        }
    }

    public class SQLiteHelper extends SQLiteOpenHelper {

        final private String CREATE_TABLE_SQL = "create table dict(_id integer primary key autoincrement, word, detail)";

        public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
            super(context, name, factory, version);
        }

        //1、创建SQLiteOpenHelper的子类，并重写onCreate及onUpgrade方法。
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

}
