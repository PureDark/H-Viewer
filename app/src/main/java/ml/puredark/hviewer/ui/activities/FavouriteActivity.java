package ml.puredark.hviewer.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.adapters.CollectionAdapter;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.dataholders.FavouriteHolder;

public class FavouriteActivity extends BaseActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_collection)
    RecyclerView rvCollection;

    private CollectionAdapter adapter;

    private FavouriteHolder favouriteHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);
        setReturnButton(btnReturn);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MDStatusBarCompat.setOrdinaryToolBar(this);

        tvTitle.setText("收藏夹");

        favouriteHolder = new FavouriteHolder(this);

        List<Collection> collections = favouriteHolder.getFavourites();
        ListDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(this, dataProvider);
        rvCollection.setAdapter(adapter);

        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Collection collection = (Collection) adapter.getDataProvider().getItem(position);
                if (collection instanceof LocalCollection) {
                    LocalCollection col = (LocalCollection) collection;
                    HViewerApplication.temp = col.site;
                    HViewerApplication.temp2 = collection;
                    Intent intent = new Intent(FavouriteActivity.this, CollectionActivity.class);
                    startActivity(intent);
                } else {
                    showSnackBar("该收藏数据有误，请删除重新添加");
                }
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                final Collection collection = (Collection) adapter.getDataProvider().getItem(position);
                new AlertDialog.Builder(FavouriteActivity.this).setTitle("是否删除？")
                        .setMessage("删除后将无法恢复")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                favouriteHolder.deleteFavourite(collection);
                                adapter.getDataProvider().setDataSet(favouriteHolder.getFavourites());
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("取消", null).show();
                return true;
            }
        });
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.btn_clear_all)
    void clear() {
        new AlertDialog.Builder(FavouriteActivity.this).setTitle("是否清空收藏夹？")
                .setMessage("清空后将无法恢复")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        favouriteHolder.clear();
                        adapter.getDataProvider().setDataSet(favouriteHolder.getFavourites());
                        adapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("取消", null).show();
    }

    @Override
    public void onDestroy(){
        if(favouriteHolder!=null)
            favouriteHolder.onDestroy();
        super.onDestroy();
    }
}
