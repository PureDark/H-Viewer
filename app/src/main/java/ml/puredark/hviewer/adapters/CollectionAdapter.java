package ml.puredark.hviewer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;

public class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private AbstractDataProvider mProvider;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public CollectionAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection, parent, false);
        // 在这里对View的参数进行设置
        CollectionViewHolder vh = new CollectionViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Collection collection = (Collection) mProvider.getItem(position);
        CollectionViewHolder holder = (CollectionViewHolder) viewHolder;
//        HViewerApplication.loadImageFromUrl(holder.ivCover, collection.cover);
        holder.tvTitle.setText(collection.title);
        holder.tvUploader.setText(collection.uploader);
        holder.tvCategory.setText(collection.category);
        TagAdapter adapter = (TagAdapter)holder.rvTags.getAdapter();
        adapter.getDataProvider().clear();
        if(collection.tags!=null)
            adapter.getDataProvider().addAll(collection.tags);
        holder.rbRating.setRating(collection.rating);
        holder.tvSubmittime.setText(collection.datetime);
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

    public class CollectionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_cover)
        ImageView ivCover;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_uploader)
        TextView tvUploader;
        @BindView(R.id.tv_category)
        TextView tvCategory;
        @BindView(R.id.rv_tags)
        RecyclerView rvTags;
        @BindView(R.id.rb_rating)
        RatingBar rbRating;
        @BindView(R.id.tv_submittime)
        TextView tvSubmittime;

        public CollectionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            rvTags.setAdapter(
                    new TagAdapter(
                            new ListDataProvider<>(
                                    new ArrayList<Tag>()
                            )
                    )
            );
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }

    }
}