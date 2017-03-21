package ml.puredark.hviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.ui.fragments.SettingFragment.KEY_PREF_PROXY_SERVER;

/**
 * Created by PureDark on 2016/9/25.
 */
public class ProxyFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private BaseActivity activity;

    public ProxyFragment() {
    }

    @SuppressLint("ValidFragment")
    public ProxyFragment(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
        addPreferencesFromResource(R.xml.proxy);

        String proxyServer = getPreferenceManager().getSharedPreferences().getString(KEY_PREF_PROXY_SERVER, null);
        if (proxyServer != null)
            getPreferenceManager().findPreference(KEY_PREF_PROXY_SERVER).setSummary(proxyServer);

        getPreferenceManager().findPreference(KEY_PREF_PROXY_SERVER).setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_PREF_PROXY_SERVER)) {
            getPreferenceManager().findPreference(KEY_PREF_PROXY_SERVER).setSummary((String) newValue);
        }
        return true;
    }
}
