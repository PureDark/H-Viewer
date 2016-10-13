package ml.puredark.hviewer.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dpizarro.autolabel.library.AutoLabelUI;
import com.dpizarro.autolabel.library.Label;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

import static android.R.attr.tag;

public class SiteTagAdapter {
    private AutoLabelUI labelView;
    private ListDataProvider mProvider;
    private OnItemClickListener mItemClickListener;

    public SiteTagAdapter(ListDataProvider provider) {
        this.mProvider = provider;
    }

    public void setLabelView(AutoLabelUI labelView){
        this.labelView = labelView;
        init();
    }

    private void init(){
        labelView.clear();
        List<Tag> tags = mProvider.getItems();
        for(Tag tag : tags) {
            labelView.addLabel(tag.title);
            labelView.setLayoutTransition(null);
            int position = labelView.getLabelsCounter()-1;
            Label label = labelView.getLabel(position);
            label.setOnLabelClickListener(v -> {
                if (position >= 0 && mProvider != null) {
                    tag.selected = !tag.selected;
                    notifyItemChanged(position);
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(v, position);
                }
            });
            if (tag.selected)
                label.getChildAt(0).setBackgroundResource(R.color.colorPrimary);
            else
                label.getChildAt(0).setBackgroundResource(R.color.dimgray);
        }
    }

    public void notifyItemChanged(int position){
        Tag tag = (Tag) mProvider.getItem(position);
        Label label = labelView.getLabel(position);
        label.setText(tag.title);
        if (tag.selected)
            label.getChildAt(0).setBackgroundResource(R.color.colorPrimary);
        else
            label.getChildAt(0).setBackgroundResource(R.color.dimgray);
    }

    public void notifyDataSetChanged(){
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