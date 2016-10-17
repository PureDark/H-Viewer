package ml.puredark.hviewer.ui.customs;

import android.content.Context;

import ml.puredark.hviewer.utils.DensityUtil;

/**
 * Created by PureDark on 2016/10/17.
 */

public class AreaClickHelper {
    private int xOffset, yOffset;
    private OnAreaClickListener onAreaClickListener;
    private int width, height;

    public AreaClickHelper(Context context) {
        width = DensityUtil.getScreenWidth(context);
        height = DensityUtil.getScreenHeight(context);
        xOffset = width * 3 / 10;
        yOffset = height * 3 / 10;
    }

    public AreaClickHelper(Context context, int xOffset, int yOffset) {
        width = DensityUtil.getScreenWidth(context);
        height = DensityUtil.getScreenHeight(context);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public void onClick(float x, float y) {
        if (onAreaClickListener == null)
            return;
        if (x < xOffset) {
            onAreaClickListener.left();
        } else if (x > width - xOffset){
            onAreaClickListener.right();
        }
        if (y < yOffset) {
            onAreaClickListener.top();
        } else if (y > height - yOffset){
            onAreaClickListener.bottom();
        }

    }

    public void setAreaClickListener(OnAreaClickListener onAreaClickListener) {
        this.onAreaClickListener = onAreaClickListener;
    }

    public interface OnAreaClickListener {
        void left();

        void right();

        void top();

        void bottom();
    }

    public static abstract class OnLeftRightClickListener implements OnAreaClickListener {
        public abstract void left();

        public abstract void right();

        @Override
        public void top() {
        }

        @Override
        public void bottom() {
        }
    }

    public static abstract class OnTopBottomClickListener implements OnAreaClickListener {
        @Override
        public void left() {
        }

        @Override
        public void right() {
        }

        public abstract void top();

        public abstract void bottom();
    }

}
