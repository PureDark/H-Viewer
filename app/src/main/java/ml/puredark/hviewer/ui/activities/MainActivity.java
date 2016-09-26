package ml.puredark.hviewer.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.gson.Gson;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.helpers.ExampleSites;
import ml.puredark.hviewer.ui.adapters.CategoryAdapter;
import ml.puredark.hviewer.ui.adapters.MySearchAdapter;
import ml.puredark.hviewer.ui.adapters.SiteAdapter;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.ui.customs.AppBarStateChangeListener;
import ml.puredark.hviewer.ui.dataproviders.ExpandableDataProvider;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.CollectionFragment;
import ml.puredark.hviewer.ui.fragments.MyFragment;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.utils.SimpleFileUtil;

import static ml.puredark.hviewer.HViewerApplication.temp;


public class MainActivity extends AnimationActivity {
    private static int RESULT_ADD_SITE = 1;
    private static int RESULT_MODIFY_SITE = 2;
    private static int RESULT_LOGIN = 3;

    @BindView(R.id.content)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_search)
    FloatingActionButton fabSearch;

    @BindView(R.id.nav_header_view)
    LinearLayout navHeaderView;
    @BindView(R.id.rv_site)
    RecyclerView rvSite;

    @BindView(R.id.rv_category)
    RecyclerView rvCategory;

    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    private SiteAdapter siteAdapter;
    private CategoryAdapter categoryAdapter;

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

    //记录当前加载的是哪个Fragment
    private MyFragment currFragment;

    //当前搜索的查询关键字
    private String currQuery;
    private boolean isSuggestionEmpty = true;

    private SiteHolder siteHolder;

    //按下返回键次数
    private int backCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MDStatusBarCompat.setCollapsingToolbar(this, coordinatorLayout, appBar, backdrop, toolbar);

        // User interface
        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);

        siteHolder = new SiteHolder(this);

        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchView.getLayoutParams();
            lp.topMargin = MDStatusBarCompat.getStatusBarHeight(this);
            searchView.setLayoutParams(lp);
        }


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //appbar折叠时显示搜索按钮和搜索框，否则隐藏
        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                int size = toolbar.getMenu().size();
                if (state == State.COLLAPSED) {
                    if (size > 1)
                        toolbar.getMenu().getItem(size - 1).setVisible(true);
                    searchView.animate().alpha(1f).setDuration(300);
                } else {
                    if (size > 1)
                        toolbar.getMenu().getItem(size - 1).setVisible(false);
                    searchView.animate().alpha(0f).setDuration(300);
                }
            }
        });

        initSearchSuggestions();

        searchView.setSubmitOnClick(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                currQuery = keyword;
                HViewerApplication.searchHistoryHolder.addSearchHistory(keyword);
                if (!"".equals(keyword) && currFragment != null)
                    currFragment.onSearch(keyword);
                searchView.setSuggestions(new String[0]);
                isSuggestionEmpty = true;
                searchView.clearFocus();
                searchView.hideKeyboard(coordinatorLayout);
                searchView.dismissSuggestions();
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (isSuggestionEmpty)
                    initSearchSuggestions();
                currQuery = newText;
                return false;
            }
        });

        final List<Pair<SiteGroup, List<Site>>> siteGroups = siteHolder.getSites();

        // 测试新站点用
