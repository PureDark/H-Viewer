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
import ml.puredark.hviewer.configs.ImagePipelineConfigBuilder;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.http.DownloadUtil;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.activities.BaseActivity;

public class UpdateManager {

    private Context mContext;
    private String title = "新版本";
    private String updateMsg = "应用更新了哦，亲快下载吧~";
    private String apkUrl = null;
    private Dialog noticeDialog;
    private Dialog downloadDialog;
    private ProgressBar barProgress;
    private TextView tvFileSize;
    private boolean interceptFlag = false;

    private String getCacheDirPath() {
        return ImagePipelineConfigBuilder.getDiskCacheDir(mContext).getAbsolutePath();
    }

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
        barProgress = (ProgressBar) v.findViewById(R.id.progress);
        tvFileSize = (TextView) v.findViewById(R.id.fileSize);

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
        HViewerHttpClient.getDownloadUtil().download(apkUrl, getCacheDirPath(), new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                Logger.d("UpdateManager", "file.getAbsolutePath():" + file.getAbsolutePath());
                installApk(file);
            }

            @Override
            public boolean onDownloading(int progress, long downloadedBytes, long totalBytes) {
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String downloadedSize = (downloadedBytes / (1024)) + "KB/" + (totalBytes / (1024)) + "KB";
                        tvFileSize.setText(downloadedSize);
                        barProgress.setProgress(progress);
                    }
                });
                return !interceptFlag;
            }

            @Override
            public void onDownloadFailed(Exception e) {
                e.printStackTrace();
                downloadDialog.dismiss();
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Logger.d("UpdateManager", "apkUrl: " + apkUrl);
                        if (mContext instanceof BaseActivity)
                            ((BaseActivity) mContext).alert("下载失败", "网络错误");
                    }
                });
            }
        });
    }

    /**
     * 安装apk
     */
    private void installApk(File apkfile) {
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Logger.d("UpdateManager", apkfile.toString());
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}
