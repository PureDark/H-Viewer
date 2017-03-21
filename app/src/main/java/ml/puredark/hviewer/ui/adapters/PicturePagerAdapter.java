package ml.puredark.hviewer.ui.adapters;

import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.ui.activities.PictureViewerActivity;
import ml.puredark.hviewer.ui.customs.AreaClickHelper;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;

import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_LEFT_TO_RIGHT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_RIGHT_TO_LEFT;

/**
 * Created by PureDark on 2016/10/5.
 */


public class PicturePagerAdapter extends PagerAdapter {

    public List<Picture> pictures;
    private String viewDirection = DIREACTION_LEFT_TO_RIGHT;
    private PictureViewerActivity activity;
    private Site site;
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

    public String getViewDirection() {
        return viewDirection;
    }

    public void setViewDirection(String viewDirection) {
        this.viewDirection = viewDirection;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setAreaClickListener(AreaClickHelper.OnAreaClickListener onAreaClickListener) {
        areaClickHelper.setAreaClickListener(onAreaClickListener);
    }

    public void onConfigurationChanged() {
        areaClickHelper.updateScreenSize(activity, 0, 0);
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

    public PictureViewHolder getViewHolderAt(int position) {
        if (position >= 0 && position < viewHolders.size())
            return viewHolders.get(getPicturePostion(position));
        else
            return null;
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
            activity.getUrlAndLoadImage(viewHolder, picture, false);
            viewHolder.btnRefresh.setOnClickListener(v -> {
                activity.getUrlAndLoadImage(viewHolder, picture, false);
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

    public class PictureViewHolder {
        @BindView(R.id.iv_picture)
        public PhotoDraweeView ivPicture;
        @BindView(R.id.progress_bar)
        public ProgressBarCircularIndeterminate progressBar;
        @BindView(R.id.btn_refresh)
        public ImageView btnRefresh;
        View view;

        public PictureViewHolder(View view) {
            ButterKnife.bind(this, view);
            this.view = view;
            ivPicture.setOrientation(LinearLayout.HORIZONTAL);
        }
    }


}