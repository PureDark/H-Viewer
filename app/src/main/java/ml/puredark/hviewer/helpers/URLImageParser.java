package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;

import ml.puredark.hviewer.http.ImageLoader;

/**
 * Created by PureDark on 2016/10/1.
 */

public class URLImageParser implements Html.ImageGetter {
    private Context context;
    private TextView textView;
    private String cookie, referer;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     *
     * @param textView
     * @param context
     */
    public URLImageParser(Context context, TextView textView, String cookie, String referer) {
        this.context = context;
        this.textView = textView;
        this.cookie = cookie;
        this.referer = referer;
    }

    @Override
    public Drawable getDrawable(String source) {
        Logger.d("URLImageParser", "source:" + source);
        final LevelListDrawable mDrawable = new LevelListDrawable();
        Drawable empty = new BitmapDrawable();
        mDrawable.addLevel(0, 0, empty);
        mDrawable.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
        // get the actual source
        ImageLoader.loadBitmapFromUrl(context, source, cookie, referer, new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                if (bitmap != null) {
                    Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Drawable drawable = new BitmapDrawable(context.getResources(), copy);
                    mDrawable.addLevel(1, 1, drawable);
                    mDrawable.setBounds(0, 0, copy.getWidth(), copy.getHeight());
                    mDrawable.setLevel(1);
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            CharSequence t = textView.getText();
                            textView.setText(t);
                        }
                    });
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            }
        });

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return mDrawable;
    }
}
