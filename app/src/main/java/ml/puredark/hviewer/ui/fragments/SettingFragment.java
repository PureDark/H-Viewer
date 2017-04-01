package ml.puredark.hviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.CollectionGroup;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.configs.Names;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.helpers.DataBackup;
import ml.puredark.hviewer.helpers.DataRestore;
import ml.puredark.hviewer.helpers.DynamicIjkLibLoader;
import ml.puredark.hviewer.helpers.DynamicLibDownloader;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.UpdateManager;
import ml.puredark.hviewer.http.DownloadUtil;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.activities.LicenseActivity;
import ml.puredark.hviewer.ui.activities.PrivacyActivity;
import ml.puredark.hviewer.ui.preferences.LongClickPreference;
import ml.puredark.hviewer.utils.DensityUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.app.Activity.RESULT_OK;
import static ml.puredark.hviewer.HViewerApplication.mContext;

/**
 * Created by PureDark on 2016/9/25.
 */
public class SettingFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, DirectoryChooserFragment.OnFragmentInteractionListener {
    public static final String KEY_PREF_PROXY_DETAIL = "pref_proxy_detail";
    public static final String KEY_PREF_PROXY_ENABLED = "pref_proxy_enabled";
    public static final String KEY_PREF_PROXY_REQUEST = "pref_proxy_request";
    public static final String KEY_PREF_PROXY_PICTURE = "pref_proxy_picture";
    public static final String KEY_PREF_PROXY_SERVER = "pref_proxy_server";

    public static final String KEY_PRER_VIEW_REMLASTSITE = "pref_view_rememberLastSite";
    public static final String KEY_PREF_VIEW_HIGH_RES = "pref_view_high_res";
    public static final String KEY_PREF_VIEW_PRELOAD_PAGES = "pref_view_preload_pages";
    public static final String KEY_PREF_VIEW_DIRECTION = "pref_view_direction";
    public static final String KEY_PREF_VIEW_VOLUME_FLICK = "pref_view_volume_flick";
    public static final String KEY_PREF_VIEW_ONE_PIC_GALLERY = "pref_view_one_pic_gallery";
    public static final String KEY_PREF_VIEW_ONE_HAND = "pref_view_one_hand";
    public static final String KEY_PREF_VIEW_VIDEO_PLAYER = "pref_view_video_player";

    public static final String DIREACTION_LEFT_TO_RIGHT = mContext.getResources().getStringArray(R.array.settings_view_direction_values)[0];
    public static final String DIREACTION_RIGHT_TO_LEFT = mContext.getResources().getStringArray(R.array.settings_view_direction_values)[1];
    public static final String DIREACTION_TOP_TO_BOTTOM = mContext.getResources().getStringArray(R.array.settings_view_direction_values)[2];

    public static final String VIDEO_IJKPLAYER = mContext.getResources().getStringArray(R.array.settings_view_video_player_values)[0];
    public static final String VIDEO_H5PLAYER = mContext.getResources().getStringArray(R.array.settings_view_video_player_values)[1];
    public static final String VIDEO_OTHERPLAYER = mContext.getResources().getStringArray(R.array.settings_view_video_player_values)[2];

    public static final String KEY_PREF_DOWNLOAD_HIGH_RES = "pref_download_high_res";
    public static final String KEY_PREF_DOWNLOAD_NOMEDIA = "pref_download_nomedia";
    public static final String KEY_PREF_DOWNLOAD_PATH = "pref_download_path";
    public static final String KEY_PREF_DOWNLOAD_IMPORT = "pref_download_import";

    public static final String KEY_PREF_FAVOURITE_EXPORT = "pref_favourite_export";
    public static final String KEY_PREF_FAVOURITE_IMPORT = "pref_favourite_import";

    public static final String KEY_PREF_CACHE_SIZE = "pref_cache_size";
    public static final String KEY_PREF_CACHE_CLEAN = "pref_cache_clean";

    public static final String KEY_PREF_BKRS_BACKUP = "pref_backupandrestore_backup";
    public static final String KEY_PREF_BKRS_RESTORE = "pref_backupandrestore_restore";

    public static final String KEY_PREF_LOCK_METHODS_DETAIL = "pref_lock_methods_detail";

