package ml.puredark.hviewer.core;

import android.text.TextUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;

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
import ml.puredark.hviewer.beans.Comment;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Tag;
import ml.puredark.hviewer.beans.Video;
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
        if (TextUtils.isEmpty(url))
            return map;
        Pattern pattern = Pattern.compile("\\{([^{}]*?):([^{}]*?)\\}", DOTALL);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        Pattern pattern2 = Pattern.compile("\\{([^{}]*?):([^{}]*?\\{[^{}]*?\\}[^{}]*?)\\}", DOTALL);
        Matcher matcher2 = pattern2.matcher(url);
        while (matcher2.find()) {
            map.put(matcher2.group(1), matcher2.group(2));
        }
        return map;
    }

    public static boolean isJson(String string) {
        if (string == null)
            return false;
        string = string.trim();
        return string.startsWith("{") || string.startsWith("[");
    }

    public static List<Collection> getCollections(List<Collection> collections, String text, Rule rule, String sourceUrl) {
        try {
            List items;
            if (!isJson(text)) {
                Document doc = Jsoup.parse(text);
                items = doc.select(rule.item.selector);
            } else {
                ReadContext ctx = JsonPath.parse(text);
                items = ctx.read(rule.item.path, new TypeRef<List<ReadContext>>() {
                });
            }
            for (Object item : items) {
                String itemStr;
                if (item instanceof Element)
                    if ("attr".equals(rule.item.fun))
                        itemStr = ((Element) item).attr(rule.title.param);
                    else if ("html".equals(rule.item.fun))
                        itemStr = ((Element) item).html();
                    else
                        itemStr = item.toString();
                else if (item instanceof ReadContext)
                    itemStr = ((ReadContext) item).jsonString();
                else
                    continue;
                if (rule.item.regex != null) {
                    Pattern pattern = Pattern.compile(rule.item.regex);
                    Matcher matcher = pattern.matcher(itemStr);
                    if (!matcher.find()) {
                        continue;
                    }
                }
                Collection collection = new Collection(collections.size() + 1);
                collection = getCollectionDetail(collection, item, rule, sourceUrl);
                collections.add(collection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collections;
    }

    public static Collection getCollectionDetail(Collection collection, String text, Rule rule, String sourceUrl) {
        try {
            if (!isJson(text)) {
                Document element = Jsoup.parse(text);
                collection = getCollectionDetail(collection, element, rule, sourceUrl);
            } else {
                ReadContext ctx = JsonPath.parse(text);
                collection = getCollectionDetail(collection, ctx, rule, sourceUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collection;
    }

    public static Collection getCollectionDetail(Collection collection, Object source, Rule rule, String sourceUrl) throws Exception {

        String idCode = parseSingleProperty(source, rule.idCode, sourceUrl, false);

        String title = parseSingleProperty(source, rule.title, sourceUrl, false);

        String uploader = parseSingleProperty(source, rule.uploader, sourceUrl, false);

        String cover = parseSingleProperty(source, rule.cover, sourceUrl, true);

        String category = parseSingleProperty(source, rule.category, sourceUrl, false);

        String datetime = parseSingleProperty(source, rule.datetime, sourceUrl, false);

        String description = parseSingleProperty(source, rule.description, sourceUrl, false);
        if(source instanceof Element){
            try{
                Element element = Jsoup.parse(description);
                element.select("iframe").remove();
                element.select("script").remove();
                description = element.toString();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        String ratingStr = parseSingleProperty(source, rule.rating, sourceUrl, false);

        float rating;

        if (ratingStr.matches("\\d+(.\\d+)?") && ratingStr.indexOf(".") > 0) {
            rating = Float.parseFloat(ratingStr);
        } else if (StringUtil.isNumeric(ratingStr)) {
            rating = Float.parseFloat(ratingStr);
        } else {
            String result = MathUtil.computeString(ratingStr);
            try {
                rating = result.contains("NaN") ? 0 : Float.parseFloat(result);
            } catch (NumberFormatException e) {
                rating = Math.min(ratingStr.replace(" ", "").length(), 5);
            }
        }

        List temp;

        List<Tag> tags = new ArrayList<>();
        if (rule.tagRule != null && rule.tagRule.item != null) {
            if (source instanceof Element)
                temp = ((Element) source).select(rule.tagRule.item.selector);
            else if (source instanceof ReadContext)
                temp = ((ReadContext) source).read(rule.tagRule.item.path, new TypeRef<List<ReadContext>>() {
                });
            else
                return collection;
            for (Object element : temp) {
                if (rule.tagRule.item.regex != null) {
                    Pattern pattern = Pattern.compile(rule.tagRule.item.regex);
                    Matcher matcher = pattern.matcher(element.toString());
                    if (!matcher.find()) {
                        continue;
                    }
                }
                String tagTitle = parseSingleProperty(element, rule.tagRule.title, sourceUrl, false);
                String tagUrl = parseSingleProperty(element, rule.tagRule.url, sourceUrl, true);
                if (TextUtils.isEmpty(tagUrl))
                    tagUrl = null;
                tags.add(new Tag(tags.size() + 1, tagTitle, tagUrl));
            }
        } else if (rule.tags != null) {
            List<String> tagStrs = parseSinglePropertyMatchAll(source, rule.tags, sourceUrl, false);
            for (String tagStr : tagStrs) {
                if (!TextUtils.isEmpty(tagStr))
                    tags.add(new Tag(tags.size() + 1, tagStr));
            }
        }

        List<Picture> pictures = new ArrayList<>();

        Selector pictureId = null, pictureItem = null, pictureThumbnail = null, pictureUrl = null, pictureHighRes = null;
        if (rule.pictureRule != null && rule.pictureRule.url != null && rule.pictureRule.thumbnail != null) {
            pictureId = rule.pictureRule.id;
            pictureItem = rule.pictureRule.item;
            pictureThumbnail = rule.pictureRule.thumbnail;
            pictureUrl = rule.pictureRule.url;
            pictureHighRes = rule.pictureRule.highRes;
        } else if (rule.pictureUrl != null && rule.pictureThumbnail != null) {
            pictureId = rule.pictureId;
            pictureItem = rule.item;
            pictureThumbnail = rule.pictureThumbnail;
            pictureUrl = rule.pictureUrl;
            pictureHighRes = rule.pictureHighRes;
        }

        if (pictureUrl != null && pictureThumbnail != null) {
            if (pictureItem != null) {
                if (source instanceof Element)
                    temp = ((Element) source).select(pictureItem.selector);
                else if (source instanceof ReadContext)
                    temp = ((ReadContext) source).read(pictureItem.path, new TypeRef<List<ReadContext>>() {});
                else
                    return collection;
                for (Object element : temp) {
                    if (pictureItem.regex != null) {
                        Pattern pattern = Pattern.compile(pictureItem.regex);
                        Matcher matcher = pattern.matcher(element.toString());
                        if (!matcher.find()) {
                            continue;
                        }
                    }
                    String pId = parseSingleProperty(element, pictureId, sourceUrl, false);
                    int pid;
                    try {
                        pid = Integer.parseInt(pId);
                    } catch (Exception e) {
                        pid = 0;
                    }
                    pid = (pid != 0) ? pid : (pictures.size() > 0) ? pictures.get(pictures.size() - 1).pid + 1 : pictures.size() + 1;
                    String pUrl = parseSingleProperty(element, pictureUrl, sourceUrl, true);
                    String PHighRes = parseSingleProperty(element, pictureHighRes, sourceUrl, true);
                    String pThumbnail = parseSingleProperty(element, pictureThumbnail, sourceUrl, true);
                    pictures.add(new Picture(pid, pUrl, pThumbnail, PHighRes, sourceUrl));
                }
            } else {
                List<String> pids = parseSinglePropertyMatchAll(source, pictureId, sourceUrl, false);
                List<String> urls = parseSinglePropertyMatchAll(source, pictureUrl, sourceUrl, true);
                List<String> thumbnails = parseSinglePropertyMatchAll(source, pictureThumbnail, sourceUrl, true);
                List<String> highReses = parseSinglePropertyMatchAll(source, pictureHighRes, sourceUrl, true);
                for (int i = 0; i < urls.size(); i++) {
                    String pId = (i < pids.size()) ? pids.get(i) : "";
                    int pid;
                    try {
                        pid = Integer.parseInt(pId);
                    } catch (Exception e) {
                        pid = 0;
                    }
                    pid = (pid != 0) ? pid : (pictures.size() > 0) ? pictures.get(pictures.size() - 1).pid + 1 : pictures.size() + 1;
                    String url = urls.get(i);
                    String thumbnail = (i < thumbnails.size()) ? thumbnails.get(i) : "";
                    String highRes = (i < highReses.size()) ? highReses.get(i) : "";
                    pictures.add(new Picture(pid, url, thumbnail, highRes, sourceUrl));
                }
            }
        }


        List<Video> videos = new ArrayList<>();
        if (rule.videoRule != null && rule.videoRule.item != null) {
            if (source instanceof Element)
                temp = ((Element) source).select(rule.videoRule.item.selector);
            else if (source instanceof ReadContext)
                temp = ((ReadContext) source).read(rule.videoRule.item.path, new TypeRef<List<ReadContext>>() {});
            else
                return collection;
            for (Object element : temp) {
                if (rule.videoRule.item.regex != null) {
                    Pattern pattern = Pattern.compile(rule.videoRule.item.regex);
                    Matcher matcher = pattern.matcher(element.toString());
                    if (!matcher.find()) {
                        continue;
                    }
                }
                String vId = parseSingleProperty(element, rule.videoRule.id, sourceUrl, false);
                int vid;
                try {
                    vid = Integer.parseInt(vId);
                } catch (Exception e) {
                    vid = 0;
                }
                vid = (vid != 0) ? vid : (videos.size() > 0) ? videos.get(videos.size() - 1).vid + 1 : videos.size() + 1;
                String vThumbnail = parseSingleProperty(element, rule.videoRule.thumbnail, sourceUrl, true);
                if(TextUtils.isEmpty(vThumbnail))
                    vThumbnail = cover;
                String vContent = parseSingleProperty(element, rule.videoRule.content, sourceUrl, true);
                videos.add(new Video(vid, vThumbnail, vContent));
            }
        }

        Selector commentItem = null, commentAvatar = null, commentAuthor = null, commentDatetime = null, commentContent = null;
        List<Comment> comments = new ArrayList<>();
        if (rule.commentRule != null && rule.commentRule.item != null && rule.commentRule.content != null) {
            commentItem = rule.commentRule.item;
            commentAvatar = rule.commentRule.avatar;
            commentAuthor = rule.commentRule.author;
            commentDatetime = rule.commentRule.datetime;
            commentContent = rule.commentRule.content;
        } else if (rule.commentItem != null && rule.commentContent != null) {
            commentItem = rule.commentItem;
            commentAvatar = rule.commentAvatar;
            commentAuthor = rule.commentAuthor;
            commentDatetime = rule.commentDatetime;
            commentContent = rule.commentContent;
        }
        if (commentItem != null && commentContent != null) {
            if (source instanceof Element)
                temp = ((Element) source).select(commentItem.selector);
            else if (source instanceof ReadContext)
                temp = ((ReadContext) source).read(commentItem.path, new TypeRef<List<ReadContext>>() {
                });
            else
                return collection;
            for (Object element : temp) {
                if (commentItem.regex != null) {
                    Pattern pattern = Pattern.compile(commentItem.regex);
                    Matcher matcher = pattern.matcher(element.toString());
                    if (!matcher.find()) {
                        continue;
                    }
                }
                String cAvatar = parseSingleProperty(element, commentAvatar, sourceUrl, false);
                String cAuthor = parseSingleProperty(element, commentAuthor, sourceUrl, false);
                String cDatetime = parseSingleProperty(element, commentDatetime, sourceUrl, false);
                String cContent = parseSingleProperty(element, commentContent, sourceUrl, false);
                comments.add(new Comment(comments.size() + 1, cAvatar, cAuthor, cDatetime, cContent, sourceUrl));
            }
        }

        if (!TextUtils.isEmpty(idCode))
            collection.idCode = idCode;
        if (!TextUtils.isEmpty(title))
            collection.title = title;
        if (!TextUtils.isEmpty(uploader))
            collection.uploader = uploader;
        if (!TextUtils.isEmpty(cover))
            collection.cover = cover;
        if (!TextUtils.isEmpty(category))
            collection.category = category;
        if (!TextUtils.isEmpty(datetime))
            collection.datetime = datetime;
        if (!TextUtils.isEmpty(description))
            collection.description = description;
        if (rating > 0)
            collection.rating = rating;
        if (!TextUtils.isEmpty(sourceUrl))
            collection.referer = sourceUrl;
        if (tags != null && tags.size() > 0)
            collection.tags = tags;
        if (pictures != null && pictures.size() > 0)
            collection.pictures = pictures;
        if (videos != null && videos.size() > 0)
            collection.videos = videos;
        if (comments != null && comments.size() > 0)
            collection.comments = comments;
        return collection;
    }

    public static String parseSingleProperty(Object source, Selector selector, String sourceUrl, boolean isUrl) throws Exception {
        List<String> props = parseSinglePropertyMatchAll(source, selector, sourceUrl, isUrl);
        return (props.size() > 0) ? props.get(0) : "";
    }

    public static List<String> parseSinglePropertyMatchAll(Object source, Selector selector, String sourceUrl, boolean isUrl) throws Exception {
        List<String> props = new ArrayList<>();

        if (selector != null) {
            String prop;
            if (source instanceof Element) {
                Elements temp = ("this".equals(selector.selector)) ? new Elements((Element) source) : ((Element) source).select(selector.selector);
                if (temp != null) {
                    for (Element elem : temp) {
                        if ("attr".equals(selector.fun)) {
                            prop = elem.attr(selector.param);
                        } else if ("html".equals(selector.fun)) {
                            prop = elem.html();
                        } else {
                            prop = elem.toString();
                        }
                        props = getPropertyAfterRegex(props, prop, selector, sourceUrl, isUrl);
                    }
                }
            } else if (source instanceof ReadContext) {
                List<ReadContext> temp = new ArrayList<>();
                if ("this".equals(selector.path))
                    temp.add((ReadContext) source);
                else
                    temp = ((ReadContext) source).read(selector.path);

                if (temp != null) {
                    for (ReadContext item : temp) {
                        prop = item.toString();
                        props = getPropertyAfterRegex(props, prop, selector, sourceUrl, isUrl);
                    }
                }
            }
        }
        if (props.size() == 0)
            props.add("");
        return props;
    }

    public static List<String> getPropertyAfterRegex(List<String> props, String prop, Selector selector, String sourceUrl, boolean isUrl) {
        if (selector.regex != null) {
            Pattern pattern = Pattern.compile(selector.regex, DOTALL);
            Matcher matcher = pattern.matcher(prop);
            while (matcher.find() && matcher.groupCount() >= 1) {
                if (selector.replacement != null) {
                    prop = selector.replacement;
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String replace = matcher.group(i);
                        prop = prop.replaceAll("\\$" + i, (replace != null) ? replace : "");
                    }
                } else {
                    prop = matcher.group(1);
                }
                if (isUrl) {
                    if (TextUtils.isEmpty(prop))
                        break;
                    prop = RegexValidateUtil.getAbsoluteUrlFromRelative(prop, sourceUrl);
                }
                props.add(StringEscapeUtils.unescapeHtml(prop.trim()));
            }
        } else {
            if (isUrl) {
                if (!TextUtils.isEmpty(prop)) {
                    prop = RegexValidateUtil.getAbsoluteUrlFromRelative(prop, sourceUrl);
                    props.add(StringEscapeUtils.unescapeHtml(prop.trim()));
                }
            } else
                props.add(StringEscapeUtils.unescapeHtml(prop.trim()));
        }
        return props;
    }

    public static String getPictureUrl(String html, Selector selector, String sourceUrl) {
        try {
            Document doc = Jsoup.parse(html);
            return parseSingleProperty(doc, selector, sourceUrl, true);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
