package ml.puredark.hviewer.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private AbstractDataProvider mProvider;
    private OnItemClickListener mItemClickListener;
    public int selectedRid = 0;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public SiteAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
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
        Site site = (Site) mProvider.getItem(position);
        RuleViewHolder holder = (RuleViewHolder) viewHolder;
        Log.d("SiteAdapter", site.title);
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
        if (selectedRid == site.rid)
            holder.container.setBackgroundResource(R.color.black_10);
        else
            holder.container.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return (mProvider == null) ? 0 : mProvider.getCount();
    }

    @Override
    public long getItemId(int position) {
        return (mProvider == null) ? 0 : mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setDataProvider(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public AbstractDataProvider getDataProvider() {
        return mProvider;
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
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }
    }
}