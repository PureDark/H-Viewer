package ml.puredark.hviewer.helpers;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.nostra13.universalimageloader.core.imageaware.ViewAware;

public class BgViewAware extends ViewAware {

    public BgViewAware(View view) {
        super(view);
    }

    public BgViewAware(View view, boolean checkActualViewSize) {
        super(view, checkActualViewSize);
    }

    @Override
    protected void setImageDrawableInto(Drawable drawable, View view) {
        view.setBackgroundDrawable(drawable);
    }

    @Override
    protected void setImageBitmapInto(Bitmap bitmap, View view) {
        view.setBackgroundDrawable(new BitmapDrawable(view.getResources(), bitmap));
    }
}