package ml.puredark.hviewer.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.ui.adapters.PicturePagerAdapter;
import ml.puredark.hviewer.ui.adapters.PictureViewerAdapter;
import ml.puredark.hviewer.ui.customs.MultiTouchViewPager;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
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

        Site site = null;
        if (HViewerApplication.temp2 instanceof Site)
            site = (Site) HViewerApplication.temp2;
        Collection collection = null;
        if (HViewerApplication.temp3 instanceof Collection)
            collection = (Collection) HViewerApplication.temp3;
        List<Picture> pictures = null;
        if (HViewerApplication.temp4 instanceof List)
            pictures = (List<Picture>) HViewerApplication.temp4;

        if (site == null || collection == null || pictures == null) {
            Toast.makeText(this, "数据错误，请刷新后重试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        HViewerApplication.temp = null;
        HViewerApplication.temp2 = null;
        HViewerApplication.temp3 = null;
        HViewerApplication.temp4 = null;
        if (collectionActivity != null)
            collectionActivity.setPictureViewerActivity(this);

        volumeKeyEnabled = (boolean) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_VOLUME_FLICK, true);
        viewDirection = (String) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_DIRECTION, DIREACTION_LEFT_TO_RIGHT);
        if(!DIREACTION_LEFT_TO_RIGHT.equals(viewDirection)
                && !DIREACTION_RIGHT_TO_LEFT.equals(viewDirection)
                && !DIREACTION_TOP_TO_BOTTOM.equals(viewDirection))
            viewDirection = DIREACTION_LEFT_TO_RIGHT;

        int position = getIntent().getIntExtra("position", 0);

        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection) || DIREACTION_RIGHT_TO_LEFT.equals(viewDirection)) {
            viewPager.setVisibility(View.VISIBLE);
            rvPicture.setVisibility(View.GONE);
            picturePagerAdapter = new PicturePagerAdapter(this, site, collection, pictures);
            picturePagerAdapter.setViewDirection(viewDirection);
            position = picturePagerAdapter.getPicturePostion(position);
            tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());
            viewPager.setAdapter(picturePagerAdapter);
            ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    tvCount.setText((picturePagerAdapter.getPicturePostion(position) + 1) + "/" + picturePagerAdapter.getCount());
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            };
            viewPager.addOnPageChangeListener(listener);
            int limit = (int) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                    SettingFragment.KEY_PREF_VIEW_PRELOAD_PAGES, 2);
            viewPager.setOffscreenPageLimit(limit);
            viewPager.setCurrentItem(position);
        } else if (DIREACTION_TOP_TO_BOTTOM.equals(viewDirection)) {
            viewPager.setVisibility(View.GONE);
            rvPicture.setVisibility(View.VISIBLE);
            ListDataProvider<Picture> dataProvider = new ListDataProvider<>(pictures);
            pictureViewerAdapter = new PictureViewerAdapter(this, site, collection, dataProvider);
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
                    int position = linearLayoutManager.findLastVisibleItemPosition();
                    tvCount.setText((position + 1) + "/" + pictureViewerAdapter.getItemCount());
                }
            });
            moveToPosition(rvPicture, position);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvPicture.getLayoutManager();
            position = linearLayoutManager.findLastVisibleItemPosition();
            tvCount.setText((position + 1) + "/" + pictureViewerAdapter.getItemCount());
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

    public void notifyDataSetChanged() {
        if (picturePagerAdapter != null)
            picturePagerAdapter.notifyDataSetChanged();
        if (pictureViewerAdapter != null)
            pictureViewerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (picturePagerAdapter != null)
            picturePagerAdapter.clearItems();
        super.onDestroy();
    }

    // 监听音量键，实现翻页
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (volumeKeyEnabled)
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    nextPage();
                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    prevPage();
                    return true;
            }
        return super.onKeyDown(keyCode, event);
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
                if (picturePagerAdapter != null)
                    picturePagerAdapter.onSelectDirectory(uriTree);
                else if (pictureViewerAdapter != null)
                    pictureViewerAdapter.onSelectDirectory(uriTree);
            }
        }
    }

    private void prevPage() {
        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem > 0)
                viewPager.setCurrentItem(currItem - 1, true);
        } else if (DIREACTION_RIGHT_TO_LEFT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem + 1 < viewPager.getAdapter().getCount())
                viewPager.setCurrentItem(currItem + 1, true);
        } else if (DIREACTION_TOP_TO_BOTTOM.equals(viewDirection) && pictureViewerAdapter != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) rvPicture.getLayoutManager();
            int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstItemPosition > 0) {
                moveToPosition(rvPicture, firstItemPosition - 1);
            }
        }
    }

    private void nextPage() {
        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem + 1 < viewPager.getAdapter().getCount())
                viewPager.setCurrentItem(currItem + 1, true);
        } else if (DIREACTION_RIGHT_TO_LEFT.equals(viewDirection) && picturePagerAdapter != null) {
            int currItem = viewPager.getCurrentItem();
            if (currItem > 0)
                viewPager.setCurrentItem(currItem - 1, true);
        } else if (DIREACTION_TOP_TO_BOTTOM.equals(viewDirection) && pictureViewerAdapter != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) rvPicture.getLayoutManager();
            int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstItemPosition + 1 < pictureViewerAdapter.getItemCount()) {
                moveToPosition(rvPicture, firstItemPosition + 1);
            }
        }
    }


}
