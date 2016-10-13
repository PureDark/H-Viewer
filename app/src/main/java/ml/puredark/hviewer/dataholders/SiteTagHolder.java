package ml.puredark.hviewer.dataholders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Tag;

/**
 * Created by PureDark on 2016/10/12.
 */

public class SiteTagHolder extends AbstractTagHolder{
    private final static String dbName = "siteTags";
    private DBHelper dbHelper;

    public SiteTagHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    @Override
    public void addTag(int sid, Tag item) {
        if (item == null || tagExist(sid, item)) return;
        ContentValues contentValues = new ContentValues();
        contentValues.put("sid", sid);
        contentValues.put("title", item.title);
        contentValues.put("url", item.url);
        dbHelper.insert(dbName, contentValues);
    }

    @Override
    public void clear(int sid) {
        dbHelper.delete(dbName, "`sid` = " + sid, null);
    }

    @Override
    public void deleteTag(int sid, Tag item) {
        dbHelper.delete(dbName, "`sid` = " + sid + " AND `title` = ?",
                new String[]{item.title});
    }

    @Override
    public List<Tag> getTags(int sid) {
        return getRandomTags(sid, 30);
    }

    public List<Tag> getRandomTags(int sid, int limit) {
        List<Tag> tags = new ArrayList<>();

        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " WHERE `sid` = " + sid + " ORDER BY RANDOM() LIMIT " + limit);
        while (cursor.moveToNext()) {
            String title = cursor.getString(1);
            String url = cursor.getString(2);
            Tag tag = new Tag(tags.size() + 1, title);
            tag.url = TextUtils.isEmpty(url) ? null : url;
            tags.add(tag);
        }

        return tags;
    }

    @Override
    public boolean tagExist(int sid, Tag item) {
        Cursor cursor = dbHelper.query("SELECT 1 FROM " + dbName + " WHERE `sid` = " + sid + " AND `title` = ?",
                new String[]{item.title});
        if (cursor.moveToNext())
            return true;
        else
            return false;
    }

    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}
