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
        checkNoGroupSites();
    }

    public synchronized int addSiteGroup(SiteGroup item) {
        if (item == null) return 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        return (int) dbHelper.insert(groupDbName, contentValues);
    }

    public synchronized int addSite(Site item) {
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

    public synchronized void deleteSiteGroup(SiteGroup item) {
        dbHelper.delete(groupDbName, "`gid` = ?",
                new String[]{item.gid + ""});
        dbHelper.delete(dbName, "`gid` = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void deleteSite(Site item) {
        dbHelper.delete(dbName, "`sid` = ?",
                new String[]{item.sid + ""});
    }

    public synchronized void updateSiteGroup(SiteGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void updateSite(Site item) {
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

    public synchronized void updateSiteGroupIndex(SiteGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void updateSiteIndex(Site item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`index`", item.index);
        dbHelper.update(dbName, contentValues, "sid = ?",
                new String[]{item.sid + ""});
    }

    public int getMaxGroupId() {
        Cursor cursor = dbHelper.query("SELECT MAX(`gid`) AS `maxid` FROM " + groupDbName);
        int maxId = (cursor.moveToNext()) ? cursor.getInt(0) : 0;
        cursor.close();
        return maxId;
    }

    public int getMaxSiteId() {
        Cursor cursor = dbHelper.query("SELECT MAX(`sid`) AS `maxid` FROM " + dbName);
        int maxId = (cursor.moveToNext()) ? cursor.getInt(0) : 0;
        cursor.close();
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
                cursor.close();
            }
        }
        groupCursor.close();

        return siteGroups;
    }

    public List<SiteGroup> getGroups() {
        List<SiteGroup> siteGroups = new ArrayList<>();

        Cursor groupCursor = dbHelper.query("SELECT * FROM " + groupDbName + " ORDER BY `index` ASC");

        while (groupCursor.moveToNext()) {
            int i = groupCursor.getColumnIndex("title");
            int gid = groupCursor.getInt(0);
            if (i >= 0) {
                String title = groupCursor.getString(i);
                SiteGroup group = new SiteGroup(gid, title);
                siteGroups.add(group);
            }
        }
        groupCursor.close();

        return siteGroups;
    }

    public void checkNoGroupSites() {
        // 检测是否有gid为0，无法显示的站点，如有则全部添加到新建的“未分类”组别中
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `gid` = 0");
        if (cursor.moveToNext()) {
            SiteGroup group = getGroupByTitle("未分类");
            int gid = (group != null) ? group.gid : addSiteGroup(new SiteGroup(0, "未分类"));
            dbHelper.nonQuery("UPDATE " + dbName + " SET `gid` = " + gid + " WHERE `gid` = 0");
        }
        cursor.close();
    }

    public SiteGroup getGroupByTitle(String title) {
        Cursor cursor = dbHelper.query("SELECT * FROM " + groupDbName + " WHERE `title` = '" + title + "' ORDER BY `index` ASC LIMIT 1");
        try {
            if (cursor.moveToNext()) {
                int gid = cursor.getInt(0);
                SiteGroup group = new SiteGroup(gid, title);
                return group;
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public SiteGroup getGroupById(int gid) {
        Cursor cursor = dbHelper.query("SELECT * FROM " + groupDbName + " WHERE `gid` = '" + gid + "' ORDER BY `index` ASC LIMIT 1");
        try {
            if (cursor.moveToNext()) {
                String title = cursor.getString(1);
                SiteGroup group = new SiteGroup(gid, title);
                return group;
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public Site getSiteByTitle(String title) {
        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " WHERE `title` = '" + title + "' ORDER BY `index` ASC LIMIT 1");
        try {
            if (cursor.moveToNext()) {
                int j = cursor.getColumnIndex("json");
                int id = cursor.getInt(0);
                if (j >= 0) {
                    String json = cursor.getString(j);
                    Site site = new Gson().fromJson(json, Site.class);
                    site.sid = id;
                    return site;
                }
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}
