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

public class FavouriteHolder {
    private final static String dbName = "favourites";
    private DBHelper dbHelper;

    public FavouriteHolder(Context context) {
        dbHelper = new DBHelper();
        dbHelper.open(context);
    }

    public void addFavourite(LocalCollection item) {
        if (item == null) return;
        deleteFavourite(item);
        ContentValues contentValues = new ContentValues();
        contentValues.put("idCode", item.idCode);
        contentValues.put("title", item.title);
        contentValues.put("referer", item.referer);
        contentValues.put("json", new Gson().toJson(item));
        dbHelper.insert(dbName, contentValues);
    }

    public void clear() {
        dbHelper.delete(dbName, "", null);
    }

    public void deleteFavourite(Collection item) {
        dbHelper.delete(dbName, "`idCode` = ? AND `title` = ? AND `referer` = ?",
                new String[]{item.idCode, item.title, item.referer});
    }

    public List<Collection> getFavourites() {
        List<Collection> favourites = new ArrayList<>();

        Cursor cursor = dbHelper.query("SELECT * FROM " + dbName + " ORDER BY `fid` DESC");
        while (cursor.moveToNext()) {
            int i = cursor.getColumnIndex("json");
            int id = cursor.getInt(0);
            if (i >= 0) {
                String json = cursor.getString(i);
                Collection collection = new Gson().fromJson(json, LocalCollection.class);
                collection.cid = id;
                favourites.add(collection);
            }
        }
        cursor.close();

        return favourites;
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
            cursor.close();
        }
    }

    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}
