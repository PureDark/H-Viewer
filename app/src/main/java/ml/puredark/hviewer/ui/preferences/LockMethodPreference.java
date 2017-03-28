package ml.puredark.hviewer.ui.preferences;

import android.content.Context;
import android.preference.Preference;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.TextUtils;
import android.util.AttributeSet;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.ui.fragments.LockMethodFragment;

/**
 * Created by PureDark on 2016/10/9.
 */

public class LockMethodPreference extends Preference {

    public LockMethodPreference(Context context) {
        super(context);
    }

    public LockMethodPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LockMethodPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {
        Context context = getContext();
        FingerprintManagerCompat manager = FingerprintManagerCompat.from(context);
        boolean isFingerPrintLock = false;
        try {
            isFingerPrintLock = manager.hasEnrolledFingerprints();
        } catch (Exception e){
        }
        boolean isPatternLock = LockMethodFragment.getCurrentLockMethod(context) == LockMethodFragment.METHOD_PATTERN;
        boolean isPinLock = LockMethodFragment.getCurrentLockMethod(context) == LockMethodFragment.METHOD_PIN;
        String summary = (isPatternLock) ? "图案解锁" : (isPinLock) ? "数字解锁" : "";
        if (!TextUtils.isEmpty(summary))
            summary += (isFingerPrintLock) ? "、指纹" : "";
        else
            summary = context.getString(R.string.settings_lock_methods_detail_summary);
        return summary;
    }

}
