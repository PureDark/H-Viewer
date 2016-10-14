package ml.puredark.hviewer.ui.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.umeng.analytics.MobclickAgent;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.activities.PictureViewerActivity;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.FileType;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_LEFT_TO_RIGHT;
import static ml.puredark.hviewer.ui.fragments.SettingFragment.DIREACTION_RIGHT_TO_LEFT;

/**
 * Created by PureDark on 2016/10/5.
 */


public class PicturePagerAdapter extends PagerAdapter implements DirectoryChooserFragment.OnFragmentInteractionListener {

    private String viewDirection = DIREACTION_LEFT_TO_RIGHT;

    private BaseActivity activity;

    private Site site;
    private Collection collection;

    public List<Picture> pictures;
    private List<PictureViewHolder> viewHolders = new ArrayList<>();

    private DirectoryChooserFragment mDialog;
    private String lastPath = DownloadManager.getDownloadPath();
    private Picture pictureToBeSaved;
    private boolean firstTime = true;

    public PicturePagerAdapter(BaseActivity activity, Site site, Collection collection, List<Picture> pictures) {
        this.activity = activity;
        this.site = site;
        this.collection = collection;
        this.pictures = pictures;
        for (int i = 0; i < getCount(); i++)
            viewHolders.add(null);
    }

    public class PictureViewHolder {
        View view;
        @BindView(R.id.iv_picture)
        PhotoDraweeView ivPicture;
        @BindView(R.id.progress_bar)
        ProgressBarCircularIndeterminate progressBar;
        @BindView(R.id.btn_refresh)
        ImageView btnRefresh;

        public PictureViewHolder(View view) {
            ButterKnife.bind(this, view);
            this.view = view;
            ivPicture.setOrientation(LinearLayout.HORIZONTAL);
        }
    }

    public void clearItems() {
        pictures = null;
        notifyDataSetChanged();
    }

    public void setViewDirection(String viewDirection) {
        this.viewDirection = viewDirection;
    }

