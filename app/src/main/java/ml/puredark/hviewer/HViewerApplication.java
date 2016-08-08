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

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

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

    public static List<Collection> histories;
    public static List<Collection> favourites;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
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
            Glide.with(mContext).load(url).into(imageView);
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