    public static final String KEY_PREF_ABOUT_UPGRADE = "pref_about_upgrade";
    public static final String KEY_PREF_ABOUT_LICENSE = "pref_about_license";
    public static final String KEY_PREF_ABOUT_PRIVACY = "pref_about_privacy";
    public static final String KEY_PREF_ABOUT_H_VIEWER = "pref_about_h_viewer";

    public static final String KEY_PREF_MODE_R18_ENABLED = "pref_mode_r18_enabled";

    public static final String KEY_LAST_SITE_ID = "last_site_id";

    public static final String KEY_FIRST_TIME = "key_first_time";

    public static final String KEY_CUSTOM_HEADER_IMAGE = "key_custom_header_image";

    private static final int RESULT_CHOOSE_DIRECTORY = 1;

    private BaseActivity activity;
    private DirectoryChooserFragment mDialog;

    private boolean checking = false;

    //是否已打开路径选择器
    private boolean opened = false;

    public SettingFragment() {
    }

    @SuppressLint("ValidFragment")
    public SettingFragment(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtil.FILE_NAME);
        addPreferencesFromResource(R.xml.preferences);

        String downloadPath = DownloadManager.getDownloadPath();
        if (downloadPath != null) {
            String displayPath = Uri.decode(downloadPath);
            getPreferenceManager().findPreference(KEY_PREF_DOWNLOAD_PATH).setSummary(displayPath);
        }
        ListPreference listPreference = (ListPreference) getPreferenceManager().findPreference(KEY_PREF_VIEW_DIRECTION);
        CharSequence[] entries = listPreference.getEntries();
        int i = listPreference.findIndexOfValue(listPreference.getValue());
        i = (i <= 0) ? 0 : i;
        listPreference.setSummary(entries[i]);
        listPreference.setOnPreferenceChangeListener(this);
        listPreference = (ListPreference) getPreferenceManager().findPreference(KEY_PREF_VIEW_VIDEO_PLAYER);
        entries = listPreference.getEntries();
        i = listPreference.findIndexOfValue(listPreference.getValue());
        i = (i <= 0) ? 0 : i;
        listPreference.setSummary(entries[i]);
        listPreference.setOnPreferenceChangeListener(this);

        getPreferenceScreen().setOnPreferenceChangeListener(this);
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .initialDirectory((downloadPath.startsWith("/")) ? downloadPath : DownloadManager.DEFAULT_PATH)
                .newDirectoryName("download")
                .allowNewDirectoryNameModification(true)
                .build();
        mDialog = DirectoryChooserFragment.newInstance(config);
        mDialog.setTargetFragment(this, 0);

        float size = (float) Fresco.getImagePipelineFactory().getMainFileCache().getSize() / ByteConstants.MB;
        Preference cacheCleanPreference = getPreferenceManager().findPreference(KEY_PREF_CACHE_CLEAN);
        cacheCleanPreference.setSummary(String.format("已使用 %.2f MB", size));

