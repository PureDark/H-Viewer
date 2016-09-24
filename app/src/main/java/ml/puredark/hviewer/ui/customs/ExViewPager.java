package ml.puredark.hviewer.ui.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.R;

/**
 * Created by PureDark on 2016/7/30.
 */

public class ExViewPager extends ViewPager {
    public ExViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExViewPager);
            preview(context, a);
        }
    }

    private void preview(Context context, TypedArray a) {
        List<View> viewList = new ArrayList<>();
        int layoutResId;
        if ((layoutResId = a.getResourceId(R.styleable.ExViewPager_pagerLayout1, 0)) != 0)
            viewList.add(inflate(context, layoutResId, null));
        if ((layoutResId = a.getResourceId(R.styleable.ExViewPager_pagerLayout2, 0)) != 0)
            viewList.add(inflate(context, layoutResId, null));
        if ((layoutResId = a.getResourceId(R.styleable.ExViewPager_pagerLayout3, 0)) != 0)
            viewList.add(inflate(context, layoutResId, null));
        if ((layoutResId = a.getResourceId(R.styleable.ExViewPager_pagerLayout4, 0)) != 0)
            viewList.add(inflate(context, layoutResId, null));
        if ((layoutResId = a.getResourceId(R.styleable.ExViewPager_pagerLayout5, 0)) != 0)
            viewList.add(inflate(context, layoutResId, null));
        a.recycle();
        setAdapter(new PreviewPagerAdapter(viewList));
        int currItem = a.getInt(R.styleable.ExViewPager_pagerCurrItem, 0);
        setCurrentItem(currItem);
    }

    public static class PreviewPagerAdapter extends PagerAdapter {
        private List<View> mViewList;

        public PreviewPagerAdapter(List<View> viewList) {
            mViewList = viewList;
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mViewList.get(position) != null) {
                container.removeView(mViewList.get(position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position), 0);
            return mViewList.get(position);
        }
    }
}
