package ml.puredark.hviewer.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.helpers.HProxy;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.helpers.UpdateManager;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

public class SettingActivity extends AnimationActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_return)
    ImageView btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getFragmentManager().beginTransaction().replace(R.id.setting_content, new SettingFragment()).commit();
        ButterKnife.bind(this);
        MDStatusBarCompat.setOrdinaryToolBar(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setContainer(coordinatorLayout);
        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);

    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    public static class SettingFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
        public static final String KEY_PREF_PROXY_ENABLED = "pref_proxy_enabled";
        public static final String KEY_PREF_PROXY_REQUEST = "pref_proxy_request";
        public static final String KEY_PREF_PROXY_PICTURE = "pref_proxy_picture";
        public static final String KEY_PREF_PROXY_SERVER = "pref_proxy_server";

        public static final String KEY_PREF_ABOUT_UPGRADE = "pref_about_upgrade";
        public static final String KEY_PREF_ABOUT_LICENSE = "pref_about_license";
        public static final String KEY_PREF_ABOUT_ABOUT = "pref_about_about";

        private AnimationActivity activity;

        public SettingFragment(){
            activity = (AnimationActivity) this.getActivity();
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);

            updateProxyOptions(HProxy.isEnabled());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_PREF_PROXY_ENABLED)) {
                updateProxyOptions(sharedPreferences.getBoolean(key, true));
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if(preference.getKey().equals(KEY_PREF_ABOUT_UPGRADE)){
                checkUpdate();
            }else if(preference.getKey().equals(KEY_PREF_ABOUT_LICENSE)){

            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public void updateProxyOptions(boolean isEnabled) {
            getPreferenceScreen().findPreference(KEY_PREF_PROXY_REQUEST).setEnabled(isEnabled);
            getPreferenceScreen().findPreference(KEY_PREF_PROXY_PICTURE).setEnabled(isEnabled);
            getPreferenceScreen().findPreference(KEY_PREF_PROXY_SERVER).setEnabled(isEnabled);
        }

        public void checkUpdate(){
            String url = "https://api.github.com/repos/PureDark/H-Viewer/releases/latest";
            HViewerHttpClient.get(url, null, new HViewerHttpClient.OnResponseListener() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject version = new JsonParser().parse(result).getAsJsonObject();
                        JsonArray assets = version.get("assets").getAsJsonArray();
                        if(assets.size()>0) {
                            String oldVersion = HViewerApplication.getVersionName();
                            String newVersion = version.get("tag_name").getAsString().substring(1);
                            String url = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                            String detail = version.get("body").getAsString();
                            new UpdateManager(HViewerApplication.mContext, url, newVersion + "版本更新", detail).checkUpdateInfo(oldVersion, newVersion);
                        }else{
                            activity.showSnackBar("当前已是最新版本！");
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        activity.showSnackBar("当前已是最新版本！");
                    }
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    activity.showSnackBar("当前已是最新版本！");
                }
            });
        }

    }

}