//        List<Site> sites = ExampleSites.get();
//        siteGroups.add(0, new Pair<SiteGroup, List<Site>>(new SiteGroup(1, "TEST"), new ArrayList<Site>()));
//        siteGroups.get(0).second.addAll(sites);
//        SimpleFileUtil.writeString("/sdcard/sites.txt", new Gson().toJson(sites), "utf-8");

        ExpandableDataProvider dataProvider = new ExpandableDataProvider(siteGroups);

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(new RecyclerViewExpandableItemManager.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition, boolean fromUser) {
                if (fromUser) {
                    int childItemHeight = getResources().getDimensionPixelSize(R.dimen.item_site_height);
                    mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight, 0, 0);
                }
            }
        });
        mRecyclerViewExpandableItemManager.setOnGroupCollapseListener(new RecyclerViewExpandableItemManager.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition, boolean fromUser) {
            }
        });

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setInitiateOnTouch(true);

        siteAdapter = new SiteAdapter(dataProvider);

        // wrap for expanding
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(siteAdapter);
        // wrap for dragging
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mWrappedAdapter);

        rvSite.setAdapter(mWrappedAdapter);
        rvSite.setHasFixedSize(false);
        // NOTE: need to disable change animations to ripple effect work properly
        ((SimpleItemAnimator) rvSite.getItemAnimator()).setSupportsChangeAnimations(false);

        mRecyclerViewDragDropManager.attachRecyclerView(rvSite);
        mRecyclerViewExpandableItemManager.attachRecyclerView(rvSite);

        siteAdapter.setOnItemClickListener(new SiteAdapter.OnItemClickListener() {
            @Override
            public void onGroupClick(View v, int groupPosition) {
                if (groupPosition == siteAdapter.getGroupCount() - 1) {
                    final EditText inputGroupTitle = new EditText(MainActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("新建组名").setView(inputGroupTitle)
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String title = inputGroupTitle.getText().toString();
                                    SiteGroup group = new SiteGroup(0, title);
                                    siteHolder.addSiteGroup(group);
                                    int gid = siteHolder.getMaxGroupId();
                                    group.gid = gid;
                                    group.index = gid;
                                    siteHolder.updateSiteGroupIndex(group);
                                    siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                                    siteAdapter.notifyDataSetChanged();
                                }
                            }).show();
                } else {
                    notifyGroupItemChanged(groupPosition);
                }
            }

            @Override
            public boolean onGroupLongClick(View v, final int groupPosition) {
                final SiteGroup group = siteAdapter.getDataProvider().getGroupItem(groupPosition);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("操作")
                        .setItems(new String[]{"重命名", "删除"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    final EditText inputGroupTitle = new EditText(MainActivity.this);
                                    inputGroupTitle.setText(group.title);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("重命名组").setView(inputGroupTitle)
                                            .setNegativeButton("取消", null)
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String title = inputGroupTitle.getText().toString();
                                                    group.title = title;
                                                    siteHolder.updateSiteGroup(group);
                                                    siteAdapter.notifyDataSetChanged();
                                                }
                                            }).show();
                                } else if (i == 1) {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("是否删除？")
                                            .setMessage("删除后将无法恢复")
                                            .setNegativeButton("取消", null)
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    siteHolder.deleteSiteGroup(group);
                                                    siteAdapter.getDataProvider().removeGroupItem(groupPosition);
                                                    siteAdapter.notifyDataSetChanged();
                                                }
                                            }).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }

            @Override
            public void onItemClick(View v, int groupPosition, int childPosition, boolean isGrid) {
                if (childPosition == siteAdapter.getChildCount(groupPosition) - 1) {
                    Pair<SiteGroup, List<Site>> pair = siteAdapter.getDataProvider().getItem(groupPosition);
                    Intent intent = new Intent(MainActivity.this, AddSiteActivity.class);
                    HViewerApplication.temp = pair;
                    startActivityForResult(intent, RESULT_ADD_SITE);
                } else {
                    Site site = siteAdapter.getDataProvider().getChildItem(groupPosition, childPosition);
                    CollectionFragment fragment = CollectionFragment.newInstance(site);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isGrid", isGrid);
                    fragment.setArguments(bundle);
                    selectSite(fragment, site);
                    notifyChildItemChanged(groupPosition, childPosition);
                }
                drawer.closeDrawer(GravityCompat.START);
            }

            @Override
            public boolean onItemLongClick(View v, final int groupPosition, final int childPosition) {
                final Site site = siteAdapter.getDataProvider().getChildItem(groupPosition, childPosition);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("操作")
                        .setItems(new String[]{"登录", "编辑", "删除"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    temp = site;
                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivityForResult(intent, RESULT_LOGIN);
                                } else if (i == 1) {
                                    temp = site;
                                    Intent intent = new Intent(MainActivity.this, ModifySiteActivity.class);
                                    startActivityForResult(intent, RESULT_MODIFY_SITE);
                                } else if (i == 2) {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("是否删除？")
                                            .setMessage("删除后将无法恢复")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    siteHolder.deleteSite(site);
                                                    siteAdapter.getDataProvider().removeChildItem(groupPosition, childPosition);
                                                    siteAdapter.notifyDataSetChanged();
                                                }
                                            }).setNegativeButton("取消", null).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }

            public void notifyGroupItemChanged(int groupPosition) {
                final long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForGroup(groupPosition);
                final int flatPosition = mRecyclerViewExpandableItemManager.getFlatPosition(expandablePosition);

                mWrappedAdapter.notifyItemChanged(flatPosition);
            }

            public void notifyChildItemChanged(int groupPosition, int childPosition) {
                final long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, childPosition);
                final int flatPosition = mRecyclerViewExpandableItemManager.getFlatPosition(expandablePosition);

                mWrappedAdapter.notifyItemChanged(flatPosition);
            }
        });

        siteAdapter.setOnItemMoveListener(new SiteAdapter.OnItemMoveListener() {
            @Override
            public void onGroupMove(int fromGroupPosition, int toGroupPosition) {
                int groupCount = siteAdapter.getGroupCount()-1;
                for (int m = 0; m < groupCount; m++) {
                    SiteGroup group = siteAdapter.getDataProvider().getGroupItem(m);
                    group.index = m + 1;
                    siteHolder.updateSiteGroupIndex(group);
                }
            }

            @Override
            public void onItemMove(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
                SiteGroup group = siteAdapter.getDataProvider().getGroupItem(toGroupPosition);
                Site site = siteAdapter.getDataProvider().getChildItem(toGroupPosition, toChildPosition);
                site.gid = group.gid;
                siteHolder.updateSite(site);
                updateGroupItemIndex(fromGroupPosition);
                if (fromGroupPosition != toGroupPosition)
                    updateGroupItemIndex(toGroupPosition);
                notifyChildItemMoved(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
            }

            private void updateGroupItemIndex(int groupPosition) {
                int childCount = siteAdapter.getChildCount(groupPosition)-1;
                for (int i = 0; i < childCount; i++) {
                    Site site = siteAdapter.getDataProvider().getChildItem(groupPosition, i);
                    site.index = i + 1;
                    siteHolder.updateSiteIndex(site);
                }
            }

            public void notifyChildItemMoved(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
                final long fromPosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(fromGroupPosition, fromChildPosition);
                final int flatFromPosition = mRecyclerViewExpandableItemManager.getFlatPosition(fromPosition);
                final long toPosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(toGroupPosition, toChildPosition);
                final int flatToPosition = mRecyclerViewExpandableItemManager.getFlatPosition(toPosition);

                mWrappedAdapter.notifyItemMoved(flatFromPosition, flatToPosition);
            }
        });

        siteAdapter.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final boolean right) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (right)
                            currFragment.setRecyclerViewToGrid();
                        else
                            currFragment.setRecyclerViewToList();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        }, 200);
                    }
                }, 300);
            }
        });

        ListDataProvider<Category> categoryProvider = new ListDataProvider<>(new ArrayList<Category>());
        categoryAdapter = new CategoryAdapter(categoryProvider);
        categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Category category = (Category) categoryAdapter.getDataProvider().getItem(position);
                categoryAdapter.selectedCid = category.cid;
                categoryAdapter.notifyDataSetChanged();
                currFragment.onCategorySelected(category);
                drawer.closeDrawer(GravityCompat.END);
            }
        });
        rvCategory.setAdapter(categoryAdapter);

        if (siteGroups.size() > 0 && siteGroups.get(0).second.size() > 0) {
            mRecyclerViewExpandableItemManager.expandGroup(0);
            Site site = siteGroups.get(0).second.get(0);
            selectSite(CollectionFragment.newInstance(site), site);
        }

        HViewerApplication.checkUpdate(this);
    }

    private void initSearchSuggestions() {
        List<String> histories = HViewerApplication.searchHistoryHolder.getSearchHistory();
        List<String> suggestions = HViewerApplication.searchSuggestionHolder.getSearchSuggestion();
        suggestions.addAll(histories);
        suggestions = new ArrayList(new HashSet(suggestions));
        Collections.sort(suggestions, String.CASE_INSENSITIVE_ORDER);
        int size = suggestions.size();
        String[] kwStrings = new String[size];
        kwStrings = suggestions.toArray(kwStrings);
        final MySearchAdapter adapter = new MySearchAdapter(this, kwStrings);
        searchView.setAdapter(adapter);

        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] keywords = currQuery.toString().split(" ");
                String keyword = "";
                for (int i = 0; i < keywords.length - 1; i++)
                    keyword += keywords[i] + " ";

                keyword += adapter.getItem(position);
                searchView.setQuery(keyword, false);
            }
        });
        isSuggestionEmpty = false;
    }

    public void setTitle(String title) {
        collapsingToolbarLayout.setTitle(title);
    }


    public void replaceFragment(MyFragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
        currFragment = fragment;
    }

    public void selectSite(MyFragment fragment, Site site) {
        siteAdapter.selectedSid = site.sid;
        siteAdapter.notifyDataSetChanged();
        setTitle(site.title);
        replaceFragment(fragment, site.title);

        if (site.categories != null && site.categories.size() > 0) {
            ListDataProvider<Category> dataProvider = new ListDataProvider<>(site.categories);
            categoryAdapter.setDataProvider(dataProvider);
            categoryAdapter.notifyDataSetChanged();

            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            Category category = site.categories.get(0);
            categoryAdapter.selectedCid = category.cid;
            categoryAdapter.notifyDataSetChanged();
            currFragment.onCategorySelected(category);
        } else {
            categoryAdapter.getDataProvider().clear();
            categoryAdapter.notifyDataSetChanged();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        //一开始隐藏搜索按钮
        item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Intent intent;
        switch (item.getItemId()) {
            case R.id.action_download:
                intent = new Intent(MainActivity.this, DownloadActivity.class);
                break;
            case R.id.action_history:
                intent = new Intent(MainActivity.this, HistoryActivity.class);
                break;
            case R.id.action_favourite:
                intent = new Intent(MainActivity.this, FavouriteActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
            }
        }, 500);
        return true;
    }

    @OnClick(R.id.fab_search)
    void search() {
        appBar.setExpanded(false);
        searchView.showSearch();
        searchView.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_ADD_SITE) {
                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                siteAdapter.notifyDataSetChanged();
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            selectSite(CollectionFragment.newInstance(site), site);
                        }
                    };
                    handler.post(r);
                }
            } else if (requestCode == RESULT_MODIFY_SITE) {
                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                siteAdapter.notifyDataSetChanged();
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            selectSite(CollectionFragment.newInstance(site), site);
                        }
                    };
                    handler.post(r);
                }
            } else if (requestCode == RESULT_LOGIN) {
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    siteHolder.updateSite(site);
                    Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            selectSite(CollectionFragment.newInstance(site), site);
                        }
                    };
                    handler.post(r);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            backCount++;
            if(backCount==1)
                showSnackBar("再按一次退出应用！");
            else if(backCount>=2)
                super.onBackPressed();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    backCount = 0;
                }
            }, 1000);
        }
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (siteHolder != null)
            siteHolder.onDestroy();
        HViewerApplication.searchHistoryHolder.saveSearchHistory();
        HViewerApplication.searchSuggestionHolder.saveSearchSuggestion();
        new DownloadTaskHolder(this).setAllPaused();

        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (rvSite != null) {
            rvSite.setItemAnimator(null);
            rvSite.setAdapter(null);
            rvSite = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        super.onDestroy();
    }

    @OnClick(R.id.btn_setting)
    void openSetting() {
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_exit)
    void exit() {
        finish();
    }

}
