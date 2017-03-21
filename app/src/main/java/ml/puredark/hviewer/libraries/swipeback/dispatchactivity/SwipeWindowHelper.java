package ml.puredark.hviewer.libraries.swipeback.dispatchactivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import ml.puredark.hviewer.libraries.swipeback.common.SwipeBackApplication;


/**
 * Created by fhf11991 on 2016/9/18.
 */
public class SwipeWindowHelper extends Handler {

    private static final String TAG = "SwipeWindowHelper";

    private static final String CURRENT_POINT_X = "currentPointX"; //点击事件

    private static final int MSG_ACTION_DOWN = 1; //点击事件
    private static final int MSG_ACTION_MOVE = 2; //滑动事件
    private static final int MSG_ACTION_UP = 3;  //点击结束
    private static final int MSG_SLIDE_CANCEL = 4; //开始滑动，不返回前一个页面
    private static final int MSG_SLIDE_CANCELED = 5;  //结束滑动，不返回前一个页面
    private static final int MSG_SLIDE_PROCEED = 6; //开始滑动，返回前一个页面
    private static final int MSG_SLIDE_FINISHED = 7;//结束滑动，返回前一个页面

    private static final int SHADOW_WIDTH = 50; //px 阴影宽度
    private static final int MARGIN_THRESHOLD = 40;  //px 默认拦截手势区间 0~40
    private final FrameLayout mCurrentContentView;
    private boolean mIsSliding; //是否正在滑动
    private boolean mIsSlideAnimPlaying; //滑动动画展示过程中
    private float mDistanceX;  //px 当前滑动距离 （正数或0）
    private int mMarginThreshold;  //px 拦截手势区间
    private float mLastPointX;  //记录手势在屏幕上的X轴坐标
    private boolean mIsSupportSlideBack; //
    private Window mCurrentWindow;
    private ViewManager mViewManager;


    public SwipeWindowHelper(@NonNull Window window) {
        this(window, true);
    }

    public SwipeWindowHelper(@NonNull Window window, boolean isSupportSlideBack) {
        mCurrentWindow = window;
        mIsSupportSlideBack = isSupportSlideBack;
        mCurrentContentView = getContentView(mCurrentWindow);
        mViewManager = new ViewManager();
    }

    public void setSupportSlideBack(boolean supportSlideBack) {
        mIsSupportSlideBack = supportSlideBack;
    }

    public boolean processTouchEvent(MotionEvent ev) {
        if (!mIsSupportSlideBack) { //不支持滑动返回，则手势事件交给View处理
            return false;
        }

        if (mIsSlideAnimPlaying) {  //正在滑动动画播放中，直接消费手势事件
            return true;
        }

        if (mMarginThreshold == 0) {  //动态设置滑动拦截事件的区域
            final int commonMargin = 45;
            mMarginThreshold = Math.min(MARGIN_THRESHOLD, commonMargin);
        }

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        final int actionIndex = ev.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastPointX = ev.getRawX();
                boolean inThresholdArea = mLastPointX >= 0 && mLastPointX <= mMarginThreshold;

                if (inThresholdArea && mIsSliding) {
                    return true;
                } else if (inThresholdArea && !mIsSliding) { //开始滑动
                    mIsSliding = true;
                    sendEmptyMessage(MSG_ACTION_DOWN);
                    return true;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (mIsSliding) {  //有第二个手势事件加入，而且正在滑动事件中，则直接消费事件
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsSliding && actionIndex == 0) { //开始滑动
                    Message message = obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putFloat(CURRENT_POINT_X, ev.getRawX());
                    message.what = MSG_ACTION_MOVE;
                    message.setData(bundle);
                    sendMessage(message);
                    return true;
                } else if (mIsSliding && actionIndex != 0) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_OUTSIDE:
                if (mIsSliding && actionIndex == 0) { // 取消滑动 或 手势抬起 ，而且手势事件是第一手势，开始滑动动画
                    mIsSliding = false;
                    sendEmptyMessage(MSG_ACTION_UP);
                    return true;
                } else if (mIsSliding && actionIndex != 0) {
                    return true;
                }
                break;
            default:
                mIsSliding = false;
                break;
        }
        return false;
    }

