package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.http.DownloadUtil;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import tv.danmaku.ijk.media.player.IjkLibLoader;

/**
 * Created by PureDark on 2017/4/1.
 */

public class DynamicIjkLibLoader implements IjkLibLoader {
    private final static String TAG = "DynamicIjkLibLoader";
    private final static List<String> SUPPORTED_ABIS = new ArrayList<>();

    static {
        SUPPORTED_ABIS.add("armeabi-v7a");
        SUPPORTED_ABIS.add("armeabi");
        //SUPPORTED_ABIS.add("arm64-v8a");
        //SUPPORTED_ABIS.add("x86");
        //SUPPORTED_ABIS.add("x86_64");
    }

    @Override
    public void loadLibrary(String s) throws UnsatisfiedLinkError, SecurityException {
        System.load(getLibDir().getAbsolutePath() + "/lib" + s + ".so");
    }

    public static File getLibDir() {
        return HViewerApplication.mContext.getDir("lib", Context.MODE_PRIVATE);
    }

    public static boolean isLibrariesDownloaded() {
        File libDir = getLibDir();
        boolean isLoaded = new File(libDir, "libijkffmpeg.so").exists()
                && new File(libDir, "libijkplayer.so").exists()
                && new File(libDir, "libijksdl.so").exists();
        printDirectory(libDir);
        Logger.d("DynamicIjkLibLoader", libDir.getAbsolutePath());
        return isLoaded;
    }

    public static void printDirectory(File file) {
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                printDirectory(childFile);
            }
            Logger.d("DynamicIjkLibLoader", childFile.getName());
        }
    }

    public static String getSupportedAbi(){
        String cpuAbi = null;
        if (Build.VERSION.SDK_INT >= 21) {
            String[] abis = Build.SUPPORTED_ABIS;
            if (abis != null && abis.length > 0) {
                for (String abi : abis) {
                    Logger.d(TAG, "try supported abi:" + abi);
                    if (SUPPORTED_ABIS.contains(abi)) {
                        cpuAbi = abi;
                        break;
                    }
                }
            } else {
                Logger.d(TAG, " get abis == null");
            }
        } else {
            Logger.d(TAG, "try supported api:" + Build.CPU_ABI + " " + Build.CPU_ABI2);
            if (SUPPORTED_ABIS.contains(Build.CPU_ABI)) {
                cpuAbi = Build.CPU_ABI;
            } else if (SUPPORTED_ABIS.contains(Build.CPU_ABI2)) {
                cpuAbi = Build.CPU_ABI2;
            }
        }
        Logger.d(TAG, " last supported abi:" + cpuAbi);
        return cpuAbi;
    }


}