        LongClickPreference prefDownloadPath = (LongClickPreference) getPreferenceManager().findPreference(KEY_PREF_DOWNLOAD_PATH);
        prefDownloadPath.setOnLongClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle("选择路径方式")
                    .setItems(new String[]{"系统文档（新）", "路径选择框（旧）"}, (dialogInterface, pos) -> {
                        if (pos == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            try {
                                startActivityForResult(intent, RESULT_CHOOSE_DIRECTORY);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                mDialog.show(getFragmentManager(), null);
                            }
                            new Handler().postDelayed(() -> {
                                if (!opened)
                                    activity.showSnackBar("如无法开启系统文档，长按使用旧工具");
                            }, 1000);
                        } else if (pos == 1) {
                            mDialog.show(getFragmentManager(), null);
                        } else
                            activity.showSnackBar("当前系统版本不支持");
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        opened = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        opened = false;
    }

    @Override
    public void onSelectDirectory(@NonNull String path) {
        SharedPreferencesUtil.saveData(getActivity(), KEY_PREF_DOWNLOAD_PATH, Uri.encode(path));
        getPreferenceManager().findPreference(KEY_PREF_DOWNLOAD_PATH).setSummary(Uri.decode(path));
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_PREF_PROXY_SERVER)) {
            preference.setSummary((String) newValue);
        } else if (preference.getKey().equals(KEY_PREF_VIEW_DIRECTION)) {
            ListPreference directionPreference = (ListPreference) preference;
            CharSequence[] entries = directionPreference.getEntries();
            int i = directionPreference.findIndexOfValue((String) newValue);
            i = (i <= 0) ? 0 : i;
            directionPreference.setSummary(entries[i]);
        } else if (preference.getKey().equals(KEY_PREF_VIEW_VIDEO_PLAYER)) {
            ListPreference videoPlayerPreference = (ListPreference) preference;
            CharSequence[] entries = videoPlayerPreference.getEntries();
            int i = videoPlayerPreference.findIndexOfValue((String) newValue);
            i = (i <= 0) ? 0 : i;
            videoPlayerPreference.setSummary(entries[i]);
            if (VIDEO_IJKPLAYER.equals(newValue) && !DynamicIjkLibLoader.isLibrariesDownloaded()) {
                // 未下载播放器解码so包
                new DynamicLibDownloader(activity).checkDownloadLib();
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_PREF_ABOUT_UPGRADE)) {
            //检查新版本
            if (!checking)
                UpdateManager.checkUpdate(activity);
        } else if (preference.getKey().equals(KEY_PREF_BKRS_BACKUP)) {
            //备份
            new AlertDialog.Builder(activity).setTitle("确认备份?")
                    .setMessage("将会覆盖之前的备份")
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        String backup = new DataBackup().DoBackup();
                        activity.showSnackBar(backup);
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();

        } else if (preference.getKey().equals(KEY_PREF_BKRS_RESTORE)) {
            //还原
            new AlertDialog.Builder(activity).setTitle("确认恢复?")
                    .setMessage("如已存在同名站点，不会覆盖")
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        String restore = new DataRestore().DoRestore();
                        Intent intent = new Intent();
                        activity.setResult(RESULT_OK, intent);
                        Toast.makeText(activity, restore, Toast.LENGTH_LONG).show();
                        activity.finish();
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();

        } else if (preference.getKey().equals(KEY_PREF_ABOUT_LICENSE)) {
            //开源协议
            Intent intent = new Intent(activity, LicenseActivity.class);
            startActivity(intent);
        } else if (preference.getKey().equals(KEY_PREF_ABOUT_PRIVACY)) {
            //隐私权政策
            Intent intent = new Intent(activity, PrivacyActivity.class);
            startActivity(intent);
        } else if (preference.getKey().equals(KEY_PREF_ABOUT_H_VIEWER)) {
            //关于
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.setting_content, new AboutFragment(activity));
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (preference.getKey().equals(KEY_PREF_DOWNLOAD_PATH)) {
            //下载路径
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                try {
                    startActivityForResult(intent, RESULT_CHOOSE_DIRECTORY);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    mDialog.show(getFragmentManager(), null);
                }
                new Handler().postDelayed(() -> {
                    if (!opened)
                        activity.showSnackBar("如无法开启系统文档，长按使用旧工具");
                }, 1000);
            } else {
                mDialog.show(getFragmentManager(), null);
            }
        } else if (preference.getKey().equals(KEY_PREF_DOWNLOAD_IMPORT)) {
            //导入已下载
            new AlertDialog.Builder(activity).setTitle("确定要导入已下载图册？")
                    .setMessage("将从当前指定的下载目录进行搜索")
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        DownloadedImport();
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        } else if (preference.getKey().equals(KEY_PREF_FAVOURITE_EXPORT)) {
            //导出收藏夹
            new AlertDialog.Builder(activity).setTitle("确定要导出收藏夹？")
                    .setMessage("将导出至当前指定的下载目录")
                    .setPositiveButton("确定", (dialog, which) -> {
                        DocumentFile file = FileHelper.createFileIfNotExist(Names.favouritesname, DownloadManager.getDownloadPath(), Names.backupdirname);
                        if (file != null) {
                            FavouriteHolder holder = new FavouriteHolder(activity);
                            String json = new Gson().toJson(holder.getFavourites());
                            FileHelper.writeString(json, file);
                            holder.onDestroy();
                            activity.showSnackBar("导出收藏夹成功");
                        } else
                            activity.showSnackBar("创建文件失败，请检查下载目录");
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        } else if (preference.getKey().equals(KEY_PREF_FAVOURITE_IMPORT)) {
            //导入收藏夹
            new AlertDialog.Builder(activity).setTitle("确定要导入收藏夹？")
                    .setMessage("将从当前指定的下载目录搜索收藏夹备份")
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        String json = FileHelper.readString(Names.favouritesname, DownloadManager.getDownloadPath(), Names.backupdirname);
                        if (json == null) {
                            activity.showSnackBar("未在下载目录中找到收藏夹备份");
                        } else {
                            try {
                                List<Pair<CollectionGroup, List<LocalCollection>>> favGroups =
                                        new Gson().fromJson(json, new TypeToken<ArrayList<Pair<CollectionGroup, ArrayList<LocalCollection>>>>() {
                                        }.getType());
                                FavouriteHolder holder = new FavouriteHolder(activity);
                                for (Pair<CollectionGroup, List<LocalCollection>> pair : favGroups) {
                                    Logger.d("DataStore", "" + pair.first);
                                    CollectionGroup group = holder.getGroupByTitle(pair.first.title);
                                    if (group == null) {
                                        group = pair.first;
                                        group.gid = holder.addFavGroup(group);
                                    }
                                    for (LocalCollection collection : pair.second) {
                                        collection.gid = group.gid;
                                        holder.addFavourite(collection);
                                    }
                                }
                                holder.onDestroy();
                                activity.showSnackBar("导入收藏夹成功");
                            } catch (Exception e) {
                                e.printStackTrace();
                                activity.showSnackBar("导入收藏夹失败");
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        } else if (preference.getKey().equals(KEY_PREF_CACHE_CLEAN)) {
            //清空图片缓存
            new AlertDialog.Builder(activity).setTitle("确定要清空图片缓存？")
                    .setMessage("近期加载过的图片将会需要重新下载")
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        ImagePipeline imagePipeline = Fresco.getImagePipeline();
                        imagePipeline.clearDiskCaches();
                        activity.showSnackBar("缓存清理成功");
                        preference.setSummary("已使用 0.00 MB");
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        } else if (preference.getKey().equals(KEY_PREF_PROXY_DETAIL)) {
            //PROXY代理
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.setting_content, new ProxyFragment(activity));
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (preference.getKey().equals(KEY_PREF_LOCK_METHODS_DETAIL)) {
            //应用解锁方式
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.setting_content, new LockMethodFragment(activity));
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_CHOOSE_DIRECTORY) {
                Uri uriTree = data.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        getActivity().getContentResolver().takePersistableUriPermission(
                                uriTree, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                String path = uriTree.toString();
                String displayPath = Uri.decode(path);
                SharedPreferencesUtil.saveData(getActivity(), KEY_PREF_DOWNLOAD_PATH, path);
                getPreferenceManager().findPreference(KEY_PREF_DOWNLOAD_PATH).setSummary(displayPath);
            }
        }
    }

    public void DownloadedImport() {
        // 关闭边缘滑动返回
        activity.setSwipeBackEnable(false);
        // 阻止退出
        activity.setAllowExit(false);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null);
        TextView tvLoadingText = (TextView) view.findViewById(R.id.tv_loading_text);
        tvLoadingText.setText("正在导入已下载图册");
        final Dialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        //设置对话框位置
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = DensityUtil.getScreenWidth(activity) - DensityUtil.dp2px(activity, 64);
        dialog.getWindow().setAttributes(lp);
        dialog.show();
        new Thread(() -> {
            DownloadTaskHolder holder = new DownloadTaskHolder(activity);
            final int count = holder.scanPathForDownloadTask(DownloadManager.getDownloadPath());
            holder.onDestroy();
            activity.runOnUiThread(() -> {
                if (count > 0)
                    Toast.makeText(mContext, "成功导入" + count + "个已下载图册", Toast.LENGTH_SHORT).show();
                else if (count == 0)
                    Toast.makeText(mContext, "未发现不在下载管理中的已下载图册", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "导入失败", Toast.LENGTH_SHORT).show();
            });
            activity.setSwipeBackEnable(true);
            activity.setAllowExit(true);
            dialog.dismiss();
        }).start();
    }

}
