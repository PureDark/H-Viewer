package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.LocalCollection;

/**
 * Created by PureDark on 2016/8/12.
 */

public class HistoryHolder {
    private final static String dbName = "histories";
    private DBHelper dbHelper;

    public HistoryHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    public synchronized void addHistory(LocalCollection item) {
        if (item == null) return;
        deleteHistory(item);
        ContentValues contentValues = new ContentValues();
        contentValues.put("idCode", item.idCode);
        contentValues.put("title", item.title);
        contentValues.put("referer", item.referer);
        contentValues.put("json", new Gson().toJson(item));
        dbHelper.insert(dbName, contentValues);
    }

    public synchronized void clear() {
        dbHelper.delete(dbName, "", null);
    }

    public synchronized void deleteHistory(Collection item) {
        dbHelper.delete(dbName, "`idCode` = ? AND `title` = ? AND `referer` = ?",
                new String[]{item.idCode, item.title, item.referer});
    }

    public synchronized void trimHistory() {
        dbHelper.nonQuery("DELETE FROM " + dbName + " WHERE `hid` NOT IN (SELECT `hid` FROM " + dbName + " ORDER BY `hid` DESC LIMIT 0, 20)");
    }

    public List<Collection> getHistories() {
        List<Collection> histories = new ArrayList<>();

        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " ORDER BY `hid` DESC");
        while (cursor.moveToNext()) {
            int i = cursor.getColumnIndex("json");
            int id = cursor.getInt(0);
            if (i >= 0) {
                String json = cursor.getString(i);
                Collection collection = new Gson().fromJson(json, LocalCollection.class);
                collection.cid = id;
                histories.add(collection);
            }
        }
        cursor.close();

        return histories;
    }

    public void onDestroy() {
        if (dbHelper != null) {
            trimHistory();
            dbHelper.close();
        }
    }
}
