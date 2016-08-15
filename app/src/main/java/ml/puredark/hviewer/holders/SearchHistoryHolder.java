package ml.puredark.hviewer.holders;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2016/8/12.
 */

public class SearchHistoryHolder {
    private static List<String> searchHistories;
    private Context mContext;

    public SearchHistoryHolder(Context context) {
        this.mContext = context;
        String searchHistoryStr = (String) SharedPreferencesUtil.getData(context, "SearchHistory", "[]");
        searchHistories = new Gson().fromJson(searchHistoryStr, new TypeToken<ArrayList<String>>() {
        }.getType());
        removeDuplicate();
    }

    public void removeDuplicate(){
        searchHistories = new ArrayList(new HashSet(searchHistories));
    }

    public void saveSearchHistory() {
        removeDuplicate();
        SharedPreferencesUtil.saveData(mContext, "SearchHistory", new Gson().toJson(searchHistories));
    }

    public void addSearchHistory(String item) {
        if (item == null) return;
        deleteSearchHistory(item);
        searchHistories.add(0, item);
        trimSearchHistory();
        saveSearchHistory();
    }

    public void deleteSearchHistory(String item) {
        for (int i = 0, size = searchHistories.size(); i < size; i++) {
            if (searchHistories.get(i).equals(item)) {
                searchHistories.remove(i);
                size--;
                i--;
            }
        }
        saveSearchHistory();
    }

    public void trimSearchHistory() {
        while (searchHistories.size() > 100)
            searchHistories.remove(100);
    }

    public List<String> getSearchHistory() {
        if (searchHistories == null)
            return new ArrayList<>();
        else
            return searchHistories;
    }

    public List<String> getSearchHistory(String query) {
        List<String> keywords = new ArrayList<>();
        if (searchHistories != null) {
            for (String keyword : searchHistories) {
                if (keyword.startsWith(query))
                    keywords.add(keyword);
            }
        }
        return keywords;
    }

}
