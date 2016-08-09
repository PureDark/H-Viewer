package ml.puredark.hviewer.helpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Tag;

import static android.R.attr.tag;

/**
 * Created by PureDark on 2016/8/9.
 */

public class RuleParser {

    public static List<Collection> getCollections(String html, Rule rule) {
        List<Collection> collections = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select(rule.item.selector);
        for (Element element : elements) {
            int cid = collections.size() + 1;

            String title = "";
            Elements temp = element.select(rule.title.selector);
            if ("attr".equals(rule.title.fun)) {
                title = temp.attr(rule.title.param);
            } else if ("html".equals(rule.title.fun)) {
                title = temp.html();
            }

            String uploader = "";
            temp = element.select(rule.uploader.selector);
            if ("attr".equals(rule.uploader.fun)) {
                uploader = temp.attr(rule.uploader.param);
            } else if ("html".equals(rule.uploader.fun)) {
                uploader = temp.html();
            }

            String cover = "";
            temp = element.select(rule.cover.selector);
            if ("attr".equals(rule.cover.fun)) {
                cover = temp.attr(rule.cover.param);
            } else if ("html".equals(rule.cover.fun)) {
                cover = temp.html();
            }

            String category = "";
            temp = element.select(rule.category.selector);
            if ("attr".equals(rule.category.fun)) {
                category = temp.attr(rule.category.param);
            } else if ("html".equals(rule.category.fun)) {
                category = temp.html();
            }

            String datetime = "";
            temp = element.select(rule.datetime.selector);
            if ("attr".equals(rule.datetime.fun)) {
                datetime = temp.attr(rule.datetime.param);
            } else if ("html".equals(rule.datetime.fun)) {
                datetime = temp.html();
            }

            String ratingStr = "";
            temp = element.select(rule.rating.selector);
            if ("attr".equals(rule.rating.fun)) {
                ratingStr = temp.attr(rule.rating.param);
            } else if ("html".equals(rule.rating.fun)) {
                ratingStr = temp.html();
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
                    if ("split".equals(rule.tags.fun)) {
                        String[] tagsStr = tagElement.html().split(rule.tags.param);
                        for (String tagStr : tagsStr) {
                            tags.add(new Tag(tags.size() + 1, tagStr));
                        }
                    } else {
                        String tagStr = tagElement.html();
                        tags.add(new Tag(tags.size() + 1, tagStr));
                    }
                }
            }
            List<Picture> pictures = new ArrayList<>();

            Collection collection = new Collection(cid, title, uploader, cover, category, datetime, rating, tags, pictures);
            collections.add(collection);
        }
        return collections;
    }

}
