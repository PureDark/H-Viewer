package ml.puredark.hviewer.ui.activities;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.umeng.analytics.MobclickAgent;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Comment;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.adapters.CollectionTagAdapter;
import ml.puredark.hviewer.ui.adapters.CommentAdapter;
import ml.puredark.hviewer.ui.adapters.PictureAdapter;
import ml.puredark.hviewer.ui.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.ui.customs.AutoFitGridLayoutManager;
import ml.puredark.hviewer.ui.customs.AutoFitStaggeredGridLayoutManager;
import ml.puredark.hviewer.ui.customs.ExTabLayout;
import ml.puredark.hviewer.ui.customs.ExViewPager;
import ml.puredark.hviewer.ui.customs.SwipeBackOnPageChangeListener;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.DensityUtil;

import static ml.puredark.hviewer.configs.PasteEEConfig.url;

public class DownloadTaskActivity extends BaseActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.backdrop)
    ImageView backdrop;
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
    @BindView(R.id.fab_browser)
    FloatingActionButton fabBorwser;
    @BindView(R.id.fab_favor)
    FloatingActionButton fabFavor;
    @BindView(R.id.fab_download)
    FloatingActionButton fabDownload;

    private DownloadTask task;

    private PullLoadMoreRecyclerView rvIndex;
    private RecyclerView rvComment;

    private PictureAdapter pictureAdapter;
    private CommentAdapter commentAdapter;

    private CollectionViewHolder holder;

    private int startPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        ButterKnife.bind(this);
        MDStatusBarCompat.setCollapsingToolbar(this, coordinatorLayout, appBar, backdrop, toolbar);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
            lp.height = (int) (getResources().getDimension(R.dimen.tool_bar_height));
            lp.topMargin = MDStatusBarCompat.getStatusBarHeight(this);
            toolbar.setLayoutParams(lp);
        }

        setContainer(coordinatorLayout);

        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);
        setAppBar(appBar);
        setFabMenu(fabMenu);

        //获取传递过来的Collection实例
        if (HViewerApplication.temp instanceof DownloadTask)
            task = (DownloadTask) HViewerApplication.temp;

        //获取失败则结束此界面
        if (task == null || task.collection == null) {
            finish();
            return;
        }

        //解析URL模板
        parseUrl(task.collection.site.galleryUrl);

        toolbar.setTitle(task.collection.title);
        setSupportActionBar(toolbar);

        setCover(task.collection);

        initCover(task.collection.cover);
        initTabAndViewPager();
        refreshDescription();
        fabFavor.setVisibility(View.GONE);
        fabDownload.setVisibility(View.GONE);
    }

    private void parseUrl(String url) {
        String pageStr = RuleParser.parseUrl(url).get("page");
        try {
            if (pageStr == null) {
                startPage = 0;
            } else {
                String[] pageStrs = pageStr.split(":");
                if (pageStrs.length > 1) {
                    startPage = Integer.parseInt(pageStrs[0]);
                } else {
                    startPage = Integer.parseInt(pageStr);
                }
            }
        } catch (NumberFormatException e) {
            startPage = 0;
        }
    }

    private void initCover(String cover) {
        if (cover != null) {
            ImageLoader.loadImageFromUrl(this, backdrop, cover);
        }
    }

    private void setCover(LocalCollection collection) {
        if(collection==null)
            return;
        if (collection.pictures != null) {
            if(collection.pictures.size()>0)
                collection.cover = collection.pictures.get(0).thumbnail;
        }
    }

    private void initTabAndViewPager() {
        //初始化Tab和ViewPager
        List<View> views = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        titles.add("目录");
        View viewIndex = getLayoutInflater().inflate(R.layout.view_collection_index, null);
        views.add(viewIndex);
        titles.add("详情");
        View viewDescription = getLayoutInflater().inflate(R.layout.view_collection_desciption, null);
        views.add(viewDescription);
        View viewComment = null;
        if (commentEnabled()) {
            titles.add("评论");
            viewComment = getLayoutInflater().inflate(R.layout.view_collection_comment, null);
            views.add(viewComment);
        }

        holder = new CollectionViewHolder(viewDescription);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views, titles);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new SwipeBackOnPageChangeListener(this));

        //初始化相册目录
        rvIndex = (PullLoadMoreRecyclerView) viewIndex.findViewById(R.id.rv_index);
        pictureAdapter = new PictureAdapter(this, new ListDataProvider(task.collection.pictures));
        pictureAdapter.setCookie(task.collection.site.cookie);
        rvIndex.setAdapter(pictureAdapter);

        rvIndex.getRecyclerView().addOnScrollListener(new PictureAdapter.ScrollDetector(){
            @Override
            public void onScrollUp() {
                fabMenu.hideMenu(true);
            }

            @Override
            public void onScrollDown() {
                fabMenu.showMenu(true);
            }
        });

        rvIndex.getRecyclerView().setClipToPadding(false);
        rvIndex.getRecyclerView().setPadding(
                DensityUtil.dp2px(this, 8),
                DensityUtil.dp2px(this, 16),
                DensityUtil.dp2px(this, 8),
                DensityUtil.dp2px(this, 16));

        pictureAdapter.setOnItemClickListener((v, position) -> {
            HViewerApplication.temp = DownloadTaskActivity.this;
            HViewerApplication.temp2 = task.collection.site;
            HViewerApplication.temp3 = task.collection;
            HViewerApplication.temp4 = task.collection.pictures;
            Intent intent = new Intent(DownloadTaskActivity.this, PictureViewerActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        });

        //根据item宽度自动设置spanCount
        GridLayoutManager layoutManager = new AutoFitGridLayoutManager(this, DensityUtil.dp2px(this, 100));
        rvIndex.getRecyclerView().setLayoutManager(layoutManager);
        rvIndex.setPullRefreshEnable(false);
        rvIndex.setPushRefreshEnable(false);

        if (viewComment != null) {
            //初始化评论列表
            rvComment = (RecyclerView) viewComment.findViewById(R.id.rv_comment);
            List<Comment> comments = new ArrayList<>();
            commentAdapter = new CommentAdapter(this, new ListDataProvider(comments));
            commentAdapter.setCookie(task.collection.site.cookie);
            rvComment.setAdapter(commentAdapter);

            //禁用下拉刷新和加载更多（暂时）
//            rvComment.setPullRefreshEnable(false);
//            rvComment.setPushRefreshEnable(false);
        }
    }

    private boolean commentEnabled() {
        return task.collection.site.galleryRule.commentItem != null &&
                task.collection.site.galleryRule.commentAuthor != null &&
                task.collection.site.galleryRule.commentContent != null;
    }

    private void refreshDescription() {
        toolbar.setTitle(task.collection.title);
        holder.tvTitle.setText(task.collection.title);
        holder.tvUploader.setText(task.collection.uploader);
        holder.tvCategory.setText(task.collection.category);
        CollectionTagAdapter adapter = (CollectionTagAdapter) holder.rvTags.getAdapter();
        if (task.collection.tags != null) {
            adapter.getDataProvider().clear();
            adapter.getDataProvider().addAll(task.collection.tags);
        }
        adapter.notifyDataSetChanged();
        holder.rbRating.setRating(task.collection.rating);
        holder.tvSubmittime.setText(task.collection.datetime);
        if(task.collection.description!=null)
            holder.tvDescription.setText(RuleParser.getClickableHtml(this, task.collection.description, url, source -> new BitmapDrawable()));
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.fab_browser)
    void fab_browser() {
        final String url = task.collection.site.getGalleryUrl(task.collection.idCode, startPage);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);
        // 统计打开浏览器访问次数
        MobclickAgent.onEvent(HViewerApplication.mContext, "SwitchToBrowser");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        @BindView(R.id.tv_description)
        TextView tvDescription;

        public CollectionViewHolder(View view) {
            ButterKnife.bind(this, view);
            rvTags.setAdapter(
                    new CollectionTagAdapter(
                            new ListDataProvider<>(
                                    new ArrayList<Tag>()
                            )
                    )
            );
            StaggeredGridLayoutManager layoutManager =
                    new AutoFitStaggeredGridLayoutManager(getApplicationContext(), OrientationHelper.HORIZONTAL);
            rvTags.setLayoutManager(layoutManager);
            tvDescription.setAutoLinkMask(Linkify.EMAIL_ADDRESSES|Linkify.WEB_URLS);
            tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
