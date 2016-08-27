package ml.puredark.hviewer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.request.BasePostprocessor;

import java.lang.annotation.Target;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.dataproviders.ListDataProvider;

import static android.R.attr.bitmap;
import static android.R.attr.resource;
import static android.R.attr.y;

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
        else{
            Log.d("PictureAdapter", "repeatedThumbnail");
            HViewerApplication.loadImageFromUrl(context, holder.ivPicture, picture.thumbnail, cookie, picture.referer,  new BasePostprocessor() {
                @Override
                public String getName() {
                    return "dividePostprocessor";
                }

                @Override
                public void process(Bitmap bitmap) {
                    super.process(bitmap);
                    Log.d("PictureAdapter", "dividePostprocessor process");
                }

                @Override
                public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
                    Log.d("PictureAdapter", "dividePostprocessor");
                    CloseableReference<Bitmap> bitmapRef = null;
                    try {
                        List<Picture> pictures = mProvider.getItems();
                        int count = 0;
                        for (Picture pic : pictures) {
                            if (picture.thumbnail.equals(pic.thumbnail))
                                count++;
                        }
                        if (sourceBitmap.getWidth() >= sourceBitmap.getHeight()) {
                            int width = sourceBitmap.getWidth() / count;
                            int height = sourceBitmap.getHeight();
                            int startX = width * (position % count);
                            int startY = 0;
                            if (width * 2 > height) {
                                Log.d("PictureAdapter", "width:" + width + " height:" + height);
                                if (startX + width > sourceBitmap.getWidth())
                                    width = sourceBitmap.getWidth() - startX;
                                bitmapRef = bitmapFactory.createBitmap(width, height);
                                Bitmap destBitmap = bitmapRef.get();
                                for (int x = startX; x < destBitmap.getWidth(); x++) {
                                    for (int y = startY; y < destBitmap.getHeight(); y++) {
                                        destBitmap.setPixel(x, y, sourceBitmap.getPixel(x, y));
                                    }
                                }
                                Log.d("PictureAdapter", "startX:" + startX + " startY:" + startY);
                            } else {
//                                bitmapRef = bitmapFactory.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight());
//                                Bitmap destBitmap = bitmapRef.get();
//                                for (int x = 0; x < destBitmap.getWidth(); x++) {
//                                    for (int y = 0; y < destBitmap.getHeight(); y++) {
//                                        destBitmap.setPixel(x, y, sourceBitmap.getPixel(x, y));
//                                    }
//                                }
                            }
                        } else {
                            int width = sourceBitmap.getWidth();
                            int height = sourceBitmap.getHeight() / count;
                            int startX = 0;
                            int startY = height * (position % count);
                            if (height * 2 > width) {
                                if (startY + height > sourceBitmap.getHeight())
                                    height = sourceBitmap.getHeight() - startY;
                                bitmapRef = bitmapFactory.createBitmap(width, height);
                                Bitmap destBitmap = bitmapRef.get();
                                for (int x = startX; x < destBitmap.getWidth(); x++) {
                                    for (int y = startY; y < destBitmap.getHeight(); y++) {
                                        destBitmap.setPixel(x, y, sourceBitmap.getPixel(x, y));
                                    }
                                }
                            } else {
//                                bitmapRef = bitmapFactory.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight());
//                                Bitmap destBitmap = bitmapRef.get();
//                                for (int x = 0; x < destBitmap.getWidth(); x++) {
//                                    for (int y = 0; y < destBitmap.getHeight(); y++) {
//                                        destBitmap.setPixel(x, y, sourceBitmap.getPixel(x, y));
//                                    }
//                                }
                            }
                        }
                        return CloseableReference.cloneOrNull(bitmapRef);
                    } finally {
                        if(bitmapRef!=null)
                            CloseableReference.closeSafely(bitmapRef);
                    }
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