package ml.puredark.hviewer.beans;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.libraries.advrecyclerview.common.data.AbstractExpandableDataProvider;
import ml.puredark.hviewer.utils.RegexValidateUtil;
import okhttp3.Cookie;

public class Site extends AbstractExpandableDataProvider.ChildData {
    public final static String FLAG_NO_COVER = "noCover";
    public final static String FLAG_NO_TITLE = "noTitle";
    public final static String FLAG_NO_RATING = "noRating";
    public final static String FLAG_NO_TAG = "noTag";
    public final static String FLAG_SECOND_LEVEL_GALLERY = "secondLevelGallery";
    public final static String FLAG_REPEATED_THUMBNAIL = "repeatedThumbnail";
    public final static String FLAG_SINGLE_PAGE_BIG_PICTURE = "singlePageBigPicture";
    public final static String FLAG_PRELOAD_GALLERY = "preloadGallery";
    public final static String FLAG_JS_NEEDED = "jsNeeded";

    public int sid, gid;
    public String title = "";
    public String indexUrl = "", galleryUrl = "", searchUrl = "", loginUrl = "";
    public List<Category> categories;
    public Rule indexRule, galleryRule, searchRule, extraRule;
    public int versionCode;

    @Deprecated
    public Selector picUrlSelector;

    public String cookie = "";
    public String flag = "";
    public int index;

    public Site() {
    }

    public Site(int sid, String title, String indexUrl, String galleryUrl, String searchUrl, String loginUrl,
                Rule indexRule, Rule galleryRule, Rule searchRule, Rule extraRule, String flag) {
        this.sid = sid;
        this.title = title;
        this.indexUrl = indexUrl;
        this.galleryUrl = galleryUrl;
        this.searchUrl = searchUrl;
        this.loginUrl = loginUrl;
        this.indexRule = indexRule;
        this.galleryRule = galleryRule;
        this.searchRule = searchRule;
        this.extraRule = extraRule;
        this.flag = flag;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void setGroupId(int gid) {
        this.gid = gid;
    }

    @Override
    public long getChildId() {
        return sid;
    }

    @Override
    public String getText() {
        return title;
    }


    public List<Cookie> getCookies() {
        List<Cookie> cookies = new ArrayList<>();
        if (cookie == null || "".equals(cookie))
            return cookies;
        Pattern pattern = Pattern.compile("(.*?)=([^;]*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cookie);
        while (matcher.find()) {
            cookies.add(new Cookie.Builder()
                    .name(matcher.group(1).trim())
                    .value(matcher.group(2).trim())
                    .domain(RegexValidateUtil.getDominFromUrl(indexUrl))
                    .build()
            );
        }
        return cookies;
    }

    public boolean hasFlag(String flag) {
        if (this.flag == null)
            return false;
        else
            return this.flag.contains(flag);
    }

    public String getListUrl(String url, int page, String keyword) {
        Map<String, String> matchResult = RuleParser.parseUrl(url);
        String pageStr = matchResult.get("page");
        int startPage;
        try {
            startPage = (pageStr != null) ? Integer.parseInt(pageStr) : 0;
        } catch (NumberFormatException e) {
            startPage = 0;
        }
        url = url.replaceAll("\\{pageStr:(.*?\\{.*?\\}.*?)\\}", (page == startPage) ? "" : "" + matchResult.get("pageStr"))
                .replaceAll("\\{page:.*?\\}", "" + page)
                .replaceAll("\\{keyword:.*?\\}", keyword);
        return url;
    }

    public String getGalleryUrl(String idCode, int page) {
        return galleryUrl.replaceAll("\\{idCode:\\}", idCode)
                .replaceAll("\\{page:\\d+?\\}", "" + page);
    }

    public void replace(Site site) {
        Field[] fs = Site.class.getDeclaredFields();
        try {
            for (Field f : fs) {
                if ("sid".equals(f.getName()) || "gid".equals(f.getName()) || "index".equals(f.getName()))
                    continue;
                f.setAccessible(true);
                if (f.getType() == String.class) {
                    String value = (String) f.get(site);
                    if (!TextUtils.isEmpty(value))
                        f.set(this, value);
                } else if (f.getType() == Integer.class) {
                    int value = (int) f.get(site);
                    if (value != 0)
                        f.set(this, value);
                } else if("categories".equals(f.getName())){
                    List<Category> categories = (List<Category>) f.get(site);
                    if(this.categories!=null){
                        if(categories==null)
                            categories = new ArrayList<>();
                        for(Category category : this.categories){
                            if(!this.categories.contains(category))
                                categories.add(category);
                        }
                    }
                    f.set(this, categories);
                } else {
                    f.set(this, f.get(site));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
