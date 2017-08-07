package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ml.puredark.hviewer.beans.MarketSource;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2017/8/7.
 */

public class MarketSourceHolder {
    private final static String dbName = "marketSources";
    private DBHelper dbHelper;

    public MarketSourceHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    public synchronized int addSource(MarketSource item) {
        if (item == null) return -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", item.name);
        contentValues.put("jsonUrl", item.jsonUrl);
        long id = dbHelper.insert(dbName, contentValues);
        Logger.d("MarketSourceHolder", "inserted");
        return (int)id;
    }

    public synchronized void clear() {
        dbHelper.delete(dbName, "", null);
    }

    public synchronized void deleteSource(MarketSource item) {
        dbHelper.delete(dbName, "`msid` = ?",
                new String[]{item.msid+""});
    }

    public List<MarketSource> getMarketSources() {
        List<MarketSource> sources = new ArrayList<>();

        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " ORDER BY `msid` ASC");
        while (cursor.moveToNext()) {
            int msid = cursor.getInt(0);
            String name = cursor.getString(1);
            String jsonUrl = cursor.getString(2);
            MarketSource source = new MarketSource(msid, name, jsonUrl);
            sources.add(source);
        }
        cursor.close();

        return sources;
    }

    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
