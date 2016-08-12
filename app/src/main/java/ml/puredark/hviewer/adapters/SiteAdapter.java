package ml.puredark.hviewer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class SiteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public int selectedSid = 0;
    private AbstractDataProvider mProvider;
    private OnItemClickListener mItemClickListener;

    public SiteAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nav_menu, parent, false);
        // 在这里对View的参数进行设置
        RuleViewHolder vh = new RuleViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RuleViewHolder holder = (RuleViewHolder) viewHolder;
        if (position == getItemCount() - 1) {
            holder.ivIcon.setImageResource(R.drawable.ic_add_black);
            holder.tvTitle.setText("添加新站点");
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
            if (selectedSid == site.sid)
                holder.container.setBackgroundResource(R.color.black_10);
            else
                holder.container.setBackground(null);
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

    public AbstractDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemLongClick(View v, int position);
    }

    public class RuleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        LinearLayout container;
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_title)
        TextView tvTitle;

        public RuleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null)
                        mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            });
        }
    }
}