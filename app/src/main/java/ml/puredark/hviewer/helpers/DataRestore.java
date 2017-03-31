package ml.puredark.hviewer.helpers;

import android.support.v4.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ml.puredark.hviewer.beans.CollectionGroup;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.configs.Names;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.HViewerApplication.mContext;

/**
 * Created by GKF on 2016/12/1.
 */

public class DataRestore {
    int sid;
    private SiteHolder siteHolder = new SiteHolder(mContext);

    public String DoRestore() {
        String settingRestore = SettingRestore();
        String siteRestore = SiteRestore();
        String favouriteRestore = FavouriteRestore();
        return settingRestore + "\n" + siteRestore + "\n" + favouriteRestore;
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


    public String SiteRestore() {
        String json = FileHelper.readString(Names.sitename, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (json == null) {
            return "未在下载目录中找到站点备份";
        } else {
            try {
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
                    SiteGroup existSiteGroup = siteHolder.getGroupByTitle(siteGroup.title);
                    siteGroup.gid = (existSiteGroup == null)
                            ? siteHolder.addSiteGroup(siteGroup)
                            : existSiteGroup.gid;
                    for (int j = 0; j < sites.size(); j++) {
                        site = sites.get(j);
                        site.gid = siteGroup.gid;
                        if (siteHolder.getSiteByTitle(site.title) == null) {
                            sid = siteHolder.addSite(site);
                            if (sid < 0) {
                                return "插入数据库失败";
                            }
                            site.sid = sid;
                            siteHolder.updateSiteIndex(site);
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                return "站点还原失败";
            }
            return "站点还原成功";
        }
    }

    public String FavouriteRestore() {
        String json = FileHelper.readString(Names.favouritesname, DownloadManager.getDownloadPath(), Names.backupdirname);
        if (json == null) {
            return "未在下载目录中找到收藏夹备份";
        } else {
            try {
                List<Pair<CollectionGroup, List<LocalCollection>>> favGroups =
                        new Gson().fromJson(json, new TypeToken<ArrayList<Pair<CollectionGroup, ArrayList<LocalCollection>>>>() {}.getType());
                FavouriteHolder holder = new FavouriteHolder(mContext);
                for(Pair<CollectionGroup, List<LocalCollection>> pair : favGroups){
                    CollectionGroup group = holder.getGroupByTitle(pair.first.title);
                    if(group==null) {
                        group = pair.first;
                        group.gid = holder.addFavGroup(group);
                    }
                    for (LocalCollection collection : pair.second) {
                        collection.gid = group.gid;
                        holder.addFavourite(collection);
                    }
                }
                holder.onDestroy();
                return "收藏夹还原成功";
            } catch (Exception e) {
                e.printStackTrace();
                return "收藏夹还原失败";
            }
        }
    }
}


