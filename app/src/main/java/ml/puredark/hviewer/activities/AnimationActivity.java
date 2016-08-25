package ml.puredark.hviewer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.nineoldandroids.animation.Animator;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.customs.AnimationOnActivity;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.services.DownloadService;

/**
 * Created by PureDark on 2016/8/13.
 */

public class AnimationActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private DrawerArrowDrawable btnReturnIcon;

    private View container;
    private ImageView btnReturn;
    private AppBarLayout appBar;
    private FloatingActionMenu fabMenu;

    private DownloadReceiver receiver;


    //是否动画中
    private boolean animating = false;

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

    protected void setAppBar(AppBarLayout appBar) {
        this.appBar = appBar;
    }

    protected void setFabMenu(FloatingActionMenu fabMenu) {
        this.fabMenu = fabMenu;
    }

    protected void setDownloadReceiver(DownloadReceiver receiver) {
        this.receiver = receiver;
    }

    public void showSnackBar(String content){
        if(container==null)return;
        Snackbar snackbar = Snackbar.make(
                container,
                content,
                Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if (animating)
            return;
        else if(btnReturnIcon!=null)
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
            super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (animating) return false;
        return super.dispatchTouchEvent(event);
    }


    @Override
    public void onResume() {
        super.onResume();
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
        unregisterReceiver(receiver);
        if (appBar != null)
            appBar.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (appBar != null)
            appBar.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset != 0) {
            if (fabMenu != null)
                fabMenu.hideMenu(true);
        } else {
            if (fabMenu != null)
                fabMenu.showMenu(true);
        }
    }


    public class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(DownloadService.ON_START)||
                    intent.getAction().equals(DownloadService.ON_PROGRESS)) {
            }
            else if(intent.getAction().equals(DownloadService.ON_PAUSE)) {
            }
            else if(intent.getAction().equals(DownloadService.ON_FAILURE)){
                String message = intent.getStringExtra("message");
                message = ("".equals(message))?"当前任务下载失败，请重试":message;
                showSnackBar(message);
            }
            else if(intent.getAction().equals(DownloadService.ON_COMPLETE)){
                showSnackBar("一个任务下载成功");
            }
        }

    }

}
