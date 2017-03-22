package ml.puredark.hviewer.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.widget.ImageView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.JsonObject;

import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.ui.customs.RetainingDataSourceSupplier;
import ml.puredark.hviewer.utils.DensityUtil;

import static ml.puredark.hviewer.HViewerApplication.getGson;

/**
 * Created by PureDark on 2016/9/2.
 */

public class ImageLoader {

    public static void loadImageFromUrl(Context context, ImageView imageView, String url) {
        loadImageFromUrl(context, imageView, url, null, null, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie) {
        loadImageFromUrl(context, imageView, url, cookie, null, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer) {
        loadImageFromUrl(context, imageView, url, cookie, referer, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer, boolean noCache) {
        loadImageFromUrl(context, imageView, url, cookie, referer, noCache, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer, ControllerListener controllerListener) {
        loadImageFromUrl(context, imageView, url, cookie, referer, false, controllerListener);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer, boolean noCache, ControllerListener controllerListener) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageURI(null);
        }
        Uri uri = Uri.parse(url);
        JsonObject header = new JsonObject();
        header.addProperty("Cookie", cookie);
        header.addProperty("Referer", referer);
        if (url != null && url.startsWith("http")) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
            }
            MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
        }
        if (imageView instanceof SimpleDraweeView) {
            SimpleDraweeView draweeView = ((SimpleDraweeView) imageView);
            ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(1080, 1920));
            if (noCache)
                requestBuilder.disableDiskCache();
            ImageRequest request = requestBuilder.build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setCallerContext(context)
                    .setTapToRetryEnabled(true)
                    .setAutoPlayAnimations(true)
                    .setOldController(draweeView.getController())
                    .setControllerListener(controllerListener)
                    .setImageRequest(request)
                    .build();
            draweeView.setController(controller);
        }
    }

    public static RetainingDataSourceSupplier loadImageFromUrlRetainingImage(Context context, ImageView imageView, String url, String cookie, String referer, boolean noCache, ControllerListener controllerListener) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageURI(null);
            return null;
        }
        Uri uri = Uri.parse(url);
        JsonObject header = new JsonObject();
        header.addProperty("Cookie", cookie);
        header.addProperty("Referer", referer);
        if (url != null && url.startsWith("http")) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
            }
            MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
        }
        if (imageView instanceof SimpleDraweeView) {
            SimpleDraweeView draweeView = ((SimpleDraweeView) imageView);
            RetainingDataSourceSupplier<CloseableReference<CloseableImage>> retainingSupplier = new RetainingDataSourceSupplier<>();
            PipelineDraweeControllerBuilder draweeControllerBuilder = Fresco.newDraweeControllerBuilder();
            draweeControllerBuilder.setDataSourceSupplier(retainingSupplier);
            DraweeController controller = draweeControllerBuilder
                    .setCallerContext(context)
                    .setTapToRetryEnabled(true)
                    .setAutoPlayAnimations(true)
                    .setOldController(draweeView.getController())
                    .setControllerListener(controllerListener)
                    .build();
            draweeView.setController(controller);
            ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(1080, 1920));
            if (noCache)
                requestBuilder.disableDiskCache();
            ImageRequest request = requestBuilder.build();
            retainingSupplier.setSupplier(Fresco.getImagePipeline().getDataSourceSupplier(request, null, ImageRequest.RequestLevel.FULL_FETCH));
            return retainingSupplier;
        }
        return null;
    }

    public static void loadBitmapFromUrl(Context context, String url, String cookie, String referer, BaseBitmapDataSubscriber dataSubscriber) {
        if (TextUtils.isEmpty(url))
            return;
        Uri uri = Uri.parse(url);
        JsonObject header = new JsonObject();
        header.addProperty("Cookie", cookie);
        header.addProperty("Referer", referer);
        if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
            HProxy proxy = new HProxy(url);
            header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
        }
        MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest request = builder.build();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(request, context);
        dataSource.subscribe(dataSubscriber, CallerThreadExecutor.getInstance());
    }

    public static void loadResourceFromUrl(Context context, String url, String cookie, String referer, BaseDataSubscriber dataSubscriber) {
        if (TextUtils.isEmpty(url))
            return;
        Uri uri = Uri.parse(url);
        loadResourceFromUrl(context, uri, cookie, referer, dataSubscriber);
    }

    public static void loadResourceFromUrl(Context context, Uri uri, String cookie, String referer, BaseDataSubscriber dataSubscriber) {
        if (uri.getScheme().startsWith("http")) {
            JsonObject header = new JsonObject();
            header.addProperty("Cookie", cookie);
            header.addProperty("Referer", referer);
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(uri.toString());
                header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
            }
            MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
        }
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest request = builder.build();
        DataSource<CloseableReference<PooledByteBuffer>>
                dataSource = imagePipeline.fetchEncodedImage(request, context);
        dataSource.subscribe(dataSubscriber, CallerThreadExecutor.getInstance());
    }


    public static void loadThumbFromUrl(Context context, ImageView imageView, int resizeWidthDp, int resizeHeightDp, String url) {
        loadThumbFromUrl(context, imageView, resizeWidthDp, resizeHeightDp, url, null, null, null);
    }

    public static void loadThumbFromUrl(Context context, ImageView imageView, int resizeWidthDp, int resizeHeightDp, String url, String cookie) {
        loadThumbFromUrl(context, imageView, resizeWidthDp, resizeHeightDp, url, cookie, null, null);
    }

    public static void loadThumbFromUrl(Context context, ImageView imageView, int resizeWidthDp, int resizeHeightDp, String url, String cookie, String referer) {
        loadThumbFromUrl(context, imageView, resizeWidthDp, resizeHeightDp, url, cookie, referer, null);
    }

    public static void loadThumbFromUrl(Context context, ImageView imageView, int resizeWidthDp, int resizeHeightDp, String url, String cookie, String referer, ControllerListener controllerListener) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageURI(null);
            return;
        }
        Uri uri = Uri.parse(url);
        JsonObject header = new JsonObject();
        header.addProperty("Cookie", cookie);
        header.addProperty("Referer", referer);
        if (url != null && url.startsWith("http")) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
                MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
            }
            MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
        }
        if (imageView instanceof SimpleDraweeView) {
            ImageDecodeOptions imageDecodeOptions = new ImageDecodeOptionsBuilder()
                    .setForceStaticImage(true)
                    .setDecodePreviewFrame(true)
                    .build();
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(DensityUtil.dp2px(context, resizeWidthDp), DensityUtil.dp2px(context, resizeHeightDp)))
                    .setImageDecodeOptions(imageDecodeOptions)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setCallerContext(context)
                    .setTapToRetryEnabled(true)
                    .setAutoPlayAnimations(false)
                    .setOldController(((SimpleDraweeView) imageView).getController())
                    .setControllerListener(controllerListener)
                    .setImageRequest(request)
                    .build();
            ((SimpleDraweeView) imageView).setController(controller);
        }
    }

    public static void loadThumbnailForVideo(Context context, ImageView imageView, int resizeWidthDp, int resizeHeightDp, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            imageView.setImageURI(null);
            return;
        }
        new Thread(() -> {
            try {
                String rootPath = filePath.substring(0, filePath.lastIndexOf("/"));
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
                fileName += ".jpg";
                DocumentFile documentFile = FileHelper.getDocumentFile(fileName, rootPath);
                if (documentFile == null || !documentFile.exists()) {
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
                    documentFile = FileHelper.createFileIfNotExist(fileName, rootPath);
                    if(documentFile!=null)
                        FileHelper.saveBitmapToFile(bitmap, documentFile);
                }
                if(documentFile!=null)
                    loadThumbFromUrl(context, imageView, resizeWidthDp, resizeHeightDp, documentFile.getUri().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
