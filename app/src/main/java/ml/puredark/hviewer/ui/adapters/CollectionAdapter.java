package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.helpers.SiteFlagHandler;

public class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int TYPE_LIST = 1;
    public final static int TYPE_GRID = 2;
    private Context context;
    private ListDataProvider mProvider;
    private OnItemClickListener mItemClickListener;
    private Site site;
    private boolean isGrid = false;

    public CollectionAdapter(Context context, ListDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LIST) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection, parent, false);
            RecyclerView.ViewHolder vh = new CollectionViewHolder(v);
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
        Collection collection = (Collection) mProvider.getItem(position);
        if (viewHolder instanceof CollectionViewHolder) {
            CollectionViewHolder holder = (CollectionViewHolder) viewHolder;
            String cookie = (site == null) ? "" : site.cookie;
            if (collection instanceof LocalCollection) {
                cookie = ((LocalCollection) collection).site.cookie;
            }
            Logger.d("CollectionAdapter", "collection.cover:" + collection.cover);
            ImageLoader.loadImageFromUrl(context, holder.ivCover, collection.cover, cookie, collection.referer);
            holder.tvTitle.setText(collection.title);
            holder.tvUploader.setText(collection.uploader);
            holder.tvCategory.setText(collection.category);
            if (collection.tags == null) {
                holder.tvTitle.setMaxLines(2);
                holder.rvTags.setAdapter(
                        new TagAdapter(new ListDataProvider<>(new ArrayList()))
                );
            } else {
                holder.tvTitle.setMaxLines(1);
                holder.rvTags.setAdapter(
                        new TagAdapter(new ListDataProvider<>(collection.tags))
                );
            }
            holder.rbRating.setRating(collection.rating);
            holder.tvSubmittime.setText(collection.datetime);
            if(site!=null) {
                checkSiteFlags(holder, site, collection);
            }else if (collection instanceof LocalCollection) {
                holder.layoutCover.setVisibility(View.VISIBLE);
                holder.tvTitle.setVisibility(View.VISIBLE);
                holder.rbRating.setVisibility(View.VISIBLE);
                holder.rvTags.setVisibility(View.VISIBLE);
                checkSiteFlags(holder, ((LocalCollection) collection).site);
                checkSiteFlags(holder, ((LocalCollection) collection).site, collection);
            }
        } else if (viewHolder instanceof CollectionGridViewHolder) {
            CollectionGridViewHolder holder = (CollectionGridViewHolder) viewHolder;
            String cookie = (site == null) ? "" : site.cookie;
            if (collection instanceof LocalCollection) {
                cookie = ((LocalCollection) collection).site.cookie;
            }
            ImageLoader.loadImageFromUrl(context, holder.ivCover, collection.cover, cookie, collection.referer);
        }
    }

    private void checkSiteFlags(CollectionViewHolder holder, Site site, Collection collection) {
        if (site.hasFlag(Site.FLAG_PRELOAD_GALLERY) && !collection.preloaded) {
            SiteFlagHandler.preloadGallery(holder, site, collection);
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
        }else{
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
                    new TagAdapter(new ListDataProvider<>(new ArrayList()))
            );
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
        return (isGrid) ? TYPE_GRID : TYPE_LIST;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setSite(Site site) {
        this.site = site;
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
            if(site!=null) {
                checkSiteFlags(this, site);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
            rippleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            rippleLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
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
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
            rippleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            rippleLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemClickListener != null && getAdapterPosition() >= 0)
                        return mItemClickListener.onItemLongClick(v, getAdapterPosition());
                    else
                        return false;
                }
            });
        }

    }
}