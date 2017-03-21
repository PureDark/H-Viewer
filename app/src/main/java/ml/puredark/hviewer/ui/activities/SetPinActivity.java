package ml.puredark.hviewer.ui.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.ui.fragments.LockMethodFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.VibratorUtil;

public class SetPinActivity extends BaseActivity {

    public final static int STEP_1ST_SET_PW = 1;
    public final static int STEP_2ND_CONFIRM = 2;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.layout_pin_lock)
    LinearLayout layoutPinLock;
    @BindView(R.id.pin_lock_view)
    PinLockView mPinLockView;
    @BindView(R.id.indicator_dots)
    IndicatorDots mIndicatorDots;
    @BindView(R.id.tv_message)
    TextView tvMessage;
    @BindView(R.id.btn_left)
    Button btnLeft;
    @BindView(R.id.btn_right)
    Button btnRight;

    private int step = STEP_1ST_SET_PW;
    private String correctPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);
        ButterKnife.bind(this);
        MDStatusBarCompat.setSwipeBackToolBar(this, coordinatorLayout, appbar, toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setContainer(coordinatorLayout);
        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);

        btnLeft.setText("取消");
        btnLeft.setOnClickListener((v) -> {
            finish();
        });

        btnRight.setText("继续");
        btnRight.setEnabled(false);
        btnRight.setClickable(false);
        btnRight.setOnClickListener((v) -> {
            if (step == STEP_1ST_SET_PW) {
                step = STEP_2ND_CONFIRM;
                mPinLockView.resetPinLockView();
                btnRight.setText("确认");
                tvMessage.setText(SetPinActivity.this.getString(R.string.setting_set_pin_confirm_message));
                btnRight.setEnabled(false);
                btnRight.setClickable(false);
            } else if (step == STEP_2ND_CONFIRM) {
                SharedPreferencesUtil.saveData(this, LockMethodFragment.KEY_PREF_PIN_LOCK, correctPin);
                SharedPreferencesUtil.saveData(this, LockMethodFragment.KEY_PREF_CURR_LOCK_METHOD, "pin");
                finish();
            }
        });
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if (step == STEP_1ST_SET_PW) {
                    correctPin = pin;
                    btnRight.setEnabled(true);
                    btnRight.setClickable(true);
                } else if (step == STEP_2ND_CONFIRM) {
                    if (pin.equals(correctPin)) {
                        btnRight.setText("确认");
                        btnRight.setEnabled(true);
                        btnRight.setClickable(true);
                    } else {
                        showErrorMessage(SetPinActivity.this.getString(R.string.setting_set_pin_confirm_error));
                    }
                }
            }

            @Override
            public void onEmpty() {
//                tvMessage.setText(LockActivity.this.getString(R.string.pin_lock_message));
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                if (pinLength < 4) {
                    btnRight.setEnabled(false);
                    btnRight.setClickable(false);
                }
                if (step == STEP_1ST_SET_PW) {
                    btnRight.setText("继续");
                    tvMessage.setText(SetPinActivity.this.getString(R.string.setting_set_pin_message));
                } else if (step == STEP_2ND_CONFIRM) {
                    btnRight.setText("确认");
                    tvMessage.setText(SetPinActivity.this.getString(R.string.setting_set_pin_confirm_message));
                }
            }
        });

        mPinLockView.setPinLength(4);
        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL);
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }


    private void showErrorMessage(String message) {
        VibratorUtil.Vibrate(SetPinActivity.this, 50);
        tvMessage.setText(message);
        YoYo.with(Techniques.BounceInUp)
                .duration(200)
                .playOn(tvMessage);
    }

}
