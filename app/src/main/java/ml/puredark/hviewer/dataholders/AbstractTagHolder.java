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

public abstract class AbstractTagHolder {

    public abstract void addTag(int sid, Tag item);

    public abstract void clear(int sid);

    public abstract void deleteTag(int sid, Tag item);

    public abstract List<Tag> getTags(int sid);

    public abstract boolean tagExist(int sid, Tag item);

}
