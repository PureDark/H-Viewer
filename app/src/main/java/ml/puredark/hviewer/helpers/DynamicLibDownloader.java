package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.http.DownloadUtil;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.activities.BaseActivity;

/**
 * Created by PureDark on 2017/4/1.
 */

public class DynamicLibDownloader {
    private BaseActivity mActivity;
    private boolean interceptFlag = false;
    private AlertDialog downloadDialog;
    private ProgressBar barProgress;
    private TextView tvFileCount, tvFileSize;
    private int filecount;

    public DynamicLibDownloader(BaseActivity activity) {
        mActivity = activity;
    }

    public void checkDownloadLib() {
        new AlertDialog.Builder(mActivity).setTitle("需要下载解码器")
                .setMessage("大概4mb，确认下载吗？")
                .setPositiveButton(mActivity.getString(R.string.ok), (dialog, which) -> {
                    String supportedAbi = DynamicIjkLibLoader.getSupportedAbi();
                    if (supportedAbi == null) {
                        mActivity.alert("下载失败", "不支持该机型的CPU架构");
                    } else {
                        String libDirPath = DynamicIjkLibLoader.getLibDir().getAbsolutePath();
                        String[] urls = UrlConfig.getIjkLibUrl(supportedAbi);
                        showDownloadDialog(mActivity);
                        filecount = urls.length;
                        if (urls.length == 0) {
                            mActivity.alert("下载失败", "无可用链接");
                        } else {
                            downloadLibs(urls, libDirPath, 0);
                        }

                    }
                })
                .setNegativeButton(mActivity.getString(R.string.cancel), null)
                .show();
    }


    private void showDownloadDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("下载解码器");

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_update, null);
        barProgress = (ProgressBar) v.findViewById(R.id.progress);
        tvFileCount = (TextView) v.findViewById(R.id.fileCount);
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
    }

    private void downloadLibs(String[] urls, String saveDir, int pos) {
        if (pos >= urls.length) {
            downloadDialog.dismiss();
            mActivity.runOnUiThread(() -> {
                mActivity.alert("下载成功", "所有解码包均已下载完成");
            });
            return;
        }
        Logger.d("DynamicLibDownloader", "saveDir:" + saveDir);
        HViewerHttpClient.getDownloadUtil().download(urls[pos], saveDir, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                Logger.d("DynamicLibDownloader", "file.getAbsolutePath():" + file.getAbsolutePath());
                downloadLibs(urls, saveDir, pos + 1);
            }

            @Override
            public boolean onDownloading(int progress, long downloadedBytes, long totalBytes) {
                mActivity.runOnUiThread(() -> {
                    String downloadedSize = (downloadedBytes / (1024)) + "KB/" + (totalBytes / (1024)) + "KB";
                    tvFileCount.setText(pos + "/" + filecount);
                    tvFileSize.setText(downloadedSize);
                    barProgress.setProgress(progress);
                });
                return !interceptFlag;
            }

            @Override
            public void onDownloadFailed(Exception e) {
                e.printStackTrace();
                downloadDialog.dismiss();
                mActivity.runOnUiThread(() -> {
                    Logger.d("DynamicLibDownloader", "urls[" + pos + "]:" + urls[pos]);
                    mActivity.alert("下载失败", "网络错误");
                });
            }
        });
    }

}
