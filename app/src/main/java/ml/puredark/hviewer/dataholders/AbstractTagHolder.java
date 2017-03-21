package ml.puredark.hviewer.dataholders;

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
