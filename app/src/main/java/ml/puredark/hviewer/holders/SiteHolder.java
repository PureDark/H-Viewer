package ml.puredark.hviewer.holders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.DBHelper;

/**
 * Created by PureDark on 2016/8/12.
 */

public class SiteHolder {
    private final static String dbName = "sites";
    private DBHelper dbHelper;

    public SiteHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    public void addSite(Site item) {
        if (item == null) return;
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", item.title);
        contentValues.put("indexUrl", item.indexUrl);
        contentValues.put("galleryUrl", item.galleryUrl);
        contentValues.put("json", new Gson().toJson(item));
        dbHelper.insert(dbName, contentValues);
    }

    public void deleteSite(Site item) {
        dbHelper.delete(dbName, "`sid` = ?",
                new String[]{item.sid+""});
    }

    public void updateSite(Site item){
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", item.title);
        contentValues.put("indexUrl", item.indexUrl);
        contentValues.put("galleryUrl", item.galleryUrl);
        contentValues.put("json", new Gson().toJson(item));
        dbHelper.update(dbName, contentValues, "sid = ?",
                new String[]{item.sid + ""});
    }

    public List<Site> getSites() {
        List<Site> sites = new ArrayList<>();

        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " ORDER BY `sid` DESC");
        while (cursor.moveToNext()) {
            int i = cursor.getColumnIndex("json");
            int id = cursor.getInt(0);
            if (i >= 0) {
                String json = cursor.getString(i);
                Site site = new Gson().fromJson(json, Site.class);
                site.sid = id;
                sites.add(site);
            }
        }

        return sites;
    }

    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}
