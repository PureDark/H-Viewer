package ml.puredark.hviewer.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.utils.RegexValidateUtil;
import okhttp3.Cookie;

public class Site extends AbstractDataProvider.Data {
    public int sid;
    public String title = "";
    public String indexUrl = "", galleryUrl = "", searchUrl = "";
    public List<Category> categories;
    public Rule indexRule, galleryRule, searchRule;
    public Selector picUrlSelector;
    public String cookie = "";

    public Site() {
    }

    public Site(int sid, String title, String indexUrl, String galleryUrl, String searchUrl,
                Rule indexRule, Rule galleryRule, Rule searchRule, Selector picUrlSelector) {
        this.sid = sid;
        this.title = title;
        this.indexUrl = indexUrl;
        this.galleryUrl = galleryUrl;
        this.searchUrl = searchUrl;
        this.indexRule = indexRule;
        this.galleryRule = galleryRule;
        this.searchRule = searchRule;
        this.picUrlSelector = picUrlSelector;
    }

    public void setCategories(List<Category> categories){
        this.categories = categories;
    }

    @Override
    public int getId() {
        return sid;
    }

    public List<Cookie> getCookies() {
        List<Cookie> cookies = new ArrayList<>();
        if(cookie==null||"".equals(cookie))
            return cookies;
        Pattern pattern = Pattern.compile("(.*?)=(.*?);", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cookie);
        while(matcher.find()){
            cookies.add(new Cookie.Builder()
                    .name(matcher.group(1).trim())
                    .value(matcher.group(2).trim())
                    .domain(RegexValidateUtil.getDominFromUrl(indexUrl))
                    .build()
            );
        }
        return cookies;
    }

}
