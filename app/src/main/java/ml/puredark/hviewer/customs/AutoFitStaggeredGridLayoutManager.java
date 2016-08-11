package ml.puredark.hviewer.customs;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;


public class AutoFitStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

    public AutoFitStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AutoFitStaggeredGridLayoutManager(Context context, int orientation) {
        /* Initially set spanCount to 1, will be changed automatically later. */
        super(1, orientation);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        int rvWidth = getWidth();
        int rvHeight = getHeight();
        int childrenWidth = 0;
        int childrenHeight = 0;
        for (int i = 0; i < getItemCount(); i+=getSpanCount()) {
            View view = findViewByPosition(i);
            if(view!=null){
                childrenWidth += view.getWidth();
                childrenHeight += view.getHeight();
            }
        }
        if (rvWidth > 0 && rvHeight > 0) {
            if (getOrientation() == VERTICAL && childrenHeight > rvHeight) {
                setSpanCount(getSpanCount() + 1);
            } else if (getOrientation() == HORIZONTAL && childrenWidth > rvWidth) {
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