package ml.puredark.hviewer.core;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import ml.puredark.hviewer.helpers.Logger;
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
        Pattern pattern3 = Pattern.compile("\\{(json):(.*)\\}", DOTALL);
        Matcher matcher3 = pattern3.matcher(url);
        while (matcher3.find()) {
            map.put(matcher3.group(1), matcher3.group(2));
        }
        return map;
    }

    public static String parseUrl(String url, int page, String idCode, String keyword, Object[] objs){
        return parseUrl(url, page, idCode, keyword, objs, false);
    }

    public static String parseUrl(String url, int page, String idCode, String keyword, Object[] objs, boolean getJsonParams) {
        Map<String, String> matchResult = RuleParser.parseUrl(url);
        if(getJsonParams && !matchResult.containsKey("json"))
            return null;
        String result = (getJsonParams) ? matchResult.get("json") : url;
        String pageStr = matchResult.get("page");
        int startPage = 0;
        int pageStep = 1;
        try {
            if ("minid".equals(pageStr) && objs != null) {
                int min = Integer.MAX_VALUE;
                for (Object obj : objs) {
                    if (obj instanceof Collection)
                        min = Math.min(min, Integer.parseInt(((Collection) obj).idCode.replaceAll("[^0-9]", "")));
                    else if (obj instanceof Picture)
                        min = Math.min(min, ((Picture) obj).pid);
                }
                page = min;
            } else if ("maxid".equals(pageStr) && objs != null) {
                int max = Integer.MIN_VALUE;
                for (Object obj : objs) {
                    if (obj instanceof Collection)
                        max = Math.max(max, Integer.parseInt(((Collection) obj).idCode.replaceAll("[^0-9]", "")));
                    else if (obj instanceof Picture)
                        max = Math.max(max, ((Picture) obj).pid);
                }
                page = max;
            } else if (pageStr != null) {
                String[] pageStrs = pageStr.split(":");
                if (pageStrs.length > 1) {
                    pageStep = Integer.parseInt(pageStrs[1]);
                    startPage = Integer.parseInt(pageStrs[0]);
                } else {
                    pageStep = 1;
                    startPage = Integer.parseInt(pageStr);
                }
            }
        } catch (NumberFormatException e) {
        }
        if (page < startPage)
            page = startPage;
        int realPage = page + (page - startPage) * (pageStep - 1);
        result = result.replaceAll("\\{pageStr:(.*?\\{.*?\\}.*?)\\}", (realPage == startPage) ? "" : matchResult.get("pageStr"))
                .replaceAll("\\{page:.*?\\}", "" + realPage)
                .replaceAll("\\{keyword:.*?\\}", keyword)
                .replaceAll("\\{idCode:\\}", idCode);
        if (matchResult.containsKey("date")) {
            String dateStr = matchResult.get("date");
            int index = dateStr.lastIndexOf(':');
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat;
            try {
                if (index > 0) {
                    String firstParam = dateStr.substring(0, index);
                    String lastParam = dateStr.substring(index + 1);
                    int offset = Integer.parseInt(lastParam);
                    calendar.add(Calendar.DAY_OF_MONTH, offset);
                    dateFormat = new SimpleDateFormat(firstParam);
                } else {
                    dateFormat = new SimpleDateFormat(dateStr);
                }
            } catch (Exception e) {
                dateFormat = new SimpleDateFormat(dateStr);
            }
            String currDate = dateFormat.format(calendar.getTime());
            result = result.replaceAll("\\{date:.*?\\}", currDate);
        }
        if (matchResult.containsKey("time")) {
            String timeStr = matchResult.get("time");
            int index = timeStr.lastIndexOf(':');
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat;
            try {
                if (index > 0) {
                    String firstParam = timeStr.substring(0, index);
                    String lastParam = timeStr.substring(index + 1);
                    int offset = Integer.parseInt(lastParam);
                    dateFormat = new SimpleDateFormat(firstParam);
                    calendar.add(Calendar.SECOND, offset);
                } else {
                    dateFormat = new SimpleDateFormat(timeStr);
                }
            } catch (NumberFormatException e) {
                dateFormat = new SimpleDateFormat(timeStr);
            }
            String currTime = dateFormat.format(calendar.getTime());
            result = result.replaceAll("\\{time:.*?\\}", currTime);
        }
        return result;
    }

    public static boolean isJson(String string) {
        if (string == null)
            return false;
        string = string.trim();
        return string.startsWith("{") || string.startsWith("[");
    }

    public static JsonArray getJsonArray(ReadContext ctx, String jsonPaths) {
        JsonArray items = new JsonArray();
        String[] paths = jsonPaths.split(",");
        for (int i = 0; i < paths.length; i++) {
            JsonElement element;
            try {
                element = ctx.read(paths[i], JsonElement.class);
            } catch (Exception e) {
                Logger.d("RuleParser", "path[" + i + "]:" + paths[i]);
                //e.printStackTrace();
                try {
                    if (paths.length > i + 1) {
                        element = ctx.read(paths[i] + paths[i + 1], JsonElement.class);
                        i++;
                    } else
                        continue;
                } catch (Exception e1) {
                    //e.printStackTrace();
                    continue;
                }
            }
            if (element == null || element.isJsonNull())
                continue;
            if (element instanceof JsonArray) {
                items.addAll(element.getAsJsonArray());
            } else {
                items.add(element);
            }
        }
        return items;
    }

    public static List<Collection> getCollections(List<Collection> collections, String text, Rule rule, String sourceUrl) {
        return getCollections(collections, text, rule, sourceUrl, false);
    }

    public static List<Collection> getCollections(List<Collection> collections, String text, Rule rule, String sourceUrl, boolean noRegex) {
        try {
            Iterable items;
            if (!isJson(text)) {
                Document doc = Jsoup.parse(text);
                items = doc.select(rule.item.selector);
                for (Object item : items) {
                    String itemStr;
                    if (item instanceof Element) {
                        if ("attr".equals(rule.item.fun))
                            itemStr = ((Element) item).attr(rule.title.param);
                        else if ("html".equals(rule.item.fun))
                            itemStr = ((Element) item).html();
                        else if ("text".equals(rule.item.fun))
                            itemStr = ((Element) item).text();
                        else
                            itemStr = item.toString();
                    } else
                        continue;
                    if (!noRegex && rule.item.regex != null) {
                        Pattern pattern = Pattern.compile(rule.item.regex);
                        Matcher matcher = pattern.matcher(itemStr);
                        Logger.d("RuleParser", "beforeMatch");
                        if (!matcher.find()) {
                            continue;
                        } else if (matcher.groupCount() >= 1) {
                            Logger.d("RuleParser", "matcher.groupCount() >= 1");
                            if (rule.item.replacement != null) {
                                itemStr = rule.item.replacement;
                                for (int i = 1; i <= matcher.groupCount(); i++) {
                                    String replace = matcher.group(i);
                                    itemStr = itemStr.replaceAll("\\$" + i, (replace != null) ? replace : "");
                                }
                            } else {
                                itemStr = matcher.group(1);
                            }
                        }
                    }
                    if (rule.item.path != null && isJson(itemStr)) {
                        Logger.d("RuleParser", "isJson : true");
                        collections = getCollections(collections, itemStr, rule, sourceUrl, true);
                    } else {
                        Collection collection = new Collection(collections.size() + 1);
                        collection = getCollectionDetail(collection, item, rule, sourceUrl);
                        collections.add(collection);
                    }
                }
            } else {
                ReadContext ctx = JsonPath.parse(text);
                items = getJsonArray(ctx, rule.item.path);
                Logger.d("RuleParser", items.toString());
                for (Object item : items) {
                    String itemStr;
                    if (item instanceof JsonElement)
                        itemStr = item.toString();
                    else
                        continue;
                    if (!noRegex && rule.item.regex != null) {
                        Pattern pattern = Pattern.compile(rule.item.regex);
                        Matcher matcher = pattern.matcher(itemStr);
                        if (!matcher.find()) {
                            continue;
                        } else if (matcher.groupCount() >= 1) {
                            if (rule.item.replacement != null) {
                                itemStr = rule.item.replacement;
                                for (int i = 1; i <= matcher.groupCount(); i++) {
                                    String replace = matcher.group(i);
                                    itemStr = itemStr.replaceAll("\\$" + i, (replace != null) ? replace : "");
                                }
                            } else {
                                itemStr = matcher.group(1);
                            }
                        }
                    }
                    if (rule.item.selector != null && !isJson(itemStr)) {
                        Logger.d("RuleParser", "isJson : false");
                        collections = getCollections(collections, itemStr, rule, sourceUrl, true);
                    } else {
                        Collection collection = new Collection(collections.size() + 1);
                        collection = getCollectionDetail(collection, item, rule, sourceUrl);
                        collections.add(collection);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collections;
    }

    public static Collection getCollectionDetail(Collection collection, String text, Rule rule, String sourceUrl) {
        if (rule == null)
            return collection;
        try {
            if (rule.item != null && rule.pictureRule != null && rule.pictureRule.item != null) {
                List<Collection> collections = new ArrayList<>();
                Collection newCollection = getCollections(collections, text, rule, sourceUrl).get(0);
                collection.replace(newCollection);
            } else {
                if (!isJson(text)) {
                    Document element = Jsoup.parse(text);
                    collection = getCollectionDetail(collection, element, rule, sourceUrl);
                } else {
                    JsonElement elemet = new JsonParser().parse(text);
                    collection = getCollectionDetail(collection, elemet, rule, sourceUrl);
                }
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

        if (source instanceof Element) {
            try {
                Element element = Jsoup.parse(description);
                element.select("iframe").remove();
                element.select("script").remove();
                description = element.select("body").html();
            } catch (Exception e) {
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

        Iterable temp;
        JsonParser jsonParser = new JsonParser();

        List<Tag> tags = new ArrayList<>();
        if (rule.tagRule != null && rule.tagRule.item != null) {
            temp = parseItemMatchAll(source, rule.tagRule.item, sourceUrl);
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
                temp = parseItemMatchAll(source, pictureItem, sourceUrl);
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
            temp = parseItemMatchAll(source, rule.videoRule.item, sourceUrl);
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
                if (TextUtils.isEmpty(vThumbnail))
                    vThumbnail = (TextUtils.isEmpty(cover)) ? collection.cover : cover;
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
            temp = parseItemMatchAll(source, commentItem, sourceUrl);
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

    public static List<Object> parseItemMatchAll(Object source, Selector selector, String sourceUrl) throws Exception {
        List<Object> items = new ArrayList<>();

        if (selector != null) {
            String prop;
            if (source instanceof Element) {
                Elements temp = ("this".equals(selector.selector)) ? new Elements((Element) source) : ((Element) source).select(selector.selector);
                if (temp != null) {
                    boolean doJsonParse = !TextUtils.isEmpty(selector.path);
                    for (Element elem : temp) {
                        if(doJsonParse){
                            if ("attr".equals(selector.fun)) {
                                prop = elem.attr(selector.param);
                            } else if ("html".equals(selector.fun)) {
                                prop = elem.html();
                            } else if ("text".equals(selector.fun)) {
                                prop = elem.text();
                            } else {
                                prop = elem.toString();
                            }
                            List<String> props = getPropertyAfterRegex(new ArrayList<>(), prop, selector, sourceUrl, false);
                            for(String string : props){
                                ReadContext ctx = JsonPath.parse(string);
                                JsonArray jsonArray = getJsonArray(ctx, selector.path);
                                for(JsonElement jsonElem : jsonArray){
                                    items.add(jsonElem);
                                }
                            }
                        } else {
                            if (selector.regex != null) {
                                Pattern pattern = Pattern.compile(selector.regex);
                                Matcher matcher = pattern.matcher(elem.toString());
                                if (!matcher.find()) {
                                    continue;
                                }
                            }
                            items.add(elem);
                        }
                    }
                }
            } else if (source instanceof JsonElement) {
                ReadContext ctx = JsonPath.parse(source.toString());
                JsonArray temp = getJsonArray(ctx, selector.path);
                if (temp != null) {
                    boolean doDocument = !TextUtils.isEmpty(selector.selector);
                    for (JsonElement item : temp) {
                        if (doDocument) {
                            try {
                                if (item instanceof JsonPrimitive)
                                    prop = item.getAsString();
                                else
                                    continue;
                                Elements elements = ("this".equals(selector.selector)) ? new Elements(Jsoup.parse(prop)) : Jsoup.parse(prop).select(selector.selector);
                                items.addAll(elements);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else{
                            if (selector.regex != null) {
                                Pattern pattern = Pattern.compile(selector.regex);
                                Matcher matcher = pattern.matcher(item.toString());
                                if (!matcher.find()) {
                                    continue;
                                }
                            }
                            items.add(item);
                        }
                    }
                }
            }
        }
        return items;
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
                    boolean doJsonParse = !TextUtils.isEmpty(selector.path);
                    for (Element elem : temp) {
                        if ("attr".equals(selector.fun)) {
                            prop = elem.attr(selector.param);
                        } else if ("html".equals(selector.fun)) {
                            prop = elem.html();
                        } else if ("text".equals(selector.fun)) {
                            prop = elem.text();
                        } else {
                            prop = elem.toString();
                        }
                        if (doJsonParse)
                            props = getPropertyAfterRegex(props, prop, selector, sourceUrl, false);
                        else
                            props = getPropertyAfterRegex(props, prop, selector, sourceUrl, isUrl);
                    }
                    if (doJsonParse) {
                        try {
                            for (int i = 0; i < props.size(); i++) {
                                prop = props.get(i);
                                ReadContext ctx = JsonPath.parse(prop);
                                JsonArray tempArray = getJsonArray(ctx, selector.path);
                                if (tempArray.size() > 0) {
                                    JsonElement tempItem = tempArray.get(0);
                                    if (tempItem instanceof JsonPrimitive)
                                        prop = tempItem.getAsString();
                                    else
                                        prop = tempItem.toString();
                                    if (!TextUtils.isEmpty(prop)) {
                                        if (isUrl)
                                            prop = RegexValidateUtil.getAbsoluteUrlFromRelative(prop, sourceUrl);
                                        props.set(i, prop);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (source instanceof JsonElement) {
                ReadContext ctx = JsonPath.parse(source.toString());
                JsonArray temp = getJsonArray(ctx, selector.path);
                if (temp != null) {
                    boolean doDocument = !TextUtils.isEmpty(selector.selector);
                    for (JsonElement item : temp) {
                        if (item instanceof JsonPrimitive)
                            prop = item.getAsString();
                        else
                            prop = item.toString();
                        if (doDocument) {
                            try {
                                Elements element = ("this".equals(selector.selector)) ? new Elements(Jsoup.parse(prop)) : Jsoup.parse(prop).select(selector.selector);
                                if ("attr".equals(selector.fun)) {
                                    prop = element.attr(selector.param);
                                } else if ("html".equals(selector.fun)) {
                                    prop = element.html();
                                } else if ("text".equals(selector.fun)) {
                                    prop = element.text();
                                } else {
                                    prop = element.toString();
                                }
                                if (!TextUtils.isEmpty(prop))
                                    props = getPropertyAfterRegex(props, prop, selector, sourceUrl, isUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (!TextUtils.isEmpty(prop) && !"null".equals(prop.trim())) {
                            props = getPropertyAfterRegex(props, prop, selector, sourceUrl, isUrl);
                        }
                    }
                }
            }
        }
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
            if (isUrl && !TextUtils.isEmpty(prop)) {
                prop = RegexValidateUtil.getAbsoluteUrlFromRelative(prop, sourceUrl);
            }
            props.add(StringEscapeUtils.unescapeHtml(prop.trim()));
        }
        return props;
    }

    public static String getPictureUrl(String text, Selector selector, String sourceUrl) {
        try {
            if (!isJson(text)) {
                Document doc = Jsoup.parse(text);
                return parseSingleProperty(doc, selector, sourceUrl, true);
            } else {
                ReadContext ctx = JsonPath.parse(text);
                return parseSingleProperty(ctx, selector, sourceUrl, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static List<String> getVideoUrl(String html, String sourceUrl) {
        List<String> videoUrls = new ArrayList<>();
        try {
            Pattern p = Pattern.compile("https?[^\"'<>]*?[^\"'<>]+?\\.(?:mp4|flv)[^\"'<>]*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(html);
            while (matcher.find()) {
                String videoUrl = matcher.group();
                if (TextUtils.isEmpty(videoUrl))
                    continue;
                videoUrl = RegexValidateUtil.getAbsoluteUrlFromRelative(videoUrl, sourceUrl);
                videoUrls.add(videoUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoUrls;
    }

}
