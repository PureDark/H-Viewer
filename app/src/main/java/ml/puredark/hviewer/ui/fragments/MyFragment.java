package ml.puredark.hviewer.ui.fragments;


import android.support.v4.app.Fragment;

import ml.puredark.hviewer.beans.Site;

/**
 * Created by PureDark on 2015/12/9.
 */
public abstract class MyFragment extends Fragment {

    public abstract Site getCurrSite();

    public abstract void onSearch(String keyword);

    public abstract void onLoadUrl(String url);

    public abstract void onJumpToPage(int page);

    public abstract void setRecyclerViewToList();

    public abstract void setRecyclerViewToGrid();

}
