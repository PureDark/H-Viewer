package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;

import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.dataholders.SiteTagHolder;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.adapters.PictureVideoAdapter.PictureViewHolder;

/**
 * Created by PureDark on 2016/9/5.
 */

public class SiteFlagHandler {

    public static void repeatedThumbnail(final Context context, final PictureViewHolder holder, String cookie, final int position, final Picture picture, final List<Picture> pictures) {
        holder.ivPicture.setImageBitmap(null);
        holder.ivPicture.setTag("pid=" + picture.pid);
        ImageLoader.loadBitmapFromUrl(context, picture.thumbnail, cookie, picture.referer, new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable final Bitmap resource) {
                if (resource == null)
                    return;
                new Thread(() -> {
                    try {
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

                        new Handler(context.getMainLooper()).post(() -> {
                            if (("pid=" + picture.pid).equals(holder.ivPicture.getTag())) {
                                holder.ivPicture.setImageBitmap(bitmap);
                                holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }
                        });
                    } catch (Exception e){
                        // prevent exception of trying to use a recycled bitmap
                    }
                }).start();
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
            }
        });
    }

    public static void preloadGallery(final Context context, final RecyclerView.Adapter adapter, final int position, final Site site, final Collection collection, final SiteTagHolder siteTagHolder) {
        final String url = site.getGalleryUrl(collection.idCode, 0, null);
        HViewerHttpClient.get(url, site.getHeaders(), new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                if (result == null)
                    return;
                new Thread(() -> {
                    RuleParser.getCollectionDetail(collection, (String) result, site.galleryRule, url);
                    collection.preloaded = true;
                    synchronized (context) {
                        if (collection.tags != null) {
                            for (Tag tag : collection.tags) {
                                HViewerApplication.searchSuggestionHolder.addSearchSuggestion(tag.title);
                                if (siteTagHolder != null)
                                    siteTagHolder.addTag(site.sid, tag);
                            }
                        }
                    }

                    new Handler(context.getMainLooper()).post(() -> {
                        adapter.notifyItemChanged(position);
                    });
                }).start();
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
            }
        });
    }

}
