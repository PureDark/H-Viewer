package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Video;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.SiteFlagHandler;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class PictureVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int TYPE_PICTURE = 0;
    public final static int TYPE_VIDEO = 1;
    private Context context;
    private ListDataProvider<Picture> pictures;
    private ListDataProvider<Video> videos;
    private OnItemClickListener mItemClickListener;
    private boolean repeatedThumbnail = false;
    private String cookie;

    public PictureVideoAdapter(Context context, ListDataProvider<Picture> pictures, ListDataProvider<Video> videos) {
        setHasStableIds(false);
        this.context = context;
        this.pictures = pictures;
        this.videos = videos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == TYPE_PICTURE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_picture_index, parent, false);
            vh = new PictureViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video_index, parent, false);
            vh = new VideoViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof PictureViewHolder) {
            Picture picture = pictures.getItem(position);
            PictureViewHolder holder = (PictureViewHolder) viewHolder;
            Logger.d("PictureVideoAdapter", "picture.thumbnail:" + picture.thumbnail);
            if (!repeatedThumbnail)
                ImageLoader.loadThumbFromUrl(context, holder.ivPicture, 100, 140, picture.thumbnail, cookie, picture.referer);
            else
                SiteFlagHandler.repeatedThumbnail(context, holder, cookie, position, picture, pictures.getItems());
        } else if (viewHolder instanceof VideoViewHolder) {
            Video video = videos.getItem(position - getPictureSize());
            VideoViewHolder holder = (VideoViewHolder) viewHolder;
            Logger.d("PictureVideoAdapter", "video.thumbnail:" + video.thumbnail);

            if (video.vlink != null && (video.vlink.startsWith("content://") || video.vlink.startsWith("file://"))) {
                Logger.d("PictureVideoAdapter", "loadThumbnailForVideo(video.vlink)");
                ImageLoader.loadThumbnailForVideo(context, holder.ivVideo, 100, 140, video.vlink);
            } else {
                Logger.d("PictureVideoAdapter", "loadThumbFromUrl(video.thumbnail)");
                ImageLoader.loadThumbFromUrl(context, holder.ivVideo, 100, 140, video.thumbnail, cookie);
            }
        }
    }

    public void setLayoutManager(GridLayoutManager layoutManager) {
        layoutManager.setSpanCount(6);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position < getPictureSize())
                    return 2;
                else if (position == getPictureSize() && getPictureSize() % 3 == 1)
                    return 4;
                else
                    return 3;
            }
        });
    }

    public int getPictureSize() {
        return (pictures == null) ? 0 : pictures.getCount();
    }

    public int getVideoSize() {
        return (videos == null) ? 0 : videos.getCount();
    }

    @Override
    public int getItemCount() {
        return getPictureSize() + getVideoSize();
    }

    @Override
    public long getItemId(int position) {
        int id = 0;
        if (position < getPictureSize())
            id = (pictures == null) ? 0 : pictures.getItem(position).getId();
        else if (position - getPictureSize() < getVideoSize())
            id = (videos == null) ? 0 : videos.getItem(position).getId();
        return id;
    }

    @Override
    public int getItemViewType(int position) {
        int type = TYPE_PICTURE;
        if (position < getPictureSize())
            type = TYPE_PICTURE;
        else if (position - getPictureSize() < getVideoSize())
            type = TYPE_VIDEO;
        return type;
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

    public ListDataProvider getPictureDataProvider() {
        return pictures;
    }

    public ListDataProvider getVideoDataProvider() {
        return videos;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.pictures = mProvider;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public static abstract class ScrollDetector extends RecyclerView.OnScrollListener {
        private int mScrollThreshold;

        public abstract void onScrollUp();

        public abstract void onScrollDown();

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            boolean isSignificantDelta = Math.abs(dy) > mScrollThreshold;
            if (isSignificantDelta) {
                if (dy > 0) {
                    onScrollUp();
                } else {
                    onScrollDown();
                }
            }
        }

        public void setScrollThreshold(int scrollThreshold) {
            mScrollThreshold = scrollThreshold;
        }
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_picture)
        public ImageView ivPicture;

        public PictureViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount())
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_video)
        public ImageView ivVideo;

        public VideoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount())
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
        }
    }
}