package ml.puredark.hviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.widget.BaseAdapter;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.utils.PatternLockUtils;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.R.string.refresh;

/**
 * Created by PureDark on 2016/9/25.
 */
public class LockMethodFragment extends PreferenceFragment {
    public static final String KEY_PREF_PIN_LOCK = "pref_pin_lock";
    public static final String KEY_PREF_PATTERN_LOCK = "pref_pattern_lock";
    public static final String KEY_PREF_CLEAR_LOCK_METHODS = "pref_clear_lock_methods";

    private BaseActivity activity;

    public LockMethodFragment() {
    }

    @SuppressLint("ValidFragment")
    public LockMethodFragment(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
        addPreferencesFromResource(R.xml.preferences_lock_methods);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh(getPreferenceScreen());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_PREF_CLEAR_LOCK_METHODS)) {
            //清空所有解锁方式
            new AlertDialog.Builder(activity).setTitle(activity.getString(R.string.setting_dialog_message_clear_lock_methods))
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        PatternLockUtils.clearPattern(activity);
                        SharedPreferencesUtil.deleteData(activity, KEY_PREF_PIN_LOCK);
                        activity.showSnackBar(activity.getString(R.string.setting_lock_methods_cleared));
                        refresh(preferenceScreen);
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void refresh(PreferenceScreen preferenceScreen){
        ((BaseAdapter)preferenceScreen.getRootAdapter()).notifyDataSetChanged();
    }

}
