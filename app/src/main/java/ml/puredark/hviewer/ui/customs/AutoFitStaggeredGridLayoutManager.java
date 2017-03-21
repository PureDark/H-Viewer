package ml.puredark.hviewer.ui.customs;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;


public class AutoFitStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

    public AutoFitStaggeredGridLayoutManager(Context context, int orientation) {
        /* Initially set spanCount to 1, will be changed automatically later. */
        super(1, orientation);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        int rvWidth = getWidth();
        int rvHeight = getHeight();
        if (rvWidth > 0 && rvHeight > 0) {
            rvWidth -= 100;
            int childrenWidth1st = 0;
            int childrenWidth2nd = 0;
            View firstView = findViewByPosition(0);
            if (firstView == null)
                return;
            View secoundView = findViewByPosition(1);
            if (secoundView == null)
                return;

            int firstY = (int) firstView.getY();
            int secoundY = (int) secoundView.getY();
            for (int i = 0; i < getItemCount(); i++) {
                View view = findViewByPosition(i);
                if (view != null) {
                    if (firstY == (int) view.getY()) {
                        childrenWidth1st += view.getWidth();
                    } else if (secoundY == (int) view.getY()) {
                        childrenWidth2nd += view.getWidth();
                    }
                }
            }
            if (getOrientation() == HORIZONTAL && (childrenWidth1st > rvWidth || childrenWidth2nd > rvWidth)) {
                Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        setSpanCount(getSpanCount() + 1);
                    }
                };
                handler.post(r);
            }
        }
    }


}