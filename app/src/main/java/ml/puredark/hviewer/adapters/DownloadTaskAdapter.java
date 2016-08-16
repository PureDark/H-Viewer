package ml.puredark.hviewer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.gc.materialdesign.views.ProgressBarDeterminate;
import com.gc.materialdesign.views.ProgressBarIndeterminateDeterminate;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class DownloadTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private AbstractDataProvider mProvider;
    private OnItemClickListener mItemClickListener;

    public DownloadTaskAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download_task, parent, false);
        // 在这里对View的参数进行设置
        DownloadTaskViewHolder vh = new DownloadTaskViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        DownloadTask task = (DownloadTask) mProvider.getItem(position);
        DownloadTaskViewHolder holder = (DownloadTaskViewHolder) viewHolder;
        HViewerApplication.loadImageFromUrl(holder.ivCover, task.collection.cover, task.collection.site.cookie);
        holder.tvTitle.setText(task.collection.title);
        holder.tvUploader.setText(task.collection.uploader);
        holder.tvCategory.setText(task.collection.category);
        holder.rbRating.setRating(task.collection.rating);
        holder.tvSubmittime.setText(task.collection.datetime);
        holder.tvCount.setText(task.curPosition + "/" + task.collection.pictures.size());
        int percent = (int) ((float) task.curPosition * 100 / task.collection.pictures.size());
        holder.tvPercentage.setText(percent + "%");
        holder.progressBar.setProgress(percent);
        holder.btnStartPause.setImageResource(
                (task.paused)
                ?R.drawable.ic_play_arrow_primary_dark_24dp
                :R.drawable.ic_pause_primary_dark);
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

    public AbstractDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        boolean onItemLongClick(View v, int position);
    }

    public class DownloadTaskViewHolder extends RecyclerView.ViewHolder {
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

        public DownloadTaskViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

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