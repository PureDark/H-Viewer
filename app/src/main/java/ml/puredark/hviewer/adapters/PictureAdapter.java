package ml.puredark.hviewer.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private AbstractDataProvider mProvider;
    private OnItemClickListener mItemClickListener;
    private boolean repeatedThumbnail = false;
    private String cookie;

    public PictureAdapter(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        Picture picture = (Picture) mProvider.getItem(position);
        final PictureViewHolder holder = (PictureViewHolder) viewHolder;
        if (!repeatedThumbnail)
            HViewerApplication.loadImageFromUrl(holder.ivPicture, picture.thumbnail, cookie, picture.referer);
        else
            HViewerApplication.loadBitmapFromUrl(picture.thumbnail, cookie, picture.referer, new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {

                @Override
                public void onResourceReady(final Bitmap resource, GlideAnimation glideAnimation) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap bitmap;
                            if (resource.getWidth() >= resource.getHeight()) {
                                int width = resource.getWidth() / getItemCount();
                                int height = resource.getHeight();
                                int startX = width * position;
                                int startY = 0;
                                bitmap = Bitmap.createBitmap(resource, startX, startY, width, height);
                            } else {
                                int width = resource.getWidth();
                                int height = resource.getHeight() / getItemCount();
                                int startX = 0;
                                int startY = height * position;
                                bitmap = Bitmap.createBitmap(resource, startX, startY, width, height);
                            }
                            holder.ivPicture.post(new Runnable() {
                                @Override
                                public void run() {
                                    holder.ivPicture.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                }
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setRepeatedThumbnail(boolean repeated) {
        repeatedThumbnail = repeated;
    }

    public AbstractDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(AbstractDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
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
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }
    }
}