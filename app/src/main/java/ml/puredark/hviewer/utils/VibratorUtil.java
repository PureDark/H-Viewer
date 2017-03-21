package ml.puredark.hviewer.utils;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;


public class VibratorUtil {

    /**
     * 按一定模式震动
     *
     * @param context      上下文
     * @param milliseconds 震动时长，单位是毫秒
     */
    public static void Vibrate(Context context, long milliseconds) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }


    /**
     * 按一定模式震动
     *
     * @param context  上下文
     * @param pattern  自定义震动模式，数组含义：[静止时长，震动时长，静止时长，震动时长……] 单位是毫秒
     * @param isRepeat 是否重复震动
     */
    public static void Vibrate(Context context, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

}
