package ml.puredark.hviewer.beans;


import ml.puredark.hviewer.libraries.advrecyclerview.common.data.AbstractExpandableDataProvider;

/**
 * Created by PureDark on 2016/9/21.
 */

public class CollectionGroup extends AbstractExpandableDataProvider.GroupData {
    public int gid, index;
    public String title;

    public CollectionGroup(int gid, String title) {
        this.gid = gid;
        this.title = title;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    @Override
    public int getId() {
        return gid;
    }

    @Override
    public long getGroupId() {
        return gid;
    }

    @Override
    public String getText() {
        return title;
    }

    @Override
    public String toString(){
        return "gid="+gid+" title="+title;
    }
}
