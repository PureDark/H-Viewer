package ml.puredark.hviewer.ui.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.umeng.analytics.MobclickAgent;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.adapters.PicturePagerAdapter;
import ml.puredark.hviewer.ui.adapters.PictureViewerAdapter;
import ml.puredark.hviewer.ui.customs.AreaClickHelper;
import ml.puredark.hviewer.ui.customs.MultiTouchViewPager;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;
import ml.puredark.hviewer.utils.DensityUtil;
import ml.puredark.hviewer.utils.FileType;
import ml.puredark.hviewer.utils.FileUtils;
import ml.puredark.hviewer.utils.RegexValidateUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_LEFT_TO_RIGHT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_RIGHT_TO_LEFT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_TOP_TO_BOTTOM;


public class PictureViewerActivity extends BaseActivity {

    public final static int RESULT_CHOOSE_DIRECTORY = 1;
    private static int ACTION_SAVE = 0;
    private static int ACTION_SHARE = 1;
    private static int ACTION_SHOW_INFO = 2;
    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.view_pager)
    MultiTouchViewPager viewPager;
    @BindView(R.id.rv_picture)
    RecyclerView rvPicture;
    @BindView(R.id.bottom_bar)
    LinearLayout bottomBar;
    @BindView(R.id.btn_load_high_res)
    ImageView btnLoadHighRes;
    @BindView(R.id.btn_rotate_screen)
    ImageView btnRotateScreen;
    @BindView(R.id.btn_picture_info)
    ImageView btnPictureInfo;
    InfoDialogViewHolder viewHolder;
    private boolean volumeKeyEnabled = false;
    private String viewDirection = DIREACTION_LEFT_TO_RIGHT;
    private CollectionActivity collectionActivity;
    private PicturePagerAdapter picturePagerAdapter;
    private PictureViewerAdapter pictureViewerAdapter;
    private Site site = null;
    private Collection collection = null;
    private List<Picture> pictures = null;
    private MyOnItemLongClickListener onItemLongClickListener;
    private int currPos = 0;
    private boolean move;
    private int mIndex;
    private Map<Integer, Pair<Picture, Object>> pictureInQueue = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);
        ButterKnife.bind(this);
        MDStatusBarCompat.setImageTransparent(this);
        setContainer(container);

        // 关闭边缘滑动返回
        setSwipeBackEnable(false);

        if (HViewerApplication.temp instanceof CollectionActivity)
            collectionActivity = (CollectionActivity) HViewerApplication.temp;

        if (HViewerApplication.temp2 instanceof Site)
            site = (Site) HViewerApplication.temp2;
        if (HViewerApplication.temp3 instanceof Collection)
            collection = (Collection) HViewerApplication.temp3;
        if (HViewerApplication.temp4 instanceof List)
            pictures = (List<Picture>) HViewerApplication.temp4;

        if (site == null || collection == null || pictures == null) {
            Toast.makeText(this, "数据错误，请刷新后重试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (collectionActivity != null)
            collectionActivity.setPictureViewerActivity(this);
        HViewerApplication.temp = null;
        HViewerApplication.temp2 = null;
        HViewerApplication.temp3 = null;
        HViewerApplication.temp4 = null;

        volumeKeyEnabled = (boolean) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_VOLUME_FLICK, true);

        currPos = getIntent().getIntExtra("position", 0);

        onItemLongClickListener = new MyOnItemLongClickListener();

        initViewDirection();
        initBottomBar();

    }

    private void initViewDirection() {
        viewDirection = (String) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_DIRECTION, DIREACTION_LEFT_TO_RIGHT);
        if (!DIREACTION_LEFT_TO_RIGHT.equals(viewDirection)
                && !DIREACTION_RIGHT_TO_LEFT.equals(viewDirection)
                && !DIREACTION_TOP_TO_BOTTOM.equals(viewDirection))
            viewDirection = DIREACTION_LEFT_TO_RIGHT;

        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection) || DIREACTION_RIGHT_TO_LEFT.equals(viewDirection)) {
            viewPager.setVisibility(View.VISIBLE);
            rvPicture.setVisibility(View.GONE);
            picturePagerAdapter = new PicturePagerAdapter(this, site, pictures);
            picturePagerAdapter.setViewDirection(viewDirection);
            picturePagerAdapter.setOnItemLongClickListener(onItemLongClickListener);
            picturePagerAdapter.setAreaClickListener(new AreaClickHelper.OnLeftRightClickListener() {
                @Override
                public void left() {
                    if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection))
                        prevPage(false);
                    else
                        nextPage(false);
                }

                @Override
                public void right() {
                    if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection))
                        nextPage(false);
                    else
                        prevPage(false);
                }

                @Override
                public void center() {
                    toogleStatus();
                    if (isStatusBarEnabled()) {
                        Animation animation = AnimationUtils.loadAnimation(PictureViewerActivity.this, R.anim.bottom_bar_show_from_bottom);
                        animation.setFillAfter(true);
                        bottomBar.startAnimation(animation);
                    } else {
                        Animation animation = AnimationUtils.loadAnimation(PictureViewerActivity.this, R.anim.bottom_bar_hide_to_bottom);
                        animation.setFillAfter(true);
                        bottomBar.startAnimation(animation);
                    }
                }
            });

            int position = picturePagerAdapter.getPicturePostion(currPos);
            tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());
            if (position < pictures.size() && pictures.size() > 0) {
                Picture picture = (position < 0) ? pictures.get(0) : pictures.get(position);
                if (TextUtils.isEmpty(picture.highRes) || picture.loadedHighRes)
                    btnLoadHighRes.setVisibility(View.GONE);
                else
                    btnLoadHighRes.setVisibility(View.VISIBLE);
            }
            viewPager.setAdapter(picturePagerAdapter);
            ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    currPos = position;
                    position = picturePagerAdapter.getPicturePostion(currPos);
                    tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());
                    if (position < pictures.size()) {
                        Picture picture = pictures.get(position);
                        if (TextUtils.isEmpty(picture.highRes) || picture.loadedHighRes)
                            btnLoadHighRes.setVisibility(View.GONE);
                        else
                            btnLoadHighRes.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            };
            viewPager.addOnPageChangeListener(listener);
            int limit = (int) SharedPreferencesUtil.getData(this,
                    SettingFragment.KEY_PREF_VIEW_PRELOAD_PAGES, 2);
            viewPager.setOffscreenPageLimit(limit);
            viewPager.setCurrentItem(position);
        } else if (DIREACTION_TOP_TO_BOTTOM.equals(viewDirection)) {
            viewPager.setVisibility(View.GONE);
            rvPicture.setVisibility(View.VISIBLE);
            ListDataProvider<Picture> dataProvider = new ListDataProvider<>(pictures);
            pictureViewerAdapter = new PictureViewerAdapter(this, site, dataProvider);
            pictureViewerAdapter.setOnItemLongClickListener(onItemLongClickListener);
            rvPicture.setAdapter(pictureViewerAdapter);
            rvPicture.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    //在这里进行第二次滚动（最后的100米！）
                    if (move) {
                        move = false;
                        //获取要置顶的项在当前屏幕的位置，mIndex是记录的要置顶项在RecyclerView中的位置
                        int n = mIndex - linearLayoutManager.findFirstVisibleItemPosition();
                        if (0 <= n && n < rvPicture.getChildCount()) {
                            //获取要置顶的项顶部离RecyclerView顶部的距离
                            int top = rvPicture.getChildAt(n).getTop();
                            //最后的移动
                            rvPicture.scrollBy(0, top);
                        }
                    }
                    currPos = linearLayoutManager.findLastVisibleItemPosition();
                    tvCount.setText((currPos + 1) + "/" + pictureViewerAdapter.getItemCount());
                    Picture picture = pictures.get(currPos);
                    if (TextUtils.isEmpty(picture.highRes) || picture.loadedHighRes)
                        btnLoadHighRes.setVisibility(View.GONE);
                    else
                        btnLoadHighRes.setVisibility(View.VISIBLE);
                }
            });
            moveToPosition(rvPicture, currPos);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvPicture.getLayoutManager();
            currPos = linearLayoutManager.findLastVisibleItemPosition();
            tvCount.setText((currPos + 1) + "/" + pictureViewerAdapter.getItemCount());
            if (currPos < pictures.size() && pictures.size() > 0) {
                Picture picture = (currPos < 0) ? pictures.get(0) : pictures.get(currPos);
                if (TextUtils.isEmpty(picture.highRes) || picture.loadedHighRes)
                    btnLoadHighRes.setVisibility(View.GONE);
                else
                    btnLoadHighRes.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initBottomBar() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_picture_exif, null);
        viewHolder = new InfoDialogViewHolder(view);
        Dialog dialog = new AlertDialog.Builder(PictureViewerActivity.this)
                .setView(view)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        //设置对话框位置
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = DensityUtil.getScreenWidth(this) - DensityUtil.dp2px(this, 64);
        dialog.getWindow().setAttributes(lp);

        viewHolder.btnConfirm.setOnClickListener(v -> dialog.dismiss());

        btnLoadHighRes.setOnClickListener(v -> {
            if (pictures != null && currPos >= 0 && currPos < pictures.size()) {
                Picture picture = null;
                if (picturePagerAdapter != null) {
                    int position = picturePagerAdapter.getPicturePostion(currPos);
                    if (position < pictures.size()) {
                        picture = pictures.get(position);
                        PicturePagerAdapter.PictureViewHolder picVH = picturePagerAdapter.getViewHolderAt(currPos);
                        getUrlAndLoadImage(picVH, picture, true);
                        btnLoadHighRes.setVisibility(View.GONE);
                    } else {
                        showSnackBar("无法加载原图");
                        return;
                    }
                } else if (pictureViewerAdapter != null) {
                    if (pictures.size() > 0) {
                        for (int pos = 0; pos < pictures.size(); pos++) {
                            picture = pictures.get(pos);
                            picture.loadedHighRes = true;
                        }
                        btnLoadHighRes.setVisibility(View.GONE);
                        pictureViewerAdapter.notifyDataSetChanged();
                    } else {
                        showSnackBar("无法加载原图");
                        return;
                    }
                }
            }
        });

        btnRotateScreen.setOnClickListener(v -> {
            int width = DensityUtil.getScreenWidth(this);
            int height = DensityUtil.getScreenHeight(this);
            if (height > width)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        btnPictureInfo.setOnClickListener(v -> {
            viewHolder.tvImageType.setText("");
            viewHolder.tvFileSize.setText("");
            viewHolder.tvImageSize.setText("");
            if (pictures != null && currPos >= 0 && currPos <= pictures.size()) {
                Picture picture = null;
                int position = 0;
                if (picturePagerAdapter != null) {
                    position = picturePagerAdapter.getPicturePostion(currPos);
                } else if (pictureViewerAdapter != null) {
                    position = currPos;
                }
                if (position < pictures.size()) {
                    picture = pictures.get(position);
                } else {
                    showSnackBar("图片未加载，请等待");
                    return;
                }
                if (picture.loadedHighRes)
                    viewHolder.iconHighRes.setVisibility(View.VISIBLE);
                else
                    viewHolder.iconHighRes.setVisibility(View.GONE);
                loadPicture(picture, "", ACTION_SHOW_INFO);
                dialog.show();
            }
        });
    }

    private void moveToPosition(RecyclerView recyclerView, int n) {
        mIndex = n;
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //先从RecyclerView的LayoutManager中获取第一项和最后一项的Position
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();
        //然后区分情况
        if (n <= firstItem) {
            //当要置顶的项在当前显示的第一个项的前面时
            recyclerView.scrollToPosition(n);
        } else if (n <= lastItem) {
            //当要置顶的项已经在屏幕上显示时
            int top = recyclerView.getChildAt(n - firstItem).getTop();
            recyclerView.scrollBy(0, top);
        } else {
            //当要置顶的项在当前显示的最后一项的后面时
            recyclerView.scrollToPosition(n);
            //这里这个变量是用在RecyclerView滚动监听里面的
            move = true;
        }
    }

    public void notifyDataSetChanged(List<Picture> pictures) {
        this.pictures = pictures;
        if (picturePagerAdapter != null) {
            picturePagerAdapter.pictures = pictures;
            picturePagerAdapter.notifyDataSetChanged();
            tvCount.setText((picturePagerAdapter.getPicturePostion(currPos) + 1) + "/" + picturePagerAdapter.getCount());
        }
        if (pictureViewerAdapter != null) {
            pictureViewerAdapter.getDataProvider().setDataSet(pictures);
            pictureViewerAdapter.notifyDataSetChanged();
            tvCount.setText((currPos + 1) + "/" + pictureViewerAdapter.getItemCount());
        }
    }

    private boolean viewHighRes() {
        return (boolean) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_HIGH_RES, false);
    }

    public void getUrlAndLoadImage(Object viewHolder, Picture picture, boolean loadHighRes) {
        if (picture.pic != null) {
            loadImage(picture, viewHolder, loadHighRes);
        } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null) {
            if (site.extraRule.pictureRule != null && site.extraRule.pictureRule.url != null)
                getPictureUrl(viewHolder, picture, site.extraRule.pictureRule.url, site.extraRule.pictureRule.highRes);
            else if (site.extraRule.pictureUrl != null)
                getPictureUrl(viewHolder, picture, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
        } else if (site.picUrlSelector != null) {
            getPictureUrl(viewHolder, picture, site.picUrlSelector, null);
        } else {
            picture.pic = picture.url;
            loadImage(picture, viewHolder, loadHighRes);
        }
    }

    public void loadImage(Picture picture, final Object viewHolder, boolean loadHighRes) {
        String url = ((loadHighRes || picture.loadedHighRes || viewHighRes()) && !TextUtils.isEmpty(picture.highRes)) ? picture.highRes : picture.pic;
        if (url.equals(picture.highRes))
            picture.loadedHighRes = true;
        if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE)) {
            String galleryRefererHost = RegexValidateUtil.getHostFromUrl(site.galleryUrl);
            String pictureRefererHost = RegexValidateUtil.getHostFromUrl(picture.referer);
            if(!galleryRefererHost.equals(pictureRefererHost))
                picture.referer = galleryRefererHost;
        }
        Logger.d("PictureViewerActivity", "url:" + url + "\n picture.referer:" + picture.referer);
        if (site == null) return;
        DraweeView draweeView;
        if (viewHolder instanceof PicturePagerAdapter.PictureViewHolder) {
            draweeView = ((PicturePagerAdapter.PictureViewHolder) viewHolder).ivPicture;
        } else if (viewHolder instanceof PictureViewerAdapter.PictureViewHolder) {
            draweeView = ((PictureViewerAdapter.PictureViewHolder) viewHolder).ivPicture;
        } else
            return;
        ImageLoader.loadImageFromUrl(this, draweeView, url, site.cookie, picture.referer, new BaseControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
                super.onSubmit(id, callerContext);
                if (viewHolder instanceof PicturePagerAdapter.PictureViewHolder) {
                    PicturePagerAdapter.PictureViewHolder pictureViewHolder = (PicturePagerAdapter.PictureViewHolder) viewHolder;
                    pictureViewHolder.progressBar.setVisibility(View.VISIBLE);
                    pictureViewHolder.btnRefresh.setVisibility(View.GONE);
                } else if (viewHolder instanceof PictureViewerAdapter.PictureViewHolder) {
                    PictureViewerAdapter.PictureViewHolder pictureViewHolder = (PictureViewerAdapter.PictureViewHolder) viewHolder;
                    pictureViewHolder.progressBar.setVisibility(View.VISIBLE);
                    pictureViewHolder.btnRefresh.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                super.onFinalImageSet(id, imageInfo, anim);
                if (imageInfo == null) {
                    return;
                }
                if (viewHolder instanceof PicturePagerAdapter.PictureViewHolder) {
                    PicturePagerAdapter.PictureViewHolder pictureViewHolder = (PicturePagerAdapter.PictureViewHolder) viewHolder;
                    pictureViewHolder.progressBar.setVisibility(View.GONE);
                    pictureViewHolder.btnRefresh.setVisibility(View.GONE);
                    pictureViewHolder.ivPicture.update(imageInfo.getWidth(), imageInfo.getHeight());
                } else if (viewHolder instanceof PictureViewerAdapter.PictureViewHolder) {
                    PictureViewerAdapter.PictureViewHolder pictureViewHolder = (PictureViewerAdapter.PictureViewHolder) viewHolder;
                    pictureViewHolder.progressBar.setVisibility(View.GONE);
                    pictureViewHolder.btnRefresh.setVisibility(View.GONE);
                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    final float factor = (float) imageInfo.getHeight() / imageInfo.getWidth();
                    final int originWidth = wm.getDefaultDisplay().getWidth();
                    final int originHeight = (int) (factor * originWidth);
                    pictureViewHolder.ivPicture.getLayoutParams().height = originHeight;
                    pictureViewHolder.ivPicture.requestLayout();
                    pictureViewHolder.ivPicture.update(imageInfo.getWidth(), imageInfo.getHeight());

                    pictureViewHolder.ivPicture.setOnScaleChangeListener((scaleFactor, focusX, focusY) -> {
                        float scale = pictureViewHolder.ivPicture.getScale();
                        if (scale > 1) {
                            pictureViewHolder.ivPicture.getLayoutParams().height = (int) (scale * originHeight) + 1;
                        } else {
                            pictureViewHolder.ivPicture.getLayoutParams().height = originHeight;
                        }
                        pictureViewHolder.ivPicture.requestLayout();
                    });
                    pictureViewHolder.ivPicture.setAllowParentInterceptOnEdge(true);
                }
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                FLog.e(getClass(), throwable, "Error loading %s", id);
                if (viewHolder instanceof PicturePagerAdapter.PictureViewHolder) {
                    PicturePagerAdapter.PictureViewHolder pictureViewHolder = (PicturePagerAdapter.PictureViewHolder) viewHolder;
                    pictureViewHolder.progressBar.setVisibility(View.GONE);
                    pictureViewHolder.btnRefresh.setVisibility(View.VISIBLE);
                } else if (viewHolder instanceof PictureViewerAdapter.PictureViewHolder) {
                    PictureViewerAdapter.PictureViewHolder pictureViewHolder = (PictureViewerAdapter.PictureViewHolder) viewHolder;
                    pictureViewHolder.progressBar.setVisibility(View.GONE);
                    pictureViewHolder.btnRefresh.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void getPictureUrl(final Object viewHolder, final Picture picture, final Selector selector, final Selector highResSelector) {
        Logger.d("PictureViewerActivity", "picture.url = " + picture.url);
        if (Picture.hasPicPosfix(picture.url)) {
            picture.pic = picture.url;
            loadImage(picture, viewHolder, false);
        } else
            //如果需要执行JS才能获取完整数据，则不得不使用webView来载入页面
            if (site.hasFlag(Site.FLAG_JS_NEEDED_ALL) || site.hasFlag(Site.FLAG_JS_NEEDED_PICTURE)) {
                WebView webView = new WebView(this);
                WebSettings mWebSettings = webView.getSettings();
                mWebSettings.setJavaScriptEnabled(true);
                mWebSettings.setBlockNetworkImage(true);
                mWebSettings.setDomStorageEnabled(true);
                mWebSettings.setUserAgentString(getResources().getString(R.string.UA));
                mWebSettings.setCacheMode(LOAD_CACHE_ELSE_NETWORK);
                webView.addJavascriptInterface(this, "HtmlParser");

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        //Load HTML
                        pictureInQueue.put(picture.pid, new Pair<>(picture, viewHolder));
                        boolean extra = !selector.equals(site.picUrlSelector);
                        webView.loadUrl("javascript:window.HtmlParser.onResultGot(document.documentElement.outerHTML, " + picture.pid + ", " + extra + ");");
                        Logger.d("PictureViewerActivity", "onPageFinished");
                    }
                });
                webView.loadUrl(picture.url);
                new Handler().postDelayed(() -> webView.stopLoading(), 30000);
                Logger.d("PictureViewerActivity", "WebView");
            } else
                HViewerHttpClient.get(picture.url, site.disableHProxy, site.getHeaders(), site.hasFlag(Site.FLAG_POST_PICTURE), new HViewerHttpClient.OnResponseListener() {

                    @Override
                    public void onSuccess(String contentType, Object result) {
                        if (result == null || result.equals(""))
                            return;
                        if (contentType.contains("image")) {
                            picture.pic = picture.url;
                            if (result instanceof Bitmap) {
                                if (viewHolder instanceof PicturePagerAdapter.PictureViewHolder) {
                                    PicturePagerAdapter.PictureViewHolder pictureViewHolder = (PicturePagerAdapter.PictureViewHolder) viewHolder;
                                    pictureViewHolder.ivPicture.setImageBitmap((Bitmap) result);
                                    pictureViewHolder.progressBar.setVisibility(View.GONE);
                                } else if (viewHolder instanceof PictureViewerAdapter.PictureViewHolder) {
                                    PictureViewerAdapter.PictureViewHolder pictureViewHolder = (PictureViewerAdapter.PictureViewHolder) viewHolder;
                                    pictureViewHolder.ivPicture.setImageBitmap((Bitmap) result);
                                    pictureViewHolder.progressBar.setVisibility(View.GONE);
                                }
                            } else {
                                loadImage(picture, viewHolder, false);
                            }
                        } else {
                            picture.pic = RuleParser.getPictureUrl((String) result, selector, picture.url);
                            picture.highRes = RuleParser.getPictureUrl((String) result, highResSelector, picture.url);
                            Logger.d("PictureViewerActivity", "getPictureUrl: picture.pic: " + picture.pic);
                            Logger.d("PictureViewerActivity", "getPictureUrl: picture.highRes: " + picture.highRes);
                            if (picture.pic != null) {
                                picture.retries = 0;
                                picture.referer = picture.url;
                                loadImage(picture, viewHolder, false);
                            } else {
                                onFailure(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(HViewerHttpClient.HttpError error) {
                        if (picture.retries < 15) {
                            picture.retries++;
                            getPictureUrl(viewHolder, picture, selector, highResSelector);
                        } else {
                            picture.retries = 0;
                            if (viewHolder instanceof PicturePagerAdapter.PictureViewHolder) {
                                PicturePagerAdapter.PictureViewHolder pictureViewHolder = (PicturePagerAdapter.PictureViewHolder) viewHolder;
                                pictureViewHolder.progressBar.setVisibility(View.GONE);
                                pictureViewHolder.btnRefresh.setVisibility(View.VISIBLE);
                            } else if (viewHolder instanceof PictureViewerAdapter.PictureViewHolder) {
                                PictureViewerAdapter.PictureViewHolder pictureViewHolder = (PictureViewerAdapter.PictureViewHolder) viewHolder;
                                pictureViewHolder.progressBar.setVisibility(View.GONE);
                                pictureViewHolder.btnRefresh.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    @JavascriptInterface
    public void onResultGot(String html, int pid, boolean extra) {
        Pair<Picture, Object> pair = pictureInQueue.get(pid);
        if (pair == null)
            return;
        Picture picture = pair.first;
        Object viewHolder = pair.second;
        if (picture == null || viewHolder == null)
            return;
        pictureInQueue.remove(pid);
        Selector selector = (extra) ? (site.extraRule.pictureRule != null) ? site.extraRule.pictureRule.url : site.extraRule.pictureUrl : site.picUrlSelector;
        Selector highResSelector = (extra) ? (site.extraRule.pictureRule != null) ? site.extraRule.pictureRule.highRes : site.extraRule.pictureHighRes : null;
        picture.pic = RuleParser.getPictureUrl(html, selector, picture.url);
        picture.highRes = RuleParser.getPictureUrl(html, highResSelector, picture.url);
        Logger.d("PicturePagerAdapter", "getPictureUrl: picture.pic: " + picture.pic);
        Logger.d("PicturePagerAdapter", "getPictureUrl: picture.highRes: " + picture.highRes);
        if (picture.pic != null) {
            picture.retries = 0;
            picture.referer = picture.url;
            new Handler(Looper.getMainLooper()).post(() -> loadImage(picture, viewHolder, false));
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (picture.retries < 15) {
                    picture.retries++;
                    getPictureUrl(viewHolder, picture, selector, highResSelector);
                } else {
                    picture.retries = 0;
                    if (viewHolder instanceof PicturePagerAdapter.PictureViewHolder) {
                        PicturePagerAdapter.PictureViewHolder pictureViewHolder = (PicturePagerAdapter.PictureViewHolder) viewHolder;
                        pictureViewHolder.progressBar.setVisibility(View.GONE);
                        pictureViewHolder.btnRefresh.setVisibility(View.VISIBLE);
                    } else if (viewHolder instanceof PictureViewerAdapter.PictureViewHolder) {
                        PictureViewerAdapter.PictureViewHolder pictureViewHolder = (PictureViewerAdapter.PictureViewHolder) viewHolder;
                        pictureViewHolder.progressBar.setVisibility(View.GONE);
                        pictureViewHolder.btnRefresh.setVisibility(View.VISIBLE);
                    }
                }
            });

        }
    }

    private void loadPicture(final Picture picture, final String path, int action) {
        String url = (picture.loadedHighRes && !TextUtils.isEmpty(picture.highRes)) ? picture.highRes : picture.pic;
        if (url != null && (url.startsWith("file://"))) {
            if (action == ACTION_SHARE) {
                Uri uri = Uri.parse(url);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");
                startActivity(Intent.createChooser(shareIntent, "将图片分享到"));
                MobclickAgent.onEvent(this, "ShareSinglePicture");
            } else if (action == ACTION_SHOW_INFO) {
                Uri uri = Uri.parse(url);
                viewHolder.tvImageType.setText(FileUtils.getMimeType(this, uri));
                viewHolder.tvFileSize.setText(FileUtils.getReadableFileSize((int) SimpleFileUtil.getFileSize(new File(uri.getPath()))));
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                opts.inSampleSize = 1;
                BitmapFactory.decodeFile(uri.getPath(), opts);
                int width = opts.outWidth;
                int height = opts.outHeight;
                viewHolder.tvImageSize.setText(width + " × " + height);
            }
        } else if (url != null && url.startsWith("content://")) {
            ImageLoader.loadBitmapFromUrl(this, url, site.cookie, picture.referer, new BaseBitmapDataSubscriber() {
                @Override
                protected void onNewResultImpl(Bitmap bitmap) {
                    Logger.d("PictureViewerActivity", "onNewResultImpl");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] bytes = baos.toByteArray();
                    if (action == ACTION_SAVE)
                        savePicture(path, bytes, false);
                    else if (action == ACTION_SHARE)
                        savePicture(path, bytes, true);
                    else if (action == ACTION_SHOW_INFO) {
                        runOnUiThread(() -> {
                            String postfix = FileType.getFileType(bytes, FileType.TYPE_IMAGE);
                            viewHolder.tvImageType.setText(MimeTypeMap.getSingleton().getMimeTypeFromExtension(postfix));
                            viewHolder.tvFileSize.setText(FileUtils.getReadableFileSize(bytes.length));
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            viewHolder.tvImageSize.setText(width + " × " + height);
                        });
                    }
                }

                @Override
                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                    Logger.d("PictureViewerActivity", "onFailureImpl");
                }
            });
        } else {
            ImageLoader.loadResourceFromUrl(this, url, site.cookie, picture.referer,
                    new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
                        @Override
                        protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                            Logger.d("PictureViewerActivity", "onNewResultImpl");
                            if (!dataSource.isFinished()) {
                                return;
                            }
                            CloseableReference<PooledByteBuffer> ref = dataSource.getResult();
                            if (ref != null) {
                                try {
                                    PooledByteBuffer imageBuffer = ref.get();
                                    byte[] bytes = new byte[imageBuffer.size()];
                                    imageBuffer.read(0, bytes, 0, imageBuffer.size());
                                    if (action == ACTION_SAVE)
                                        savePicture(path, bytes, false);
                                    else if (action == ACTION_SHARE)
                                        savePicture(path, bytes, true);
                                    else if (action == ACTION_SHOW_INFO) {
                                        runOnUiThread(() -> {
                                            String postfix = FileType.getFileType(bytes, FileType.TYPE_IMAGE);
                                            viewHolder.tvImageType.setText(MimeTypeMap.getSingleton().getMimeTypeFromExtension(postfix));
                                            viewHolder.tvFileSize.setText(FileUtils.getReadableFileSize(bytes.length));
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            if (bitmap != null) {
                                                int width = bitmap.getWidth();
                                                int height = bitmap.getHeight();
                                                viewHolder.tvImageSize.setText(width + " × " + height);
                                            }
                                        });
                                    }
                                } finally {
                                    CloseableReference.closeSafely(ref);
                                }
                            }
                        }

                        @Override
                        protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                            Logger.d("PictureViewerActivity", "onFailureImpl");
                        }
                    }
            );
        }
    }

    private void savePicture(String path, byte[] bytes, boolean share) {
        try {
            String postfix = FileType.getFileType(bytes, FileType.TYPE_IMAGE);
            String fileName;
            if (share) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    path = Environment.getExternalStorageDirectory().getAbsolutePath();
                fileName = "tempImage";
            } else {
                int i = 1;
                do {
                    fileName = Uri.encode(site.title + "_" + FileHelper.filenameFilter(collection.idCode) + "_" + (i++) + "." + postfix);
                } while (FileHelper.isFileExist(fileName, path));
            }
            DocumentFile documentFile = FileHelper.createFileIfNotExist(fileName, path);
            if (FileHelper.writeBytes(bytes, documentFile)) {
                if (share) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, documentFile.getUri());
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "将图片分享到"));
                    MobclickAgent.onEvent(this, "ShareSinglePicture");
                } else {
                    showSnackBar("保存成功");
                    // 统计保存单图次数
                    MobclickAgent.onEvent(this, "SaveSinglePicture");
                }
            } else {
                showSnackBar("保存失败，请重新设置下载目录");
            }
        } catch (OutOfMemoryError error) {
            showSnackBar("保存失败，内存不足");
        }
    }

    // 监听音量键，实现翻页
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (volumeKeyEnabled)
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    nextPage(false);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    prevPage(false);
                    return true;
            }
        return super.onKeyDown(keyCode, event);
    }

    // 监听音量键，消除按键音
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (volumeKeyEnabled)
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return true;
            }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 屏幕旋转时调用此方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (picturePagerAdapter != null)
            picturePagerAdapter.onConfigurationChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_CHOOSE_DIRECTORY) {
                Uri uriTree = data.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uriTree, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                onItemLongClickListener.onSelectDirectory(uriTree);
            }
        }
    }

    private void prevPage(boolean anim) {
        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem > 0)
                viewPager.setCurrentItem(currItem - 1, anim);
        } else if (DIREACTION_RIGHT_TO_LEFT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem + 1 < viewPager.getAdapter().getCount())
                viewPager.setCurrentItem(currItem + 1, anim);
        } else if (DIREACTION_TOP_TO_BOTTOM.equals(viewDirection) && pictureViewerAdapter != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) rvPicture.getLayoutManager();
            int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstItemPosition > 0) {
                moveToPosition(rvPicture, firstItemPosition - 1);
            }
        }
    }

    private void nextPage(boolean anim) {
        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem + 1 < viewPager.getAdapter().getCount())
                viewPager.setCurrentItem(currItem + 1, anim);
        } else if (DIREACTION_RIGHT_TO_LEFT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem > 0)
                viewPager.setCurrentItem(currItem - 1, anim);
        } else if (DIREACTION_TOP_TO_BOTTOM.equals(viewDirection) && pictureViewerAdapter != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) rvPicture.getLayoutManager();
            int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstItemPosition + 1 < pictureViewerAdapter.getItemCount()) {
                moveToPosition(rvPicture, firstItemPosition + 1);
            }
        }
    }

    public static class InfoDialogViewHolder {
        @BindView(R.id.tv_image_type)
        TextView tvImageType;
        @BindView(R.id.tv_file_size)
        TextView tvFileSize;
        @BindView(R.id.tv_image_size)
        TextView tvImageSize;
        @BindView(R.id.btn_confirm)
        TextView btnConfirm;
        @BindView(R.id.icon_high_res)
        ImageView iconHighRes;

        InfoDialogViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private class MyOnItemLongClickListener implements OnItemLongClickListener {
        private DirectoryChooserFragment mDialog;
        private String lastPath = DownloadManager.getDownloadPath();
        private Picture pictureToBeSaved;

        private DirectoryChooserFragment.OnFragmentInteractionListener onFragmentInteractionListener =
                new DirectoryChooserFragment.OnFragmentInteractionListener() {
                    @Override
                    public void onSelectDirectory(@NonNull String path) {
                        if (pictureToBeSaved == null)
                            return;
                        lastPath = path;
                        loadPicture(pictureToBeSaved, path, ACTION_SAVE);
                        mDialog.dismiss();
                    }

                    @Override
                    public void onCancelChooser() {
                        mDialog.dismiss();
                    }
                };


        public void onSelectDirectory(Uri rootUri) {
            String path = rootUri.toString();
            if (pictureToBeSaved == null)
                return;
            lastPath = path;
            loadPicture(pictureToBeSaved, path, ACTION_SAVE);
        }

        @Override
        public boolean onItemLongClick(View view, int position) {
            if (!(position >= 0 && position < pictures.size()))
                return false;
            pictureToBeSaved = pictures.get(position);
            new AlertDialog.Builder(PictureViewerActivity.this)
                    .setTitle("操作")
                    .setItems(new String[]{"保存", "分享"}, (dialogInterface, i) -> {
                        if (i == 0) {
                            new AlertDialog.Builder(PictureViewerActivity.this).setTitle("是否直接保存到下载目录？")
                                    .setMessage("或者另存到其他目录")
                                    .setPositiveButton("保存", (dialog1, which1) ->
                                            onSelectDirectory(Uri.parse(DownloadManager.getDownloadPath())))
                                    .setNegativeButton("选择目录", (dialog12, which12) -> {
                                        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                                                .initialDirectory(lastPath)
                                                .newDirectoryName("download")
                                                .allowNewDirectoryNameModification(true)
                                                .build();
                                        mDialog = DirectoryChooserFragment.newInstance(config);
                                        mDialog.setDirectoryChooserListener(onFragmentInteractionListener);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                            try {
                                                startActivityForResult(intent, PictureViewerActivity.RESULT_CHOOSE_DIRECTORY);
                                            } catch (ActivityNotFoundException e) {
                                                e.printStackTrace();
                                                mDialog.show(getFragmentManager(), null);
                                            }
                                        } else {
                                            mDialog.show(getFragmentManager(), null);
                                        }
                                    }).show();
                        } else if (i == 1) {
                            loadPicture(pictureToBeSaved, DownloadManager.getDownloadPath(), ACTION_SHARE);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        }
    }
}
