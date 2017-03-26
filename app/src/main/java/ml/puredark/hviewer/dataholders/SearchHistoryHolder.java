package ml.puredark.hviewer.dataholders;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2016/8/12.
 */

public class SearchHistoryHolder extends AbstractTagHolder {
    private static List<String> searchHistories;
    private Context mContext;

    public SearchHistoryHolder(Context context) {
        this.mContext = context;
        String searchHistoryStr = (String) SharedPreferencesUtil.getData(context, "SearchHistory", "[]");
        searchHistories = new Gson().fromJson(searchHistoryStr, new TypeToken<ArrayList<String>>() {
        }.getType());
        removeDuplicate();
    }

    public synchronized void removeDuplicate() {
        searchHistories = new ArrayList(new HashSet(searchHistories));
    }

    public synchronized void clear() {
        searchHistories = new ArrayList();
        saveSearchHistory();
    }

    public synchronized void saveSearchHistory() {
        removeDuplicate();
        SharedPreferencesUtil.saveData(mContext, "SearchHistory", new Gson().toJson(searchHistories));
    }

    public synchronized void addSearchHistory(String item) {
        if (item == null) return;
        deleteSearchHistory(item);
        searchHistories.add(0, item);
        trimSearchHistory();
        saveSearchHistory();
    }

    public synchronized void deleteSearchHistory(String item) {
        for (int i = 0, size = searchHistories.size(); i < size; i++) {
            if (searchHistories.get(i).equals(item)) {
                searchHistories.remove(i);
                size--;
                i--;
            }
        }
        saveSearchHistory();
    }

    public synchronized void trimSearchHistory() {
        while (searchHistories.size() > 50)
            searchHistories.remove(50);
    }

    public List<String> getSearchHistory() {
        if (searchHistories == null)
            return new ArrayList<>();
        else
            return searchHistories;
    }

    public List<Tag> getSearchHistoryAsTag() {
        if (searchHistories == null)
            return new ArrayList<>();
        else {
            List<Tag> tags = new ArrayList<>();
            for (String keyword : searchHistories) {
                tags.add(new Tag(tags.size() + 1, keyword));
            }
            return tags;
        }
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

    public boolean searchHistoryExist(String item) {
        if (item == null) return false;
        for (String searchHistory : searchHistories) {
            if (searchHistory.equals(item))
                return true;
        }
        return false;
    }

    @Override
    public void addTag(int sid, Tag item) {
    }

    @Override
    public void clear(int sid) {
        clear();
    }

    @Override
    public void deleteTag(int sid, Tag item) {
        deleteSearchHistory(item.title);
    }

    @Override
    public List<Tag> getTags(int sid) {
        return getSearchHistoryAsTag();
    }

    @Override
    public boolean tagExist(int sid, Tag item) {
        return searchHistoryExist(item.title);
    }
}
