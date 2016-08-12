package ml.puredark.hviewer.helpers;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.utils.MathUtil;
import ml.puredark.hviewer.utils.RegexValidateUtil;

import static java.util.regex.Pattern.DOTALL;

/**
 * Created by PureDark on 2016/8/9.
 */

public class RuleParser {

    public static Map<String, String> parseUrl(String url) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("\\{(.*?):(.*?)\\}", DOTALL);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    public static List<Collection> getCollections(List<Collection> collections, String html, Rule rule, String sourceUrl) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select(rule.item.selector);
        for (Element element : elements) {
            String itemStr;
            if ("attr".equals(rule.item.fun)) {
                itemStr = element.attr(rule.title.param);
            } else if ("html".equals(rule.item.fun)) {
                itemStr = element.html();
            } else {
                itemStr = element.toString();
            }
            if (rule.item.regex != null) {
                Pattern pattern = Pattern.compile(rule.item.regex);
                Matcher matcher = pattern.matcher(itemStr);
                if (!matcher.find()) {
                    continue;
                }
            }

            Collection collection = new Collection(collections.size() + 1);
            collection = getCollectionDetail(collection, element, rule, sourceUrl);

            collections.add(collection);
        }
        return collections;
    }

    public static Collection getCollectionDetail(Collection collection, String html, Rule rule, String sourceUrl) {
        Document element = Jsoup.parse(html);
        return getCollectionDetail(collection, element, rule, sourceUrl);
    }

    public static Collection getCollectionDetail(Collection collection, Element element, Rule rule, String sourceUrl) {
        Elements temp;

        String idCode = "";
        if (rule.idCode != null) {
            temp = element.select(rule.idCode.selector);
            if ("attr".equals(rule.idCode.fun)) {
                idCode = temp.attr(rule.idCode.param);
            } else if ("html".equals(rule.idCode.fun)) {
                idCode = temp.html();
            }
            if (rule.idCode.regex != null) {
                Pattern pattern = Pattern.compile(rule.idCode.regex, DOTALL);
                Matcher matcher = pattern.matcher(idCode);
                if (matcher.find()) {
                    idCode = matcher.group(1);
                }
            }
        }

        String title = "";
        if (rule.title != null) {
            temp = element.select(rule.title.selector);
            if ("attr".equals(rule.title.fun)) {
                title = temp.attr(rule.title.param);
            } else if ("html".equals(rule.title.fun)) {
                title = temp.html();
            }
            if (rule.title.regex != null && title != null) {
                Pattern pattern = Pattern.compile(rule.title.regex, DOTALL);
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    title = matcher.group(1);
                }
            }
        }

        String uploader = "";
        if (rule.uploader != null) {
            temp = element.select(rule.uploader.selector);
            if ("attr".equals(rule.uploader.fun)) {
                uploader = temp.attr(rule.uploader.param);
            } else if ("html".equals(rule.uploader.fun)) {
                uploader = temp.html();
            }
            if (rule.uploader.regex != null && uploader != null) {
                Pattern pattern = Pattern.compile(rule.uploader.regex, DOTALL);
                Matcher matcher = pattern.matcher(uploader);
                if (matcher.find()) {
                    uploader = matcher.group(1);
                }
            }
        }

        String cover = "";
        if (rule.cover != null) {
            temp = element.select(rule.cover.selector);
            if ("attr".equals(rule.cover.fun)) {
                cover = temp.attr(rule.cover.param);
            } else if ("html".equals(rule.cover.fun)) {
                cover = temp.html();
            }
            if (rule.cover.regex != null && cover != null) {
                Pattern pattern = Pattern.compile(rule.cover.regex, DOTALL);
                Matcher matcher = pattern.matcher(cover);
                if (matcher.find()) {
                    cover = matcher.group(1);
                }
            }
            cover = RegexValidateUtil.getAbsoluteUrlFromRelative(cover, sourceUrl);
        }

        String category = "";
        if (rule.category != null) {
            temp = element.select(rule.category.selector);
            if ("attr".equals(rule.category.fun)) {
                category = temp.attr(rule.category.param);
            } else if ("html".equals(rule.category.fun)) {
                category = temp.html();
            }
            if (rule.category.regex != null && category != null) {
                Pattern pattern = Pattern.compile(rule.category.regex, DOTALL);
                Matcher matcher = pattern.matcher(category);
                if (matcher.find()) {
                    category = matcher.group(1);
                }
            }
        }

        String datetime = "";
        if (rule.datetime != null) {
            temp = element.select(rule.datetime.selector);
            if ("attr".equals(rule.datetime.fun)) {
                datetime = temp.attr(rule.datetime.param);
            } else if ("html".equals(rule.datetime.fun)) {
                datetime = temp.html();
            }
            if (rule.datetime.regex != null && datetime != null) {
                Pattern pattern = Pattern.compile(rule.datetime.regex, DOTALL);
                Matcher matcher = pattern.matcher(datetime);
                if (matcher.find()) {
                    datetime = matcher.group(1);
                }
            }
        }

        String ratingStr = "";
        float rating = 0;
        if (rule.rating != null) {
            temp = element.select(rule.rating.selector);
            if (rule.rating.fun != null && rule.rating.fun.contains("attr")) {
                ratingStr = temp.attr(rule.rating.param);
            } else if ("html".equals(rule.rating.fun)) {
                ratingStr = temp.html();
            }
            if (rule.rating.regex != null && ratingStr != null) {
                Pattern pattern = Pattern.compile(rule.rating.regex, DOTALL);
                Matcher matcher = pattern.matcher(ratingStr);
                if (matcher.find()) {
                    ratingStr = matcher.group(1);
                }
            }

            if (ratingStr.matches("\\d+(.\\d+)?") && ratingStr.indexOf(".") > 0) {
                rating = Float.parseFloat(ratingStr);
            } else if (StringUtil.isNumeric(ratingStr)) {
                rating = Float.parseFloat(ratingStr);
            } else {
                rating = Math.min(ratingStr.replace(" ", "").length(), 5);
            }

            if (rule.rating.fun != null) {
                Pattern pattern0 = Pattern.compile("\\|\\|(.*)", DOTALL);
                Matcher matcher0 = pattern0.matcher(rule.rating.fun);
                if (matcher0.find()) {
                    String exp = matcher0.group(1);
                    exp = exp.replace("{1}", "" + (int) rating);
                    String result = MathUtil.computeString(exp);
                    try {
                        rating = Float.parseFloat(result);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }


        List<Tag> tags = new ArrayList<>();
        if (rule.tags != null) {
            temp = element.select(rule.tags.selector);
            for (Element tagElement : temp) {
                String tagStr;
                if ("attr".equals(rule.tags.fun)) {
                    tagStr = tagElement.attr(rule.tags.param);
                } else if ("html".equals(rule.tags.fun)) {
                    tagStr = tagElement.html();
                } else {
                    continue;
                }

                if (rule.tags.regex != null && tagStr != null) {
                    Pattern pattern = Pattern.compile(rule.tags.regex, DOTALL);
                    Matcher matcher = pattern.matcher(tagStr);
                    while (matcher.find()) {
                        tags.add(new Tag(tags.size() + 1, matcher.group(1)));
                    }
                } else {
                    tags.add(new Tag(tags.size() + 1, tagStr));
                }
            }
        }

        List<Picture> pictures = new ArrayList<>();
        if (rule.pictures != null) {

            temp = element.select(rule.pictures.selector);
            Log.d("RuleParser", "temp.size():" + temp.size());
            for (Element pictureElement : temp) {
                String pictureStr;
                if ("attr".equals(rule.pictures.fun)) {
                    pictureStr = pictureElement.attr(rule.pictures.param);
                } else if ("html".equals(rule.pictures.fun)) {
                    pictureStr = pictureElement.html();
                } else {
                    pictureStr = pictureElement.toString();
                }

                if (rule.pictures.regex != null && pictureStr != null) {
                    Pattern pattern = Pattern.compile(rule.pictures.regex, DOTALL);
                    Matcher matcher = pattern.matcher(pictureStr);
                    if (matcher.find()) {
                        String url = RegexValidateUtil.getAbsoluteUrlFromRelative(matcher.group(1), sourceUrl);
                        String thumbnail = RegexValidateUtil.getAbsoluteUrlFromRelative(matcher.group(2), sourceUrl);
                        pictures.add(new Picture(pictures.size() + 1, url, thumbnail));
                    }
                } else {
                    pictureStr = RegexValidateUtil.getAbsoluteUrlFromRelative(pictureStr, sourceUrl);
                    pictures.add(new Picture(pictures.size() + 1, pictureStr, ""));
                }
            }
        }

        if (idCode != null && !idCode.equals(""))
            collection.idCode = idCode;
        if (title != null && !title.equals(""))
            collection.title = title;
        if (uploader != null && !uploader.equals(""))
            collection.uploader = uploader;
        if (cover != null && !cover.equals(""))
            collection.cover = cover;
        if (category != null && !category.equals(""))
            collection.category = category;
        if (datetime != null && !datetime.equals(""))
            collection.datetime = datetime;
        if (rating > 0)
            collection.rating = rating;
        if (tags != null && tags.size() > 0)
            collection.tags = tags;
        if (pictures != null && pictures.size() > 0)
            collection.pictures = pictures;
        return collection;
    }

    public static String getPictureUrl(String html, Selector selector, String sourceUrl) {
        Document doc = Jsoup.parse(html);
        String url = null;
        if (selector != null) {
            Elements temp = doc.select(selector.selector);
            if ("attr".equals(selector.fun)) {
                url = temp.attr(selector.param);
            } else if ("html".equals(selector.fun)) {
                url = temp.html();
            }
            if (selector.regex != null && url != null) {
                Pattern pattern = Pattern.compile(selector.regex, DOTALL);
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    url = matcher.group(1);
                }
            }
            url = RegexValidateUtil.getAbsoluteUrlFromRelative(url, sourceUrl);
        }
        return url;
    }

}
