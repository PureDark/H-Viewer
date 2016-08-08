package ml.puredark.hviewer;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ml.puredark.hviewer.utils.SharedPreferencesUtil;

public class HViewerApplication extends Application {
    public static Context mContext;
    // 全局变量，用于跨Activity传输复杂对象
    public static Object temp;

    //服务器地址
    public static String serverHost = "";

    //默认头像的载入Option
    public static  DisplayImageOptions avatarOptions;

    public static List<Collection> histories;
    public static List<Collection> favourites;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        initImageLoader(getApplicationContext());
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    private void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)                //设置为RGB565比起默认的ARGB_8888要节省大量的内存
                        .delayBeforeLoading(100)
                        //.showImageForEmptyUri(R.drawable.img_default)    //设置图片Uri为空或是错误的时候显示的图片
                        //.showImageOnFail(R.drawable.img_default)         //设置图片加载/解码过程中错误时候显示的图片
                        .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                        .threadPriority(Thread.NORM_PRIORITY - 2)
                        .defaultDisplayImageOptions(defaultOptions)
                        .denyCacheImageMultipleSizesInMemory()
                        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                        .diskCacheSize(100 * 1024 * 1024) // 100 MiB
                        .memoryCache(new LruMemoryCache(10 * 1024 * 1024))
                        .memoryCacheSize(10 * 1024 * 1024)
                        .tasksProcessingOrder(QueueProcessingType.FIFO)
                        //.writeDebugLogs() // Remove for release app
                        .build();

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);


        //设置默认头像的Option
        avatarOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.avatar)    //设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.avatar)         //设置图片加载/解码过程中错误时候显示的图片
                //.displayer(new FadeInBitmapDisplayer(300))  //图片加载好后渐入的动画时间
                .build();                                   //构建完成
    }

    public static String getVersionName() throws Exception
    {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);
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

    public static void loadImageFromUrl(ImageView imageView, String url){
        imageView.setImageBitmap(null);
        if(url!=null) {
            ImageAware imageAware = new ImageViewAware(imageView, false);
            ImageLoader.getInstance().displayImage(url, imageAware);
        }
    }


    public static void saveHistory() {
        SharedPreferencesUtil.saveData(mContext, "History", new Gson().toJson(histories));
    }

    public static void addHistory(Collection item) {
        if(item==null)return;
        deleteHistory(item);
        histories.add(0, item);
        trimHistory();
        saveHistory();
    }

    public static void deleteHistory(Collection item) {
        for(int i=0,size=histories.size();i<size;i++){
            if(histories.get(i).equals(item)){
                histories.remove(i);
                size--;
                i--;
            }
        }
        saveHistory();
    }

    public static List<Collection> getHistory() {
        if (histories == null)
            return new ArrayList<>();
        else
            return histories;
    }

    public static void trimHistory() {
        while(histories.size()>20)
            histories.remove(20);
    }

    public static void saveFavourite() {
        SharedPreferencesUtil.saveData(mContext, "Favourite", new Gson().toJson(favourites));
    }

    public static void addFavourite(Collection item) {
        if(item==null)return;
        deleteFavourite(item);
        favourites.add(0, item);
        saveFavourite();
    }

    public static void deleteFavourite(Collection item) {
        for(int i=0,size=favourites.size();i<size;i++){
            if(favourites.get(i).equals(item)){
                favourites.remove(i);
                size--;
                i--;
            }
        }
        saveFavourite();
    }

    public static List<Collection> getFavourite() {
        if(favourites==null)
            return new ArrayList<>();
        else
            return favourites;
    }

    public static boolean isFavourite(Collection item) {
        for(int i=0,size=favourites.size();i<size;i++){
            if(favourites.get(i).equals(item))
                return true;
        }
        return false;
    }

}
