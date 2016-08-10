package ml.puredark.hviewer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.activities.CollectionActivity;
import ml.puredark.hviewer.adapters.CollectionAdapter;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.RuleParser;

import static ml.puredark.hviewer.helpers.RuleParser.parseUrl;
import static okhttp3.Protocol.get;

public class CollectionFragment extends MyFragment {

    @BindView(R.id.rv_collection)
    PullLoadMoreRecyclerView rvCollection;

    CollectionAdapter adapter;

    private Site site;

    private int startPage;
    private int currPage;

    public CollectionFragment() {
    }

    public static CollectionFragment newInstance() {
        CollectionFragment fragment = new CollectionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (HViewerApplication.temp instanceof Site)
            site = (Site) HViewerApplication.temp;


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);
        ButterKnife.bind(this, rootView);


        List<Collection> collections = new ArrayList<>();
        AbstractDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(dataProvider);
        rvCollection.setAdapter(adapter);

        rvCollection.setLinearLayout();
        rvCollection.setPullRefreshEnable(true);

        //下拉刷新和加载更多
        rvCollection.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                currPage=1;
                getCollections(currPage);
            }

            @Override
            public void onLoadMore() {
                getCollections(currPage+1);
            }
        });

        if (site != null) {
            Map<String, String> map = RuleParser.parseUrl(site.indexUrl);
            String pageStr = map.get("page");
            try {
                startPage = (pageStr != null) ? Integer.parseInt(pageStr) : 0;
                currPage = startPage;
            }catch (NumberFormatException e){
                startPage = 0;
                currPage = startPage;
            }
            getCollections(startPage);
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
        });

        return rootView;
    }

    private void getCollections(final int page) {
        String url = site.indexUrl.replaceFirst("\\{page:"+startPage+"\\}", ""+page);
        HViewerHttpClient.get(url, new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String result) {
                List<Collection> collections = RuleParser.getCollections(result, site.indexRule);
                if(collections.size()>0){
                    if(page==startPage){
                        adapter.getDataProvider().clear();
                    }
                    adapter.getDataProvider().addAll(collections);
                    adapter.notifyDataSetChanged();
                    currPage = page;
                }
                rvCollection.setPullLoadMoreCompleted();
                for(Collection c:collections){
                    Log.d("CollectionFragment", c.cid+" "+c.cover);
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                Toast.makeText(getContext(), error.getErrorString(), Toast.LENGTH_SHORT).show();
                rvCollection.setPullLoadMoreCompleted();
            }
        });
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

    }
}
