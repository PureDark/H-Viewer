package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static ml.puredark.hviewer.R.id.container;

public class PictureViewerAdapter extends RecyclerView.Adapter<PictureViewerAdapter.PictureViewerViewHolder> {
    private BaseActivity activity;
    private Site site;
    private ListDataProvider mProvider;
    private OnItemLongClickListener mOnItemLongClickListener;

    public PictureViewerAdapter(BaseActivity activity, Site site, ListDataProvider provider) {
        setHasStableIds(true);
        this.activity = activity;
        this.site = site;
        this.mProvider = provider;
    }

    @Override
    public PictureViewerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_picture_viewer, parent, false);
        // 在这里对View的参数进行设置
        PictureViewerViewHolder vh = new PictureViewerViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PictureViewerViewHolder viewHolder, int position) {
        Picture picture = (Picture) mProvider.getItem(position);
        if (picture.pic != null) {
            loadImage(activity, picture, viewHolder);
        } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null) {
            if(site.extraRule.pictureRule != null && site.extraRule.pictureRule.url != null)
                getPictureUrl(activity, viewHolder, picture, site, site.extraRule.pictureRule.url, site.extraRule.pictureRule.highRes);
            else if(site.extraRule.pictureUrl != null)
                getPictureUrl(activity, viewHolder, picture, site, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
        } else if (site.picUrlSelector != null) {
            getPictureUrl(activity, viewHolder, picture, site, site.picUrlSelector, null);
        } else {
            picture.pic = picture.url;
            loadImage(activity, picture, viewHolder);
        }
        viewHolder.btnRefresh.setOnClickListener(v -> {
            if (picture.pic != null) {
                loadImage(activity, picture, viewHolder);
            } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null) {
                if(site.extraRule.pictureRule != null && site.extraRule.pictureRule.url != null)
                    getPictureUrl(activity, viewHolder, picture, site, site.extraRule.pictureRule.url, site.extraRule.pictureRule.highRes);
                else if(site.extraRule.pictureUrl != null)
                    getPictureUrl(activity, viewHolder, picture, site, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
            } else if (site.picUrlSelector == null) {
                picture.pic = picture.url;
                loadImage(activity, picture, viewHolder);
            } else {
                getPictureUrl(activity, viewHolder, picture, site, site.picUrlSelector, null);
            }
        });
        viewHolder.ivPicture.setOnLongClickListener(v -> {
            if (mOnItemLongClickListener != null)
                return mOnItemLongClickListener.onItemLongClick(v, position);
            else
                return false;
        });
    }

    @Override
    public int getItemCount() {
        return (mProvider == null) ? 0 : mProvider.getCount();
    }

    @Override
    public long getItemId(int position) {
        return (mProvider == null) ? 0 : mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public class PictureViewerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_picture)
        PhotoDraweeView ivPicture;
        @BindView(R.id.progress_bar)
        ProgressBarCircularIndeterminate progressBar;
        @BindView(R.id.btn_refresh)
        ImageView btnRefresh;

        public PictureViewerViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            ivPicture.setOrientation(LinearLayout.VERTICAL);
        }
    }

    public boolean viewHighRes() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingFragment.KEY_PREF_VIEW_HIGH_RES, false);
    }

    private void loadImage(Context context, Picture picture, final PictureViewerViewHolder viewHolder) {
        String url = (viewHighRes() && !TextUtils.isEmpty(picture.highRes)) ? picture.highRes : picture.pic;
        Logger.d("PictureViewerAdapter", "url = " + url);
        if (site == null) return;
        ImageLoader.loadImageFromUrl(context, viewHolder.ivPicture, url, site.cookie, picture.referer, new BaseControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
                super.onSubmit(id, callerContext);
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.btnRefresh.setVisibility(View.GONE);
            }

            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                super.onFinalImageSet(id, imageInfo, anim);
                if (imageInfo == null) {
                    return;
                }
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.btnRefresh.setVisibility(View.GONE);
                WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                final float factor = (float) imageInfo.getHeight() / imageInfo.getWidth();
                final int originWidth = wm.getDefaultDisplay().getWidth();
                final int originHeight = (int) (factor * originWidth);
                viewHolder.ivPicture.getLayoutParams().height = originHeight;
                viewHolder.ivPicture.requestLayout();
                viewHolder.ivPicture.update(imageInfo.getWidth(), imageInfo.getHeight());

                viewHolder.ivPicture.setOnScaleChangeListener((scaleFactor, focusX, focusY) -> {
                    float scale = viewHolder.ivPicture.getScale();
                    if(scale>1) {
                        viewHolder.ivPicture.getLayoutParams().height = (int) (scale * originHeight) + 1;
                    }else{
                        viewHolder.ivPicture.getLayoutParams().height = originHeight;
                    }
                    viewHolder.ivPicture.requestLayout();
                });
                viewHolder.ivPicture.setAllowParentInterceptOnEdge(true);
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                FLog.e(getClass(), throwable, "Error loading %s", id);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.btnRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    private Map<Integer, Pair<Picture, PictureViewerViewHolder>> pictureInQueue = new HashMap<>();

    private void getPictureUrl(final Context context, final PictureViewerViewHolder viewHolder, final Picture picture, final Site site, final Selector selector, final Selector highResSelector) {
        Logger.d("PictureViewerAdapter", "picture.url = " + picture.url);
        if (Picture.hasPicPosfix(picture.url)) {
            picture.pic = picture.url;
            loadImage(context, picture, viewHolder);
        } else
            //如果需要执行JS才能获取完整数据，则不得不使用webView来载入页面
            if (site.hasFlag(Site.FLAG_JS_NEEDED_ALL)) {
                WebView webView = new WebView(context);
                WebSettings mWebSettings = webView.getSettings();
                mWebSettings.setJavaScriptEnabled(true);
                mWebSettings.setBlockNetworkImage(true);
                mWebSettings.setDomStorageEnabled(true);
                mWebSettings.setUserAgentString(context.getResources().getString(R.string.UA));
                mWebSettings.setCacheMode(LOAD_CACHE_ELSE_NETWORK);
                webView.addJavascriptInterface(this, "HtmlParser");

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        //Load HTML
                        pictureInQueue.put(picture.pid, new Pair<>(picture, viewHolder));
                        boolean extra = !selector.equals(site.picUrlSelector);
                        webView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, " + picture.pid + ", " + extra + ");");
                        Logger.d("PictureViewerAdapter", "onPageFinished");
                    }
                });
                webView.loadUrl(picture.url);
                new Handler().postDelayed(() -> webView.stopLoading(), 30000);
                Logger.d("PictureViewerAdapter", "WebView");
            } else
                HViewerHttpClient.get(picture.url, site.getCookies(), new HViewerHttpClient.OnResponseListener() {

                    @Override
                    public void onSuccess(String contentType, Object result) {
                        if (result == null || result.equals(""))
                            return;
                        if (contentType.contains("image")) {
                            picture.pic = picture.url;
                            if (result instanceof Bitmap) {
                                viewHolder.ivPicture.setImageBitmap((Bitmap) result);
                                viewHolder.progressBar.setVisibility(View.GONE);
                            } else {
                                loadImage(context, picture, viewHolder);
                            }
                        } else {
                            picture.pic = RuleParser.getPictureUrl((String) result, selector, picture.url);
                            picture.highRes = RuleParser.getPictureUrl((String) result, highResSelector, picture.url);
                            Logger.d("PictureViewerAdapter", "getPictureUrl: picture.pic: " + picture.pic);
                            Logger.d("PictureViewerAdapter", "getPictureUrl: picture.highRes: " + picture.highRes);
                            if (picture.pic != null) {
                                picture.retries = 0;
                                picture.referer = picture.url;
                                loadImage(context, picture, viewHolder);
                            } else {
                                onFailure(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(HViewerHttpClient.HttpError error) {
                        if (picture.retries < 15) {
                            picture.retries++;
                            getPictureUrl(context, viewHolder, picture, site, selector, highResSelector);
                        } else {
                            picture.retries = 0;
                            viewHolder.progressBar.setVisibility(View.GONE);
                            viewHolder.btnRefresh.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    @JavascriptInterface
    public void onResultGot(String html, int pid, boolean extra) {
        Pair<Picture, PictureViewerViewHolder> pair = pictureInQueue.get(pid);
        if (pair == null)
            return;
        Picture picture = pair.first;
        PictureViewerViewHolder viewHolder = pair.second;
        if (picture == null || viewHolder == null)
            return;
        pictureInQueue.remove(pid);
        Selector selector = (extra) ? site.extraRule.pictureUrl : site.picUrlSelector;
        Selector highResSelector = (extra) ? site.extraRule.pictureHighRes : null;
        picture.pic = RuleParser.getPictureUrl(html, selector, picture.url);
        picture.highRes = RuleParser.getPictureUrl(html, highResSelector, picture.url);
        Logger.d("PictureViewerAdapter", "getPictureUrl: picture.pic: " + picture.pic);
        Logger.d("PictureViewerAdapter", "getPictureUrl: picture.highRes: " + picture.highRes);
        if (picture.pic != null) {
            picture.retries = 0;
            picture.referer = picture.url;
            new Handler(Looper.getMainLooper()).post(()->loadImage(activity, picture, viewHolder));
        } else {
            new Handler(Looper.getMainLooper()).post(()->{
                if (picture.retries < 15) {
                    picture.retries++;
                    getPictureUrl(activity, viewHolder, picture, site, selector, highResSelector);
                } else {
                    picture.retries = 0;
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.btnRefresh.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}