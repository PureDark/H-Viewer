package ml.puredark.hviewer.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;

import com.umeng.analytics.MobclickAgent;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.dataholders.SiteTagHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.activities.CollectionActivity;
import ml.puredark.hviewer.ui.adapters.CollectionAdapter;
import ml.puredark.hviewer.ui.customs.AutoFitGridLayoutManager;
import ml.puredark.hviewer.ui.customs.WrappedLinearLayoutManager;
import ml.puredark.hviewer.ui.customs.WrappedStaggeredGridLayoutManager;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.DensityUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

public class CollectionFragment extends MyFragment {

    @BindView(R.id.rv_collection)
    PullLoadMoreRecyclerView rvCollection;

    CollectionAdapter adapter;

    private WebView mWebView;

    private RecyclerView.LayoutManager mLinearLayoutManager, mGridLayoutManager;

    private BaseActivity activity;
    private Site site;
    private SiteTagHolder siteTagHolder;

    private String currUrl = null;
    private String keyword = null;

    private int clickPos = 0;

    private boolean onePage = false;
    private int startPage;
    private int currPage;

    private boolean noTouch = false;
    // 记录使用js进行scroll的次数，每5秒检测一次，如果没有获得新item，则+1，达到3次判定已获取完毕
    private int scrollTimes = 0;

    public CollectionFragment() {
    }

    public static CollectionFragment newInstance(Site site, SiteTagHolder siteTagHolder) {
        CollectionFragment fragment = new CollectionFragment();
        fragment.site = site;
        fragment.siteTagHolder = siteTagHolder;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);
        ButterKnife.bind(this, rootView);
        if (getActivity() instanceof BaseActivity)
            activity = (BaseActivity) getActivity();

        if (site != null && site.hasFlag(Site.FLAG_WATERFALL_AS_LIST))
            mLinearLayoutManager = new WrappedStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        else
            mLinearLayoutManager = new WrappedLinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        if (site != null && site.hasFlag(Site.FLAG_WATERFALL_AS_GRID))
            mGridLayoutManager = new WrappedStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        else
            mGridLayoutManager = new AutoFitGridLayoutManager(this.getContext(), DensityUtil.dp2px(this.getContext(), 100));

        if (site != null && (site.hasFlag(Site.FLAG_JS_NEEDED_ALL) || site.hasFlag(Site.FLAG_JS_NEEDED_INDEX))) {
            mWebView = new WebView(getContext());
            WebSettings mWebSettings = mWebView.getSettings();
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setBlockNetworkImage(true);
            mWebSettings.setDomStorageEnabled(false);
            mWebSettings.setUseWideViewPort(true);
            mWebSettings.setLoadWithOverviewMode(true);
            mWebSettings.setUserAgentString(getResources().getString(R.string.UA));
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.addJavascriptInterface(this, "HtmlParser");
            ((ViewGroup) rootView.findViewById(R.id.content)).addView(mWebView);
            mWebView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWebView.requestLayout();
            mWebView.setVisibility(View.INVISIBLE);
        }

        List<Collection> collections = new ArrayList<>();
        ListDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(getContext(), dataProvider, siteTagHolder);
        rvCollection.setAdapter(adapter);

        Bundle bundle = getArguments();
        boolean isGrid = (bundle == null) ? false : bundle.getBoolean("isGrid", false);

        if (isGrid)
            setRecyclerViewToGrid();
        else
            setRecyclerViewToList();

