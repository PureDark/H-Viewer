package ml.puredark.hviewer.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.umeng.analytics.MobclickAgent;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Comment;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.beans.Video;
import ml.puredark.hviewer.core.HtmlContentParser;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.dataholders.HistoryHolder;
import ml.puredark.hviewer.dataholders.SiteTagHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.adapters.CollectionTagAdapter;
import ml.puredark.hviewer.ui.adapters.CommentAdapter;
import ml.puredark.hviewer.ui.adapters.PictureVideoAdapter;
import ml.puredark.hviewer.ui.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.ui.customs.AutoFitStaggeredGridLayoutManager;
import ml.puredark.hviewer.ui.customs.ExTabLayout;
import ml.puredark.hviewer.ui.customs.ExViewPager;
import ml.puredark.hviewer.ui.customs.WrappedGridLayoutManager;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.SwipeBackOnPageChangeListener;
import ml.puredark.hviewer.utils.DensityUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;


public class CollectionActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    ExTabLayout tabLayout;
    @BindView(R.id.view_pager)
    ExViewPager viewPager;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.fab_menu)
    FloatingActionMenu fabMenu;
    @BindView(R.id.fab_download)
    FloatingActionButton fabDownload;

    private Site site;

    private Collection collection;
    private LocalCollection myCollection;

    private PullLoadMoreRecyclerView rvIndex;
    private RecyclerView rvComment;

    private PictureVideoAdapter pictureVideoAdapter;
    private CommentAdapter commentAdapter;

    private boolean flagSetNull = false;
    private PictureViewerActivity pictureViewerActivity;

    private CollectionViewHolder holder;

    private boolean onePic = false;
    private boolean onePage = false;
    private int startPage;
    private int currPage;

    private boolean isIndexComplete = false;
    private boolean refreshing = true;

    private WebView mWebView;

    private DownloadManager manager;

    private HistoryHolder historyHolder;
    private FavouriteHolder favouriteHolder;
    private SiteTagHolder siteTagHolder;

    private String currGalleryUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        ButterKnife.bind(this);
        MDStatusBarCompat.setCollapsingToolbar(this, coordinatorLayout, appBar, backdrop, toolbar);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
            lp.height = (int) (getResources().getDimension(R.dimen.tool_bar_height));
            lp.topMargin = MDStatusBarCompat.getStatusBarHeight(this);
            toolbar.setLayoutParams(lp);
        }

        setContainer(coordinatorLayout);

        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);
        setAppBar(appBar);
        setFabMenu(fabMenu);

        //获取传递过来的Collection实例
        if (HViewerApplication.temp instanceof Site)
            site = (Site) HViewerApplication.temp;
        if (HViewerApplication.temp2 instanceof Collection)
            collection = (Collection) HViewerApplication.temp2;

        //获取失败则结束此界面
        if (site == null || collection == null) {
            finish();
            return;
        }
        myCollection = new LocalCollection(collection, site);

        currGalleryUrl = site.galleryUrl;

        toolbar.setTitle(collection.title);
        setSupportActionBar(toolbar);

        manager = new DownloadManager(this);

        //解析URL模板
        parseUrl(site.galleryUrl);

        onePic = (boolean) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_ONE_PIC_GALLERY, false);
        onePage &= !site.hasFlag(Site.FLAG_SECOND_LEVEL_GALLERY);

        initCover(myCollection.cover);
        initTabAndViewPager();
        refreshDescription(site.galleryUrl);
        rvIndex.setRefreshing(true);

        if (site != null && (site.hasFlag(Site.FLAG_JS_NEEDED_ALL) || site.hasFlag(Site.FLAG_JS_NEEDED_GALLERY))) {
            mWebView = new WebView(this);
            WebSettings mWebSettings = mWebView.getSettings();
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setBlockNetworkImage(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setUserAgentString(getResources().getString(R.string.UA));
            mWebSettings.setCacheMode(LOAD_CACHE_ELSE_NETWORK);
            mWebSettings.setAllowUniversalAccessFromFileURLs(true);
            mWebView.addJavascriptInterface(this, "HtmlParser");
            coordinatorLayout.addView(mWebView);
            mWebView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.requestLayout();
            mWebView.setVisibility(View.INVISIBLE);
        }
        getCollectionDetail(startPage);

        historyHolder = new HistoryHolder(this);
        favouriteHolder = new FavouriteHolder(this);
        siteTagHolder = new SiteTagHolder(this);
        //加入历史记录
        historyHolder.addHistory(myCollection);

        if (onePic && site.hasFlag(Site.FLAG_ONE_PIC_GALLERY)) {
            openPictureViewerActivity(0);
        }
    }

    private void parseUrl(String url) {
        String pageStr = RuleParser.parseUrl(url).get("page");
        try {
            if (pageStr == null) {
                onePage = true;
                startPage = 0;
            } else {
                onePage = false;
                String[] pageStrs = pageStr.split(":");
                if (pageStrs.length > 1) {
                    startPage = Integer.parseInt(pageStrs[0]);
                } else {
                    startPage = Integer.parseInt(pageStr);
                }
            }
            currPage = startPage;
        } catch (NumberFormatException e) {
            startPage = 0;
            currPage = startPage;
        }
    }

    private void initCover(String cover) {
        if (cover != null) {
            ImageLoader.loadImageFromUrl(this, backdrop, cover, site.cookie, collection.referer);
        }
    }

    private void initTabAndViewPager() {
        //初始化Tab和ViewPager
        List<View> views = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        titles.add("目录");
        View viewIndex = getLayoutInflater().inflate(R.layout.view_collection_index, null);
        views.add(viewIndex);
        titles.add("详情");
        View viewDescription = getLayoutInflater().inflate(R.layout.view_collection_desciption, null);
        views.add(viewDescription);
        View viewComment = null;
        if (commentEnabled()) {
            titles.add("评论");
            viewComment = getLayoutInflater().inflate(R.layout.view_collection_comment, null);
            views.add(viewComment);
        }


        holder = new CollectionViewHolder(viewDescription);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views, titles);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        viewPager.addOnPageChangeListener(new SwipeBackOnPageChangeListener(this));

        //初始化相册目录
        rvIndex = (PullLoadMoreRecyclerView) viewIndex.findViewById(R.id.rv_index);
        List<Picture> pictures = new ArrayList<>();
        List<Video> videos = new ArrayList<>();
        if (collection.pictures != null)
            pictures.addAll(collection.pictures);
        if (collection.videos != null)
            videos.addAll(collection.videos);
        pictureVideoAdapter = new PictureVideoAdapter(this, new ListDataProvider(pictures), new ListDataProvider(videos));
        pictureVideoAdapter.setCookie(site.cookie);
        pictureVideoAdapter.setRepeatedThumbnail(site.hasFlag(Site.FLAG_REPEATED_THUMBNAIL));
        rvIndex.setAdapter(pictureVideoAdapter);

        rvIndex.getRecyclerView().addOnScrollListener(new PictureVideoAdapter.ScrollDetector() {
            @Override
            public void onScrollUp() {
                fabMenu.hideMenu(true);
            }

            @Override
            public void onScrollDown() {
                fabMenu.showMenu(true);
            }
        });

        rvIndex.getRecyclerView().setClipToPadding(false);
        rvIndex.getRecyclerView().setPadding(
                DensityUtil.dp2px(this, 8),
                DensityUtil.dp2px(this, 16),
                DensityUtil.dp2px(this, 8),
                DensityUtil.dp2px(this, 16));

        pictureVideoAdapter.setOnItemClickListener((v, position) -> {
            if (position < pictureVideoAdapter.getPictureSize())
                openPictureViewerActivity(position);
            else
                openVideoViewerActivity(position);
        });

        //根据item宽度自动设置spanCount
        GridLayoutManager layoutManager = new WrappedGridLayoutManager(this, 6);
        rvIndex.getRecyclerView().setLayoutManager(layoutManager);
        pictureVideoAdapter.setLayoutManager(layoutManager);
        rvIndex.setPullRefreshEnable(true);
        rvIndex.setPushRefreshEnable(false);

        //下拉刷新和加载更多
        rvIndex.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                currPage = startPage;
                getCollectionDetail(currPage);
            }

            @Override
            public void onLoadMore() {
            }
        });

        if (viewComment != null) {
            //初始化评论列表
            rvComment = (RecyclerView) viewComment.findViewById(R.id.rv_comment);
            List<Comment> comments = new ArrayList<>();
            if (collection.comments != null)
                comments.addAll(collection.comments);
            commentAdapter = new CommentAdapter(this, new ListDataProvider(comments));
            commentAdapter.setCookie(site.cookie);
            rvComment.setAdapter(commentAdapter);

            //禁用下拉刷新和加载更多（暂时）
//            rvComment.setPullRefreshEnable(false);
//            rvComment.setPushRefreshEnable(false);
        }
    }

    private void openPictureViewerActivity(int position) {
        HViewerApplication.temp = this;
        HViewerApplication.temp2 = site;
        HViewerApplication.temp3 = collection;
        List<Picture> pictures = new ArrayList<>();
        pictures.addAll(pictureVideoAdapter.getPictureDataProvider().getItems());
        HViewerApplication.temp4 = pictures;
        Intent intent = new Intent(this, PictureViewerActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void openVideoViewerActivity(int position) {
        HViewerApplication.temp = pictureVideoAdapter.getVideoDataProvider().getItem(position - pictureVideoAdapter.getPictureSize());
        Intent intent = new Intent(this, VideoViewerActivity.class);
        startActivity(intent);
    }

    public void setPictureViewerActivity(PictureViewerActivity activity) {
        pictureViewerActivity = activity;
    }

    private boolean commentEnabled() {
        if (site.galleryRule == null)
            return false;
        else
            return (site.galleryRule.commentRule != null &&
                    site.galleryRule.commentRule.item != null &&
                    site.galleryRule.commentRule.author != null &&
                    site.galleryRule.commentRule.content != null)
                    || (site.galleryRule.commentItem != null &&
                    site.galleryRule.commentAuthor != null &&
                    site.galleryRule.commentContent != null);
    }

    private void refreshDescription(String url) {
        getSupportActionBar().setTitle(myCollection.title);
        holder.tvTitle.setText(myCollection.title);
        holder.tvUploader.setText(myCollection.uploader);
        holder.tvCategory.setText(myCollection.category);
        CollectionTagAdapter adapter = (CollectionTagAdapter) holder.rvTags.getAdapter();
        if (myCollection.tags != null) {
            int preSize = adapter.getItemCount();
            if (preSize > 0) {
                adapter.getDataProvider().clear();
                adapter.notifyItemRangeChanged(0, preSize);
            }
            adapter.getDataProvider().addAll(myCollection.tags);
            adapter.notifyItemRangeChanged(0, myCollection.tags.size());
            adapter.setOnItemClickListener((v, position) -> {
                if (myCollection.tags != null) {
                    Tag tag = adapter.getDataProvider().getItem(position);
                    Intent intent = new Intent(CollectionActivity.this, MainActivity.class);
                    intent.setAction("search");
                    intent.putExtra("tag", tag);
                    startActivity(intent);
                    this.finish();
                }
            });
        }
        holder.rbRating.setRating(myCollection.rating);
        Logger.d("CollectionActivity", "myCollection.rating:" + myCollection.rating);
        holder.tvSubmittime.setText(myCollection.datetime);
        if (myCollection.description != null)
            holder.tvDescription.setText(HtmlContentParser.getClickableHtml(this, myCollection.description, url, source -> new BitmapDrawable()));
        collection.title = myCollection.title;
        collection.uploader = myCollection.uploader;
        collection.category = myCollection.category;
        collection.tags = myCollection.tags;
        collection.rating = myCollection.rating;
        collection.datetime = myCollection.datetime;
        collection.description = myCollection.description;
//        if (myCollection.videos != null && myCollection.videos.size() > 0)
//            fabDownload.setVisibility(View.GONE);
    }

    private void getCollectionDetail(final int page) {
        if (site.galleryRule == null || (onePage && page > startPage) || (onePage && collection.pictures != null && collection.pictures.size() > 0)) {
            // 如果没有galleryRule，或者URL中根本没有page参数的位置，肯定只有1页，则不继续加载
            rvIndex.setPullLoadMoreCompleted();
            isIndexComplete = true;
            return;
        }
        Log.d("CollectionActivity", "myCollection.idCode:" + myCollection.idCode);
        final String url = site.getGalleryUrl(currGalleryUrl, myCollection.idCode, page, myCollection.pictures);
        Logger.d("CollectionActivity", "site.getGalleryUrl:" + url);
        Logger.d("CollectionActivity", "starPage:" + startPage + " page:" + page);
        //如果需要执行JS才能获取完整数据，则不得不使用webView来载入页面
        if (site.hasFlag(Site.FLAG_JS_NEEDED_ALL) || site.hasFlag(Site.FLAG_JS_NEEDED_GALLERY)) {
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    //Load HTML
                    if (site.hasFlag(Site.FLAG_IFRAME_GALLERY)) {
                        String js = "javascript:" +
                                "var iframes =document.querySelectorAll(\"iframe\");" +
                                "var host = window.location.protocol + \"//\" + window.location.host;" +
                                "for (i = 0; i < iframes.length; i++) {" +
                                "var iframe = iframes[i];" +
                                "if(iframe.src.startsWith(host))" +
                                "iframe.outerHTML = iframe.contentWindow.document.body.innerHTML;" +
                                "} ";
                        mWebView.loadUrl(js);
                        Logger.d("CollectionActivity", "FLAG_IFRAME_GALLERY");
                    }
                    if(!TextUtils.isEmpty(site.galleryRule.js)){
                        mWebView.loadUrl("javascript:"+site.galleryRule.js);
                        new Handler().postDelayed(()->{
                            mWebView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, '" + url + "', " + page + ");");
                        },1000);
                    } else {
                        mWebView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, '" + url + "', " + page + ");");
                    }
                    Logger.d("CollectionActivity", "onPageFinished");
                }
            });
            mWebView.loadUrl(url);
            new Handler().postDelayed(() -> mWebView.stopLoading(), 30000);
            Logger.d("CollectionActivity", "WebView");
        } else
            HViewerHttpClient.get(url, site.disableHProxy, site.getHeaders(), site.hasFlag(Site.FLAG_POST_GALLERY), new HViewerHttpClient.OnResponseListener() {
                @Override
                public void onSuccess(String contentType, final Object result) {
                    if (result == null) {
                        onFailure(new HViewerHttpClient.HttpError(HViewerHttpClient.HttpError.ERROR_NETWORK));
                        return;
                    }
                    Logger.d("CollectionActivity", "HViewerHttpClient");
                    String html = (String) result;
                    onResultGot(html, url, page);
                    if (flagSetNull)
                        new Handler().postDelayed(() -> pictureViewerActivity = null, 500);
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    refreshing = false;
                    showSnackBar(error.getErrorString());
                    rvIndex.setPullLoadMoreCompleted();
                    if (flagSetNull)
                        new Handler().postDelayed(() -> pictureViewerActivity = null, 500);
                }
            });
    }

    @JavascriptInterface
    public void onResultGot(String html, String url, int page) {
        new Handler(Looper.getMainLooper()).post(() -> {
            boolean flagNextPage = false, emptyPicture = false, emptyVideo = false;

            if (HViewerApplication.DEBUG)
                SimpleFileUtil.writeString("/sdcard/html.txt", html, "utf-8");
            Rule applyRule = (currGalleryUrl != null && currGalleryUrl.equals(site.galleryUrl)) ? site.galleryRule : site.extraRule;
            Logger.d("CollectionActivity", "applyRule:"+(applyRule.equals(site.galleryRule) ? "galleryRule" : "extraRule"));
            myCollection = new LocalCollection(RuleParser.getCollectionDetail(myCollection, html, applyRule, url), site);

            if (myCollection.videos != null && myCollection.videos.size() > 0) {
                Logger.d("CollectionActivity", "myCollection.videos.size():" + myCollection.videos.size());
                Logger.d("CollectionActivity", "myCollection.videos.get(0):" + myCollection.videos.get(0));
            } else {
                Logger.d("CollectionActivity", "myCollection.videos.size(): 0");
            }
            Logger.d("CollectionActivity", "myCollection.comments:" + ((myCollection.comments != null) ? myCollection.comments.size() : 0));

            if (myCollection.tags != null) {
                for (Tag tag : myCollection.tags) {
                    HViewerApplication.searchSuggestionHolder.addSearchSuggestion(tag.title);
                    siteTagHolder.addTag(site.sid, tag);
                }
            }

            /************
             * 图片处理
             ************/
            // 如果当前页获取到的图片数量不为0，则进行后续判断是否添加进图片目录中
            if (myCollection.pictures != null && myCollection.pictures.size() > 0) {
                // 当前页获取到的第一个图片
                final Picture picture = myCollection.pictures.get(0);
                Logger.d("CollectionActivity", "picture.url:"+picture.url);
                // 如果有FLAG_SECOND_LEVEL_GALLERY的特殊处理
                if (site.isFirstLoadSecondLevelGallery(myCollection.pictures)) {
                    Logger.d("CollectionActivity", "site.hasFlag(Site.FLAG_SECOND_LEVEL_GALLERY)");
                    currGalleryUrl = picture.url;
                    parseUrl(currGalleryUrl);
                    getCollectionDetail(currPage);
//                    HViewerHttpClient.get(picture.url, site.getHeaders(), new HViewerHttpClient.OnResponseListener() {
//                        @Override
//                        public void onSuccess(String contentType, Object result) {
//                            myCollection = RuleParser.getCollectionDetail(myCollection, (String) result, site.extraRule, picture.url);
//                            int preSize = pictureVideoAdapter.getPictureSize();
//                            if (preSize > 0) {
//                                pictureVideoAdapter.getPictureDataProvider().clear();
//                                pictureVideoAdapter.notifyItemRangeRemoved(0, preSize);
//                            }
//                            pictureVideoAdapter.getPictureDataProvider().addAll(myCollection.pictures);
//                            pictureVideoAdapter.notifyItemRangeInserted(0, myCollection.pictures.size());
//                            isIndexComplete = true;
//                            myCollection.pictures = pictureVideoAdapter.getPictureDataProvider().getItems();
//                        }
//
//                        @Override
//                        public void onFailure(HViewerHttpClient.HttpError error) {
//                            showSnackBar(error.getErrorString());
//                            rvIndex.setPullLoadMoreCompleted();
//                        }
//                    });
                } else {
                    // 没有flag的话
                    if (page == startPage) {
                        // 当前获取的是第一页，则清空原目录中所有图片，再添加当前获取到的所有图片进入目录中
                        int preSize = pictureVideoAdapter.getPictureSize();
                        if (preSize > 0) {
                            pictureVideoAdapter.getPictureDataProvider().clear();
                            pictureVideoAdapter.notifyItemRangeRemoved(0, preSize);
                        }
                        pictureVideoAdapter.getPictureDataProvider().addAll(myCollection.pictures);
                        pictureVideoAdapter.notifyItemRangeInserted(0, myCollection.pictures.size());
                        currPage = page;
                        refreshing = true;
                        flagNextPage = true;
                    } else if (!pictureVideoAdapter.getPictureDataProvider().getItems().contains(picture)) {
                        // 如果当前获取的不是第一页，且当前第一张图片不在于图片目录中，则添加当前获取到的所有图片到图片目录中
                        int currPid = pictureVideoAdapter.getItemCount() + 1;
                        for (int i = 0; i < myCollection.pictures.size(); i++) {
                            if (myCollection.pictures.get(i).pid < currPid + i)
                                myCollection.pictures.get(i).pid = currPid + i;
                        }
                        int preSize = pictureVideoAdapter.getPictureSize();
                        pictureVideoAdapter.getPictureDataProvider().addAll(myCollection.pictures);
                        pictureVideoAdapter.notifyItemRangeInserted(preSize, myCollection.pictures.size());
                        currPage = page;
                        refreshing = true;
                        flagNextPage = true;
                    } else {
                        // 如果当前获取的不是第一页，且当前第一张图片已存在于图片目录中，则判定此次获取到的图片数量为0
                        emptyPicture = true;
                    }
                }
            } else {
                // 获取到的图片数量为0
                emptyPicture = true;
            }

            /************
             * 视频处理
             ************/

            if (myCollection.videos != null && myCollection.videos.size() > 0) {
                final Video firstVideo = myCollection.videos.get(0);
                if (page == startPage) {
                    // 当前获取的是第一页，则清空原目录中所有视频，再添加当前获取到的所有视频进入目录中
                    int preSize = pictureVideoAdapter.getVideoSize();
                    if (preSize > 0) {
                        pictureVideoAdapter.getVideoDataProvider().clear();
                        pictureVideoAdapter.notifyItemRangeRemoved(pictureVideoAdapter.getPictureSize(), preSize);
                    }
                    pictureVideoAdapter.getVideoDataProvider().addAll(myCollection.videos);
                    pictureVideoAdapter.notifyItemRangeInserted(pictureVideoAdapter.getPictureSize(), myCollection.videos.size());
                    currPage = page;
                    refreshing = true;
                    flagNextPage = true;
                } else if (!pictureVideoAdapter.getVideoDataProvider().getItems().contains(firstVideo)) {
                    // 如果当前获取的不是第一页，且当前第一个视频不在于视频目录中，则添加当前获取到的所有视频到视频目录中
                    pictureVideoAdapter.getVideoDataProvider().addAll(myCollection.videos);
                    pictureVideoAdapter.notifyItemRangeInserted(pictureVideoAdapter.getPictureSize(), myCollection.videos.size());
                    currPage = page;
                    refreshing = true;
                    flagNextPage = true;
                } else {
                    // 如果当前获取的不是第一页，且当前第一个视频已存在于视频目录中，则判定此次获取到的视频数量为0
                    emptyVideo = true;
                }
            } else {
                // 获取到的视频数量为0
                emptyVideo = true;
            }

            /************
             * 评论处理
             ************/

            if (rvComment != null && commentAdapter != null && myCollection.comments != null && myCollection.comments.size() > 0) {
                // 当前页获取到的第一个评论
                final Comment firstComment = myCollection.comments.get(0);
                if (!commentAdapter.getDataProvider().getItems().contains(firstComment)) {
                    int preSize = commentAdapter.getItemCount();
                    commentAdapter.getDataProvider().addAll(myCollection.comments);
                    commentAdapter.notifyItemRangeInserted(preSize, myCollection.comments.size());
                }
            }

            if (emptyPicture && emptyVideo) {
                // 获取到的图片和视频数量都为0，则直接判定已达到末尾
                isIndexComplete = true;
                refreshing = false;
                myCollection.pictures = pictureVideoAdapter.getPictureDataProvider().getItems();
                myCollection.videos = pictureVideoAdapter.getVideoDataProvider().getItems();
                if (commentAdapter != null)
                    myCollection.comments = commentAdapter.getDataProvider().getItems();
            }
            boolean finalFlagNextPage = flagNextPage;
            if (!refreshing)
                rvIndex.setPullLoadMoreCompleted();
            refreshDescription(url);
            if (pictureViewerActivity != null) {
                List<Picture> pictures = new ArrayList<>();
                pictures.addAll(pictureVideoAdapter.getPictureDataProvider().getItems());
                pictureViewerActivity.notifyDataSetChanged(pictures);
            }
            if (finalFlagNextPage)
                getCollectionDetail(currPage + 1);
        });
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.fab_browser)
    void viewInBrowser() {
        final String url = site.getGalleryUrl(myCollection.idCode, startPage, pictureVideoAdapter.getPictureDataProvider().getItems());
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        if (content_url != null && !TextUtils.isEmpty(url)) {
            intent.setData(content_url);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                myClipboard.setPrimaryClip(ClipData.newPlainText("url", url));
                showSnackBar("没有可调用的浏览器，网址已复制到剪贴板");
            }
            // 统计打开浏览器访问次数
            MobclickAgent.onEvent(HViewerApplication.mContext, "SwitchToBrowser");
        } else {
            showSnackBar("网址为空！");
        }
    }

    @OnClick(R.id.fab_favor)
    void favor() {
        int cid = favouriteHolder.addFavourite(new LocalCollection(collection, site));
        if(cid>=0) {
            collection.cid = cid;
            myCollection.cid = cid;
            showSnackBar("收藏成功！");
        }else{
            showSnackBar("图册已收藏！");
        }
        // 统计收藏次数
        MobclickAgent.onEvent(HViewerApplication.mContext, "FavorCollection");
    }

    @OnClick(R.id.fab_download)
    void download() {
        if (isIndexComplete) {
            if (!manager.createDownloadTask(myCollection))
                showSnackBar("下载任务创建失败，请重新选择下载目录");
            else
                showSnackBar("下载任务已添加");
        } else {
            showSnackBar("请等待目录加载完毕再下载！");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pictureViewerActivity != null) {
            if (onePic && site.hasFlag(Site.FLAG_ONE_PIC_GALLERY))
                finish();
            else if (refreshing)
                flagSetNull = true;
            else
                pictureViewerActivity = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null)
            manager.unbindService(this);
        if (historyHolder != null)
            historyHolder.onDestroy();
        if (favouriteHolder != null)
            favouriteHolder.onDestroy();
        if (siteTagHolder != null)
            siteTagHolder.onDestroy();
        pictureViewerActivity = null;
    }

    public class CollectionViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_uploader)
        TextView tvUploader;
        @BindView(R.id.tv_category)
        TextView tvCategory;
        @BindView(R.id.rv_tags)
        RecyclerView rvTags;
        @BindView(R.id.rb_rating)
        RatingBar rbRating;
        @BindView(R.id.tv_submittime)
        TextView tvSubmittime;
        @BindView(R.id.tv_description)
        TextView tvDescription;

        public CollectionViewHolder(View view) {
            ButterKnife.bind(this, view);
            rvTags.setAdapter(
                    new CollectionTagAdapter(
                            new ListDataProvider<>(
                                    new ArrayList<Tag>()
                            )
                    )
            );
            StaggeredGridLayoutManager layoutManager =
                    new AutoFitStaggeredGridLayoutManager(CollectionActivity.this, OrientationHelper.HORIZONTAL);
            rvTags.setLayoutManager(layoutManager);
            tvDescription.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
            tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }
}
