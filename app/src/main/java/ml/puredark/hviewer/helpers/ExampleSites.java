package ml.puredark.hviewer.helpers;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;

import static ml.puredark.hviewer.R.color.brown;

/**
 * Created by PureDark on 2016/9/21.
 */

public class ExampleSites {

    public static List<Site> get(){

        List<Site> sites = new ArrayList<>();

        // Lofi.E-hentai
        Rule indexRule = new Rule();
        indexRule.item = new Selector("#ig .ig", null, null, null, null);
        indexRule.idCode = new Selector("td.ii a", "attr", "href", "/g/(.*)", null);
        indexRule.title = new Selector("table.it tr:eq(0) a", "html", null, null, null);
        indexRule.uploader = new Selector("table.it tr:eq(1) td:eq(1)", "html", null, "(by .*)", null);
        indexRule.cover = new Selector("td.ii img", "attr", "src", null, null);
        indexRule.category = new Selector("table.it tr:eq(2) td:eq(1)", "html", null, null, null);
        indexRule.datetime = new Selector("table.it tr:eq(1) td:eq(1)", "html", null, "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})", null);
        indexRule.rating = new Selector("table.it tr:eq(4) td:eq(1)", "html", null, null, null);
        indexRule.tags = new Selector("table.it tr:eq(3) td:eq(1)", "html", null, "([a-zA-Z0-9 -]+)", null);

        Rule galleryRule = new Rule();
        galleryRule.item = new Selector("#gh .gi", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a img", "attr", "src", null, null);

        Rule extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#sm", "attr", "src", null, null);

        //新设计中即将取消PicUrlSelector，使用singlePageBigPicture这个flag和extraRule来兼容大图是单页浏览的站点
        //Selector pic = new Selector("img#sm", "attr", "src", null, null);

        sites.add(new Site(1, "Lofi.E-hentai",
                "http://lofi.e-hentai.org/?page={page:0}",
                "http://lofi.e-hentai.org/g/{idCode:}/{page:0}",
                "http://lofi.e-hentai.org/?f_search={keyword:}&page={page:0}",
                "https://forums.e-hentai.org/index.php?act=Login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE));

        // G.E-hentai
        indexRule = new Rule();
        indexRule.item = new Selector("table.itg tr.gtr0,tr.gtr1", null, null, null, null);
        indexRule.idCode = new Selector("td.itd div div.it5 a", "attr", "href", "/g/(.*)", null);
        indexRule.title = new Selector("td.itd div div.it5 a", "html", null, null, null);
        indexRule.uploader = new Selector("td.itu div a", "html", null, null, null);
        indexRule.cover = new Selector("td.itd div div.it2", "html", null, "(//|inits?~)(.*?org)[~/]([^~]*\\.jpg)[~\"]", "http://$2/$3");
        indexRule.category = new Selector("td.itdc a img", "attr", "alt", null, null);
        indexRule.datetime = new Selector("td.itd[style]", "html", null, null, null);
        indexRule.rating = new Selector("td.itd div div.it4 div", "attr", "style", "background-position:-?(\\d+)px -?(\\d+)px", "5-$1/16-($2-1)/40");

        galleryRule = new Rule();
        galleryRule.title = new Selector("h1#gj", "html", null, null, null);
        galleryRule.tags = new Selector("div#taglist table tr td:eq(1) div a", "html", null, null, null);
        galleryRule.item = new Selector("div.gdtl,div.gdtm", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("this", null, null, "(http://[^\"]*?\\.jpg)", null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("div.sni a img[style]", "attr", "src", null, null);
        //pic = new Selector("div.sni a img[style]", "attr", "src", null, null);

        sites.add(new Site(2, "G.E-hentai",
                "http://g.e-hentai.org/?page={page:0}",
                "http://g.e-hentai.org/g/{idCode:}/?p={page:0}",
                "http://g.e-hentai.org/?f_search={keyword:}&page={page:0}",
                "https://forums.e-hentai.org/index.php?act=Login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_REPEATED_THUMBNAIL + "|" + Site.FLAG_PRELOAD_GALLERY));

        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://g.e-hentai.org/?page={page:0}"));
        categories.add(new Category(2, "同人志", "http://g.e-hentai.org/doujinshi/{page:0}"));
        categories.add(new Category(3, "漫画", "http://g.e-hentai.org/manga/{page:0}"));
        categories.add(new Category(4, "同人CG", "http://g.e-hentai.org/artistcg/{page:0}"));
        categories.add(new Category(5, "游戏CG", "http://g.e-hentai.org/gamecg/{page:0}"));
        categories.add(new Category(6, "欧美", "http://g.e-hentai.org/western/{page:0}"));
        categories.add(new Category(7, "Non-H", "http://g.e-hentai.org/non-h/{page:0}"));
        categories.add(new Category(8, "图集", "http://g.e-hentai.org/imageset/{page:0}"));
        categories.add(new Category(9, "Cosplay", "http://g.e-hentai.org/cosplay/{page:0}"));
        categories.add(new Category(10, "亚洲AV", "http://g.e-hentai.org/asianporn/{page:0}"));
        categories.add(new Category(11, "MISC", "http://g.e-hentai.org/misc/{page:0}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // Ex-hentai
        indexRule = new Rule();
        indexRule.item = new Selector("table.itg tr.gtr0,tr.gtr1", null, null, null, null);
        indexRule.idCode = new Selector("td.itd div div.it5 a", "attr", "href", "/g/(.*)", null);
        indexRule.title = new Selector("td.itd div div.it5 a", "html", null, null, null);
        indexRule.uploader = new Selector("td.itu div a", "html", null, null, null);
        indexRule.cover = new Selector("td.itd div div.it2", "html", null, "(//|inits?~)(.*?org)[~/]([^~]*\\.jpg)[~\"]", "http://$2/$3");
        indexRule.category = new Selector("td.itdc a img", "attr", "alt", null, null);
        indexRule.datetime = new Selector("td.itd[style]", "html", null, null, null);
        indexRule.rating = new Selector("td.itd div div.it4 div", "attr", "style", "background-position:-?(\\d+)px -?(\\d+)px", "5-$1/16-($2-1)/40");

        galleryRule = new Rule();
        galleryRule.title = new Selector("h1#gj", "html", null, null, null);
        galleryRule.tags = new Selector("div#taglist table tr td:eq(1) div a", "html", null, null, null);
        galleryRule.item = new Selector("div.gdtl,div.gdtm", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("this", null, null, "(http://[^\"]*?\\.jpg)", null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("div.sni a img[style]", "attr", "src", null, null);
        //pic = new Selector("div.sni a img[style]", "attr", "src", null, null);

        sites.add(new Site(3, "Ex-hentai",
                "https://exhentai.org/?page={page:0}",
                "http://exhentai.org/g/{idCode:}/?p={page:0}",
                "http://exhentai.org/?f_search={keyword:}&page={page:0}",
                "https://forums.e-hentai.org/index.php?act=Login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_REPEATED_THUMBNAIL + "|" + Site.FLAG_PRELOAD_GALLERY));
        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://exhentai.org/?page={page:0}"));
        categories.add(new Category(2, "同人志", "http://exhentai.org/doujinshi/{page:0}"));
        categories.add(new Category(3, "漫画", "http://exhentai.org/manga/{page:0}"));
        categories.add(new Category(4, "同人CG", "http://exhentai.org/artistcg/{page:0}"));
        categories.add(new Category(5, "游戏CG", "http://exhentai.org/gamecg/{page:0}"));
        categories.add(new Category(6, "欧美", "http://exhentai.org/western/{page:0}"));
        categories.add(new Category(7, "Non-H", "http://exhentai.org/non-h/{page:0}"));
        categories.add(new Category(8, "图集", "http://exhentai.org/imageset/{page:0}"));
        categories.add(new Category(9, "Cosplay", "http://exhentai.org/cosplay/{page:0}"));
        categories.add(new Category(10, "亚洲AV", "http://exhentai.org/asianporn/{page:0}"));
        categories.add(new Category(11, "MISC", "http://exhentai.org/misc/{page:0}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // 绅士漫画
        indexRule = new Rule();
        indexRule.item = new Selector("div.gallary_wrap ul li.gallary_item", null, null, null, null);
        indexRule.idCode = new Selector("div.pic_box a", "attr", "href", "aid-(\\d+)", null);
        indexRule.title = new Selector("div.info div.title a", "html", null, null, null);
        indexRule.cover = new Selector("div.pic_box a img", "attr", "data-original", null, null);
        indexRule.datetime = new Selector("div.info div.info_col", "html", null, "(\\d{4}-\\d{2}-\\d{2})", null);

        galleryRule = new Rule();
        galleryRule.description = new Selector("div.uwconn > p:last-child", "html", null, null, null);
        galleryRule.item = new Selector("div.gallary_wrap ul li.gallary_item div.pic_box", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a img", "attr", "data-original", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#picarea", "attr", "src", null, null);
        //pic = new Selector("img#picarea", "attr", "src", null, null);

        sites.add(new Site(4, "绅士漫画",
                "http://www.wnacg.org/albums-index-page-{page:1}.html",
                "http://www.wnacg.org/photos-index-page-{page:1}-aid-{idCode:}.html",
                "http://www.wnacg.org/albums-index-page-{page:1}-sname-{keyword:}.html",
                "http://www.wnacg.com/users-login.html",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_NO_RATING + "|" + Site.FLAG_NO_TAG));
        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://www.wnacg.org/albums-index-page-{page:1}.html"));
        categories.add(new Category(2, "同人志", "http://www.wnacg.com/albums-index-page-{page:1}-cate-5.html"));
        categories.add(new Category(3, "同人志->汉化", "http://www.wnacg.com/albums-index-page-{page:1}-cate-1.html"));
        categories.add(new Category(4, "同人志->日语", "http://www.wnacg.com/albums-index-page-{page:1}-cate-12.html"));
        categories.add(new Category(5, "同人志->CG画集", "http://www.wnacg.com/albums-index-page-{page:1}-cate-2.html"));
        categories.add(new Category(6, "同人志->Cosplay", "http://www.wnacg.com/albums-index-page-{page:1}-cate-3.html"));
        categories.add(new Category(7, "单行本", "http://www.wnacg.com/albums-index-page-{page:1}-cate-6.html"));
        categories.add(new Category(8, "单行本->汉化", "http://www.wnacg.com/albums-index-page-{page:1}-cate-9.html"));
        categories.add(new Category(9, "单行本->日语", "http://www.wnacg.com/albums-index-page-{page:1}-cate-13.html"));
        categories.add(new Category(10, "杂志", "http://www.wnacg.com/albums-index-page-{page:1}-cate-7.html"));
        categories.add(new Category(11, "杂志->单篇汉化", "http://www.wnacg.com/albums-index-page-{page:1}-cate-10.html"));
        categories.add(new Category(12, "杂志->日语", "http://www.wnacg.com/albums-index-page-{page:1}-cate-14.html"));
        sites.get(sites.size() - 1).setCategories(categories);

        // nhentai
        indexRule = new Rule();
        indexRule.item = new Selector("div.container div.gallery", null, null, null, null);
        indexRule.idCode = new Selector("a", "attr", "href", null, null);
        indexRule.title = new Selector("a div.caption", "html", null, null, null);
        indexRule.cover = new Selector("a img", "attr", "src", "(.*)", "https:$1");

        galleryRule = new Rule();
        galleryRule.title = new Selector("div#info h2", "html", null, null, null);
        galleryRule.category = new Selector(".tag-container:eq(6) span.tags a", "html", null, "(.*)<span", null);
        galleryRule.tags = new Selector("span.tags a", "html", null, "(.*)<span", null);
        galleryRule.item = new Selector("div.container div.thumb-container", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a img", "attr", "data-src", "(.*)", "https:$1");

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#picarea", "attr", "src", null, null);
        //pic = new Selector("#image-container a img", "attr", "src", "(.*)", "https:$1");

        sites.add(new Site(5, "nhentai",
                "https://nhentai.net/?page={page:1}",
                "https://nhentai.net{idCode:}",
                "https://nhentai.net/search/?q={keyword:}&page={page:1}",
                "https://nhentai.net/login/",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_NO_RATING + "|" + Site.FLAG_NO_TAG));

        // 草榴社区
        indexRule = new Rule();
        indexRule.item = new Selector("#ajaxtable tr.tr3.t_one:gt(10)", null, null, null, null);
        indexRule.idCode = new Selector("td:eq(1) h3 a", "attr", "href", "htm_data/(.*?).html", null);
        indexRule.category = new Selector("td:eq(1)", "html", null, "\\[([^<>]*?)\\]", null);
        indexRule.title = new Selector("td:eq(1) h3 a", "html", null, "(<font.*?>)?([^<>]*)(</font>)?", "$2");
        indexRule.uploader = new Selector("td:eq(2) a", "html", null, null, null);
        indexRule.datetime = new Selector("td:eq(2) div", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.cover = new Selector("div.tpc_content input:eq(0)", "attr", "src", null, null);
        galleryRule.item = new Selector("div.tpc_content input", null, null, null, null);
        galleryRule.pictureUrl = new Selector("this", "attr", "src", null, null);
        galleryRule.pictureThumbnail = new Selector("this", "attr", "src", null, null);

        sites.add(new Site(6, "草榴社区",
                "http://cl.deocool.pw/thread0806.php?fid=8&page={page:1}",
                "http://cl.deocool.pw/htm_data/{idCode:}.html",
                null,
                "http://cl.deocool.pw/login.php",
                indexRule, galleryRule, null, null, Site.FLAG_NO_COVER + "|" + Site.FLAG_NO_RATING + "|" + Site.FLAG_NO_TAG));
        categories = new ArrayList<>();
        categories.add(new Category(1, "贴图区", "http://cl.deocool.pw/thread0806.php?fid=8&page={page:1}"));
        categories.add(new Category(2, "自拍区", "http://cl.deocool.pw/thread0806.php?fid=16&page={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // 177漫画
        indexRule = new Rule();
        indexRule.item = new Selector("div.post_box", null, null, null, null);
        indexRule.idCode = new Selector("div.c-top div.tit h2 a", "attr", "href", "html/(.*)\\.html", null);
        indexRule.title = new Selector("div.c-top div.tit h2 a", "html", null, null, null);
        indexRule.cover = new Selector("div.c-con a[rel='bookmark'] img", "attr", "src", null, null);
        indexRule.category = new Selector("div.c-top div.tit p span a", "html", null, null, null);
        indexRule.datetime = new Selector("div.c-top div.datetime", "html", null, "(\\d{4})<br>(\\d{2})-(\\d{2})", "$1-$2-$3");

        galleryRule = new Rule();
        galleryRule.tags = new Selector("div#taglist table tr td:eq(1) div a", "html", null, null, null);
        galleryRule.item = new Selector("div.entry-content > p", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img", "attr", "src", null, null);
        galleryRule.pictureThumbnail = new Selector("img", "attr", "src", null, null);

        sites.add(new Site(7, "177漫画",
                "http://www.177pic66.com/page/{page:1}?variant=zh-hans",
                "http://www.177pic66.com/html/{idCode:}.html/{page:1}",
                "http://www.177pic66.com/page/{page:1}?s={keyword:}&variant=zh-hans",
                null,
                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://www.177pic66.com/page/{page:1}?variant=zh-hans"));
        categories.add(new Category(2, "中文漫画", "http://www.177pic66.com/html/category/tt/page/{page:1}?variant=zh-hans"));
        categories.add(new Category(3, "全彩CG", "http://www.177pic66.com/html/category/cg/page/{page:1}?variant=zh-hans"));
        categories.add(new Category(4, "日文漫画", "http://www.177pic66.com/html/category/jj/page/{page:1}?variant=zh-hans"));
        sites.get(sites.size() - 1).setCategories(categories);

        // 二次萌エロ画像ブログ
        indexRule = new Rule();
        indexRule.item = new Selector("div.post", null, null, null, null);
        indexRule.idCode = new Selector("h2 > a", "attr", "href", "/(\\d+).html", null);
        indexRule.title = new Selector("h2 > a", "attr", "title", null, null);
        indexRule.cover = new Selector("div.more-field > div.box > a", "attr", "href", null, null);
        indexRule.category = new Selector("div.blog_info > ul > li.cat > a", "html", null, null, null);
        indexRule.datetime = new Selector("div.blog_info > ul > li.cal", "html", null, "(\\d{4}.\\d{2}.\\d{2}. [0-9:]+)", null);
        indexRule.tags = new Selector("div.blog_info > ul > li.tag > a[rel='tag']", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("div.box > a", null, null, null, null);
        galleryRule.pictureUrl = new Selector("this", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("this", "attr", "href", null, null);

        sites.add(new Site(8, "二次萌エロ画像ブログ",
                "http://moeimg.net/page/{page:1}",
                "http://moeimg.net/{idCode:}.html",
                "http://moeimg.net/?cat=0&s={keyword:}&submit=%E6%A4%9C%E7%B4%A2",
                null,
                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://moeimg.net/page/{page:1}"));
        categories.add(new Category(2, "エロ画像", "http://moeimg.net/category/%E3%82%A8%E3%83%AD%E7%94%BB%E5%83%8F/page/{page:1}"));
        categories.add(new Category(3, "非エロ・微エロ画像", "http://moeimg.net/category/%E9%9D%9E%E3%82%A8%E3%83%AD%E3%83%BB%E5%BE%AE%E3%82%A8%E3%83%AD%E7%94%BB%E5%83%8F/page/{page:1}"));
        categories.add(new Category(4, "ネタ画像", "http://moeimg.net/category/%E3%83%8D%E3%82%BF%E7%94%BB%E5%83%8F/page/{page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);


        // 二次元のエッチな画像
        indexRule = new Rule();
        indexRule.item = new Selector("section>div.post:not(.add) , #mainContent>div.post:not(.add)", null, null, null, null);
        indexRule.idCode = new Selector("a", "attr", "href", "com/(.*).html", null);
        indexRule.title = new Selector("section > h1 > a", "html", null, null, null);
        indexRule.cover = new Selector("div.postImage > img", "attr", "src", null, null);
        indexRule.category = new Selector("div.postDate > dl:first-child > dd > a", "html", null, null, null);
        indexRule.datetime = new Selector("div.postDate > dl:nth-child(2) > dd", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("div#entry > ul > li:not([class])", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img", "attr", "src", null, null);
        galleryRule.pictureThumbnail = new Selector("img", "attr", "src", null, null);

        sites.add(new Site(9, "二次元のエッチな画像",
                "http://nijiero-ch.com/page/{page:1}",
                "http://nijiero-ch.com/{idCode:}.html",
                "http://nijiero-ch.com/?s={keyword:}&paged={page:1}",
                "http://nijiero-ch.com/wp-login.php",
                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING + "|" + Site.FLAG_NO_TAG));


        /*******booru图站*******/

        // yande.re Post
        indexRule = new Rule();
        indexRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        indexRule.idCode = new Selector("div > a.thumb", "attr", "href", "/post/show/(\\d+)", null);
        indexRule.cover = new Selector("div > a.thumb > img", "attr", "src", null, null);
        indexRule.category = new Selector("a > span.directlink-res", "html", null, null, null);
        indexRule.uploader = new Selector("div > a.thumb > img", "attr", "title", "User: (\\w+)", null);
        indexRule.rating = new Selector("div > a.thumb > img", "attr", "title", "Rating:.*?(\\d+)", null);
        indexRule.tags = new Selector("div > a.thumb > img", "attr", "title", " ([a-z_]+)", null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(https://files.yande.re/image/.*?\\.(jpg|png|gif|bmp))\"", null);
        galleryRule.pictureThumbnail = new Selector("#post-view", "html", null, "\"(https://assets.yande.re/data/preview/.*?\\.(jpg|png|gif|bmp))\"", null);

        sites.add(new Site(31, "Yande.re Post",
                "https://yande.re/post?page={page:1}",
                "https://yande.re/post/show/{idCode:}",
                "https://yande.re/post?tags={keyword:}&page={page:1}",
                "https://yande.re/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "https://yande.re/post?page={page:1}"));
        categories.add(new Category(2, "随机", "https://yande.re/post?page={page:1}&tags=order%3Arandom"));
        categories.add(new Category(3, "热门（过去一天）", "https://yande.re/post/popular_recent?period=1d"));
        categories.add(new Category(4, "热门（过去一周）", "https://yande.re/post/popular_recent?period=1w"));
        categories.add(new Category(5, "热门（过去一月）", "https://yande.re/post/popular_recent?period=1m"));
        categories.add(new Category(6, "热门（过去一年）", "https://yande.re/post/popular_recent?period=1y"));
        categories.add(new Category(7, "热门（2016年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(8, "热门（2015年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(9, "热门（2014年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(10, "热门（2013年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2013"));
        sites.get(sites.size() - 1).setCategories(categories);

        // yande.re Pool
        indexRule = new Rule();
        indexRule.item = new Selector("#pool-index > table > tbody > tr", null, null, null, null);
        indexRule.idCode = new Selector("td:eq(0) > a", "attr", "href", "/pool/show/(\\d+)", null);
        indexRule.title = new Selector("td:eq(0) > a", "html", null, null, null);
        // booru的pool的封面是js动态显示的，无法通过选择器获取到
        //indexRule.cover = new Selector("document div > a.thumb > img", "attr", "src", null, null);
        indexRule.uploader = new Selector("td:eq(1)", "html", null, null, null);
        indexRule.category = new Selector("td:eq(2)", "html", null, "(\\d+)", "共 $1 页");
        indexRule.datetime = new Selector("td:eq(4)", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.cover = new Selector("#post-list-posts > li:first-child a.thumb > img", "attr", "src", null, null);
        galleryRule.description = new Selector("#pool-show > div:nth-child(2)", "html", null, null, null);
        galleryRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a.thumb", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a.thumb > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(https://files.yande.re/image/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

        sites.add(new Site(32, "Yande.re Pool",
                "https://yande.re/pool?page={page:1}",
                "https://yande.re/pool/show/{idCode:}",
                "https://yande.re/pool?query={keyword:}&page={page:1}",
                "https://yande.re/user/login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY));

        // lolibooru Post
        indexRule = new Rule();
        indexRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        indexRule.idCode = new Selector("div > a.thumb", "attr", "href", "/post/show/(\\d+)", null);
        indexRule.cover = new Selector("div > a.thumb > img", "attr", "src", null, null);
        indexRule.category = new Selector("a > span.directlink-res", "html", null, null, null);
        indexRule.uploader = new Selector("div > a.thumb > img", "attr", "title", "User: (\\w+)", null);
        indexRule.rating = new Selector("div > a.thumb > img", "attr", "title", "Rating:.*?(\\d+)", null);
        indexRule.tags = new Selector("div > a.thumb > img", "attr", "title", " ([a-z_]+)", null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(https://lolibooru.moe/image/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureThumbnail = new Selector("#post-view", "html", null, "\"https:\\\\/\\\\/lolibooru.moe\\\\/data\\\\/preview\\\\/(.*?\\.(jpg|jpeg|png|gif|bmp))\"", "https://lolibooru.moe/data/preview/$1");

        sites.add(new Site(33, "Lolibooru Post",
                "https://lolibooru.moe/post?page={page:1}",
                "https://lolibooru.moe/post/show/{idCode:}",
                "https://lolibooru.moe/post?tags={keyword:}&page={page:1}",
                "https://lolibooru.moe/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "https://lolibooru.moe/post?page={page:1}"));
        categories.add(new Category(2, "随机", "https://lolibooru.moe/post?page={page:1}&tags=order%3Arandom"));
        categories.add(new Category(3, "热门（过去一天）", "https://lolibooru.moe/post/popular_recent?period=1d"));
        categories.add(new Category(4, "热门（过去一周）", "https://lolibooru.moe/post/popular_recent?period=1w"));
        categories.add(new Category(5, "热门（过去一月）", "https://lolibooru.moe/post/popular_recent?period=1m"));
        categories.add(new Category(6, "热门（过去一年）", "https://lolibooru.moe/post/popular_recent?period=1y"));
        categories.add(new Category(7, "热门（2016年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(8, "热门（2015年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(9, "热门（2014年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(10, "热门（2013年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2013"));
        sites.get(sites.size() - 1).setCategories(categories);

        // lolibooru Pool
        indexRule = new Rule();
        indexRule.item = new Selector("#pool-index > table > tbody > tr", null, null, null, null);
        indexRule.idCode = new Selector("td:eq(0) > a", "attr", "href", "/pool/show/(\\d+)", null);
        indexRule.title = new Selector("td:eq(0) > a", "html", null, null, null);
        indexRule.uploader = new Selector("td:eq(1)", "html", null, null, null);
        indexRule.category = new Selector("td:eq(2)", "html", null, "(\\d+)", "共 $1 页");
        indexRule.datetime = new Selector("td:eq(4)", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.cover = new Selector("#post-list-posts > li:first-child a.thumb > img", "attr", "src", null, null);
        galleryRule.description = new Selector("#pool-show > div:nth-child(2)", "html", null, null, null);
        galleryRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a.thumb", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a.thumb > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(https://lolibooru.moe/image/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

        sites.add(new Site(34, "Lolibooru Pool",
                "https://lolibooru.moe/pool?page={page:1}",
                "https://lolibooru.moe/pool/show/{idCode:}",
                "https://lolibooru.moe/pool?query={keyword:}&page={page:1}",
                "https://lolibooru.moe/user/login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY));


        // konachan Post
        indexRule = new Rule();
        indexRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        indexRule.idCode = new Selector("div > a.thumb", "attr", "href", "/post/show/(\\d+)", null);
        indexRule.cover = new Selector("div > a.thumb > img", "attr", "src", null, null);
        indexRule.category = new Selector("a > span.directlink-res", "html", null, null, null);
        indexRule.uploader = new Selector("div > a.thumb > img", "attr", "title", "User: (\\w+)", null);
        indexRule.rating = new Selector("div > a.thumb > img", "attr", "title", "Rating:.*?(\\d+)", null);
        indexRule.tags = new Selector("div > a.thumb > img", "attr", "title", " ([a-z_]+)", null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(http://konachan.net/image/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureThumbnail = new Selector("#post-view", "html", null, "\"http:\\\\/\\\\/konachan.net\\\\/data\\\\/preview\\\\/(.*?\\.(jpg|jpeg|png|gif|bmp))\"", "http://konachan.net/data/preview/$1");

        sites.add(new Site(35, "Konachan Post",
                "https://konachan.net/post?page={page:1}",
                "https://konachan.net/post/show/{idCode:}",
                "https://konachan.net/post?tags={keyword:}&page={page:1}",
                "https://konachan.net/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "https://konachan.net/post?page={page:1}"));
        categories.add(new Category(2, "随机", "https://konachan.net/post?page={page:1}&tags=order%3Arandom"));
        categories.add(new Category(3, "热门（过去一天）", "https://konachan.net/post/popular_recent?period=1d"));
        categories.add(new Category(4, "热门（过去一周）", "https://konachan.net/post/popular_recent?period=1w"));
        categories.add(new Category(5, "热门（过去一月）", "https://konachan.net/post/popular_recent?period=1m"));
        categories.add(new Category(6, "热门（过去一年）", "https://konachan.net/post/popular_recent?period=1y"));
        categories.add(new Category(7, "热门（2016年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(8, "热门（2015年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(9, "热门（2014年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(10, "热门（2013年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2013"));
        sites.get(sites.size() - 1).setCategories(categories);

        // konachan Pool
        indexRule = new Rule();
        indexRule.item = new Selector("#pool-index > table > tbody > tr", null, null, null, null);
        indexRule.idCode = new Selector("td:eq(0) > a", "attr", "href", "/pool/show/(\\d+)", null);
        indexRule.title = new Selector("td:eq(0) > a", "html", null, null, null);
        indexRule.uploader = new Selector("td:eq(1)", "html", null, null, null);
        indexRule.category = new Selector("td:eq(2)", "html", null, "(\\d+)", "共 $1 页");
        indexRule.datetime = new Selector("td:eq(4)", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.cover = new Selector("#post-list-posts > li:first-child a.thumb > img", "attr", "src", null, null);
        galleryRule.description = new Selector("#pool-show > div:nth-child(2)", "html", null, null, null);
        galleryRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a.thumb", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a.thumb > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(http://konachan.net/image/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

        sites.add(new Site(36, "Konachan Pool",
                "https://konachan.net/pool?page={page:1}",
                "https://konachan.net/pool/show/{idCode:}",
                "https://konachan.net/pool?query={keyword:}&page={page:1}",
                "https://konachan.net/user/login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY));

        // 3dbooru Post
        indexRule = new Rule();
        indexRule.item = new Selector("span.thumb", null, null, null, null);
        indexRule.idCode = new Selector("a", "attr", "href", "/post/show/(\\d+)", null);
        indexRule.cover = new Selector("a > img", "attr", "src", null, null);
        indexRule.uploader = new Selector("a > img", "attr", "title", "user:(\\w+)", null);
        indexRule.rating = new Selector("a > img", "attr", "title", "rating:.*?(\\d+)", null);
        indexRule.tags = new Selector("a > img", "attr", "title", " ([a-z_]+)", null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("div.content", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureHighRes = new Selector("img#image", "attr", "src", "\"(http://behoimi.org/data)/sample/(.*?)/sample(\\w+\\.(jpg|jpeg|png|gif|bmp))\"", "$1/$2/$3");
        galleryRule.pictureThumbnail = new Selector("img#image", "attr", "src", "\"(http://behoimi.org/data)/sample/(.*?)/sample(\\w+\\.(jpg|jpeg|png|gif|bmp))\"", "$1/preview/$2/$3");

        sites.add(new Site(37, "3dbooru Post",
                "http://behoimi.org/post?page={page:1}",
                "http://behoimi.org/post/show/{idCode:}",
                "http://behoimi.org/post?tags={keyword:}&page={page:1}",
                "http://behoimi.org/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://behoimi.org/post?page={page:1}"));
        categories.add(new Category(2, "热门（过去一天）", "http://behoimi.org/post/popular_by_day"));
        categories.add(new Category(3, "热门（过去一周）", "http://behoimi.org/post/popular_by_week"));
        categories.add(new Category(4, "热门（过去一月）", "http://behoimi.org/post/popular_by_month"));
        categories.add(new Category(6, "热门（2016年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(7, "热门（2015年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(8, "热门（2014年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(9, "热门（2013年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2013"));
        sites.get(sites.size() - 1).setCategories(categories);

        // 3dbooru Pool
        indexRule = new Rule();
        indexRule.item = new Selector("#pool-index > table > tbody > tr", null, null, null, null);
        indexRule.idCode = new Selector("td:eq(0) > a", "attr", "href", "/pool/show/(\\d+)", null);
        indexRule.title = new Selector("td:eq(0) > a", "html", null, null, null);
        indexRule.uploader = new Selector("td:eq(1) > a", "html", null, null, null);
        indexRule.datetime = new Selector("td:eq(2)", "html", null, "(\\d+)", "共 $1 页");

        galleryRule = new Rule();
        galleryRule.cover = new Selector("span.thumb:first-child img", "attr", "src", null, null);
        galleryRule.description = new Selector("#pool-show > div:nth-child(2)", "html", null, null, null);
        galleryRule.item = new Selector("span.thumb", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureHighRes = new Selector("img#image", "attr", "src", "\"(http://behoimi.org/data)/sample/(.*?)/sample(\\w+\\.(jpg|jpeg|png|gif|bmp))\"", "$1/$2/$3");

        sites.add(new Site(38, "3dbooru Pool",
                "http://behoimi.org/pool?page={page:1}",
                "http://behoimi.org/pool/show/{idCode:}?page={page:1}",
                "http://behoimi.org/pool?query={keyword:}?page={page:1}",
                "http://behoimi.org/user/login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY));

        // gelbooru Post
        indexRule = new Rule();
        indexRule.item = new Selector("span.thumb", null, null, null, null);
        indexRule.idCode = new Selector("a", "attr", "href", "&id=(\\d+)", null);
        indexRule.cover = new Selector("a > img", "attr", "src", null, null);
        indexRule.uploader = new Selector("a > img", "attr", "title", "rating:(\\w+)", "安全等级：$1");
        indexRule.rating = new Selector("a > img", "attr", "title", "score:.*?(\\d+)", null);
        indexRule.tags = new Selector("a > img", "attr", "title", " ([a-z_]+)", null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(http://gelbooru.com//images/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureThumbnail = new Selector("img#image", "attr", "src", "http://.*?gelbooru.com//(samples|images)/(.*)/(sample_)?([^/]*)\\.", "http://gelbooru.com/thumbnails/$2/thumbnail_$4.jpg");

        sites.add(new Site(39, "Gelbooru Post",
                "http://gelbooru.com/index.php?page=post&s=list&tags=all&pid={page:0:42}",
                "http://gelbooru.com/index.php?page=post&s=view&id={idCode:}",
                "http://gelbooru.com/index.php?page=post&s=list&tags={keyword:}&pid={page:0:42}",
                "http://gelbooru.com/index.php?page=account&s=login&code=00",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://gelbooru.com/index.php?page=post&s=list&tags=all&pid={page:0:42}"));
        categories.add(new Category(2, "1boy", "http://gelbooru.com/index.php?page=post&s=list&tags=1boy&pid={page:0:42}"));
        categories.add(new Category(3, "1girl", "http://gelbooru.com/index.php?page=post&s=list&tags=1girl&pid={page:0:42}"));
        categories.add(new Category(4, "ass", "http://gelbooru.com/index.php?page=post&s=list&tags=ass&pid={page:0:42}"));
        categories.add(new Category(5, "bare shoulders", "http://gelbooru.com/index.php?page=post&s=list&tags=bare_shoulders&pid={page:0:42}"));
        categories.add(new Category(6, "blush", "http://gelbooru.com/index.php?page=post&s=list&tags=blush&pid={page:0:42}"));
        categories.add(new Category(7, "breasts", "http://gelbooru.com/index.php?page=post&s=list&tags=breasts&pid={page:0:42}"));
        categories.add(new Category(8, "brown eyes", "http://gelbooru.com/index.php?page=post&s=list&tags=brown_eyes&pid={page:0:42}"));
        categories.add(new Category(9, "brown hair", "http://gelbooru.com/index.php?page=post&s=list&tags=brown_hair&pid={page:0:42}"));
        categories.add(new Category(10, "eyes closed", "http://gelbooru.com/index.php?page=post&s=list&tags=eyes_closed&pid={page:0:42}"));
        categories.add(new Category(11, "fingerless gloves", "http://gelbooru.com/index.php?page=post&s=list&tags=fingerless_gloves&pid={page:0:42}"));
        categories.add(new Category(12, "from behind", "http://gelbooru.com/index.php?page=post&s=list&tags=from_behind&pid={page:0:42}"));
        categories.add(new Category(13, "hand on", "http://gelbooru.com/index.php?page=post&s=list&tags=hand_on&pid={page:0:42}"));
        categories.add(new Category(14, "hat", "http://gelbooru.com/index.php?page=post&s=list&tags=hat&pid={page:0:42}"));
        categories.add(new Category(15, "headband", "http://gelbooru.com/index.php?page=post&s=list&tags=headband&pid={page:0:42}"));
        categories.add(new Category(16, "hifumi", "http://gelbooru.com/index.php?page=post&s=list&tags=hifumi&pid={page:0:42}"));
        categories.add(new Category(17, "king of fighters", "http://gelbooru.com/index.php?page=post&s=list&tags=king_of_fighters&pid={page:0:42}"));
        categories.add(new Category(17, "large breasts", "http://gelbooru.com/index.php?page=post&s=list&tags=large_breasts&pid={page:0:42}"));
        categories.add(new Category(18, "leaning", "http://gelbooru.com/index.php?page=post&s=list&tags=leaning&pid={page:0:42}"));
        categories.add(new Category(19, "leaning forward", "http://gelbooru.com/index.php?page=post&s=list&tags=leaning_forward&pid={page:0:42}"));
        categories.add(new Category(20, "looking at viewer", "http://gelbooru.com/index.php?page=post&s=list&tags=looking_at_viewer&pid={page:0:42}"));
        categories.add(new Category(21, "male focus", "http://gelbooru.com/index.php?page=post&s=list&tags=male_focus&pid={page:0:42}"));
        categories.add(new Category(22, "monkey d luffy", "http://gelbooru.com/index.php?page=post&s=list&tags=monkey_d_luffy&pid={page:0:42}"));
        categories.add(new Category(23, "monochrome", "http://gelbooru.com/index.php?page=post&s=list&tags=monochrome&pid={page:0:42}"));
        categories.add(new Category(24, "one piece", "http://gelbooru.com/index.php?page=post&s=list&tags=one_piece&pid={page:0:42}"));
        categories.add(new Category(25, "open clothes", "http://gelbooru.com/index.php?page=post&s=list&tags=open_clothes&pid={page:0:42}"));
        categories.add(new Category(26, "open shirt", "http://gelbooru.com/index.php?page=post&s=list&tags=open_shirt&pid={page:0:42}"));
        categories.add(new Category(27, "pantylines", "http://gelbooru.com/index.php?page=post&s=list&tags=pantylines&pid={page:0:42}"));
        categories.add(new Category(28, "scar", "http://gelbooru.com/index.php?page=post&s=list&tags=scar&pid={page:0:42}"));
        categories.add(new Category(29, "shiny", "http://gelbooru.com/index.php?page=post&s=list&tags=shiny&pid={page:0:42}"));
        categories.add(new Category(30, "shiny clothes", "http://gelbooru.com/index.php?page=post&s=list&tags=shiny_clothes&pid={page:0:42}"));
        categories.add(new Category(31, "shiny hair", "http://gelbooru.com/index.php?page=post&s=list&tags=shiny_hair&pid={page:0:42}"));
        categories.add(new Category(32, "shiny skin", "http://gelbooru.com/index.php?page=post&s=list&tags=shiny_skin&pid={page:0:42}"));
        categories.add(new Category(33, "short hair", "http://gelbooru.com/index.php?page=post&s=list&tags=short_hair&pid={page:0:42}"));
        categories.add(new Category(34, "sideboob", "http://gelbooru.com/index.php?page=post&s=list&tags=sideboob&pid={page:0:42}"));
        categories.add(new Category(35, "simple background", "http://gelbooru.com/index.php?page=post&s=list&tags=simple_background&pid={page:0:42}"));
        categories.add(new Category(36, "skin tight", "http://gelbooru.com/index.php?page=post&s=list&tags=skin_tight&pid={page:0:42}"));
        categories.add(new Category(37, "smile", "http://gelbooru.com/index.php?page=post&s=list&tags=smile&pid={page:0:42}"));
        categories.add(new Category(38, "solo", "http://gelbooru.com/index.php?page=post&s=list&tags=solo&pid={page:0:42}"));
        categories.add(new Category(39, "spandex", "http://gelbooru.com/index.php?page=post&s=list&tags=spandex&pid={page:0:42}"));
        categories.add(new Category(40, "straw hat", "http://gelbooru.com/index.php?page=post&s=list&tags=straw_hat&pid={page:0:42}"));
        categories.add(new Category(41, "teeth", "http://gelbooru.com/index.php?page=post&s=list&tags=teeth&pid={page:0:42}"));
        categories.add(new Category(42, "the king of fighters", "http://gelbooru.com/index.php?page=post&s=list&tags=the_king_of_fighters&pid={page:0:42}"));
        categories.add(new Category(43, "tongue", "http://gelbooru.com/index.php?page=post&s=list&tags=tongue&pid={page:0:42}"));
        categories.add(new Category(44, "tongue out", "http://gelbooru.com/index.php?page=post&s=list&tags=tongue_out&pid={page:0:42}"));
        categories.add(new Category(45, "yuri sakazaki", "http://gelbooru.com/index.php?page=post&s=list&tags=yuri_sakazaki&pid={page:0:42}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // gelbooru Pool
        indexRule = new Rule();
        indexRule.item = new Selector("#content > table tr", null, null, null, null);
        indexRule.idCode = new Selector("td:first-child > a", "attr", "href", "id=(\\d+)", null);
        indexRule.title = new Selector("td:nth-child(2)> div:first-child > a", "html", null, null, null);
        indexRule.uploader = new Selector("td:nth-child(2) > span > a", "html", null, null, null);
        indexRule.category = new Selector("td:nth-child(4)", "html", null, null, null);
        indexRule.datetime = new Selector("td:nth-child(3)", "html", null, "(\\d+)", "共 $1 页");

        galleryRule = new Rule();
        galleryRule.cover = new Selector("span.thumb img", "attr", "src", null, null);
        galleryRule.description = new Selector("div#content > div", "html", null, null, null);
        galleryRule.item = new Selector("span.thumb", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(http://gelbooru.com//images/.*?\\.(jpg|png|gif|bmp))\"", null);

        sites.add(new Site(40, "Gelbooru Pool",
                "http://gelbooru.com/index.php?page=pool&s=list&pid={page:0:25}",
                "http://gelbooru.com/index.php?page=pool&s=show&id={idCode:}",
                null,
                "http://gelbooru.com/index.php?page=account&s=login&code=00",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY));



        // xbooru Post
        indexRule = new Rule();
        indexRule.item = new Selector("span.thumb", null, null, null, null);
        indexRule.idCode = new Selector("a", "attr", "href", "&id=(\\d+)", null);
        indexRule.cover = new Selector("a > img", "attr", "src", null, null);
        indexRule.uploader = new Selector("a > img", "attr", "title", "rating:(\\w+)", "安全等级：$1");
        indexRule.rating = new Selector("a > img", "attr", "title", "score:.*?(\\d+)", null);
        indexRule.tags = new Selector("a > img", "attr", "title", " ([a-z_]+)", null);

        galleryRule = new Rule();
        galleryRule.item = new Selector("div.content", null, null, null, null);
        galleryRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(http://img.xbooru.com//images/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureThumbnail = new Selector("img#image", "attr", "src", "http://img.xbooru.com//(samples|images)/(.*)/(sample_)?([^/]*)\\.", "http://img.xbooru.com/thumbnails/$2/thumbnail_$4.jpg");

        sites.add(new Site(41, "Xbooru Post",
                "http://xbooru.com/index.php?page=post&s=list&pid={page:0:42}",
                "http://xbooru.com/index.php?page=post&s=view&id={idCode:}",
                "http://xbooru.com/index.php?page=post&s=list&tags={keyword:}&pid={page:0:42}",
                "http://xbooru.com/index.php?page=account&s=login&code=00",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE));

        // xbooru Pool
        indexRule = new Rule();
        indexRule.item = new Selector("#pool-index > table > tbody > tr", null, null, null, null);
        indexRule.idCode = new Selector("td:eq(0) > a", "attr", "href", "id=(\\d+)", null);
        indexRule.title = new Selector("td:eq(0) > a", "html", null, null, null);
        indexRule.uploader = new Selector("td:eq(1) > a", "html", null, null, null);
        indexRule.datetime = new Selector("td:eq(2)", "html", null, "(\\d+)", "共 $1 页");

        galleryRule = new Rule();
        galleryRule.cover = new Selector("span.thumb img", "attr", "src", null, null);
        galleryRule.description = new Selector("div#content > div", "html", null, null, null);
        galleryRule.item = new Selector("span.thumb", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(http://img.xbooru.com//images/.*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

        sites.add(new Site(42, "Xbooru Pool",
                "http://xbooru.com/index.php?page=pool&s=list&pid={page:0:25}",
                "http://xbooru.com/index.php?page=pool&s=show&id={idCode:}",
                null,
                "http://gelbooru.com/index.php?page=account&s=login&code=00",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY));

        /*******非和谐站*******/

        // 绝对领域
        indexRule = new Rule();
        indexRule.item = new Selector("div#postlist > div.pin", null, null, null, null);
        indexRule.idCode = new Selector("div.pin-coat a", "attr", "href", "http://.*?/(\\d+)", null);
        indexRule.title = new Selector("div.pin-coat > a span", "html", null, null, null);
        indexRule.cover = new Selector("div.pin-coat > a img", "attr", "original", null, null);
        indexRule.datetime = new Selector("div.pin-coat div.pin-data span.timer span", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.datetime = new Selector("div.main-header > div.main-meta > span:eq(0)", "html", null, null, null);
        galleryRule.category = new Selector("div.main-header > div.main-meta > span:eq(1) > a", "html", null, null, null);
        galleryRule.tags = new Selector("div.main-tags > a[rel='tag']", "html", null, null, null);
        galleryRule.item = new Selector("div.main-body p > a", null, null, null, null);
        galleryRule.pictureUrl = new Selector("this", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("this", "attr", "href", null, null);

        sites.add(new Site(51, "绝对领域",
                "http://www.jdlingyu.moe/page/{page:1}/",
                "http://www.jdlingyu.moe/{idCode:}/",
                "http://www.jdlingyu.moe/page/{page:1}/?s={keyword:}",
                "http://www.jdlingyu.moe/wp-login.php",
                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://www.jdlingyu.moe/page/{page:1}/"));
        categories.add(new Category(2, "专题", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/{pageStr:page/{page:1}/}"));
        categories.add(new Category(3, "专题->自拍", "http://www.jdlingyu.moe/%e8%87%aa%e6%8b%8d/{pageStr:page/{page:1}/}"));
        categories.add(new Category(4, "专题->放流", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/%e6%94%be%e6%b5%81/{pageStr:page/{page:1}/}"));
        categories.add(new Category(5, "专题->独家", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/%e7%8b%ac%e5%ae%b6/{pageStr:page/{page:1}/}"));
        categories.add(new Category(6, "专题->漫展最前线", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/mzzqx/{pageStr:page/{page:1}/}"));
        categories.add(new Category(7, "特点", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/{pageStr:page/{page:1}/}"));
        categories.add(new Category(8, "特点->胖次", "http://www.jdlingyu.moe/%e8%83%96%e6%ac%a1/{pageStr:page/{page:1}/}"));
        categories.add(new Category(9, "特点->丝袜", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e4%b8%9d%e8%a2%9c/{pageStr:page/{page:1}/}"));
        categories.add(new Category(10, "特点->汉服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e6%b1%89%e6%9c%8d/{pageStr:page/{page:1}/}"));
        categories.add(new Category(11, "特点->死库水", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e6%ad%bb%e5%ba%93%e6%b0%b4/{pageStr:page/{page:1}/}"));
        categories.add(new Category(12, "特点->体操服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e4%bd%93%e6%93%8d%e6%9c%8d/{pageStr:page/{page:1}/}"));
        categories.add(new Category(13, "特点->女仆装", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e5%a5%b3%e4%bb%86%e8%a3%85/{pageStr:page/{page:1}/}"));
        categories.add(new Category(14, "特点->水手服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e6%b0%b4%e6%89%8b%e6%9c%8d/{pageStr:page/{page:1}/}"));
        categories.add(new Category(15, "特点->和服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e5%92%8c%e6%9c%8d%e6%b5%b4%e8%a1%a3/{pageStr:page/{page:1}/}"));
        categories.add(new Category(16, "弄潮", "http://www.jdlingyu.moe/%e5%bc%84%e6%bd%ae/{pageStr:page/{page:1}/}"));
        categories.add(new Category(16, "弄潮->束缚", "http://www.jdlingyu.moe/%e5%bc%84%e6%bd%ae/%e6%9d%9f%e7%bc%9a/{pageStr:page/{page:1}/}"));
        categories.add(new Category(17, "Cosplay", "http://www.jdlingyu.moe/cosplay/{pageStr:page/{page:1}/}"));
        categories.add(new Category(18, "写真", "http://www.jdlingyu.moe/%e5%86%99%e7%9c%9f/{pageStr:page/{page:1}/}"));
        categories.add(new Category(19, "下载", "http://www.jdlingyu.moe/%e4%b8%8b%e8%bd%bd/{pageStr:page/{page:1}/}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // E-shuushuu
        indexRule = new Rule();
        indexRule.item = new Selector("div.display:has(.thumb)", null, null, null, null);
        indexRule.idCode = new Selector(".title h2 a", "attr", "href", null, null);
        indexRule.title = new Selector(".title h2 a", "html", null, null, null);
        indexRule.uploader = new Selector(".meta dl dd span.reg_user", "html", null, null, null);
        indexRule.cover = new Selector("a.thumb_image img", "attr", "src", null, null);
        indexRule.datetime = new Selector(".meta dd:eq(3)", "html", null, null, null);
        indexRule.tags = new Selector(".meta span.tag a", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.rating = new Selector(".display .meta dl dd[id^='rating']", "html", null, "(\\d*\\.?\\d*).*?<img", "$2/2");
        galleryRule.item = new Selector(".image_thread .image_block", null, null, null, null);
        galleryRule.pictureUrl = new Selector("a.thumb_image", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("a.thumb_image img", "attr", "src", null, null);

        sites.add(new Site(52, "E-shuushuu",
                "http://e-shuushuu.net/?page={page:1}",
                "http://e-shuushuu.net/{idCode:}",
                null,
                "http://e-shuushuu.net/",
                indexRule, galleryRule, null, null, null));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://e-shuushuu.net/?page={page:1}"));
        categories.add(new Category(2, "排行榜", "http://e-shuushuu.net/top.php?page={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);


        // Pixiv
        indexRule = new Rule();
        indexRule.item = new Selector("ul._image-items>li.image-item, section.ranking-item", null, null, null, null);
        indexRule.idCode = new Selector("a.work", "attr", "href", "illust_id=(\\d+)", null);
        indexRule.cover = new Selector("a.work img._thumbnail", null, null, "\"(http://[^\"]*?\\.jpg)\"", null);
        indexRule.title = new Selector("a>h1.title, h2>a.title", "html", null, null, null);
        indexRule.uploader = new Selector("a.user, a.user-container>span", "html", null, null, null);
        indexRule.category = new Selector("div.rank>h1>a", "html", null, null, null);
        indexRule.datetime = new Selector("a.work img._thumbnail", null, null, ".*img/(\\d{4})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})", "$1-$2-$3 $4:$5:$6");

        galleryRule = new Rule();
        galleryRule.title = new Selector("div.ui-expander-target > h1.title", "html", null, null, null);
        galleryRule.uploader = new Selector("a.user-link > h1.user", "html", null, null, null);
        galleryRule.datetime = new Selector("ul.meta > li:eq(0)", "html", null, null, null);
        galleryRule.description = new Selector("div.ui-expander-target > p.caption", "html", null, null, null);
        galleryRule.tags = new Selector("ul.tags > li.tag > a.text", "html", null, null, null);
        galleryRule.rating = new Selector("section.score", "html", null, "rated-count\">(\\d+).*score-count\">(\\d+)", "$2/$1/2");
        galleryRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureUrl = new Selector("div#wrapper", "html", null, "\"(member_illust.php\\?mode=manga.*?|http://i\\d.pixiv.net/img-original/img/.*?\\.(png|jpg|bmp|gif)))\"", null);
        galleryRule.pictureThumbnail = new Selector("div.works_display div._layout-thumbnail > img", "attr", "src", "(http://.*?c)/\\d+x\\d+/(.*?\\.jpg)", "$1/150x150/$2");

        extraRule = new Rule();
        extraRule.item = new Selector("div.item-container", null, null, null, null);
        extraRule.pictureUrl = new Selector("img", "attr", "data-src", null, null);
        extraRule.pictureThumbnail = new Selector("img", "attr", "data-src", "(http://.*?c)/\\d+x\\d+/(.*?\\.jpg)", "$1/150x150/$2");

        sites.add(new Site(53, "Pixiv",
                "http://www.pixiv.net/new_illust.php?p={page:1}",
                "http://www.pixiv.net/member_illust.php?mode=medium&illust_id={idCode:}",
                "http://www.pixiv.net/search.php?word={keyword:}&p={page:1}",
                "https://accounts.pixiv.net/login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SECOND_LEVEL_GALLERY+"|"+Site.FLAG_PRELOAD_GALLERY));
        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://www.pixiv.net/new_illust.php?p={page:1}"));
        categories.add(new Category(2, "综合今日排行榜", "http://www.pixiv.net/ranking.php?mode=daily&p={page:1}"));
        categories.add(new Category(3, "综合本周排行榜", "http://www.pixiv.net/ranking.php?mode=weekly&p={page:1}"));
        categories.add(new Category(4, "综合本月排行榜", "http://www.pixiv.net/ranking.php?mode=monthly&p={page:1}"));
        categories.add(new Category(5, "18R", "http://www.pixiv.net/new_illust_r18.php?p={page:1}"));
        categories.add(new Category(6, "10000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=10000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(7, "5000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=5000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(8, "3000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=3000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(9, "1000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=1000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(10, "Loli", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%E3%83%AD%E3%83%AA&p={page:1}"));
        categories.add(new Category(11, "東方", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%E6%9D%B1%E6%96%B9&p={page:1}"));
        categories.add(new Category(12, "艦これ", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%E8%89%A6%E3%81%93%E3%82%8C&p={page:1}"));
        categories.add(new Category(13, "VOCALOID", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=VOCALOID&p={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);
        sites.get(sites.size() - 1).cookie = "p_ab_id=4; _gat=1; PHPSESSID=19726569_cf8243e85368f6e8965c6e19068b4da5; device_token=0074d3631c53eff71393c60ac338f0ef; a_type=0; __utmt=1; __utma=235335808.1998756366.1474474879.1474475016.1474475016.1; __utmb=235335808.1.10.1474475016; __utmc=235335808; __utmz=235335808.1474475016.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmv=235335808.|2=login%20ever=yes=1^3=plan=normal=1^5=gender=male=1^6=user_id=19726569=1; _ga=GA1.2.1998756366.1474474879; _gat_UA-74360115-3=1";

        return sites;
    }
}
