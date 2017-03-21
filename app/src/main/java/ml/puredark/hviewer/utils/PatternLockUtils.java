/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package ml.puredark.hviewer.utils;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;
import ml.puredark.hviewer.ui.fragments.LockMethodFragment;

public class PatternLockUtils {
    public static void setPattern(Context context, List<PatternView.Cell> pattern) {
        SharedPreferencesUtil.saveData(context, LockMethodFragment.KEY_PREF_PATTERN_LOCK,
                PatternUtils.patternToSha1String(pattern));
    }

    private static String getPatternSha1(Context context) {
        return (String) SharedPreferencesUtil.getData(context, LockMethodFragment.KEY_PREF_PATTERN_LOCK, "");
    }

    public static boolean hasPattern(Context context) {
        return !TextUtils.isEmpty(getPatternSha1(context));
    }

    public static boolean isPatternCorrect(Context context, List<PatternView.Cell> pattern) {
        return TextUtils.equals(PatternUtils.patternToSha1String(pattern), getPatternSha1(context));
    }

    public static void clearPattern(Context context) {
        SharedPreferencesUtil.deleteData(context, LockMethodFragment.KEY_PREF_PATTERN_LOCK);
    }
}
