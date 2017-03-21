package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Comment;
import ml.puredark.hviewer.core.HtmlContentParser;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.URLImageParser;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private ListDataProvider<Comment> mProvider;
    private OnItemClickListener mItemClickListener;
    private String cookie;

    public CommentAdapter(Context context, ListDataProvider<Comment> mProvider) {
        this.mProvider = mProvider;
        setHasStableIds(false);
        this.context = context;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        try {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            v = new LinearLayout(parent.getContext());
        }
        CommentViewHolder vh = new CommentViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Logger.d("CommentAdapter", "mProvider.getCount():" + mProvider.getCount());
        Comment comment = mProvider.getItem(position);
        Logger.d("CommentAdapter", "comment.avatar:" + comment.avatar);
        if (!TextUtils.isEmpty(comment.avatar))
            ImageLoader.loadImageFromUrl(context, holder.ivAvatar, comment.avatar, cookie, comment.referer);
        else
            ImageLoader.loadImageFromUrl(context, holder.ivAvatar, "res:///" + R.drawable.avatar);
        holder.tvAuthor.setText(comment.author);
        holder.tvDatetime.setText(comment.datetime);
        if (comment.content != null) {
            try {
                Document content = Jsoup.parse(comment.content);
                Elements imgs = content.select("img[data-src]");
                for (Element img : imgs) {
                    String imgUrl = img.attr("data-src");
                    if (!TextUtils.isEmpty(imgUrl)) {
                        img.attr("src", imgUrl);
                    }
                }
                comment.content = content.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.tvContent.setText(HtmlContentParser.getClickableHtml(context, comment.content, comment.referer, new URLImageParser(context, holder.tvContent, cookie, comment.referer), null));
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
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

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_avatar)
        SimpleDraweeView ivAvatar;
        @BindView(R.id.tv_author)
        TextView tvAuthor;
        @BindView(R.id.tv_datetime)
        TextView tvDatetime;
        @BindView(R.id.tv_content)
        TextView tvContent;
        @BindView(R.id.container)
        LinearLayout container;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                if (mItemClickListener != null && getAdapterPosition() >= 0)
                    mItemClickListener.onItemClick(v, getAdapterPosition());
            });
            tvContent.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
            tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}