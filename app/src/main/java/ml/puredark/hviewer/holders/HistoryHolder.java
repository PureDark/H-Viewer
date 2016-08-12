package ml.puredark.hviewer.holders;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2016/8/12.
 */

public class HistoryHolder {
    private Context mContext;
    private List<Collection> histories;

    public HistoryHolder(Context context) {
        this.mContext = context;
        String historyStr = (String) SharedPreferencesUtil.getData(context, "History", "[]");
        histories = new Gson().fromJson(historyStr, new TypeToken<ArrayList<Collection>>() {
        }.getType());
    }

    public void saveHistory() {
        SharedPreferencesUtil.saveData(mContext, "History", new Gson().toJson(histories));
    }

    public void addHistory(Collection item) {
        if (item == null) return;
        deleteHistory(item);
        histories.add(0, item);
        trimHistory();
        saveHistory();
    }

    public void deleteHistory(Collection item) {
        for (int i = 0, size = histories.size(); i < size; i++) {
            if (histories.get(i).equals(item)) {
                histories.remove(i);
                size--;
                i--;
            }
        }
        saveHistory();
    }

    public void trimHistory() {
        while (histories.size() > 20)
            histories.remove(20);
    }

    public List<Collection> getHistory() {
        if (histories == null)
            return new ArrayList<>();
        else
            return histories;
    }
}
