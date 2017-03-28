package ml.puredark.hviewer;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.sina.util.dnscache.DNSCache;
import com.umeng.analytics.MobclickAgent;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;

import ml.puredark.hviewer.configs.ImagePipelineConfigBuilder;
import ml.puredark.hviewer.core.CrashHandler;
import ml.puredark.hviewer.dataholders.SearchHistoryHolder;
import ml.puredark.hviewer.dataholders.SearchSuggestionHolder;
import ml.puredark.hviewer.download.DownloadService;
import ml.puredark.hviewer.libraries.swipeback.common.SwipeBackApplication;
import okhttp3.OkHttpClient;

public class HViewerApplication extends SwipeBackApplication {
    /**
     * 是否开启日志输出,在Debug状态下开启,
     * 在Release状态下关闭以提示程序性能
     */
    public final static boolean DEBUG = BuildConfig.DEBUG;
    public static Context mContext;
    // 全局变量，用于跨Activity传递复杂对象的引用
    public static Object temp, temp2, temp3, temp4;

    public static SearchHistoryHolder searchHistoryHolder;
    public static SearchSuggestionHolder searchSuggestionHolder;
    public static Gson gson;

    public final static String INTENT_SHORTCUT = "ml.puredark.hviewer.intent.action.SHORTCUT";
    public final static String INTENT_FROM_DOWNLOAD = "ml.puredark.hviewer.intent.action.FROMDOWNLOAD";
    public final static String INTENT_FROM_FAVOURITE = "ml.puredark.hviewer.intent.action.FROMFAVOURITE";

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
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        Fresco.initialize(this, ImagePipelineConfigBuilder.getDefaultImagePipelineConfig(this));

        Stetho.initializeWithDefaults(this);

        searchHistoryHolder = new SearchHistoryHolder(this);
        searchSuggestionHolder = new SearchSuggestionHolder(this);

        startService(new Intent(this, DownloadService.class));

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setCatchUncaughtExceptions(false);

        DNSCache.Init(this);

        CrashHandler crashHandler = CrashHandler.getInstance();
        //注册crashHandler
        crashHandler.init(getApplicationContext());

        // 设置Json默认配置
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new GsonJsonProvider();
            private final MappingProvider mappingProvider = new GsonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                Set<Option> options = EnumSet.noneOf(Option.class);
                options.add(Option.DEFAULT_PATH_LEAF_TO_NULL);
                return options;
            }
        });

        //必须调用初始化
        OkGo.init(this);
        OkGo.getInstance()
                .debug("OkGo", Level.INFO, DEBUG)
                .setCacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                .setRetryCount(3)
                .setCertificates();

    }


}
