package ml.puredark.hviewer.ui.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dpizarro.autolabel.library.AutoLabelUI;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.gc.materialdesign.views.ButtonFlat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.majiajie.pagerbottomtabstrip.Controller;
import me.majiajie.pagerbottomtabstrip.PagerBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.TabItemBuilder;
import me.majiajie.pagerbottomtabstrip.TabStripBuild;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectListener;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.dataholders.AbstractTagHolder;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.dataholders.FavorTagHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.dataholders.SiteTagHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.helpers.UpdateManager;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.adapters.CategoryAdapter;
import ml.puredark.hviewer.ui.adapters.MySearchAdapter;
import ml.puredark.hviewer.ui.adapters.SiteAdapter;
import ml.puredark.hviewer.ui.adapters.SiteTagAdapter;
import ml.puredark.hviewer.ui.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.ui.customs.DragMarginDrawerLayout;
import ml.puredark.hviewer.ui.customs.RetainingDataSourceSupplier;
import ml.puredark.hviewer.ui.dataproviders.ExpandableDataProvider;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.CollectionFragment;
import ml.puredark.hviewer.ui.fragments.MyFragment;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.AppBarStateChangeListener;
import ml.puredark.hviewer.utils.DocumentUtil;
import ml.puredark.hviewer.utils.RegexValidateUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.HViewerApplication.mContext;
import static ml.puredark.hviewer.HViewerApplication.searchHistoryHolder;
import static ml.puredark.hviewer.HViewerApplication.temp;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.KEY_CUSTOM_HEADER_IMAGE;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.KEY_FIRST_TIME;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.KEY_PREF_DOWNLOAD_PATH;


public class MainActivity extends BaseActivity {
    private static int RESULT_ADD_SITE = 1;
    private static int RESULT_MODIFY_SITE = 2;
    private static int RESULT_LOGIN = 3;
    private static int RESULT_SITE_MARKET = 4;
    private static int RESULT_SETTING = 5;
    private static int RESULT_RDSQ = 6;
    private static int RESULT_SET_HEADER_IMAGE = 7;

