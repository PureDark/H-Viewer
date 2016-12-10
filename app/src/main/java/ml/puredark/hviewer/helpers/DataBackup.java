package ml.puredark.hviewer.helpers;

import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Xml;


import com.google.gson.Gson;

import org.xmlpull.v1.XmlSerializer;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.DocumentUtil;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.HViewerApplication.mContext;

/**
 * Created by GKF on 2016/12/1.
 */

public class DataBackup {
    private SiteHolder siteHolder;
    private Pair<SiteGroup, List<Site>> siteGroupListPair;
    private SiteGroup siteGroup;
    private List<Site> sites;
    private Site site;
    private String settingBackup = "设置备份失败";
    private String siteBackup = "站点备份失败";
    private String favouriteBackup = "收藏夹备份失败";

    public String DoBackup() {
        settingBackup = SettingBackup();
        siteBackup = SiteBackup();
        favouriteBackup = FavouriteBackup();
        return settingBackup + " " + siteBackup + " " + favouriteBackup;
    }

    public String SiteBackup() {
        DocumentFile file = FileHelper.createFileIfNotExist(FileHelper.sitename, DownloadManager.getDownloadPath(), FileHelper.appdirname);
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
        DocumentFile file = FileHelper.createFileIfNotExist(FileHelper.favouritesname, DownloadManager.getDownloadPath(), FileHelper.appdirname);
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

    public String SettingBackup(){
        DocumentFile file = FileHelper.createFileIfNotExist(FileHelper.settingname, DownloadManager.getDownloadPath(), FileHelper.appdirname);
        if( file == null ){
                return "设置备份失败";
        }

        SharedPreferences pref = mContext.getSharedPreferences(SharedPreferencesUtil.FILE_NAME, mContext.MODE_PRIVATE);
        String json = new Gson().toJson(pref.getAll());
        FileHelper.writeString(json, file);

        return "设置备份成功";
    }

}
