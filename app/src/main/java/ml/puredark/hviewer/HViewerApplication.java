package ml.puredark.hviewer;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.JsonObject;

import ml.puredark.hviewer.helpers.HProxy;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.holders.FavouriteHolder;
import ml.puredark.hviewer.holders.HistoryHolder;
import ml.puredark.hviewer.holders.SearchHistoryHolder;
import ml.puredark.hviewer.holders.SearchSuggestionHolder;
import ml.puredark.hviewer.holders.SiteHolder;
import ml.puredark.hviewer.services.DownloadService;

import static android.R.attr.data;

public class HViewerApplication extends Application {
    public static Context mContext;
    // 全局变量，用于跨Activity传递复杂对象的引用
    public static Object temp, temp2;

    public static SiteHolder siteHolder;
    public static HistoryHolder historyHolder;
    public static FavouriteHolder favouriteHolder;
    public static SearchHistoryHolder searchHistoryHolder;
    public static SearchSuggestionHolder searchSuggestionHolder;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static String getVersionName(){
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

    public static void loadImageFromUrl(ImageView imageView, String url, String cookie) {
        imageView.setImageBitmap(null);
        if (url != null && url.startsWith("http")) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                GlideUrl glideUrl = new GlideUrl(proxy.getProxyUrl(), new LazyHeaders.Builder()
                        .addHeader(proxy.getHeaderKey(), proxy.getHeaderValue())
                        .addHeader("cookie", cookie)
                        .build());
                Glide.with(mContext).load(glideUrl).into(imageView);
            } else {
                GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                        .addHeader("cookie", cookie)
                        .build());
                Glide.with(mContext).load(glideUrl).into(imageView);
            }
        }else{
            Glide.with(mContext).load(url).into(imageView);
        }
    }

    public static void loadBitmapFromUrl(String url, SimpleTarget listener){
        if (url != null) {
            if (HProxy.isEnabled() && HProxy.isAllowPicture()) {
                HProxy proxy = new HProxy(url);
                GlideUrl glideUrl = new GlideUrl(proxy.getProxyUrl(), new LazyHeaders.Builder()
                        .addHeader(proxy.getHeaderKey(), proxy.getHeaderValue())
                        .build());
                Glide.with(mContext).load(glideUrl).asBitmap().into(listener);
            } else {
                Glide.with(mContext).load(url).asBitmap().into(listener);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        siteHolder = new SiteHolder(this);
        historyHolder = new HistoryHolder(this);
        favouriteHolder = new FavouriteHolder(this);
        searchHistoryHolder = new SearchHistoryHolder(this);
        searchSuggestionHolder = new SearchSuggestionHolder(this);

        startService(new Intent(this, DownloadService.class));
    }


}
