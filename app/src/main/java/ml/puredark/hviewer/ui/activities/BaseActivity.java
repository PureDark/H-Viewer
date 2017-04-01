package ml.puredark.hviewer.ui.activities;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionMenu;
import com.nineoldandroids.animation.Animator;
import com.umeng.analytics.MobclickAgent;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.download.DownloadService;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.libraries.swipeback.dispatchactivity.SwipeBackActivity;
import ml.puredark.hviewer.ui.customs.AnimationOnActivity;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2016/8/13.
 */

public class BaseActivity extends SwipeBackActivity implements AppBarLayout.OnOffsetChangedListener {

    protected boolean isCategoryEnable = false;
    protected boolean isStatusBarEnabled = true;
    protected boolean isDoubleBackExitEnabled = false;
    private DrawerArrowDrawable btnReturnIcon;
    private View container;
    private DrawerLayout drawerLayout;
    private ImageView btnReturn;
    private AppBarLayout appBar;
    private FloatingActionMenu fabMenu;
    private DownloadReceiver receiver;
    //按下返回键次数
    private int backCount = 0;

    //是否动画中
    private boolean animating = false;

    //允许退出当前Activity
    private boolean allowExit = true;

    //是否开始页面统计
    private boolean analyze = true;
    private int lastOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 返回按钮图标 */
        btnReturnIcon = new DrawerArrowDrawable(this);
        btnReturnIcon.setColor(getResources().getColor(R.color.white));
        btnReturnIcon.setProgress(1f);

        receiver = new DownloadReceiver();
    }

    protected void setReturnButton(ImageView btnReturn) {
        this.btnReturn = btnReturn;
        btnReturn.setImageDrawable(btnReturnIcon);
    }

    protected void setContainer(View container) {
        this.container = container;
    }

    protected void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    protected void setAppBar(AppBarLayout appBar) {
        this.appBar = appBar;
    }

    protected void setToolbar(Toolbar toolbar) {
        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) toolbar.getLayoutParams();
            lp.topMargin = MDStatusBarCompat.getStatusBarHeight(this);
            toolbar.setLayoutParams(lp);
        }
    }

    protected void setFabMenu(FloatingActionMenu fabMenu) {
        this.fabMenu = fabMenu;
    }

    protected void setDownloadReceiver(DownloadReceiver receiver) {
        this.receiver = receiver;
    }

    protected void setAnalyze(boolean analyze) {
        this.analyze = analyze;
    }

    public boolean isInOneHandMode() {
        return (boolean) SharedPreferencesUtil.getData(this, SettingFragment.KEY_PREF_VIEW_ONE_HAND, false);
    }

    public void setDrawerEnabled(boolean enabled) {
        if (drawerLayout != null) {
            if (enabled) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
                if (isCategoryEnable)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
                if (isCategoryEnable)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }
        }
    }

    public void setDoubleBackExitEnabled(boolean doubleBackExitEnabled) {
        isDoubleBackExitEnabled = doubleBackExitEnabled;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    public void setAllowExit(boolean allow) {
        allowExit = allow;
    }

    public void showSnackBar(String content) {
        if (container == null) return;
        Snackbar snackbar = Snackbar.make(
                container,
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        snackbar.show();
    }

    public void showSnackBar(String content, String actionText, View.OnClickListener listener) {
        if (container == null) return;
        Snackbar snackbar = Snackbar.make(
                container,
                content,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        snackbar.setAction(actionText, listener);
        snackbar.show();
    }

    public void alert(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null).show();
    }

    protected boolean isStatusBarEnabled() {
        return isStatusBarEnabled;
    }

    protected void toogleStatus() {
        showStatus(!isStatusBarEnabled);
    }

    //控制显示/隐藏系统状态栏和底部计数栏
    protected void showStatus(boolean enabled) {
        isStatusBarEnabled = enabled;
        if (enabled) {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attr);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void reportShortcutUsed(Context context, String shortcutId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.getSystemService(ShortcutManager.class).reportShortcutUsed(shortcutId);
        }
    }

    @Override
    public void onBackPressed() {
        if (animating || !allowExit)
            return;
        if (isDoubleBackExitEnabled) {
            backCount++;
            if (backCount == 1)
                showSnackBar("再按一次退出");
            else if (backCount >= 2) {
                if (btnReturnIcon != null)
                    AnimationOnActivity.reverse(btnReturnIcon, new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            animating = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animating = false;
                            finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            animating = false;
                            finish();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                else
                    finish();
            }
            new Handler().postDelayed(() -> backCount = 0, 1000);
        } else
            finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (animating) return false;
        try {
            return super.dispatchTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (analyze) {
            MobclickAgent.onPageStart(this.getClass().getSimpleName());
            MobclickAgent.onResume(this);
        }
        IntentFilter downloadIntentFilter = new IntentFilter();
        downloadIntentFilter.addAction(DownloadService.ON_START);
        downloadIntentFilter.addAction(DownloadService.ON_PAUSE);
        downloadIntentFilter.addAction(DownloadService.ON_PROGRESS);
        downloadIntentFilter.addAction(DownloadService.ON_COMPLETE);
        downloadIntentFilter.addAction(DownloadService.ON_FAILURE);
        registerReceiver(receiver, downloadIntentFilter);
        if (appBar != null)
            appBar.addOnOffsetChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (analyze) {
            MobclickAgent.onPageEnd(this.getClass().getSimpleName());
            MobclickAgent.onPause(this);
        }
        unregisterReceiver(receiver);
        if (appBar != null)
            appBar.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new DownloadTaskHolder(this).onDestroy();
        if (appBar != null)
            appBar.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset < lastOffset) {
            if (fabMenu != null)
                fabMenu.hideMenu(true);
        } else if (verticalOffset > lastOffset) {
            if (fabMenu != null)
                fabMenu.showMenu(true);
        }
        lastOffset = verticalOffset;
    }


    public class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ON_START) ||
                    intent.getAction().equals(DownloadService.ON_PROGRESS)) {
            } else if (intent.getAction().equals(DownloadService.ON_PAUSE)) {
            } else if (intent.getAction().equals(DownloadService.ON_FAILURE)) {
                String message = intent.getStringExtra("message");
                message = ("".equals(message)) ? "当前任务下载失败，请重试" : message;
                showSnackBar(message);
            } else if (intent.getAction().equals(DownloadService.ON_COMPLETE)) {
                showSnackBar("一个任务下载成功");
            }
            Logger.d("DownloadReceiver", intent.getAction());
        }

    }

}
