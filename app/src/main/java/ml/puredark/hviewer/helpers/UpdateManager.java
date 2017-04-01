package ml.puredark.hviewer.helpers;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.http.HViewerHttpClient;

public class UpdateManager {

    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;

    private Context mContext;
    //标题
    private String title = "新版本";
    //提示语
    private String updateMsg = "应用更新了哦，亲快下载吧~";
    //返回的安装包url
    private String apkUrl = null;
    private Dialog noticeDialog;
    private Dialog downloadDialog;
    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;
    private TextView fileSize;
    private String fileString;
    private int progress;
    private Thread downLoadThread;
    private boolean interceptFlag = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    fileSize.setText(fileString);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    private static String getCacheDirPath() {
        return HViewerApplication.mContext.getCacheDir().getAbsolutePath();
    }

    private static String getCacheFilePath() {
        return getCacheDirPath() + "/Update.apk";
    }


    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Logger.d("UpdateManager", "apkUrl: " + apkUrl);
                URL url = new URL(apkUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(getCacheDirPath());
                if (!file.exists()) {
                    file.mkdir();
                }
                File apkFile = new File(getCacheFilePath());
                FileOutputStream fos = new FileOutputStream(apkFile);

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    fileString = (count / (1024)) + "KB/" + (length / (1024)) + "KB";
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);//点击取消就停止下载.

                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    public UpdateManager(Context context, String apkUrl, String title, String updateMsg) {
        this.mContext = context;
        this.apkUrl = apkUrl;
        this.title = title;
        this.updateMsg = updateMsg;
    }

    public static void checkUpdate(final Context context) {
        String url = UrlConfig.updateUrl;
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
                        String regex = "\\[.*?\\]\\((.*?)\\)";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(detail);
                        Logger.d("UpdateManager", detail);
                        if (matcher.find() && matcher.groupCount() > 0) {
                            url = matcher.group(1);
                            detail = detail.replaceAll(regex, "");
                        }
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

    public static boolean compareVersion(String oldVersion, String newVersion) {
        if (newVersion == null || "".equals(newVersion)) return false;
        String[] l1 = newVersion.split("\\.");
        String[] l2 = oldVersion.split("\\.");
        int len = Math.max(l1.length, l2.length);
        for (int i = 0; i < len; i++) {
            int n1 = (l1.length > i) ? Integer.parseInt(l1[i]) : 0;
            int n2 = (l2.length > i) ? Integer.parseInt(l2[i]) : 0;
            if (n1 > n2) {
                return true;//需要更新
            } else if (n1 < n2) {
                return false;//不需要更新
            }
        }
        return false;
    }

    //外部接口让主Activity调用
    public boolean checkUpdateInfo(String oldVersion, String newVersion) {
        boolean update = compareVersion(oldVersion, newVersion);
        if (update)
            showNoticeDialog();
        return update;
    }

    private void showNoticeDialog() {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage("正在下载");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.dialog_update, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        fileSize = (TextView) v.findViewById(R.id.fileSize);

        builder.setView(v);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk();
    }

    /**
     * 下载apk
     */

    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File apkfile = new File(getCacheFilePath());
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
