package ml.puredark.hviewer.ui.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import ml.puredark.hviewer.R;


/**
 * Created by PureDark on 2016/7/30.
 */

public class ExTabLayout extends TabLayout {
    private TypedArray typedArray;

    public ExTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExTabLayout);
        // preview
        if (isInEditMode()) {
            preview(context, typedArray);
        }
    }

    @Override
    public void setupWithViewPager(ViewPager viewPager) {
        super.setupWithViewPager(viewPager);
        setIconsAndTextColor(typedArray);
    }

    private void preview(Context context, TypedArray a) {
        final String tabStrArr = a.getString(R.styleable.ExTabLayout_tabTitleArray);
        final String[] tabRealStrArr = getTabRealStrArr(tabStrArr);
        ViewPager viewPager = new ViewPager(context);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return tabRealStrArr.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabRealStrArr[position];
            }
        });
        viewPager.setCurrentItem(0);
        setupWithViewPager(viewPager);
    }

    private String[] getTabRealStrArr(String tabStrArr) {
        if (tabStrArr != null && !tabStrArr.equals(""))
            return tabStrArr.split(",");
        else
            return new String[0];
    }

    private void setIconsAndTextColor(TypedArray a) {
        int length = this.getTabCount();
        for (int i = 0; i < length; i++) {
            TabLayout.Tab tab = getTabAt(i);
            tab.setCustomView(R.layout.item_tab);
            Drawable icon = null;
            switch (i) {
                case 0:
                    icon = a.getDrawable(R.styleable.ExTabLayout_tabIcon1);
                    break;
                case 1:
                    icon = a.getDrawable(R.styleable.ExTabLayout_tabIcon2);
                    break;
                case 2:
                    icon = a.getDrawable(R.styleable.ExTabLayout_tabIcon3);
                    break;
                case 3:
                    icon = a.getDrawable(R.styleable.ExTabLayout_tabIcon4);
                    break;
                case 4:
                    icon = a.getDrawable(R.styleable.ExTabLayout_tabIcon5);
                    break;
            }
            tab.setIcon(icon);

            int color = a.getColor(R.styleable.ExTabLayout_tabMyTextColor, Color.rgb(0, 0, 0));
            View view = tab.getCustomView();
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setTextColor(color);
            tab.setCustomView(view);
        }
    }
}
