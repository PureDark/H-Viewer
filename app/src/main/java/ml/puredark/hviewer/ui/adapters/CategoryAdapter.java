package ml.puredark.hviewer.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public int selectedCid = 0;
    private ListDataProvider<Category> mProvider;
    private OnItemClickListener mItemClickListener;

    public CategoryAdapter(ListDataProvider<Category> mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        // 在这里对View的参数进行设置
        CategoryViewHolder vh = new CategoryViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        CategoryViewHolder holder = (CategoryViewHolder) viewHolder;
        Category category = mProvider.getItem(position);
        holder.tvTitle.setText(category.title);
        if (selectedCid == category.cid) {
            holder.tvTitle.setTextColor(holder.tvTitle.getResources().getColor(R.color.white));
            holder.container.setBackgroundResource(R.color.colorPrimaryDark);
        } else {
            holder.tvTitle.setTextColor(holder.tvTitle.getResources().getColor(R.color.black));
            holder.container.setBackground(null);
        }
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

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        MaterialRippleLayout container;
        @BindView(R.id.tv_title)
        TextView tvTitle;

        public CategoryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount())
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
        }
    }

}