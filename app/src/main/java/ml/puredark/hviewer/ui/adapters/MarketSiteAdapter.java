package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.gc.materialdesign.views.ButtonFlat;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.MarketSiteCategory;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.RegexValidateUtil;

public class MarketSiteAdapter extends RecyclerView.Adapter<MarketSiteAdapter.MarketSiteViewHolder> {
    private Context context;
    private ListDataProvider<MarketSiteCategory.MarketSite> mProvider;
    private ItemListener mItemListener;
    private String categoryTitle;

    public MarketSiteAdapter(Context context, ListDataProvider<MarketSiteCategory.MarketSite> mProvider, String categoryTitle) {
        this.mProvider = mProvider;
        setHasStableIds(false);
        this.context = context;
        this.categoryTitle = categoryTitle;
    }

    @Override
    public MarketSiteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_market_site, parent, false);
        MarketSiteViewHolder vh = new MarketSiteViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MarketSiteViewHolder holder, int position) {
        MarketSiteCategory.MarketSite marketSite = mProvider.getItem(position);
        if (!TextUtils.isEmpty(marketSite.icon))
            ImageLoader.loadImageFromUrl(context, holder.ivFavicon, marketSite.icon, null, RegexValidateUtil.getHostFromUrl(marketSite.icon));
        holder.tvTitle.setText(marketSite.title);
        if (TextUtils.isEmpty(marketSite.description)) {
            holder.tvDescription.setText("@" + marketSite.author);
        } else {
            holder.tvDescription.setText(marketSite.description + " @" + marketSite.author);
        }
        if (mItemListener != null) {
            mItemListener.onItemCheckUpdate(holder, position, marketSite);
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
        return 0;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public void setItemListener(ItemListener listener) {
        this.mItemListener = listener;
    }

    public interface ItemListener {
        void onItemCheckUpdate(MarketSiteViewHolder holder, int position, MarketSiteCategory.MarketSite marketSite);

        void onItemBtnAddClick(View v, int position, MarketSiteCategory.MarketSite marketSite, String categoryTitle);
    }

    public class MarketSiteViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_favicon)
        public SimpleDraweeView ivFavicon;
        @BindView(R.id.tv_title)
        public TextView tvTitle;
        @BindView(R.id.tv_description)
        public TextView tvDescription;
        @BindView(R.id.btn_add)
        public ButtonFlat btnAdd;

        public MarketSiteViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            btnAdd.setOnClickListener(v -> {
                if (mItemListener != null && getAdapterPosition() >= 0) {
                    MarketSiteCategory.MarketSite marketSite = mProvider.getItem(getAdapterPosition());
                    mItemListener.onItemBtnAddClick(v, getAdapterPosition(), marketSite, categoryTitle);
                }
            });
        }

    }
}