package ml.puredark.hviewer.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.http.HViewerHttpClient;
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
        onBackPressed();
    }

    public static class SettingFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, DirectoryChooserFragment.OnFragmentInteractionListener {
        public static final String KEY_PREF_PROXY_ENABLED = "pref_proxy_enabled";
        public static final String KEY_PREF_PROXY_REQUEST = "pref_proxy_request";
        public static final String KEY_PREF_PROXY_PICTURE = "pref_proxy_picture";
        public static final String KEY_PREF_PROXY_SERVER = "pref_proxy_server";

        public static final String KEY_PREF_VIEW_PRELOAD_PAGES = "pref_view_preload_pages";

        public static final String KEY_PREF_DOWNLOAD_HIGH_RES = "pref_download_high_res";
        public static final String KEY_PREF_DOWNLOAD_PATH = "pref_download_path";

        public static final String KEY_PREF_ABOUT_UPGRADE = "pref_about_upgrade";
        public static final String KEY_PREF_ABOUT_LICENSE = "pref_about_license";
        public static final String KEY_PREF_ABOUT_H_VIEWER = "pref_about_h_viewer";

        private static final int RESULT_CHOOSE_DIRECTORY = 1;

        private AnimationActivity activity;
        private DirectoryChooserFragment mDialog;

        private boolean checking = false;

        public SettingFragment() {
        }

        @SuppressLint("ValidFragment")
        public SettingFragment(AnimationActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
            addPreferencesFromResource(R.xml.preferences);

            String proxyServer = getPreferenceManager().getSharedPreferences().getString(KEY_PREF_PROXY_SERVER, null);
            if (proxyServer != null)
                getPreferenceManager().findPreference(KEY_PREF_PROXY_SERVER).setSummary(proxyServer);

            String downloadPath = DownloadManager.getDownloadPath();
            if (downloadPath != null) {
                String displayPath = Uri.decode(downloadPath);
                getPreferenceManager().findPreference(KEY_PREF_DOWNLOAD_PATH).setSummary(displayPath);
            }
            getPreferenceScreen().setOnPreferenceChangeListener(this);
            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .initialDirectory((downloadPath.startsWith("/")) ? downloadPath : "/")
                    .newDirectoryName("download")
                    .allowNewDirectoryNameModification(true)
                    .build();
            mDialog = DirectoryChooserFragment.newInstance(config);
            mDialog.setTargetFragment(this, 0);
        }

        @Override
        public void onSelectDirectory(@NonNull String path) {
            SharedPreferencesUtil.saveData(getActivity(), KEY_PREF_DOWNLOAD_PATH, Uri.encode(path));
            getPreferenceManager().findPreference(KEY_PREF_DOWNLOAD_PATH).setSummary(Uri.decode(path));
            mDialog.dismiss();
        }

        @Override
        public void onCancelChooser() {
            mDialog.dismiss();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals(KEY_PREF_PROXY_SERVER)) {
                getPreferenceManager().findPreference(KEY_PREF_PROXY_SERVER).setSummary((String) newValue);
            }
            return true;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference.getKey().equals(KEY_PREF_ABOUT_UPGRADE)) {
                if (!checking)
                    checkUpdate();
            } else if (preference.getKey().equals(KEY_PREF_ABOUT_LICENSE)) {
                Intent intent = new Intent(activity, LicenseActivity.class);
                startActivity(intent);
            } else if (preference.getKey().equals(KEY_PREF_ABOUT_H_VIEWER)) {
                Intent intent = new Intent(activity, AboutActivity.class);
                startActivity(intent);
            } else if (preference.getKey().equals(KEY_PREF_DOWNLOAD_PATH)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, RESULT_CHOOSE_DIRECTORY);
                } else {
                    mDialog.show(getFragmentManager(), null);
                }
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                if (requestCode == RESULT_CHOOSE_DIRECTORY) {
                    Uri uriTree = data.getData();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        getActivity().getContentResolver().takePersistableUriPermission(
                                uriTree, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    String path = uriTree.toString();
                    path = Uri.encode("/sdcard/H-Viewer/download");
                    String displayPath = Uri.decode(path);
                    SharedPreferencesUtil.saveData(getActivity(), KEY_PREF_DOWNLOAD_PATH, path);
                    getPreferenceManager().findPreference(KEY_PREF_DOWNLOAD_PATH).setSummary(displayPath);
                }
            }
        }

        public void checkUpdate() {
            checking = true;
            String url = getString(R.string.update_site_url);
            HViewerHttpClient.get(url, null, new HViewerHttpClient.OnResponseListener() {
                @Override
                public void onSuccess(String contentType, Object result) {
                    try {
                        JsonObject version = new JsonParser().parse((String) result).getAsJsonObject();
                        boolean prerelease = version.get("prerelease").getAsBoolean();
                        if (prerelease) {
                            onFailure(null);
                            return;
                        }
                        JsonArray assets = version.get("assets").getAsJsonArray();
                        if (assets.size() > 0) {
                            checking = false;
                            String oldVersion = HViewerApplication.getVersionName();
                            String newVersion = version.get("tag_name").getAsString().substring(1);
                            String url = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                            String detail = version.get("body").getAsString();
                            new UpdateManager(activity, url, newVersion + "版本更新", detail)
                                    .checkUpdateInfo(oldVersion, newVersion);
                        } else {
                            onFailure(null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(null);
                    }
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    activity.showSnackBar("当前已是最新版本！");
                    checking = false;
                }
            });
        }
    }

}
