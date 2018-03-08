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
}
