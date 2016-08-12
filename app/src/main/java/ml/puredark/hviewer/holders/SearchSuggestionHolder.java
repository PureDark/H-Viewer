package ml.puredark.hviewer.holders;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.utils.SharedPreferencesUtil;


/**
 * Created by PureDark on 2016/8/12.
 */

public class SearchSuggestionHolder {
    public static List<String> searchSuggestions;
    private Context mContext;

    public SearchSuggestionHolder(Context context) {
        this.mContext = context;
        String searchSuggestionStr = (String) SharedPreferencesUtil.getData(context, "SearchSuggestion",
                "[\"double anal\",\"pregnant\",\"furry\",\"impregnation\",\"defloration\",\"lizard guy\",\"double penetration\",\"eggs\",\"sole female\",\"drugs\",\"exhibitionism\",\"fox boy\",\"kissing\",\"bbm\",\"anal\",\"big penis\",\"orc\",\"bestiality\",\"huge penis\",\"nakadashi\",\"elf\",\"milf\",\"schoolgirl uniform\",\"bondage\",\"females only\",\"ahegao\",\"sister\",\"gender bender\",\"glasses\",\"tanlines\",\"thigh high boots\",\"yaoi\",\"muscle\",\"vore\",\"shibari\",\"incest\",\"collar\",\"stockings\",\"huge breasts\",\"x-ray\",\"futanari\",\"big breasts\",\"schoolboy uniform\",\"dark skin\",\"cheating\",\"monster\",\"sunglasses\",\"males only\",\"father\",\"garter belt\",\"small breasts\",\"bunny girl\",\"masturbation\",\"bbw\",\"mind control\",\"harem\",\"blowjob\",\"catgirl\",\"yuri\",\"sex toys\",\"sole male\",\"orgasm denial\",\"rape\",\"dilf\",\"lizard girl\",\"human on furry\",\"parasite\",\"dougi\",\"kimono\",\"bikini\",\"mouse\",\"fundoshi\",\"shemale\",\"monster girl\",\"dickgirl on male\",\"swimsuit\",\"cat\",\"freckles\",\"dickgirl on dickgirl\",\"solo action\",\"giantess\",\"big areolae\",\"age progression\",\"group\",\"snake girl\",\"demon girl\",\"tentacles\",\"alien girl\",\"shotacon\",\"unusual pupils\",\"cunnilingus\",\"fingering\",\"business suit\",\"lactation\",\"sundress\",\"handjob\",\"filming\",\"catboy\",\"bisexual\",\"big ass\",\"tomboy\",\"monkey\",\"tickling\",\"crossdressing\",\"tomgirl\",\"footjob\",\"fox girl\",\"squid girl\",\"inseki\",\"octopus\",\"first person perspective\",\"teacher\",\"eye penetration\",\"tribadism\",\"paizuri\",\"oni\",\"onahole\",\"triple penetration\",\"eyepatch\",\"dog girl\",\"fox\",\"prostitution\",\"foot licking\",\"tall girl\",\"minigirl\",\"mother\",\"ghost\",\"aunt\",\"vampire\",\"wrestling\",\"lingerie\",\"femdom\",\"insect girl\",\"horse girl\",\"gyaru\",\"inflation\",\"old man\",\"old lady\",\"school swimsuit\",\"insect boy\",\"miko\"]");
        searchSuggestions = new Gson().fromJson(searchSuggestionStr, new TypeToken<ArrayList<String>>() {
        }.getType());
    }


    public void saveSearchSuggestion() {
        SharedPreferencesUtil.saveData(mContext, "SearchSuggestion", new Gson().toJson(searchSuggestions));
    }

    public void addSearchSuggestion(String item) {
        if (item == null) return;
        deleteSearchSuggestion(item);
        searchSuggestions.add(0, item);
        saveSearchSuggestion();
    }

    public void deleteSearchSuggestion(String item) {
        for (int i = 0, size = searchSuggestions.size(); i < size; i++) {
            if (searchSuggestions.get(i).equals(item)) {
                searchSuggestions.remove(i);
                size--;
                i--;
            }
        }
        saveSearchSuggestion();
    }

    public List<String> getSearchSuggestion() {
        if(searchSuggestions==null)
            return new ArrayList<>();
        else
            return searchSuggestions;
    }

    public List<String> getSearchSuggestion(String query) {
        List<String> keywords = new ArrayList<>();
        if (searchSuggestions != null) {
            for (String keyword : searchSuggestions) {
                if (keyword.startsWith(query))
                    keywords.add(keyword);
            }
        }
        return keywords;
    }

}
