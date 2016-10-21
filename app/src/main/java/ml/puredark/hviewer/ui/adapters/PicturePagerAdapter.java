package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.customs.AreaClickHelper;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;
import ml.puredark.hviewer.utils.RegexValidateUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_LEFT_TO_RIGHT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_RIGHT_TO_LEFT;

/**
 * Created by PureDark on 2016/10/5.
 */


public class PicturePagerAdapter extends PagerAdapter {

    private String viewDirection = DIREACTION_LEFT_TO_RIGHT;

    private BaseActivity activity;

    private Site site;

    public List<Picture> pictures;
    private List<PictureViewHolder> viewHolders = new ArrayList<>();

    private OnItemLongClickListener mOnItemLongClickListener;
    private boolean firstTime = true;

    private AreaClickHelper areaClickHelper;

    public PicturePagerAdapter(BaseActivity activity, Site site, List<Picture> pictures) {
        this.activity = activity;
        this.site = site;
        this.pictures = pictures;
        for (int i = 0; i < getCount(); i++)
            viewHolders.add(null);
        areaClickHelper = new AreaClickHelper(activity);
    }

    public class PictureViewHolder {
        View view;
        @BindView(R.id.iv_picture)
        PhotoDraweeView ivPicture;
        @BindView(R.id.progress_bar)
        ProgressBarCircularIndeterminate progressBar;
        @BindView(R.id.btn_refresh)
        ImageView btnRefresh;

        public PictureViewHolder(View view) {
            ButterKnife.bind(this, view);
            this.view = view;
            ivPicture.setOrientation(LinearLayout.HORIZONTAL);
        }
    }

    public void setViewDirection(String viewDirection) {
        this.viewDirection = viewDirection;
    }

