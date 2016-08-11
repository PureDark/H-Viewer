package ml.puredark.hviewer.customs;

import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by PureDark on 2016/8/12.
 */

public class AnimationOnActivity {

    public AnimationOnActivity() {
    }

    public static void start(DrawerArrowDrawable btnReturnIcon, Animator.AnimatorListener listener) {
        ValueAnimator arrow = getArrowAnimator(btnReturnIcon);
        arrow.addListener(listener);
        arrow.start();
    }

    public static void reverse(DrawerArrowDrawable btnReturnIcon, Animator.AnimatorListener listener) {
        ValueAnimator arrow = getArrowAnimator(btnReturnIcon);
        arrow.addListener(listener);
        arrow.reverse();
    }

    static ValueAnimator getArrowAnimator(final DrawerArrowDrawable btnReturnIcon) {
        float start = 0f;
        float end = 1f;
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btnReturnIcon.setProgress((Float) animation.getAnimatedValue());
            }
        });
        return animator;
    }
}
