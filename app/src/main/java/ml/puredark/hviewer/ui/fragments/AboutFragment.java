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

/**
 * Created by PureDark on 2016/9/25.
 */
public class AboutFragment extends PreferenceFragment {
    public static final String KEY_PREF_ABOUT_AUTHOR = "pref_about_author";
    public static final String KEY_PREF_ABOUT_GITHUB = "pref_about_github";
    public static final String KEY_PREF_ABOUT_CHANGELOG = "pref_about_changelog";
    public static final String KEY_PREF_ABOUT_VERSION = "pref_about_version";
    public static final String KEY_PREF_ABOUT_DONATE = "pref_about_donate";

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
            activity.showSnackBar("为什么按我，觉得我帅吗");
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
