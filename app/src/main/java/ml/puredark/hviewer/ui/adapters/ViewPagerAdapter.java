package ml.puredark.hviewer.ui.adapters;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    List<View> viewLists;
    List<String> titles;

    public ViewPagerAdapter(List<View> lists, List<String> titles) {
        viewLists = lists;
        this.titles = titles;
    }

    @Override
    public int getCount() {
        return viewLists.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (viewLists.get(position) != null) {
            container.removeView(viewLists.get(position));
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position > container.getChildCount())
            return viewLists.get(position);
        container.addView(viewLists.get(position), position);
        return viewLists.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
