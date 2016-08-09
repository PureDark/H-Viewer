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
import ml.puredark.hviewer.beans.Tag;

import static android.R.attr.tag;
import static ml.puredark.hviewer.HViewerApplication.temp;

/**
 * Created by PureDark on 2016/8/9.
 */

public class RuleParser {

    public static List<Collection> getCollections(String html, Rule rule) {
        List<Collection> collections = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select(rule.item.selector);
        for (Element element : elements) {
            String itemStr = "";
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

            String title = "";
            Elements temp = element.select(rule.title.selector);
            if ("attr".equals(rule.title.fun)) {
                title = temp.attr(rule.title.param);
            } else if ("html".equals(rule.title.fun)) {
                title = temp.html();
            }
            if (rule.title.regex != null) {
                Pattern pattern = Pattern.compile(rule.title.regex);
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    title = matcher.group();
                }
            }

            String uploader = "";
            temp = element.select(rule.uploader.selector);
            if ("attr".equals(rule.uploader.fun)) {
                uploader = temp.attr(rule.uploader.param);
            } else if ("html".equals(rule.uploader.fun)) {
                uploader = temp.html();
            }
            if (rule.uploader.regex != null) {
                Pattern pattern = Pattern.compile(rule.uploader.regex);
                Matcher matcher = pattern.matcher(uploader);
                if (matcher.find()) {
                    uploader = matcher.group();
                }
            }

            String cover = "";
            temp = element.select(rule.cover.selector);
            if ("attr".equals(rule.cover.fun)) {
                cover = temp.attr(rule.cover.param);
            } else if ("html".equals(rule.cover.fun)) {
                cover = temp.html();
            }
            if (rule.cover.regex != null) {
                Pattern pattern = Pattern.compile(rule.cover.regex);
                Matcher matcher = pattern.matcher(cover);
                if (matcher.find()) {
                    cover = matcher.group();
                }
            }

            String category = "";
            temp = element.select(rule.category.selector);
            if ("attr".equals(rule.category.fun)) {
                category = temp.attr(rule.category.param);
            } else if ("html".equals(rule.category.fun)) {
                category = temp.html();
            }
            if (rule.category.regex != null) {
                Pattern pattern = Pattern.compile(rule.category.regex);
                Matcher matcher = pattern.matcher(category);
                if (matcher.find()) {
                    category = matcher.group();
                }
            }

            String datetime = "";
            temp = element.select(rule.datetime.selector);
            if ("attr".equals(rule.datetime.fun)) {
                datetime = temp.attr(rule.datetime.param);
            } else if ("html".equals(rule.datetime.fun)) {
                datetime = temp.html();
            }
            if (rule.datetime.regex != null) {
                Pattern pattern = Pattern.compile(rule.datetime.regex);
                Matcher matcher = pattern.matcher(datetime);
                if (matcher.find()) {
                    datetime = matcher.group();
                }
            }

            String ratingStr = "";
            temp = element.select(rule.rating.selector);
            if ("attr".equals(rule.rating.fun)) {
                ratingStr = temp.attr(rule.rating.param);
            } else if ("html".equals(rule.rating.fun)) {
                ratingStr = temp.html();
            }
            if (rule.rating.regex != null) {
                Pattern pattern = Pattern.compile(rule.rating.regex);
                Matcher matcher = pattern.matcher(ratingStr);
                if (matcher.find()) {
                    ratingStr = matcher.group();
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

                    if (rule.tags.regex != null) {
                        Pattern pattern = Pattern.compile(rule.tags.regex);
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

            Collection collection = new Collection(collections.size() + 1, title, uploader, cover, category, datetime, rating, tags, pictures);
            collections.add(collection);
        }
        return collections;
    }

}
