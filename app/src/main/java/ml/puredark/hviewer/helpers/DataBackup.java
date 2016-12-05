package ml.puredark.hviewer.helpers;

import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import android.util.Xml;


import com.google.gson.Gson;

import org.xmlpull.v1.XmlSerializer;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
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
    //文件路径
    private String SDPATH = Environment.getExternalStorageDirectory()  + "/H-ViewerSites.xml/" ;


    public String DoBackup() {
        settingBackup = SettingBackup();
        siteBackup = SiteBackup();
        favouriteBackup = FavouriteBackup();
        return settingBackup + " " + siteBackup + " " + favouriteBackup;
    }

    public String SiteBackup() {
        File file = new File( SDPATH ) ;
        XmlSerializer serializer;
        if( !file.exists() ){
            try {
                file.createNewFile() ;
            } catch (IOException e) {
                e.printStackTrace();
                return "站点备份失败";
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            serializer = Xml.newSerializer();
            serializer.setOutput(fos, "utf-8");
            serializer.startDocument("utf-8", true);
            serializer.startTag(null, "sites");
            siteHolder = new SiteHolder(mContext);
            final List<Pair<SiteGroup, List<Site>>> siteGroups = siteHolder.getSites();
            for (int i = 0; i < siteGroups.size(); i++) {
                siteGroupListPair = siteGroups.get(i);
                siteGroup = siteGroupListPair.first;
                sites = siteGroupListPair.second;
                serializer.startTag(siteGroup.title, "group");
                for (int j = 0; j < sites.size(); j++) {
                    site = sites.get(j);
                    site.group = siteGroup.title;
                    final String jsonStr = new Gson().toJson(site);
                    serializer.startTag(site.title, "site");
                    serializer.text(jsonStr);
                    serializer.endTag(site.title, "site");
                }
                serializer.endTag(siteGroup.title, "group");
            }
            serializer.endTag(null, "sites");
            serializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
            return "站点备份失败";
        }
        return "站点备份成功";
    }

    public String FavouriteBackup() {
        DocumentFile file = FileHelper.createFileIfNotExist("favourites.json", DownloadManager.getDownloadPath());
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


        return "设置备份成功";
    }


}