        rvCollection.setPullRefreshEnable(true);
        rvCollection.getRecyclerView().setClipToPadding(false);
        rvCollection.getRecyclerView().setPadding(0, DensityUtil.dp2px(getActivity(), 8), 0, DensityUtil.dp2px(getActivity(), 8));
        rvCollection.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (activity == null || !activity.isInOneHandMode()) return;
                switch (newState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        activity.setDrawerEnabled(false);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        activity.setDrawerEnabled(true);
                        break;
                }
            }
        });

        rvCollection.getRecyclerView().setOnTouchListener((v, event) -> noTouch);

        //下拉刷新和加载更多
        rvCollection.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                currPage = startPage;
                rvCollection.setRefreshing(true);
                activity.setDrawerEnabled(true);
                getCollections(keyword, currPage);
            }

            @Override
            public void onLoadMore() {
                activity.setDrawerEnabled(true);
                getCollections(keyword, currPage + 1);
            }
        });

        if (site != null) {
            adapter.setSite(site);
        }
        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Collection collection = (Collection) adapter.getDataProvider().getItem(position);
                HViewerApplication.temp = site;
                HViewerApplication.temp2 = collection;
                Intent intent = new Intent(CollectionFragment.this.getContext(), CollectionActivity.class);
                startActivity(intent);
                clickPos = position;
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                return false;
            }
        });

        return rootView;
    }

    public void toggleWebView() {
        if (mWebView != null) {
            if (mWebView.getVisibility() == View.VISIBLE) {
                mWebView.setVisibility(View.INVISIBLE);
                activity.setDrawerEnabled(true);
            } else {
                mWebView.setVisibility(View.VISIBLE);
                activity.setDrawerEnabled(false);
            }
        }
    }

    private void getCollections(String keyword, final int page) {
        if (onePage && page > startPage && !site.hasFlag(Site.FLAG_JS_SCROLL)) {
            // 如果URL中根本没有page参数的位置，则肯定只有1页，无需多加载一次
            new Handler().postDelayed(() -> rvCollection.setPullLoadMoreCompleted(), 250);
            noTouch = false;
            return;
        }
        this.keyword = keyword;
        if (keyword == null)
            keyword = "";
        if (currUrl == null || site == null)
            return;
        final String url = site.getListUrl(currUrl, page, keyword, adapter.getDataProvider().getItems());
        Logger.d("CollectionFragment", url);
        //如果需要执行JS才能获取完整数据，则不得不使用webView来载入页面
        if (site.hasFlag(Site.FLAG_JS_NEEDED_ALL) || site.hasFlag(Site.FLAG_JS_NEEDED_INDEX)) {
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    //Load HTML
                    if(!TextUtils.isEmpty(site.indexRule.js)){
                        mWebView.loadUrl("javascript:"+site.indexRule.js);
                        new Handler().postDelayed(()->{
                            mWebView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, '" + url + "', " + page + ");");
                        },1000);
                    } else {
                        mWebView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, '" + url + "', " + page + ");");
                    }
                    Logger.d("CollectionFragment", "onPageFinished");
                }
            });
            if (site.hasFlag(Site.FLAG_JS_SCROLL) && page != startPage && mWebView.getUrl().equals(url)) {
                Logger.d("CollectionFragment", "FLAG_JS_SCROLL");
                mWebView.loadUrl("javascript:document.body.scrollTop = document.body.scrollHeight;");
                new Handler().postDelayed(() -> {
                    scrollTimes++;
                    if(!TextUtils.isEmpty(site.indexRule.js)){
                        mWebView.loadUrl("javascript:"+site.indexRule.js);
                        new Handler().postDelayed(()->{
                            mWebView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, '" + url + "', " + page + ");");
                        },1000);
                    } else {
                        mWebView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, '" + url + "', " + page + ");");
                    }
                    Logger.d("CollectionFragment", "onAjaxFinished");
                }, 5000);
            } else {
                mWebView.loadUrl(url);
                new Handler().postDelayed(() -> mWebView.stopLoading(), 30000);
            }
            Logger.d("CollectionFragment", "WebView");
        } else
            HViewerHttpClient.get(url, site.disableHProxy, site.getHeaders(), site.hasFlag(Site.FLAG_POST_INDEX) || site.hasFlag(Site.FLAG_POST_ALL),
                    new HViewerHttpClient.OnResponseListener() {
                        @Override
                        public void onSuccess(String contentType, final Object result) {
                            if (!(result instanceof String))
                                return;
                            String html = (String) result;
                            onResultGot(html, url, page);
                        }

                        @Override
                        public void onFailure(HViewerHttpClient.HttpError error) {
                            BaseActivity activity = (BaseActivity) getActivity();
                            if (activity != null)
                                activity.showSnackBar(error.getErrorString());
                            rvCollection.setPullLoadMoreCompleted();
                            noTouch = false;
                        }
                    });
    }

    @JavascriptInterface
    public void onResultGot(String html, String url, int page) {
        if (page == startPage) {
            int preSize = adapter.getItemCount();
            if (preSize > 0) {
                adapter.getDataProvider().clear();
                adapter.notifyItemRangeRemoved(0, preSize);
            }
        }
        noTouch = true;
        new Thread(() -> {
            if (HViewerApplication.DEBUG)
                FileHelper.writeString(html, "html.txt", DownloadManager.getDownloadPath());
            final Rule rule;

            rule = (keyword != null && site.searchRule != null && site.searchRule.item != null) ? site.searchRule : site.indexRule;

            List<Collection> newCollections = RuleParser.getCollections(new ArrayList<>(), html, rule, url);
            Logger.d("CollectionFragment", "newCollections.size():" + newCollections.size());

            List<Collection> collections = adapter.getDataProvider().getItems();
            Logger.d("CollectionFragment", "collections.size():" + collections.size());
            if (site.hasFlag(Site.FLAG_EXTRA_INDEX_INFO) && site.extraRule != null) {
                List<Collection> extraCollections = RuleParser.getCollections(new ArrayList<>(), html, site.extraRule, url);
                for (int i = 0; i < extraCollections.size() && i < newCollections.size(); i++) {
                    newCollections.get(i).fillEmpty(extraCollections.get(i));
                }
            }
            int oldSize = collections.size();
            Logger.d("CollectionFragment", "oldSize():" + oldSize);
            for (Collection newCollection : newCollections) {
                if (!TextUtils.isEmpty(newCollection.idCode) && !collections.contains(newCollection)) {
                    newCollection.cid = collections.size() + 1;
                    collections.add(newCollection);
                }
            }
            Logger.d("CollectionFragment", "newSize():" + collections.size());

            if (collections.size() > oldSize || scrollTimes == 3) {
                currPage = page;
                scrollTimes = 0;
                addSearchSuggestions(collections, oldSize);
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.notifyItemRangeInserted(oldSize, collections.size() - oldSize);
                    rvCollection.setPullLoadMoreCompleted();
                    noTouch = false;
                });
            } else if (site.hasFlag(Site.FLAG_JS_SCROLL) && mWebView != null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    scrollTimes++;
                    mWebView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, '" + url + "', " + page + ");");
                    Logger.d("CollectionFragment", "onAjaxFinished");
                }, 5000);
            } else {
                scrollTimes = 0;
                new Handler(Looper.getMainLooper()).post(() -> {
                    rvCollection.setPullLoadMoreCompleted();
                    noTouch = false;
                });
            }
        }).start();
    }

    private void addSearchSuggestions(List<Collection> collections, int start) {
        for (int i = start; i < collections.size(); i++) {
            Collection collection = collections.get(i);
            if (collection.tags != null) {
                for (Tag tag : collection.tags) {
                    HViewerApplication.searchSuggestionHolder.addSearchSuggestion(tag.title);
                    siteTagHolder.addTag(site.sid, tag);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (clickPos >= 0 && clickPos < adapter.getItemCount())
            adapter.notifyItemChanged(clickPos);
        if (site != null)
            MobclickAgent.onPageStart(site.title);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (clickPos >= 0 && clickPos < adapter.getItemCount())
            adapter.notifyItemChanged(clickPos);
        if (site != null)
            MobclickAgent.onPageEnd(site.title);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Site getCurrSite() {
        return site;
    }

    @Override
    public void onSearch(String keyword) {
        if (site == null || site.searchUrl == null || "".equals(site.searchUrl)) {
            BaseActivity activity = (BaseActivity) getActivity();
            if (activity != null)
                activity.showSnackBar("该站点不支持搜索，试试单选TAG");
            return;
        }
        try {
            keyword = URLEncoder.encode(keyword, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Logger.d("CollectionFragment", " keyword:" + keyword);
        currUrl = site.searchUrl;
        parseUrl(currUrl);
        rvCollection.setRefreshing(true);
        getCollections(keyword, startPage);
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

    @Override
    public void onLoadUrl(String url) {
        currUrl = url;
        if (rvCollection != null) {
            parseUrl(currUrl);
            rvCollection.setRefreshing(true);
            getCollections(null, startPage);
        } else {
            new Handler().postDelayed(() -> {
                if (rvCollection != null) {
                    parseUrl(currUrl);
                    rvCollection.setRefreshing(true);
                    getCollections(null, startPage);
                }
            }, 300);
        }
    }

    @Override
    public void onJumpToPage(int page) {
        rvCollection.setRefreshing(true);
        adapter.getDataProvider().clear();
        getCollections(keyword, page);
    }

    @Override
    public void setRecyclerViewToList() {
        adapter.setIsGrid(false);
        rvCollection.getRecyclerView().setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void setRecyclerViewToGrid() {
        adapter.setIsGrid(true);
        rvCollection.getRecyclerView().setLayoutManager(mGridLayoutManager);
    }
}
