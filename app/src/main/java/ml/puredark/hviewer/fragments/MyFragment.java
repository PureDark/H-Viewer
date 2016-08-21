package ml.puredark.hviewer.fragments;


import android.support.v4.app.Fragment;

/**
 * Created by PureDark on 2015/12/9.
 */
public abstract class MyFragment extends Fragment {

    public abstract void onSearch(String keyword);

    public abstract void setRecyclerViewToList();

    public abstract void setRecyclerViewToGrid();

}
