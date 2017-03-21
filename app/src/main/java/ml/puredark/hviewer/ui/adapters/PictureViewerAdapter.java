package ml.puredark.hviewer.ui.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.ui.activities.PictureViewerActivity;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;

public class PictureViewerAdapter extends RecyclerView.Adapter<PictureViewerAdapter.PictureViewHolder> {
    private PictureViewerActivity activity;
    private Site site;
    private ListDataProvider<Picture> mProvider;
    private OnItemLongClickListener mOnItemLongClickListener;

    public PictureViewerAdapter(PictureViewerActivity activity, Site site, ListDataProvider<Picture> provider) {
        setHasStableIds(true);
        this.activity = activity;
        this.site = site;
        this.mProvider = provider;
    }

    @Override
    public PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_picture_viewer, parent, false);
        // 在这里对View的参数进行设置
        PictureViewHolder vh = new PictureViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PictureViewHolder viewHolder, int position) {
        Picture picture = mProvider.getItem(position);
        Uri uri = null;
        viewHolder.ivPicture.setImageURI(uri);
        activity.getUrlAndLoadImage(viewHolder, picture, false);
        viewHolder.btnRefresh.setOnClickListener(v -> {
            activity.getUrlAndLoadImage(viewHolder, picture, false);
        });
        viewHolder.ivPicture.setOnLongClickListener(v -> {
            if (mOnItemLongClickListener != null)
                return mOnItemLongClickListener.onItemLongClick(v, position);
            else
                return false;
        });
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

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_picture)
        public PhotoDraweeView ivPicture;
        @BindView(R.id.progress_bar)
        public ProgressBarCircularIndeterminate progressBar;
        @BindView(R.id.btn_refresh)
        public ImageView btnRefresh;

        public PictureViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            ivPicture.setOrientation(LinearLayout.VERTICAL);
        }
    }
}