package ml.puredark.hviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.activities.SetPinActivity;
import ml.puredark.hviewer.ui.fragments.LockMethodFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2016/10/9.
 */

public class SetPinLockPreference extends Preference
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SetPinLockPreference(Context context) {
        super(context);
    }

    public SetPinLockPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SetPinLockPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public CharSequence getSummary() {
        Context context = getContext();
        String pin = (String) SharedPreferencesUtil.getData(context, LockMethodFragment.KEY_PREF_PIN_LOCK, "");
        return !TextUtils.isEmpty(pin) ?
                context.getString(R.string.setting_summary_set_pin_has) :
                context.getString(R.string.setting_summary_set_pin_none);
    }

    @Override
    protected void onClick() {
        Context context = getContext();
        context.startActivity(new Intent(context, SetPinActivity.class));
    }

    @Override
    public boolean shouldDisableDependents() {
        String pin = (String) SharedPreferencesUtil.getData(getContext(), LockMethodFragment.KEY_PREF_PIN_LOCK, "");
        return super.shouldDisableDependents() || TextUtils.isEmpty(pin);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, LockMethodFragment.KEY_PREF_PIN_LOCK)) {
            notifyChanged();
            notifyDependencyChange(shouldDisableDependents());
        }
    }
}
