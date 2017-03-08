package ml.puredark.hviewer.helpers;

import android.content.SharedPreferences;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;

import com.google.gson.Gson;

import java.util.List;

import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.configs.Names;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.HViewerApplication.mContext;

/**
 * Created by GKF on 2016/12/1.
 */

public class DataBackup {
    private SiteHolder siteHolder;

    public String DoBackup() {
        try {
            String settingBackup = SettingBackup();
            String siteBackup = SiteBackup();
            String favouriteBackup = FavouriteBackup();
            return settingBackup + " " + siteBackup + " " + favouriteBackup;
        } catch (Exception e) {
            e.printStackTrace();
            return "备份失败，请检查下载目录是否正确设置";
        }
    }

    public String SiteBackup() {
        DocumentFile file = FileHelper.createFileIfNotExist(Names.sitename, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (file == null) {
            return "站点备份失败";
        } else {
            siteHolder = new SiteHolder(mContext);
            final List<Pair<SiteGroup, List<Site>>> siteGroups = siteHolder.getSites();
            String json = new Gson().toJson(siteGroups);
            FileHelper.writeString(json, file);
            siteHolder.onDestroy();
        }
        return "站点备份成功";
    }

    public String FavouriteBackup() {
        DocumentFile file = FileHelper.createFileIfNotExist(Names.favouritesname, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (file == null) {
            return "收藏夹备份失败";
        } else {
            FavouriteHolder holder = new FavouriteHolder(mContext);
            String json = new Gson().toJson(holder.getFavourites());
            FileHelper.writeString(json, file);
            holder.onDestroy();
            return "收藏夹备份成功";
        }
    }

    public String SettingBackup() {
        DocumentFile file = FileHelper.createFileIfNotExist(Names.settingname, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (file == null) {
            return "设置备份失败";
        }

        SharedPreferences pref = mContext.getSharedPreferences(SharedPreferencesUtil.FILE_NAME, mContext.MODE_PRIVATE);
        String json = new Gson().toJson(pref.getAll());
        FileHelper.writeString(json, file);

        return "设置备份成功";
    }

}
