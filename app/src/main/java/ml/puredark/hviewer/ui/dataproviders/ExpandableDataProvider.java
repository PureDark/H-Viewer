package ml.puredark.hviewer.ui.dataproviders;

import android.support.v4.util.Pair;

import java.util.List;

import ml.puredark.hviewer.libraries.advrecyclerview.common.data.AbstractExpandableDataProvider;

public class ExpandableDataProvider<G extends AbstractExpandableDataProvider.GroupData, C extends AbstractExpandableDataProvider.ChildData>
        extends AbstractExpandableDataProvider<G, C> {
    private List<Pair<G, List<C>>> mData;
    private C mLastRemovedItem;
    private int mLastRemovedGroupPosition = -1;
    private int mLastRemovedChildPosition = -1;

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


    public int getAllChildCount() {
        int size = 0;
        for (Pair<G, List<C>> pair : mData) {
            size += pair.second.size();
        }
        return size;
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
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition + ", childPosition = " + childPosition);
        }

        final List<C> children = getItem(groupPosition).second;

        if (childPosition < 0 || childPosition >= children.size()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition + ", childPosition = " + childPosition);
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
        mLastRemovedGroupPosition = -1;
        mLastRemovedChildPosition = -1;
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
        mLastRemovedGroupPosition = -1;
        mLastRemovedChildPosition = -1;
    }

    @Override
    public void removeGroupItem(int groupPosition) {
        mData.remove(groupPosition);
    }

    @Override
    public void removeChildItem(int groupPosition, int childPosition) {
        try {
            mLastRemovedItem = mData.get(groupPosition).second.remove(childPosition);
            mLastRemovedGroupPosition = groupPosition;
            mLastRemovedChildPosition = childPosition;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pair<G, List<C>> getItem(int position) {
        return mData.get(position);
    }

    public void setDataSet(List<Pair<G, List<C>>> data) {
        mData = data;
    }

    public C undoLastRemoval() {
        if (mLastRemovedItem != null) {
            int insertedGroupPosition, insertedChildPosition;
            if (mLastRemovedGroupPosition >= 0 && mLastRemovedGroupPosition < mData.size()) {
                insertedGroupPosition = mLastRemovedGroupPosition;
                if (mLastRemovedChildPosition >= 0 && mLastRemovedChildPosition < mData.get(mLastRemovedGroupPosition).second.size()) {
                    insertedChildPosition = mLastRemovedChildPosition;
                } else {
                    insertedChildPosition = mData.get(mLastRemovedGroupPosition).second.size();
                }
            } else {
                insertedGroupPosition = mData.size();
                insertedChildPosition = mData.get(mData.size() - 1).second.size();
            }

            mData.get(insertedGroupPosition).second.add(insertedChildPosition, mLastRemovedItem);

            C temp = mLastRemovedItem;

            mLastRemovedItem = null;
            mLastRemovedGroupPosition = -1;
            mLastRemovedChildPosition = -1;

            return temp;
        } else {
            return null;
        }
    }
}
