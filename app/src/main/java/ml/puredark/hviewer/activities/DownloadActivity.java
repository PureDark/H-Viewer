package ml.puredark.hviewer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.adapters.DownloadTaskAdapter;
import ml.puredark.hviewer.adapters.PictureAdapter;
import ml.puredark.hviewer.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.customs.AutoFitGridLayoutManager;
import ml.puredark.hviewer.customs.ExTabLayout;
import ml.puredark.hviewer.customs.ExViewPager;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.DownloadManager;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.services.DownloadService;
import ml.puredark.hviewer.utils.DensityUtil;

import static ml.puredark.hviewer.HViewerApplication.historyHolder;

public class DownloadActivity extends AnimationActivity {

    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.tab_layout)
    ExTabLayout tabLayout;
    @BindView(R.id.view_pager)
    ExViewPager viewPager;
    @BindView(R.id.shadowDown)
    View shadowDown;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private RecyclerView rvDownloading, rvDownloaded;
    private DownloadTaskAdapter downloadTaskAdapter;

    private DownloadManager manager;
    private DownloadReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);
        setReturnButton(btnReturn);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MDStatusBarCompat.setToolbarTabLayout(this);

        manager = new DownloadManager(this);

        initTabAndViewPager();
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter downloadIntentFilter = new IntentFilter();
        downloadIntentFilter.addAction(DownloadService.ON_START);
        downloadIntentFilter.addAction(DownloadService.ON_PAUSE);
        downloadIntentFilter.addAction(DownloadService.ON_PROGRESS);
        downloadIntentFilter.addAction(DownloadService.ON_FAILURE);
        receiver = new DownloadReceiver();
        registerReceiver(receiver, downloadIntentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        manager.unbindService(this);
    }

    private void initTabAndViewPager() {
        //初始化Tab和ViewPager
        List<View> views = new ArrayList<>();
        View viewDownloading = getLayoutInflater().inflate(R.layout.view_collection_list, null);
        View viewDownloaded = getLayoutInflater().inflate(R.layout.view_collection_list, null);

        views.add(viewDownloading);
        views.add(viewDownloaded);
        List<String> titles = new ArrayList<>();
        titles.add("当前任务");
        titles.add("已完成");

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views, titles);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        rvDownloading = (RecyclerView) viewDownloading.findViewById(R.id.rv_collection);
        rvDownloaded = (RecyclerView) viewDownloaded.findViewById(R.id.rv_collection);
        List<DownloadTask> downloadTasks = manager.getDownloadTasks();
        downloadTaskAdapter = new DownloadTaskAdapter(new ListDataProvider(downloadTasks));
        rvDownloading.setAdapter(downloadTaskAdapter);

        downloadTaskAdapter.setOnItemClickListener(new DownloadTaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                DownloadTask task = (DownloadTask) downloadTaskAdapter.getDataProvider().getItem(position);
                if(task.paused)
                    manager.startDownload(task);
                else
                    manager.pauseDownload();
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                final DownloadTask task = (DownloadTask) downloadTaskAdapter.getDataProvider().getItem(position);
                new AlertDialog.Builder(DownloadActivity.this).setTitle("是否删除？")
                        .setMessage("删除后将无法恢复")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                manager.deleteDownloadTask(task);
                                downloadTaskAdapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("取消", null).show();
                return true;
            }
        });

    }

    public class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ON_START)||
                    intent.getAction().equals(DownloadService.ON_PAUSE)||
                    intent.getAction().equals(DownloadService.ON_PROGRESS)) {
                downloadTaskAdapter.notifyDataSetChanged();
            }else if(intent.getAction().equals(DownloadService.ON_FAILURE)){
                showSnackBar("下载失败，请重试");
                downloadTaskAdapter.notifyDataSetChanged();
            }else if(intent.getAction().equals(DownloadService.ON_COMPLETE)){
                showSnackBar("任务下载成功");
                downloadTaskAdapter.notifyDataSetChanged();
            }
        }

    }

}
