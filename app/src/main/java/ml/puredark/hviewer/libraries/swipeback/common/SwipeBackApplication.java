package ml.puredark.hviewer.libraries.swipeback.common;

import android.app.Activity;
import android.app.Application;
import android.view.View;

/**
 * Created by fhf11991 on 2016/7/18.
 */

public class SwipeBackApplication extends Application {

    private ActivityLifecycleHelper mActivityLifecycleHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(mActivityLifecycleHelper = new ActivityLifecycleHelper());
    }

    public ActivityLifecycleHelper getActivityLifecycleHelper() {
        return mActivityLifecycleHelper;
    }

    public void onSlideBack(boolean isReset, float distance) {
        if (mActivityLifecycleHelper != null) {
            Activity lastActivity = mActivityLifecycleHelper.getPreActivity();
            if (lastActivity != null) {
                View contentView = lastActivity.findViewById(android.R.id.content);
                if (isReset) {
                    contentView.setX(contentView.getLeft());
                } else {
                    final int width = getResources().getDisplayMetrics().widthPixels;
                    contentView.setX(-width / 3 + distance / 3);
                }
            }
        }
    }
}
