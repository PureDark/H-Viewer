package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.util.Xml;

import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataholders.FavouriteHolder;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.download.DownloadManager;
import ml.puredark.hviewer.ui.adapters.MarketSiteAdapter;

import static android.app.Activity.RESULT_OK;
import static ml.puredark.hviewer.HViewerApplication.mContext;

/**
 * Created by GKF on 2016/12/1.
 */

public class DataRestore {
    //文件路径
    private String SDPATH = Environment.getExternalStorageDirectory()  + "/H-ViewerSites.xml/" ;
    private String jsonStr;
    private Site site;
    private SiteHolder siteHolder = new SiteHolder(mContext);
    private InputStream inputStream;
    int sid;

    public String DoRestore(){
        File file = new File( SDPATH ) ;
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
                        sid = siteHolder.addSite(site);
                        if(sid<0){
                            return "插入数据库失败";
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

        return mContext.getString(R.string.restore_Succes);

    }
}
