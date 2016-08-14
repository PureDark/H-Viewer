package ml.puredark.hviewer.helpers;

import android.text.Html;

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
import ml.puredark.hviewer.utils.StringEscapeUtils;

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

        String idCode = parseSingleProperty(element, rule.idCode, sourceUrl, false);

        String title = parseSingleProperty(element, rule.title, sourceUrl, false);

        String uploader = parseSingleProperty(element, rule.uploader, sourceUrl, false);

        String cover = parseSingleProperty(element, rule.cover, sourceUrl, true);

        String category = parseSingleProperty(element, rule.category, sourceUrl, false);

        String datetime = parseSingleProperty(element, rule.datetime, sourceUrl, false);

        String ratingStr = parseSingleProperty(element, rule.rating, sourceUrl, false);

        float rating;

        if (ratingStr.matches("\\d+(.\\d+)?") && ratingStr.indexOf(".") > 0) {
            rating = Float.parseFloat(ratingStr);
        } else if (StringUtil.isNumeric(ratingStr)) {
            rating = Float.parseFloat(ratingStr);
        } else {
            String result = MathUtil.computeString(ratingStr);
            try {
                rating = Float.parseFloat(result);
            } catch (NumberFormatException e) {
                rating = Math.min(ratingStr.replace(" ", "").length(), 5);
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
                    tagStr = tagElement.toString();
                }
                if (rule.tags.regex != null) {
                    Pattern pattern = Pattern.compile(rule.tags.regex, DOTALL);
                    Matcher matcher = pattern.matcher(tagStr);
                    while (matcher.find() && matcher.groupCount() >= 1) {
                        if (rule.tags.replacement != null) {
                            tagStr = rule.tags.replacement;
                            for (int i = 1; i <= matcher.groupCount(); i++)
                                tagStr = tagStr.replaceAll("\\$" + i, matcher.group(i));
                            tags.add(new Tag(tags.size() + 1, tagStr));
                        } else {
                            tagStr = matcher.group(1);
                            tags.add(new Tag(tags.size() + 1, tagStr));
                        }
                    }
                } else {
                    tags.add(new Tag(tags.size() + 1, tagStr));
                }
            }
        }

        List<Picture> pictures = new ArrayList<>();
        if (rule.item != null && rule.pictureUrl != null && rule.pictureThumbnail != null) {
            temp = element.select(rule.item.selector);
            for (Element pictureElement : temp) {
                String pictureUrl = parseSingleProperty(pictureElement, rule.pictureUrl, sourceUrl, true);
                String pictureThumbnail = parseSingleProperty(pictureElement, rule.pictureThumbnail, sourceUrl, true);
                pictures.add(new Picture(pictures.size() + 1, pictureUrl, pictureThumbnail));
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

    public static String parseSingleProperty(Element element, Selector selector, String sourceUrl, boolean isUrl) {
        String prop = "";

        if (selector != null) {
            Elements temp = element.select(selector.selector);
            if (temp != null) {
                if ("attr".equals(selector.fun)) {
                    prop = temp.attr(selector.param);
                } else if ("html".equals(selector.fun)) {
                    prop = temp.html();
                } else {
                    prop = temp.toString();
                }
                if (selector.regex != null) {
                    Pattern pattern = Pattern.compile(selector.regex, DOTALL);
                    Matcher matcher = pattern.matcher(prop);
                    if (matcher.find() && matcher.groupCount() >= 1) {
                        if (selector.replacement != null) {
                            prop = selector.replacement;
                            for (int i = 1; i <= matcher.groupCount(); i++)
                                prop = prop.replaceAll("\\$" + i, matcher.group(i));
                        } else {
                            prop = matcher.group(1);
                        }
                    }
                }
                if (isUrl)
                    prop = RegexValidateUtil.getAbsoluteUrlFromRelative(prop, sourceUrl);
            }
        }
        return StringEscapeUtils.unescapeHtml(prop);
    }

    public static String getPictureUrl(String html, Selector selector, String sourceUrl) {
        Document doc = Jsoup.parse(html);
        return parseSingleProperty(doc, selector, sourceUrl, true);
    }

}
