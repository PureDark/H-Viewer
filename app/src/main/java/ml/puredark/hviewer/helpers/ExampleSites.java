package ml.puredark.hviewer.helpers;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.CommentRule;
import ml.puredark.hviewer.beans.PictureRule;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.TagRule;
import ml.puredark.hviewer.beans.VideoRule;

/**
 * Created by PureDark on 2016/9/21.
 */

public class ExampleSites {

    public static List<Site> get() {

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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("#gh .gi", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a img", "attr", "src", null, null);

        Rule searchRule = new Rule();

        Rule extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("img#sm", "attr", "src", null, null);

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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.gdtl,div.gdtm", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", null, null, "(http://[^\"]*?\\.jpg)", null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div#cdiv > div.c1", null, null, null, null);
        galleryRule.commentRule.author = new Selector("div.c3 > a:first-child", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.c3", "html", null, "Posted on (.*?) UTC by", null);
        galleryRule.commentRule.content = new Selector("div.c6", "html", null, null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("div.sni a img[style]", "attr", "src", null, null);
        //pic = new Selector("div.sni a img[style]", "attr", "src", null, null);

        sites.add(new Site(2, "G.E-hentai",
                "http://g.e-hentai.org/?page={page:0}",
                "http://g.e-hentai.org/g/{idCode:}/?p={page:0}&hc=1",
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.gdtl,div.gdtm", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", null, null, "(http://[^\"]*?\\.jpg)", null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div#cdiv > div.c1", null, null, null, null);
        galleryRule.commentRule.author = new Selector("div.c3 > a:first-child", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.c3", "html", null, "Posted on (.*?) UTC by", null);
        galleryRule.commentRule.content = new Selector("div.c6", "html", null, null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("div.sni a img[style]", "attr", "src", null, null);
        //pic = new Selector("div.sni a img[style]", "attr", "src", null, null);

        sites.add(new Site(3, "Ex-hentai",
                "https://exhentai.org/?page={page:0}",
                "http://exhentai.org/g/{idCode:}/?p={page:0}&hc=1",
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.gallary_wrap ul li.gallary_item div.pic_box", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a img", "attr", "data-original", null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("img#picarea", "attr", "src", null, null);
        //pic = new Selector("img#picarea", "attr", "src", null, null);

        sites.add(new Site(4, "绅士漫画",
                "http://www.wnacg.com/albums-index-page-{page:1}.html",
                "http://www.wnacg.com/photos-index-page-{page:1}-aid-{idCode:}.html",
                "http://www.wnacg.com/albums-index-page-{page:1}-sname-{keyword:}.html",
                "http://www.wnacg.com/users-login.html",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_NO_RATING + "|" + Site.FLAG_NO_TAG));
        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://www.wnacg.com/albums-index-page-{page:1}.html"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.container div.thumb-container", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a img", "attr", "data-src", "(.*)", "https:$1");

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("#image-container a img", "attr", "src", "(.*)", "https:$1");

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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.tpc_content input", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "src", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "src", null, null);
        galleryRule.videoRule = new VideoRule();
        galleryRule.videoRule.item = new Selector("div.tpc_content", null, null, "點擊這里打開新視窗", null);
        galleryRule.videoRule.content = new Selector("a[onclick]", null, null, "getElementById\\('iframe1'\\)\\.src='(.*?)'", null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div.t.t2:not([style])", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("td.tac > img", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("th.r_two > font > b", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.tipad", "html", null, "Posted:(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})", null);
        galleryRule.commentRule.content = new Selector("div.tpc_content", "html", null, null, null);

        sites.add(new Site(6, "草榴社区",
                "http://cl.deocool.pw/thread0806.php?fid=8&page={page:1}",
                "http://cl.deocool.pw/htm_data/{idCode:}.html",
                null,
                "http://cl.deocool.pw/login.php",
                indexRule, galleryRule, null, null, Site.FLAG_NO_COVER + "|" + Site.FLAG_NO_RATING + "|" + Site.FLAG_NO_TAG));
        categories = new ArrayList<>();
        categories.add(new Category(1, "贴图区", "http://cl.deocool.pw/thread0806.php?fid=8&page={page:1}"));
        categories.add(new Category(2, "自拍区", "http://cl.deocool.pw/thread0806.php?fid=16&page={page:1}"));
        categories.add(new Category(3, "在线视频", "http://cl.deocool.pw/thread0806.php?fid=22&page={page:1}"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.entry-content > p", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img", "attr", "src", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("img", "attr", "src", null, null);

        sites.add(new Site(7, "177漫画",
                "http://www.177picxx.info/page/{page:1}?variant=zh-hans",
                "http://www.177picxx.info/html/{idCode:}.html/{page:1}",
                "http://www.177picxx.info/page/{page:1}?s={keyword:}&variant=zh-hans",
                null,
                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://www.177picxx.info/page/{page:1}?variant=zh-hans"));
        categories.add(new Category(2, "中文漫画", "http://www.177picxx.info/html/category/tt/page/{page:1}?variant=zh-hans"));
        categories.add(new Category(3, "全彩CG", "http://www.177picxx.info/html/category/cg/page/{page:1}?variant=zh-hans"));
        categories.add(new Category(4, "日文漫画", "http://www.177picxx.info/html/category/jj/page/{page:1}?variant=zh-hans"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.box > a", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "href", null, null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("ol.commentlist > li.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("div.comment-author > img", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("div.comment-author > cite", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.comment-meta > a", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.comment-body > p", null, null, null, null);

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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div#entry > ul > li:not([class])", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img", "attr", "src", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("img", "attr", "src", null, null);

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
        indexRule.tags = new Selector("div > a.thumb > img", "attr", "title", " ([a-z_()]+)", null);

        galleryRule = new Rule();
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(https://files.yande.re/image/[^\"]*?\\.(jpg|png|gif|bmp))\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("#post-view", "html", null, "\"(https://assets.yande.re/data/preview/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div.response-list > div.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("img.avatar", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("div.author > h6 > a", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.author > span.date > a", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.content > div.body", "html", null, null, null);

        sites.add(new Site(31, "Yande.re Post",
                "https://yande.re/post?page={page:1}",
                "https://yande.re/post/show/{idCode:}",
                "https://yande.re/post?tags={keyword:}&page={page:1}",
                "https://yande.re/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE + "|" + Site.FLAG_ONE_PIC_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "https://yande.re/post?page={page:1}"));
        categories.add(new Category(2, "随机", "https://yande.re/post?page={page:1}&tags=order%3Arandom"));
        categories.add(new Category(3, "评级：安全", "https://yande.re/post?tags=rating%3Asafe&page={page:1}"));
        categories.add(new Category(4, "评级：存疑", "https://yande.re/post?tags=rating%3Aquestionable&page={page:1}"));
        categories.add(new Category(5, "评级：露骨", "https://yande.re/post?tags=rating%3Aexplicit&page={page:1}"));
        categories.add(new Category(6, "热门（过去一天）", "https://yande.re/post/popular_recent?period=1d"));
        categories.add(new Category(7, "热门（过去一周）", "https://yande.re/post/popular_recent?period=1w"));
        categories.add(new Category(8, "热门（过去一月）", "https://yande.re/post/popular_recent?period=1m"));
        categories.add(new Category(9, "热门（过去一年）", "https://yande.re/post/popular_recent?period=1y"));
        categories.add(new Category(10, "热门（2016年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(11, "热门（2015年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(12, "热门（2014年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(13, "热门（2013年）", "https://yande.re/post/popular_by_month?month={page:1}&year=2013"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a.thumb", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a.thumb > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureUrl = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureHighRes = new Selector("#post-view", "html", null, "\"(https://files.yande.re/image/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

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
        indexRule.tags = new Selector("div > a.thumb > img", "attr", "title", " ([a-z_()]+)", null);

        galleryRule = new Rule();
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(https://lolibooru.moe/image/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("#post-view", "html", null, "\"https:\\\\/\\\\/lolibooru.moe\\\\/data\\\\/preview\\\\/([^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", "https://lolibooru.moe/data/preview/$1");
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div.response-list > div.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("img.avatar", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("div.author > h6 > a", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.author > span.date > a", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.content > div.body", "html", null, null, null);

        sites.add(new Site(33, "Lolibooru Post",
                "https://lolibooru.moe/post?page={page:1}",
                "https://lolibooru.moe/post/show/{idCode:}",
                "https://lolibooru.moe/post?tags={keyword:}&page={page:1}",
                "https://lolibooru.moe/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE + "|" + Site.FLAG_ONE_PIC_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "https://lolibooru.moe/post?page={page:1}"));
        categories.add(new Category(2, "随机", "https://lolibooru.moe/post?page={page:1}&tags=order%3Arandom"));
        categories.add(new Category(3, "评级：安全", "https://lolibooru.moe/post?tags=rating%3Asafe&page={page:1}"));
        categories.add(new Category(4, "评级：存疑", "https://lolibooru.moe/post?tags=rating%3Aquestionable&page={page:1}"));
        categories.add(new Category(5, "评级：露骨", "https://lolibooru.moe/post?tags=rating%3Aexplicit&page={page:1}"));
        categories.add(new Category(6, "热门（过去一天）", "https://lolibooru.moe/post/popular_recent?period=1d"));
        categories.add(new Category(7, "热门（过去一周）", "https://lolibooru.moe/post/popular_recent?period=1w"));
        categories.add(new Category(8, "热门（过去一月）", "https://lolibooru.moe/post/popular_recent?period=1m"));
        categories.add(new Category(9, "热门（过去一年）", "https://lolibooru.moe/post/popular_recent?period=1y"));
        categories.add(new Category(10, "热门（2016年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(11, "热门（2015年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(12, "热门（2014年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(13, "热门（2013年）", "https://lolibooru.moe/post/popular_by_month?month={page:1}&year=2013"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a.thumb", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a.thumb > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(https://lolibooru.moe/image/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

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
        indexRule.tags = new Selector("div > a.thumb > img", "attr", "title", " ([a-z_()]+)", null);

        galleryRule = new Rule();
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://konachan.net/image/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("#post-view", "html", null, "\"http:\\\\/\\\\/konachan.net\\\\/data\\\\/preview\\\\/([^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", "http://konachan.net/data/preview/$1");
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div.response-list > div.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("img.avatar", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("div.author > h6 > a", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.author > span.date > a", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.content > div.body", "html", null, null, null);

        sites.add(new Site(35, "Konachan Post",
                "https://konachan.net/post?page={page:1}",
                "https://konachan.net/post/show/{idCode:}",
                "https://konachan.net/post?tags={keyword:}&page={page:1}",
                "https://konachan.net/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE + "|" + Site.FLAG_ONE_PIC_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "https://konachan.net/post?page={page:1}"));
        categories.add(new Category(2, "随机", "https://konachan.net/post?page={page:1}&tags=order%3Arandom"));
        categories.add(new Category(3, "评级：安全", "https://konachan.net/post?tags=rating%3Asafe&page={page:1}"));
        categories.add(new Category(4, "评级：存疑", "https://konachan.net/post?tags=rating%3Aquestionable&page={page:1}"));
        categories.add(new Category(5, "评级：露骨", "https://konachan.net/post?tags=rating%3Aexplicit&page={page:1}"));
        categories.add(new Category(6, "热门（过去一天）", "https://konachan.net/post/popular_recent?period=1d"));
        categories.add(new Category(7, "热门（过去一周）", "https://konachan.net/post/popular_recent?period=1w"));
        categories.add(new Category(8, "热门（过去一月）", "https://konachan.net/post/popular_recent?period=1m"));
        categories.add(new Category(9, "热门（过去一年）", "https://konachan.net/post/popular_recent?period=1y"));
        categories.add(new Category(10, "热门（2016年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(11, "热门（2015年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(12, "热门（2014年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(13, "热门（2013年）", "https://konachan.net/post/popular_by_month?month={page:1}&year=2013"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("#post-list-posts > li", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a.thumb", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a.thumb > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://konachan.net/image/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

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
        indexRule.tags = new Selector("a > img", "attr", "title", " ([a-z_()]+)", null);

        galleryRule = new Rule();
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://behoimi.org/data/(?!sample)[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("img#image", "attr", "src", "(http://behoimi.org/data)(/sample)?/([^\"]*?)/(sample)?([^/]*)\\.", "$1/preview/$3/$5.jpg");
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div.response-list > div.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("img.avatar", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("div.author > h6 > a", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.author > span.date > a", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.content > div.body", "html", null, null, null);

        sites.add(new Site(37, "3dbooru Post",
                "http://behoimi.org/post?page={page:1}",
                "http://behoimi.org/post/show/{idCode:}",
                "http://behoimi.org/post?tags={keyword:}&page={page:1}",
                "http://behoimi.org/user/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE + "|" + Site.FLAG_ONE_PIC_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://behoimi.org/post?page={page:1}"));
        categories.add(new Category(2, "评级：安全", "http://behoimi.org/post?tags=rating%3Asafe&page={page:1}"));
        categories.add(new Category(3, "评级：存疑", "http://behoimi.org/post?tags=rating%3Aquestionable&page={page:1}"));
        categories.add(new Category(4, "评级：露骨", "http://behoimi.org/post?tags=rating%3Aexplicit&page={page:1}"));
        categories.add(new Category(5, "热门（过去一天）", "http://behoimi.org/post/popular_by_day"));
        categories.add(new Category(6, "热门（过去一周）", "http://behoimi.org/post/popular_by_week"));
        categories.add(new Category(7, "热门（过去一月）", "http://behoimi.org/post/popular_by_month"));
        categories.add(new Category(8, "热门（2016年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2016"));
        categories.add(new Category(9, "热门（2015年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2015"));
        categories.add(new Category(10, "热门（2014年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2014"));
        categories.add(new Category(11, "热门（2013年）", "http://behoimi.org/post/popular_by_month?month={page:1}&year=2013"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("span.thumb", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://behoimi.org/data/(?!sample)[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

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
        indexRule.tags = new Selector("a > img", "attr", "title", " ([a-z_()]+)", null);

        galleryRule = new Rule();
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://gelbooru.com//images/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("img#image", "attr", "src", "http://.*?gelbooru.com//(samples|images)/(.*)/(sample_)?([^/]*)\\.", "http://gelbooru.com/thumbnails/$2/thumbnail_$4.jpg");
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div[id^=c][style*='display']", null, null, null, null);
        galleryRule.commentRule.author = new Selector("a[href^='index']", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("b", "html", null, "Posted on (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})", null);
        galleryRule.commentRule.content = new Selector("this", "html", null, "(<br>[^<>]*?<br>)", null);

        sites.add(new Site(39, "Gelbooru Post",
                "http://gelbooru.com/index.php?page=post&s=list&tags=all&pid={page:0:42}",
                "http://gelbooru.com/index.php?page=post&s=view&id={idCode:}",
                "http://gelbooru.com/index.php?page=post&s=list&tags={keyword:}&pid={page:0:42}",
                "http://gelbooru.com/index.php?page=account&s=login&code=00",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE + "|" + Site.FLAG_ONE_PIC_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://gelbooru.com/index.php?page=post&s=list&tags=all&pid={page:0:42}"));
        categories.add(new Category(3, "评级：安全", "http://gelbooru.com/index.php?page=post&s=list&tags=rating%3Asafe&pid={page:0:42}"));
        categories.add(new Category(4, "评级：存疑", "http://gelbooru.com/index.php?page=post&s=list&tags=rating%3Aquestionable&pid={page:0:42}"));
        categories.add(new Category(5, "评级：露骨", "http://gelbooru.com/index.php?page=post&s=list&tags=rating%3Aexplicit&pid={page:0:42}"));
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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("span.thumb", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://gelbooru.com//images/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

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
        indexRule.tags = new Selector("a > img", "attr", "title", " ([a-z_()]+)", null);

        galleryRule = new Rule();
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        galleryRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://img.xbooru.com//images/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("img#image", "attr", "src", "http://img.xbooru.com//(samples|images)/([^\"]*)/(sample_)?([^/]*)\\.", "http://img.xbooru.com/thumbnails/$2/thumbnail_$4.jpg");
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div[id^=c][style*='display']", null, null, null, null);
        galleryRule.commentRule.author = new Selector("a[href^='index']", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("b", "html", null, "Posted on (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})", null);
        galleryRule.commentRule.content = new Selector("this", "html", null, "(<br>[^<>]*?<br>)", null);

        sites.add(new Site(41, "Xbooru Post",
                "http://xbooru.com/index.php?page=post&s=list&pid={page:0:42}",
                "http://xbooru.com/index.php?page=post&s=view&id={idCode:}",
                "http://xbooru.com/index.php?page=post&s=list&tags={keyword:}&pid={page:0:42}",
                "http://xbooru.com/index.php?page=account&s=login&code=00",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_TITLE + "|" + Site.FLAG_ONE_PIC_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://xbooru.com/index.php?page=post&s=list&pid={page:0:42}"));
        categories.add(new Category(2, "评级：安全", "http://xbooru.com/index.php?page=post&s=list&tags=rating%3Asafe&pid={page:0:42}"));
        categories.add(new Category(3, "评级：存疑", "http://xbooru.com/index.php?page=post&s=list&tags=rating%3Aquestionable&pid={page:0:42}"));
        categories.add(new Category(4, "评级：露骨", "http://xbooru.com/index.php?page=post&s=list&tags=rating%3Aexplicit&pid={page:0:42}"));
        sites.get(sites.size() - 1).setCategories(categories);

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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("span.thumb", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a > img", "attr", "src", null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("img#image", "attr", "src", null, null);
        extraRule.pictureRule.highRes = new Selector("#post-view", "html", null, "\"(http://img.xbooru.com//images/[^\"]*?\\.(jpg|jpeg|png|gif|bmp))\"", null);

        sites.add(new Site(42, "Xbooru Pool",
                "http://xbooru.com/index.php?page=pool&s=list&pid={page:0:25}",
                "http://xbooru.com/index.php?page=pool&s=show&id={idCode:}",
                null,
                "http://gelbooru.com/index.php?page=account&s=login&code=00",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY));

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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.main-body p > a", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "href", null, null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("ol.commentlist > li.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("div.comment-author > img", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("div.comment-meta > span.comment-name", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.comment-meta > span.comment-date", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.comment-entry", "html", null, null, null);

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
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector(".image_thread .image_block", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("a.thumb_image", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a.thumb_image img", "attr", "src", null, null);

        sites.add(new Site(52, "E-shuushuu",
                "http://e-shuushuu.net/?page={page:1}",
                "http://e-shuushuu.net/{idCode:}",
                null,
                "http://e-shuushuu.net/",
                indexRule, galleryRule, null, null,
                Site.FLAG_ONE_PIC_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://e-shuushuu.net/?page={page:1}"));
        categories.add(new Category(2, "排行榜", "http://e-shuushuu.net/top.php?page={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);


        // Pixiv
        indexRule = new Rule();
        indexRule.item = new Selector("ul._image-items>li.image-item, section.ranking-item", null, null, null, null);
        indexRule.idCode = new Selector("a.work", "attr", "href", "illust_id=(\\d+)", null);
        indexRule.cover = new Selector("div._layout-thumbnail>img", null, null, "\"(http://[^\"]*?\\.(?:jpg|jpeg|png|bmp))\"", null);
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
        galleryRule.rating = new Selector("div.ui-expander-target > div.user-reaction", "html", null, "rated-count\".*?(\\d+).*?score-count\".*?(\\d+)", "$2/$1/2");
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("body", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("div#wrapper", "html", null, "\"(member_illust.php\\?mode=manga.*?|http://i\\d.pixiv.net/img-original/img/.*?\\.(?:jpg|jpeg|png|gif|bmp))\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("div.works_display div._layout-thumbnail > img", "attr", "src", "(http://.*?c)/\\d+x\\d+/(.*?\\.(?:jpg|jpeg|png|gif|bmp))", "$1/150x150/$2");
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("div._comment-items > div._comment-item", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("a.user-icon-container > img", "attr", "data-src", null, null);
        galleryRule.commentRule.author = new Selector("div.comment > div.meta > a.user-name", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.comment > div.meta > span.date", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.comment>div.body,div.comment>div.sticker-container", "html", null, null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.item = new Selector("div.item-container", null, null, null, null);
        extraRule.pictureRule.url = new Selector("img", "attr", "data-src", null, null);
        extraRule.pictureRule.thumbnail = new Selector("img", "attr", "data-src", "(http://.*?c)/\\d+x\\d+/(.*?\\.(?:jpg|jpeg|png|gif|bmp))", "$1/150x150/$2");

        sites.add(new Site(53, "Pixiv",
                "http://www.pixiv.net/new_illust.php?p={page:1}",
                "http://www.pixiv.net/member_illust.php?mode=medium&illust_id={idCode:}",
                "http://www.pixiv.net/search.php?word={keyword:}&p={page:1}",
                "https://accounts.pixiv.net/login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_SECOND_LEVEL_GALLERY + "|" + Site.FLAG_PRELOAD_GALLERY + "|" + Site.FLAG_WATERFALL_AS_GRID));
        categories = new ArrayList<>();
        categories.add(new Category(1, "首页", "http://www.pixiv.net/new_illust.php?p={page:1}"));
        categories.add(new Category(2, "收藏夹", "http://www.pixiv.net/bookmark.php?p={page:1}"));
        categories.add(new Category(3, "R18", "http://www.pixiv.net/new_illust_r18.php?p={page:1}"));
        categories.add(new Category(4, "综合今日排行榜", "http://www.pixiv.net/ranking.php?mode=daily&p={page:1}"));
        categories.add(new Category(5, "综合本周排行榜", "http://www.pixiv.net/ranking.php?mode=weekly&p={page:1}"));
        categories.add(new Category(6, "综合本月排行榜", "http://www.pixiv.net/ranking.php?mode=monthly&p={page:1}"));
        categories.add(new Category(7, "R18今日排行榜", "http://www.pixiv.net/ranking.php?mode=daily_r18&p={page:1}"));
        categories.add(new Category(8, "R18本周排行榜", "http://www.pixiv.net/ranking.php?mode=weekly_r18&p={page:1}"));
        categories.add(new Category(9, "R18G本周排行榜", "http://www.pixiv.net/ranking.php?mode=r18g&p={page:1}"));
        categories.add(new Category(10, "10000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=10000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(11, "5000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=5000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(12, "3000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=3000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(13, "1000users入り", "http://www.pixiv.net/search.php?s_mode=s_tag&word=1000users%E5%85%A5%E3%82%8A&p={page:1}"));
        categories.add(new Category(14, "Loli", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%E3%83%AD%E3%83%AA&p={page:1}"));
        categories.add(new Category(15, "東方", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%E6%9D%B1%E6%96%B9&p={page:1}"));
        categories.add(new Category(16, "艦これ", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%E8%89%A6%E3%81%93%E3%82%8C&p={page:1}"));
        categories.add(new Category(17, "LoveLive", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=LoveLive&p={page:1}"));
        categories.add(new Category(18, "VOCALOID", "http://www.pixiv.net/search.php?s_mode=s_tag_full&word=VOCALOID&p={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);
        sites.get(sites.size() - 1).cookie = "p_ab_id=4; _gat=1; PHPSESSID=19726569_cf8243e85368f6e8965c6e19068b4da5; device_token=0074d3631c53eff71393c60ac338f0ef; a_type=0; __utmt=1; __utma=235335808.1998756366.1474474879.1474475016.1474475016.1; __utmb=235335808.1.10.1474475016; __utmc=235335808; __utmz=235335808.1474475016.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmv=235335808.|2=login%20ever=yes=1^3=plan=normal=1^5=gender=male=1^6=user_id=19726569=1; _ga=GA1.2.1998756366.1474474879; _gat_UA-74360115-3=1";


        // 妹子图
        indexRule = new Rule();
        indexRule.item = new Selector("div.postlist > ul > li", null, null, null, null);
        indexRule.idCode = new Selector("span > a", "attr", "href", "http://www\\.mzitu\\.com/(\\d+)", null);
        indexRule.title = new Selector("span > a", "html", null, null, null);
        indexRule.cover = new Selector("a img", "attr", "data-original", null, null);
        indexRule.uploader = new Selector("span.view", "html", null, null, null);
        indexRule.datetime = new Selector("span.time", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.category = new Selector("div.main-meta > span > a", "html", null, null, null);
        galleryRule.tags = new Selector("div.main-tags > a[rel='tag']", "html", null, null, null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.main-image", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("p > a > img", "attr", "src", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("p > a > img", "attr", "src", "(.*)", "http://www.rosiyy.com/usr/themes/mm/timthumb.php?src=$1&h=210&w=150&zc=1&q=100");

        sites.add(new Site(54, "妹子图",
                "http://www.mzitu.com/page/{page:1}",
                "http://www.mzitu.com/{idCode:}/{page:1}",
                "http://www.mzitu.com/search/{keyword:}/page/{page:1}",
                null,
                indexRule, galleryRule, null, null, Site.FLAG_NO_TAG));

        categories = new ArrayList<>();
        categories.add(new Category(1, "最新", "http://www.mzitu.com/page/{page:1}"));
        categories.add(new Category(2, "最热", "http://www.mzitu.com/hot/page/{page:1}"));
        categories.add(new Category(3, "推荐", "http://www.mzitu.com/best/page/{page:1}"));
        categories.add(new Category(4, "性感妹子", "http://www.mzitu.com/xinggan/page/{page:1}"));
        categories.add(new Category(5, "日本妹子", "http://www.mzitu.com/japan/page/{page:1}"));
        categories.add(new Category(6, "台湾妹子", "http://www.mzitu.com/taiwan/page/{page:1}"));
        categories.add(new Category(7, "清纯妹子", "http://www.mzitu.com/mm/page/{page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);


        // Dribbble
        indexRule = new Rule();
        indexRule.item = new Selector("ol.dribbbles > li", null, null, null, null);
        indexRule.idCode = new Selector("div.dribbble-img > a.dribbble-link", "attr", "href", "shots/(.*)", null);
        indexRule.title = new Selector("div.dribbble-img > a.dribbble-link img", "attr", "alt", null, null);
        indexRule.cover = new Selector("div.dribbble-img > a.dribbble-link img", "attr", "src", null, null);
        indexRule.uploader = new Selector("span.attribution-user", "html", null, "<a class=\"url hoverable\".*?>([^<>\"]+)</a>", null);
        indexRule.datetime = new Selector("div.dribbble-shot > ul.tools", "html", null, "<li class=\"fav\">.*?>([0-9, ]+).*?<li class=\"cmnt\">.*?>([0-9, ]+).*?<li class=\"views\">.*?>([0-9, ]+).*?</li>", "✦$3    ✎$2    ❤$1");

        galleryRule = new Rule();
        galleryRule.tags = new Selector("ol#tags > li.tag > a > strong", "html", null, null, null);
        galleryRule.description = new Selector("div.shot-desc", "html", null, null, null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.single-img,ul.thumbs>li", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("this", "html", null, "href=\"(.*?)\"|src=\"([^\"]*?(?<!_1x)\\.(?:jpg|jpeg|png|gif|bmp))\"", "$1$2");
        galleryRule.pictureRule.thumbnail = new Selector("img", "attr", "src", null, null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("ol#comments > li.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("img.photo", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("h2 > a.url", "html", null, "(?:<img.*?>)?(.*)", null);
        galleryRule.commentRule.datetime = new Selector("p.comment-meta > a.posted", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.comment-body", "html", null, null, null);

        extraRule = new Rule();
        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector("#viewer img", "attr", "src", null, null);

        sites.add(new Site(55, "Dribbble",
                "https://dribbble.com/?page={page:1}&per_page=12",
                "https://dribbble.com/shots/{idCode:}",
                "https://dribbble.com/search?q={keyword:}&page={page:1}&per_page=12",
                "https://dribbble.com/session/new",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|" + Site.FLAG_PRELOAD_GALLERY
                        + "|" + Site.FLAG_JS_NEEDED_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "Popular", "https://dribbble.com/shots?page={page:1}&per_page=12"));
        categories.add(new Category(2, "Recent", "https://dribbble.com/shots?page={page:1}&per_page=12&sort=recent"));
        categories.add(new Category(3, "Most Viewed", "https://dribbble.com/shots?page={page:1}&per_page=12&sort=views"));
        categories.add(new Category(4, "Most Commented", "https://dribbble.com/shots?page={page:1}&per_page=12&sort=comments"));
        categories.add(new Category(5, "Debuts", "https://dribbble.com/shots?page={page:1}&per_page=12&list=debuts"));
        categories.add(new Category(6, "Team Shots", "https://dribbble.com/shots?page={page:1}&per_page=12&list=teams"));
        categories.add(new Category(7, "Playoffs", "https://dribbble.com/shots?page={page:1}&per_page=12&list=playoffs"));
        categories.add(new Category(8, "Rebounds", "https://dribbble.com/shots?page={page:1}&per_page=12&list=rebounds"));
        categories.add(new Category(9, "Animated GIFs", "https://dribbble.com/shots?page={page:1}&per_page=12&list=animated"));
        categories.add(new Category(10, "Shots with Attachments", "https://dribbble.com/shots?page={page:1}&per_page=12&list=attachments"));
        sites.get(sites.size() - 1).setCategories(categories);


        // UI中国
        indexRule = new Rule();
        indexRule.item = new Selector("ul.post > li", null, null, null, null);
        indexRule.idCode = new Selector("div.cover > a", "attr", "href", "detail/(\\d+).html", null);
        indexRule.title = new Selector("div.cover > a", "attr", "title", null, null);
        indexRule.cover = new Selector("div.cover > a > img", "attr", "data-original", null, null);
        indexRule.uploader = new Selector("div.info > p.user strong.name > em", "html", null, null, null);
        indexRule.category = new Selector("div.info > div.msg > span.classify", "html", null, null, null);
        indexRule.datetime = new Selector("div.info > div.msg", "html", null, "<em>([0-9 ]*)</em>.*?<em>([0-9 ]*)</em>.*?<em>([0-9 ]*)</em>", "✦$1    ✎$2    ❤$3");

        galleryRule = new Rule();
        galleryRule.tags = new Selector("ol#tags > li.tag > a > strong", "html", null, null, null);
        galleryRule.description = new Selector("div.works-cont", "html", null, "(<p.*/p>)", null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.works-cont>a:has(img),div.works-cont>p:has(img)", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("this", "html", null, "(?:href|src)=\"([^\"]*?)\"", null);
        galleryRule.pictureRule.thumbnail = new Selector("img", "attr", "src", null, null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("ul.comment-main > li.item", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("a[class^='avatar'] > img", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("div.comment-cont > .user > a", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.comment-cont > .user > time", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.comment-cont>div.retext,div.comment-cont>p.text", null, null, null, null);

        sites.add(new Site(56, "UI中国",
                "http://www.ui.cn/?p={page:1}#project",
                "http://www.ui.cn/detail/{idCode:}.html",
                "http://s.ui.cn/index.html?keywords={keyword:}&page={page:1}&type=project",
                "http://ui.cn/login.html",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_WATERFALL_AS_GRID));

        categories = new ArrayList<>();
        categories.add(new Category(1, "首页推荐", "http://www.ui.cn/?p={page:1}#project"));
        categories.add(new Category(2, "佳作推荐", "http://www.ui.cn/?t=share&p={page:1}#project"));
        categories.add(new Category(3, "最新作品", "http://www.ui.cn/?t=new&p={page:1}#project"));
        sites.get(sites.size() - 1).setCategories(categories);


        // AVMOO
        indexRule = new Rule();
        indexRule.item = new Selector("#waterfall > div.item", null, null, null, null);
        indexRule.idCode = new Selector("a.movie-box", "attr", "href", "movie/(.*)", null);
        indexRule.title = new Selector("div.photo-frame > img", "attr", "title", null, null);
        indexRule.cover = new Selector("div.photo-frame > img", "attr", "src", null, null);
        indexRule.category = new Selector("div.photo-info > span > date:nth-of-type(1)", "html", null, null, null);
        indexRule.datetime = new Selector("div.photo-info > span > date:nth-of-type(2)", "html", null, null, null);

        galleryRule = new Rule();
        //新规则支持获取TAG独有的URL
        //galleryRule.tags = new Selector("div.info > p > span.genre > a", "html", null, null, null);
        galleryRule.tagRule = new TagRule();
        galleryRule.tagRule.item = new Selector("div.info > p > span.genre", null, null, null, null);
        galleryRule.tagRule.title = new Selector("a", "html", null, null, null);
        galleryRule.tagRule.url = new Selector("a", "attr", "href", "(.*)", "$1/page/{page:1}");
        galleryRule.description = new Selector("div.info", "html", null, null, null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div#sample-waterfall > .sample-box", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("img", "attr", "src", null, null);

        sites.add(new Site(60, "AVMOO",
                "https://avmo.pw/cn/page/{page:1}",
                "https://avmo.pw/cn/movie/{idCode:}",
                "https://avmo.pw/cn/search/{keyword:}/page/{page:1}",
                null,
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_PRELOAD_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "全部", "https://avmo.pw/cn/page/{page:1}"));
        categories.add(new Category(2, "已发布", "https://avmo.pw/cn/released/page/{page:1}"));
        categories.add(new Category(3, "热门", "https://avmo.pw/cn/popular/page/{page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // AVMEMO
        indexRule = new Rule();
        indexRule.item = new Selector("#waterfall > div.item", null, null, null, null);
        indexRule.idCode = new Selector("a.main-movie-box", "attr", "href", "movie/(.*)", null);
        indexRule.title = new Selector("div.photo-frame > img", "attr", "title", null, null);
        indexRule.cover = new Selector("div.photo-frame > img", "attr", "src", null, null);
        indexRule.category = new Selector("div.photo-info > span > date:nth-of-type(1)", "html", null, null, null);
        indexRule.datetime = new Selector("div.photo-info > span > date:nth-of-type(2)", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.tagRule = new TagRule();
        galleryRule.tagRule.item = new Selector("div.info > p > span.genre", null, null, null, null);
        galleryRule.tagRule.title = new Selector("a", "html", null, null, null);
        galleryRule.tagRule.url = new Selector("a", "attr", "href", "(.*)", "$1/page/{page:1}");
        galleryRule.description = new Selector("div.info", "html", null, null, null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div#sample-waterfall > .movie-sample-box", null, null, null, null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "href", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("img", "attr", "src", null, null);

        sites.add(new Site(61, "AVMEMO",
                "https://avxo.pw/cn/page/{page:1}",
                "https://avxo.pw/cn/movie/{idCode:}",
                "https://avxo.pw/cn/search/{keyword:}/page/{page:1}",
                null,
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_PRELOAD_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "全部", "https://avxo.pw/cn/page/{page:1}"));
        categories.add(new Category(2, "已发布", "https://avxo.pw/cn/released/page/{page:1}"));
        categories.add(new Category(3, "热门", "https://avxo.pw/cn/popular/page/{page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // 半次元
        indexRule = new Rule();
        indexRule.item = new Selector("li>.imageCard,li.disc_one,li.l-work-thumbnail", null, null, null, null);
        indexRule.idCode = new Selector("a", "attr", "href", "/(\\w+/detail/\\d+/\\d+)", null);
        indexRule.cover = new Selector("img", "attr", "src", null, null);
        indexRule.title = new Selector("div.work-thumbnail__ft > a", "html", null, null, null);
        indexRule.uploader = new Selector("a.name>span,div.center.cut>span>a", "html", null, null, null);
        indexRule.category = new Selector(".imageCard__img span.countBadge", "html", null, null, null);
        indexRule.datetime = new Selector("div.mt10 > span", "html", null, "(\\d+)", "❤$1");

        galleryRule = new Rule();
        galleryRule.title = new Selector("article.post > header > div.post__title > h1", "html", null, null, null);
        galleryRule.uploader = new Selector(".l-detailUser-name > a", "html", null, null, null);
        galleryRule.category = new Selector("div.container > div.row", "html", null, "<div class=\"btn__text-wrap\">.*?<i></i>.*?赞&nbsp;\\((\\d+)\\).*?<div class=\"post__type post__info-group mb20\">.*?([^<>\"]+)</a>.*?(共\\d+P)", "$2　$3　❤$1");
        galleryRule.datetime = new Selector("article.post > header > div.post__info > div.post__type", "html", null, "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})", null);
        galleryRule.tags = new Selector("ul.tags>li.tag>a>div,div.post__role h2", null, null, "([^<>\"]+)(?:</div>|</a>)", null);
        galleryRule.description = new Selector("div.post__content,div.l-detail-no-right-to-see", "html", null, null, null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.post__content img.detail_std", null, null, null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "src", "(.*\\.(?:jpg|jpeg|png|gif|bmp))", "$1/2X3");
        galleryRule.pictureRule.url = new Selector("this", "attr", "src", null, null);
        galleryRule.pictureRule.highRes = new Selector("this", "attr", "src", "(.*\\.(?:jpg|jpeg|png|gif|bmp))", null);
        galleryRule.commentRule = new CommentRule();
        galleryRule.commentRule.item = new Selector("ul.publish__comment-list > li.comment", null, null, null, null);
        galleryRule.commentRule.avatar = new Selector("a.comment__avatar-img > img", "attr", "src", null, null);
        galleryRule.commentRule.author = new Selector("a.comment__user-name", "html", null, null, null);
        galleryRule.commentRule.datetime = new Selector("div.comment__right > div.minor", "html", null, null, null);
        galleryRule.commentRule.content = new Selector("div.comment__content", null, null, null, null);

        sites.add(new Site(62, "半次元",
                "http://bcy.net/illust/index/ajaxLoadHotIllust?n={page:0}",
                "http://bcy.net/{idCode:}",
                "http://bcy.net/search/all?k={keyword:}&p={page:1}",
                "http://bcy.net/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_PRELOAD_GALLERY + "|" + Site.FLAG_WATERFALL_AS_GRID));

        categories = new ArrayList<>();
        categories.add(new Category(1, "热门绘画", "http://bcy.net/illust/index/ajaxLoadHotIllust?n={page:0}"));
        categories.add(new Category(2, "精选绘画", "http://bcy.net/illust/discover?&p={page:1}"));
        categories.add(new Category(3, "最新同人", "http://bcy.net/illust/allfanart?&p={page:1}"));
        categories.add(new Category(4, "最新原创", "http://bcy.net/illust/allartwork?&{page:1}"));
        categories.add(new Category(5, "日排行榜", "http://bcy.net/illust/toppost100?type=lastday"));
        categories.add(new Category(6, "周排行榜", "http://bcy.net/illust/toppost100"));
        categories.add(new Category(7, "热门COS", "http://bcy.net/coser/index/ajaxLoadHotCos?n={page:0}"));
        categories.add(new Category(8, "精选COS", "http://bcy.net/coser/discover?&p={page:1}"));
        categories.add(new Category(9, "最新正片", "http://bcy.net/coser/allwork?&p={page:1}"));
        categories.add(new Category(10, "最新预告", "http://bcy.net/coser/allpre?&p={page:1}"));
        categories.add(new Category(11, "日排行榜", "http://bcy.net/coser/toppost100?type=lastday"));
        categories.add(new Category(12, "周排行榜", "http://bcy.net/coser/toppost100"));
        sites.get(sites.size() - 1).setCategories(categories);

        // 美女图片集
        indexRule = new Rule();
        indexRule.item = new Selector("div.album-item", null, null, null, null);
        indexRule.idCode = new Selector("h2 > a", "attr", "href", "album/(.*)", null);
        indexRule.cover = new Selector("div.album-grid > a.one-third", "attr", "photo", null, null);
        indexRule.title = new Selector("h2 > a", "html", null, null, null);
        indexRule.uploader = new Selector("p.desp > a", "html", null, null, null);
        indexRule.category = new Selector("p.desp", "html", null, "<code>(\\d+)</code>.*?<code>(\\d+)</code>", "$1张照片　浏览$2次");
        indexRule.datetime = new Selector("p.desp", "html", null, "(\\d{4}/\\d{2}/\\d{2})", null);

        galleryRule = new Rule();
        galleryRule.tagRule = new TagRule();
        galleryRule.tagRule.item = new Selector("span.tag", null, null, null, null);
        galleryRule.tagRule.title = new Selector("a", "html", null, null, null);
        galleryRule.tagRule.url = new Selector("a", "attr", "href", "(.*)", "$1?p={page:1}");
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("ul.gridview > li", null, null, null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a > img", null, null, "(?:src|data-original)=\"(.*?)\"", null);
        galleryRule.pictureRule.url = new Selector("a", "attr", "href", null, null);

        sites.add(new Site(63, "美女图片集",
                "http://www.girl-atlas.com/?p={page:1}",
                "http://www.girl-atlas.com/album/{idCode:}?display=2",
                null,
                "http://www.girl-atlas.com/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_PRELOAD_GALLERY));

        categories = new ArrayList<>();
        categories.add(new Category(1, "精华图片集", "http://www.girl-atlas.com/?p={page:1}"));
        categories.add(new Category(2, "最新图片集", "http://www.girl-atlas.com/index1?p={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // Nude-Atlas
        indexRule = new Rule();
        indexRule.item = new Selector("div#posts div.post-with-pic", null, null, null, null);
        indexRule.idCode = new Selector("a", "attr", "href", "blog/post/(.*)", null);
        indexRule.cover = new Selector("a > img", "attr", "src", null, null);
        indexRule.title = new Selector("a.post-title", "html", null, null, null);
        indexRule.uploader = new Selector("span.post_date", "html", null, "</i> (\\d+&nbsp;views)", null);
        indexRule.tags = new Selector("span.post-tag", "html", null, null, null);
        indexRule.datetime = new Selector("span.post-date", "html", null, "</i>(.*?)<i", null);

        galleryRule = new Rule();
        galleryRule.description = new Selector("div.post-body", "html", null, null, null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.post-pics > a", null, null, null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "href", null, null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "href", null, null);

        sites.add(new Site(64, "Nude-Atlas",
                "http://nude-atlas.com/blog/index?p={page:1}",
                "http://nude-atlas.com/blog/post/{idCode:}",
                null,
                "http://nude-atlas.com/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING));

        categories = new ArrayList<>();
        categories.add(new Category(1, "#ALL", "http://nude-atlas.com/blog/index?p={page:1}"));
        categories.add(new Category(2, "#CHINESE", "http://nude-atlas.com/blog/index?t=57beb41e5ca248101fb2332d&p={page:1}"));
        categories.add(new Category(3, "#JAPANESE", "http://nude-atlas.com/blog/index?t=57beb4e25ca248101fb2332e&p={page:1}"));
        categories.add(new Category(4, "#WESTERN", "http://nude-atlas.com/blog/index?t=57beb5345ca248101fb2332f&p={page:1}"));
        categories.add(new Category(5, "#RUSSIAN", "http://nude-atlas.com/blog/index?t=57bf2d5c5ca24815fa29e77c&p={page:1}"));
        categories.add(new Category(6, "#FRENCH", "http://nude-atlas.com/blog/index?t=57bf302e5ca24815fa29e781&p={page:1}"));
        categories.add(new Category(7, "#MIDDLE EASTERN", "http://nude-atlas.com/blog/index?t=57bf31935ca24815fa29e783&p={page:1}"));
        categories.add(new Category(8, "#EASTERN EUROPEAN", "http://nude-atlas.com/blog/index?t=57bf36f45ca24815fa29e788&p={page:1}"));
        categories.add(new Category(9, "#GERMAN", "http://nude-atlas.com/blog/index?t=57bf3beb5ca24815fa29e78e&p={page:1}"));
        categories.add(new Category(10, "#USA", "http://nude-atlas.com/blog/index?t=57c27e465ca248175753c287&p={page:1}"));
        categories.add(new Category(11, "#KOREAN", "http://nude-atlas.com/blog/index?t=57fb0df05ca24814954f5a2f&p={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // 宅男女神
        indexRule = new Rule();
        indexRule.item = new Selector("ul>li.galleryli,ul>li.igalleryli", null, null, null, null);
        indexRule.idCode = new Selector("a.galleryli_link,a.igalleryli_link", "attr", "href", "/g/(\\d*)", null);
        indexRule.cover = new Selector("a > img", "attr", "data-original", null, null);
        indexRule.title = new Selector("div.galleryli_title>a,div.igalleryli_title>a", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.tagRule = new TagRule();
        galleryRule.tagRule.item = new Selector("ul#utag > li", null, null, null, null);
        galleryRule.tagRule.title = new Selector("a", "html", null, null, null);
        galleryRule.tagRule.url = new Selector("a", "attr", "href", "(.*)", "$1{pageStr:{page:1}.html}");
        galleryRule.description = new Selector("div#ddesc", "html", null, null, null);
        galleryRule.category = new Selector("div#dinfo", "html", null, ">(\\d+)张.*?浏览了(.*?)次", "$1张照片　浏览$2次");
        galleryRule.datetime = new Selector("div#dinfo", "html", null, "(\\d{4}/\\d{2}/\\d{2})", null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("ul#hgallery img", null, null, null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "src", null, null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "src", null, null);

        sites.add(new Site(65, "宅男女神",
                "http://www.zngirls.com/gallery/{pageStr:{page:1}.html}",
                "http://www.zngirls.com/g/{idCode:}/{page:1}.html",
                null,
                null,
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_PRELOAD_GALLERY));

        // 花瓣网
        indexRule = new Rule();
        indexRule.item = new Selector("#waterfall>div:not(.google):not(.ad), #recommend_container .recommend-imgbox", null, null, null, null);
        indexRule.idCode = new Selector("a.img,a.link", "attr", "href", "/(.*)/", null);
        indexRule.cover = new Selector("a>img", "attr", "src", null, null);
        indexRule.title = new Selector("p.description,div.over>h3", "html", null, "([^<>]*)", null);
        indexRule.uploader = new Selector("div.attribution a.author,a.BoardUserUrl", "html", null, null, null);
        indexRule.category = new Selector("div.attribution div.line:nth-child(2) > a.x", "html", null, null, null);
        indexRule.datetime = new Selector("div.pin-count", "html", null, "(\\d+)", "$1 采集");
//        indexRule.item = new Selector("script", "html", null, "app\\.page\\[\"(?:recommends|pins)\"\\].*?(\\[\\{.*?\\}\\]);|\"pins\":(\\[.*?\\}\\])\\};", "$1$2");
//        indexRule.item.path = "$[?(@.pin_id||@.board_id)]";
//        indexRule.idCode = new Selector("$['pin_id','board_id']", null, null, "\"(pin|board)_id\":(\\d+)", "$1s/$2", true);
//        indexRule.cover = new Selector("$.['cover','file'].key", null, null, "(.*)", "http://img.hb.aicdn.com/$1_fw236", true);
//        indexRule.title = new Selector("$.['title','raw_text']", null, null, "\"(?:title|raw_text)\":\"(.*?)\"", null, true);
//        indexRule.uploader = new Selector("$.['user','source']", null, null, "\"(?:username|source)\":\"(.*?)\"", null, true);
//        indexRule.category = new Selector("$.board.title", null, null, null, null, true);
//        indexRule.datetime = new Selector("$.pin_count", null, null, "(\\d+)", "$1 采集", true);

        extraRule = new Rule();
        extraRule.item = new Selector("#recommend_container .recommend-infobox:not(.user)", null, null, null, null);
        extraRule.idCode = new Selector("h2 > a", "attr", "href", "/(.*)/", null);
        extraRule.title = new Selector("h2 > a", "html", null, null, null);
        extraRule.uploader = new Selector("span > a", "html", null, null, null);
        extraRule.category = new Selector("p > span:nth-child(2)", "html", null, null, null);
        extraRule.datetime = new Selector("p > span:nth-child(1)", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("#waterfall>.pin,.main-image", null, null, null, null);
        galleryRule.pictureRule.id = new Selector("this", "attr", "data-id", null, null);
        galleryRule.pictureRule.thumbnail = new Selector("a.img>img,.main-image img", "attr", "src", null, null);
        galleryRule.pictureRule.url = new Selector("a.img,.main-image img", null, null, "(?:href|src)=\"(.*?)\"", null);

        extraRule.pictureRule = new PictureRule();
        extraRule.pictureRule.url = new Selector(".main-image img, body>img", "attr", "src", null, null);
        extraRule.pictureRule.highRes = new Selector(".main-image img, body>img", "attr", "src", "(.*)_fw\\d{3}$", null);

        sites.add(new Site(66, "花瓣网",
                "http://huaban.com/?page={page:1}",
                "http://huaban.com/{idCode:}/{pageStr:?max={page:minid}&limit=20&wfl=1}",
                "http://huaban.com/search/?q={keyword:}&page={page:1}&per_page=20&wfl=1",
                "http://huaban.com/login",
                indexRule, galleryRule, null, extraRule,
                Site.FLAG_NO_RATING + "|"
                        + Site.FLAG_JS_NEEDED_ALL + "|"
                        + Site.FLAG_EXTRA_INDEX_INFO + "|"
                        + Site.FLAG_SINGLE_PAGE_BIG_PICTURE + "|"
                        + Site.FLAG_WATERFALL_AS_LIST));

        categories = new ArrayList<>();
        categories.add(new Category(categories.size() + 1, "发现", "http://huaban.com/?page={page:1}"));
        categories.add(new Category(categories.size() + 1, "最新", "http://huaban.com/all/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "UI/UX-采集", "http://huaban.com/favorite/web_app_icon/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "UI/UX-画板", "http://huaban.com/boards/favorite/web_app_icon/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "平面-采集", "http://huaban.com/favorite/design/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "平面-画板", "http://huaban.com/boards/favorite/design/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "插画/漫画-采集", "http://huaban.com/favorite/illustration/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "插画/漫画-画板", "http://huaban.com/boards/favorite/illustration/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "家居/家装-采集", "http://huaban.com/favorite/home/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "家居/家装-画板", "http://huaban.com/boards/favorite/home/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "女装/搭配-采集", "http://huaban.com/favorite/apparel/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "女装/搭配-画板", "http://huaban.com/boards/favorite/apparel/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "男装/风尚-采集", "http://huaban.com/favorite/men/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "男装/风尚-画板", "http://huaban.com/boards/favorite/men/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "婚礼-采集", "http://huaban.com/favorite/wedding_events/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "婚礼-画板", "http://huaban.com/boards/favorite/wedding_events/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "工业设计-采集", "http://huaban.com/favorite/industrial_design/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "工业设计-画板", "http://huaban.com/boards/favorite/industrial_design/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "摄影-采集", "http://huaban.com/favorite/photography/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "摄影-画板", "http://huaban.com/boards/favorite/photography/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "造型/美妆-采集", "http://huaban.com/favorite/modeling_hair/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "造型/美妆-画板", "http://huaban.com/boards/favorite/modeling_hair/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "美食-采集", "http://huaban.com/favorite/food_drink/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "美食-画板", "http://huaban.com/boards/favorite/food_drink/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "旅行-采集", "http://huaban.com/favorite/travel_places/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "旅行-画板", "http://huaban.com/boards/favorite/travel_places/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "手工/布艺-采集", "http://huaban.com/favorite/diy_crafts/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "手工/布艺-画板", "http://huaban.com/boards/favorite/diy_crafts/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "健身/舞蹈-采集", "http://huaban.com/favorite/fitness/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "健身/舞蹈-画板", "http://huaban.com/boards/favorite/fitness/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "儿童-采集", "http://huaban.com/favorite/kids/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "儿童-画板", "http://huaban.com/boards/favorite/kids/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "宠物-采集", "http://huaban.com/favorite/pets/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "宠物-画板", "http://huaban.com/boards/favorite/pets/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "美图-采集", "http://huaban.com/favorite/quotes/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "美图-画板", "http://huaban.com/boards/favorite/quotes/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "明星-采集", "http://huaban.com/favorite/people/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "明星-画板", "http://huaban.com/boards/favorite/people/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "美女-采集", "http://huaban.com/favorite/beauty/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "美女-画板", "http://huaban.com/boards/favorite/beauty/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "礼物-采集", "http://huaban.com/favorite/desire/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "礼物-画板", "http://huaban.com/boards/favorite/desire/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "极客-采集", "http://huaban.com/favorite/geek/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "极客-画板", "http://huaban.com/boards/favorite/geek/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "动漫-采集", "http://huaban.com/favorite/anime/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "动漫-画板", "http://huaban.com/boards/favorite/anime/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "建筑设计-采集", "http://huaban.com/favorite/architecture/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "建筑设计-画板", "http://huaban.com/boards/favorite/architecture/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "人文艺术-采集", "http://huaban.com/favorite/art/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "人文艺术-画板", "http://huaban.com/boards/favorite/art/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "数据图-采集", "http://huaban.com/favorite/data_presentation/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "数据图-画板", "http://huaban.com/boards/favorite/data_presentation/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "游戏-采集", "http://huaban.com/favorite/games/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "游戏-画板", "http://huaban.com/boards/favorite/games/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "汽车/摩托-采集", "http://huaban.com/favorite/cars_motorcycles/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "汽车/摩托-画板", "http://huaban.com/boards/favorite/cars_motorcycles/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "电影/图书-采集", "http://huaban.com/favorite/film_music_books/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "电影/图书-画板", "http://huaban.com/boards/favorite/film_music_books/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "生活百科-采集", "http://huaban.com/favorite/tips/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "生活百科-画板", "http://huaban.com/boards/favorite/tips/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "教育-采集", "http://huaban.com/favorite/education/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "教育-画板", "http://huaban.com/boards/favorite/education/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "运动-采集", "http://huaban.com/favorite/sports/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "运动-画板", "http://huaban.com/boards/favorite/sports/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "搞笑-采集", "http://huaban.com/favorite/funny/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        categories.add(new Category(categories.size() + 1, "搞笑-画板", "http://huaban.com/boards/favorite/funny/{pageStr:?max={page:minid}&limit=20&wfl=1}"));
        sites.get(sites.size() - 1).setCategories(categories);

        // Pinterest
        indexRule = new Rule();
        indexRule.item = new Selector(".GridItems > .item", null, null, null, null);
        indexRule.idCode = new Selector(".pinHolder > a", "attr", "href", "/(.*)/", null);
        indexRule.cover = new Selector("div.Image img", "attr", "src", null, null);
        indexRule.title = new Selector("h3.richPinGridTitle,.pinMeta>.pinDescription", "html", null, null, null);
        indexRule.uploader = new Selector("div.pinCreditNameTitleWrapper>.creditName", "html", null, null, null);
        indexRule.category = new Selector("div.pinCreditNameTitleWrapper>.creditTitle", "html", null, null, null);
        indexRule.datetime = new Selector("em.repinCountSmall", "html", null, "(\\d+)", "$1 \uD83D\uDCCC");

        galleryRule = new Rule();
        galleryRule.description = new Selector(".userActivity .userNote", "html", null, null, null);
        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector(".imageContainer", null, null, null, null);
        galleryRule.pictureRule.thumbnail = new Selector("img.pinImage", "attr", "src", null, null);
        galleryRule.pictureRule.url = new Selector("img.pinImage", "attr", "src", null, null);

        sites.add(new Site(67, "Pinterest",
                "https://www.pinterest.com/",
                "https://www.pinterest.com/{idCode:}",
                "http://huaban.com/search/?q={keyword:}&page={page:1}&per_page=20&wfl=1",
                "https://www.pinterest.com/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_JS_NEEDED_ALL + "|" + Site.FLAG_WATERFALL_AS_LIST + "|" + Site.FLAG_JS_SCROLL));

        categories = new ArrayList<>();
        categories.add(new Category(categories.size() + 1, "HomePage", "https://www.pinterest.com/"));
        categories.add(new Category(categories.size() + 1, "Popular", "https://www.pinterest.com/categories/popular/"));
        categories.add(new Category(categories.size() + 1, "Everything", "https://www.pinterest.com/categories/everything/"));
        categories.add(new Category(categories.size() + 1, "Gifts", "https://www.pinterest.com/categories/gifts/"));
        categories.add(new Category(categories.size() + 1, "Videos", "https://www.pinterest.com/categories/videos/"));
        categories.add(new Category(categories.size() + 1, "Animals and pets", "https://www.pinterest.com/categories/animals/"));
        categories.add(new Category(categories.size() + 1, "Architecture", "https://www.pinterest.com/categories/architecture/"));
        categories.add(new Category(categories.size() + 1, "Art", "https://www.pinterest.com/categories/art/"));
        categories.add(new Category(categories.size() + 1, "Cars and motorcycles", "https://www.pinterest.com/categories/cars_motorcycles/"));
        categories.add(new Category(categories.size() + 1, "Celebrities", "https://www.pinterest.com/categories/celebrities/"));
        categories.add(new Category(categories.size() + 1, "DIY and crafts", "https://www.pinterest.com/categories/diy_crafts/"));
        categories.add(new Category(categories.size() + 1, "Design", "https://www.pinterest.com/categories/design/"));
        categories.add(new Category(categories.size() + 1, "Education", "https://www.pinterest.com/categories/education/"));
        categories.add(new Category(categories.size() + 1, "Film, music and books", "https://www.pinterest.com/categories/film_music_books/"));
        categories.add(new Category(categories.size() + 1, "Food and drink", "https://www.pinterest.com/categories/food_drink/"));
        categories.add(new Category(categories.size() + 1, "Gardening", "https://www.pinterest.com/categories/gardening/"));
        categories.add(new Category(categories.size() + 1, "Geek", "https://www.pinterest.com/categories/geek/"));
        categories.add(new Category(categories.size() + 1, "Hair and beauty", "https://www.pinterest.com/categories/hair_beauty/"));
        categories.add(new Category(categories.size() + 1, "Health and fitness", "https://www.pinterest.com/categories/health_fitness/"));
        categories.add(new Category(categories.size() + 1, "History", "https://www.pinterest.com/categories/history/"));
        categories.add(new Category(categories.size() + 1, "Holidays and events", "https://www.pinterest.com/categories/holidays_events/"));
        categories.add(new Category(categories.size() + 1, "Home decor", "https://www.pinterest.com/categories/home_decor/"));
        categories.add(new Category(categories.size() + 1, "Humor", "https://www.pinterest.com/categories/humor/"));
        categories.add(new Category(categories.size() + 1, "Illustrations and posters", "https://www.pinterest.com/categories/illustrations_posters/"));
        categories.add(new Category(categories.size() + 1, "Kids and parenting", "https://www.pinterest.com/categories/kids/"));
        categories.add(new Category(categories.size() + 1, "Men's fashion", "https://www.pinterest.com/categories/mens_fashion/"));
        categories.add(new Category(categories.size() + 1, "Outdoors", "https://www.pinterest.com/categories/outdoors/"));
        categories.add(new Category(categories.size() + 1, "Photography", "https://www.pinterest.com/categories/photography/"));
        categories.add(new Category(categories.size() + 1, "Products", "https://www.pinterest.com/categories/products/"));
        categories.add(new Category(categories.size() + 1, "Quotes", "https://www.pinterest.com/categories/quotes/"));
        categories.add(new Category(categories.size() + 1, "Science and nature", "https://www.pinterest.com/categories/science_nature/"));
        categories.add(new Category(categories.size() + 1, "Sports", "https://www.pinterest.com/categories/sports/"));
        categories.add(new Category(categories.size() + 1, "Tattoos", "https://www.pinterest.com/categories/tattoos/"));
        categories.add(new Category(categories.size() + 1, "Technology", "https://www.pinterest.com/categories/technology/"));
        categories.add(new Category(categories.size() + 1, "Travel", "https://www.pinterest.com/categories/travel/"));
        categories.add(new Category(categories.size() + 1, "Weddings", "https://www.pinterest.com/categories/weddings/"));
        categories.add(new Category(categories.size() + 1, "Women's fashion", "https://www.pinterest.com/categories/womens_fashion/"));
        sites.get(sites.size() - 1).setCategories(categories);


        // Tumblr
        indexRule = new Rule();
        indexRule.item = new Selector(".is_photo,.is_photoset,.is_video", null, null, null, null);
        indexRule.idCode = new Selector(".post_header", "html", null, "(http[^\"]*?tumblr.com/post/[^\"]*)", null);
        indexRule.cover = new Selector(".post_media img,.video_poster,.vjs-poster", null, null, "(?:background-image.*?(http.*?\\.(?:jpg|jpeg|png|gif|bmp))|(http[^\"]*?_\\d{3,4}\\.(?:jpg|jpeg|png|gif|bmp)))", "$1$2");
        indexRule.title = new Selector(".post_header", "html", null, "tumblr.com/[^\"]*?/[^\"]*?/([^\"/]*)", null);
        indexRule.uploader = new Selector(".post_header a.post_info_link, .post_header .post-info-tumblelog>a", "html", null, null, null);
        indexRule.datetime = new Selector("span.note_link_current", "html", null, null, null);
        indexRule.tags = new Selector(".post_tags .post_tag", "html", null, "([^#]+)", null);
        indexRule.description = new Selector(".post_content_inner,.reblog-list", "html", null, null, null);
        indexRule.pictureRule = new PictureRule();
        indexRule.pictureRule.item = new Selector("[data-lightbox],img.post_media_photo,a.photoset_photo", null, null, null, null);
        indexRule.pictureRule.thumbnail = new Selector("this", null, null, "(?:src=|\"low_res\":)\"(http.*?\\.(?:jpg|jpeg|png|gif|bmp))\"", null);
        indexRule.pictureRule.url = new Selector("this", null, null, "(?:href=|src=|\"high_res\":)\"(http[^\"]*?\\.(?:jpg|jpeg|png|gif|bmp))\"", null);
        indexRule.videoRule = new VideoRule();
        indexRule.videoRule.item = new Selector(".video_embed,.dockable_video_embed", null, null, null, null);
        indexRule.videoRule.content = new Selector("script.embed_source,video>source", null, null, "src=\"(http.*)(?:/480)|(http.*)\"", "$1$2");

//        galleryRule = new Rule();
//        galleryRule.pictureRule = new PictureRule();
//        galleryRule.pictureRule.item = new Selector(":not(.related-posts-wrapper)>*>*>*>*>.post-content img,.posts img,.photo-slideshow img,#posts img,.photo-stage img,.stat-photo img,#content img,.photo > img,.photo > a > img", null, null, "\"http[^\"]*?_\\d{3,4}\\.(?:jpg|jpeg|png|gif|bmp)\"", null);
//        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "src", "(http[^\"]*?)_\\d{3,4}(\\.(?:jpg|jpeg|png|gif|bmp))", "$1_400$2");
//        galleryRule.pictureRule.url = new Selector("this", "attr", "src", "(http[^\"]*?)_\\d{3,4}(\\.(?:jpg|jpeg|png|gif|bmp))", "$1_1280$2");
//        galleryRule.videoRule = new VideoRule();
//        galleryRule.videoRule.item = new Selector("#posts", null, null, null, null);
//        galleryRule.videoRule.content = new Selector("iframe", "attr", "src", null, null);

        sites.add(new Site(68, "Tumblr",
                "https://www.tumblr.com/dashboard",
                "{idCode:}",
                "https://www.tumblr.com/search/{keyword:}",
                "https://www.tumblr.com/login",
                indexRule, null, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_JS_NEEDED_INDEX + "|" + Site.FLAG_WATERFALL_AS_LIST
                        + "|" + Site.FLAG_JS_SCROLL));

        categories = new ArrayList<>();
        categories.add(new Category(categories.size() + 1, "主页", "https://www.tumblr.com/dashboard"));
        categories.add(new Category(categories.size() + 1, "发现-热门", "https://www.tumblr.com/explore/trending"));
        categories.add(new Category(categories.size() + 1, "发现-官博精选", "https://www.tumblr.com/explore/staff-picks"));
        categories.add(new Category(categories.size() + 1, "发现-图片", "https://www.tumblr.com/explore/photos"));
        categories.add(new Category(categories.size() + 1, "发现-动图", "https://www.tumblr.com/explore/gifs"));
        categories.add(new Category(categories.size() + 1, "发现-视频", "https://www.tumblr.com/explore/video"));
        sites.get(sites.size() - 1).setCategories(categories);

        // H-Anime
        indexRule = new Rule();
        indexRule.item = new Selector("$.hits", null, null, null, null, true);
        indexRule.idCode = new Selector("$.slug", null, null, null, null, true);
        indexRule.cover = new Selector("$.cover_url", null, null, "(http[^\"]*?\\.(?:jpg|jpeg|png|gif|bmp))", null, true);
        indexRule.title = new Selector("$.name", null, null, null, null, true);
        indexRule.tags = new Selector("$.tags", null, null, null, null, true);
        indexRule.uploader = new Selector("$.brand", null, null, null, null, true);
        indexRule.category = new Selector("$", null, null, "\"views\":(\\d+).*?\"favorites_count\":(\\d+)", "✦$1    ❤$2", true);
        indexRule.description = new Selector("$.description", null, null, null, null, true);
        indexRule.datetime = new Selector("$.created_at", null, null, null, null, true);

        galleryRule = new Rule();
        galleryRule.datetime = new Selector("div.details>div.detail:nth-child(5)>div.data", "html", null, null, null);

        galleryRule.pictureRule = new PictureRule();
        galleryRule.pictureRule.item = new Selector("div.section div.storyboard-thumbnail:first-child", null, null, null, null);
        galleryRule.pictureRule.thumbnail = new Selector("this", "attr", "style", "(http[^\"]*?\\.(?:jpg|jpeg|png|gif|bmp))", null);
        galleryRule.pictureRule.url = new Selector("this", "attr", "style", "(http[^\"]*?\\.(?:jpg|jpeg|png|gif|bmp))", null);
        galleryRule.videoRule = new VideoRule();
        galleryRule.videoRule.item = new Selector("#video_container", null, null, null, null);
        galleryRule.videoRule.content = new Selector("#video_element", "attr", "src", "(.*)", "https://hanime.tv$1");

        sites.add(new Site(69, "H-Anime",
                "https://solarian.hanime.tv/do_search?q=&search_by=all&search_from={page:0:48}&page_size=48",
                "https://hanime.tv/hentai-videos/{idCode:}",
                "https://solarian.hanime.tv/do_search?q={keyword:}&search_by=all&search_from={page:0:48}&page_size=48",
                "https://hanime.tv/log-in",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_POST_INDEX + "|" + Site.FLAG_JS_NEEDED_GALLERY + "|" + Site.FLAG_WATERFALL_AS_GRID));
        categories = new ArrayList<>();
        categories.add(new Category(categories.size() + 1, "全部", "https://solarian.hanime.tv/do_search?q=&search_by=all&search_from={page:0:48}&page_size=48"));
        categories.add(new Category(categories.size() + 1, "最多观看", "https://solarian.hanime.tv/do_search?q=&search_by=all&sort_by=views&search_from={page:0:48}&page_size=48"));
        categories.add(new Category(categories.size() + 1, "最多收藏", "https://solarian.hanime.tv/do_search?q=&search_by=all&sort_by=favorites_count&search_from={page:0:48}&page_size=48"));
        sites.get(sites.size() - 1).setCategories(categories);

        // Hentai Play
        indexRule = new Rule();
        indexRule.item = new Selector(".loop-content>div>div.item", null, null, null, null);
        indexRule.idCode = new Selector("div.thumb>a.clip-link", "attr", "href", "hentaiplay.net/(.*)", null);
        indexRule.cover = new Selector("div.thumb img", "attr", "src", null, null);
        indexRule.title = new Selector("div.data .entry-title>a", "html", null, null, null);
        indexRule.uploader = new Selector("div.data>.entry-meta>.author>a", "html", null, null, null);
        indexRule.datetime = new Selector("div.data>.entry-meta>time", "html", null, null, null);
        indexRule.tagRule = new TagRule();
        indexRule.tagRule.item = new Selector(".new-wrapper>a", null, null, null, null);
        indexRule.tagRule.title = new Selector("this", "attr", "title", null, null);
        indexRule.tagRule.url = new Selector("this", "attr", "href", "(.*)", "$1/page/{page:1}/");
        indexRule.description = new Selector("div.data .entry-summary", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.videoRule = new VideoRule();
        galleryRule.videoRule.item = new Selector("#Source-1,#Source-2,#Source-3", null, null, "file: 'http://hentaiplanet.info|<iframe", null);
        galleryRule.videoRule.content = new Selector("script,iframe", null, null, "(http[^\"']*?\\.(?:mp4|webm|ogg))", null);

        sites.add(new Site(70, "Hentai Play",
                "http://hentaiplay.net/hentai/episodes/new-releases/page/{page:1}/",
                "http://hentaiplay.net/{idCode:}",
                "http://hentaiplay.net/page/{page:1}/?s={keyword:}",
                "http://hentaiplay.net/wp-login.php",
                indexRule, galleryRule, null, null,
                Site.FLAG_NO_RATING + "|" + Site.FLAG_WATERFALL_AS_GRID));
        categories = new ArrayList<>();
        categories.add(new Category(categories.size() + 1, "最新", "http://hentaiplay.net/hentai/episodes/new-releases/page/{page:1}/"));
        categories.add(new Category(categories.size() + 1, "英语字幕", "http://hentaiplay.net/hentai/episodes/english-subbed/page/{page:1}/"));
        categories.add(new Category(categories.size() + 1, "无码", "http://hentaiplay.net/hentai/episodes/uncensored/page/{page:1}/"));
        sites.get(sites.size() - 1).setCategories(categories);

        // Pornhub
        indexRule = new Rule();
        indexRule.item = new Selector(".videos>.videoBox", null, null, null, null);
        indexRule.idCode = new Selector("div.phimage>a", "attr", "href", "viewkey=(.*)", null);
        indexRule.cover = new Selector("div.img img", "attr", "data-mediumthumb", null, null);
        indexRule.title = new Selector("span.title>a", "attr", "title", null, null);
        indexRule.uploader = new Selector("div.img>.marker-overlays", "html", null, ">(.*?)</var>.*?>(.*?)</span>", "$1 $2");
        indexRule.category = new Selector("span.views>var", "html", null, "(.*)", "$1 views");
        indexRule.rating = new Selector(".rating-container>.value", "html", null, "(\\d+)%", "$1/20");
        indexRule.datetime = new Selector("var.added", "html", null, null, null);

        galleryRule = new Rule();
        galleryRule.tagRule = new TagRule();
        galleryRule.tagRule.item = new Selector(".categoriesWrapper>a", null, null, null, null);
        galleryRule.tagRule.title = new Selector("this", "html", null, null, null);
        galleryRule.tagRule.url = new Selector("this", "attr", "href", "(.*)", "$1&page={page:1}");
        galleryRule.videoRule = new VideoRule();
        galleryRule.videoRule.item = new Selector("#player script", null, null, "player_quality_\\d{3}p", null);
        galleryRule.videoRule.content = new Selector("this", "html", null, "player_quality_(?:720|480)p.*?'(http.*?)';", null);

        sites.add(new Site(71, "Pornhub",
                "http://www.pornhub.com/video?page={page:1}",
                "http://www.pornhub.com/view_video.php?viewkey={idCode:}",
                "http://www.pornhub.com/video/search?search={keyword:}&page={page:1}",
                "http://www.pornhub.com/login",
                indexRule, galleryRule, null, null,
                Site.FLAG_PRELOAD_GALLERY + "|" + Site.FLAG_WATERFALL_AS_GRID));
        categories = new ArrayList<>();
        categories.add(new Category(categories.size() + 1, "首页", "http://www.pornhub.com/video?page={page:1}"));
        categories.add(new Category(categories.size() + 1, "推荐", "http://www.pornhub.com/recommended?page={page:1}"));
        categories.add(new Category(categories.size() + 1, "最火-日本", "http://www.pornhub.com/video?o=ht&cc=jp&page={page:1}"));
        categories.add(new Category(categories.size() + 1, "最火-全球", "http://www.pornhub.com/video?o=ht&page={page:1}"));
        categories.add(new Category(categories.size() + 1, "最高评分", "http://www.pornhub.com/video?o=tr&page={page:1}"));
        categories.add(new Category(categories.size() + 1, "最多浏览", "http://www.pornhub.com/video?o=mv&page={page:1}"));
        sites.get(sites.size() - 1).setCategories(categories);


        return sites;
    }
}
