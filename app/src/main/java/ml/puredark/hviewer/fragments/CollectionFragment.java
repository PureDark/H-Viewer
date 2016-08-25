package ml.puredark.hviewer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.activities.AnimationActivity;
import ml.puredark.hviewer.activities.CollectionActivity;
import ml.puredark.hviewer.adapters.CollectionAdapter;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.customs.AutoFitGridLayoutManager;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.RuleParser;
import ml.puredark.hviewer.utils.DensityUtil;

public class CollectionFragment extends MyFragment {

    @BindView(R.id.rv_collection)
    PullLoadMoreRecyclerView rvCollection;

    CollectionAdapter adapter;

    private RecyclerView.LayoutManager mLinearLayoutManager, mGridLayoutManager;

    private Site site;

    private String currUrl = null;
    private String keyword = null;
    private int startPage;
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
        AbstractDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(dataProvider);
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
                getCollections(keyword, currPage + 1);
            }
        });

        if (site != null) {
            adapter.setSite(site);
            Map<String, String> map = RuleParser.parseUrl(site.indexUrl);
            String pageStr = map.get("page");
            try {
                startPage = (pageStr != null) ? Integer.parseInt(pageStr) : 0;
                currPage = startPage;
            } catch (NumberFormatException e) {
                startPage = 0;
                currPage = startPage;
            }
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
        this.keyword = keyword;
        final Rule rule;
        if(keyword==null){
            rule = site.indexRule;
        }else{
            rule = (site.searchRule != null) ? site.searchRule : site.indexRule;
        }
        final String url = currUrl.replaceAll("\\{page:" + startPage + "\\}", "" + page)
                .replaceAll("\\{keyword:\\}", keyword);
        HViewerHttpClient.get(url, site.getCookies(), new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                if (page == startPage) {
                    adapter.getDataProvider().clear();
                }
                List<Collection> collections = adapter.getDataProvider().getItems();
                int oldSize = collections.size();
                collections = RuleParser.getCollections(collections, (String) result, rule, url);
                int newSize = collections.size();
                adapter.notifyDataSetChanged();
                if (newSize > oldSize) {
                    currPage = page;
                    addSearchSuggestions(collections);
                }
                rvCollection.setPullLoadMoreCompleted();
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                AnimationActivity activity = (AnimationActivity) getActivity();
                if (activity != null)
                    activity.showSnackBar(error.getErrorString());
                rvCollection.setPullLoadMoreCompleted();
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
        try {
            keyword = URLEncoder.encode(keyword, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        currUrl = site.searchUrl;
        rvCollection.setRefreshing(true);
        getCollections(keyword, startPage);
    }

    @Override
    public void onCategorySelected(Category category){
        currUrl = category.url;
        if(rvCollection!=null)
            rvCollection.setRefreshing(true);
        getCollections(null, startPage);
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
