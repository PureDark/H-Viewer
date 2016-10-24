package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataholders.SiteTagHolder;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.SiteFlagHandler;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.DensityUtil;

import static com.umeng.analytics.a.p;

public class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int TYPE_LIST = 1;
    public final static int TYPE_GRID = 2;
    public final static int TYPE_WATERFALL = 3;
    private Context context;
    private ListDataProvider<Collection> mProvider;
    private OnItemClickListener mItemClickListener;
    private Site site;
    private boolean isGrid = false;
    private boolean waterfallAsList = false;
    private boolean waterfallAsGrid = false;
    private SiteTagHolder siteTagHolder;
    private Map<Collection, Integer> storedHeights = new HashMap<>();

    public CollectionAdapter(Context context, ListDataProvider<Collection> mProvider, SiteTagHolder siteTagHolder) {
        this.mProvider = mProvider;
        setHasStableIds(false);
        this.context = context;
        this.siteTagHolder = siteTagHolder;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LIST) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection, parent, false);
            RecyclerView.ViewHolder vh = new CollectionViewHolder(v);
            return vh;
        } else if (viewType == TYPE_WATERFALL) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection_waterfall, parent, false);
            RecyclerView.ViewHolder vh = new CollectionWaterfallViewHolder(v);
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection_grid, parent, false);
            RecyclerView.ViewHolder vh = new CollectionGridViewHolder(v);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Collection collection = mProvider.getItem(position);
        Logger.d("CollectionAdapter", "collection.cover:" + collection.cover);
        if (viewHolder instanceof CollectionViewHolder) {
            CollectionViewHolder holder = (CollectionViewHolder) viewHolder;
            String cookie = (site == null) ? "" : site.cookie;
            if (collection instanceof LocalCollection) {
                cookie = ((LocalCollection) collection).site.cookie;
            }
            ImageLoader.loadImageFromUrl(context, holder.ivCover, collection.cover, cookie, collection.referer);
            holder.tvTitle.setText(collection.title);
            holder.tvUploader.setText(collection.uploader);
            holder.tvCategory.setText(collection.category);
            if (collection.tags == null) {
                holder.tvTitle.setMaxLines(2);
                holder.rvTags.setAdapter(
                        new CollectionTagAdapter(new ListDataProvider<>(new ArrayList()))
                );
            } else {
                holder.tvTitle.setMaxLines(1);
                holder.rvTags.setAdapter(
                        new CollectionTagAdapter(new ListDataProvider<>(collection.tags))
                );
            }
            holder.rbRating.setRating(collection.rating);
            holder.tvSubmittime.setText(collection.datetime);
            if (site != null) {
                checkSiteFlags(position, site, collection);
            } else if (collection instanceof LocalCollection) {
                holder.layoutCover.setVisibility(View.VISIBLE);
                holder.tvTitle.setVisibility(View.VISIBLE);
                holder.rbRating.setVisibility(View.VISIBLE);
                holder.rvTags.setVisibility(View.VISIBLE);
                checkSiteFlags(holder, ((LocalCollection) collection).site);
                checkSiteFlags(position, ((LocalCollection) collection).site, collection);
            }
        } else if (viewHolder instanceof CollectionGridViewHolder) {
            CollectionGridViewHolder holder = (CollectionGridViewHolder) viewHolder;
            String cookie = (site == null) ? "" : site.cookie;
            if (collection instanceof LocalCollection) {
                cookie = ((LocalCollection) collection).site.cookie;
            }
            ImageLoader.loadImageFromUrl(context, holder.ivCover, collection.cover, cookie, collection.referer);
        } else if (viewHolder instanceof CollectionWaterfallViewHolder) {
            CollectionWaterfallViewHolder holder = (CollectionWaterfallViewHolder) viewHolder;
            String cookie = (site == null) ? "" : site.cookie;
            if (collection instanceof LocalCollection) {
                cookie = ((LocalCollection) collection).site.cookie;
            }
            waterfallCheckTextView(holder.tvTitle, collection.title);
            waterfallCheckTextView(holder.tvUploader, collection.uploader);
            waterfallCheckTextView(holder.tvCategory, collection.category);
            waterfallCheckTextView(holder.tvSubmittime, collection.datetime);
            int originHeight = (storedHeights.get(collection) != null)
                    ? storedHeights.get(collection)
                    : DensityUtil.dp2px(context, 215);
            holder.ivCover.getLayoutParams().height = originHeight;
            holder.ivIcon.getLayoutParams().height = originHeight;
            holder.tvDescription.getLayoutParams().height = originHeight;
            holder.ivCover.requestLayout();
            holder.ivIcon.requestLayout();
            holder.tvDescription.requestLayout();
            if (TextUtils.isEmpty(collection.cover)) {
                holder.ivIcon.setVisibility(View.GONE);
                holder.ivCover.setVisibility(View.GONE);
                holder.tvDescription.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (TextUtils.isEmpty(collection.description))
                    holder.tvDescription.setVisibility(View.GONE);
                else {
                    holder.tvDescription.setVisibility(View.VISIBLE);
                    holder.tvDescription.setText(Html.fromHtml(collection.description, source -> new BitmapDrawable(), null));
                }
                holder.tvDescription.requestLayout();
                originHeight = holder.tvDescription.getHeight();
                storedHeights.put(collection, originHeight);
            } else {
                holder.ivIcon.setVisibility(View.VISIBLE);
                holder.ivCover.setVisibility(View.VISIBLE);
                holder.tvDescription.setVisibility(View.GONE);
                ImageLoader.loadImageFromUrl(context, holder.ivCover, collection.cover, cookie, collection.referer, new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onSubmit(String id, Object callerContext) {
                        super.onSubmit(id, callerContext);
                    }

                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                        super.onFinalImageSet(id, imageInfo, anim);
                        if (imageInfo == null) {
                            return;
                        }
                        holder.ivCover.post(() -> {
                            final float factor = (float) imageInfo.getHeight() / imageInfo.getWidth();
                            final int originWidth = holder.ivCover.getWidth();
                            final int originHeight = (int) (factor * originWidth);
                            holder.ivCover.getLayoutParams().height = originHeight;
                            holder.ivIcon.getLayoutParams().height = originHeight;
                            holder.ivCover.requestLayout();
                            holder.ivIcon.requestLayout();
                            storedHeights.put(collection, originHeight);
                        });
                    }

                    @Override
                    public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        FLog.e(getClass(), throwable, "Error loading %s", id);
                    }
                });
                if (site != null) {
                    checkSiteFlags(position, site, collection);
                } else if (collection instanceof LocalCollection) {
                    holder.tvTitle.setVisibility(View.VISIBLE);
                    checkSiteFlags(holder, ((LocalCollection) collection).site);
                    checkSiteFlags(position, ((LocalCollection) collection).site, collection);
                }
            }
        }
    }

    private void waterfallCheckTextView(TextView textView, String text) {
        if (TextUtils.isEmpty(text))
            textView.setVisibility(View.GONE);
        else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }

    private void checkSiteFlags(int position, Site site, Collection collection) {
        if (site.hasFlag(Site.FLAG_PRELOAD_GALLERY) && !collection.preloaded) {
            SiteFlagHandler.preloadGallery(context, this, position, site, collection, siteTagHolder);
        }
    }

    private void checkSiteFlags(CollectionViewHolder holder, Site site) {
        if (site.hasFlag(Site.FLAG_NO_COVER)) {
            holder.layoutCover.setVisibility(View.GONE);
        }
        if (site.hasFlag(Site.FLAG_NO_TITLE)) {
            holder.tvTitle.setVisibility(View.GONE);
            if (holder.rvTags != null) {
                holder.rvTags.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.HORIZONTAL));
            }
        } else {
            if (holder.rvTags != null) {
                holder.rvTags.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.HORIZONTAL));
            }
        }
        if (site.hasFlag(Site.FLAG_NO_RATING)) {
            holder.rbRating.setVisibility(View.GONE);
        }
        if (site.hasFlag(Site.FLAG_NO_TAG)) {
            holder.tvTitle.setMaxLines(2);
            holder.rvTags.setVisibility(View.GONE);
            holder.rvTags.setAdapter(
                    new CollectionTagAdapter(new ListDataProvider<>(new ArrayList()))
            );
        }
    }

    private void checkSiteFlags(CollectionWaterfallViewHolder holder, Site site) {
        if (site.hasFlag(Site.FLAG_NO_TITLE)) {
            holder.tvTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (mProvider == null) ? 0 : mProvider.getCount();
    }

    @Override
    public long getItemId(int position) {
        return (mProvider == null) ? 0 : mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return (isGrid) ? (waterfallAsGrid ? TYPE_WATERFALL : TYPE_GRID) : (waterfallAsList ? TYPE_WATERFALL : TYPE_LIST);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setSite(Site site) {
        this.site = site;
        if (site != null && site.hasFlag(Site.FLAG_WATERFALL_AS_LIST))
            waterfallAsList = true;
        if (site != null && site.hasFlag(Site.FLAG_WATERFALL_AS_GRID))
            waterfallAsGrid = true;
    }

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public void setIsGrid(boolean isGrid) {
        this.isGrid = isGrid;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        boolean onItemLongClick(View v, int position);
    }

    public class CollectionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ripple_layout)
        public MaterialRippleLayout rippleLayout;
        @BindView(R.id.layout_cover)
        public RelativeLayout layoutCover;
        @BindView(R.id.iv_cover)
        public ImageView ivCover;
        @BindView(R.id.tv_title)
        public TextView tvTitle;
        @BindView(R.id.tv_uploader)
        public TextView tvUploader;
        @BindView(R.id.tv_category)
        public TextView tvCategory;
        @BindView(R.id.rv_tags)
        public RecyclerView rvTags;
        @BindView(R.id.rb_rating)
        public RatingBar rbRating;
        @BindView(R.id.tv_submittime)
        public TextView tvSubmittime;

        public CollectionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            if (site != null)
                checkSiteFlags(this, site);
            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            view.setOnLongClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                else
                    return false;
            });
            rippleLayout.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            rippleLayout.setOnLongClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                else
                    return false;
            });
        }

    }

    public class CollectionGridViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ripple_layout)
        MaterialRippleLayout rippleLayout;
        @BindView(R.id.iv_cover)
        ImageView ivCover;

        public CollectionGridViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            view.setOnLongClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                else
                    return false;
            });
            rippleLayout.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            rippleLayout.setOnLongClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                else
                    return false;
            });
        }

    }

    public class CollectionWaterfallViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.iv_cover)
        SimpleDraweeView ivCover;
        @BindView(R.id.tv_description)
        TextView tvDescription;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_submittime)
        TextView tvSubmittime;
        @BindView(R.id.tv_uploader)
        TextView tvUploader;
        @BindView(R.id.tv_category)
        TextView tvCategory;
        @BindView(R.id.ripple_layout)
        MaterialRippleLayout rippleLayout;

        public CollectionWaterfallViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            if (site != null)
                checkSiteFlags(this, site);
            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            view.setOnLongClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                else
                    return false;
            });
            rippleLayout.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            rippleLayout.setOnLongClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                else
                    return false;
            });
        }
    }
}