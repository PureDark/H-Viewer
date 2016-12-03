package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.os.Environment;
import android.support.v4.util.Pair;
import android.util.Xml;


import com.google.gson.Gson;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.dataholders.SiteHolder;

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
    //文件路径
    private String SDPATH = Environment.getExternalStorageDirectory()  + "/H-ViewerSites.xml/" ;

    public String DoBackup() {
        File file = new File( SDPATH ) ;
        if( !file.exists() ){
            try {
                file.createNewFile() ;
            } catch (IOException e) {
                e.printStackTrace();
                return "创建文件出错";
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "utf-8");
            serializer.startDocument("utf-8", true);

            siteHolder = new SiteHolder(mContext);
            final List<Pair<SiteGroup, List<Site>>> siteGroups = siteHolder.getSites();

            serializer.startTag(null, "sites");
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
            return "写入文件出错";
        }
        return "备份成功";
    }
}
