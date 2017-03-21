package ml.puredark.hviewer.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eightbitlab.com.blurview.BlurView;
import me.zhanghai.android.patternlock.PatternView;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.fragments.LockMethodFragment;
import ml.puredark.hviewer.utils.PatternLockUtils;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.VibratorUtil;
import rx.Subscriber;
import rx.Subscription;
import zwh.com.lib.FPerException;
import zwh.com.lib.RxFingerPrinter;

import static ml.puredark.hviewer.HViewerApplication.mContext;

public class LockActivity extends AppCompatActivity {

    @BindView(R.id.container)
    RelativeLayout layoutContainer;
    @BindView(R.id.dvBackground)
    SimpleDraweeView dvBackground;
    @BindView(R.id.blurView)
    BlurView mBlurView;
    @BindView(R.id.layout_pattern_lock)
    LinearLayout layoutPatternLock;
    @BindView(R.id.layout_pin_lock)
    LinearLayout layoutPinLock;
    @BindView(R.id.pattern_lock_view)
    PatternView mPatternView;
    @BindView(R.id.pin_lock_view)
    PinLockView mPinLockView;
    @BindView(R.id.indicator_dots)
    IndicatorDots mIndicatorDots;
    @BindView(R.id.tv_message)
    TextView tvMessage;

    private String correctPin;
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        ButterKnife.bind(this);
        MDStatusBarCompat.setImageTransparent(this);

        layoutPatternLock.setVisibility(View.GONE);
        layoutPinLock.setVisibility(View.GONE);

        initBlurryBackground();

        boolean isPatternLock = LockMethodFragment.getCurrentLockMethod(this) == LockMethodFragment.METHOD_PATTERN;
        boolean isPinLock = LockMethodFragment.getCurrentLockMethod(this) == LockMethodFragment.METHOD_PIN;

        initFingerPrintLock();

        correctPin = (String) SharedPreferencesUtil.getData(this, LockMethodFragment.KEY_PREF_PIN_LOCK, "");

        if (isPatternLock) {
            initPatternLock();
        } else if (isPinLock) {
            initPinkLock();
        } else {
            Intent intent = new Intent(LockActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initBlurryBackground() {
        final String rootDir = mContext.getExternalCacheDir().getAbsolutePath();
        File headerFile = new File(rootDir + "/image/header.jpg");
        String currHeaderUrl = (headerFile.exists()) ? "file://" + headerFile.getAbsolutePath() : "drawable://backdrop";
        Logger.d("HeaderImage", "currHeaderUrl : " + currHeaderUrl);

        ImageLoader.loadImageFromUrl(this, dvBackground, currHeaderUrl, null, null, true);

        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        mBlurView.setupWith(rootView)
                .windowBackground(windowBackground)
                .blurRadius(10);
    }

    private void initPatternLock() {
        tvMessage.setText(LockActivity.this.getString(R.string.pattern_lock_message));
        layoutPatternLock.setVisibility(View.VISIBLE);
        mPatternView.setOnPatternListener(new PatternView.OnPatternListener() {
            @Override
            public void onPatternStart() {
                tvMessage.setText(LockActivity.this.getString(R.string.pattern_lock_message));
            }

            @Override
            public void onPatternCleared() {
            }

            @Override
            public void onPatternCellAdded(List<PatternView.Cell> list) {
            }

            @Override
            public void onPatternDetected(List<PatternView.Cell> list) {
                if (success)
                    return;
                if (PatternLockUtils.isPatternCorrect(LockActivity.this, list)) {
                    mPatternView.setDisplayMode(PatternView.DisplayMode.Correct);
                    onSuccessUnlock();
                } else {
                    mPatternView.setDisplayMode(PatternView.DisplayMode.Wrong);
                    showErrorMessage(LockActivity.this.getString(R.string.pattern_lock_wrong), true);
                }
            }
        });
    }

    private void initPinkLock() {
        tvMessage.setText(LockActivity.this.getString(R.string.pin_lock_message));
        layoutPinLock.setVisibility(View.VISIBLE);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if (success)
                    return;
                if (pin.equals(correctPin)) {
                    onSuccessUnlock();
                } else {
                    showErrorMessage(LockActivity.this.getString(R.string.pin_lock_wrong), true);
                }
            }

            @Override
            public void onEmpty() {
//                tvMessage.setText(LockActivity.this.getString(R.string.pin_lock_message));
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                tvMessage.setText("");
            }
        });

        mPinLockView.setPinLength(4);
        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL);
    }

    private void initFingerPrintLock() {
        if (Build.VERSION.SDK_INT >= 23) {
            RxFingerPrinter rxFingerPrinter = new RxFingerPrinter(this);
            Subscription subscription =
                    rxFingerPrinter
                            .begin()
                            .subscribe(new Subscriber<Boolean>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (e instanceof FPerException) {
                                        showErrorMessage(((FPerException) e).getDisplayMessage(), false);
                                    }
                                }

                                @Override
                                public void onNext(Boolean aBoolean) {
                                    if (success)
                                        return;
                                    if (aBoolean) {
                                        onSuccessUnlock();
                                    } else {
                                        showErrorMessage(LockActivity.this.getString(R.string.finger_print_lock_wrong), true);
                                    }
                                }
                            });
            rxFingerPrinter.addSubscription(this, subscription);
        }
    }

    private void onSuccessUnlock() {
        vibrate(20, true);
        success = true;
        Intent intent = new Intent(LockActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showErrorMessage(String message, boolean vibrate) {
        if (vibrate)
            vibrate(50, false);
        tvMessage.setText(message);
        YoYo.with(Techniques.BounceInUp)
                .duration(200)
                .playOn(tvMessage);
    }

    private synchronized void vibrate(int milliseconds, boolean success) {
        if (!this.success) {
            VibratorUtil.Vibrate(LockActivity.this, milliseconds);
        }
        if (success)
            this.success = success;
    }

}
