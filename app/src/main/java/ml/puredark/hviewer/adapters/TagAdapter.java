package ml.puredark.hviewer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.dataproviders.ListDataProvider;

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ListDataProvider mProvider;

    public TagAdapter(ListDataProvider mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(true);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        // 在这里对View的参数进行设置
        TagViewHolder vh = new TagViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Tag tag = (Tag) mProvider.getItem(position);
        TagViewHolder holder = (TagViewHolder) viewHolder;
        holder.tvTag.setText(tag.title);
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


    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_tag)
        TextView tvTag;

        public TagViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}