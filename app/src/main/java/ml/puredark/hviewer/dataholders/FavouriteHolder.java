package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.FavGroup;
import ml.puredark.hviewer.beans.LocalCollection;

/**
 * Created by PureDark on 2016/8/12.
 */

public class FavouriteHolder {
    private final static String dbName = "favourites";
    private final static String groupDbName = "favGroups";
    private DBHelper dbHelper;

    public FavouriteHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
        checkNoGroupFavs();
    }

    public synchronized int addFavGroup(FavGroup item) {
        if (item == null) return 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        return (int) dbHelper.insert(groupDbName, contentValues);
    }

    public synchronized void addFavourite(LocalCollection item) {
        if (item == null) return;
        deleteFavourite(item);
        ContentValues contentValues = new ContentValues();
        contentValues.put("idCode", item.idCode);
        contentValues.put("title", item.title);
        contentValues.put("referer", item.referer);
        contentValues.put("`index`", item.index);
        contentValues.put("`gid`", item.gid);
        contentValues.put("json", new Gson().toJson(item));
        dbHelper.insert(dbName, contentValues);
    }

    public synchronized void deleteFavGroup(FavGroup item) {
        dbHelper.delete(groupDbName, "`gid` = ?",
                new String[]{item.gid + ""});
        dbHelper.delete(dbName, "`gid` = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void deleteFavourite(Collection item) {
        dbHelper.delete(dbName, "`idCode` = ? AND `title` = ? AND `referer` = ?",
                new String[]{item.idCode, item.title, item.referer});
    }

    public synchronized void updateFavGroup(FavGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void updateFavGroupIndex(FavGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void updateFavouriteIndex(LocalCollection item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`gid`", item.gid);
        contentValues.put("`index`", item.index);
        dbHelper.update(dbName, contentValues, "fid = ?",
                new String[]{item.cid + ""});
    }

    public List<Pair<FavGroup, List<LocalCollection>>> getFavourites() {
        List<Pair<FavGroup, List<LocalCollection>>> favGroups = new ArrayList<>();

        Cursor groupCursor = dbHelper.query("SELECT * FROM " + groupDbName + " ORDER BY `index` ASC");

        while (groupCursor.moveToNext()) {
            int i = groupCursor.getColumnIndex("title");
            int gid = groupCursor.getInt(0);
            if (i >= 0) {
                String title = groupCursor.getString(i);
                FavGroup group = new FavGroup(gid, title);
                List<LocalCollection> favourites = new ArrayList<>();
                Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " WHERE `gid` = " + group.gid + " ORDER BY `fid` DESC");
                while (cursor.moveToNext()) {
                    int j = cursor.getColumnIndex("json");
                    int id = cursor.getInt(0);
                    if (j >= 0) {
                        String json = cursor.getString(j);
                        LocalCollection collection = new Gson().fromJson(json, LocalCollection.class);
                        collection.cid = id;
                        favourites.add(collection);
                    }
                }
                favGroups.add(new Pair<>(group, favourites));
                cursor.close();
            }
        }
        groupCursor.close();

        return favGroups;
    }

    public List<FavGroup> getGroups() {
        List<FavGroup> favGroups = new ArrayList<>();

        Cursor groupCursor = dbHelper.query("SELECT * FROM " + groupDbName + " ORDER BY `index` ASC");

        while (groupCursor.moveToNext()) {
            int i = groupCursor.getColumnIndex("title");
            int gid = groupCursor.getInt(0);
            if (i >= 0) {
                String title = groupCursor.getString(i);
                FavGroup group = new FavGroup(gid, title);
                favGroups.add(group);
            }
        }
        groupCursor.close();

        return favGroups;
    }

    public void checkNoGroupFavs() {
        // 检测是否有gid为0，无法显示的收藏，如有则全部添加到新建的“未分类”组别中
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `gid` = 0");
        if (cursor.moveToNext()) {
            FavGroup group = getGroupByTitle("未分类");
            int gid = (group != null) ? group.gid : addFavGroup(new FavGroup(0, "未分类"));
            dbHelper.nonQuery("UPDATE " + dbName + " SET `gid` = " + gid + " WHERE `gid` = 0");
        }
        cursor.close();
    }

    public FavGroup getGroupByTitle(String title) {
        Cursor cursor = dbHelper.query("SELECT * FROM " + groupDbName + " WHERE `title` = '" + title + "' ORDER BY `index` ASC LIMIT 1");
        try {
            if (cursor.moveToNext()) {
                int gid = cursor.getInt(0);
                FavGroup group = new FavGroup(gid, title);
                return group;
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public synchronized void clear() {
        dbHelper.delete(dbName, "", null);
    }

    public boolean isFavourite(Collection item) {
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `idCode` = ? AND `title` = ? AND `referer` = ?",
                new String[]{item.idCode, item.title, item.referer});
        try {
            if (cursor == null || !cursor.moveToNext())
                return false;
            else
                return true;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}
