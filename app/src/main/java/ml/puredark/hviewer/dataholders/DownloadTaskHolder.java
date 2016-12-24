package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.provider.DocumentFile;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.helpers.FileHelper;

/**
 * Created by PureDark on 2016/8/12.
 */

public class DownloadTaskHolder {
    private final static String dbName = "downloads";
    private static List<DownloadTask> downloadTasks;
    private DBHelper dbHelper;

    public DownloadTaskHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    public void saveDownloadTasks() {
        if(downloadTasks==null)
            return;
        for (DownloadTask item : downloadTasks) {
            updateDownloadTasks(item);
        }
    }

    public void updateDownloadTasks(DownloadTask item){
        ContentValues contentValues = new ContentValues();
        contentValues.put("idCode", item.collection.idCode);
        contentValues.put("title", item.collection.title);
        contentValues.put("referer", item.collection.referer);
        contentValues.put("json", new Gson().toJson(item));
        dbHelper.update(dbName, contentValues, "did = ?",
                new String[]{item.did + ""});
    }

    public int addDownloadTask(DownloadTask item) {
        if (item == null) return -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("idCode", item.collection.idCode);
        contentValues.put("title", item.collection.title);
        contentValues.put("referer", item.collection.referer);
        contentValues.put("json", new Gson().toJson(item));
        int newId = (int) dbHelper.insert(dbName, contentValues);
        item.did = newId;
        downloadTasks.add(item);
        return newId;
    }

    public void deleteDownloadTask(DownloadTask item) {
        dbHelper.delete(dbName, "`did` = ?",
                new String[]{item.did + ""});
        downloadTasks.remove(item);
    }

    public int getMaxTaskId() {
        Cursor cursor = dbHelper.query("SELECT MAX(`did`) AS `maxid` FROM " + dbName);
        int maxId = (cursor.moveToNext()) ? cursor.getInt(0) : 0;
        return maxId;
    }

    public List<DownloadTask> getDownloadTasks(){
        if(downloadTasks==null)
            downloadTasks = getDownloadTasksFromDB();
        return downloadTasks;
    }

    public List<DownloadTask> getDownloadTasksFromDB() {
        List<DownloadTask> downloadTasks = new ArrayList<>();

        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " ORDER BY `did` DESC");
        while (cursor.moveToNext()) {
            int i = cursor.getColumnIndex("json");
            int id = cursor.getInt(0);
            if (i >= 0) {
                String json = cursor.getString(i);
                DownloadTask downloadTask = new Gson().fromJson(json, DownloadTask.class);
                downloadTask.did = id;
                downloadTasks.add(downloadTask);
            }
        }

        return downloadTasks;
    }

    public int scanPathForDownloadTask(String rootPath, String... subDirs){
        getDownloadTasks();
        int count = 0;
        try {
            DocumentFile root = FileHelper.getDirDocument(rootPath, subDirs);
            DocumentFile[] dirs = root.listFiles();

            for (DocumentFile dir : dirs) {
                if (dir.isDirectory()) {
                    DocumentFile file = dir.findFile("detail.txt");
                    if (file != null && file.isFile() && file.exists() && file.canRead()) {
                        String detail = FileHelper.readString(file);
                        DownloadTask task = new Gson().fromJson(detail, DownloadTask.class);
                        task.status = DownloadTask.STATUS_COMPLETED;
                        LocalCollection collection = task.collection;

                        if (collection.filename == null) {
                            collection.filename = collection.cover.substring(collection.cover.lastIndexOf("%2F")+3, collection.cover.length());
                            if (collection.filename.contains("/")) {
                                collection.filename = collection.filename.substring(collection.filename.lastIndexOf("/") + 1, collection.filename.length());
                            }
                        }
                        DocumentFile ifile = dir.findFile(collection.filename);
                        if (ifile != null) collection.cover = ifile.getUri().toString();
                        for (Picture picture : collection.pictures) {
                            if (picture.filename == null) {
                                picture.filename = picture.thumbnail.substring(picture.thumbnail.lastIndexOf("%2F")+3,picture.thumbnail.length());
                                if (picture.filename.contains("/")) {
                                    picture.filename = picture.filename.substring(picture.filename.lastIndexOf("/") + 1, picture.filename.length());
                                }
                            }
                            ifile = dir.findFile(picture.filename);
                            if (ifile != null) {
                                picture.thumbnail = ifile.getUri().toString();
                                picture.pic = ifile.getUri().toString();
                            }
                        }
                        FileHelper.writeString(HViewerApplication.getGson().toJson(task), "detail.txt", rootPath, dir.getName());
                        if (!isInList(task)) {
                            addDownloadTask(task);
                        } else {
                            updateDownloadTasks(task);
                        }
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            count = -1;
            e.printStackTrace();
        }
        return count;
    }

    public boolean isInList(DownloadTask item) {
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `idCode` = ? AND `title` = ? AND `referer` = ?",
                new String[]{item.collection.idCode, item.collection.title, item.collection.referer});
        if (cursor.moveToNext())
            return true;
        else
            return false;
    }

    public void setAllPaused() {
        if(downloadTasks==null)
            return;
        for (DownloadTask task : downloadTasks) {
            if (task.status == DownloadTask.STATUS_DOWNLOADING) {
                task.status = DownloadTask.STATUS_PAUSED;
            }
            for (Picture picture : task.collection.pictures) {
                if (picture.status == Picture.STATUS_DOWNLOADING) {
                    picture.status = Picture.STATUS_WAITING;
                }
            }
        }
    }

    public void onDestroy() {
        if (dbHelper != null) {
            saveDownloadTasks();
            dbHelper.close();
        }
    }
}
