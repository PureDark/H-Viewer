package ml.puredark.hviewer.activities;

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
import ml.puredark.hviewer.adapters.CollectionAdapter;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;

import static ml.puredark.hviewer.HViewerApplication.historyHolder;

public class HistoryActivity extends AnimationActivity {

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

        tvTitle.setText("历史纪录");

        List<Collection> collections = HViewerApplication.historyHolder.getHistories();
        AbstractDataProvider<Collection> dataProvider = new ListDataProvider<>(collections);
        adapter = new CollectionAdapter(dataProvider);
        rvCollection.setAdapter(adapter);

        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Collection collection = (Collection) adapter.getDataProvider().getItem(position);
                if (collection instanceof LocalCollection) {
                    LocalCollection col = (LocalCollection) collection;
                    HViewerApplication.temp = col.site;
                    HViewerApplication.temp2 = collection;
                    Intent intent = new Intent(HistoryActivity.this, CollectionActivity.class);
                    startActivity(intent);
                } else {
                    showSnackBar("该历史数据有误，请删除重新添加");
                }
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                final Collection collection = (Collection) adapter.getDataProvider().getItem(position);
                new AlertDialog.Builder(HistoryActivity.this).setTitle("是否删除？")
                        .setMessage("删除后将无法恢复")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                historyHolder.deleteHistory(collection);
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
}
