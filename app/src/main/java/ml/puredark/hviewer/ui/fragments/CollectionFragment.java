package ml.puredark.hviewer.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.activities.CollectionActivity;
import ml.puredark.hviewer.ui.adapters.CollectionAdapter;
import ml.puredark.hviewer.ui.customs.AutoFitGridLayoutManager;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.DensityUtil;

public class CollectionFragment extends MyFragment {

    @BindView(R.id.rv_collection)
    PullLoadMoreRecyclerView rvCollection;

    CollectionAdapter adapter;

    private RecyclerView.LayoutManager mLinearLayoutManager, mGridLayoutManager;

    private Site site;

    private String currUrl = null;
    private String keyword = null;

    private boolean onePage = false;
    private int startPage;
    private int pageStep = 1;
    private int currPage;

    public CollectionFragment() {
    }

    public static CollectionFragment newInstance(Site site) {
        CollectionFragment fragment = new CollectionFragment();
        fragment.site = site;
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

        mLinearLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        mGridLayoutManager = new AutoFitGridLayoutManager(this.getContext(), DensityUtil.dp2px(this.getContext(), 100));

        List<Collection> collections = new ArrayList<>();
        ListDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(getContext(), dataProvider);
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

        //下拉刷新和加载更多
        rvCollection.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                keyword = null;
                currPage = startPage;
                rvCollection.setRefreshing(true);
                getCollections(keyword, currPage);
            }

            @Override
            public void onLoadMore() {
                getCollections(keyword, currPage + pageStep);
            }
        });

        if (site != null) {
            adapter.setSite(site);
            parseUrl(site.indexUrl);
            currUrl = site.indexUrl;
            rvCollection.setRefreshing(true);
            getCollections(null, startPage);
        }
        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Collection collection = (Collection) adapter.getDataProvider().getItem(position);
                HViewerApplication.temp = site;
                HViewerApplication.temp2 = collection;
                Intent intent = new Intent(CollectionFragment.this.getContext(), CollectionActivity.class);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                return false;
            }
        });

        return rootView;
    }

    private void getCollections(String keyword, final int page) {
        if (onePage && page > startPage) {
            // 如果URL中根本没有page参数的位置，则肯定只有1页，无需多加载一次
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rvCollection.setPullLoadMoreCompleted();
                }
            });
            return;
        }
        this.keyword = keyword;
        final Rule rule;
        if (keyword == null) {
            rule = site.indexRule;
            keyword = "";
        } else {
            rule = (site.searchRule != null) ? site.searchRule : site.indexRule;
        }
        if (currUrl == null || site == null)
            return;
        final String url = site.getListUrl(currUrl, page, keyword);
        Logger.d("CollectionFragment", url);
        HViewerHttpClient.get(url, site.getCookies(), new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, final Object result) {
                if (!(result instanceof String))
                    return;
                if (page == startPage) {
                    adapter.getDataProvider().clear();
                }
                String html = (String) result;
                List<Collection> collections = adapter.getDataProvider().getItems();
                int oldSize = collections.size();
                collections = RuleParser.getCollections(collections, html, rule, url);
                int newSize = collections.size();
                if (newSize > oldSize) {
                    currPage = page;
                    addSearchSuggestions(collections);
                }
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        rvCollection.setPullLoadMoreCompleted();
                    });
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                if (getActivity() != null) {
                    BaseActivity activity = (BaseActivity) getActivity();
                    if (activity != null)
                        activity.showSnackBar(error.getErrorString());
                    rvCollection.setPullLoadMoreCompleted();
                }
            }
        });
    }

    private void addSearchSuggestions(List<Collection> collections) {
        for (Collection collection : collections) {
            if (collection.tags != null) {
                for (Tag tag : collection.tags) {
                    HViewerApplication.searchSuggestionHolder.addSearchSuggestion(tag.title);
                }
            }
        }
        HViewerApplication.searchSuggestionHolder.removeDuplicate();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        if (site != null)
            MobclickAgent.onPageStart(site.title);
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.notifyDataSetChanged();
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
    public void onSearch(String keyword) {
        if (site == null || site.searchUrl == null || "".equals(site.searchUrl)) {
            BaseActivity activity = (BaseActivity) getActivity();
            if (activity != null)
                activity.showSnackBar("该站点不支持搜索");
            return;
        }
        try {
            keyword = URLEncoder.encode(keyword, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
                pageStep = 1;
            } else {
                onePage = false;
                String[] pageStrs = pageStr.split(":");
                if (pageStrs.length > 1) {
                    pageStep = Integer.parseInt(pageStrs[1]);
                    startPage = Integer.parseInt(pageStrs[0]);
                } else {
                    pageStep = 1;
                    startPage = Integer.parseInt(pageStr);
                }
            }
            currPage = startPage;
        } catch (NumberFormatException e) {
            startPage = 0;
            pageStep = 1;
            currPage = startPage;
        }
    }

    @Override
    public void onCategorySelected(Category category) {
        currUrl = category.url;
        if (rvCollection != null) {
            parseUrl(currUrl);
            rvCollection.setRefreshing(true);
            getCollections(null, startPage);
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (rvCollection != null) {
                        parseUrl(currUrl);
                        rvCollection.setRefreshing(true);
                        getCollections(null, startPage);
                    }
                }
            }, 300);
        }
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
