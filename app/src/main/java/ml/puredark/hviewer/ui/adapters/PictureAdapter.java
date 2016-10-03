package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.SiteFlagHandler;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ListDataProvider mProvider;
    private OnItemClickListener mItemClickListener;
    private boolean repeatedThumbnail = false;
    private String cookie;

    public PictureAdapter(Context context, ListDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
        this.context = context;
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
        Logger.d("PictureAdapter", "picture.thumbnail:" + picture.thumbnail);
        if (!repeatedThumbnail)
            ImageLoader.loadThumbFromUrl(context, holder.ivPicture, 100, 140, picture.thumbnail, cookie, picture.referer);
        else {
            SiteFlagHandler.repeatedThumbnail(context, holder, cookie, position, picture, mProvider.getItems());
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

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setRepeatedThumbnail(boolean repeated) {
        repeatedThumbnail = repeated;
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

    public class PictureViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_picture)
        public ImageView ivPicture;

        public PictureViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }
    }
}