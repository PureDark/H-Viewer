package ml.puredark.hviewer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.dataproviders.DataProvider;

public class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private DataProvider mProvider;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public PictureAdapter(DataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_picture_index, parent, false);
        // 在这里对View的参数进行设置
        PictureViewHolder vh = new PictureViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Picture picture = (Picture) mProvider.getItem(position);
        PictureViewHolder holder = (PictureViewHolder) viewHolder;
        HViewerApplication.loadImageFromUrl(holder.ivPicture, picture.url);
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

    public void setDataProvider(DataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public DataProvider getDataProvider() {
        return mProvider;
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_picture)
        ImageView ivPicture;

        public PictureViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }
    }
}