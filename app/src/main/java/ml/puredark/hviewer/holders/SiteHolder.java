package ml.puredark.hviewer.holders;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2016/8/12.
 */

public class SiteHolder {
    private static List<Site> sites;
    private Context mContext;

    public SiteHolder(Context context) {
        this.mContext = context;
        String siteStr = (String) SharedPreferencesUtil.getData(context, "Site", "[]");
        sites = new Gson().fromJson(siteStr, new TypeToken<ArrayList<Site>>() {
        }.getType());
    }

    public void saveSites() {
        SharedPreferencesUtil.saveData(mContext, "Site", new Gson().toJson(sites));
    }

    public void addSite(Site item) {
        if (item == null) return;
        deleteSite(item);
        sites.add(item);
        saveSites();
    }

    public void deleteSite(Site item) {
        for (int i = 0, size = sites.size(); i < size; i++) {
            if (sites.get(i).sid == item.sid) {
                sites.remove(i);
                size--;
                i--;
            }
        }
        saveSites();
    }

    public List<Site> getSites() {
        if (sites == null)
            return new ArrayList<>();
        else
            return sites;
    }

}
