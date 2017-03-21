package ml.puredark.hviewer.ui.customs;

import android.content.Context;
import android.util.AttributeSet;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

/**
 * Created by PureDark on 2016/9/29.
 */

public class WrappedPullLoadMoreRecyclerView extends PullLoadMoreRecyclerView {

    public WrappedPullLoadMoreRecyclerView(Context context) {
        super(context);
    }

    public WrappedPullLoadMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.getRecyclerView().setOnTouchListener(null);
    }

    // 重写以支持SwipeBack
    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (Exception e) {
        }
    }
}
