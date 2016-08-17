package ml.puredark.hviewer.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.IOException;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.RuleParser;
import ml.puredark.hviewer.utils.ImageScaleUtil;

import static ml.puredark.hviewer.beans.DownloadTask.STATUS_COMPLETED;
import static ml.puredark.hviewer.beans.DownloadTask.STATUS_DOWNLOADING;
import static ml.puredark.hviewer.beans.DownloadTask.STATUS_PAUSED;

/**
 * Created by PureDark on 2016/8/16.
 */

public class DownloadService extends Service {
    public static final String ACTION = "ml.puredark.hviewer.services.DownloadService";
    public static final String ON_START = ".services.DownloadService.onStart";
    public static final String ON_PAUSE = ".services.DownloadService.onPause";
    public static final String ON_PROGRESS = ".services.DownloadService.onProgress";
    public static final String ON_COMPLETE = ".services.DownloadService.onComplete";
    public static final String ON_FAILURE = ".services.DownloadService.onFailure";
    private DownloadBinder binder;

    private DownloadTask currTask;

    public void start(final DownloadTask task) {
        pauseNoBrocast();
        currTask = task;
        currTask.status = STATUS_DOWNLOADING;
        downloadCurrPage(currTask);
        Intent intent = new Intent(ON_START);
        sendBroadcast(intent);
    }

    public void pause() {
        if (currTask != null && currTask.status != STATUS_COMPLETED) {
            currTask.status = STATUS_PAUSED;
            currTask = null;
            Intent intent = new Intent(ON_PAUSE);
            sendBroadcast(intent);
        }
    }

    private void pauseNoBrocast() {
        if (currTask != null && currTask.status != STATUS_COMPLETED) {
            currTask.status = STATUS_PAUSED;
            currTask = null;
        }
    }

    public DownloadTask getCurrTask() {
        return currTask;
    }

    private void downloadCurrPage(final DownloadTask task) {
        if (task.curPosition > task.collection.pictures.size())
            return;
        final Picture picture = task.collection.pictures.get(task.curPosition);
        if (picture.pic != null) {
            loadBitmap(picture, task);
        } else {
            HViewerHttpClient.get(picture.url, task.collection.site.getCookies(), new HViewerHttpClient.OnResponseListener() {
                @Override
                public void onSuccess(String result) {
                    picture.pic = RuleParser.getPictureUrl(result, task.collection.site.picUrlSelector, picture.url);
                    loadBitmap(picture, task);
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    task.status = STATUS_PAUSED;
                    Intent intent = new Intent(ON_FAILURE);
                    intent.putExtra("message", "图片地址获取失败，请检查网络连接");
                    sendBroadcast(intent);
                }
            });
        }
    }

    private void loadBitmap(final Picture picture, final DownloadTask task) {
        HViewerApplication.loadBitmapFromUrl(picture.pic, new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
            private DownloadTask myTask = task;

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                try {
                    String filePath = myTask.path + (myTask.curPosition + 1) + ".jpg";
                    if (myTask.collection.pictures.size() >= 100) {
                        if (myTask.curPosition >= 99)
                            filePath = myTask.path + (myTask.curPosition + 1) + ".jpg";
                        else if (myTask.curPosition >= 9)
                            filePath = myTask.path + "0" + (myTask.curPosition + 1) + ".jpg";
                        else
                            filePath = myTask.path + "00" + (myTask.curPosition + 1) + ".jpg";
                    } else if (myTask.collection.pictures.size() >= 10) {
                        if (myTask.curPosition >= 9)
                            filePath = myTask.path + (myTask.curPosition + 1) + ".jpg";
                        else
                            filePath = myTask.path + "0" + (myTask.curPosition + 1) + ".jpg";
                    }
                    ImageScaleUtil.saveToFile(HViewerApplication.mContext, resource, filePath);
                    if (myTask.curPosition == 0) {
                        myTask.collection.cover = filePath;
                    }
                    picture.thumbnail = filePath;
                    picture.pic = filePath;
                    picture.retries = 0;
                    myTask.curPosition++;
                    Intent intent = new Intent(ON_PROGRESS);
                    sendBroadcast(intent);
                    if (myTask.curPosition == myTask.collection.pictures.size()) {
                        task.status = STATUS_COMPLETED;
                        intent = new Intent(ON_COMPLETE);
                        sendBroadcast(intent);
                    } else if (task.status != STATUS_PAUSED && task.status != STATUS_COMPLETED) {
                        downloadCurrPage(myTask);
                    }

                    Log.d("DownloadManager", "myTask.curPosition=" + myTask.curPosition);
                } catch (IOException e) {
                    e.printStackTrace();
                    task.status = STATUS_PAUSED;
                    Intent intent = new Intent(ON_FAILURE);
                    intent.putExtra("message", "文件保存失败，请检查剩余空间");
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                if (picture.retries < 3) {
                    loadBitmap(picture, task);
                    picture.retries++;
                } else {
                    picture.retries = 0;
                    task.status = STATUS_PAUSED;
                    Intent intent = new Intent(ON_FAILURE);
                    intent.putExtra("message", "图片下载失败，也许您需要代理");
                    sendBroadcast(intent);
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null)
            binder = new DownloadBinder();
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pause();
    }

    public class DownloadBinder extends Binder {

        public void start(DownloadTask task) {
            DownloadService.this.start(task);
        }

        public void pause() {
            DownloadService.this.pause();
        }

        public DownloadTask getCurrTask() {
            return DownloadService.this.getCurrTask();
        }
    }

}