    public String getViewDirection() {
        return viewDirection;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setAreaClickListener(AreaClickHelper.OnAreaClickListener onAreaClickListener) {
        areaClickHelper.setAreaClickListener(onAreaClickListener);
    }

    @Override
    public int getItemPosition(Object object) {
        if (firstTime)
            return POSITION_NONE;
        return super.getItemPosition(object);
    }

    public int getPicturePostion(int position) {
        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection)) {
            return position;
        } else if (DIREACTION_RIGHT_TO_LEFT.equals(viewDirection)) {
            return getCount() - 1 - position;
        }
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        if (getCount() > viewHolders.size()) {
            int size = getCount() - viewHolders.size();
            for (int i = 0; i < size; i++)
                viewHolders.add(null);
        }
        if (firstTime)
            new Handler().postDelayed(() -> firstTime = false, 500);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (pictures == null)
            return 1;
        if (pictures.size() == 0)
            return 1;
        return pictures.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (viewHolders.size() > position && viewHolders.get(position) != null) {
            if (viewHolders.get(position).view != null)
                container.removeView(viewHolders.get(position).view);
            viewHolders.set(position, null);
        }
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.view_picture_viewer, null);
        final PictureViewHolder viewHolder = new PictureViewHolder(view);

        if (pictures != null && position < pictures.size()) {
            final Picture picture = pictures.get(getPicturePostion(position));
            if (picture.pic != null) {
                loadImage(container.getContext(), picture, viewHolder);
            } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null && site.extraRule.pictureUrl != null) {
                getPictureUrl(container.getContext(), viewHolder, picture, site, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
            } else if (site.picUrlSelector != null) {
                getPictureUrl(container.getContext(), viewHolder, picture, site, site.picUrlSelector, null);
            } else {
                picture.pic = picture.url;
                loadImage(container.getContext(), picture, viewHolder);
            }
            viewHolder.btnRefresh.setOnClickListener(v -> {
                if (picture.pic != null) {
                    loadImage(container.getContext(), picture, viewHolder);
                } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null && site.extraRule.pictureUrl != null) {
                    getPictureUrl(container.getContext(), viewHolder, picture, site, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
                } else if (site.picUrlSelector == null) {
                    picture.pic = picture.url;
                    loadImage(container.getContext(), picture, viewHolder);
                } else {
                    getPictureUrl(container.getContext(), viewHolder, picture, site, site.picUrlSelector, null);
                }
            });
            if (mOnItemLongClickListener != null)
                viewHolder.ivPicture.setOnLongClickListener(v -> mOnItemLongClickListener.onItemLongClick(v, getPicturePostion(position)));
            viewHolder.ivPicture.setOnViewTapListener((v, x, y) -> {
                if (viewHolder.ivPicture.getScale() <= 1) {
                    areaClickHelper.onClick(x, y);
                }
            });
        }
        viewHolders.set(position, viewHolder);
        container.addView(viewHolder.view, 0);
        return viewHolder.view;
    }

    public boolean viewHighRes() {
        return (boolean) SharedPreferencesUtil.getData(activity,
                SettingFragment.KEY_PREF_VIEW_HIGH_RES, false);
    }


    private void loadImage(Context context, Picture picture, final PictureViewHolder viewHolder) {
        String url = (viewHighRes() && !TextUtils.isEmpty(picture.highRes)) ? picture.highRes : picture.pic;
        if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE))
            picture.referer = RegexValidateUtil.getHostFromUrl(site.galleryUrl);
        Logger.d("PicturePagerAdapter", "url:" + url + "\n picture.referer:" + picture.referer);
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
                viewHolder.ivPicture.update(imageInfo.getWidth(), imageInfo.getHeight());
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

    private Map<Integer, Pair<Picture, PictureViewHolder>> pictureInQueue = new HashMap<>();

    private void getPictureUrl(final Context context, final PictureViewHolder viewHolder, final Picture picture, final Site site, final Selector selector, final Selector highResSelector) {
        Logger.d("PicturePagerAdapter", "picture.url = " + picture.url);
        if (Picture.hasPicPosfix(picture.url)) {
            picture.pic = picture.url;
            loadImage(context, picture, viewHolder);
        } else
            //如果需要执行JS才能获取完整数据，则不得不使用webView来载入页面
            if (site.hasFlag(Site.FLAG_JS_NEEDED_ALL) || site.hasFlag(Site.FLAG_JS_NEEDED_PICTURE)) {
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
                        Logger.d("PicturePagerAdapter", "onPageFinished");
                    }
                });
                webView.loadUrl(picture.url);
                new Handler().postDelayed(() -> webView.stopLoading(), 30000);
                Logger.d("PicturePagerAdapter", "WebView");
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
                            Logger.d("PicturePagerAdapter", "getPictureUrl: picture.pic: " + picture.pic);
                            Logger.d("PicturePagerAdapter", "getPictureUrl: picture.highRes: " + picture.highRes);
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
        Pair<Picture, PictureViewHolder> pair = pictureInQueue.get(pid);
        if (pair == null)
            return;
        Log.d("PicturePagerAdapter", html);
        Picture picture = pair.first;
        PictureViewHolder viewHolder = pair.second;
        if (picture == null || viewHolder == null)
            return;
        pictureInQueue.remove(pid);
        Selector selector = (extra) ? site.extraRule.pictureUrl : site.picUrlSelector;
        Selector highResSelector = (extra) ? site.extraRule.pictureHighRes : null;
        picture.pic = RuleParser.getPictureUrl(html, selector, picture.url);
        picture.highRes = RuleParser.getPictureUrl(html, highResSelector, picture.url);
        Logger.d("PicturePagerAdapter", "getPictureUrl: picture.pic: " + picture.pic);
        Logger.d("PicturePagerAdapter", "getPictureUrl: picture.highRes: " + picture.highRes);
        if (picture.pic != null) {
            picture.retries = 0;
            picture.referer = picture.url;
            new Handler(Looper.getMainLooper()).post(() -> loadImage(activity, picture, viewHolder));
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
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