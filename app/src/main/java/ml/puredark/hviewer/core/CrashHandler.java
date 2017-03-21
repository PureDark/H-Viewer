package ml.puredark.hviewer.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.utils.EmailUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

/**
 * @author Stay
 *         在Application中统一捕获异常，保存到文件中下次再打开时上传
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    /**
     * CrashHandler实例
     */
    private static CrashHandler INSTANCE;
    /**
     * 系统默认的UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /**
     * 程序的Context对象
     */
    private Context mContext;

    private Map<String, String> infos = new HashMap<>();


    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }


    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象,
     * 获取系统默认的UncaughtException处理器,
     * 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx
     */
    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        boolean unuploadedLog = (boolean) SharedPreferencesUtil.getData(mContext, "unupload_log", false);
        final String filePath = (String) SharedPreferencesUtil.getData(mContext, "unupload_log_file_path", "");
        if (unuploadedLog) {
            final File file = new File(filePath);
            if (file.exists()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!HViewerApplication.DEBUG) {
                            EmailUtil.sendEmail(EmailUtil.fromEmail, "v" + HViewerApplication.getVersionName() + " " + file.getName(),
                                    SimpleFileUtil.readString(filePath, "utf-8"));
                        } else
                            SharedPreferencesUtil.saveData(mContext, "unupload_log", false);
                    }
                }).start();
            } else {
                SharedPreferencesUtil.saveData(mContext, "unupload_log", false);
            }
        }
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {  //如果自己处理了异常，则不会弹出错误对话框，则需要手动退出app
            if (!(ex instanceof OutOfMemoryError)) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                if (!HViewerApplication.DEBUG) {
                    MobclickAgent.onKillProcess(mContext);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                } else
                    mDefaultHandler.uncaughtException(thread, ex);
            }
        }
    }

    /**
     * 自定义错误处理,收集错误信息
     * 发送错误报告等操作均在此完成.
     * 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @return true代表处理该异常，不再向上抛异常，
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理，
     * 简单来说就是true不会弹出那个错误提示框，false就会弹出
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        if (ex instanceof OutOfMemoryError) {
            // OOM一般是解码Bitmap造成的，不影响程序继续运行
            return true;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序出错，即将退出，报告已发送", Toast.LENGTH_LONG).show();
                //添加信息发送或本地保存
                getMobileInfo();//获取手机信息
                getVersionInfo();//获取版本信息
                getErrorInfo(ex);//获取错误信息
                saveInfo2File();
                Looper.loop();
            }

        }.start();
        if (HViewerApplication.DEBUG)
            ex.printStackTrace();
        Logger.d("CrashHandler", "catched");
        MobclickAgent.reportError(mContext, ex);
        return true;
    }

    /**
     * 获取异常信息
     *
     * @param e
     * @return 异常信息
     */
    private void getErrorInfo(Throwable e) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        pw.flush();
        String error = writer.toString();
        infos.put("error information", error);
        try {
            pw.close();
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 获取设备信息
     *
     * @return
     */
    private String getMobileInfo() {
        StringBuffer sb = new StringBuffer();
        //通过反射获取设备信息
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("UncaughtHandler", "has IllegalArgument error at method 'getMobileInfo()'");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("UncaughtHandler", "has IllegalAccess error at method 'getMobileInfo()'");
        }
        return sb.toString();
    }

    /**
     * 获取手机
     *
     * @return
     */
    private void getVersionInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UncaughtHandler", "has error at method 'getVersionInfo()'");
        }
    }

    private void saveInfo2File() {
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            buffer.append(entry.getKey() + " = " + entry.getValue());
            buffer.append("\n");
        }

        String time = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date(System.currentTimeMillis()));
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = mContext.getCacheDir().getAbsolutePath();
            String name = "crash-" + time + ".log";
            String filePath = path + File.separator + name;
            SimpleFileUtil.createIfNotExist(filePath);
            try {
                SimpleFileUtil.writeString(filePath, buffer.toString(), "utf-8");
                SharedPreferencesUtil.saveData(mContext, "unupload_log", true);
                SharedPreferencesUtil.saveData(mContext, "unupload_log_file_path", filePath);
                Logger.d("CrashHandler", "get");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e("UncaughtHandler", "an error occured while writing file...", e);
            }
        }

    }

}