package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import android.util.Xml;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.configs.Names;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.ui.adapters.MarketSiteAdapter;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.app.Activity.RESULT_OK;
import static ml.puredark.hviewer.HViewerApplication.mContext;

/**
 * Created by GKF on 2016/12/1.
 */

public class DataRestore {
    private SiteHolder siteHolder = new SiteHolder(mContext);
    int sid;

    public String DoRestore() {
        String settingRestore = SettingRestore();
        String siteRestore = SiteRestore();
        String favouriteRestore = FavouriteRestore();
        return mContext.getString(R.string.restore_Succes);

    }

    public String SettingRestore() {
        String json = FileHelper.readString(Names.settingname, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (json == null) {
            return "未在下载目录中找到设置备份";
        } else {
            try {
                Map<String, ?> entries = new Gson().fromJson(json, new TypeToken<Map<String, ?>>() {
                }.getType());
                for (Map.Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();
                    if (!key.equals(SettingFragment.KEY_PREF_DOWNLOAD_PATH)) {
                        SharedPreferencesUtil.saveData(mContext, key, v);
                    }
                }
                return "设置还原成功";
            } catch (Exception e) {
                e.printStackTrace();
                return "设置还原失败";
            }
        }

    }

    public String FavouriteRestore() {
        String json = FileHelper.readString(Names.favouritesname, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (json == null) {
            return "未在下载目录中找到收藏夹备份";
        } else {
            try {
                List<LocalCollection> favourites = new Gson().fromJson(json, new TypeToken<ArrayList<LocalCollection>>() {
                }.getType());
                FavouriteHolder holder = new FavouriteHolder(mContext);
                for (LocalCollection collection : favourites) {
                    holder.addFavourite(collection);
                }
                holder.onDestroy();
                return "导入收藏夹成功";
            } catch (Exception e) {
                e.printStackTrace();
                return "导入收藏夹失败";
            }
        }
    }

    public String SiteRestore() {
        String json = FileHelper.readString(Names.sitename, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (json == null) {
            return "未在下载目录中找到站点备份";
        } else {
            List<Pair<SiteGroup, List<Site>>> siteGroups = new Gson().fromJson(json, new TypeToken<List<Pair<SiteGroup, List<Site>>>>() {
            }.getType());
            Pair<SiteGroup, List<Site>> siteGroupListPair;
            SiteGroup siteGroup;
            List<Site> sites;
            Site site;
            for (int i = 0; i < siteGroups.size(); i++) {
                siteGroupListPair = siteGroups.get(i);
                siteGroup = siteGroupListPair.first;
                sites = siteGroupListPair.second;
                for (int j = 0; j < sites.size(); j++) {
                    site = sites.get(j);
                    site.group = siteGroup.title;
                    if (siteHolder.getSiteByTitle(site.title) == null) {
                        sid = siteHolder.addSite(site);
                        if (sid < 0) {
                            return "插入数据库失败";
                        }
                        site.sid = sid;
                        site.index = sid;
                        siteHolder.updateSiteIndex(site);
                    }
                }
            }
            return "站点还愿成功";
        }
    }
}


