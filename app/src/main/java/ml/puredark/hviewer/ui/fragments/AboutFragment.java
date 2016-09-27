package ml.puredark.hviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.activities.AnimationActivity;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.ui.fragments.SettingFragment.KEY_PREF_MODE_R18_ENABLED;

/**
 * Created by PureDark on 2016/9/25.
 */
public class AboutFragment extends PreferenceFragment {
    public static final String KEY_PREF_ABOUT_AUTHOR = "pref_about_author";
    public static final String KEY_PREF_ABOUT_GITHUB = "pref_about_github";
    public static final String KEY_PREF_ABOUT_CHANGELOG = "pref_about_changelog";
    public static final String KEY_PREF_ABOUT_VERSION = "pref_about_version";
    public static final String KEY_PREF_ABOUT_DONATE = "pref_about_donate";

    private int pressCount = 0;

    private AnimationActivity activity;

    public AboutFragment() {
    }

    @SuppressLint("ValidFragment")
    public AboutFragment(AnimationActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
        addPreferencesFromResource(R.xml.about);
        Preference preference = getPreferenceScreen().findPreference(KEY_PREF_ABOUT_VERSION);
        preference.setSummary("v" + HViewerApplication.getVersionName());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_PREF_ABOUT_AUTHOR)) {
            String message = "";

            boolean r18enabled = (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext, KEY_PREF_MODE_R18_ENABLED, false);
            if (r18enabled) {
                message = "已开启R18模式！无法关闭！哈哈哈后悔吧！";
            } else {
                switch ((pressCount)) {
                    case 0:
                        message = "为什么按我，觉得我帅吗";
                        break;
                    case 1:
                        message = "快住手！还有5步，我体内的洪荒之力就要爆发了！";
                        break;
                    case 2:
                        message = "别这样！还有4步，我控几不住我记几啊！";
                        break;
                    case 3:
                        message = "还有3步！啊，我的右手！我的王の几旮旯！";
                        break;
                    case 4:
                        message = "还有2步！现在停手还来得及！";
                        break;
                    case 5:
                        message = "还有1步！你一定会后悔的！";
                        break;
                    case 6:
                        message = "已开启R18模式";
                        SharedPreferencesUtil.saveData(HViewerApplication.mContext, KEY_PREF_MODE_R18_ENABLED, true);
                        break;
                }
            }
            activity.showSnackBar(message);
            pressCount++;
        } else if (preference.getKey().equals(KEY_PREF_ABOUT_GITHUB)
                || preference.getKey().equals(KEY_PREF_ABOUT_CHANGELOG)) {
            String url = preference.getSummary().toString();
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(it);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


}
