package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
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
import com.gc.materialdesign.views.ProgressBarDeterminate;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.DownloadItemStatus;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.beans.Video;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class DownloadingTaskAdapter extends RecyclerView.Adapter<DownloadingTaskAdapter.DownloadTaskViewHolder> {
    private final static int VIEW_TYPE_DOWNLOADING = 1;
    private final static int VIEW_TYPE_DOWNLOADED = 2;
    private Context context;
    private ListDataProvider<DownloadTask> mProvider;
    private OnItemClickListener mItemClickListener;

    public DownloadingTaskAdapter(Context context, ListDataProvider<DownloadTask> mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
        this.context = context;
    }

    @Override
    public DownloadTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DOWNLOADING) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_download_task, parent, false);
            return new DownloadingTaskViewHolder(v);
        } else if (viewType == VIEW_TYPE_DOWNLOADED) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection, parent, false);
            return new DownloadedTaskViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(DownloadTaskViewHolder viewHolder, int position) {
        DownloadTask task = mProvider.getItem(position);
        checkSiteFlags(viewHolder, task.collection.site);
        if (viewHolder instanceof DownloadingTaskViewHolder) {
            DownloadingTaskViewHolder holder = (DownloadingTaskViewHolder) viewHolder;
            if(!task.collection.cover.equals(holder.ivCover.getTag())) {
                holder.ivCover.setTag(holder.ivCover);
                ImageLoader.loadImageFromUrl(context, holder.ivCover, task.collection.cover, null);
            }
            holder.tvTitle.setText(task.collection.title);
            holder.tvUploader.setText(task.collection.uploader);
            holder.tvCategory.setText(task.collection.category);
            holder.rbRating.setRating(task.collection.rating);
            holder.tvSubmittime.setText(task.collection.datetime);
            int percent = 0;
            if (task.collection.videos != null && task.collection.videos.size() > 0) {
                for (Video video : task.collection.videos) {
                    if (video.status == DownloadItemStatus.STATUS_DOWNLOADING) {
                        percent = video.percent;
                        break;
                    } else if (video.status == DownloadItemStatus.STATUS_WAITING && percent == 0) {
                        percent = video.percent;
                    }
                }
                holder.tvCount.setText(task.getDownloadedVideoCount() + "/" + task.collection.videos.size());
            } else {
                int size = (task.collection.pictures != null) ? task.collection.pictures.size() : 0;
                holder.tvCount.setText(task.getDownloadedPictureCount() + "/" + size);
                percent = Math.round(((float) task.getDownloadedPictureCount() * 100 / size));
            }
            holder.tvPercentage.setText(percent + "%");
            holder.progressBar.setProgress(percent);
            int resID = R.drawable.ic_play_arrow_primary_dark;
            switch (task.status) {
                case DownloadTask.STATUS_PAUSED:
                    resID = R.drawable.ic_play_arrow_primary_dark;
                    break;
                case DownloadTask.STATUS_GETTING:
                    resID = R.drawable.ic_pause_primary_dark;
                    break;
                case DownloadTask.STATUS_IN_QUEUE:
                    resID = R.drawable.ic_next_download_primary_dark;
                    break;
            }
            holder.btnStartPause.setImageResource(resID);
        } else if (viewHolder instanceof DownloadedTaskViewHolder) {
            DownloadedTaskViewHolder holder = (DownloadedTaskViewHolder) viewHolder;
            if(!task.collection.cover.equals(holder.ivCover.getTag())) {
                holder.ivCover.setTag(holder.ivCover);
                ImageLoader.loadImageFromUrl(context, holder.ivCover, task.collection.cover, null);
            }
            holder.tvTitle.setText(task.collection.title);
            holder.tvUploader.setText(task.collection.uploader);
            holder.tvCategory.setText(task.collection.category);
            CollectionTagAdapter adapter = (CollectionTagAdapter) holder.rvTags.getAdapter();
            if (adapter != null) {
                adapter.getDataProvider().clear();
                if (task.collection.tags != null)
                    adapter.getDataProvider().addAll(task.collection.tags);
            }
            holder.rbRating.setRating(task.collection.rating);
            holder.tvSubmittime.setText(task.collection.datetime);
        }
    }

    private void checkSiteFlags(DownloadTaskViewHolder holder, Site site) {
        if (site.hasFlag(Site.FLAG_NO_COVER)) {
            holder.layoutCover.setVisibility(View.GONE);
        }
        if (site.hasFlag(Site.FLAG_NO_TITLE)) {
            holder.tvTitle.setVisibility(View.GONE);
            if (holder.rvTags != null) {
                holder.rvTags.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.HORIZONTAL));
            }
        }
        if (site.hasFlag(Site.FLAG_NO_RATING)) {
            holder.rbRating.setVisibility(View.GONE);
        }
        if (site.hasFlag(Site.FLAG_NO_TAG) && holder.rvTags != null) {
            holder.tvTitle.setMaxLines(2);
            holder.rvTags.setVisibility(View.GONE);
            holder.rvTags.setAdapter(
                    new CollectionTagAdapter(new ListDataProvider<>(new ArrayList()))
            );
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
        DownloadTask task = mProvider.getItem(position);
        return (task.status == DownloadTask.STATUS_COMPLETED) ? VIEW_TYPE_DOWNLOADED : VIEW_TYPE_DOWNLOADING;
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

        boolean onItemLongClick(View v, int position);
    }

    class DownloadTaskViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ripple_layout)
        MaterialRippleLayout rippleLayout;
        @BindView(R.id.layout_cover)
        RelativeLayout layoutCover;
        @BindView(R.id.iv_cover)
        ImageView ivCover;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_uploader)
        TextView tvUploader;
        @BindView(R.id.tv_category)
        TextView tvCategory;
        @BindView(R.id.rv_tags)
        @Nullable
        RecyclerView rvTags;
        @BindView(R.id.rb_rating)
        RatingBar rbRating;
        @BindView(R.id.tv_submittime)
        TextView tvSubmittime;

        DownloadTaskViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class DownloadingTaskViewHolder extends DownloadTaskViewHolder {
        @BindView(R.id.tv_count)
        TextView tvCount;
        @BindView(R.id.btn_start_pause)
        ImageView btnStartPause;
        @BindView(R.id.tv_percentage)
        TextView tvPercentage;
        @BindView(R.id.progress_bar)
        ProgressBarDeterminate progressBar;

        DownloadingTaskViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount())
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            view.setOnLongClickListener(v -> mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount()
                    && mItemClickListener.onItemLongClick(v, getAdapterPosition()));
            rippleLayout.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            rippleLayout.setOnLongClickListener(v -> mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount()
                    && mItemClickListener.onItemLongClick(v, getAdapterPosition()));
        }

    }

    class DownloadedTaskViewHolder extends DownloadTaskViewHolder {

        DownloadedTaskViewHolder(View view) {
            super(view);
            rvTags.setAdapter(
                    new CollectionTagAdapter(
                            new ListDataProvider<>(
                                    new ArrayList<Tag>()
                            )
                    )
            );
            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount())
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            view.setOnLongClickListener(v -> mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount()
                    && mItemClickListener.onItemLongClick(v, getAdapterPosition()));
            rippleLayout.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            rippleLayout.setOnLongClickListener(v -> mItemClickListener != null && getAdapterPosition() >= 0 && getAdapterPosition() < getItemCount()
                    && mItemClickListener.onItemLongClick(v, getAdapterPosition()));
        }

    }

}