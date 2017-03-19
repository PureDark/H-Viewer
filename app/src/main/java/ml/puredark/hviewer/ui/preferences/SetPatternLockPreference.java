package ml.puredark.hviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.activities.SetPatternActivity;
import ml.puredark.hviewer.ui.fragments.LockMethodFragment;
import ml.puredark.hviewer.utils.PatternLockUtils;

/**
 * Created by PureDark on 2016/10/9.
 */

public class SetPatternLockPreference extends Preference
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SetPatternLockPreference(Context context) {
        super(context);
    }

    public SetPatternLockPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SetPatternLockPreference(Context context, AttributeSet attrs) {
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
        return PatternLockUtils.hasPattern(context) ?
                context.getString(R.string.setting_summary_set_pattern_has) :
                context.getString(R.string.setting_summary_set_pattern_none);
    }

    @Override
    protected void onClick() {
        Context context = getContext();
        context.startActivity(new Intent(context, SetPatternActivity.class));
    }

    @Override
    public boolean shouldDisableDependents() {
        return super.shouldDisableDependents() || !PatternLockUtils.hasPattern(getContext());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, LockMethodFragment.KEY_PREF_PATTERN_LOCK)) {
            notifyChanged();
            notifyDependencyChange(shouldDisableDependents());
        }
    }
}
