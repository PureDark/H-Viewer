package ml.puredark.hviewer.beans;

import java.util.List;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;

/**
 * Created by PureDark on 2016/9/27.
 */

public class MarketSiteCategory {
    public int cid;
    public String title, english;
    public boolean r18;
    public List<MarketSite> sites;

    public static class MarketSite extends AbstractDataProvider.Data {
        public int sid;
        public String title, author, icon, description, json;
        public int versionCode;
        public String lastUpdate;
        public boolean r18;

        @Override
        public int getId() {
            return sid;
        }
    }
}
