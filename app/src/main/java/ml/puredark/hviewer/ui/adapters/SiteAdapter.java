package ml.puredark.hviewer.ui.adapters;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableDraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.expandable.GroupPositionItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.libraries.advrecyclerview.common.widget.ExpandableItemIndicator;
import ml.puredark.hviewer.ui.dataproviders.ExpandableDataProvider;
import ml.puredark.hviewer.utils.ViewUtil;

public class SiteAdapter extends AbstractExpandableItemAdapter<SiteAdapter.SiteGroupViewHolder, SiteAdapter.SiteViewHolder>
        implements ExpandableDraggableItemAdapter<SiteAdapter.SiteGroupViewHolder, SiteAdapter.SiteViewHolder> {
    public int selectedSid = 0;
    private ExpandableDataProvider<SiteGroup, Site> mProvider;
    private OnItemClickListener mItemClickListener;
    private OnItemMoveListener onItemMoveListener;
    private MaterialAnimatedSwitch.OnCheckedChangeListener mOnCheckedChangeListener;

    public SiteAdapter(ExpandableDataProvider<SiteGroup, Site> mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }

    public ExpandableDataProvider<SiteGroup, Site> getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ExpandableDataProvider<SiteGroup, Site> mProvider) {
        this.mProvider = mProvider;
    }

    @Override
    public SiteGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_site_group, parent, false);
        SiteGroupViewHolder vh = new SiteGroupViewHolder(v);
        return vh;
    }

    @Override
    public SiteViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_site, parent, false);
        SiteViewHolder vh = new SiteViewHolder(v);
        return vh;
    }

    @Override
    public void onBindGroupViewHolder(SiteGroupViewHolder holder, final int groupPosition, int viewType) {
        if (groupPosition == getGroupCount() - 1) {
            holder.ivIcon.setImageResource(R.drawable.ic_create_new_group_black);
            holder.tvTitle.setText("添加新分类");
            holder.indicator.setVisibility(View.GONE);
        } else {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(R.drawable.ic_group_black);
            SiteGroup group = mProvider.getGroupItem(groupPosition);
            holder.tvTitle.setText(group.title);
            int expandState = holder.getExpandStateFlags();
            boolean isExpanded = ((expandState & ExpandableItemConstants.STATE_FLAG_IS_EXPANDED) != 0);
            boolean animateIndicator = ((expandState & ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);
            holder.indicator.setExpandedState(isExpanded, animateIndicator);
        }
        holder.container.setOnClickListener(v -> {
            if (mItemClickListener != null && groupPosition >= 0)
                mItemClickListener.onGroupClick(v, groupPosition);
        });
        holder.container.setOnLongClickListener(v -> {
            if (mItemClickListener != null && groupPosition >= 0 && groupPosition < getGroupCount() - 1)
                return mItemClickListener.onGroupLongClick(v, groupPosition);
            else
                return false;
        });
    }

    @Override
    public void onBindChildViewHolder(final SiteViewHolder holder, final int groupPosition, final int childPosition, int viewType) {
        Site site = mProvider.getChildItem(groupPosition, childPosition);
        int rID = R.drawable.ic_filter_9_plus_black;
        switch (childPosition) {
            case 0:
                rID = R.drawable.ic_filter_1_black;
                break;
            case 1:
                rID = R.drawable.ic_filter_2_black;
                break;
            case 2:
                rID = R.drawable.ic_filter_3_black;
                break;
            case 3:
                rID = R.drawable.ic_filter_4_black;
                break;
            case 4:
                rID = R.drawable.ic_filter_5_black;
                break;
            case 5:
                rID = R.drawable.ic_filter_6_black;
                break;
            case 6:
                rID = R.drawable.ic_filter_7_black;
                break;
            case 7:
                rID = R.drawable.ic_filter_8_black;
                break;
            case 8:
                rID = R.drawable.ic_filter_9_black;
                break;
        }
        holder.ivIcon.setImageResource(rID);
        holder.tvTitle.setText(site.title);
        if (selectedSid == site.sid) {
            holder.container.setBackgroundResource(R.color.black_10);
            holder.switchListGrid.setVisibility(View.VISIBLE);
            if (holder.switchListGrid.isChecked() != site.isGrid)
                new Handler().postDelayed(() -> holder.switchListGrid.toggle(), 100);
        } else {
            holder.container.setBackgroundDrawable(null);
            holder.switchListGrid.setVisibility(View.GONE);
        }

        holder.container.setOnClickListener(v -> {
            if (mItemClickListener != null && childPosition >= 0)
                mItemClickListener.onItemClick(v, groupPosition, childPosition);
        });
        holder.container.setOnLongClickListener(v -> {
            if (mItemClickListener != null && childPosition >= 0)
                return mItemClickListener.onItemLongClick(v, groupPosition, childPosition);
            else
                return false;
        });
        holder.switchListGrid.setOnClickListener(view -> holder.switchListGrid.toggle());
        if (mOnCheckedChangeListener != null)
            holder.switchListGrid.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    @Override
    public int getGroupCount() {
        return (mProvider == null) ? 1 : mProvider.getGroupCount() + 1;
    }

    @Override
    public int getChildCount(int groupPosition) {
        if (groupPosition == getGroupCount() - 1)
            return 0;
        else
            return (mProvider == null) ? 0 : mProvider.getChildCount(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        if (groupPosition == getGroupCount() - 1)
            return 0;
        else if (mProvider == null)
            return 0;
        else
            return mProvider.getGroupItem(groupPosition).getGroupId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        if (groupPosition == getGroupCount() - 1)
            return 0;
        else if (mProvider == null)
            return 0;
        else
            return mProvider.getChildItem(groupPosition, childPosition).getChildId();
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(SiteGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        return true;
    }

    // 以下是拖拽排序相关实现

    @Override
    public boolean onCheckGroupCanStartDrag(SiteGroupViewHolder holder, int groupPosition, int x, int y) {
        if (groupPosition == getGroupCount() - 1)
            return false;
        final View dragHandleView = holder.ivIcon;
        return ViewUtil.hitTest(dragHandleView, x, y);
    }

    @Override
    public boolean onCheckChildCanStartDrag(SiteViewHolder holder, int groupPosition, int childPosition, int x, int y) {
        final View dragHandleView = holder.ivIcon;
        return ViewUtil.hitTest(dragHandleView, x, y);
    }

    @Override
    public ItemDraggableRange onGetGroupItemDraggableRange(SiteGroupViewHolder holder, int groupPosition) {
        int end = Math.max(0, mProvider.getGroupCount() - 1);
        return new GroupPositionItemDraggableRange(0, end);
    }

    @Override
    public ItemDraggableRange onGetChildItemDraggableRange(SiteViewHolder holder, int groupPosition, int childPosition) {
        int end = Math.max(0, mProvider.getGroupCount() - 1);
        return new GroupPositionItemDraggableRange(0, end);
    }

    @Override
    public void onMoveGroupItem(int fromGroupPosition, int toGroupPosition) {
        if (fromGroupPosition == toGroupPosition) {
            return;
        }
        mProvider.moveGroupItem(fromGroupPosition, toGroupPosition);
        if (onItemMoveListener != null)
            onItemMoveListener.onGroupMove(fromGroupPosition, toGroupPosition);
    }

    @Override
    public void onMoveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        if ((fromGroupPosition == toGroupPosition && fromChildPosition == toChildPosition)
                || fromGroupPosition >= mProvider.getGroupCount() || toGroupPosition >= mProvider.getGroupCount()) {
            return;
        }
        mProvider.moveChildItem(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
        if (onItemMoveListener != null)
            onItemMoveListener.onItemMove(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
    }

    @Override
    public boolean onCheckGroupCanDrop(int draggingGroupPosition, int dropGroupPosition) {
        if (draggingGroupPosition >= mProvider.getGroupCount() || dropGroupPosition >= mProvider.getGroupCount())
            return false;
        return true;
    }

    @Override
    public boolean onCheckChildCanDrop(int draggingGroupPosition, int draggingChildPosition, int dropGroupPosition, int dropChildPosition) {
        if (draggingGroupPosition >= mProvider.getGroupCount() || dropGroupPosition >= mProvider.getGroupCount() ||
                draggingChildPosition >= mProvider.getChildCount(draggingGroupPosition) || dropChildPosition >= mProvider.getChildCount(dropChildPosition))
            return false;
        return true;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setOnItemMoveListener(OnItemMoveListener listener) {
        this.onItemMoveListener = listener;
    }

    public void setOnCheckedChangeListener(MaterialAnimatedSwitch.OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public interface OnItemClickListener {

        void onGroupClick(View v, int groupPosition);

        boolean onGroupLongClick(View v, int groupPosition);

        void onItemClick(View v, int groupPosition, int childPosition);

        boolean onItemLongClick(View v, int groupPosition, int childPosition);
    }

    public interface OnItemMoveListener {
        void onGroupMove(int fromGroupPosition, int toGroupPosition);

        void onItemMove(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);
    }

    public class SiteGroupViewHolder extends AbstractDraggableItemViewHolder
            implements ExpandableItemViewHolder {
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.indicator)
        ExpandableItemIndicator indicator;
        @BindView(R.id.container)
        MaterialRippleLayout container;

        private int mExpandStateFlags;

        public SiteGroupViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public int getExpandStateFlags() {
            return mExpandStateFlags;
        }

        @Override
        public void setExpandStateFlags(int flag) {
            mExpandStateFlags = flag;
        }
    }

    public class SiteViewHolder extends AbstractDraggableItemViewHolder
            implements ExpandableItemViewHolder {
        @BindView(R.id.container)
        MaterialRippleLayout container;
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.switch_list_grid)
        MaterialAnimatedSwitch switchListGrid;

        private int mExpandStateFlags;

        public SiteViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public int getExpandStateFlags() {
            return mExpandStateFlags;
        }

        @Override
        public void setExpandStateFlags(int flag) {
            mExpandStateFlags = flag;
        }
    }
}