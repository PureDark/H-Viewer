package ml.puredark.hviewer.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.ui.customs.MultiTouchViewPager;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.utils.FileType;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

import static ml.puredark.hviewer.ui.activities.SettingActivity.SettingFragment.KEY_PREF_DOWNLOAD_PATH;


public class PictureViewerActivity extends AnimationActivity {

    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.view_pager)
    MultiTouchViewPager viewPager;

    private static final int RESULT_CHOOSE_DIRECTORY = 1;

    private PicturePagerAdapter picturePagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);
        ButterKnife.bind(this);
        MDStatusBarCompat.setImageTransparent(this);
        setContainer(container);

        if (HViewerApplication.temp instanceof PicturePagerAdapter)
            picturePagerAdapter = (PicturePagerAdapter) HViewerApplication.temp;

        if (picturePagerAdapter == null || picturePagerAdapter.getCount() == 0) {
            Toast.makeText(this, "数据错误，请刷新后重试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        HViewerApplication.temp = null;

        picturePagerAdapter.setActivity(this);

        int position = getIntent().getIntExtra("position", 0);

        tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());

        viewPager.setAdapter(picturePagerAdapter);

        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(listener);

        int limit = (int) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingActivity.SettingFragment.KEY_PREF_VIEW_PRELOAD_PAGES, 2);

        viewPager.setOffscreenPageLimit(limit);

        viewPager.setCurrentItem(position);
    }

    @Override
    public void onDestroy() {
        if (picturePagerAdapter != null)
            picturePagerAdapter.clearItems();
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_CHOOSE_DIRECTORY) {
                Uri uriTree = data.getData();
                if (picturePagerAdapter != null)
                    picturePagerAdapter.onSelectDirectory(uriTree);
            }
        }
    }

    public static class PicturePagerAdapter extends PagerAdapter implements DirectoryChooserFragment.OnFragmentInteractionListener{

        private AnimationActivity activity;

        private Site site;

        public List<Picture> pictures;
        private List<PictureViewHolder> viewHolders = new ArrayList<>();

        private DirectoryChooserFragment mDialog;
        private String lastPath = DownloadManager.getDownloadPath();
        private Picture pictureToBeSaved;

        public PicturePagerAdapter(Site site, List<Picture> pictures) {
            this.site = site;
            this.pictures = pictures;
            for (int i = 0; i < pictures.size(); i++)
                viewHolders.add(null);
        }

        public static class PictureViewHolder {
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
            }
        }

        public void clearItems() {
            pictures = null;
            notifyDataSetChanged();
        }

        public void setActivity(AnimationActivity activity) {
            this.activity = activity;
        }

        @Override
        public void notifyDataSetChanged() {
            if (pictures != null && pictures.size() > viewHolders.size()) {
                int size = pictures.size() - viewHolders.size();
                for (int i = 0; i < size; i++)
                    viewHolders.add(null);
            }
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (pictures == null) ? 0 : pictures.size();
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
            final Picture picture = pictures.get(position);
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
            viewHolder.btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });
            viewHolder.ivPicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (activity != null) {
                        new AlertDialog.Builder(activity).setTitle("保存图片？")
                                .setMessage("是否保存当前图片")
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pictureToBeSaved = picture;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                            activity.startActivityForResult(intent, RESULT_CHOOSE_DIRECTORY);
                                        } else {
                                            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                                                    .initialDirectory(lastPath)
                                                    .newDirectoryName("download")
                                                    .allowNewDirectoryNameModification(true)
                                                    .build();
                                            mDialog = DirectoryChooserFragment.newInstance(config);
                                            mDialog.setDirectoryChooserListener(PicturePagerAdapter.this);
                                            mDialog.show(activity.getFragmentManager(), null);
                                        }
                                    }
                                }).setNegativeButton("否", null).show();
                    }
                    return true;
                }
            });
            viewHolders.set(position, viewHolder);
            container.addView(viewHolder.view, 0);
            return viewHolder.view;
        }

        @Override
        public void onSelectDirectory(@NonNull String path) {
            if(pictureToBeSaved==null)
                return;
            lastPath = path;
            loadPicture(pictureToBeSaved, path);
            mDialog.dismiss();
        }

        @Override
        public void onCancelChooser() {
            mDialog.dismiss();
        }

        public void onSelectDirectory(Uri rootUri){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity.getContentResolver().takePersistableUriPermission(
                        rootUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            String path = rootUri.toString();
            if(pictureToBeSaved==null)
                return;
            lastPath = path;
            loadPicture(pictureToBeSaved, path);
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
                    fileName = (i++)+ "." + postfix;
                }while (FileHelper.isFileExist(fileName, path));
                DocumentFile documentFile = FileHelper.createFileIfNotExist(fileName, path);
                if (FileHelper.writeBytes(documentFile, bytes)) {
                    activity.showSnackBar("保存成功");
                } else {
                    activity.showSnackBar("保存失败，请检查剩余空间");
                }
            } catch (OutOfMemoryError error) {
                activity.showSnackBar("保存失败，内存不足");
            }
        }


        private void loadImage(Context context, Picture picture, final PictureViewHolder viewHolder) {
            Logger.d("PicturePagerAdapter", "picture.pic = " + picture.pic);
            if (site == null) return;
            ImageLoader.loadImageFromUrl(context, viewHolder.ivPicture, picture.pic, site.cookie, picture.referer, new BaseControllerListener<ImageInfo>() {
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
            Log.d("PicturePagerAdapter", "picture.url = " + picture.url);
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

}
