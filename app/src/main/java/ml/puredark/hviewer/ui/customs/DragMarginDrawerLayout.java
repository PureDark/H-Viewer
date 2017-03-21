package ml.puredark.hviewer.ui.customs;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;

import java.lang.reflect.Field;

import ml.puredark.hviewer.utils.DensityUtil;

/**
 * Created by PureDark on 2016/10/19.
 */

public class DragMarginDrawerLayout extends DrawerLayout {
    private Context mContext;

    public DragMarginDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        disablePeekOnEdgeTouch();
    }

    private void disablePeekOnEdgeTouch() {
        try {
            Field leftCallbackField = getDeclaredField("mLeftCallback");
            leftCallbackField.setAccessible(true);
            Object leftPeekRunnable = leftCallbackField.get(this);
            Field leftPeekField = leftPeekRunnable.getClass().getDeclaredField("mPeekRunnable");
            leftPeekField.setAccessible(true);
            leftPeekField.set(leftPeekRunnable, null);
            Field callbackField = getDeclaredField("mRightCallback");
            callbackField.setAccessible(true);
            Object rightPeekRunnable = callbackField.get(this);
            Field rightPeekField = rightPeekRunnable.getClass().getDeclaredField("mPeekRunnable");
            rightPeekField.setAccessible(true);
            rightPeekField.set(rightPeekRunnable, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDrawerEdgeSize(float displayWidthPercentage, boolean left) {
        try {
            Field draggerField = getDeclaredField(left ? "mLeftDragger" : "mRightDragger");
            draggerField.setAccessible(true);
            ViewDragHelper dragger = (ViewDragHelper) draggerField.get(this);
            Field edgeSizeField = dragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(dragger);
            int ScreenWidth = DensityUtil.getScreenWidth(mContext);
            edgeSizeField.setInt(dragger, Math.max(edgeSize, (int) (ScreenWidth * displayWidthPercentage)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDrawerLeftEdgeSize(float displayWidthPercentage) {
        setDrawerEdgeSize(displayWidthPercentage, true);
    }

    public void setDrawerRightEdgeSize(float displayWidthPercentage) {
        setDrawerEdgeSize(displayWidthPercentage, false);
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param fieldName : 父类中的属性名
     * @return 父类中的属性对象
     */
    public Field getDeclaredField(String fieldName) {
        Field field;

        Class<?> clazz = getClass();

        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                return field;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return null;
    }

}
