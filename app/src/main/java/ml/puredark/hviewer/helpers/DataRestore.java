package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
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
    private String jsonStr;
    private Site site;
    private SiteHolder siteHolder = new SiteHolder(mContext);
    private InputStream inputStream;
    int sid;

    public String DoRestore(){
        String siteRestore = SiteRestore();
        String settingRestore = SettingRestore();
        String favouriteRestore = FavouriteRestore();
        return mContext.getString(R.string.restore_Succes);

    }

    public String SettingRestore() {
        File file = new File( FileHelper.settingPath ) ;
        if( !file.exists() ){
            return "读取文件出错";
        }
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(file));
            SharedPreferencesUtil.clearData(mContext);
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                SharedPreferencesUtil.saveData(mContext, key, v);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "设置还原失败";
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return "设置还原成功";
    }

    public String FavouriteRestore() {
        String json = FileHelper.readString("favourites.json", DownloadManager.getDownloadPath());
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
        File file = new File( FileHelper.sitePath ) ;
        if( !file.exists() ){
            return "读取文件出错";
        }
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "没有发现备份文件";
        }
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(inputStream, "UTF-8");
            int eventType = xmlPullParser.getEventType();

            while (eventType != xmlPullParser.END_DOCUMENT) {
                try {
                    if (eventType == xmlPullParser.START_TAG) {
                        if ("site".equals(xmlPullParser.getName())) {
                            jsonStr = xmlPullParser.nextText();
                            site = new Gson().fromJson(jsonStr, Site.class);
                            if (siteHolder.getSiteByTitle(site.title) == null) {
                                sid = siteHolder.addSite(site);
                                if(sid<0){
                                    return "插入数据库失败";
                                }
                                site.sid = sid;
                                site.index = sid;
                                siteHolder.updateSiteIndex(site);
                            }
                        }
                    }

                    eventType = xmlPullParser.next();
                } catch (IOException e) {
                    return "";
                }

            }


        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return "结束";
        }
        return "站点还原成功";
    }
}
