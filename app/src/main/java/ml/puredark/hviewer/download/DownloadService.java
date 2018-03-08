package ml.puredark.hviewer.download;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.listener.DownloadListener;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Video;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.FileType;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import okhttp3.MediaType;
import okhttp3.Response;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static ml.puredark.hviewer.HViewerApplication.mContext;
import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_DOWNLOADED;
import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_DOWNLOADING;
import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_WAITING;
import static ml.puredark.hviewer.beans.DownloadTask.STATUS_COMPLETED;
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

    private DownloadTaskHolder holder = new DownloadTaskHolder(mContext);

    private DownloadTask currTask;
    //    private BaseDownloadTask videoTask;
    private DownloadInfo currInfo;
    private com.lzy.okserver.download.DownloadManager downloadManager = com.lzy.okserver.download.DownloadService.getDownloadManager();

    private String cachePath = mContext.getCacheDir().getAbsolutePath();

    private Map<Integer, Picture> pictureInQueue;

    public boolean downloadHighRes() {
        return (boolean) SharedPreferencesUtil.getData(mContext,
                SettingFragment.KEY_PREF_DOWNLOAD_HIGH_RES, false);
    }

    public void start(final DownloadTask task) {
        pauseNoBrocast();
        currTask = task;
        currTask.status = DownloadTask.STATUS_GETTING;
        if (task.collection.videos != null && task.collection.videos.size() > 0) {
            //视频下载
            downloadNewVideo(currTask);
        } else {
            //图片下载
            pictureInQueue = new HashMap<>();
            downloadNewPage(currTask);
            downloadNewPage(currTask);
            downloadNewPage(currTask);
        }
        Intent intent = new Intent(ON_START);
        sendBroadcast(intent);
    }

    public void restart(final DownloadTask task) {
        if (task.collection.videos != null && task.collection.videos.size() > 0) {
            for (Video video : task.collection.videos) {
                video.vlink = null;
                video.percent = 0;
                DownloadInfo downloadInfo = downloadManager.getDownloadInfo(video.content);
                if (downloadInfo != null) {
                    downloadManager.pauseTask(downloadInfo.getTaskKey());
                    downloadManager.removeTask(downloadInfo.getTaskKey());
                    Logger.d("DownloadService", "removeTask");
                }
            }
        }
        start(task);
    }

    public void pause() {
        if (currTask != null && currTask.status != STATUS_COMPLETED) {
            currTask.status = STATUS_PAUSED;
            if (currInfo != null) {
                downloadManager.pauseTask(currInfo.getTaskKey());
                currInfo = null;
            }
            if (currTask.collection.videos != null) {
                for (Video video : currTask.collection.videos)
                    video.status = STATUS_WAITING;
            }
            holder.updateDownloadTasks(currTask);
            currTask = null;
            Intent intent = new Intent(ON_PAUSE);
            sendBroadcast(intent);
        }
    }

    private void pauseNoBrocast() {
        if (currTask != null && currTask.status != STATUS_COMPLETED) {
            currTask.status = STATUS_PAUSED;
            if (currInfo != null) {
                downloadManager.pauseTask(currInfo.getTaskKey());
                currInfo = null;
            }
            if (currTask.collection.videos != null) {
                for (Video video : currTask.collection.videos)
                    video.status = STATUS_WAITING;
            }
            holder.updateDownloadTasks(currTask);
            currTask = null;
        }
    }

    public void stop() {
        pauseNoBrocast();
        currTask = null;
        if (currInfo != null) {
            downloadManager.pauseTask(currInfo.getTaskKey());
            downloadManager.removeTask(currInfo.getTaskKey());
            currInfo = null;
        }
    }

    public DownloadTask getCurrTask() {
        return currTask;
    }

    private void downloadNewVideo(final DownloadTask task) {
        boolean isCompleted = true;
        Video currVideo = null;
        for (Video video : task.collection.videos) {
            if (video.vlink != null && (video.vlink.startsWith("file://") || video.vlink.startsWith("content://"))) {
                video.status = STATUS_DOWNLOADED;
            }
            if (video.status == STATUS_WAITING) {
                video.status = STATUS_DOWNLOADING;
                currVideo = video;
                isCompleted = false;
                break;
            } else if (video.status == STATUS_DOWNLOADING) {
                isCompleted = false;
            }
        }
        Logger.d("DownloadService", "downloadNewVideo: isCompleted = " + isCompleted);

        if (currVideo == null) {
            if (isCompleted) {
                task.status = STATUS_COMPLETED;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                task.collection.datetime = dateFormat.format(calendar.getTime());
                task.collection.gid = 0;
                holder.updateDownloadTasks(task);
                Intent intent = new Intent(ON_COMPLETE);
                sendBroadcast(intent);
                // 统计下载完成次数
                MobclickAgent.onEvent(mContext, "DownloadTaskCompleted");

                // 记录信息，以求恢复删除了的下载记录
                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                FileHelper.createFileIfNotExist("detail.txt", rootPath, dirName);
                FileHelper.writeString(HViewerApplication.getGson().toJson(task), "detail.txt", rootPath, dirName);
            }
            return;
        }
        Logger.d("DownloadService", "downloadNewVideo: isCompleted2 = " + isCompleted);
        if (!TextUtils.isEmpty(currVideo.vlink)) {
            currVideo.retries = 0;
            loadVideo(currVideo, task);
        } else {
            getVideoUrl(currVideo, task);
        }
    }

    private void getVideoUrl(final Video video, final DownloadTask task) {
        Logger.d("DownloadService", "getVideoUrl: video.content = " + video.content);
        new Thread(() -> {
            Response response = HViewerHttpClient.getResponseHeader(video.content, task.collection.site.getHeaders());
            if (response == null) {
                task.status = STATUS_PAUSED;
                holder.updateDownloadTasks(task);
                Intent intent = new Intent(ON_FAILURE);
                intent.putExtra("message", "视频地址获取失败，请检查网络连接");
                sendBroadcast(intent);
                return;
            }
            MediaType contentType = response.body().contentType();
            String finalUrl = response.request().url().toString();
            Logger.d("DownloadService", "contentType = " + contentType.toString());
            Logger.d("DownloadService", "finalUrl = " + finalUrl);
            if (contentType.type().equals("video")) {
                video.vlink = finalUrl;
                Logger.d("DownloadService", "realUrl=" + video.vlink);
                video.retries = 0;
                loadVideo(video, task);
            } else {
                HViewerHttpClient.get(video.content, task.collection.site.getHeaders(), new HViewerHttpClient.OnResponseListener() {
                    @Override
                    public void onSuccess(String contentType, Object result) {
                        if (result == null || result.equals("") || !(result instanceof String)) {
                            onFailure(null);
                        } else {
                            String html = (String) result;
                            List<String> videoUrls = RuleParser.getVideoUrl(html, video.content);
                            if (videoUrls.size() <= 0) {
                                onFailure(null);
                            } else {
                                new Thread(() -> {
                                    String realUrl = null;
                                    if (videoUrls.size() == 1) {
                                        realUrl = videoUrls.get(0);
                                        Logger.d("DownloadService", "videoUrl=" + videoUrls.get(0));
                                    } else {
                                        long maxSize = 0;
                                        for (String videoUrl : videoUrls) {
                                            Logger.d("DownloadService", "videoUrl=" + videoUrl);
                                            long size = HViewerHttpClient.getContentLength(videoUrl, task.collection.site.getHeaders());
                                            if (size == 0)
                                                continue;
                                            if (size > maxSize) {
                                                maxSize = size;
                                                realUrl = videoUrl;
                                            }
                                            Logger.d("DownloadService", "size=" + size);
                                        }
                                    }
                                    if (realUrl == null) {
                                        onFailure(null);
                                        return;
                                    }
                                    video.vlink = realUrl;
                                    Logger.d("DownloadService", "realUrl=" + realUrl);
                                    video.retries = 0;
                                    loadVideo(video, task);
                                }).start();
                            }
                        }
                    }

                    @Override
                    public void onFailure(HViewerHttpClient.HttpError error) {
                        task.status = STATUS_PAUSED;
                        holder.updateDownloadTasks(task);
                        Intent intent = new Intent(ON_FAILURE);
                        intent.putExtra("message", "视频地址获取失败，请检查网络连接");
                        Logger.d("DownloadService", "video.content : " + video.content);
                        sendBroadcast(intent);
                    }
                });
            }
        }).start();
    }

    private void loadVideo(final Video video, final DownloadTask task) {
        Logger.d("DownloadService", "loadVideo: video.vlink=" + video.vlink);
        downloadManager.pauseAllTask();
        DownloadInfo downloadInfo = downloadManager.getDownloadInfo(video.content);
        if (downloadInfo != null) {
            Logger.d("DownloadService", "loadVideo: startExisted : ");
            downloadManager.addTask(downloadInfo.getTaskKey(), downloadInfo.getRequest(), downloadInfo.getListener());
            currInfo = downloadInfo;
        } else {
            Logger.d("DownloadService", "loadVideo: addNew");
            String dirName = DownloadManager.generateDirName(task.collection, 0);
            int i = 2;
            while (FileHelper.isFileExist(dirName, cachePath)) {
                dirName = DownloadManager.generateDirName(task.collection, i++);
            }
            downloadManager.setTargetFolder(cachePath + "/" + dirName);
            GetRequest request = OkGo.get(video.vlink);
            downloadManager.addTask(video.content, request, new DownloadListener() {
                @Override
                public void onProgress(DownloadInfo downloadInfo) {
                    long currentSize = downloadInfo.getDownloadLength();
                    float progress = downloadInfo.getProgress();
                    Logger.d("DownloadService", "onProgress: currentSize=" + currentSize);
                    Logger.d("DownloadService", "onProgress: progress=" + progress);
                    Logger.d("DownloadService", "onProgress: video.status=" + video.status);
                    video.percent = Math.round(progress * 100);
                    Intent intent = new Intent(ON_PROGRESS);
                    sendBroadcast(intent);
                }

                @Override
                public void onFinish(DownloadInfo downloadInfo) {
                    new Thread(() -> {
                        long currentSize = downloadInfo.getDownloadLength();
                        Logger.d("DownloadService", "onFinish: currentSize=" + currentSize);
                        Logger.d("DownloadService", "onFinish: targetPath=" + downloadInfo.getTargetPath());
                        saveVideo(video, task, Uri.parse(downloadInfo.getTargetPath()));
                        Intent intent = new Intent(ON_PROGRESS);
                        sendBroadcast(intent);
                        if (task.status != STATUS_PAUSED && task.status != STATUS_COMPLETED) {
                            downloadNewVideo(task);
                        }
                    }).start();
                }

                @Override
                public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
                    if (video.retries < 15) {
                        int delay = 1000 * video.retries;
                        video.retries++;
                        video.status = STATUS_DOWNLOADING;
                        new Handler().postDelayed(() -> loadVideo(video, task), delay);
                    } else {
                        video.retries = 0;
                        task.status = STATUS_PAUSED;
                        video.status = STATUS_WAITING;
                        holder.updateDownloadTasks(task);
                        Intent intent = new Intent(ON_FAILURE);
                        intent.putExtra("message", errorMsg);
                        Logger.d("DownloadService", "video.content : " + video.content);
                        sendBroadcast(intent);
                    }
                }
            });
            currInfo = downloadManager.getDownloadInfo(video.vlink);
        }
    }

    private void saveVideo(Video video, DownloadTask task, Uri uri) {
        DocumentFile documentFile;
        File file = new File(uri.getPath());
        String fileName = getFileName(video.vid, task.collection.videos.size());
        String postfix = "mp4";
        fileName += "." + postfix;
        String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
        String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
        documentFile = FileHelper.createFileIfNotExist(fileName, rootPath, dirName);
        Logger.d("DownloadService", "saveVideo : documentFile : " + documentFile);
        if (documentFile == null || !FileHelper.writeFromFile(file, documentFile)) {
            task.status = STATUS_PAUSED;
            video.status = STATUS_WAITING;
            holder.updateDownloadTasks(task);
            Intent intent = new Intent(ON_FAILURE);
            intent.putExtra("message", "保存失败，请重新设置下载目录");
            sendBroadcast(intent);
            return;
        }
        Logger.d("DownloadService", "saveVideo : done : " + documentFile.getUri().toString());
        video.vlink = documentFile.getUri().toString();
        video.status = STATUS_DOWNLOADED;
        holder.updateDownloadTasks(task);
    }

    private void downloadNewPage(final DownloadTask task) {
        if (task.collection.pictures == null) {
            task.status = STATUS_PAUSED;
            holder.updateDownloadTasks(task);
            Intent intent = new Intent(ON_FAILURE);
            intent.putExtra("message", "图册中不含有任何图片");
            sendBroadcast(intent);
            return;
        }
        boolean isCompleted = true;
        Picture currPic = null;
        for (Picture picture : task.collection.pictures) {
            if (picture.status == STATUS_WAITING) {
                currPic = picture;
                currPic.status = STATUS_DOWNLOADING;
                isCompleted = false;
                break;
            } else if (picture.status == STATUS_DOWNLOADING) {
                isCompleted = false;
            }
        }

        if (currPic == null) {
            if (isCompleted) {
                task.status = STATUS_COMPLETED;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                task.collection.datetime = dateFormat.format(calendar.getTime());
                task.collection.gid = 0;
                holder.updateDownloadTasks(task);
                Intent intent = new Intent(ON_COMPLETE);
                sendBroadcast(intent);
                // 统计下载完成次数
                MobclickAgent.onEvent(mContext, "DownloadTaskCompleted");

                // 记录信息，以求恢复删除了的下载记录
                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                FileHelper.createFileIfNotExist("detail.txt", rootPath, dirName);
                FileHelper.writeString(HViewerApplication.getGson().toJson(task), "detail.txt", rootPath, dirName);
            }
            return;
        }
        final Picture picture = currPic;

        boolean highRes = downloadHighRes();
        if (!TextUtils.isEmpty(picture.highRes) && highRes) {
            picture.retries = 0;
            loadPicture(picture, task, true);
        } else if (!TextUtils.isEmpty(picture.pic) && !highRes) {
            picture.retries = 0;
            loadPicture(picture, task, false);
        } else if (task.collection.site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE)
                && task.collection.site.extraRule != null) {
            if (task.collection.site.extraRule.pictureRule != null && task.collection.site.extraRule.pictureRule.url != null)
                getPictureUrl(picture, task, task.collection.site.extraRule.pictureRule.url, task.collection.site.extraRule.pictureRule.highRes);
            else if (task.collection.site.extraRule.pictureUrl != null)
                getPictureUrl(picture, task, task.collection.site.extraRule.pictureUrl, task.collection.site.extraRule.pictureHighRes);
        } else if (task.collection.site.picUrlSelector != null) {
            getPictureUrl(picture, task, task.collection.site.picUrlSelector, null);
        } else {
            picture.pic = picture.url;
            picture.retries = 0;
            loadPicture(picture, task, false);
        }
    }

    private void getPictureUrl(final Picture picture, final DownloadTask task, final Selector selector, final Selector highResSelector) {
        Logger.d("DownloadService", picture.url);
        if (Picture.hasPicPosfix(picture.url)) {
            picture.pic = picture.url;
            loadPicture(picture, task, false);
        } else
            //如果需要执行JS才能获取完整数据，则不得不使用webView来载入页面
            if (task.collection.site.hasFlag(Site.FLAG_JS_NEEDED_ALL)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    WebView webView = new WebView(mContext);
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
                        if (result == null || result.equals("")) {
                            onFailure(null);
                        } else if (contentType.contains("image")) {
                            picture.pic = picture.url;
                            if (result instanceof Bitmap)
                                savePicture(picture, task, result);
                            else
                                loadPicture(picture, task, false);
                        } else {
                            picture.pic = RuleParser.getPictureUrl((String) result, selector, picture.url);
                            picture.highRes = RuleParser.getPictureUrl((String) result, highResSelector, picture.url);
                            if (!TextUtils.isEmpty(picture.highRes) && downloadHighRes()) {
                                picture.retries = 0;
                                picture.referer = picture.url;
                                loadPicture(picture, task, true);
                            } else if (!TextUtils.isEmpty(picture.pic)) {
                                picture.retries = 0;
                                picture.referer = picture.url;
                                loadPicture(picture, task, false);
                            } else {
                                onFailure(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(HViewerHttpClient.HttpError error) {
                        task.status = STATUS_PAUSED;
                        picture.status = STATUS_WAITING;
                        holder.updateDownloadTasks(task);
                        Intent intent = new Intent(ON_FAILURE);
                        intent.putExtra("message", "图片地址获取失败，请检查网络连接");
                        Logger.d("DownloadService", "picture.url : " + picture.url);
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
            loadPicture(picture, currTask, true);
        } else if (!TextUtils.isEmpty(picture.pic)) {
            picture.retries = 0;
            picture.referer = picture.url;
            loadPicture(picture, currTask, false);
        } else {
            currTask.status = STATUS_PAUSED;
            picture.status = STATUS_WAITING;
            holder.updateDownloadTasks(currTask);
            Intent intent = new Intent(ON_FAILURE);
            intent.putExtra("message", "图片地址获取失败，请检查网络连接");
            Logger.d("DownloadService", "apiUrl : " + picture.url);
            sendBroadcast(intent);
        }
    }

    private void loadPicture(final Picture picture, final DownloadTask task, final boolean highRes) {
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
                            int delay = 1000 * picture.retries;
                            picture.retries++;
                            picture.status = STATUS_DOWNLOADING;
                            new Handler().postDelayed(() -> loadPicture(picture, task, highRes), delay);
                        } else {
                            picture.retries = 0;
                            task.status = STATUS_PAUSED;
                            picture.status = STATUS_WAITING;
                            holder.updateDownloadTasks(task);
                            Intent intent = new Intent(ON_FAILURE);
                            intent.putExtra("message", "图片下载失败，也许您需要代理");
                            sendBroadcast(intent);
                        }
                    }
                });
    }

    private void savePicture(Picture picture, DownloadTask task, Object pic) {
        try {
            DocumentFile documentFile;
            if (pic instanceof Bitmap) {
                String fileName = getFileName(picture.pid, task.collection.pictures.size());
                fileName += ".jpg";
                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                documentFile = FileHelper.createFileIfNotExist(fileName, rootPath, dirName);
                FileHelper.saveBitmapToFile((Bitmap) pic, documentFile);
            } else if (pic instanceof PooledByteBuffer) {
                PooledByteBuffer buffer = (PooledByteBuffer) pic;
                byte[] bytes = new byte[buffer.size()];
                buffer.read(0, bytes, 0, buffer.size());
                String fileName = getFileName(picture.pid, task.collection.pictures.size());
                String postfix = FileType.getFileType(bytes, FileType.TYPE_IMAGE);
                fileName += "." + postfix;
                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                documentFile = FileHelper.createFileIfNotExist(fileName, rootPath, dirName);
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
            picture.status = STATUS_DOWNLOADED;
            Intent intent = new Intent(ON_PROGRESS);
            sendBroadcast(intent);
            if (task.status != STATUS_PAUSED && task.status != STATUS_COMPLETED) {
                downloadNewPage(task);
            }

            //Log.d("DownloadManager", "picture.pid = " + picture.pid);
        } catch (IOException e) {
            e.printStackTrace();
            task.status = STATUS_PAUSED;
            picture.status = STATUS_WAITING;
            holder.updateDownloadTasks(task);
            Intent intent = new Intent(ON_FAILURE);
            intent.putExtra("message", "保存失败，请重新设置下载目录");
            sendBroadcast(intent);
        } catch (OutOfMemoryError error) {
            // 这里就算OOM了，就当作下载失败，不影响程序继续运行
        }
    }

    private String getFileName(int id, int size) {
        String fileName;
        if (size >= 1000) {
            fileName = String.format("%04d", id);
        } else if (size >= 100) {
            fileName = String.format("%03d", id);
        } else if (size >= 10) {
            fileName = String.format("%02d", id);
        } else {
            fileName = id + "";
        }
        return fileName;
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

        public void restart(DownloadTask task) {
            DownloadService.this.restart(task);
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
