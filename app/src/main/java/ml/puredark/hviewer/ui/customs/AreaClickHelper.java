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
        updateScreenSize(context, 0, 0);
    }

    public AreaClickHelper(Context context, int xOffset, int yOffset) {
        updateScreenSize(context, xOffset, yOffset);
    }

    public void updateScreenSize(Context context, int xOffset, int yOffset) {
        this.width = DensityUtil.getScreenWidth(context);
        this.height = DensityUtil.getScreenHeight(context);
        this.xOffset = (xOffset > 0) ? xOffset : width * 3 / 10;
        this.yOffset = (yOffset > 0) ? yOffset : height * 3 / 10;
    }

    public void onClick(float x, float y) {
        if (onAreaClickListener == null)
            return;
        if (x < xOffset) {
            if (y < yOffset)
                onAreaClickListener.grid1();
            else if (y > height - yOffset)
                onAreaClickListener.grid7();
            else
                onAreaClickListener.grid4();
        } else if (x > width - xOffset) {
            if (y < yOffset)
                onAreaClickListener.grid3();
            else if (y > height - yOffset)
                onAreaClickListener.grid9();
            else
                onAreaClickListener.grid6();
        } else {
            if (y < yOffset)
                onAreaClickListener.grid2();
            else if (y > height - yOffset)
                onAreaClickListener.grid8();
            else
                onAreaClickListener.grid5();
        }

    }

    public void setAreaClickListener(OnAreaClickListener onAreaClickListener) {
        this.onAreaClickListener = onAreaClickListener;
    }

    public interface OnAreaClickListener {
        void grid1();

        void grid2();

        void grid3();

        void grid4();

        void grid5();

        void grid6();

        void grid7();

        void grid8();

        void grid9();
    }

    public static abstract class OnLeftRightClickListener implements OnAreaClickListener {
        public abstract void left();

        public abstract void right();

        public abstract void center();

        @Override
        public void grid1() {
            left();
        }

        @Override
        public void grid2() {
            center();
        }

        @Override
        public void grid3() {
            right();
        }

        @Override
        public void grid4() {
            left();
        }

        @Override
        public void grid5() {
            center();
        }

        @Override
        public void grid6() {
            right();
        }

        @Override
        public void grid7() {
            left();
        }

        @Override
        public void grid8() {
            center();
        }

        @Override
        public void grid9() {
            right();
        }
    }

    public static abstract class OnTopBottomClickListener implements OnAreaClickListener {
        public abstract void top();

        public abstract void bottom();

        public abstract void center();

        @Override
        public void grid1() {
            top();
        }

        @Override
        public void grid2() {
            top();
        }

        @Override
        public void grid3() {
            top();
        }

        @Override
        public void grid4() {
            center();
        }

        @Override
        public void grid5() {
            center();
        }

        @Override
        public void grid6() {
            center();
        }

        @Override
        public void grid7() {
            bottom();
        }

        @Override
        public void grid8() {
            bottom();
        }

        @Override
        public void grid9() {
            bottom();
        }
    }

}