    public String getViewDirection() {
        return viewDirection;
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

        if (pictures!=null && position < pictures.size()) {
            final Picture picture = pictures.get(getPicturePostion(position));
            if (picture.pic != null) {
                loadImage(container.getContext(), picture, viewHolder);
            } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null && site.extraRule.pictureUrl != null) {
                getPictureUrl(container.getContext(), viewHolder, picture, site, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
            } else if (site.picUrlSelector != null) {
                getPictureUrl(container.getContext(), viewHolder, picture, site, site.picUrlSelector, null);
            } else {
                picture.pic = picture.url;
                loadImage(container.getContext(), picture, viewHolder);
            }
            viewHolder.btnRefresh.setOnClickListener(v -> {
                if (picture.pic != null) {
                    loadImage(container.getContext(), picture, viewHolder);
                } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null && site.extraRule.pictureUrl != null) {
                    getPictureUrl(container.getContext(), viewHolder, picture, site, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
                } else if (site.picUrlSelector == null) {
                    picture.pic = picture.url;
                    loadImage(container.getContext(), picture, viewHolder);
                } else {
                    getPictureUrl(container.getContext(), viewHolder, picture, site, site.picUrlSelector, null);
                }
            });
            viewHolder.ivPicture.setOnLongClickListener(v -> {
                if (activity != null) {
                    new AlertDialog.Builder(activity).setTitle("保存图片？")
                            .setMessage("是否保存当前图片")
                            .setPositiveButton("确定", (dialog, which) -> new AlertDialog.Builder(activity).setTitle("是否直接保存到下载目录？")
                                    .setMessage("否则另存到其他目录")
                                    .setPositiveButton("是", (dialog1, which1) -> {
                                        pictureToBeSaved = picture;
                                        onSelectDirectory(Uri.parse(DownloadManager.getDownloadPath()));
                                    })
                                    .setNegativeButton("否", (dialog12, which12) -> {
                                        pictureToBeSaved = picture;
                                        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                                                .initialDirectory(lastPath)
                                                .newDirectoryName("download")
                                                .allowNewDirectoryNameModification(true)
                                                .build();
                                        mDialog = DirectoryChooserFragment.newInstance(config);
                                        mDialog.setDirectoryChooserListener(PicturePagerAdapter.this);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                            try {
                                                activity.startActivityForResult(intent, PictureViewerActivity.RESULT_CHOOSE_DIRECTORY);
                                            } catch (ActivityNotFoundException e) {
                                                e.printStackTrace();
                                                mDialog.show(activity.getFragmentManager(), null);
                                            }
                                        } else {
                                            mDialog.show(activity.getFragmentManager(), null);
                                        }
                                    }).show()).setNegativeButton("取消", null).show();
                }
                return true;
            });
        }
        viewHolders.set(position, viewHolder);
        container.addView(viewHolder.view, 0);
        return viewHolder.view;
    }

    @Override
    public void onSelectDirectory(@NonNull String path) {
        if (pictureToBeSaved == null)
            return;
        lastPath = path;
        loadPicture(pictureToBeSaved, path);
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
    }

    public void onSelectDirectory(Uri rootUri) {
        String path = rootUri.toString();
        if (pictureToBeSaved == null)
            return;
        lastPath = path;
        loadPicture(pictureToBeSaved, path);
    }

    public boolean viewHighRes() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingFragment.KEY_PREF_VIEW_HIGH_RES, false);
    }

    private void loadPicture(final Picture picture, final String path) {
        ImageLoader.loadResourceFromUrl(activity, picture.pic, site.cookie, picture.referer,
                new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {

                    @Override
                    protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        if (!dataSource.isFinished()) {
                            return;
                        }
                        CloseableReference<PooledByteBuffer> ref = dataSource.getResult();
                        if (ref != null) {
                            try {
                                PooledByteBuffer imageBuffer = ref.get();
                                savePicture(path, imageBuffer);
                            } finally {
                                CloseableReference.closeSafely(ref);
                            }
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {

                    }
                });
    }

    private void savePicture(String path, PooledByteBuffer buffer) {
        try {
            byte[] bytes = new byte[buffer.size()];
            buffer.read(0, bytes, 0, buffer.size());
            String postfix = FileType.getFileType(bytes, FileType.TYPE_IMAGE);
            String fileName;
            int i = 1;
            do {
                fileName = Uri.encode(site.title + "_" + FileHelper.filenameFilter(collection.idCode) + "_" + (i++) + "." + postfix);
            } while (FileHelper.isFileExist(fileName, path));
            DocumentFile documentFile = FileHelper.createFileIfNotExist(fileName, path);
            if (FileHelper.writeBytes(bytes, documentFile)) {
                activity.showSnackBar("保存成功");
                // 统计保存单图次数
                MobclickAgent.onEvent(HViewerApplication.mContext, "SaveSinglePicture");
            } else {
                activity.showSnackBar("保存失败，请重新设置下载目录");
            }
        } catch (OutOfMemoryError error) {
            activity.showSnackBar("保存失败，内存不足");
        }
    }


    private void loadImage(Context context, Picture picture, final PictureViewHolder viewHolder) {
        String url = (viewHighRes() && !TextUtils.isEmpty(picture.highRes)) ? picture.highRes : picture.pic;
        Logger.d("PicturePagerAdapter", "url = " + url);
        if (site == null) return;
        ImageLoader.loadImageFromUrl(context, viewHolder.ivPicture, url, site.cookie, picture.referer, new BaseControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
                super.onSubmit(id, callerContext);
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.btnRefresh.setVisibility(View.GONE);
            }

            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                super.onFinalImageSet(id, imageInfo, anim);
                if (imageInfo == null) {
                    return;
                }
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.btnRefresh.setVisibility(View.GONE);
                viewHolder.ivPicture.update(imageInfo.getWidth(), imageInfo.getHeight());
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                FLog.e(getClass(), throwable, "Error loading %s", id);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.btnRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getPictureUrl(final Context context, final PictureViewHolder viewHolder, final Picture picture, final Site site, final Selector selector, final Selector highResSelector) {
        Logger.d("PicturePagerAdapter", "picture.url = " + picture.url);
        if (Picture.hasPicPosfix(picture.url)) {
            picture.pic = picture.url;
            loadImage(context, picture, viewHolder);
        } else
            HViewerHttpClient.get(picture.url, site.getCookies(), new HViewerHttpClient.OnResponseListener() {

                @Override
                public void onSuccess(String contentType, Object result) {
                    if (result == null || result.equals(""))
                        return;
                    if (contentType.contains("image")) {
                        picture.pic = picture.url;
                        if (result instanceof Bitmap) {
                            viewHolder.ivPicture.setImageBitmap((Bitmap) result);
                            viewHolder.progressBar.setVisibility(View.GONE);
                        } else {
                            loadImage(context, picture, viewHolder);
                        }
                    } else {
                        picture.pic = RuleParser.getPictureUrl((String) result, selector, picture.url);
                        picture.highRes = RuleParser.getPictureUrl((String) result, highResSelector, picture.url);
                        Logger.d("PicturePagerAdapter", "getPictureUrl: picture.pic: " + picture.pic);
                        Logger.d("PicturePagerAdapter", "getPictureUrl: picture.highRes: " + picture.highRes);
                        if (picture.pic != null) {
                            picture.retries = 0;
                            picture.referer = picture.url;
                            loadImage(context, picture, viewHolder);
                        } else {
                            onFailure(null);
                        }
                    }
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    if (picture.retries < 15) {
                        picture.retries++;
                        getPictureUrl(context, viewHolder, picture, site, selector, highResSelector);
                    } else {
                        picture.retries = 0;
                        viewHolder.progressBar.setVisibility(View.GONE);
                        viewHolder.btnRefresh.setVisibility(View.VISIBLE);
                    }
                }
            });
    }
}