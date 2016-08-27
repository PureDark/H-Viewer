package ml.puredark.hviewer.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;

import java.io.IOException;
import java.lang.annotation.Target;
import java.util.List;

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
        downloadNewPage(currTask);
        downloadNewPage(currTask);
        downloadNewPage(currTask);
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

    private void downloadNewPage(final DownloadTask task) {
        boolean isCompleted = true;
        Picture currPic = null;
        for (Picture picture : task.collection.pictures) {
            if (picture.status == Picture.STATUS_WAITING) {
                currPic = picture;
                currPic.status = Picture.STATUS_DOWNLOADING;
                isCompleted = false;
                break;
            } else if (picture.status == Picture.STATUS_DOWNLOADING) {
                isCompleted = false;
            }
        }

        if (currPic == null) {
            if (isCompleted) {
                task.status = STATUS_COMPLETED;
                Intent intent = new Intent(ON_COMPLETE);
                sendBroadcast(intent);
            }
            return;
        }
        final Picture picture = currPic;
        if (picture.pic != null) {
            loadBitmap(picture, task, null);
        } else {
            HViewerHttpClient.get(picture.url, task.collection.site.getCookies(), new HViewerHttpClient.OnResponseListener() {
                @Override
                public void onSuccess(String contentType, Object result) {
                    if (contentType.contains("image") && result instanceof Bitmap) {
                        picture.pic = picture.url;
                        Bitmap bitmap = (Bitmap) result;
                        loadBitmap(picture, task, bitmap);
                    } else {
                        if(result == null)
                            onFailure(null);

                        picture.pic = RuleParser.getPictureUrl((String) result, task.collection.site.picUrlSelector, picture.url);
                        if(picture.pic==null)
                            onFailure(null);
                        else
                            loadBitmap(picture, task, null);
                    }
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    task.status = STATUS_PAUSED;
                    picture.status = Picture.STATUS_WAITING;
                    Intent intent = new Intent(ON_FAILURE);
                    intent.putExtra("message", "图片地址获取失败，请检查网络连接");
                    sendBroadcast(intent);
                }
            });
        }
    }

    private void loadBitmap(final Picture picture, final DownloadTask task, Bitmap bitmap) {
        if (bitmap != null) {
            saveBitmap(picture, task, bitmap);
        } else
            HViewerApplication.loadBitmapFromUrl(getApplicationContext(), picture.pic, task.collection.site.cookie, picture.referer,  new BaseBitmapDataSubscriber() {
                private DownloadTask myTask = task;

                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    saveBitmap(picture, myTask, bitmap);
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    if (picture.retries < 15) {
                        picture.status = Picture.STATUS_DOWNLOADING;
                        loadBitmap(picture, task, null);
                        picture.retries++;
                    } else {
                        picture.retries = 0;
                        task.status = STATUS_PAUSED;
                        picture.status = Picture.STATUS_WAITING;
                        Intent intent = new Intent(ON_FAILURE);
                        intent.putExtra("message", "图片下载失败，也许您需要代理");
                        sendBroadcast(intent);
                    }
                }
            });
    }

    private void saveBitmap(Picture picture, DownloadTask task, Bitmap bitmap) {
        try {
            String filePath = task.path + picture.pid + ".jpg";
            if (task.collection.pictures.size() >= 100) {
                if (picture.pid >= 99)
                    filePath = task.path + picture.pid + ".jpg";
                else if (picture.pid >= 9)
                    filePath = task.path + "0" + picture.pid + ".jpg";
                else
                    filePath = task.path + "00" + picture.pid + ".jpg";
            } else if (task.collection.pictures.size() >= 10) {
                if (picture.pid >= 9)
                    filePath = task.path + picture.pid + ".jpg";
                else
                    filePath = task.path + "0" + picture.pid + ".jpg";
            }
            ImageScaleUtil.saveToFile(HViewerApplication.mContext, bitmap, filePath);
            if (picture.pid == 0) {
                task.collection.cover = filePath;
            }
            picture.thumbnail = filePath;
            picture.pic = filePath;
            picture.retries = 0;
            picture.status = Picture.STATUS_DOWNLOADED;
            Intent intent = new Intent(ON_PROGRESS);
            sendBroadcast(intent);
            if (task.status != STATUS_PAUSED && task.status != STATUS_COMPLETED) {
                downloadNewPage(task);
            }

            //Log.d("DownloadManager", "picture.pid = " + picture.pid);
        } catch (IOException e) {
            e.printStackTrace();
            task.status = STATUS_PAUSED;
            picture.status = Picture.STATUS_WAITING;
            Intent intent = new Intent(ON_FAILURE);
            intent.putExtra("message", "文件保存失败，请检查剩余空间");
            sendBroadcast(intent);
        }
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
