package ml.puredark.hviewer.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.adapters.DownloadTaskAdapter;
import ml.puredark.hviewer.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.customs.ExTabLayout;
import ml.puredark.hviewer.customs.ExViewPager;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.DownloadManager;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.services.DownloadService;

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
    private DownloadTaskAdapter downloadingTaskAdapter, downloadedTaskAdapter;

    private DownloadManager manager;
    private DownloadReceiver receiver;

    private List<DownloadTask> downloadingTasks, downloadedTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        ButterKnife.bind(this);
        tvTitle.setText("下载管理");
        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);
        setReturnButton(btnReturn);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MDStatusBarCompat.setToolbarTabLayout(this);

        receiver = new MyDownloadReceiver();
        setDownloadReceiver(receiver);

        manager = new DownloadManager(this);
        downloadingTasks = new ArrayList<>();
        downloadedTasks = new ArrayList<>();

        initTabAndViewPager();
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
        distinguishDownloadTasks();
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

        downloadingTaskAdapter = new DownloadTaskAdapter(new ListDataProvider(downloadingTasks));
        rvDownloading.setAdapter(downloadingTaskAdapter);

        downloadingTaskAdapter.setOnItemClickListener(new DownloadTaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                DownloadTask task = (DownloadTask) downloadingTaskAdapter.getDataProvider().getItem(position);
                if(task.status==DownloadTask.STATUS_DOWNLOADING) {
                    manager.pauseDownload();
                }else if(task.status==DownloadTask.STATUS_IN_QUEUE){
                    task.status = DownloadTask.STATUS_PAUSED;
                }else if(task.status==DownloadTask.STATUS_PAUSED){
                    task.status = DownloadTask.STATUS_IN_QUEUE;
                    if(!manager.isDownloading())
                        startNextTaskInQueue();
                }
                downloadingTaskAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                final DownloadTask task = (DownloadTask) downloadingTaskAdapter.getDataProvider().getItem(position);
                new AlertDialog.Builder(DownloadActivity.this).setTitle("是否删除？")
                        .setMessage("删除后将无法恢复")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                manager.deleteDownloadTask(task);
                                distinguishDownloadTasks();
                            }
                        }).setNegativeButton("取消", null).show();
                return true;
            }
        });

        downloadedTaskAdapter = new DownloadTaskAdapter(new ListDataProvider(downloadedTasks));
        rvDownloaded.setAdapter(downloadedTaskAdapter);

        downloadedTaskAdapter.setOnItemClickListener(new DownloadTaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                DownloadTask task = (DownloadTask) downloadedTaskAdapter.getDataProvider().getItem(position);
                HViewerApplication.temp = task;
                Intent intent = new Intent(DownloadActivity.this, DownloadTaskActivity.class);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                final DownloadTask task = (DownloadTask) downloadedTaskAdapter.getDataProvider().getItem(position);
                new AlertDialog.Builder(DownloadActivity.this).setTitle("是否删除？")
                        .setMessage("删除后将无法恢复")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                manager.deleteDownloadTask(task);
                                distinguishDownloadTasks();
                            }
                        }).setNegativeButton("取消", null).show();
                return true;
            }
        });

    }

    private void distinguishDownloadTasks(){
        List<DownloadTask> downloadTasks = manager.getDownloadTasks();
        downloadingTaskAdapter.getDataProvider().clear();
        downloadedTaskAdapter.getDataProvider().clear();
        downloadingTasks = downloadingTaskAdapter.getDataProvider().getItems();
        downloadedTasks = downloadedTaskAdapter.getDataProvider().getItems();
        for(DownloadTask task : downloadTasks){
            if(task.status==DownloadTask.STATUS_COMPLETED)
                downloadedTasks.add(task);
            else
                downloadingTasks.add(task);
        }
        downloadingTaskAdapter.notifyDataSetChanged();
        downloadedTaskAdapter.notifyDataSetChanged();
    }

    private void startNextTaskInQueue(){
        for(DownloadTask task : downloadingTasks){
            if(task.status==DownloadTask.STATUS_IN_QUEUE){
                manager.startDownload(task);
                break;
            }
        }
    }


    public class MyDownloadReceiver extends DownloadReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ON_START)||
                    intent.getAction().equals(DownloadService.ON_PROGRESS)) {
                downloadingTaskAdapter.notifyDataSetChanged();
            }
            else if(intent.getAction().equals(DownloadService.ON_PAUSE)) {
                startNextTaskInQueue();
            }
            else if(intent.getAction().equals(DownloadService.ON_FAILURE)){
                String message = intent.getStringExtra("message");
                message = ("".equals(message))?"下载失败，请重试":message;
                showSnackBar(message);
                downloadingTaskAdapter.notifyDataSetChanged();
            }
            else if(intent.getAction().equals(DownloadService.ON_COMPLETE)){
                showSnackBar("任务下载成功");
                distinguishDownloadTasks();
                startNextTaskInQueue();
            }
            Log.d("DownloadReceiver", intent.getAction());
        }

    }

}
