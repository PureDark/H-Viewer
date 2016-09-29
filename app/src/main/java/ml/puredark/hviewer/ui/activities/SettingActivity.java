package ml.puredark.hviewer.ui.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.ui.fragments.SettingFragment;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        getFragmentManager().beginTransaction().replace(R.id.setting_content, new SettingFragment(this)).commit();
        ButterKnife.bind(this);
        MDStatusBarCompat.setOrdinaryToolBar(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setContainer(coordinatorLayout);
        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);

        tvTitle.setText("设置");

    }

    @OnClick(R.id.btn_return)
    void back() {
        if (getFragmentManager().getBackStackEntryCount() > 1)
            getFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        back();
    }

}
