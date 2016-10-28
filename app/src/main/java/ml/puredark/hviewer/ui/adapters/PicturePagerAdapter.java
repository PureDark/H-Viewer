package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.activities.PictureViewerActivity;
import ml.puredark.hviewer.ui.customs.AreaClickHelper;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;
import ml.puredark.hviewer.utils.RegexValidateUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_LEFT_TO_RIGHT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_RIGHT_TO_LEFT;

/**
 * Created by PureDark on 2016/10/5.
 */


public class PicturePagerAdapter extends PagerAdapter {

    private String viewDirection = DIREACTION_LEFT_TO_RIGHT;

    private PictureViewerActivity activity;

    private Site site;

    public List<Picture> pictures;
    private List<PictureViewHolder> viewHolders = new ArrayList<>();

    private OnItemLongClickListener mOnItemLongClickListener;
    private boolean firstTime = true;

    private AreaClickHelper areaClickHelper;

    public PicturePagerAdapter(PictureViewerActivity activity, Site site, List<Picture> pictures) {
        this.activity = activity;
        this.site = site;
        this.pictures = pictures;
        for (int i = 0; i < getCount(); i++)
            viewHolders.add(null);
        areaClickHelper = new AreaClickHelper(activity);
    }

    public class PictureViewHolder {
        View view;
        @BindView(R.id.iv_picture)
        public PhotoDraweeView ivPicture;
        @BindView(R.id.progress_bar)
        public ProgressBarCircularIndeterminate progressBar;
        @BindView(R.id.btn_refresh)
        public ImageView btnRefresh;

        public PictureViewHolder(View view) {
            ButterKnife.bind(this, view);
            this.view = view;
            ivPicture.setOrientation(LinearLayout.HORIZONTAL);
        }
    }

    public void setViewDirection(String viewDirection) {
        this.viewDirection = viewDirection;
    }

    public String getViewDirection() {
        return viewDirection;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setAreaClickListener(AreaClickHelper.OnAreaClickListener onAreaClickListener) {
        areaClickHelper.setAreaClickListener(onAreaClickListener);
    }

    public void onConfigurationChanged() {
        areaClickHelper = new AreaClickHelper(activity);
    }

    @Override
    public int getItemPosition(Object object) {
        if (firstTime)
            return POSITION_NONE;
        return super.getItemPosition(object);
    }

    public int getPicturePostion(int position) {
        if (DIREACTION_LEFT_TO_RIGHT.equals(viewDirection)) {
            return position;
        } else if (DIREACTION_RIGHT_TO_LEFT.equals(viewDirection)) {
            return getCount() - 1 - position;
        }
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        if (getCount() > viewHolders.size()) {
            int size = getCount() - viewHolders.size();
            for (int i = 0; i < size; i++)
                viewHolders.add(null);
        }
        if (firstTime)
            new Handler().postDelayed(() -> firstTime = false, 500);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (pictures == null)
            return 1;
        if (pictures.size() == 0)
            return 1;
        return pictures.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (viewHolders.size() > position && viewHolders.get(position) != null) {
            if (viewHolders.get(position).view != null)
                container.removeView(viewHolders.get(position).view);
            viewHolders.set(position, null);
        }
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.view_picture_viewer, null);
        final PictureViewHolder viewHolder = new PictureViewHolder(view);

        if (pictures != null && position < pictures.size()) {
            final Picture picture = pictures.get(getPicturePostion(position));
            if (picture.pic != null) {
                activity.loadImage(picture, viewHolder);
            } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null) {
                if (site.extraRule.pictureRule != null && site.extraRule.pictureRule.url != null)
                    activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureRule.url, site.extraRule.pictureRule.highRes);
                else if (site.extraRule.pictureUrl != null)
                    activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
            } else if (site.picUrlSelector != null) {
                activity.getPictureUrl(viewHolder, picture, site.picUrlSelector, null);
            } else {
                picture.pic = picture.url;
                activity.loadImage(picture, viewHolder);
            }
            viewHolder.btnRefresh.setOnClickListener(v -> {
                if (picture.pic != null) {
                    activity.loadImage(picture, viewHolder);
                } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null) {
                    if (site.extraRule.pictureRule != null && site.extraRule.pictureRule.url != null)
                        activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureRule.url, site.extraRule.pictureRule.highRes);
                    else if (site.extraRule.pictureUrl != null)
                        activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
                } else if (site.picUrlSelector == null) {
                    picture.pic = picture.url;
                    activity.loadImage(picture, viewHolder);
                } else {
                    activity.getPictureUrl(viewHolder, picture, site.picUrlSelector, null);
                }
            });
            viewHolder.ivPicture.setOnLongClickListener(v -> {
                if (mOnItemLongClickListener != null)
                    return mOnItemLongClickListener.onItemLongClick(v, getPicturePostion(position));
                else
                    return false;
            });
            viewHolder.ivPicture.setOnViewTapListener((v, x, y) -> {
                if (viewHolder.ivPicture.getScale() <= 1) {
                    areaClickHelper.onClick(x, y);
                }
            });
        }
        viewHolders.set(position, viewHolder);
        container.addView(viewHolder.view, 0);
        return viewHolder.view;
    }


}