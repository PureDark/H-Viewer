package com.sina.util.dnscache;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;

import java.io.File;

public class AppConfigUtil {

    private static Context mContext;

    public static void init(Context ctx) {
        mContext = ctx;
    }

    public static Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    /**
     * 获取缓存文件夹
     * 
     * @return
     */
    public static File getExternalCacheDir() {
        File file = mContext.getExternalCacheDir();
        if (null == file) {
            file = createExternalCacheDir();
        }
        return file;
    }

    private static File createExternalCacheDir() {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, mContext.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return mContext.getCacheDir();
            }
        }
        return appCacheDir;
    }

    /**
     * 返回当前程序版本名
     * 
     * @return
     */
    public static String getVersionName() {
        String versionName = "";
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取设备id
     * 
     * @return
     */
    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }
    
    /**
     * 获取当前的应用key
     * @return
     */
    public static String getAppKey() {
        try {
            ApplicationInfo appInfo = mContext.getPackageManager()  
                    .getApplicationInfo(mContext.getPackageName(),PackageManager.GET_META_DATA);  
            appInfo.metaData.getString("meta_name"); 
            Bundle metaData = appInfo.metaData;
            if (null != metaData) {
                String appKey = metaData.getString("DNSCACHE_APP_KEY");
                return appKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
