package ml.puredark.hviewer.ui.activities;

import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.CollectionGroup;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.ui.adapters.LocalCollectionAdapter;
import ml.puredark.hviewer.ui.dataproviders.ExpandableDataProvider;

public class FavouriteActivity extends BaseActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.app_bar)
    AppBarLayout appbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_collection)
    RecyclerView rvCollection;

    private LocalCollectionAdapter adapter;

    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

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
        MDStatusBarCompat.setSwipeBackToolBar(this, coordinatorLayout, appbar, toolbar);

        tvTitle.setText("收藏夹");

        reportShortcutUsed(this, "scfavorites");

        Intent intent = getIntent();
        if(intent!=null){
            Logger.d("ShortcutTest", "FavouriteActivity");
            Logger.d("ShortcutTest", intent.toString());
            String action = intent.getAction();
            if(HViewerApplication.INTENT_SHORTCUT.equals(action) && LockActivity.isSetLockMethod(this)){
                Intent lockIntent = new Intent(FavouriteActivity.this, LockActivity.class);
                lockIntent.setAction(HViewerApplication.INTENT_FROM_FAVOURITE);
                startActivity(lockIntent);
                finish();
                return;
            }
        }

        favouriteHolder = new FavouriteHolder(this);

        final List<Pair<CollectionGroup, List<LocalCollection>>> favGroups = favouriteHolder.getFavourites();

        ExpandableDataProvider dataProvider = new ExpandableDataProvider(favGroups);
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

        adapter = new LocalCollectionAdapter(this, dataProvider);

        // wrap for expanding
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(adapter);
        // wrap for dragging
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mWrappedAdapter);
        // wrap for swiping
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        rvCollection.setAdapter(mWrappedAdapter);
        rvCollection.setHasFixedSize(false);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        rvCollection.setItemAnimator(animator);

        adapter.setExpandableItemManager(mRecyclerViewExpandableItemManager);

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(rvCollection);
        mRecyclerViewSwipeManager.attachRecyclerView(rvCollection);
        mRecyclerViewDragDropManager.attachRecyclerView(rvCollection);
        mRecyclerViewExpandableItemManager.attachRecyclerView(rvCollection);

        if (adapter.getDataProvider().getGroupCount() > 0)
            mRecyclerViewExpandableItemManager.expandGroup(0);

        adapter.setOnItemClickListener(new LocalCollectionAdapter.OnItemClickListener() {
            @Override
            public void onGroupClick(View v, int groupPosition) {
                // 点击分类（如果是新建按钮则创建，否则展开）
                if (groupPosition == adapter.getGroupCount() - 1) {
                    View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
                    MaterialEditText inputGroupTitle = (MaterialEditText) view.findViewById(R.id.input_text);
                    new AlertDialog.Builder(FavouriteActivity.this)
                            .setTitle("新建组名")
                            .setView(view)
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", (dialog, which) -> {
                                String title = inputGroupTitle.getText().toString();
                                CollectionGroup group = new CollectionGroup(0, title);
                                int gid = favouriteHolder.addFavGroup(group);
                                group.gid = gid;
                                group.index = gid;
                                favouriteHolder.updateFavGroupIndex(group);
                                adapter.getDataProvider().setDataSet(favouriteHolder.getFavourites());
                                adapter.notifyDataSetChanged();
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
                final CollectionGroup group = adapter.getDataProvider().getGroupItem(groupPosition);
                new AlertDialog.Builder(FavouriteActivity.this)
                        .setTitle("操作")
                        .setItems(new String[]{"重命名", "删除"}, (dialogInterface, i) -> {
                            if (i == 0) {
                                View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
                                MaterialEditText inputGroupTitle = (MaterialEditText) view.findViewById(R.id.input_text);
                                new AlertDialog.Builder(FavouriteActivity.this).setTitle("重命名组")
                                        .setView(view)
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            String title = inputGroupTitle.getText().toString();
                                            group.title = title;
                                            favouriteHolder.updateFavGroup(group);
                                            adapter.notifyDataSetChanged();
                                        }).show();
                            } else if (i == 1) {
                                new AlertDialog.Builder(FavouriteActivity.this).setTitle("是否删除？")
                                        .setMessage("删除后将无法恢复")
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            favouriteHolder.deleteFavGroup(group);
                                            adapter.getDataProvider().removeGroupItem(groupPosition);
                                            adapter.notifyDataSetChanged();
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
                    LocalCollection collection = adapter.getDataProvider().getChildItem(groupPosition, childPosition);
                    HViewerApplication.temp = collection.site;
                    HViewerApplication.temp2 = collection;
                    Intent intent = new Intent(FavouriteActivity.this, CollectionActivity.class);
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

        adapter.setOnItemEventListener(new LocalCollectionAdapter.OnItemEventListener() {
            @Override
            public void onGroupMove(int fromGroupPosition, int toGroupPosition) {
                int groupCount = adapter.getGroupCount() - 1;
                for (int m = 0; m < groupCount; m++) {
                    CollectionGroup group = adapter.getDataProvider().getGroupItem(m);
                    group.index = m + 1;
                    favouriteHolder.updateFavGroupIndex(group);
                }
            }

            @Override
            public void onItemMove(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
                Logger.d("FavouriteActivity", "fromGroupPosition:" + fromGroupPosition + " fromChildPosition:" + fromChildPosition + " toGroupPosition:" + toGroupPosition + " toChildPosition:" + toChildPosition);
                CollectionGroup group = adapter.getDataProvider().getGroupItem(toGroupPosition);
                LocalCollection collection = adapter.getDataProvider().getChildItem(toGroupPosition, toChildPosition);
                Logger.d("FavouriteActivity", "collection.title:"+collection.title);
                collection.gid = group.gid;
                favouriteHolder.updateFavouriteIndex(collection);
                updateGroupItemIndex(fromGroupPosition);
                if (fromGroupPosition != toGroupPosition)
                    updateGroupItemIndex(toGroupPosition);
            }

            @Override
            public void onItemRemoved(int groupPosition, int childPosition) {
                LocalCollection collection = adapter.getDataProvider().getChildItem(groupPosition, childPosition);
                favouriteHolder.deleteFavourite(collection);
                adapter.getDataProvider().removeChildItem(groupPosition, childPosition);
                mRecyclerViewExpandableItemManager.notifyChildItemRemoved(groupPosition, childPosition);
                showSnackBar("移除了一项收藏", "撤销", v -> {
                    LocalCollection recoveredItem = adapter.getDataProvider().undoLastRemoval();
                    mRecyclerViewExpandableItemManager.notifyChildItemInserted(groupPosition, childPosition);
                    int cid = favouriteHolder.addFavourite(recoveredItem);
                    if(cid >= 0)
                        recoveredItem.cid = cid;
                });
            }

            private void updateGroupItemIndex(int groupPosition) {
                int childCount = adapter.getChildCount(groupPosition);
                for (int i = 0; i < childCount; i++) {
                    LocalCollection collection = adapter.getDataProvider().getChildItem(groupPosition, i);
                    Logger.d("FavouriteActivity", "collection.title:"+collection.title + "collection.gid:"+collection.gid);
                    collection.index = i + 1;
                    favouriteHolder.updateFavouriteIndex(collection);
                }
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
                .setPositiveButton("确定", (dialog, which) -> {
                    favouriteHolder.clear();
                    adapter.getDataProvider().setDataSet(favouriteHolder.getFavourites());
                    adapter.notifyDataSetChanged();
                }).setNegativeButton("取消", null).show();
    }

    @Override
    public void onDestroy() {
        if (favouriteHolder != null)
            favouriteHolder.onDestroy();
        super.onDestroy();
    }
}
