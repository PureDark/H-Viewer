package ml.puredark.hviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.CollectionGroup;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.download.DownloadService;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.libraries.advrecyclerview.common.data.AbstractExpandableDataProvider;
import ml.puredark.hviewer.ui.adapters.DownloadedTaskAdapter;
import ml.puredark.hviewer.ui.adapters.DownloadingTaskAdapter;
import ml.puredark.hviewer.ui.adapters.LocalCollectionAdapter;
import ml.puredark.hviewer.ui.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.ui.customs.ExTabLayout;
import ml.puredark.hviewer.ui.customs.ExViewPager;
import ml.puredark.hviewer.ui.dataproviders.ExpandableDataProvider;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class DownloadActivity extends BaseActivity {

    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.app_bar)
    AppBarLayout appbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    ExTabLayout tabLayout;
    @BindView(R.id.view_pager)
    ExViewPager viewPager;
    @BindView(R.id.shadowDown)
    View shadowDown;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private RecyclerView rvDownloading, rvDownloaded;
    private DownloadingTaskAdapter downloadingTaskAdapter;
    private DownloadedTaskAdapter downloadedTaskAdapter;

    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    private DownloadManager manager;
    private DownloadReceiver receiver;
    private DownloadTaskHolder holder;

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
        MDStatusBarCompat.setSwipeBackToolBar(this, coordinatorLayout, appbar, toolbar);

        receiver = new MyDownloadReceiver();
        setDownloadReceiver(receiver);

        reportShortcutUsed(this, "scdownload");

        Intent intent = getIntent();
        if (intent != null) {
            Logger.d("ShortcutTest", "DownloadActivity");
            Logger.d("ShortcutTest", intent.toString());
            String action = intent.getAction();
            if (HViewerApplication.INTENT_SHORTCUT.equals(action) && LockActivity.isSetLockMethod(this)) {
                Intent lockIntent = new Intent(DownloadActivity.this, LockActivity.class);
                lockIntent.setAction(HViewerApplication.INTENT_FROM_DOWNLOAD);
                startActivity(lockIntent);
                finish();
                return;
            }
        }

        manager = new DownloadManager(this);
        holder = new DownloadTaskHolder(this);

        initTabAndViewPager();
        initDownloadedTask();
        new Thread(() -> distinguishDownloadTasks()).start();

    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> distinguishDownloadTasks()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (manager != null) {
                manager.unbindService(this);
            }
        } catch (Exception e) {
        }
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
        titles.add("已完成　");

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views, titles);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        rvDownloading = (RecyclerView) viewDownloading.findViewById(R.id.rv_collection);
        rvDownloaded = (RecyclerView) viewDownloaded.findViewById(R.id.rv_collection);

        downloadingTaskAdapter = new DownloadingTaskAdapter(this, new ListDataProvider(new ArrayList<DownloadTask>()));
        rvDownloading.setAdapter(downloadingTaskAdapter);

        downloadingTaskAdapter.setOnItemClickListener(new DownloadingTaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                DownloadTask task = (DownloadTask) downloadingTaskAdapter.getDataProvider().getItem(position);
                if (task.status == DownloadTask.STATUS_GETTING) {
                    manager.pauseDownload();
                } else if (task.status == DownloadTask.STATUS_IN_QUEUE) {
                    task.status = DownloadTask.STATUS_PAUSED;
                } else if (task.status == DownloadTask.STATUS_PAUSED) {
                    task.status = DownloadTask.STATUS_IN_QUEUE;
                    if (!manager.isDownloading())
                        startNextTaskInQueue();
                }
                downloadingTaskAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                final DownloadTask task = (DownloadTask) downloadingTaskAdapter.getDataProvider().getItem(position);
                String[] options = (task.collection.videos != null && task.collection.videos.size() > 0)
                        ? new String[]{"重新下载", "删除"}
                        : new String[]{"浏览", "删除"};
                new AlertDialog.Builder(DownloadActivity.this)
                        .setTitle("操作")
                        .setItems(options, (dialogInterface, i) -> {
                            if (i == 0) {
                                if (task.collection.videos != null && task.collection.videos.size() > 0) {
                                    manager.restartDownload(task);
                                } else {
                                    HViewerApplication.temp = task;
                                    Intent intent = new Intent(DownloadActivity.this, DownloadTaskActivity.class);
                                    startActivity(intent);
                                }
                            } else if (i == 1) {
                                View view = LayoutInflater.from(DownloadActivity.this).inflate(R.layout.dialog_delete_confirm, null);
                                AppCompatCheckBox checkBoxDeleteFile = (AppCompatCheckBox) view.findViewById(R.id.checkbox_delete_file);
                                new AlertDialog.Builder(DownloadActivity.this)
                                        .setView(view)
                                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                                            manager.deleteDownloadTask(task);
                                            new Thread(() -> distinguishDownloadTasks()).start();
                                            if (checkBoxDeleteFile.isChecked()) {
                                                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                                                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                                                FileHelper.deleteFile(dirName, rootPath);
                                            }
                                        }).setNegativeButton(getString(R.string.cancel), null).show();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                return true;
            }
        });
    }

    private void initDownloadedTask() {
        ExpandableDataProvider dataProvider = new ExpandableDataProvider(new ArrayList<>());
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(null);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setInitiateOnTouch(false);
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);

        // 拖拽时的阴影
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(this, R.drawable.material_shadow_z3));

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        downloadedTaskAdapter = new DownloadedTaskAdapter(this, dataProvider);

        // wrap for expanding
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(downloadedTaskAdapter);
        // wrap for dragging
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mWrappedAdapter);
        // wrap for swiping
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        rvDownloaded.setAdapter(mWrappedAdapter);
        rvDownloaded.setHasFixedSize(false);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        rvDownloaded.setItemAnimator(animator);

        downloadedTaskAdapter.setExpandableItemManager(mRecyclerViewExpandableItemManager);

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(rvDownloaded);
        mRecyclerViewSwipeManager.attachRecyclerView(rvDownloaded);
        mRecyclerViewDragDropManager.attachRecyclerView(rvDownloaded);
        mRecyclerViewExpandableItemManager.attachRecyclerView(rvDownloaded);

        downloadedTaskAdapter.setOnItemClickListener(new DownloadedTaskAdapter.OnItemClickListener() {
            @Override
            public void onGroupClick(View v, int groupPosition) {
                // 点击分类（如果是新建按钮则创建，否则展开）
                if (groupPosition == downloadedTaskAdapter.getGroupCount() - 1) {
                    View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
                    MaterialEditText inputGroupTitle = (MaterialEditText) view.findViewById(R.id.input_text);
                    new AlertDialog.Builder(DownloadActivity.this)
                            .setTitle("新建组名")
                            .setView(view)
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", (dialog, which) -> {
                                String title = inputGroupTitle.getText().toString();
                                CollectionGroup group = new CollectionGroup(0, title);
                                int gid = holder.addDlGroup(group);
                                group.gid = gid;
                                group.index = gid;
                                holder.updateDlGroupIndex(group);
                                downloadedTaskAdapter.getDataProvider().setDataSet(holder.getDownloadedTasksFromDB());
                                downloadedTaskAdapter.notifyDataSetChanged();
                            }).show();
                } else {
                    mRecyclerViewExpandableItemManager.notifyGroupItemChanged(groupPosition);
                }
            }

            @Override
            public boolean onGroupLongClick(View v, final int groupPosition) {
                if (mRecyclerViewDragDropManager.isDragging())
                    return true;
                // 分类上长按，选择操作
                ExpandableDataProvider<CollectionGroup, DownloadTask> provider = downloadedTaskAdapter.getDataProvider();
                final CollectionGroup group = provider.getGroupItem(groupPosition);
                new AlertDialog.Builder(DownloadActivity.this)
                        .setTitle("操作")
                        .setItems(new String[]{"重命名", "删除"}, (dialogInterface, i) -> {
                            if (i == 0) {
                                View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
                                MaterialEditText inputGroupTitle = (MaterialEditText) view.findViewById(R.id.input_text);
                                new AlertDialog.Builder(DownloadActivity.this).setTitle("重命名组")
                                        .setView(view)
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            String title = inputGroupTitle.getText().toString();
                                            group.title = title;
                                            holder.updateDlGroup(group);
                                            downloadedTaskAdapter.notifyDataSetChanged();
                                        }).show();
                            } else if (i == 1) {
                                new AlertDialog.Builder(DownloadActivity.this).setTitle("是否删除？")
                                        .setMessage("删除后将无法恢复")
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            holder.deleteDlGroup(group);
                                            downloadedTaskAdapter.getDataProvider().removeGroupItem(groupPosition);
                                            downloadedTaskAdapter.notifyDataSetChanged();
                                        }).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }

            @Override
            public void onItemClick(View v, int groupPosition, int childPosition) {
                try {
                    ExpandableDataProvider<CollectionGroup, DownloadTask> provider = downloadedTaskAdapter.getDataProvider();
                    DownloadTask task = provider.getChildItem(groupPosition, childPosition);
                    HViewerApplication.temp = task;
                    Intent intent = new Intent(DownloadActivity.this, DownloadTaskActivity.class);
                    startActivity(intent);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onItemLongClick(View v, final int groupPosition, final int childPosition) {
                return false;
            }

        });

        downloadedTaskAdapter.setOnItemEventListener(new DownloadedTaskAdapter.OnItemEventListener() {
            @Override
            public void onGroupMove(int fromGroupPosition, int toGroupPosition) {
                ExpandableDataProvider<CollectionGroup, DownloadTask> provider = downloadedTaskAdapter.getDataProvider();
                int groupCount = downloadedTaskAdapter.getGroupCount() - 1;
                for (int m = 0; m < groupCount; m++) {
                    CollectionGroup group = provider.getGroupItem(m);
                    group.index = m + 1;
                    holder.updateDlGroupIndex(group);
                }
            }

            @Override
            public void onItemMove(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
                ExpandableDataProvider<CollectionGroup, DownloadTask> provider = downloadedTaskAdapter.getDataProvider();
                CollectionGroup group = provider.getGroupItem(toGroupPosition);
                DownloadTask task = provider.getChildItem(toGroupPosition, toChildPosition);
                task.collection.gid = group.gid;
                holder.updateDownloadItemIndex(task);
                updateGroupItemIndex(fromGroupPosition);
                if (fromGroupPosition != toGroupPosition)
                    updateGroupItemIndex(toGroupPosition);
            }

            @Override
            public void onItemRemoved(int groupPosition, int childPosition) {
                final ExpandableDataProvider<CollectionGroup, DownloadTask> provider = downloadedTaskAdapter.getDataProvider();
                final DownloadTask task = provider.getChildItem(groupPosition, childPosition);
                holder.deleteDownloadTask(task);
                downloadedTaskAdapter.getDataProvider().removeChildItem(groupPosition, childPosition);
                mRecyclerViewExpandableItemManager.notifyChildItemRemoved(groupPosition, childPosition);
                View view = LayoutInflater.from(DownloadActivity.this).inflate(R.layout.dialog_delete_confirm, null);
                AppCompatCheckBox checkBoxDeleteFile = (AppCompatCheckBox) view.findViewById(R.id.checkbox_delete_file);
                new AlertDialog.Builder(DownloadActivity.this)
                        .setView(view)
                        .setPositiveButton("确定", (dialog, which) -> {
                            manager.deleteDownloadTask(task);
                            new Thread(() -> distinguishDownloadTasks()).start();
                            if (checkBoxDeleteFile.isChecked()) {
                                String rootPath = task.path.substring(0, task.path.lastIndexOf("/"));
                                String dirName = task.path.substring(task.path.lastIndexOf("/") + 1, task.path.length());
                                FileHelper.deleteFile(dirName, rootPath);
                            }
                        }).setNegativeButton("撤销", (dialog, which) -> {
                    DownloadTask recoveredItem = provider.undoLastRemoval();
                    mRecyclerViewExpandableItemManager.notifyChildItemInserted(groupPosition, childPosition);
                    holder.addDownloadTask(recoveredItem);
                }).show();
            }

            private void updateGroupItemIndex(int groupPosition) {
                ExpandableDataProvider<CollectionGroup, DownloadTask> provider = downloadedTaskAdapter.getDataProvider();
                int childCount = downloadedTaskAdapter.getChildCount(groupPosition);
                for (int i = 0; i < childCount; i++) {
                    DownloadTask task = provider.getChildItem(groupPosition, i);
                    task.collection.index = i + 1;
                    holder.updateDownloadItemIndex(task);
                }
            }
        });
    }

    private synchronized void distinguishDownloadTasks() {
        manager.checkDownloadedTask();
        List<DownloadTask> downloadingTasks = manager.getDownloadingTasks();
        List<Pair<CollectionGroup, List<DownloadTask>>> downloadedTasks = manager.getDownloadedTasks();
        downloadingTaskAdapter.getDataProvider().setDataSet(downloadingTasks);
        downloadedTaskAdapter.getDataProvider().setDataSet(downloadedTasks);
        runOnUiThread(() -> {
            downloadingTaskAdapter.notifyDataSetChanged();
            downloadedTaskAdapter.notifyDataSetChanged();
        });
    }

    private void startNextTaskInQueue() {
        List<DownloadTask> downloadingTasks = manager.getDownloadingTasks();
        for (DownloadTask task : downloadingTasks) {
            if (task.status == DownloadTask.STATUS_IN_QUEUE) {
                manager.startDownload(task);
                break;
            }
        }
    }


    public class MyDownloadReceiver extends DownloadReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ON_START) ||
                    intent.getAction().equals(DownloadService.ON_PROGRESS)) {
                downloadingTaskAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(DownloadService.ON_PAUSE)) {
                startNextTaskInQueue();
            } else if (intent.getAction().equals(DownloadService.ON_FAILURE)) {
                String message = intent.getStringExtra("message");
                message = ("".equals(message)) ? "下载失败，请重试" : message;
                showSnackBar(message);
                downloadingTaskAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(DownloadService.ON_COMPLETE)) {
                showSnackBar("任务下载成功");
                new Thread(() -> distinguishDownloadTasks()).start();
                startNextTaskInQueue();
            }
            Log.d("MyDownloadReceiver", intent.getAction());
        }

    }

}
