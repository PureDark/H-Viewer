package ml.puredark.hviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.configs.Names;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.download.DownloadService;
import ml.puredark.hviewer.helpers.DataRestore;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.UpdateManager;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.activities.BaseActivity;
import ml.puredark.hviewer.ui.activities.LicenseActivity;
import ml.puredark.hviewer.ui.activities.LoginActivity;
import ml.puredark.hviewer.ui.activities.MainActivity;
import ml.puredark.hviewer.ui.activities.ModifySiteActivity;
import ml.puredark.hviewer.ui.customs.LongClickPreference;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.helpers.DataBackup;

import static android.R.attr.path;
import static android.app.Activity.RESULT_OK;
import static ml.puredark.hviewer.HViewerApplication.mContext;
import static ml.puredark.hviewer.HViewerApplication.temp;

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

    public static final String DIREACTION_LEFT_TO_RIGHT = mContext.getResources().getStringArray(R.array.settings_view_direction_values)[0];
    public static final String DIREACTION_RIGHT_TO_LEFT = mContext.getResources().getStringArray(R.array.settings_view_direction_values)[1];
    public static final String DIREACTION_TOP_TO_BOTTOM = mContext.getResources().getStringArray(R.array.settings_view_direction_values)[2];

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

    public static final String KEY_PREF_ABOUT_UPGRADE = "pref_about_upgrade";
    public static final String KEY_PREF_ABOUT_LICENSE = "pref_about_license";
    public static final String KEY_PREF_ABOUT_H_VIEWER = "pref_about_h_viewer";

    public static final String KEY_PREF_MODE_R18_ENABLED = "pref_mode_r18_enabled";

    public static final String KEY_LAST_SITE_ID = "last_site_id";

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
        ListPreference directionPreference = (ListPreference) getPreferenceManager().findPreference(KEY_PREF_VIEW_DIRECTION);
        CharSequence[] entries = directionPreference.getEntries();
        int i = directionPreference.findIndexOfValue(directionPreference.getValue());
        i = (i <= 0) ? 0 : i;
        directionPreference.setSummary(entries[i]);
        directionPreference.setOnPreferenceChangeListener(this);

        getPreferenceScreen().setOnPreferenceChangeListener(this);
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .initialDirectory((downloadPath.startsWith("/")) ? downloadPath : DownloadManager.DEFAULT_PATH)
                .newDirectoryName("download")
                .allowNewDirectoryNameModification(true)
                .build();
        mDialog = DirectoryChooserFragment.newInstance(config);
        mDialog.setTargetFragment(this, 0);
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
                                if(!opened)
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
    public void onPause(){
        super.onPause();
        opened = true;
    }

    @Override
    public void onResume(){
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
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_PREF_ABOUT_UPGRADE)) {
            //检查新版本
            if (!checking)
                checkUpdate();
        } else if (preference.getKey().equals(KEY_PREF_BKRS_BACKUP)) {
            //备份
            new AlertDialog.Builder(activity).setTitle("确认备份?")
                    .setMessage("将会覆盖之前的备份")
                    .setPositiveButton(getString(R.string.ok),((dialog, which) -> {
                        String backup = new DataBackup().DoBackup();
                        activity.showSnackBar(backup);
                    }))
                    .setNegativeButton(getString(R.string.cancel), null).show();

        } else if (preference.getKey().equals(KEY_PREF_BKRS_RESTORE)) {
            //还原
            new AlertDialog.Builder(activity).setTitle("确认恢复?")
                    .setMessage("将会新增站点,不会删除原有站点")
                    .setPositiveButton(getString(R.string.ok),((dialog, which) -> {
                        String restore = new DataRestore().DoRestore();
                        Intent intent = new Intent();
                        activity.setResult(RESULT_OK, intent);
                        Toast.makeText(activity, restore, Toast.LENGTH_LONG).show();
                        activity.finish();
                    }))
                    .setNegativeButton(getString(R.string.cancel), null).show();

        } else if (preference.getKey().equals(KEY_PREF_ABOUT_LICENSE)) {
            //开源协议
            Intent intent = new Intent(activity, LicenseActivity.class);
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
                    if(!opened)
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
                        new DownloadService().DownloadedImport();
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        } else if (preference.getKey().equals(KEY_PREF_FAVOURITE_EXPORT)) {
            //导出收藏夹
            new AlertDialog.Builder(activity).setTitle("确定要导出收藏夹？")
                    .setMessage("将导出至当前指定的下载目录")
                    .setPositiveButton("确定", (dialog, which) -> {
                        DocumentFile file = FileHelper.createFileIfNotExist("favourites.json", DownloadManager.getDownloadPath(), Names.backupdirname);
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
                                List<LocalCollection> favourites = new Gson().fromJson(json, new TypeToken<ArrayList<LocalCollection>>() {
                                }.getType());
                                FavouriteHolder holder = new FavouriteHolder(activity);
                                for (LocalCollection collection : favourites) {
                                    holder.addFavourite(collection);
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
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        } else if (preference.getKey().equals(KEY_PREF_PROXY_DETAIL)) {
            //PROXY代理
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.setting_content, new ProxyFragment(activity));
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

    public void checkUpdate() {
        checking = true;
        String url = UrlConfig.updateUrl;
        HViewerHttpClient.get(url, null, new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                try {
                    JsonObject version = new JsonParser().parse((String) result).getAsJsonObject();
                    boolean prerelease = version.get("prerelease").getAsBoolean();
                    if (prerelease) {
                        onFailure(null);
                        return;
                    }
                    JsonArray assets = version.get("assets").getAsJsonArray();
                    if (assets.size() > 0) {
                        checking = false;
                        String oldVersion = HViewerApplication.getVersionName();
                        String newVersion = version.get("tag_name").getAsString().substring(1);
                        String url = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                        String detail = version.get("body").getAsString();
                        new UpdateManager(activity, url, newVersion + "版本更新", detail)
                                .checkUpdateInfo(oldVersion, newVersion);
                    } else {
                        onFailure(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(null);
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                activity.showSnackBar("当前已是最新版本！");
                checking = false;
            }
        });
    }
}
