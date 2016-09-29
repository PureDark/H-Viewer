package ml.puredark.hviewer.ui.customs;

import android.content.Context;
import android.util.AttributeSet;

import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

/**
 * Created by PureDark on 2016/9/29.
 */

public class MyPullLoadMoreRecyclerView extends PullLoadMoreRecyclerView {

    public MyPullLoadMoreRecyclerView(Context context) {
        super(context);
    }

    public MyPullLoadMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 重写以支持SwipeBack
    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        }catch (Exception e){
        }
    }
}
