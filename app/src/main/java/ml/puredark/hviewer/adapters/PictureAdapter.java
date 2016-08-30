package ml.puredark.hviewer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.dataproviders.ListDataProvider;

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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final Picture picture = (Picture) mProvider.getItem(position);
        final PictureViewHolder holder = (PictureViewHolder) viewHolder;
        if (!repeatedThumbnail)
            HViewerApplication.loadImageFromUrl(context, holder.ivPicture, picture.thumbnail, cookie, picture.referer);
        else {
            holder.ivPicture.setImageBitmap(null);
            holder.ivPicture.setTag("pid=" + picture.pid);
            HViewerApplication.loadBitmapFromUrl(context, picture.thumbnail, cookie, picture.referer, new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable final Bitmap resource) {
                    if (resource == null)
                        return;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<Picture> pictures = mProvider.getItems();
                            int count = 0;
                            for (Picture pic : pictures) {
                                if (picture.thumbnail.equals(pic.thumbnail))
                                    count++;
                            }
                            final Bitmap bitmap;
                            if (resource.getWidth() >= resource.getHeight()) {
                                int width = resource.getWidth() / count;
                                int height = resource.getHeight();
                                int startX = width * (position % count);
                                int startY = 0;
                                if (width * 2 > height) {
                                    if (startX + width > resource.getWidth())
                                        width = resource.getWidth() - startX;
                                    bitmap = Bitmap.createBitmap(resource, startX, startY, width, height);
                                } else {
                                    bitmap = resource;
                                }
                            } else {
                                int width = resource.getWidth();
                                int height = resource.getHeight() / count;
                                int startX = 0;
                                int startY = height * (position % count);
                                if (height * 2 > width) {
                                    if (startY + height > resource.getHeight())
                                        height = resource.getHeight() - startY;
                                    bitmap = Bitmap.createBitmap(resource, startX, startY, width, height);
                                } else {
                                    bitmap = resource;
                                }
                            }

                            new Handler(context.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(("pid=" + picture.pid).equals(holder.ivPicture.getTag())) {
                                        holder.ivPicture.setImageBitmap(bitmap);
                                        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    }
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                }
            });
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