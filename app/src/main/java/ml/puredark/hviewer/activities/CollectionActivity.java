package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.clans.fab.FloatingActionMenu;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.activities.PictureViewerActivity.PicturePagerAdapter;
import ml.puredark.hviewer.adapters.PictureAdapter;
import ml.puredark.hviewer.adapters.TagAdapter;
import ml.puredark.hviewer.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.customs.AutoFitGridLayoutManager;
import ml.puredark.hviewer.customs.AutoFitStaggeredGridLayoutManager;
import ml.puredark.hviewer.customs.ExTabLayout;
import ml.puredark.hviewer.customs.ExViewPager;
import ml.puredark.hviewer.customs.ScalingImageView;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.helpers.DownloadManager;
import ml.puredark.hviewer.helpers.FastBlur;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.helpers.RuleParser;
import ml.puredark.hviewer.utils.DensityUtil;


public class CollectionActivity extends AnimationActivity implements AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.backdrop)
    ScalingImageView backdrop;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    ExTabLayout tabLayout;
    @BindView(R.id.view_pager)
    ExViewPager viewPager;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.fab_menu)
    FloatingActionMenu fabMenu;

    private Site site;

    private Collection collection;

    private PullLoadMoreRecyclerView rvIndex;

    private PictureAdapter pictureAdapter;

    private PicturePagerAdapter picturePagerAdapter;

    private CollectionViewHolder holder;

    private int startPage;
    private int currPage;

    private boolean isIndexComplete = false;

    private DownloadManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        ButterKnife.bind(this);
        MDStatusBarCompat.setCollapsingToolbar(this, coordinatorLayout, appBar, backdrop, toolbar);

        setContainer(coordinatorLayout);

        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);
        setAppBar(appBar);
        setFabMenu(fabMenu);

        //获取传递过来的Collection实例
        if (HViewerApplication.temp instanceof Site)
            site = (Site) HViewerApplication.temp;
        if (HViewerApplication.temp2 instanceof Collection)
            collection = (Collection) HViewerApplication.temp2;

        //获取失败则结束此界面
        if (site == null || collection == null) {
            finish();
            return;
        }
        collection = new LocalCollection(collection, site);

        toolbar.setTitle(collection.title);
        setSupportActionBar(toolbar);

        manager = new DownloadManager(this);

        //解析URL模板
        Map<String, String> map = RuleParser.parseUrl(site.galleryUrl);
        String pageStr = map.get("page");
        try {
            startPage = (pageStr != null) ? Integer.parseInt(pageStr) : 0;
            currPage = startPage;
        } catch (NumberFormatException e) {
            startPage = 0;
            currPage = startPage;
        }

        initCover(collection.cover);
        initTabAndViewPager();
        refreshDescription();
        rvIndex.setRefreshing(true);
        getCollectionDetail(startPage);

        //加入历史记录
        HViewerApplication.historyHolder.addHistory((LocalCollection) collection);
    }

    private void initCover(String cover) {
        if (cover != null && !CollectionActivity.this.isDestroyed())
            Glide.with(CollectionActivity.this).load(cover).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            /* 给背景封面加上高斯模糊 */
                            final Bitmap overlay = FastBlur.doBlur(resource.copy(Bitmap.Config.ARGB_8888, true), 2, true);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    backdrop.setImageBitmap(overlay);
                                    /* 让背景的封面大图来回缓慢移动 */
                                    float targetY = (overlay.getHeight() > overlay.getWidth()) ? -0.4f : 0f;
                                    Animation translateAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                            TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                            TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                            TranslateAnimation.RELATIVE_TO_SELF, targetY);
                                    translateAnimation.setDuration(50000);
                                    translateAnimation.setRepeatCount(-1);
                                    translateAnimation.setRepeatMode(Animation.REVERSE);
                                    translateAnimation.setInterpolator(new LinearInterpolator());
                                    backdrop.startAnimation(translateAnimation);
                                }
                            });
                        }
                    }).start();
                }
            });
    }

    private void initTabAndViewPager() {
        //初始化Tab和ViewPager
        List<View> views = new ArrayList<>();
        View viewIndex = getLayoutInflater().inflate(R.layout.view_collection_index, null);
        View viewDescription = getLayoutInflater().inflate(R.layout.view_collection_desciption, null);
        holder = new CollectionViewHolder(viewDescription);

        views.add(viewIndex);
        views.add(viewDescription);
        List<String> titles = new ArrayList<>();
        titles.add("目录");
        titles.add("相关");

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views, titles);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //初始化相册目录
        rvIndex = (PullLoadMoreRecyclerView) viewIndex.findViewById(R.id.rv_index);
        List<Picture> pictures = new ArrayList<>();
        pictureAdapter = new PictureAdapter(new ListDataProvider(pictures));
        pictureAdapter.setCookie(site.cookie);
        rvIndex.setAdapter(pictureAdapter);

        rvIndex.getRecyclerView().setClipToPadding(false);
        rvIndex.getRecyclerView().setPadding(
                DensityUtil.dp2px(this, 8),
                DensityUtil.dp2px(this, 16),
                DensityUtil.dp2px(this, 8),
                DensityUtil.dp2px(this, 16));

        pictureAdapter.setOnItemClickListener(new PictureAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                picturePagerAdapter = new PicturePagerAdapter(site, pictureAdapter.getDataProvider().getItems());
                HViewerApplication.temp = picturePagerAdapter;
                Intent intent = new Intent(CollectionActivity.this, PictureViewerActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        //根据item宽度自动设置spanCount
        GridLayoutManager layoutManager = new AutoFitGridLayoutManager(this, DensityUtil.dp2px(this, 100));
        rvIndex.getRecyclerView().setLayoutManager(layoutManager);
        rvIndex.setPullRefreshEnable(true);
        rvIndex.setPushRefreshEnable(false);

        //下拉刷新和加载更多
        rvIndex.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                currPage = startPage;
                getCollectionDetail(currPage);
            }

            @Override
            public void onLoadMore() {
            }
        });

    }

    private void refreshDescription(){
        toolbar.setTitle(collection.title);
        holder.tvTitle.setText(collection.title);
        holder.tvUploader.setText(collection.uploader);
        holder.tvCategory.setText(collection.category);
        TagAdapter adapter = (TagAdapter) holder.rvTags.getAdapter();
        if (collection.tags != null) {
            adapter.getDataProvider().clear();
            adapter.getDataProvider().addAll(collection.tags);
        }
        adapter.notifyDataSetChanged();
        holder.rbRating.setRating(collection.rating);
        holder.tvSubmittime.setText(collection.datetime);
    }

    private void getCollectionDetail(final int page) {
        final String url = site.galleryUrl.replaceAll("\\{idCode:\\}", collection.idCode)
                .replaceAll("\\{page:" + startPage + "\\}", "" + page);
        HViewerHttpClient.get(url, site.getCookies(), new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String result) {
                collection = RuleParser.getCollectionDetail(collection, result, site.galleryRule, url);

                refreshDescription();

                if (collection.pictures != null && collection.pictures.size() > 0) {
                    if (page == startPage) {
                        pictureAdapter.getDataProvider().clear();
                        pictureAdapter.getDataProvider().addAll(collection.pictures);
                        pictureAdapter.notifyDataSetChanged();
                        if (picturePagerAdapter != null)
                            picturePagerAdapter.notifyDataSetChanged();
                        currPage = page;
                        getCollectionDetail(currPage + 1);
                    } else if (!pictureAdapter.getDataProvider().getItems().contains(collection.pictures.get(0))) {
                        pictureAdapter.getDataProvider().addAll(collection.pictures);
                        pictureAdapter.notifyDataSetChanged();
                        if (picturePagerAdapter != null)
                            picturePagerAdapter.notifyDataSetChanged();
                        currPage = page;
                        getCollectionDetail(currPage + 1);
                    } else {
                        isIndexComplete = true;
                        collection.pictures = pictureAdapter.getDataProvider().getItems();
                        rvIndex.setPullLoadMoreCompleted();
                    }
                } else {
                    isIndexComplete = true;
                    collection.pictures = pictureAdapter.getDataProvider().getItems();
                    rvIndex.setPullLoadMoreCompleted();
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                showSnackBar(error.getErrorString());
                rvIndex.setPullLoadMoreCompleted();
            }
        });
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.fab_favor)
    void favor() {
        HViewerApplication.favouriteHolder.addFavourite((LocalCollection) collection);
        showSnackBar("收藏成功！");
    }

    @OnClick(R.id.fab_download)
    void download() {
        if (isIndexComplete) {
            if (!manager.createDownloadTask((LocalCollection) collection))
                showSnackBar("下载任务已在列表中！");
            else
                showSnackBar("下载任务已添加");
        } else {
            showSnackBar("请等待目录加载完毕再下载！");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.unbindService(this);
    }

    public class CollectionViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_uploader)
        TextView tvUploader;
        @BindView(R.id.tv_category)
        TextView tvCategory;
        @BindView(R.id.rv_tags)
        RecyclerView rvTags;
        @BindView(R.id.rb_rating)
        RatingBar rbRating;
        @BindView(R.id.tv_submittime)
        TextView tvSubmittime;

        public CollectionViewHolder(View view) {
            ButterKnife.bind(this, view);
            rvTags.setAdapter(
                    new TagAdapter(
                            new ListDataProvider<>(
                                    new ArrayList<Tag>()
                            )
                    )
            );
            StaggeredGridLayoutManager layoutManager =
                    new AutoFitStaggeredGridLayoutManager(getApplicationContext(), OrientationHelper.HORIZONTAL);
            rvTags.setLayoutManager(layoutManager);
        }

    }
}
