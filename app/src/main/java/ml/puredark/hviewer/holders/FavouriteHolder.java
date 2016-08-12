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

public class FavouriteHolder {
    private Context mContext;
    private List<Collection> favourites;

    public FavouriteHolder(Context context){
        this.mContext = context;
        String favouriteStr = (String) SharedPreferencesUtil.getData(context, "Favourite", "[]");
        favourites = new Gson().fromJson(favouriteStr, new TypeToken<ArrayList<Collection>>() {
        }.getType());
    }

    public void saveFavourite() {
        SharedPreferencesUtil.saveData(mContext, "Favourite", new Gson().toJson(favourites));
    }

    public void addFavourite(Collection item) {
        if (item == null) return;
        deleteFavourite(item);
        favourites.add(0, item);
        saveFavourite();
    }

    public void deleteFavourite(Collection item) {
        for (int i = 0, size = favourites.size(); i < size; i++) {
            if (favourites.get(i).equals(item)) {
                favourites.remove(i);
                size--;
                i--;
            }
        }
        saveFavourite();
    }

    public List<Collection> getFavourite() {
        if (favourites == null)
            return new ArrayList<>();
        else
            return favourites;
    }

    public boolean isFavourite(Collection item) {
        for (int i = 0, size = favourites.size(); i < size; i++) {
            if (favourites.get(i).equals(item))
                return true;
        }
        return false;
    }

}
