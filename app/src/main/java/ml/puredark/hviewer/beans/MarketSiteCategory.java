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

    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof MarketSiteCategory)) {
            MarketSiteCategory item = (MarketSiteCategory) obj;
            return equals(item.title, title);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int ret = String.valueOf(title).hashCode();
        return ret;
    }

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

        @Override
        public boolean equals(Object obj) {
            if ((obj instanceof MarketSite)) {
                MarketSite item = (MarketSite) obj;
                return MarketSiteCategory.equals(item.title, title)
                        && MarketSiteCategory.equals(item.author, author);
            }
            return false;
        }
    }

}
