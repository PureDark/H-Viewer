package ml.puredark.hviewer.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.customs.AnimationOnActivity;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_return)
    ImageView btnReturn;

    private DrawerArrowDrawable btnReturnIcon;

    //是否动画中
    private boolean animating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getFragmentManager().beginTransaction().replace(R.id.setting_content, new SettingFragment()).commit();
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /* 为返回按钮加载图标 */
        btnReturnIcon = new DrawerArrowDrawable(this);
        btnReturnIcon.setColor(getResources().getColor(R.color.white));
        btnReturn.setImageDrawable(btnReturnIcon);
        btnReturnIcon.setProgress(1f);

    }

    @Override
    public void onBackPressed() {
        if (animating)
            return;
        else
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
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        //super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (animating) return false;
        return super.dispatchTouchEvent(event);
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    public static class SettingFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
    {
        public static final String KEY_PREF_PROXY_ENABLED = "pref_proxy_enabled";
        public static final String KEY_PREF_PROXY_REQUEST = "pref_proxy_request";
        public static final String KEY_PREF_PROXY_PICTURE = "pref_proxy_picture";
        public static final String KEY_PREF_PROXY_SERVER = "pref_proxy_server";

        public static final String KEY_PREF_ABOUT_UPGRADE = "pref_about_upgrade";
        public static final String KEY_PREF_ABOUT_LICENSE = "pref_about_license";
        public static final String KEY_PREF_ABOUT_ABOUT = "pref_about_about";

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        }

    }

}
