package ml.puredark.hviewer.ui.dataproviders;

import android.support.v4.util.Pair;

import java.util.List;

import ml.puredark.hviewer.libraries.advrecyclerview.common.data.AbstractExpandableDataProvider;

public class ExpandableDataProvider<G extends AbstractExpandableDataProvider.GroupData, C extends AbstractExpandableDataProvider.ChildData>
        extends AbstractExpandableDataProvider<G, C> {
    private List<Pair<G, List<C>>> mData;

    public ExpandableDataProvider(List<Pair<G, List<C>>> mData) {
        this.mData = mData;
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mData.get(groupPosition).second.size();
    }

    @Override
    public G getGroupItem(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        return mData.get(groupPosition).first;
    }

    @Override
    public C getChildItem(int groupPosition, int childPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        final List<C> children = mData.get(groupPosition).second;

        if (childPosition < 0 || childPosition >= children.size()) {
            throw new IndexOutOfBoundsException("childPosition = " + childPosition);
        }

        return children.get(childPosition);
    }

    @Override
    public void moveGroupItem(int fromGroupPosition, int toGroupPosition) {
        if (fromGroupPosition == toGroupPosition) {
            return;
        }

        final Pair<G, List<C>> item = mData.remove(fromGroupPosition);
        mData.add(toGroupPosition, item);
    }

    @Override
    public void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        if ((fromGroupPosition == toGroupPosition) && (fromChildPosition == toChildPosition)) {
            return;
        }

        final Pair<G, List<C>> fromGroup = mData.get(fromGroupPosition);
        final Pair<G, List<C>> toGroup = mData.get(toGroupPosition);

        final C item = fromGroup.second.remove(fromChildPosition);
        toGroup.second.add(toChildPosition, item);
    }

    @Override
    public void removeGroupItem(int groupPosition) {
        mData.remove(groupPosition);
    }

    @Override
    public void removeChildItem(int groupPosition, int childPosition) {
        mData.get(groupPosition).second.remove(childPosition);
    }

    public Pair<G, List<C>> getItem(int position){
        return mData.get(position);
    }

    public void setDataSet(List<Pair<G, List<C>>> data) {
        mData = data;
    }
}
