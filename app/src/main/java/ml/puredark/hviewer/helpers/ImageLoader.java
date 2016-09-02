package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.google.gson.JsonObject;

import ml.puredark.hviewer.customs.MyOkHttpNetworkFetcher;
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

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer, ControllerListener controllerListener) {
        Uri uri = Uri.parse(url);
        JsonObject header = new JsonObject();
        header.addProperty("cookie", cookie);
        header.addProperty("referer", referer);
        if (url != null && url.startsWith("http")) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
                MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
            }
            MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
        }
        if (imageView instanceof SimpleDraweeView) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setCallerContext(context)
                    .setTapToRetryEnabled(true)
                    .setAutoPlayAnimations(true)
                    .setOldController(((SimpleDraweeView) imageView).getController())
                    .setControllerListener(controllerListener)
                    .setImageRequest(request)
                    .build();
            ((SimpleDraweeView) imageView).setController(controller);
        }
    }

    public static void loadBitmapFromUrl(Context context, String url, String cookie, String referer, BaseBitmapDataSubscriber dataSubscriber) {

        Uri uri = Uri.parse((url != null && url.startsWith("http")) ? url : "");
        JsonObject header = new JsonObject();
        header.addProperty("cookie", cookie);
        header.addProperty("referer", referer);
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
        Uri uri = Uri.parse((url != null && url.startsWith("http")) ? url : "");
        JsonObject header = new JsonObject();
        header.addProperty("cookie", cookie);
        header.addProperty("referer", referer);
        if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
            HProxy proxy = new HProxy(url);
            header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
        }
        MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
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
        Uri uri = Uri.parse(url);
        JsonObject header = new JsonObject();
        header.addProperty("cookie", cookie);
        header.addProperty("referer", referer);
        if (url != null && url.startsWith("http")) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
                MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
            }
            MyOkHttpNetworkFetcher.headers.put(uri, getGson().toJson(header));
        }
        if (imageView instanceof SimpleDraweeView) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(DensityUtil.dp2px(context, resizeWidthDp), DensityUtil.dp2px(context, resizeHeightDp)))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setCallerContext(context)
                    .setTapToRetryEnabled(true)
                    .setAutoPlayAnimations(true)
                    .setOldController(((SimpleDraweeView) imageView).getController())
                    .setControllerListener(controllerListener)
                    .setImageRequest(request)
                    .build();
            ((SimpleDraweeView) imageView).setController(controller);
        }
    }
}
