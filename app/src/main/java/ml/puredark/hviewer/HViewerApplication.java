package ml.puredark.hviewer;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.WeakHashMap;

import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.customs.MyOkHttpNetworkFetcher;
import ml.puredark.hviewer.helpers.CrashHandler;
import ml.puredark.hviewer.helpers.HProxy;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.UpdateManager;
import ml.puredark.hviewer.holders.SearchHistoryHolder;
import ml.puredark.hviewer.holders.SearchSuggestionHolder;
import ml.puredark.hviewer.services.DownloadService;
import okhttp3.OkHttpClient;

public class HViewerApplication extends Application {
    public static Context mContext;
    // 全局变量，用于跨Activity传递复杂对象的引用
    public static Object temp, temp2;

    public static SearchHistoryHolder searchHistoryHolder;
    public static SearchSuggestionHolder searchSuggestionHolder;
    public static Gson gson;

    //为了Fresco请求时可以根据不同Uri加不同的header，必须要得有一个全局变量（Fresco的硬伤）
    public static WeakHashMap<Uri, String> headers = new WeakHashMap<>();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }

    public static String getVersionName() {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "0.0.0";
        }
        String version = packInfo.versionName;
        return version;
    }

    /**
     * 检测网络是否连接
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        // 得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url) {
        loadImageFromUrl(context, imageView, url, null, null, null, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie) {
        loadImageFromUrl(context, imageView, url, cookie, null, null, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer) {
        loadImageFromUrl(context, imageView, url, cookie, referer, null, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer, ControllerListener controllerListener) {
        loadImageFromUrl(context, imageView, url, cookie, referer, controllerListener, null);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer, Postprocessor postprocessor) {
        loadImageFromUrl(context, imageView, url, cookie, referer, null, postprocessor);
    }

    public static void loadImageFromUrl(Context context, ImageView imageView, String url, String cookie, String referer, ControllerListener controllerListener, Postprocessor postprocessor) {
        Uri uri = Uri.parse(url);
        JsonObject header = new JsonObject();
        header.addProperty("cookie", cookie);
        header.addProperty("referer", referer);
        if (url != null && url.startsWith("http")) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                header.addProperty(proxy.getHeaderKey(), proxy.getHeaderValue());
                headers.put(uri, getGson().toJson(header));
            }
            headers.put(uri, getGson().toJson(header));
        }
        if (imageView instanceof SimpleDraweeView) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setPostprocessor(postprocessor)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setCallerContext(context)
                    .setTapToRetryEnabled(true)
                    .setAutoPlayAnimations(true)
                    .setImageRequest(request)
                    .setOldController(((SimpleDraweeView) imageView).getController())
                    .setControllerListener(controllerListener)
                    .setUri(uri)
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
        headers.put(uri, getGson().toJson(header));
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest request = builder.build();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(request, context);
        dataSource.subscribe(dataSubscriber, CallerThreadExecutor.getInstance());
    }

    public static void checkUpdate(final Context context) {
        String url = context.getString(R.string.update_site_url);
        HViewerHttpClient.get(url, null, new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                try {
                    JsonObject version = new JsonParser().parse((String) result).getAsJsonObject();
                    boolean prerelease = version.get("prerelease").getAsBoolean();
                    if (prerelease)
                        return;
                    JsonArray assets = version.get("assets").getAsJsonArray();
                    if (assets.size() > 0) {
                        String oldVersion = HViewerApplication.getVersionName();
                        String newVersion = version.get("tag_name").getAsString().substring(1);
                        String url = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                        String detail = version.get("body").getAsString();
                        new UpdateManager(context, url, newVersion + "版本更新", detail)
                                .checkUpdateInfo(oldVersion, newVersion);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setNetworkFetcher(new MyOkHttpNetworkFetcher(okHttpClient))
                .build();
        Fresco.initialize(this, config);

        searchHistoryHolder = new SearchHistoryHolder(this);
        searchSuggestionHolder = new SearchSuggestionHolder(this);

        startService(new Intent(this, DownloadService.class));

        CrashHandler crashHandler = CrashHandler.getInstance();
        //注册crashHandler
        crashHandler.init(getApplicationContext());
    }


}
