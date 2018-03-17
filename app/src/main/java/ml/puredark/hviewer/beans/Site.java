package ml.puredark.hviewer.beans;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.libraries.advrecyclerview.common.data.AbstractExpandableDataProvider;

public class Site extends AbstractExpandableDataProvider.ChildData {
    public final static String FLAG_NO_COVER = "noCover";
    public final static String FLAG_NO_TITLE = "noTitle";
    public final static String FLAG_NO_RATING = "noRating";
    public final static String FLAG_NO_TAG = "noTag";
    public final static String FLAG_WATERFALL_AS_LIST = "waterfallAsList";
    public final static String FLAG_WATERFALL_AS_GRID = "waterfallAsGrid";
    public final static String FLAG_SECOND_LEVEL_GALLERY = "secondLevelGallery";
    public final static String FLAG_REPEATED_THUMBNAIL = "repeatedThumbnail";
    public final static String FLAG_SINGLE_PAGE_BIG_PICTURE = "singlePageBigPicture";
    public final static String FLAG_PRELOAD_GALLERY = "preloadGallery";
    public final static String FLAG_ONE_PIC_GALLERY = "onePicGallery";
    public final static String FLAG_EXTRA_INDEX_INFO = "extraIndexInfo";
    public final static String FLAG_JS_NEEDED_ALL = "jsNeededAll";
    public final static String FLAG_JS_NEEDED_INDEX = "jsNeededIndex";
    public final static String FLAG_JS_NEEDED_GALLERY = "jsNeededGallery";
    public final static String FLAG_JS_NEEDED_PICTURE = "jsNeededPicture";
    public final static String FLAG_JS_SCROLL = "jsScroll";
    public final static String FLAG_IFRAME_GALLERY = "iframeGallery";
    public final static String FLAG_POST_ALL = "postAll";
    public final static String FLAG_POST_INDEX = "postIndex";
    public final static String FLAG_POST_GALLERY = "postGallery";
    public final static String FLAG_POST_PICTURE = "postPicture";
    public final static String FLAG_LOGIN_REQUIRED = "loginRequired";
    public final static String FLAG_COVER_LEFT = "coverLeft";
    public final static String FLAG_COVER_RIGHT = "coverRight";
    public final static String FLAG_COVER_CENTER = "coverCenter";

    public int sid, gid;
    public String title = "";
    public String indexUrl = "", galleryUrl = "", searchUrl = "", loginUrl = "";
    public List<Category> categories;
    public Rule indexRule, galleryRule, searchRule, extraRule;
    public int versionCode;

    @Deprecated
    public Selector picUrlSelector;

    public String cookie = "";
    public String header = "";
    public String flag = "";
    public int index;
    public boolean isGrid = false;
    public boolean disableHProxy = false;

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
    public int getId() {
        return sid;
    }

    @Override
    public long getChildId() {
        return sid;
    }

    @Override
    public String getText() {
        return title;
    }


    public List<Pair<String, String>> getHeaders() {
        List<Pair<String, String>> headers = new ArrayList<>();
        if (!TextUtils.isEmpty(cookie))
            headers.add(new Pair<>("cookie", cookie));
        if (!TextUtils.isEmpty(header)) {
            Pattern pattern = Pattern.compile("([^\\r\\n]*?):([^\\r\\n]*)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(header);
            while (matcher.find() && matcher.groupCount() == 2) {
                headers.add(new Pair<>(matcher.group(1), matcher.group(2)));
            }
        }
        return headers;
    }

    public boolean hasFlag(String flag) {
        if (this.flag == null)
            return false;
        else
            return this.flag.contains(flag);
    }

    public String getListUrl(String url, int page, String keyword, List<Collection> collections) {
        Object[] array = (collections != null) ? collections.toArray() : null;
        return RuleParser.parseUrl(url, page, "", keyword, array);
    }

    public String getGalleryUrl(String idCode, int page, List<Picture> pictures) {
        return getGalleryUrl(galleryUrl, idCode, page, pictures);
    }

    public String getGalleryUrl(String inUrl, String idCode, int page, List<Picture> pictures) {
        Object[] array = (pictures != null) ? pictures.toArray() : null;
        return RuleParser.parseUrl(inUrl, page, idCode, "", array);
    }

    public static String getJsonParams(String url, int page, String keyword, List<Collection> collections) {
        Object[] array = (collections != null) ? collections.toArray() : null;
        return RuleParser.parseUrl(url, page, "", keyword, array, true);
    }

    public boolean isFirstLoadSecondLevelGallery(List<Picture> pictures) {
        return (pictures != null && pictures.size() > 0 && this.hasFlag(Site.FLAG_SECOND_LEVEL_GALLERY)
                && !Picture.hasPicPosfix(pictures.get(0).url) && this.extraRule != null);
    }

    public void replace(Site site) {
        if (site == null)
            return;
        Field[] fs = Site.class.getDeclaredFields();
        try {
            for (Field f : fs) {
                if ("sid".equals(f.getName()) || "gid".equals(f.getName()) || "index".equals(f.getName()))
                    continue;
                f.setAccessible(true);
                if (f.getType() == String.class) {
                    String value = (String) f.get(site);
                    f.set(this, value);
                } else if (f.getType() == Integer.class) {
                    int value = (int) f.get(site);
                    if (value != 0)
                        f.set(this, value);
                } else if ("categories".equals(f.getName())) {
                    List<Category> categories = (List<Category>) f.get(site);
                    if (this.categories != null) {
                        if (categories == null)
                            categories = new ArrayList<>();
                        for (Category category : this.categories) {
                            if (!this.categories.contains(category))
                                categories.add(category);
                        }
                    }
                    f.set(this, categories);
                } else if (f.getType() == Selector.class) {
                    Selector oldProp = (Selector) f.get(this);
                    Selector newProp = (Selector) f.get(site);
                    /*if (oldProp == null)
                        oldProp = newProp;
                    else
                        oldProp.replace(newProp);*/
                    oldProp = newProp;
                    f.set(this, oldProp);
                } else if (f.getType() == Rule.class) {
                    Rule oldProp = (Rule) f.get(this);
                    Rule newProp = (Rule) f.get(site);
                    /*if (oldProp == null)
                        oldProp = newProp;
                    else
                        oldProp.replace(newProp);*/
                    oldProp = newProp;
                    f.set(this, oldProp);
                } else {
                    f.set(this, f.get(site));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
