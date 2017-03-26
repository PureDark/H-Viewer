package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.helpers.Logger;

/**
 * Created by PureDark on 2016/10/12.
 */

public class FavorTagHolder extends AbstractTagHolder {
    private final static String dbName = "favorSiteTags";
    private DBHelper dbHelper;

    public FavorTagHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    public synchronized void addTag(Tag item) {
        Logger.d("FavorTagHolder", "tagExist(" + item + "):" + tagExist(item));
        if (item == null || tagExist(item)) return;
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", item.title);
        contentValues.put("url", item.url);
        dbHelper.insert(dbName, contentValues);
        Logger.d("FavorTagHolder", "inserted");
    }

    public synchronized void clear() {
        dbHelper.delete(dbName, "", null);
    }

    public synchronized void deleteTag(Tag item) {
        dbHelper.delete(dbName, "`title` = ?",
                new String[]{item.title});
    }

    public List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();

        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " ORDER BY `index` ASC");
        while (cursor.moveToNext()) {
            int tid = cursor.getInt(0);
            String title = cursor.getString(1);
            String url = cursor.getString(2);
            Tag tag = new Tag(tid, title);
            tag.url = TextUtils.isEmpty(url) ? null : url;
            tags.add(tag);
        }
        cursor.close();

        return tags;
    }

    public boolean tagExist(Tag item) {
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `title` = ?",
                new String[]{item.title});
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

    @Override
    public void addTag(int sid, Tag item) {
        addTag(item);
    }

    @Override
    public void clear(int sid) {
        clear();
    }

    @Override
    public void deleteTag(int sid, Tag item) {
        deleteTag(item);
    }

    @Override
    public List<Tag> getTags(int sid) {
        return getTags();
    }

    @Override
    public boolean tagExist(int sid, Tag item) {
        return tagExist(item);
    }
}
