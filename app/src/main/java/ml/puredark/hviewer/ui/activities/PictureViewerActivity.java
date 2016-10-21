package ml.puredark.hviewer.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.umeng.analytics.MobclickAgent;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.adapters.PicturePagerAdapter;
import ml.puredark.hviewer.ui.adapters.PictureViewerAdapter;
import ml.puredark.hviewer.ui.customs.AreaClickHelper;
import ml.puredark.hviewer.ui.customs.MultiTouchViewPager;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;
import ml.puredark.hviewer.utils.FileType;
import ml.puredark.hviewer.utils.RegexValidateUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_LEFT_TO_RIGHT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_RIGHT_TO_LEFT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_TOP_TO_BOTTOM;


public class PictureViewerActivity extends BaseActivity {

    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.view_pager)
    MultiTouchViewPager viewPager;
    @BindView(R.id.rv_picture)
    RecyclerView rvPicture;

    public final static int RESULT_CHOOSE_DIRECTORY = 1;

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
        viewDirection = (String) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_DIRECTION, DIREACTION_LEFT_TO_RIGHT);
        if (!DIREACTION_LEFT_TO_RIGHT.equals(viewDirection)
                && !DIREACTION_RIGHT_TO_LEFT.equals(viewDirection)
                && !DIREACTION_TOP_TO_BOTTOM.equals(viewDirection))
            viewDirection = DIREACTION_LEFT_TO_RIGHT;

        currPos = getIntent().getIntExtra("position", 0);

        onItemLongClickListener = new MyOnItemLongClickListener();


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
            });

            int position = picturePagerAdapter.getPicturePostion(currPos);
            tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());
            viewPager.setAdapter(picturePagerAdapter);
            ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    currPos = position;
                    tvCount.setText((picturePagerAdapter.getPicturePostion(currPos) + 1) + "/" + picturePagerAdapter.getCount());
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
                }
            });
            moveToPosition(rvPicture, currPos);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvPicture.getLayoutManager();
            currPos = linearLayoutManager.findLastVisibleItemPosition();
            tvCount.setText((currPos + 1) + "/" + pictureViewerAdapter.getItemCount());
        }
    }

    private boolean move;
    private int mIndex;

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
            int top = rvPicture.getChildAt(n - firstItem).getTop();
            recyclerView.scrollBy(0, top);
        } else {
            //当要置顶的项在当前显示的最后一项的后面时
            recyclerView.scrollToPosition(n);
            //这里这个变量是用在RecyclerView滚动监听里面的
            move = true;
        }
    }

    public void notifyDataSetChanged(List<Picture> pictures) {
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
                        loadPicture(pictureToBeSaved, path, false);
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
            loadPicture(pictureToBeSaved, path, false);
        }

        @Override
        public boolean onItemLongClick(View view, int position) {
            if (!(position > 0 && position < pictures.size()))
                return false;
            pictureToBeSaved = pictures.get(position);
            new AlertDialog.Builder(PictureViewerActivity.this)
                    .setTitle("操作")
                    .setItems(new String[]{"保存", "分享"}, (dialogInterface, i) -> {
                        if (i == 0) {
                            new AlertDialog.Builder(PictureViewerActivity.this).setTitle("保存图片？")
                                    .setMessage("是否保存当前图片")
                                    .setPositiveButton("确定", (dialog, which) -> new AlertDialog.Builder(PictureViewerActivity.this).setTitle("是否直接保存到下载目录？")
                                            .setMessage("否则另存到其他目录")
                                            .setPositiveButton("是", (dialog1, which1) ->
                                                    onSelectDirectory(Uri.parse(DownloadManager.getDownloadPath())))
                                            .setNegativeButton("否", (dialog12, which12) -> {
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
                                            }).show())
                                    .setNegativeButton("取消", null)
                                    .show();
                        } else if (i == 1) {
                            loadPicture(pictureToBeSaved, DownloadManager.getDownloadPath(), true);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        }
    }


    private void loadPicture(final Picture picture, final String path, boolean share) {
        if(share && picture.pic !=null && picture.pic.startsWith("file://")) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(picture.pic));
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "将图片分享到"));
            MobclickAgent.onEvent(this, "ShareSinglePicture");
            return;
        }
        if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE))
            picture.referer = RegexValidateUtil.getHostFromUrl(site.galleryUrl);
        ImageLoader.loadResourceFromUrl(PictureViewerActivity.this, picture.pic, site.cookie, picture.referer,
                new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {

                    @Override
                    protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        if (!dataSource.isFinished()) {
                            return;
                        }
                        CloseableReference<PooledByteBuffer> ref = dataSource.getResult();
                        if (ref != null) {
                            try {
                                PooledByteBuffer imageBuffer = ref.get();
                                savePicture(path, imageBuffer, share);
                            } finally {
                                CloseableReference.closeSafely(ref);
                            }
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {

                    }
                });
    }

    private void savePicture(String path, PooledByteBuffer buffer, boolean share) {
        try {
            byte[] bytes = new byte[buffer.size()];
            buffer.read(0, bytes, 0, buffer.size());
            String postfix = FileType.getFileType(bytes, FileType.TYPE_IMAGE);
            String fileName;
            if (share) {
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
}