    public Context getContext() {
        return mCurrentWindow == null ? null : mCurrentWindow.getContext();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_ACTION_DOWN:
                // hide input method
                InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = mCurrentWindow.getCurrentFocus();
                if (view != null) {
                    inputMethod.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (!mViewManager.addViewFromPreviousActivity()) return;

                // add shadow view on the left of content view
                mViewManager.addShadowView();

                if (mCurrentContentView.getChildCount() >= 3) {
                    View curView = mViewManager.getDisplayView();
                    if (curView.getBackground() == null) {
                        int color = getWindowBackgroundColor();
                        curView.setBackgroundColor(color);
                    }
                }
                break;

            case MSG_ACTION_MOVE:
                final float curPointX = msg.getData().getFloat(CURRENT_POINT_X);
                onSliding(curPointX);
                break;

            case MSG_ACTION_UP:
                final int width = getContext().getResources().getDisplayMetrics().widthPixels;
                if (mDistanceX == 0) {
                    if (mCurrentContentView.getChildCount() >= 3) {
                        mViewManager.removeShadowView();
                        mViewManager.resetPreviousView();
                    }
                } else if (mDistanceX > width / 4) {
                    sendEmptyMessage(MSG_SLIDE_PROCEED);
                } else {
                    sendEmptyMessage(MSG_SLIDE_CANCEL);
                }
                break;

            case MSG_SLIDE_CANCEL:
                startSlideAnim(true);
                break;

            case MSG_SLIDE_CANCELED:
                mDistanceX = 0;
                mIsSliding = false;
                mViewManager.removeShadowView();
                mViewManager.resetPreviousView();
                break;

            case MSG_SLIDE_PROCEED:
                startSlideAnim(false);
                break;

            case MSG_SLIDE_FINISHED:
                mViewManager.addCacheView();
                mViewManager.removeShadowView();
                mViewManager.resetPreviousView();

                if (getContext() instanceof Activity) {
                    Activity activity = (Activity) getContext();
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                } else if (getContext() instanceof ContextWrapper) {
                    Context baseContext = ((ContextWrapper) getContext()).getBaseContext();
                    if (baseContext instanceof Activity) {
                        Activity activity = (Activity) baseContext;
                        activity.finish();
                        activity.overridePendingTransition(0, 0);
                    }
                }
                break;

            default:
                break;
        }
    }

