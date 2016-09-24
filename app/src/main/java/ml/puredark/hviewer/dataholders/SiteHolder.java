package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;

/**
 * Created by PureDark on 2016/8/12.
 */

public class SiteHolder {
    private final static String dbName = "sites";
    private final static String groupDbName = "siteGroups";
    private DBHelper dbHelper;

    public SiteHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    public void addSiteGroup(SiteGroup item) {
        if (item == null) return;
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        dbHelper.insert(groupDbName, contentValues);
    }

    public int addSite(Site item) {
        if (item == null) return -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`indexUrl`", item.indexUrl);
        contentValues.put("`galleryUrl`", item.galleryUrl);
        contentValues.put("`index`", item.index);
        contentValues.put("`gid`", item.gid);
        contentValues.put("`json`", new Gson().toJson(item));
        return (int) dbHelper.insert(dbName, contentValues);
    }

    public void deleteSiteGroup(SiteGroup item) {
        dbHelper.delete(groupDbName, "`gid` = ?",
                new String[]{item.gid + ""});
        dbHelper.delete(dbName, "`gid` = ?",
                new String[]{item.gid + ""});
    }

    public void deleteSite(Site item) {
        dbHelper.delete(dbName, "`sid` = ?",
                new String[]{item.sid + ""});
    }

    public void updateSiteGroup(SiteGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public void updateSite(Site item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`indexUrl`", item.indexUrl);
        contentValues.put("`galleryUrl`", item.galleryUrl);
        contentValues.put("`index`", item.index);
        contentValues.put("`gid`", item.gid);
        contentValues.put("`json`", new Gson().toJson(item));
        dbHelper.update(dbName, contentValues, "sid = ?",
                new String[]{item.sid + ""});
    }

    public void updateSiteGroupIndex(SiteGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public void updateSiteIndex(Site item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`index`", item.index);
        dbHelper.update(dbName, contentValues, "sid = ?",
                new String[]{item.sid + ""});
    }

    public int getMaxGroupId() {
        Cursor cursor = dbHelper.query("SELECT MAX(`gid`) AS `maxid` FROM " + groupDbName);
        int maxId = (cursor.moveToNext()) ? cursor.getInt(0) : 0;
        return maxId;
    }

    public int getMaxSiteId() {
        Cursor cursor = dbHelper.query("SELECT MAX(`sid`) AS `maxid` FROM " + dbName);
        int maxId = (cursor.moveToNext()) ? cursor.getInt(0) : 0;
        return maxId;
    }

    public List<Pair<SiteGroup, List<Site>>> getSites() {
        List<Pair<SiteGroup, List<Site>>> siteGroups = new ArrayList<>();

        Cursor groupCursor = dbHelper.query("SELECT * FROM " + groupDbName + " ORDER BY `index` ASC");

        while (groupCursor.moveToNext()) {
            int i = groupCursor.getColumnIndex("title");
            int gid = groupCursor.getInt(0);
            if (i >= 0) {
                String title = groupCursor.getString(i);
                SiteGroup group = new SiteGroup(gid, title);

                List<Site> sites = new ArrayList<>();
                Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " WHERE `gid` = " + gid + " ORDER BY `index` ASC");
                while (cursor.moveToNext()) {
                    int j = cursor.getColumnIndex("json");
                    int id = cursor.getInt(0);
                    if (j >= 0) {
                        String json = cursor.getString(j);
                        Site site = new Gson().fromJson(json, Site.class);
                        site.sid = id;
                        sites.add(site);
                    }
                }
                siteGroups.add(new Pair<>(group, sites));
            }
        }


        return siteGroups;
    }

    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}
