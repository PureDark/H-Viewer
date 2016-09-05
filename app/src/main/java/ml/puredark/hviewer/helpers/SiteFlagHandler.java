package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;

import java.util.List;

import ml.puredark.hviewer.activities.PictureViewerActivity.PicturePagerAdapter;
import ml.puredark.hviewer.adapters.PictureAdapter;
import ml.puredark.hviewer.adapters.PictureAdapter.PictureViewHolder;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;

/**
 * Created by PureDark on 2016/9/5.
 */

public class SiteFlagHandler {

    public static void repeatedThumbnail(final Context context, final PictureViewHolder holder, String cookie, final int position, final Picture picture, final List<Picture> pictures){
        holder.ivPicture.setImageBitmap(null);
        holder.ivPicture.setTag("pid=" + picture.pid);
        ImageLoader.loadBitmapFromUrl(context, picture.thumbnail, cookie, picture.referer, new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable final Bitmap resource) {
                if (resource == null)
                    return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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
