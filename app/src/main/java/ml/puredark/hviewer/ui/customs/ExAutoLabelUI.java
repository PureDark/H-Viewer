package ml.puredark.hviewer.ui.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.dpizarro.autolabel.library.AutoLabelUI;

import java.util.ArrayList;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.ui.adapters.SiteTagAdapter;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

/**
 * Created by PureDark on 2017/9/20.
 */

public class ExAutoLabelUI extends AutoLabelUI {
    private TypedArray typedArray;
    private SiteTagAdapter mSiteTagAdapter;

    public ExAutoLabelUI(Context context) {
        super(context);
    }

    public ExAutoLabelUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExAutoLabelUI);
            preview(context, typedArray);
        }
    }

    public ExAutoLabelUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExAutoLabelUI);
            preview(context, typedArray);
        }
    }

    public void preview(Context context, TypedArray a){
        mSiteTagAdapter = new SiteTagAdapter(new ListDataProvider(new ArrayList<>()));
        mSiteTagAdapter.setLabelView(this);
        final String tagStrArr = a.getString(R.styleable.ExAutoLabelUI_tagArray);
        final String[] tagRealStrArr = getRealStrArr(tagStrArr);
        a.recycle();
        for(String tag : tagRealStrArr){
            mSiteTagAdapter.getDataProvider().addItem(new Tag(0, tag));
        }
        mSiteTagAdapter.notifyDataSetChanged();
    }

    private String[] getRealStrArr(String strArr) {
        if (strArr != null && !strArr.equals(""))
            return strArr.split(",");
        else
            return new String[0];
    }
}
