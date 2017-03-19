package ml.puredark.hviewer.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import eightbitlab.com.blurview.BlurView;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import me.zhanghai.android.patternlock.PatternView;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.utils.VibratorUtil;
import rx.Subscriber;
import rx.Subscription;
import zwh.com.lib.FPerException;
import zwh.com.lib.RxFingerPrinter;

import static ml.puredark.hviewer.HViewerApplication.mContext;
import static ml.puredark.hviewer.R.id.blurView;

public class LockActivity extends AppCompatActivity {

    @BindView(R.id.container)
    RelativeLayout layoutContainer;
    @BindView(R.id.dvBackground)
    SimpleDraweeView dvBackground;
    @BindView(R.id.blurView)
    BlurView mBlurView;
    @BindView(R.id.pin_lock_view)
    PinLockView mPinLockView;
    @BindView(R.id.indicator_dots)
    IndicatorDots mIndicatorDots;
    @BindView(R.id.tv_message)
    TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        ButterKnife.bind(this);
        MDStatusBarCompat.setImageTransparent(this);

        initBlurryBackground();
        initPinkLock();
        initFingerPrintLock();
    }

    private void initBlurryBackground(){
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

    private void initPinkLock(){
        String rightPin = "5566";
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if(rightPin.equals(pin)){
                    onSuccessUnlock();
                } else {
                    showErrorMessage(LockActivity.this.getString(R.string.pin_lock_wrong));
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
        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL);
    }

    private void initFingerPrintLock(){
        if(Build.VERSION.SDK_INT >= 23) {
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
                                    if(e instanceof FPerException){
                                        showErrorMessage(((FPerException) e).getDisplayMessage());
                                    }
                                }

                                @Override
                                public void onNext(Boolean aBoolean) {
                                    if (aBoolean){
                                        onSuccessUnlock();
                                    }else {
                                        showErrorMessage(LockActivity.this.getString(R.string.finger_print_lock_wrong));
                                    }
                                }
                            });
            rxFingerPrinter.addSubscription(this,subscription);
        }
    }

    private void onSuccessUnlock(){
        VibratorUtil.Vibrate(LockActivity.this, 20);
        Intent intent = new Intent(LockActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showErrorMessage(String message){
        VibratorUtil.Vibrate(LockActivity.this, 100);
        tvMessage.setText(message);
        YoYo.with(Techniques.BounceInUp)
                .duration(200)
                .playOn(tvMessage);
    }

}
