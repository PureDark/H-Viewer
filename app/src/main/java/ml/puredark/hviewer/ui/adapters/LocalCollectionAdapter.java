package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableDraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableSwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.expandable.GroupPositionItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.CollectionGroup;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.SiteFlagHandler;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.libraries.advrecyclerview.common.widget.ExpandableItemIndicator;
import ml.puredark.hviewer.ui.dataproviders.ExpandableDataProvider;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.ViewUtil;

public class LocalCollectionAdapter
        extends AbstractExpandableItemAdapter<LocalCollectionAdapter.GroupViewHolder, LocalCollectionAdapter.CollectionViewHolder>
        implements ExpandableDraggableItemAdapter<LocalCollectionAdapter.GroupViewHolder, LocalCollectionAdapter.CollectionViewHolder>,
        ExpandableSwipeableItemAdapter<LocalCollectionAdapter.GroupViewHolder, LocalCollectionAdapter.CollectionViewHolder> {
    private Context context;
    private ExpandableDataProvider<CollectionGroup, LocalCollection> mProvider;
    private RecyclerViewExpandableItemManager mExpandableItemManager;
    private OnItemClickListener mItemClickListener;
    private OnItemEventListener onItemEventListener;
    private CollectionTagAdapter.OnItemClickListener mTagClickListener;

    public LocalCollectionAdapter(Context context, ExpandableDataProvider<CollectionGroup, LocalCollection> mProvider) {
        this.mProvider = mProvider;
        this.context = context;
        setHasStableIds(true);
    }


    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection_group, parent, false);
        GroupViewHolder vh = new GroupViewHolder(v);
        return vh;
    }

    @Override
    public CollectionViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection, parent, false);
        CollectionViewHolder vh = new CollectionViewHolder(v);
        return vh;
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder, final int groupPosition, int viewType) {
        if (groupPosition == getGroupCount() - 1) {
            holder.ivIcon.setImageResource(R.drawable.ic_create_new_group_black);
            holder.tvTitle.setText("添加新分类");
            holder.indicator.setVisibility(View.GONE);
        } else {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(R.drawable.ic_group_black);
            CollectionGroup group = mProvider.getGroupItem(groupPosition);
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
    public void onBindChildViewHolder(final CollectionViewHolder holder, final int groupPosition, final int childPosition, int viewType) {
        LocalCollection collection = mProvider.getChildItem(groupPosition, childPosition);
        String cookie = (collection.site == null) ? "" : collection.site.cookie;
        if (holder.ivCover != null)
            ImageLoader.loadImageFromUrl(context, holder.ivCover, collection.cover, cookie, collection.referer);
        holder.tvTitle.setText(collection.title);
        holder.tvUploader.setText(collection.uploader);
        holder.tvCategory.setText(collection.category);
        if (collection.tags == null) {
            holder.tvTitle.setMaxLines(2);
            CollectionTagAdapter adapter = new CollectionTagAdapter(new ListDataProvider<>(new ArrayList()));
            adapter.setOnItemClickListener(mTagClickListener);
            holder.rvTags.setAdapter(adapter);
        } else {
            holder.tvTitle.setMaxLines(1);
            CollectionTagAdapter adapter = new CollectionTagAdapter(new ListDataProvider<>(collection.tags));
            adapter.setOnItemClickListener(mTagClickListener);
            holder.rvTags.setAdapter(adapter);
        }
        holder.rbRating.setRating(collection.rating);
        holder.tvSubmittime.setText(collection.datetime);
        final long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, childPosition);
        final int flatPosition = mExpandableItemManager.getFlatPosition(expandablePosition);
        holder.layoutCover.setVisibility(View.VISIBLE);
        holder.tvTitle.setVisibility(View.VISIBLE);
        holder.rbRating.setVisibility(View.VISIBLE);
        holder.rvTags.setVisibility(View.VISIBLE);
        checkSiteFlags(holder, collection.site);
        checkSiteFlags(flatPosition, collection.site, collection);
        holder.rippleLayout.setOnClickListener(v -> {
            if (mItemClickListener != null && childPosition >= 0)
                mItemClickListener.onItemClick(v, groupPosition, childPosition);
        });
        holder.rippleLayout.setOnLongClickListener(v -> {
            if (mItemClickListener != null && childPosition >= 0)
                return mItemClickListener.onItemLongClick(v, groupPosition, childPosition);
            else
                return false;
        });
    }

    private void checkSiteFlags(int position, Site site, Collection collection) {
        if (site != null && site.hasFlag(Site.FLAG_PRELOAD_GALLERY) && !collection.preloaded) {
            SiteFlagHandler.preloadGallery(context, this, position, site, collection, null);
        }
    }

    private void checkSiteFlags(CollectionViewHolder holder, Site site) {
        if(site==null)
            return;
        if (site.hasFlag(Site.FLAG_NO_COVER)) {
            holder.layoutCover.setVisibility(View.GONE);
        }
        if (site.hasFlag(Site.FLAG_NO_TITLE)) {
            holder.tvTitle.setVisibility(View.GONE);
            if (holder.rvTags != null) {
                holder.rvTags.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.HORIZONTAL));
            }
        } else {
            if (holder.rvTags != null) {
                holder.rvTags.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.HORIZONTAL));
            }
        }
        if (site.hasFlag(Site.FLAG_NO_RATING)) {
            holder.rbRating.setVisibility(View.GONE);
        }
        if (site.hasFlag(Site.FLAG_NO_TAG)) {
            holder.tvTitle.setMaxLines(2);
            holder.rvTags.setVisibility(View.GONE);
            holder.rvTags.setAdapter(
                    new CollectionTagAdapter(new ListDataProvider<>(new ArrayList()))
            );
        }
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
    public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        return true;
    }

    // 以下是拖拽排序相关实现

    @Override
    public boolean onCheckGroupCanStartDrag(GroupViewHolder holder, int groupPosition, int x, int y) {
        if (groupPosition == getGroupCount() - 1)
            return false;
        final View dragHandleView = holder.ivIcon;
        return ViewUtil.hitTest(dragHandleView, x, y);
    }

    @Override
    public boolean onCheckChildCanStartDrag(CollectionViewHolder holder, int groupPosition, int childPosition, int x, int y) {
        final View dragHandleView = holder.ivCover;
        return ViewUtil.hitTest(dragHandleView, x, y);
    }

    @Override
    public ItemDraggableRange onGetGroupItemDraggableRange(GroupViewHolder holder, int groupPosition) {
        int end = Math.max(0, mProvider.getGroupCount() - 1);
        return new GroupPositionItemDraggableRange(0, end);
    }

    @Override
    public ItemDraggableRange onGetChildItemDraggableRange(CollectionViewHolder holder, int groupPosition, int childPosition) {
        int end = Math.max(0, mProvider.getGroupCount() - 1);
        return new GroupPositionItemDraggableRange(0, end);
    }

    @Override
    public void onMoveGroupItem(int fromGroupPosition, int toGroupPosition) {
        if (fromGroupPosition == toGroupPosition) {
            return;
        }
        mProvider.moveGroupItem(fromGroupPosition, toGroupPosition);
        if (onItemEventListener != null)
            onItemEventListener.onGroupMove(fromGroupPosition, toGroupPosition);
    }

    @Override
    public void onMoveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        if ((fromGroupPosition == toGroupPosition && fromChildPosition == toChildPosition)
                || fromGroupPosition >= mProvider.getGroupCount() || toGroupPosition >= mProvider.getGroupCount()) {
            return;
        }
        mProvider.moveChildItem(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
        if (onItemEventListener != null)
            onItemEventListener.onItemMove(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
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

    public void setExpandableItemManager(RecyclerViewExpandableItemManager itemManager) {
        this.mExpandableItemManager = itemManager;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setOnItemEventListener(OnItemEventListener listener) {
        this.onItemEventListener = listener;
    }

    public void setOnTagClickListener(CollectionTagAdapter.OnItemClickListener listener) {
        this.mTagClickListener = listener;
    }

    public ExpandableDataProvider<CollectionGroup, LocalCollection> getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ExpandableDataProvider<CollectionGroup, LocalCollection> mProvider) {
        this.mProvider = mProvider;
    }

    @Override
    public SwipeResultAction onSwipeGroupItem(GroupViewHolder holder, int groupPosition, int result) {
        return null;
    }

    @Override
    public SwipeResultAction onSwipeChildItem(CollectionViewHolder holder, int groupPosition, int childPosition, int result) {
        Logger.d("LocalCollectionAdapter", "onSwipeChildItem(groupPosition = " + groupPosition + ", childPosition = " + childPosition + ", result = " + result + ")");
        switch (result) {
            // swipe right
            case SwipeableItemConstants.RESULT_SWIPED_RIGHT:
            case SwipeableItemConstants.RESULT_SWIPED_LEFT:
                return new ChildSwipeResultAction(groupPosition, childPosition);
            // other --- do nothing
            case SwipeableItemConstants.RESULT_CANCELED:
            default:
                return null;
        }
    }

    @Override
    public int onGetGroupItemSwipeReactionType(GroupViewHolder holder, int groupPosition, int x, int y) {
        return SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY;
    }

    @Override
    public int onGetChildItemSwipeReactionType(CollectionViewHolder holder, int groupPosition, int childPosition, int x, int y) {
        if (onCheckChildCanStartDrag(holder, groupPosition, childPosition, x, y)) {
            return SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H;
        } else {
            return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
        }
    }

    @Override
    public void onSetGroupItemSwipeBackground(GroupViewHolder holder, int groupPosition, int type) {

    }

    @Override
    public void onSetChildItemSwipeBackground(CollectionViewHolder holder, int groupPosition, int childPosition, int type) {

    }

    public interface OnItemEventListener {
        void onGroupMove(int fromGroupPosition, int toGroupPosition);

        void onItemMove(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);

        void onItemRemoved(int groupPosition, int childPosition);
    }

    public interface OnItemClickListener {

        void onGroupClick(View v, int groupPosition);

        boolean onGroupLongClick(View v, int groupPosition);

        void onItemClick(View v, int groupPosition, int childPosition);

        boolean onItemLongClick(View v, int groupPosition, int childPosition);
    }

    private class ChildSwipeResultAction extends SwipeResultActionRemoveItem {
        private final int mGroupPosition;
        private final int mChildPosition;

        ChildSwipeResultAction(int groupPosition, int childPosition) {
            mGroupPosition = groupPosition;
            mChildPosition = childPosition;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (onItemEventListener != null) {
                onItemEventListener.onItemRemoved(mGroupPosition, mChildPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
        }
    }

    public class GroupViewHolder extends AbstractDraggableSwipeableItemViewHolder
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

        public GroupViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public View getSwipeableContainerView() {
            return null;
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

    public class CollectionViewHolder extends AbstractDraggableSwipeableItemViewHolder {
        @BindView(R.id.container)
        public CardView container;
        @BindView(R.id.ripple_layout)
        public MaterialRippleLayout rippleLayout;
        @BindView(R.id.layout_cover)
        public RelativeLayout layoutCover;
        @BindView(R.id.iv_cover)
        public ImageView ivCover;
        @BindView(R.id.tv_title)
        public TextView tvTitle;
        @BindView(R.id.tv_uploader)
        public TextView tvUploader;
        @BindView(R.id.tv_category)
        public TextView tvCategory;
        @BindView(R.id.rv_tags)
        public RecyclerView rvTags;
        @BindView(R.id.rb_rating)
        public RatingBar rbRating;
        @BindView(R.id.tv_submittime)
        public TextView tvSubmittime;

        public CollectionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }
    }
}