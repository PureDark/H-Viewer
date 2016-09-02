package ml.puredark.hviewer.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.memory.PooledByteBuffer;

import java.io.File;
import java.io.IOException;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.ImageLoader;
import ml.puredark.hviewer.helpers.RuleParser;
import ml.puredark.hviewer.utils.FileType;
import ml.puredark.hviewer.utils.ImageScaleUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

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
        if (task.collection.site.picUrlSelector == null) {
            picture.pic = picture.url;
            loadBitmap(picture, task, null);
        } else if (picture.pic != null) {
            loadBitmap(picture, task, null);
        } else {
            getPictureUrl(picture, task);
        }
    }

    private void getPictureUrl(final Picture picture, final DownloadTask task) {
        if (picture.url.endsWith(".jpg") || picture.url.endsWith(".png") || picture.url.endsWith(".bmp")) {
            picture.pic = picture.url;
            loadBitmap(picture, task, null);
        } else
            HViewerHttpClient.get(picture.url, task.collection.site.getCookies(), new HViewerHttpClient.OnResponseListener() {

                @Override
                public void onSuccess(String contentType, Object result) {
                    if (result == null || result.equals(""))
                        onFailure(null);
                    else if (contentType.contains("image")) {
                        picture.pic = picture.url;
                        if (result instanceof Bitmap) {
                            loadBitmap(picture, task, (Bitmap) result);
                        } else {
                            loadBitmap(picture, task, null);
                        }
                    } else {
                        picture.pic = RuleParser.getPictureUrl((String) result, task.collection.site.picUrlSelector, picture.url);
                        picture.retries = 0;
                        picture.referer = picture.url;
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

    private void loadBitmap(final Picture picture, final DownloadTask task, Bitmap bitmap) {
        if (bitmap != null) {
            savePicture(picture, task, bitmap);
        } else
            ImageLoader.loadResourceFromUrl(getApplicationContext(), picture.pic, task.collection.site.cookie, picture.referer,
                    new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
                        private DownloadTask myTask = task;

                        @Override
                        protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                            if (!dataSource.isFinished()) {
                                return;
                            }
                            CloseableReference<PooledByteBuffer> ref = dataSource.getResult();
                            if (ref != null) {
                                try {
                                    PooledByteBuffer imageBuffer = ref.get();
                                    savePicture(picture, myTask, imageBuffer);
                                } finally {
                                    CloseableReference.closeSafely(ref);
                                }
                            }
                        }

                        @Override
                        protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                            if (picture.retries < 15) {
                                picture.retries++;
                                picture.status = Picture.STATUS_DOWNLOADING;
                                loadBitmap(picture, task, null);
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

    private void savePicture(Picture picture, DownloadTask task, Object pic) {
        try {
            String fileName, filePath;
            if (pic instanceof Bitmap) {
                if (task.collection.pictures.size() >= 1000) {
                    fileName = String.format("%04d", picture.pid);
                } else if (task.collection.pictures.size() >= 100) {
                    fileName = String.format("%03d", picture.pid);
                } else if (task.collection.pictures.size() >= 10) {
                    fileName = String.format("%02d", picture.pid);
                } else {
                    fileName = picture.pid + "";
                }
                filePath = task.path + fileName + ".jpg";
                ImageScaleUtil.saveToFile(HViewerApplication.mContext, (Bitmap) pic, filePath);
            } else if (pic instanceof PooledByteBuffer) {
                PooledByteBuffer buffer = (PooledByteBuffer) pic;
                byte[] bytes = new byte[buffer.size()];
                buffer.read(0, bytes, 0, buffer.size());
                if (task.collection.pictures.size() >= 1000) {
                    fileName = String.format("%04d", picture.pid);
                } else if (task.collection.pictures.size() >= 100) {
                    fileName = String.format("%03d", picture.pid);
                } else if (task.collection.pictures.size() >= 10) {
                    fileName = String.format("%02d", picture.pid);
                } else {
                    fileName = picture.pid + "";
                }
                String postfix = FileType.getFileType(bytes, FileType.TYPE_IMAGE);
                fileName += "." + postfix;
                filePath = task.path + fileName;

                SimpleFileUtil.createIfNotExist(filePath);
                SimpleFileUtil.writeBytes(filePath, bytes);
            }else
                return;
            if (picture.pid == 0) {
                task.collection.cover = "file://" + filePath;
            }
            picture.thumbnail = "file://" + filePath;
            picture.pic = "file://" + filePath;
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
        } catch (OutOfMemoryError error) {
            // 这里就算OOM了，就当作下载失败，不影响程序继续运行
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
