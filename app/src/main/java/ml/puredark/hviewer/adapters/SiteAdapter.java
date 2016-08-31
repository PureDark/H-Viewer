package ml.puredark.hviewer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.ViewUtil;

public class SiteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DraggableItemAdapter<RecyclerView.ViewHolder> {
    public int selectedSid = 0;
    private ListDataProvider mProvider;
    private OnItemClickListener mItemClickListener;
    private OnItemMoveListener onItemMoveListener;
    private MaterialAnimatedSwitch.OnCheckedChangeListener mOnCheckedChangeListener;

    public SiteAdapter(ListDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nav_menu, parent, false);
        // 在这里对View的参数进行设置
        SiteViewHolder vh = new SiteViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        SiteViewHolder holder = (SiteViewHolder) viewHolder;
        if (position == getItemCount() - 1) {
            holder.ivIcon.setImageResource(R.drawable.ic_add_black);
            holder.tvTitle.setText("添加新站点");
            holder.switchListGrid.setVisibility(View.GONE);
        } else {
            Site site = (Site) mProvider.getItem(position);
            int rID = R.drawable.ic_filter_9_plus_black;
            switch (position) {
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
            } else {
                holder.container.setBackground(null);
                holder.switchListGrid.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return (mProvider == null) ? 1 : mProvider.getCount() + 1;
    }

    @Override
    public long getItemId(int position) {
        if (position == getItemCount() - 1)
            return 0;
        else
            return (mProvider == null) ? 0 : mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
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

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position, boolean isGrid);

        boolean onItemLongClick(View v, int position);
    }

    public interface OnItemMoveListener {
        void onItemMove(int fromPosition, int toPosition);
    }

    public class SiteViewHolder extends AbstractDraggableItemViewHolder {
        @BindView(R.id.container)
        MaterialRippleLayout container;
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.switch_list_grid)
        MaterialAnimatedSwitch switchListGrid;

        public SiteViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition(), switchListGrid.isChecked());
                }
            });
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount() - 1)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
            switchListGrid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchListGrid.toggle();
                }
            });
            if (mOnCheckedChangeListener != null)
                switchListGrid.setOnCheckedChangeListener(mOnCheckedChangeListener);
        }
    }

    // 以下是拖拽排序相关实现

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {

        if (fromPosition == toPosition) {
            return;
        }

        mProvider.moveItem(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        if (onItemMoveListener != null)
            onItemMoveListener.onItemMove(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(RecyclerView.ViewHolder viewHolder, int position, int x, int y) {
        SiteViewHolder holder = (SiteViewHolder) viewHolder;
        final View dragHandleView = holder.ivIcon;
        return ViewUtil.hitTest(dragHandleView, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(RecyclerView.ViewHolder holder, int position) {
        int end = Math.max(0,getItemCount()-2);
        return new ItemDraggableRange(0, end);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        if (draggingPosition == getItemCount() - 1 || dropPosition == getItemCount() - 1)
            return false;
        return true;
    }

}