package ml.puredark.hviewer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.gc.materialdesign.views.ProgressBarDeterminate;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;

public class DownloadTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int VIEW_TYPE_DOWNLOADING = 1;
    private final static int VIEW_TYPE_DOWNLOADED = 2;
    private Context context;
    private ListDataProvider mProvider;
    private OnItemClickListener mItemClickListener;

    public DownloadTaskAdapter(Context context, ListDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DOWNLOADING) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_download_task, parent, false);
            // 在这里对View的参数进行设置
            DownloadingTaskViewHolder vh = new DownloadingTaskViewHolder(v);
            return vh;
        } else if (viewType == VIEW_TYPE_DOWNLOADED) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection, parent, false);
            // 在这里对View的参数进行设置
            DownloadedTaskViewHolder vh = new DownloadedTaskViewHolder(v);
            return vh;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        DownloadTask task = (DownloadTask) mProvider.getItem(position);
        if (viewHolder instanceof DownloadingTaskViewHolder) {
            DownloadingTaskViewHolder holder = (DownloadingTaskViewHolder) viewHolder;
            HViewerApplication.loadImageFromUrl(context, holder.ivCover, task.collection.cover, null);
            holder.tvTitle.setText(task.collection.title);
            holder.tvUploader.setText(task.collection.uploader);
            holder.tvCategory.setText(task.collection.category);
            holder.rbRating.setRating(task.collection.rating);
            holder.tvSubmittime.setText(task.collection.datetime);
            holder.tvCount.setText(task.getDownloadedPictureCount() + "/" + task.collection.pictures.size());
            int percent = Math.round(((float) task.getDownloadedPictureCount() * 100 / task.collection.pictures.size()));
            holder.tvPercentage.setText(percent + "%");
            holder.progressBar.setProgress(percent);
            int resID = R.drawable.ic_play_arrow_primary_dark;
            switch (task.status) {
                case DownloadTask.STATUS_PAUSED:
                    resID = R.drawable.ic_play_arrow_primary_dark;
                    break;
                case DownloadTask.STATUS_DOWNLOADING:
                    resID = R.drawable.ic_pause_primary_dark;
                    break;
                case DownloadTask.STATUS_IN_QUEUE:
                    resID = R.drawable.ic_next_download_primary_dark;
                    break;
            }
            holder.btnStartPause.setImageResource(resID);
        } else if (viewHolder instanceof DownloadedTaskViewHolder) {
            DownloadedTaskViewHolder holder = (DownloadedTaskViewHolder) viewHolder;
            HViewerApplication.loadImageFromUrl(context, holder.ivCover, task.collection.cover, null);
            holder.tvTitle.setText(task.collection.title);
            holder.tvUploader.setText(task.collection.uploader);
            holder.tvCategory.setText(task.collection.category);
            TagAdapter adapter = (TagAdapter) holder.rvTags.getAdapter();
            adapter.getDataProvider().clear();
            if (task.collection.tags != null)
                adapter.getDataProvider().addAll(task.collection.tags);
            holder.rbRating.setRating(task.collection.rating);
            holder.tvSubmittime.setText(task.collection.datetime);
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
        DownloadTask task = (DownloadTask) mProvider.getItem(position);
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

    public class DownloadingTaskViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_cover)
        ImageView ivCover;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_uploader)
        TextView tvUploader;
        @BindView(R.id.tv_category)
        TextView tvCategory;
        @BindView(R.id.tv_count)
        TextView tvCount;
        @BindView(R.id.btn_start_pause)
        ImageView btnStartPause;
        @BindView(R.id.tv_percentage)
        TextView tvPercentage;
        @BindView(R.id.progress_bar)
        ProgressBarDeterminate progressBar;
        @BindView(R.id.rb_rating)
        RatingBar rbRating;
        @BindView(R.id.tv_submittime)
        TextView tvSubmittime;
        @BindView(R.id.ripple_layout)
        MaterialRippleLayout rippleLayout;

        public DownloadingTaskViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
            rippleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            rippleLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
        }

    }

    public class DownloadedTaskViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ripple_layout)
        MaterialRippleLayout rippleLayout;
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

        public DownloadedTaskViewHolder(View view) {
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
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
            rippleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            rippleLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
        }

    }

}