    @BindView(R.id.content)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.drawer_layout)
    DragMarginDrawerLayout drawer;
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
    @BindView(R.id.rv_site)
    RecyclerView rvSite;
    @BindView(R.id.rv_category)
    RecyclerView rvCategory;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.bottom_sheet)
    View bottomSheet;
    @BindView(R.id.bottom_sheet_view_pager)
    ViewPager bottomSheetViewPager;
    @BindView(R.id.pager_bottom_tab_layout)
    PagerBottomTabLayout pagerBottomTabLayout;

    private SiteAdapter siteAdapter;
    private CategoryAdapter categoryAdapter;
    private SiteTagAdapter siteTagAdapter;
    private SiteTagAdapter favorTagAdapter;
    private SiteTagAdapter historyTagAdapter;

    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

    //记录当前加载的是哪个Fragment
    private MyFragment currFragment;

    //当前搜索的查询关键字
    private String currQuery;
    private boolean isSuggestionEmpty = true;

    //记录当前页头图url
    private Uri headerImageUri;

    private RetainingDataSourceSupplier supplier;

    private SiteHolder siteHolder;
    private SiteTagHolder siteTagHolder;
    private FavorTagHolder favorTagHolder;
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MDStatusBarCompat.setCollapsingToolbar(this, coordinatorLayout, appBar, backdrop, toolbar);

        // User interface
        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);
        setDrawerLayout(drawer);

        // 关闭默认统计，手动进行Fragment统计
        setAnalyze(false);

        // 关闭边缘滑动返回
        setSwipeBackEnable(false);

        // 开启按两次返回退出
        setDoubleBackExitEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }

        if (HViewerApplication.DEBUG)
            toolbar.setOnLongClickListener(v -> {
                if (currFragment != null && currFragment.getCurrSite() != null
                        && (currFragment.getCurrSite().hasFlag(Site.FLAG_JS_NEEDED_ALL)
                        || currFragment.getCurrSite().hasFlag(Site.FLAG_JS_NEEDED_INDEX))) {
                    if (currFragment instanceof CollectionFragment)
                        ((CollectionFragment) currFragment).toggleWebView();
                    return true;
                }
                return false;
            });

        siteHolder = new SiteHolder(this);
        siteTagHolder = new SiteTagHolder(this);
        favorTagHolder = new FavorTagHolder(this);

        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchView.getLayoutParams();
            lp.topMargin = MDStatusBarCompat.getStatusBarHeight(this);
            searchView.setLayoutParams(lp);
        }

        //获取下载目录权限
        if ((boolean) SharedPreferencesUtil.getData(this, KEY_FIRST_TIME, true)) {
            initSetDefultDownloadPath();
        }

        backdrop.setOnLongClickListener(v -> {
            String[] options = new String[]{"自定义", "随机图片"};
            new AlertDialog.Builder(this)
                    .setTitle("更改顶部图片")
                    .setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            Intent intent = new Intent();
                            if (Build.VERSION.SDK_INT < 19) {
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                            } else {
                                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                            }
                            intent.setType("image/*");
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(intent, RESULT_SET_HEADER_IMAGE);
                        } else if (i == 1) {
                            getBingImage();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });

        downloadManager = new DownloadManager(this);
        downloadManager.setAllPaused();

        initHeaderImage();

        initDrawer();

        initSearchSuggestions();

        initSearchView();

        initNavCategories();

        initNavSites();

        initBottomSheet();

        UpdateManager.checkUpdate(this);
    }

    private void initHeaderImage() {
        final String rootDir = mContext.getExternalCacheDir().getAbsolutePath();
        File headerFile = new File(rootDir + "/image/header.jpg");
        String currHeaderUrl = (headerFile.exists()) ? "file://" + headerFile.getAbsolutePath() : "drawable://backdrop";
        Logger.d("HeaderImage", "currHeaderUrl : " + currHeaderUrl);

        supplier = ImageLoader.loadImageFromUrlRetainingImage(this, backdrop, currHeaderUrl, null, null, true,
                new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        Animatable animatable = ((SimpleDraweeView) backdrop).getController().getAnimatable();
                        if (animatable != null)
                            animatable.start();
                        if (headerImageUri == null || headerImageUri.getPath().endsWith("header.jpg"))
                            return;
                        if (headerImageUri.getScheme().startsWith("http")) {
                            ImageLoader.loadBitmapFromUrl(MainActivity.this, headerImageUri.toString(), null, null, new BaseBitmapDataSubscriber() {
                                @Override
                                protected void onNewResultImpl(Bitmap bitmap) {
                                    DocumentFile documentFile = FileHelper.createFileIfNotExist("header.jpg", rootDir, "image");
                                    try {
                                        FileHelper.saveBitmapToFile(bitmap, documentFile);
                                        Logger.d("HeaderImage", "Header image saved!");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Logger.d("HeaderImage", "Header image save failed!");
                                    }
                                }

                                @Override
                                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                                    Logger.d("HeaderImage", "Header image save failed!");
                                }
                            });
                        } else {
                            Logger.d("HeaderImage", "headerImageUrl : " + headerImageUri.toString());
                            DocumentFile imageFile = DocumentFile.fromSingleUri(mContext, headerImageUri);
                            DocumentFile documentFile = FileHelper.createFileIfNotExist("header.jpg", rootDir, "image");
                            try {
                                InputStream in = DocumentUtil.getFileInputSteam(MainActivity.this, imageFile);
                                FileHelper.writeFromInputStream(in, documentFile);
                                Logger.d("HeaderImage", "Header image saved!");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Logger.d("HeaderImage", "Header image save failed!");
                            }
                        }
                    }
                });

        boolean hasCustomHeader = (boolean) SharedPreferencesUtil.getData(mContext, SettingFragment.KEY_CUSTOM_HEADER_IMAGE, false);
        if (!hasCustomHeader || "drawable://backdrop".equals(currHeaderUrl)) {
            getBingImage();
        }
    }

    private void getBingImage() {
        HViewerHttpClient.get(UrlConfig.getBingAPIUrl(), null, new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                if (!(result instanceof String))
                    return;
                try {
                    String text = (String) result;
                    JsonObject jsonObject = new JsonParser().parse(text).getAsJsonObject();
//                    String url = "http://www.bing.com";
//                    url += jsonObject.get("images").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                    String url = jsonObject.get("data").getAsJsonObject().get("original_pic").getAsString();
                    Uri uri = Uri.parse(url);
                    headerImageUri = uri;
                    //Fresco.getImagePipeline().evictFromMemoryCache(uri);
                    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                            .setResizeOptions(new ResizeOptions(1080, 1920))
                            .disableDiskCache()
                            .build();
                    supplier.setSupplier(Fresco.getImagePipeline().getDataSourceSupplier(request, this, ImageRequest.RequestLevel.FULL_FETCH));
                    SharedPreferencesUtil.saveData(mContext, SettingFragment.KEY_CUSTOM_HEADER_IMAGE, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
            }
        });
    }

    private void initDrawer() {
        if (isInOneHandMode()) {
            // 设定侧边栏滑动边距
            drawer.setDrawerLeftEdgeSize(0.5f);
            drawer.setDrawerRightEdgeSize(0.5f);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView.getId() == R.id.nav_main) {
                    if (isCategoryEnable)
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
                } else
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (drawerView.getId() == R.id.nav_main) {
                    if (isCategoryEnable)
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
                } else
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
            }
        });
    }

    private void initNavSites() {

        final List<Pair<SiteGroup, List<Site>>> siteGroups = siteHolder.getSites();
        //测试新站点
//        List<Site> sites = ExampleSites.get();
//        if (siteGroups.size() == 0)
//            siteGroups.add(0, new Pair<>(new SiteGroup(1, "TEST"), new ArrayList<>()));
//        siteGroups.get(0).second.addAll(sites);
//        siteGroups.get(0).second.add(0, sites.get(sites.size() - 2));
//        siteGroups.get(0).second.add(0, sites.get(sites.size()-1));
//        SimpleFileUtil.writeString("/sdcard/sites1.txt", new Gson().toJson(sites.get(sites.size() - 1)), "utf-8");
//        SimpleFileUtil.writeString("/sdcard/sites2.txt", new Gson().toJson(sites.get(sites.size() - 2)), "utf-8");
//        SimpleFileUtil.writeString("/sdcard/sites3.txt", new Gson().toJson(sites.get(sites.size() - 3)), "utf-8");
//        SimpleFileUtil.writeString("/sdcard/sites.txt", new Gson().toJson(sites), "utf-8");

        ExpandableDataProvider dataProvider = new ExpandableDataProvider(siteGroups);
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(null);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setInitiateOnTouch(false);
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);

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

        int lastSiteId = (int) SharedPreferencesUtil.getData(this, SettingFragment.KEY_LAST_SITE_ID, 0);
        Boolean openLastSite = (Boolean) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PRER_VIEW_REMLASTSITE, false);
        // 默认展开并选中上次打开的站点
        if (siteGroups.size() > 0 && siteGroups.get(0).second.size() > 0) {
            Site lastSite = null;
            int groupPos = 0;
            for (int i = 0; i < siteGroups.size(); i++) {
                Pair<SiteGroup, List<Site>> pair = siteGroups.get(i);
                for (int j = 0; j < pair.second.size(); j++) {
                    groupPos = i;
                    Site site = pair.second.get(j);
                    if (site.sid == lastSiteId) {
                        lastSite = site;
                        break;
                    }
                }
                if (lastSite != null)
                    break;
            }
            if (lastSite == null || !openLastSite) {
                groupPos = 0;
                lastSite = siteGroups.get(0).second.get(0);
            }
            mRecyclerViewExpandableItemManager.expandGroup(groupPos);
            selectSite(lastSite);
        }

        siteAdapter.setOnItemClickListener(new SiteAdapter.OnItemClickListener() {
            @Override
            public void onGroupClick(View v, int groupPosition) {
                // 点击分类（如果是新建按钮则创建，否则展开）
                if (groupPosition == siteAdapter.getGroupCount() - 1) {
                    View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
                    MaterialEditText inputGroupTitle = (MaterialEditText) view.findViewById(R.id.input_text);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("新建组名")
                            .setView(view)
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", (dialog, which) -> {
                                String title = inputGroupTitle.getText().toString();
                                SiteGroup group = new SiteGroup(0, title);
                                int gid = siteHolder.addSiteGroup(group);
                                group.gid = gid;
                                group.index = gid;
                                siteHolder.updateSiteGroupIndex(group);
                                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                                siteAdapter.notifyDataSetChanged();
                            }).show();
                } else {
                    notifyGroupItemChanged(groupPosition);
                }
            }

            @Override
            public boolean onGroupLongClick(View v, final int groupPosition) {
                if (mRecyclerViewDragDropManager.isDragging())
                    return true;
                // 分类上长按，选择操作
                final SiteGroup group = siteAdapter.getDataProvider().getGroupItem(groupPosition);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("操作")
                        .setItems(new String[]{"重命名", "删除"}, (dialogInterface, i) -> {
                            if (i == 0) {
                                View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
                                MaterialEditText inputGroupTitle = (MaterialEditText) view.findViewById(R.id.input_text);
                                new AlertDialog.Builder(MainActivity.this).setTitle("重命名组")
                                        .setView(view)
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            String title = inputGroupTitle.getText().toString();
                                            group.title = title;
                                            siteHolder.updateSiteGroup(group);
                                            siteAdapter.notifyDataSetChanged();
                                        }).show();
                            } else if (i == 1) {
                                if (groupPosition < siteAdapter.getDataProvider().getGroupCount()) {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("是否删除？")
                                            .setMessage("删除后将无法恢复")
                                            .setNegativeButton("取消", null)
                                            .setPositiveButton("确定", (dialog, which) -> {
                                                siteHolder.deleteSiteGroup(group);
                                                siteAdapter.getDataProvider().removeGroupItem(groupPosition);
                                                siteAdapter.notifyDataSetChanged();
                                            }).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }

            @Override
            public void onItemClick(View v, int groupPosition, int childPosition) {
                // 点击站点
                Site site = siteAdapter.getDataProvider().getChildItem(groupPosition, childPosition);
                setTitle(site.title);
                new Handler().postDelayed(() -> selectSite(site), 300);
                notifyChildItemChanged(groupPosition, childPosition);
                drawer.closeDrawer(GravityCompat.START);
            }

            @Override
            public boolean onItemLongClick(View v, final int groupPosition, final int childPosition) {
                // 长按站点，选择操作
                final Site site = siteAdapter.getDataProvider().getChildItem(groupPosition, childPosition);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("操作")
                        .setItems(new String[]{"登录", "编辑", "删除"}, (dialogInterface, i) -> {
                            if (i == 0) {
                                temp = site;
                                new Handler().postDelayed(() -> {
                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivityForResult(intent, RESULT_LOGIN);
                                }, 300);
                            } else if (i == 1) {
                                new Handler().postDelayed(() -> {
                                    temp = site;
                                    Intent intent = new Intent(MainActivity.this, ModifySiteActivity.class);
                                    startActivityForResult(intent, RESULT_MODIFY_SITE);
                                }, 300);
                            } else if (i == 2) {
                                new AlertDialog.Builder(MainActivity.this).setTitle("是否删除？")
                                        .setMessage("删除后将无法恢复")
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            siteHolder.deleteSite(site);
                                            siteAdapter.getDataProvider().removeChildItem(groupPosition, childPosition);
                                            siteAdapter.notifyDataSetChanged();
                                        }).setNegativeButton("取消", null).show();
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
                int groupCount = siteAdapter.getGroupCount() - 1;
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
                int childCount = siteAdapter.getChildCount(groupPosition);
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

        siteAdapter.setOnCheckedChangeListener(right -> new Handler().postDelayed(() -> {
            if (right) {
                currFragment.getCurrSite().isGrid = true;
                currFragment.setRecyclerViewToGrid();
            } else {
                currFragment.getCurrSite().isGrid = false;
                currFragment.setRecyclerViewToList();
            }
            new Handler().postDelayed(() -> {
                siteHolder.updateSite(currFragment.getCurrSite());
                drawer.closeDrawer(GravityCompat.START);
            }, 200);
        }, 300));
    }

    private void initNavCategories() {
        ListDataProvider<Category> categoryProvider = new ListDataProvider<>(new ArrayList<Category>());
        categoryAdapter = new CategoryAdapter(categoryProvider);
        categoryAdapter.setOnItemClickListener((v, position) -> {
            Category category = (Category) categoryAdapter.getDataProvider().getItem(position);
            categoryAdapter.selectedCid = category.cid;
            categoryAdapter.notifyDataSetChanged();
            currFragment.onLoadUrl(category.url);
            drawer.closeDrawer(GravityCompat.END);
        });
        rvCategory.setAdapter(categoryAdapter);
    }

    private void initSearchSuggestions() {
        List<String> histories = HViewerApplication.searchHistoryHolder.getSearchHistory();
        List<String> suggestions = HViewerApplication.searchSuggestionHolder.getSearchSuggestion();
        List<String> mySuggestions = new ArrayList<>();
        mySuggestions.addAll(histories);
        mySuggestions.addAll(suggestions);
        mySuggestions = new ArrayList(new HashSet(mySuggestions));
        mySuggestions.removeAll(Collections.singleton(null));
        Collections.sort(mySuggestions, String.CASE_INSENSITIVE_ORDER);
        int size = mySuggestions.size();
        String[] kwStrings = new String[size];
        kwStrings = mySuggestions.toArray(kwStrings);
        final MySearchAdapter adapter = new MySearchAdapter(this, kwStrings);
        searchView.setAdapter(adapter);

        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String[] keywords = currQuery.toString().split(" ");
            String keyword = "";
            for (int i = 0; i < keywords.length - 1; i++)
                keyword += keywords[i] + " ";

            keyword += adapter.getItem(position);
            searchView.setQuery(keyword, false);
        });
        isSuggestionEmpty = false;
    }

    private void initSearchView() {
        final ViewPagerBottomSheetBehavior behavior = ViewPagerBottomSheetBehavior.from(bottomSheet);
        //appbar折叠时显示搜索按钮和搜索框，否则隐藏
        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                int size = toolbar.getMenu().size();
                if (state == State.COLLAPSED) {
                    if (size > 1)
                        toolbar.getMenu().getItem(size - 1).setVisible(true);
                    if (size > 2)
                        toolbar.getMenu().getItem(size - 2).setVisible(true);
                    if (!isAnimating() && searchView.isSearchOpen()) {
                        searchView.animate().alpha(1f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                searchView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                searchView.setVisibility(View.VISIBLE);
                            }
                        });
                        showBottomSheet(behavior, true);
                    }
                } else {
                    if (size > 1)
                        toolbar.getMenu().getItem(size - 1).setVisible(false);
                    if (size > 2)
                        toolbar.getMenu().getItem(size - 2).setVisible(false);
                    if (!isAnimating() && searchView.isSearchOpen()) {
                        searchView.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                searchView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                searchView.setVisibility(View.GONE);
                            }
                        });
                        showBottomSheet(behavior, false);
                    }
                }
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                showBottomSheet(behavior, true);
            }

            @Override
            public void onSearchViewClosed() {
                showBottomSheet(behavior, false);
            }
        });
        searchView.setSubmitOnClick(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                currQuery = keyword;
                if (!"".equals(keyword) && currFragment != null) {
                    currFragment.onSearch(keyword);
                    HViewerApplication.searchHistoryHolder.addSearchHistory(keyword);
                    ListDataProvider provider = historyTagAdapter.getDataProvider();
                    provider.addItem(new Tag(provider.getCount() + 1, keyword));
                    historyTagAdapter.notifyDataSetChanged();
                }
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
    }

    private void initBottomSheet() {
        //初始化BottomSheet
        List<View> views = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        View view1 = getLayoutInflater().inflate(R.layout.view_tag_list, null);
        views.add(view1);
        titles.add("当前站点");
        View view2 = getLayoutInflater().inflate(R.layout.view_tag_list, null);
        views.add(view2);
        titles.add("收藏TAG");
        View view3 = getLayoutInflater().inflate(R.layout.view_tag_list, null);
        views.add(view3);
        titles.add("搜索历史");

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views, titles);
        bottomSheetViewPager.setAdapter(viewPagerAdapter);
        BottomSheetUtils.setupViewPager(bottomSheetViewPager);

        siteTagAdapter = new SiteTagAdapter(new ListDataProvider(new ArrayList<>()));
        favorTagAdapter = new SiteTagAdapter(new ListDataProvider(favorTagHolder.getTags()));
        historyTagAdapter = new SiteTagAdapter(new ListDataProvider(searchHistoryHolder.getSearchHistoryAsTag()));

        TagTabViewHolder siteTagTab = new TagTabViewHolder(view1, siteTagAdapter);
        TagTabViewHolder favorTagTab = new TagTabViewHolder(view2, favorTagAdapter);
        TagTabViewHolder historyTagTab = new TagTabViewHolder(view3, historyTagAdapter);

        siteTagTab.setButtonText(getString(R.string.search), getString(R.string.favorite), getString(R.string.refresh), getString(R.string.clear));
        favorTagTab.setButtonText(getString(R.string.search), getString(R.string.add), getString(R.string.delete), getString(R.string.clear));
        historyTagTab.setButtonText(getString(R.string.search), getString(R.string.favorite), getString(R.string.delete), getString(R.string.clear));

        siteTagTab.btnTag1.setOnClickListener(v -> siteTagTab.searchTags());
        siteTagTab.btnTag2.setOnClickListener(v -> siteTagTab.favorTags());
        siteTagTab.btnTag3.setOnClickListener(v -> siteTagTab.refreshTags(siteAdapter.selectedSid, siteTagHolder));
        siteTagTab.btnTag4.setOnClickListener(v -> siteTagTab.clearTags(siteAdapter.selectedSid, siteTagHolder));

        favorTagTab.btnTag1.setOnClickListener(v -> favorTagTab.searchTags());
        favorTagTab.btnTag2.setOnClickListener(v -> favorTagTab.addTags(siteAdapter.selectedSid, favorTagHolder));
        favorTagTab.btnTag3.setOnClickListener(v -> favorTagTab.deleteTags(siteAdapter.selectedSid, favorTagHolder));
        favorTagTab.btnTag4.setOnClickListener(v -> favorTagTab.clearTags(siteAdapter.selectedSid, favorTagHolder));

        historyTagTab.btnTag1.setOnClickListener(v -> historyTagTab.searchTags());
        historyTagTab.btnTag2.setOnClickListener(v -> historyTagTab.favorTags());
        historyTagTab.btnTag3.setOnClickListener(v -> historyTagTab.deleteTags(siteAdapter.selectedSid, searchHistoryHolder));
        historyTagTab.btnTag4.setOnClickListener(v -> historyTagTab.clearTags(siteAdapter.selectedSid, searchHistoryHolder));

        //底部TAG面板
        final ViewPagerBottomSheetBehavior behavior = ViewPagerBottomSheetBehavior.from(bottomSheet);
        //默认设置为隐藏
        behavior.setState(ViewPagerBottomSheetBehavior.STATE_HIDDEN);
        TabItemBuilder tabItem1 = new TabItemBuilder(this).create()
                .setDefaultColor(getResources().getColor(R.color.dimgray))
                .setSelectedColor(getResources().getColor(R.color.colorPrimaryDark))
                .setDefaultIcon(R.drawable.ic_add_black)
                .setText("当前站点")
                .setTag("currSite")
                .build();
        TabItemBuilder tabItem2 = new TabItemBuilder(this).create()
                .setDefaultColor(getResources().getColor(R.color.dimgray))
                .setSelectedColor(getResources().getColor(R.color.colorPrimaryDark))
                .setDefaultIcon(R.drawable.ic_favorite_border_white)
                .setText("收藏TAG")
                .setTag("favourite")
                .build();
        TabItemBuilder tabItem3 = new TabItemBuilder(this).create()
                .setDefaultColor(getResources().getColor(R.color.dimgray))
                .setSelectedColor(getResources().getColor(R.color.colorPrimaryDark))
                .setDefaultIcon(R.drawable.ic_history_white)
                .setText("搜索历史")
                .setTag("history")
                .build();
        TabStripBuild builder = pagerBottomTabLayout.builder();
        Controller controller = builder.addTabItem(tabItem1)
                .addTabItem(tabItem2)
                .addTabItem(tabItem3)
                .build();
        controller.addTabItemClickListener(new OnTabItemSelectListener() {
            @Override
            public void onSelected(int index, Object tag) {
                bottomSheetViewPager.setCurrentItem(index);
            }

            @Override
            public void onRepeatClick(int index, Object tag) {
            }
        });
        bottomSheetViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                controller.setSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        behavior.setBottomSheetCallback(new ViewPagerBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case ViewPagerBottomSheetBehavior.STATE_HIDDEN:
                    case ViewPagerBottomSheetBehavior.STATE_COLLAPSED:
                        setDrawerEnabled(true);
                        break;
                    case ViewPagerBottomSheetBehavior.STATE_DRAGGING:
                    case ViewPagerBottomSheetBehavior.STATE_EXPANDED:
                        setDrawerEnabled(false);
                        break;
                }
                Log.d("MainActivity", "newState:" + newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    private void showBottomSheet(ViewPagerBottomSheetBehavior behavior, boolean show) {
        if (show) {
            if (currFragment != null && siteAdapter.selectedSid != 0) {
                siteTagAdapter.getDataProvider().setDataSet(siteTagHolder.getRandomTags(siteAdapter.selectedSid, 30));
                siteTagAdapter.notifyDataSetChanged();
            }
            behavior.setState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED);
        } else {
            behavior.setState(ViewPagerBottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public void setTitle(String title) {
        collapsingToolbarLayout.setTitle(title);
    }

    public void replaceFragment(MyFragment fragment, String tag) {
        try {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment, tag)
                    .commit();
            currFragment = fragment;
        } catch (Exception e) {
            e.printStackTrace();
            showSnackBar(getString(R.string.site_loading_error));
        }
    }

    public void selectSite(Site site) {
        MyFragment fragment = CollectionFragment.newInstance(site, siteTagHolder);
        siteAdapter.selectedSid = site.sid;
        siteAdapter.notifyDataSetChanged();
        setTitle(site.title);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isGrid", site.isGrid);
        fragment.setArguments(bundle);
        replaceFragment(fragment, site.title);
        searchView.closeSearch();
        SharedPreferencesUtil.saveData(this, SettingFragment.KEY_LAST_SITE_ID, site.sid);

        if (site.categories != null && site.categories.size() > 0) {
            ListDataProvider<Category> dataProvider = new ListDataProvider<>(site.categories);
            categoryAdapter.setDataProvider(dataProvider);
            categoryAdapter.notifyDataSetChanged();
            isCategoryEnable = true;
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            Category category = site.categories.get(0);
            categoryAdapter.selectedCid = category.cid;
            categoryAdapter.notifyDataSetChanged();
            currFragment.onLoadUrl(category.url);
        } else {
            categoryAdapter.getDataProvider().clear();
            categoryAdapter.notifyDataSetChanged();
            isCategoryEnable = false;
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            currFragment.onLoadUrl(site.indexUrl);
        }
        if(site.hasFlag(Site.FLAG_LOGIN_REQUIRED) && TextUtils.isEmpty(site.cookie)){
            Toast.makeText(this, "该站点需要登录才能访问", Toast.LENGTH_SHORT);
            temp = site;
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, RESULT_LOGIN);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        //一开始隐藏搜索按钮
        item.setVisible(false);
        if (menu.size() > 1)
            menu.getItem(menu.size() - 1).setVisible(false);
        if (menu.size() > 2)
            menu.getItem(menu.size() - 2).setVisible(false);

        return true;
    }

    private void initSetDefultDownloadPath() {
        //下载路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            new AlertDialog.Builder(this)
                    .setTitle("请选择默认下载目录")
                    .setMessage("需要手动选择目录以获取读写权限")
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        SharedPreferencesUtil.saveData(MainActivity.this, KEY_FIRST_TIME, false);
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        try {
                            startActivityForResult(intent, RESULT_RDSQ);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }).setCancelable(false)
                    .show();
        }
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
            case R.id.action_jump_to_page:
                View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
                MaterialEditText inputJumpToPage = (MaterialEditText) view.findViewById(R.id.input_text);
                inputJumpToPage.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.jump_to_page)
                        .setView(view)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            String pageStr = inputJumpToPage.getText().toString();
                            int page = 0;
                            try {
                                page = Integer.parseInt(pageStr);
                            } catch (Exception e) {
                                page = 0;
                            }
                            if (currFragment != null)
                                currFragment.onJumpToPage(page);
                        }).show();
                return true;
            case R.id.action_search:
                search();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        new Handler().postDelayed(() -> startActivity(intent), 500);
        return true;
    }

    @OnClick(R.id.fab_search)
    void search() {
        if (!searchView.isSearchOpen()) {
            setAnimating(true);
            searchView.showSearch();
            new Handler().postDelayed(() -> setAnimating(false), 500);
            searchView.clearFocus();
        }
        appBar.setExpanded(false);
    }

    public void search(String keyword, boolean doSearch) {
        search();
        EditText editText = (EditText) searchView.getChildAt(0).findViewById(R.id.searchTextView);
        editText.setText(keyword);

        if (doSearch && currFragment != null)
            currFragment.onSearch(keyword);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ("search".equals(intent.getAction()) && currFragment != null) {
            Tag tag = (Tag) intent.getSerializableExtra("tag");
            Site currSite = currFragment.getCurrSite();
            if (tag.url != null && currSite != null) {
                String domin1 = RegexValidateUtil.getDominFromUrl(tag.url);
                String domin2 = RegexValidateUtil.getDominFromUrl(currSite.indexUrl);
                if (domin1.equals(domin2)) {
                    currFragment.onLoadUrl(tag.url);
                } else
                    currFragment.onSearch(tag.title);
            } else
                currFragment.onSearch(tag.title);
            HViewerApplication.searchHistoryHolder.addSearchHistory(tag.title);
            historyTagAdapter.setDataProvider(new ListDataProvider(HViewerApplication.searchHistoryHolder.getSearchHistoryAsTag()));
            historyTagAdapter.notifyDataSetChanged();
            search(tag.title, false);
            new Handler().postDelayed(() -> searchView.dismissSuggestions(), 200);
        }
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
                    final Runnable r = () -> selectSite(site);
                    handler.post(r);
                }
            } else if (requestCode == RESULT_MODIFY_SITE) {
                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                siteAdapter.notifyDataSetChanged();
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    Handler handler = new Handler();
                    final Runnable r = () -> selectSite(site);
                    handler.post(r);
                }
            } else if (requestCode == RESULT_LOGIN) {
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    siteHolder.updateSite(site);
                    Handler handler = new Handler();
                    final Runnable r = () -> selectSite(site);
                    handler.post(r);
                }
            } else if (requestCode == RESULT_SITE_MARKET) {
                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                siteAdapter.notifyDataSetChanged();
            } else if (requestCode == RESULT_SETTING) {
                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                siteAdapter.notifyDataSetChanged();
            } else if (requestCode == RESULT_RDSQ) {
                Uri uriTree = data.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uriTree, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                String path = uriTree.toString();
                SharedPreferencesUtil.saveData(this, KEY_PREF_DOWNLOAD_PATH, path);
            } else if (requestCode == RESULT_SET_HEADER_IMAGE) {
                Uri uri = data.getData();
                headerImageUri = uri;
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                        .setResizeOptions(new ResizeOptions(1080, 1920))
                        .disableDiskCache()
                        .build();
                supplier.setSupplier(Fresco.getImagePipeline().getDataSourceSupplier(request, this, ImageRequest.RequestLevel.FULL_FETCH));
                SharedPreferencesUtil.saveData(mContext, KEY_CUSTOM_HEADER_IMAGE, true);
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == RESULT_RDSQ) {
                showSnackBar(getString(R.string.authorization_failed));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else
            super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        mRecyclerViewDragDropManager.cancelDrag();
    }

    @Override
    public void onDestroy() {
        if (siteHolder != null)
            siteHolder.onDestroy();
        if (siteTagHolder != null)
            siteTagHolder.onDestroy();
        if (favorTagHolder != null)
            favorTagHolder.onDestroy();
        HViewerApplication.searchHistoryHolder.saveSearchHistory();
        HViewerApplication.searchSuggestionHolder.saveSearchSuggestion();

        if (mRecyclerViewDragDropManager != null) {
            downloadManager.setAllPaused();
            downloadManager.saveDownloadingTasks();
            downloadManager.unbindService(this);
            downloadManager = null;
        }

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

    @OnClick(R.id.btn_add_site)
    void addsite() {
        Intent intent = new Intent(MainActivity.this, AddSiteActivity.class);
        startActivityForResult(intent, RESULT_ADD_SITE);
    }

    @OnClick(R.id.btn_site_market)
    void openMarket() {
        drawer.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, MarketActivity.class);
            startActivityForResult(intent, RESULT_SITE_MARKET);
        }, 300);
    }

    @OnClick(R.id.btn_setting)
    void openSetting() {
        drawer.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivityForResult(intent, RESULT_SETTING);
        }, 300);
    }

    @OnClick(R.id.btn_exit)
    void exit() {
        finish();
    }

    class TagTabViewHolder {
        @BindView(R.id.label_view)
        AutoLabelUI labelView;
        @BindView(R.id.btn_tag_1)
        ButtonFlat btnTag1;
        @BindView(R.id.btn_tag_2)
        ButtonFlat btnTag2;
        @BindView(R.id.btn_tag_3)
        ButtonFlat btnTag3;
        @BindView(R.id.btn_tag_4)
        ButtonFlat btnTag4;

        private SiteTagAdapter mSiteTagAdapter;

        TagTabViewHolder(View view, SiteTagAdapter siteTagAdapter) {
            ButterKnife.bind(this, view);
            siteTagAdapter.setLabelView(labelView);
            mSiteTagAdapter = siteTagAdapter;
        }

        public void setButtonText(String text1, String text2, String text3, String text4) {
            btnTag1.setText(text1);
            btnTag2.setText(text2);
            btnTag3.setText(text3);
            btnTag4.setText(text4);
        }

        private void searchTags() {
            if (currFragment == null) return;
            List<Tag> tags = mSiteTagAdapter.getDataProvider().getItems();
            List<Tag> selectedTags = new ArrayList<>();
            for (Tag tag : tags) {
                if (tag.selected)
                    selectedTags.add(tag);
            }
            if (selectedTags.size() == 1) {
                Tag tag = selectedTags.get(0);
                Site currSite = currFragment.getCurrSite();
                if (tag.url != null && currSite != null) {
                    String domin1 = RegexValidateUtil.getDominFromUrl(tag.url);
                    String domin2 = RegexValidateUtil.getDominFromUrl(currFragment.getCurrSite().indexUrl);
                    if (domin1.equals(domin2)) {
                        currFragment.onLoadUrl(tag.url);
                    } else
                        currFragment.onSearch(tag.title);
                } else
                    currFragment.onSearch(tag.title);
                HViewerApplication.searchHistoryHolder.addSearchHistory(tag.title);
                historyTagAdapter.setDataProvider(new ListDataProvider(HViewerApplication.searchHistoryHolder.getSearchHistoryAsTag()));
                historyTagAdapter.notifyDataSetChanged();
                search(tag.title, false);
                new Handler().postDelayed(() -> searchView.dismissSuggestions(), 200);
            } else if (selectedTags.size() > 1) {
                String keyword = "";
                for (Tag tag : selectedTags) {
                    keyword += tag.title + " ";
                    HViewerApplication.searchHistoryHolder.addSearchHistory(tag.title);
                }
                historyTagAdapter.notifyDataSetChanged();
                keyword = keyword.trim();
                currFragment.onSearch(keyword);
                historyTagAdapter.getDataProvider().setDataSet(HViewerApplication.searchHistoryHolder.getSearchHistoryAsTag());
                historyTagAdapter.notifyDataSetChanged();
                search(keyword, true);
                new Handler().postDelayed(() -> searchView.dismissSuggestions(), 200);
            }
        }

        private void favorTags() {
            List<Tag> tags = mSiteTagAdapter.getDataProvider().getItems();
            for (Tag tag : tags) {
                if (tag.selected)
                    favorTagHolder.addTag(tag);
            }
            favorTagAdapter.getDataProvider().setDataSet(favorTagHolder.getTags(0));
            favorTagAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
        }

        private void addTags(int sid, AbstractTagHolder tagHolder) {
            View view = getLayoutInflater().inflate(R.layout.view_input_text, null);
            MaterialEditText inputTagTitle = (MaterialEditText) view.findViewById(R.id.input_text);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("TAG名")
                    .setView(view)
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        String title = inputTagTitle.getText().toString();
                        Tag tag = new Tag(0, title);
                        tagHolder.addTag(sid, tag);
                        refreshTags(sid, tagHolder);
                    }).show();
        }

        private void deleteTags(int sid, AbstractTagHolder tagHolder) {
            List<Tag> tags = mSiteTagAdapter.getDataProvider().getItems();
            for (Tag tag : tags) {
                if (tag.selected)
                    tagHolder.deleteTag(sid, tag);
            }
            refreshTags(sid, tagHolder);
        }

        private void refreshTags(int sid, AbstractTagHolder tagHolder) {
            mSiteTagAdapter.getDataProvider().setDataSet(tagHolder.getTags(sid));
            mSiteTagAdapter.notifyDataSetChanged();
        }

        private void clearTags(int sid, AbstractTagHolder tagHolder) {
            new AlertDialog.Builder(MainActivity.this).setTitle("是否清空？")
                    .setMessage("清空后将无法恢复")
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        tagHolder.clear(sid);
                        mSiteTagAdapter.getDataProvider().clear();
                        mSiteTagAdapter.notifyDataSetChanged();
                    }).setNegativeButton(getString(R.string.cancel), null).show();
        }
    }

}
