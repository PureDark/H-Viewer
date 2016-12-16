package ml.puredark.hviewer.download;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.FileType;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static ml.puredark.hviewer.beans.DownloadTask.STATUS_COMPLETED;
import static ml.puredark.hviewer.beans.DownloadTask.STATUS_DOWNLOADING;
import static ml.puredark.hviewer.beans.DownloadTask.STATUS_PAUSED;

/**
 * Created by PureDark on 2016/8/16.
 */

public class DownloadService extends Service {
    public static final String ACTION = "ml.puredark.hviewer.download.DownloadService";
    public static final String ON_START = ".services.DownloadService.onStart";
    public static final String ON_PAUSE = ".services.DownloadService.onPause";
    public static final String ON_PROGRESS = ".services.DownloadService.onProgress";
    public static final String ON_COMPLETE = ".services.DownloadService.onComplete";
    public static final String ON_FAILURE = ".services.DownloadService.onFailure";
    private DownloadBinder binder;

    private DownloadTask currTask;

    private Map<Integer, Picture> pictureInQueue;

    public boolean downloadHighRes() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingFragment.KEY_PREF_DOWNLOAD_HIGH_RES, false);
    }

    public void start(final DownloadTask task) {
        pauseNoBrocast();
        currTask = task;
        currTask.status = STATUS_DOWNLOADING;
        pictureInQueue = new HashMap<>();
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

    public void stop() {
        pauseNoBrocast();
        currTask = null;
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
                // 统计下载完成次数
                MobclickAgent.onEvent(HViewerApplication.mContext, "DownloadTaskCompleted");

                // 记录信息，以求恢复删除了的下载记录
                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                FileHelper.createFileIfNotExist("detail.txt", rootPath, FileHelper.appdirname,dirName);
                FileHelper.writeString(HViewerApplication.getGson().toJson(task), "detail.txt", rootPath, FileHelper.appdirname,dirName);
            }
            return;
        }
        final Picture picture = currPic;

        boolean highRes = downloadHighRes();
        if (!TextUtils.isEmpty(picture.highRes) && highRes) {
            picture.retries = 0;
            loadPicture(picture, task, null, true);
        } else if (!TextUtils.isEmpty(picture.pic) && !highRes) {
            picture.retries = 0;
            loadPicture(picture, task, null, false);
        } else if (task.collection.site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE)
                && task.collection.site.extraRule != null) {
            if(task.collection.site.extraRule.pictureRule != null && task.collection.site.extraRule.pictureRule.url != null)
                getPictureUrl(picture, task, task.collection.site.extraRule.pictureRule.url, task.collection.site.extraRule.pictureRule.highRes);
            else if(task.collection.site.extraRule.pictureUrl != null)
                getPictureUrl(picture, task, task.collection.site.extraRule.pictureUrl, task.collection.site.extraRule.pictureHighRes);
        } else if (task.collection.site.picUrlSelector != null) {
            getPictureUrl(picture, task, task.collection.site.picUrlSelector, null);
        } else {
            picture.pic = picture.url;
            picture.retries = 0;
            loadPicture(picture, task, null, false);
        }
    }

    private void getPictureUrl(final Picture picture, final DownloadTask task, final Selector selector, final Selector highResSelector) {
        Logger.d("DownloadService", picture.url);
        if (Picture.hasPicPosfix(picture.url)) {
            picture.pic = picture.url;
            loadPicture(picture, task, null, false);
        } else
            //如果需要执行JS才能获取完整数据，则不得不使用webView来载入页面
            if (task.collection.site.hasFlag(Site.FLAG_JS_NEEDED_ALL)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    WebView webView = new WebView(HViewerApplication.mContext);
                    WebSettings mWebSettings = webView.getSettings();
                    mWebSettings.setJavaScriptEnabled(true);
                    mWebSettings.setBlockNetworkImage(true);
                    mWebSettings.setDomStorageEnabled(true);
                    mWebSettings.setUserAgentString(getResources().getString(R.string.UA));
                    mWebSettings.setCacheMode(LOAD_CACHE_ELSE_NETWORK);
                    webView.addJavascriptInterface(this, "HtmlParser");

                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            //Load HTML
                            pictureInQueue.put(picture.pid, picture);
                            boolean extra = !selector.equals(task.collection.site.picUrlSelector);
                            webView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, " + picture.pid + ", " + extra + ");");
                            Logger.d("DownloadService", "onPageFinished");
                        }
                    });
                    webView.loadUrl(picture.url);
                    new Handler().postDelayed(() -> webView.stopLoading(), 30000);
                });
                Logger.d("DownloadService", "WebView");
            } else
                HViewerHttpClient.get(picture.url, task.collection.site.getHeaders(), new HViewerHttpClient.OnResponseListener() {

                    @Override
                    public void onSuccess(String contentType, Object result) {
                        if (result == null || result.equals(""))
                            onFailure(null);
                        else if (contentType.contains("image")) {
                            picture.pic = picture.url;
                            if (result instanceof Bitmap)
                                loadPicture(picture, task, (Bitmap) result, false);
                            else
                                loadPicture(picture, task, null, false);
                        } else {
                            picture.pic = RuleParser.getPictureUrl((String) result, selector, picture.url);
                            picture.highRes = RuleParser.getPictureUrl((String) result, highResSelector, picture.url);
                            if (!TextUtils.isEmpty(picture.highRes) && downloadHighRes()) {
                                picture.retries = 0;
                                picture.referer = picture.url;
                                loadPicture(picture, task, null, true);
                            } else if (!TextUtils.isEmpty(picture.pic)) {
                                picture.retries = 0;
                                picture.referer = picture.url;
                                loadPicture(picture, task, null, false);
                            } else {
                                onFailure(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(HViewerHttpClient.HttpError error) {
                        task.status = STATUS_PAUSED;
                        picture.status = Picture.STATUS_WAITING;
                        Intent intent = new Intent(ON_FAILURE);
                        intent.putExtra("message", "图片地址获取失败，请检查网络连接");
                        Logger.d("DownloadService", "apiUrl : " + picture.url);
                        sendBroadcast(intent);
                    }
                });
    }

    @JavascriptInterface
    public void onResultGot(String html, int pid, boolean extra) {
        Picture picture = pictureInQueue.get(pid);
        if (picture == null)
            return;
        pictureInQueue.remove(pid);
        Selector selector = (extra) ? currTask.collection.site.extraRule.pictureUrl : currTask.collection.site.picUrlSelector;
        Selector highResSelector = (extra) ? currTask.collection.site.extraRule.pictureHighRes : null;
        picture.pic = RuleParser.getPictureUrl(html, selector, picture.url);
        picture.highRes = RuleParser.getPictureUrl(html, highResSelector, picture.url);
        if (!TextUtils.isEmpty(picture.highRes) && downloadHighRes()) {
            picture.retries = 0;
            picture.referer = picture.url;
            loadPicture(picture, currTask, null, true);
        } else if (!TextUtils.isEmpty(picture.pic)) {
            picture.retries = 0;
            picture.referer = picture.url;
            loadPicture(picture, currTask, null, false);
        } else {
            currTask.status = STATUS_PAUSED;
            picture.status = Picture.STATUS_WAITING;
            Intent intent = new Intent(ON_FAILURE);
            intent.putExtra("message", "图片地址获取失败，请检查网络连接");
            Logger.d("DownloadService", "apiUrl : " + picture.url);
            sendBroadcast(intent);
        }
    }

    private void loadPicture(final Picture picture, final DownloadTask task, Bitmap bitmap, final boolean highRes) {
        if (bitmap != null) {
            savePicture(picture, task, bitmap);
        } else {
            String url = (highRes) ? picture.highRes : picture.pic;
            Logger.d("DownloadService", "loadPicture pic : " + picture.pic);
            Logger.d("DownloadService", "loadPicture highRes : " + picture.highRes);
            Logger.d("DownloadService", "loadPicture apiUrl : " + url);
            ImageLoader.loadResourceFromUrl(getApplicationContext(), url, task.collection.site.cookie, picture.referer,
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
                                loadPicture(picture, task, null, highRes);
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
    }

    private void savePicture(Picture picture, DownloadTask task, Object pic) {
        try {
            DocumentFile documentFile;
            if (pic instanceof Bitmap) {
                String fileName;
                if (task.collection.pictures.size() >= 1000) {
                    fileName = String.format("%04d", picture.pid);
                } else if (task.collection.pictures.size() >= 100) {
                    fileName = String.format("%03d", picture.pid);
                } else if (task.collection.pictures.size() >= 10) {
                    fileName = String.format("%02d", picture.pid);
                } else {
                    fileName = picture.pid + "";
                }
                fileName += ".jpg";
                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                documentFile = FileHelper.createFileIfNotExist(fileName, rootPath, FileHelper.appdirname,dirName);
                FileHelper.saveBitmapToFile((Bitmap) pic, documentFile);
            } else if (pic instanceof PooledByteBuffer) {
                String fileName;
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
                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                documentFile = FileHelper.createFileIfNotExist(fileName, rootPath, FileHelper.appdirname,dirName);
                if (!FileHelper.writeBytes(bytes, documentFile)) {
                    throw new IOException();
                }
            } else
                return;
            if (documentFile == null)
                return;
            if (picture.pid == 1) {
                task.collection.cover = documentFile.getUri().toString();
            }
            picture.thumbnail = documentFile.getUri().toString();
            picture.pic = documentFile.getUri().toString();
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
            intent.putExtra("message", "保存失败，请重新设置下载目录");
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

        public void stop() {
            DownloadService.this.stop();
        }

        public DownloadTask getCurrTask() {
            return DownloadService.this.getCurrTask();
        }
    }

}