    private int getWindowBackgroundColor() {
        TypedArray array = null;
        try {
            array = getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
            return array.getColor(0, ContextCompat.getColor(getContext(), android.R.color.transparent));
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }

    /**
     * 手动处理滑动事件
     */
    private synchronized void onSliding(float curPointX) {
        final int width = getContext().getResources().getDisplayMetrics().widthPixels;
        View previewActivityContentView = mViewManager.mPreviousContentView;
        View shadowView = mViewManager.mShadowView;
        View currentActivityContentView = mViewManager.getDisplayView();

        if (previewActivityContentView == null || currentActivityContentView == null || shadowView == null) {
            sendEmptyMessage(MSG_SLIDE_CANCELED);
            return;
        }

        final float distanceX = curPointX - mLastPointX;
        mLastPointX = curPointX;
        mDistanceX = mDistanceX + distanceX;
        if (mDistanceX < 0) {
            mDistanceX = 0;
        }

        previewActivityContentView.setX(-width / 3 + mDistanceX / 3);
        shadowView.setX(mDistanceX - SHADOW_WIDTH);
        currentActivityContentView.setX(mDistanceX);
    }

    /**
     * 开始自动滑动动画
     *
     * @param slideCanceled 是不是要返回（true则不关闭当前页面）
     */
    private void startSlideAnim(final boolean slideCanceled) {
        final View previewView = mViewManager.mPreviousContentView;
        final View shadowView = mViewManager.mShadowView;
        final View currentView = mViewManager.getDisplayView();

        if (previewView == null || currentView == null) {
            return;
        }

        int width = getContext().getResources().getDisplayMetrics().widthPixels;
        Interpolator interpolator = new DecelerateInterpolator(2f);

        // preview activity's animation
        ObjectAnimator previewViewAnim = new ObjectAnimator();
        previewViewAnim.setInterpolator(interpolator);
        previewViewAnim.setProperty(View.TRANSLATION_X);
        float preViewStart = mDistanceX / 3 - width / 3;
        float preViewStop = slideCanceled ? -width / 3 : 0;
        previewViewAnim.setFloatValues(preViewStart, preViewStop);
        previewViewAnim.setTarget(previewView);

        // shadow view's animation
        ObjectAnimator shadowViewAnim = new ObjectAnimator();
        shadowViewAnim.setInterpolator(interpolator);
        shadowViewAnim.setProperty(View.TRANSLATION_X);
        float shadowViewStart = mDistanceX - SHADOW_WIDTH;
        float shadowViewEnd = slideCanceled ? SHADOW_WIDTH : width + SHADOW_WIDTH;
        shadowViewAnim.setFloatValues(shadowViewStart, shadowViewEnd);
        shadowViewAnim.setTarget(shadowView);

        // current view's animation
        ObjectAnimator currentViewAnim = new ObjectAnimator();
        currentViewAnim.setInterpolator(interpolator);
        currentViewAnim.setProperty(View.TRANSLATION_X);
        float curViewStart = mDistanceX;
        float curViewStop = slideCanceled ? 0 : width;
        currentViewAnim.setFloatValues(curViewStart, curViewStop);
        currentViewAnim.setTarget(currentView);

        // play animation together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(slideCanceled ? 150 : 300);
        animatorSet.playTogether(previewViewAnim, shadowViewAnim, currentViewAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (slideCanceled) {
                    mIsSlideAnimPlaying = false;
                    previewView.setX(0);
                    shadowView.setX(-SHADOW_WIDTH);
                    currentView.setX(0);
                    sendEmptyMessage(MSG_SLIDE_CANCELED);
                } else {
                    sendEmptyMessage(MSG_SLIDE_FINISHED);
                }
            }
        });
        animatorSet.start();
        mIsSlideAnimPlaying = true;
    }


    private final FrameLayout getContentView(Window window) {
        if (window == null) return null;
        return (FrameLayout) window.findViewById(Window.ID_ANDROID_CONTENT);
    }

    class ViewManager {
        private Activity mPreviousActivity;
        private ViewGroup mPreviousContentView;
        private View mShadowView;

        /**
         * Remove view from previous Activity and add into current Activity
         *
         * @return Is view added successfully
         */
        private boolean addViewFromPreviousActivity() {
            if (mCurrentContentView.getChildCount() == 0) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }

            SwipeBackApplication application = (SwipeBackApplication) mCurrentWindow.getContext().getApplicationContext();
            mPreviousActivity = application.getActivityLifecycleHelper().getPreActivity();
            if (mPreviousActivity == null) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }

            ViewGroup previousActivityContainer = getContentView(mPreviousActivity.getWindow());
            if (previousActivityContainer.getChildAt(0) instanceof ShadowView) {
                previousActivityContainer.removeViewAt(0);
            }
            if (previousActivityContainer == null || previousActivityContainer.getChildCount() == 0
                    || !(previousActivityContainer.getChildAt(0) instanceof ViewGroup)) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }
            mPreviousContentView = (ViewGroup) previousActivityContainer.getChildAt(0);
            previousActivityContainer.removeView(mPreviousContentView);
            mCurrentContentView.addView(mPreviousContentView, 0);
            return true;
        }

        /**
         * Remove the PreviousContentView at current Activity and put it into previous Activity.
         */
        private void resetPreviousView() {
            if (mPreviousContentView == null) return;

            View view = mPreviousContentView;
            FrameLayout contentView = mCurrentContentView;
            view.setX(0);
            contentView.removeView(view);
            mPreviousContentView = null;

            if (mPreviousActivity == null || mPreviousActivity.isFinishing()) return;
            Activity preActivity = mPreviousActivity;
            final ViewGroup previewContentView = getContentView(preActivity.getWindow());
            previewContentView.addView(view, 0);
            mPreviousActivity = null;
        }

        /**
         * add shadow view on the left of content view
         */
        private void addShadowView() {
            if (mShadowView == null) {
                mShadowView = new ShadowView(getContext());
                mShadowView.setX(-SHADOW_WIDTH);
            }
            final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    SHADOW_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT);
            final FrameLayout contentView = mCurrentContentView;
            if (mShadowView.getParent() == null)
                contentView.addView(mShadowView, 1, layoutParams);
        }

        private void removeShadowView() {
            if (mShadowView == null) return;
            final FrameLayout contentView = getContentView(mCurrentWindow);
            contentView.removeView(mShadowView);
            mShadowView = null;
        }

        private void addCacheView() {
            final FrameLayout contentView = mCurrentContentView;
            final View previousView = mPreviousContentView;
            PreviousPageView previousPageView = new PreviousPageView(getContext());
            contentView.addView(previousPageView, 0);
            previousPageView.cacheView(previousView);
        }

        private View getDisplayView() {
            int index = 0;
            if (mViewManager.mPreviousContentView != null) {
                index = index + 1;
            }

            if (mViewManager.mShadowView != null) {
                index = index + 1;
            }
            return mCurrentContentView.getChildAt(index);
        }
    }
}

class PreviousPageView extends View {
    private View mView;

    public PreviousPageView(Context context) {
        super(context);
    }

    public void cacheView(View view) {
        mView = view;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mView != null) {
            mView.draw(canvas);
            mView = null;
        }
    }
}

class ShadowView extends View {
    private Drawable mDrawable;

    public ShadowView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable == null) {
            int colors[] = {0x00000000, 0x17000000, 0x43000000};//分别为开始颜色，中间夜色，结束颜色
            mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        }
        mDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        mDrawable.draw(canvas);
    }
}
