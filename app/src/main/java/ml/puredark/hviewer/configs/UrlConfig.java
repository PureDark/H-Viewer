package ml.puredark.hviewer.configs;

import java.util.Random;

/**
 * Created by PureDark on 2016/9/27.
 */

public class UrlConfig {
    public final static String updateUrl = "https://api.github.com/repos/PureDark/H-Viewer/releases/latest";
    public final static String siteSourceUrl = "https://raw.githubusercontent.com/H-Viewer-Sites/Index/master/source.json";
    public final static String bingApiUrl = "https://bing.ioliu.cn/v1/rand?type=json";

    public static String getBingAPIUrl() {
//        Random random = new Random();
//        int id = random.nextInt(8);
//        return "http://www.bing.com/HPImageArchive.aspx?format=js&idx=" + id + "&n=1";
        return bingApiUrl;
    }
}
