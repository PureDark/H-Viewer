package ml.puredark.hviewer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.balysv.materialripple.MaterialRippleLayout;
import com.dpizarro.autolabel.library.AutoLabelUI;
import com.dpizarro.autolabel.library.Label;

import java.util.List;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class SiteTagAdapter {
    private AutoLabelUI labelView;
    private ListDataProvider<Tag> mProvider;
    private OnItemClickListener mItemClickListener;

    public SiteTagAdapter(ListDataProvider<Tag> provider) {
        this.mProvider = provider;
    }

    public void setLabelView(AutoLabelUI labelView) {
        this.labelView = labelView;
        init();
    }

    private void init() {
        labelView.clear();
        List<Tag> tags = mProvider.getItems();
        for (Tag tag : tags) {
            labelView.addLabel(tag.title);
            labelView.setLayoutTransition(null);
            int position = labelView.getLabelsCounter() - 1;
            Label label = labelView.getLabel(position);
            View view = LayoutInflater.from(labelView.getContext()).inflate(R.layout.item_site_tag, null);
            MaterialRippleLayout rippleLayout = (MaterialRippleLayout) view.findViewById(R.id.ripple_layout);
            LinearLayout child = (LinearLayout) label.getChildAt(0);
            child.setBackgroundResource(android.R.color.transparent);
            label.removeView(child);
            rippleLayout.addView(child);
            label.addView(view);
            rippleLayout.setOnClickListener(v -> {
                if (position >= 0 && mProvider != null) {
                    tag.selected = !tag.selected;
                    notifyItemChanged(position);
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(v, position);
                }
            });
        }
    }

    public void notifyItemChanged(int position) {
        Tag tag = mProvider.getItem(position);
        Label label = labelView.getLabel(position);
        label.setText(tag.title);
        MaterialRippleLayout rippleLayout = (MaterialRippleLayout) label.findViewById(R.id.ripple_layout);
        if (tag.selected) {
            rippleLayout.setRippleBackground(label.getContext().getResources().getColor(R.color.colorPrimary));
            rippleLayout.setRippleColor(label.getContext().getResources().getColor(R.color.dimgray));
        } else {
            rippleLayout.setRippleBackground(label.getContext().getResources().getColor(R.color.dimgray));
            rippleLayout.setRippleColor(label.getContext().getResources().getColor(R.color.colorPrimary));
        }
        rippleLayout.setRadius(0);
    }

    public void notifyDataSetChanged() {
        init();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}