package ml.puredark.hviewer.helpers;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Tag;

import static android.R.attr.rating;
import static android.R.attr.tag;
import static ml.puredark.hviewer.HViewerApplication.temp;
import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

/**
 * Created by PureDark on 2016/8/9.
 */

public class RuleParser {

    public static List<Collection> getCollections(String html, Rule rule) {
        List<Collection> collections = new ArrayList<>();
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
            collection = getCollectionDetail(collection, element, rule);

            collections.add(collection);
        }
        return collections;
    }

    public static Collection getCollectionDetail(Collection collection, String html, Rule rule) {
        Document element = Jsoup.parse(html);
        return getCollectionDetail(collection, element, rule);
    }

    public static Collection getCollectionDetail(Collection collection, Element element, Rule rule) {
        Elements temp;

        String url = "";
        if(rule.url!=null) {
            temp = element.select(rule.url.selector);
            if ("attr".equals(rule.url.fun)) {
                url = temp.attr(rule.url.param);
            } else if ("html".equals(rule.url.fun)) {
                url = temp.html();
            }
            if (rule.url.regex != null) {
                Pattern pattern = Pattern.compile(rule.url.regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    url = matcher.group();
                }
            }
        }

        String title = "";
        if(rule.title!=null) {
            temp = element.select(rule.title.selector);
            if ("attr".equals(rule.title.fun)) {
                title = temp.attr(rule.title.param);
            } else if ("html".equals(rule.title.fun)) {
                title = temp.html();
            }
            if (rule.title.regex != null && url != null) {
                Pattern pattern = Pattern.compile(rule.title.regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    title = matcher.group();
                }
            }
        }

        String uploader = "";
        if(rule.uploader!=null) {
            temp = element.select(rule.uploader.selector);
            if ("attr".equals(rule.uploader.fun)) {
                uploader = temp.attr(rule.uploader.param);
            } else if ("html".equals(rule.uploader.fun)) {
                uploader = temp.html();
            }
            if (rule.uploader.regex != null && uploader != null) {
                Pattern pattern = Pattern.compile(rule.uploader.regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(uploader);
                if (matcher.find()) {
                    uploader = matcher.group();
                }
            }
        }

        String cover = "";
        if(rule.cover!=null) {
            temp = element.select(rule.cover.selector);
            if ("attr".equals(rule.cover.fun)) {
                cover = temp.attr(rule.cover.param);
            } else if ("html".equals(rule.cover.fun)) {
                cover = temp.html();
            }
            if (rule.cover.regex != null && cover != null) {
                Pattern pattern = Pattern.compile(rule.cover.regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(cover);
                if (matcher.find()) {
                    cover = matcher.group();
                }
            }
        }

        String category = "";
        if(rule.category!=null) {
            temp = element.select(rule.category.selector);
            if ("attr".equals(rule.category.fun)) {
                category = temp.attr(rule.category.param);
            } else if ("html".equals(rule.category.fun)) {
                category = temp.html();
            }
            if (rule.category.regex != null && category != null) {
                Pattern pattern = Pattern.compile(rule.category.regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(category);
                if (matcher.find()) {
                    category = matcher.group();
                }
            }
        }

        String datetime = "";
        if(rule.datetime!=null) {
            temp = element.select(rule.datetime.selector);
            if ("attr".equals(rule.datetime.fun)) {
                datetime = temp.attr(rule.datetime.param);
            } else if ("html".equals(rule.datetime.fun)) {
                datetime = temp.html();
            }
            if (rule.datetime.regex != null && datetime != null) {
                Pattern pattern = Pattern.compile(rule.datetime.regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(datetime);
                if (matcher.find()) {
                    datetime = matcher.group();
                }
            }
        }

        String ratingStr = "";
        if(rule.rating!=null) {
            temp = element.select(rule.rating.selector);
            if ("attr".equals(rule.rating.fun)) {
                ratingStr = temp.attr(rule.rating.param);
            } else if ("html".equals(rule.rating.fun)) {
                ratingStr = temp.html();
            }
            if (rule.rating.regex != null && ratingStr != null) {
                Pattern pattern = Pattern.compile(rule.rating.regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(ratingStr);
                if (matcher.find()) {
                    ratingStr = matcher.group();
                }
            }
        }

        float rating = 0;
        if (ratingStr.matches("\\d+(.\\d+)?") && ratingStr.indexOf(".") > 0) {
            rating = Float.parseFloat(ratingStr);
        } else {
            rating = Math.min(ratingStr.replace(" ", "").length(), 5);
        }


        List<Tag> tags = new ArrayList<>();
        if (rule.tags != null) {
            temp = element.select(rule.tags.selector);
            for (Element tagElement : temp) {
                String tagStr;
                if ("attr".equals(rule.tags.fun)) {
                    tagStr = tagElement.attr(rule.tags.param);
                } else if ("html".equals(rule.tags.fun)) {
                    tagStr = temp.html();
                } else {
                    continue;
                }

                if (rule.tags.regex != null && tagStr != null) {
                    Pattern pattern = Pattern.compile(rule.tags.regex, Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(tagStr);
                    while (matcher.find()) {
                        tags.add(new Tag(tags.size() + 1, matcher.group()));
                    }
                } else {
                    tags.add(new Tag(tags.size() + 1, tagStr));
                }
            }
        }

        List<Picture> pictures = new ArrayList<>();
        if (rule.pictures != null) {

            temp = element.select(rule.pictures.selector);
            for (Element pictureElement : temp) {
                String pictureStr;
                if ("attr".equals(rule.pictures.fun)) {
                    pictureStr = pictureElement.attr(rule.pictures.param);
                } else if ("html".equals(rule.pictures.fun)) {
                    pictureStr = temp.html();
                } else {
                    pictureStr = pictureElement.toString();
                }
                Log.d("RuleParser", "pictureStr:" + pictureStr);

                if (rule.pictures.regex != null && pictureStr != null) {
                    Log.d("RuleParser", "pictures.regex:" + rule.pictures.regex);
                    Pattern pattern = Pattern.compile(rule.pictures.regex, Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(pictureStr);
                    while (matcher.find()) {
                        Log.d("RuleParser", "matcher.groupCount():" + matcher.groupCount());
                        pictures.add(new Picture(pictures.size() + 1, matcher.group(1), matcher.group(2)));
                    }
                } else {
                    pictures.add(new Picture(pictures.size() + 1, pictureStr, ""));
                }
            }
        }

        if (url != null && !url.equals(""))
            collection.url = url;
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

    public static String getPicture(String html, Selector selector) {
        return "";
    }

}
