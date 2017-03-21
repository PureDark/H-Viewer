package ml.puredark.hviewer.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class CategoryInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int VIEW_TYPE_ADD_CATEGORY = 1;
    private final static int VIEW_TYPE_CATEGORY_INPUT = 2;
    private ListDataProvider<Category> mProvider;

    public CategoryInputAdapter(ListDataProvider<Category> mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD_CATEGORY) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_add_btn, parent, false);
            // 在这里对View的参数进行设置
            BtnAddViewHolder vh = new BtnAddViewHolder(v);
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_input, parent, false);
            // 在这里对View的参数进行设置
            CategoryInputViewHolder vh = new CategoryInputViewHolder(v);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof BtnAddViewHolder) {
            BtnAddViewHolder holder = (BtnAddViewHolder) viewHolder;
            if (position == getItemCount() - 1) {
                holder.ivIcon.setImageResource(R.drawable.ic_add_black);
                holder.tvTitle.setText("添加新分类");
            }
        } else if (viewHolder instanceof CategoryInputViewHolder) {
            CategoryInputViewHolder holder = (CategoryInputViewHolder) viewHolder;
            Category category = mProvider.getItem(position);
            if (category == null) {
                category = new Category(0, "", "");
            }
            holder.inputCategoryTitle.setText(category.title);
            holder.inputCategoryUrl.setText(category.url);
        }
    }

    @Override
    public int getItemCount() {
        return (mProvider == null) ? 1 : mProvider.getCount() + 1;
    }

    @Override
    public long getItemId(int position) {
        if (position == getItemCount() - 1)
            return 0;
        else
            return (mProvider == null) ? 0 : mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1)
            return VIEW_TYPE_ADD_CATEGORY;
        else
            return VIEW_TYPE_CATEGORY_INPUT;
    }

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public class CategoryInputViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.input_category_title)
        MaterialEditText inputCategoryTitle;
        @BindView(R.id.input_category_url)
        MaterialEditText inputCategoryUrl;

        public CategoryInputViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            inputCategoryTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int position = getAdapterPosition();
                    if (position >= 0 && position < mProvider.getCount()) {
                        Category category = mProvider.getItem(position);
                        category.title = editable.toString();
                    }
                }
            });
            inputCategoryUrl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int position = getAdapterPosition();
                    if (position >= 0 && position < mProvider.getCount()) {
                        Category category = mProvider.getItem(position);
                        category.url = editable.toString();
                    }
                }
            });
        }
    }

    public class BtnAddViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        MaterialRippleLayout container;
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_title)
        TextView tvTitle;

        public BtnAddViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container.setOnClickListener(view1 -> {
                mProvider.addItem(new Category(0, "", ""));
                notifyDataSetChanged();
            });
        }
    }

}