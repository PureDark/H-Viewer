package com.sina.util.dnscache.log;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.sina.util.dnscache.AppConfigUtil;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.File;
import java.io.IOException;

public class HttpDnsLogManager implements IDnsLog {

    /**
     * 配置文件更新地址
     */
    public static String LOG_UPLOAD_API = "";
    
    public static final boolean LOG_UPLOAD_SWITCH = false; 
    
    public static int sample_rate = 50;
    /**
     * 错误类型
     */
    public static final int TYPE_ERROR = 1;
    /**
     * 调试信息类型
     */
    public static final int TYPE_INFO = 2;
    /**
     * 测速类型
     */
    public static final int TYPE_SPEED = 3;
    /**
     * 日志文件的最大容量。8MB
     */
    private static final int DEFAULT_MAX_SIZE = 8 * 1024 * 1024;
    /**
     * 调整因子。取值大于0小于1
     */
    private static final float DEFAULT_FACTOR = 0.5f;
    /**
     * 日志文件
     */
    private File mLogFile;

    private static HttpDnsLogManager mDnsLogManager;

    /**
     * 调试信息分类中的domain信息
     */
    public static final String ACTION_INFO_DOMAIN = "httpdns_domaininfo";
    /**
     * 调试信息分类中的pack信息
     */
    public static final String ACTION_INFO_PACK = "httpdns_packinfo";
    
    /**
     * 调试信息分类中的config信息
     */
    public static final String ACTION_INFO_CONFIG = "httpdns_configinfo";
    
    /**
     * 设备sp 和 server识别出口sp 不一致错误信息
     */
    public static final String ACTION_ERR_SPINFO = "httpdns_errspinfo";

    /**
     * 设备sp 和 server识别出口sp 不一致错误信息
     */
    public static final String ACTION_ERR_DOMAININFO = "httpdns_errdomaininfo";
    
    
    
    private static final Object lock = new Object();
    private static Handler mLogHandler;
    /**设置日志上报的间隔为1小时*/
    public static long time_interval = 1 * 60 * 60 * 1000;

    public static HttpDnsLogManager getInstance() {

        if (null == mDnsLogManager) {
            synchronized (lock) {
                if (null == mDnsLogManager) {
                    HandlerThread ht = new HandlerThread("logThread");
                    ht.start();
                    Looper looper = ht.getLooper();
                    mLogHandler = new Handler(looper);
                    mDnsLogManager = new HttpDnsLogManager();
                }
            }
        }
        return mDnsLogManager;
    }

    private HttpDnsLogManager() {
        tryCreateLogFile();
    }

    private void tryCreateLogFile() {
        if (FileUtil.haveFreeSpaceInSD()) {
            mLogFile = new File(AppConfigUtil.getExternalCacheDir(), "httpdns.log");
            if (null != mLogFile && !mLogFile.exists()) {
                try {
                    mLogFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mLogFile = null;
        }
    }

    private void adjustFileSize(File file) {
        FileUtil.adjustFileSize(file, DEFAULT_MAX_SIZE, DEFAULT_FACTOR);
    }

    @Override
    public File getLogFile() {
        synchronized (lock) {
            return mLogFile;
        }
    }

    @Override
    public boolean deleteLogFile() {
        synchronized (lock) {
            if (null != mLogFile) {
                return mLogFile.delete();
            }
            return false;
        }
    }

    private String generateJsonStr(int type, String action, String body) {
        JSONStringer jsonStringer = new JSONStringer();
        try {
            jsonStringer.object()//
                    .key("type").value(type)//
                    .key("action").value(action)//
                    .key("content").value(body)//
                    .key("versionName").value(AppConfigUtil.getVersionName())//
                    .key("did").value(AppConfigUtil.getDeviceId())//
                    .key("appkey").value(AppConfigUtil.getAppKey())//
                    .key("timestamp").value(System.currentTimeMillis())//
                    .endObject();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
        return jsonStringer.toString();
    }

    @Override
    public void writeLog(int type, String action, String body) {
        writeLog(type, action, body, false);
    }
    
    @Override
    public void writeLog(final int type, final String action, final String body, final boolean enableSample) {
        writeLog(type, action, body, enableSample, -1);
    }

    @Override
    public void writeLog(final int type, final String action, final String body, final boolean enableSample, final int sampleRate) {
        mLogHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    // 做一个采样操作
                    boolean succ = true;
                    if (enableSample) {
                        if (sampleRate == -1) {
                            succ = (int) (Math.random() * sample_rate) == 0;
                        } else {
                            succ = (int) (Math.random() * sampleRate) == 0;
                        }
                    }
                    if (succ) {
                        if (null != mLogFile && !mLogFile.exists()) {
                            tryCreateLogFile();
                        }
                        if (null == mLogFile) {
                            return;
                        }
                        adjustFileSize(mLogFile);
                        String line = generateJsonStr(type, action, body);
                        FileUtil.writeFileLine(mLogFile, true, line);
                    }
                }
            }
        });
    }
}
