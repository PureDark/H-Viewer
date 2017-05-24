package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.CollectionGroup;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Video;
import ml.puredark.hviewer.helpers.FileHelper;

import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_DOWNLOADING;
import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_WAITING;

/**
 * Created by PureDark on 2016/8/12.
 */

public class DownloadTaskHolder {
    private final static String dbName = "downloads";
    private final static String groupDbName = "dlGroups";
    private DBHelper dbHelper;

    public DownloadTaskHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
        checkNoGroupDLItems();
    }

    public synchronized void updateDownloadTasks(DownloadTask item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("idCode", item.collection.idCode);
        contentValues.put("title", item.collection.title);
        contentValues.put("referer", item.collection.referer);
        contentValues.put("json", new Gson().toJson(item));
        contentValues.put("gid", item.collection.gid);
        dbHelper.update(dbName, contentValues, "did = ?",
                new String[]{item.did + ""});
    }

    public synchronized int addDlGroup(CollectionGroup item) {
        if (item == null) return 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        return (int) dbHelper.insert(groupDbName, contentValues);
    }

    public int addDownloadTask(DownloadTask item) {
        if (item == null) return -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("idCode", item.collection.idCode);
        contentValues.put("title", item.collection.title);
        contentValues.put("referer", item.collection.referer);
        contentValues.put("json", new Gson().toJson(item));
        contentValues.put("`gid`", item.collection.gid);
        int newId = (int) dbHelper.insert(dbName, contentValues);
        item.did = newId;
        return newId;
    }

    public synchronized void deleteDlGroup(CollectionGroup item) {
        dbHelper.delete(groupDbName, "`gid` = ?",
                new String[]{item.gid + ""});
        dbHelper.delete(dbName, "`gid` = ?",
                new String[]{item.gid + ""});
    }

    public void deleteDownloadTask(DownloadTask item) {
        dbHelper.delete(dbName, "`did` = ?",
                new String[]{item.did + ""});
    }

    public synchronized void updateDlGroup(CollectionGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`title`", item.title);
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void updateDlGroupIndex(CollectionGroup item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`index`", item.index);
        dbHelper.update(groupDbName, contentValues, "gid = ?",
                new String[]{item.gid + ""});
    }

    public synchronized void updateDownloadItemIndex(DownloadTask item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("`gid`", item.collection.gid);
        contentValues.put("`index`", item.collection.index);
        dbHelper.update(dbName, contentValues, "did = ?",
                new String[]{item.did + ""});
    }

    public synchronized int getMaxTaskId() {
        Cursor cursor = dbHelper.query("SELECT MAX(`did`) AS `maxid` FROM " + dbName);
        int maxId = (cursor.moveToNext()) ? cursor.getInt(0) : 0;
        return maxId;
    }

    public synchronized List<DownloadTask> getDownloadingTasksFromDB() {
        List<DownloadTask> downloadingTasks = new ArrayList<>();
        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " WHERE `gid` = -1 ORDER BY `did` DESC");
        while (cursor.moveToNext()) {
            int j = cursor.getColumnIndex("json");
            if (j >= 0) {
                try {
                    int id = cursor.getInt(0);
                    String json = cursor.getString(j);
                    DownloadTask downloadTask = new Gson().fromJson(json, DownloadTask.class);
                    downloadTask.did = id;
                    downloadingTasks.add(downloadTask);
                } catch (Exception e){
                    continue;
                }
            }
        }
        cursor.close();

        return downloadingTasks;
    }

    public synchronized List<Pair<CollectionGroup, List<DownloadTask>>> getDownloadedTasksFromDB() {
        List<Pair<CollectionGroup, List<DownloadTask>>> dlGroups = new ArrayList<>();

        Cursor groupCursor = dbHelper.query("SELECT * FROM " + groupDbName + " ORDER BY `index` ASC");

        while (groupCursor.moveToNext()) {
            int i = groupCursor.getColumnIndex("title");
            if (i >= 0) {
                int gid = groupCursor.getInt(0);
                String title = groupCursor.getString(i);
                CollectionGroup group = new CollectionGroup(gid, title);
                List<DownloadTask> downloadTasks = new ArrayList<>();
                Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " WHERE `gid` = " + group.gid + " ORDER BY `index` ASC");
                while (cursor.moveToNext()) {
                    try {
                        int j = cursor.getColumnIndex("json");
                        int id = cursor.getInt(0);
                        if (j >= 0) {
                            String json = cursor.getString(j);
                            DownloadTask downloadTask = new Gson().fromJson(json, DownloadTask.class);
                            downloadTask.did = id;
                            downloadTask.collection.gid = group.gid;
                            downloadTasks.add(downloadTask);
                        }
                    } catch (Exception e){
                        continue;
                    }
                }
                dlGroups.add(new Pair<>(group, downloadTasks));
                cursor.close();
            }
        }
        groupCursor.close();

        return dlGroups;
    }

    public synchronized int scanPathForDownloadTask(String rootPath, String... subDirs) {
        try {
            DocumentFile root = FileHelper.getDirDocument(rootPath, subDirs);
            DocumentFile[] dirs = root.listFiles();
            int count = 0;
            for (DocumentFile dir : dirs) {
                if (dir.isDirectory()) {
                    DocumentFile file = dir.findFile("detail.txt");
                    if (file != null && file.isFile() && file.exists() && file.canRead()) {
                        String detail = FileHelper.readString(file);
                        DownloadTask task = new Gson().fromJson(detail, DownloadTask.class);
                        task.status = DownloadTask.STATUS_COMPLETED;
                        task.collection.gid = 0;
                        if (!isInList(task)) {
                            count++;
                            addDownloadTask(task);
                        }
                    }
                }
            }
            checkNoGroupDLItems();
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isInList(DownloadTask item) {
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `idCode` = ? AND `title` = ? AND `referer` = ?",
                new String[]{item.collection.idCode, item.collection.title, item.collection.referer});
        if (cursor.moveToNext())
            return true;
        else
            return false;
    }

    public void checkNoGroupDLItems() {
        // 检测是否有gid为0，无法显示的下载记录，如有则全部添加到新建的“未分类”组别中
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `gid` = 0");
        if (cursor.moveToNext()) {
            CollectionGroup group = getGroupByTitle("未分类");
            int gid = (group != null) ? group.gid : addDlGroup(new CollectionGroup(0, "未分类"));
            dbHelper.nonQuery("UPDATE " + dbName + " SET `gid` = " + gid + " WHERE `gid` = 0");
        }
        cursor.close();
    }

    public CollectionGroup getGroupByTitle(String title) {
        Cursor cursor = dbHelper.query("SELECT * FROM " + groupDbName + " WHERE `title` = '" + title + "' ORDER BY `index` ASC LIMIT 1");
        try {
            if (cursor.moveToNext()) {
                int gid = cursor.getInt(0);
                CollectionGroup group = new CollectionGroup(gid, title);
                return group;
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
