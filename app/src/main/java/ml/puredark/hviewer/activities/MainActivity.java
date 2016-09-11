package ml.puredark.hviewer.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.adapters.CategoryAdapter;
import ml.puredark.hviewer.adapters.MySearchAdapter;
import ml.puredark.hviewer.adapters.SiteAdapter;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.customs.AppBarStateChangeListener;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.fragments.CollectionFragment;
import ml.puredark.hviewer.fragments.MyFragment;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.holders.DownloadTaskHolder;
import ml.puredark.hviewer.holders.SiteHolder;

import static java.security.AccessController.getContext;
import static ml.puredark.hviewer.HViewerApplication.temp;


public class MainActivity extends AnimationActivity {
    private static int RESULT_ADD_SITE = 1;
    private static int RESULT_MODIFY_SITE = 2;
    private static int RESULT_LOGIN = 3;

    @BindView(R.id.content)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_search)
    FloatingActionButton fabSearch;

    @BindView(R.id.nav_header_view)
    LinearLayout navHeaderView;
    @BindView(R.id.rv_site)
    RecyclerView rvSite;

    @BindView(R.id.rv_category)
    RecyclerView rvCategory;

    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    private SiteAdapter siteAdapter;
    private CategoryAdapter categoryAdapter;

    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

    //记录当前加载的是哪个Fragment
    private MyFragment currFragment;

    //当前搜索的查询关键字
    private String currQuery;
    private boolean isSuggestionEmpty = true;

    private SiteHolder siteHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MDStatusBarCompat.setCollapsingToolbar(this, coordinatorLayout, appBar, backdrop, toolbar);

        // User interface
        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);

        siteHolder = new SiteHolder(this);

        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchView.getLayoutParams();
            lp.topMargin = MDStatusBarCompat.getStatusBarHeight(this);
            searchView.setLayoutParams(lp);
        }


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //appbar折叠时显示搜索按钮和搜索框，否则隐藏
        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                int size = toolbar.getMenu().size();
                if (state == State.COLLAPSED) {
                    if (size > 1)
                        toolbar.getMenu().getItem(size - 1).setVisible(true);
                    searchView.animate().alpha(1f).setDuration(300);
                } else {
                    if (size > 1)
                        toolbar.getMenu().getItem(size - 1).setVisible(false);
                    searchView.animate().alpha(0f).setDuration(300);
                }
            }
        });

        initSearchSuggestions();

        searchView.setSubmitOnClick(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                currQuery = keyword;
                HViewerApplication.searchHistoryHolder.addSearchHistory(keyword);
                if (!"".equals(keyword) && currFragment != null)
                    currFragment.onSearch(keyword);
                searchView.setSuggestions(new String[0]);
                isSuggestionEmpty = true;
                searchView.clearFocus();
                searchView.hideKeyboard(coordinatorLayout);
                searchView.dismissSuggestions();
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (isSuggestionEmpty)
                    initSearchSuggestions();
                currQuery = newText;
                return false;
            }
        });

        List<Site> sites = siteHolder.getSites();

//        sites.clear();
//
//        Rule indexRule = new Rule();
//        indexRule.item = new Selector("#ig .ig", null, null, null, null);
//        indexRule.idCode = new Selector("td.ii a", "attr", "href", "/g/(.*)", null);
//        indexRule.title = new Selector("table.it tr:eq(0) a", "html", null, null, null);
//        indexRule.uploader = new Selector("table.it tr:eq(1) td:eq(1)", "html", null, "(by .*)", null);
//        indexRule.cover = new Selector("td.ii img", "attr", "src", null, null);
//        indexRule.category = new Selector("table.it tr:eq(2) td:eq(1)", "html", null, null, null);
//        indexRule.datetime = new Selector("table.it tr:eq(1) td:eq(1)", "html", null, "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})", null);
//        indexRule.rating = new Selector("table.it tr:eq(4) td:eq(1)", "html", null, null, null);
//        indexRule.tags = new Selector("table.it tr:eq(3) td:eq(1)", "html", null, "([a-zA-Z0-9 -]+)", null);
//
//        Rule galleryRule = new Rule();
//        galleryRule.item = new Selector("#gh .gi", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("a img", "attr", "src", null, null);
//
//        Selector pic = new Selector("img#sm", "attr", "src", null, null);
//
//        sites.add(new Site(1, "Lofi.E-hentai",
//                "http://lofi.e-hentai.org/?page={page:0}",
//                "http://lofi.e-hentai.org/g/{idCode:}/{page:0}",
//                "http://lofi.e-hentai.org/?f_search={keyword:}&page={page:0}",
//                "https://forums.e-hentai.org/index.php?act=Login",
//                indexRule, galleryRule, null, pic, null));
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("table.itg tr.gtr0,tr.gtr1", null, null, null, null);
//        indexRule.idCode = new Selector("td.itd div div.it5 a", "attr", "href", "/g/(.*)", null);
//        indexRule.title = new Selector("td.itd div div.it5 a", "html", null, null, null);
//        indexRule.uploader = new Selector("td.itu div a", "html", null, null, null);
//        indexRule.cover = new Selector("td.itd div div.it2", "html", null, "(//|inits?~)(.*?org)[~/]([^~]*\\.jpg)[~\"]", "http://$2/$3");
//        indexRule.category = new Selector("td.itdc a img", "attr", "alt", null, null);
//        indexRule.datetime = new Selector("td.itd[style]", "html", null, null, null);
//        indexRule.rating = new Selector("td.itd div div.it4 div", "attr", "style", "background-position:-?(\\d+)px -?(\\d+)px", "5-$1/16-($2-1)/40");
//
//        galleryRule = new Rule();
//        galleryRule.title = new Selector("h1#gj", "html", null, null, null);
//        galleryRule.tags = new Selector("div#taglist table tr td:eq(1) div a", "html", null, null, null);
//        galleryRule.item = new Selector("div.gdtl,div.gdtm", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("this", null, null, "(http://[^\"]*?\\.jpg)", null);
//
//        pic = new Selector("div.sni a img[style]", "attr", "src", null, null);
//
//        sites.add(new Site(2, "G.E-hentai",
//                "http://g.e-hentai.org/?page={page:0}",
//                "http://g.e-hentai.org/g/{idCode:}/?p={page:0}",
//                "http://g.e-hentai.org/?f_search={keyword:}&page={page:0}",
//                "https://forums.e-hentai.org/index.php?act=Login",
//                indexRule, galleryRule, null, pic, Site.FLAG_REPEATED_THUMBNAIL));
//
//        List<Category> categories = new ArrayList<>();
//        categories.add(new Category(1, "首页", "http://g.e-hentai.org/?page={page:0}"));
//        categories.add(new Category(2, "同人志", "http://g.e-hentai.org/doujinshi/{page:0}"));
//        categories.add(new Category(3, "漫画", "http://g.e-hentai.org/manga/{page:0}"));
//        categories.add(new Category(4, "同人CG", "http://g.e-hentai.org/artistcg/{page:0}"));
//        categories.add(new Category(5, "游戏CG", "http://g.e-hentai.org/gamecg/{page:0}"));
//        categories.add(new Category(6, "欧美", "http://g.e-hentai.org/western/{page:0}"));
//        categories.add(new Category(7, "Non-H", "http://g.e-hentai.org/non-h/{page:0}"));
//        categories.add(new Category(8, "图集", "http://g.e-hentai.org/imageset/{page:0}"));
//        categories.add(new Category(9, "Cosplay", "http://g.e-hentai.org/cosplay/{page:0}"));
//        categories.add(new Category(10, "亚洲AV", "http://g.e-hentai.org/asianporn/{page:0}"));
//        categories.add(new Category(11, "MISC", "http://g.e-hentai.org/misc/{page:0}"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("table.itg tr.gtr0,tr.gtr1", null, null, null, null);
//        indexRule.idCode = new Selector("td.itd div div.it5 a", "attr", "href", "/g/(.*)", null);
//        indexRule.title = new Selector("td.itd div div.it5 a", "html", null, null, null);
//        indexRule.uploader = new Selector("td.itu div a", "html", null, null, null);
//        indexRule.cover = new Selector("td.itd div div.it2", "html", null, "(//|inits?~)(.*?org)[~/]([^~]*\\.jpg)[~\"]", "http://$2/$3");
//        indexRule.category = new Selector("td.itdc a img", "attr", "alt", null, null);
//        indexRule.datetime = new Selector("td.itd[style]", "html", null, null, null);
//        indexRule.rating = new Selector("td.itd div div.it4 div", "attr", "style", "background-position:-?(\\d+)px -?(\\d+)px", "5-$1/16-($2-1)/40");
//
//        galleryRule = new Rule();
//        galleryRule.title = new Selector("h1#gj", "html", null, null, null);
//        galleryRule.tags = new Selector("div#taglist table tr td:eq(1) div a", "html", null, null, null);
//        galleryRule.item = new Selector("div.gdtl,div.gdtm", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("this", null, null, "(http://[^\"]*?\\.jpg)", null);
//
//        pic = new Selector("div.sni a img[style]", "attr", "src", null, null);
//
//        sites.add(new Site(3, "Ex-hentai",
//                "https://exhentai.org/?page={page:0}",
//                "http://exhentai.org/g/{idCode:}/?p={page:0}",
//                "http://exhentai.org/?f_search={keyword:}&page={page:0}",
//                "https://forums.e-hentai.org/index.php?act=Login",
//                indexRule, galleryRule, null, pic, Site.FLAG_REPEATED_THUMBNAIL));
//        categories = new ArrayList<>();
//        categories.add(new Category(1, "首页", "http://exhentai.org/?page={page:0}"));
//        categories.add(new Category(2, "同人志", "http://exhentai.org/doujinshi/{page:0}"));
//        categories.add(new Category(3, "漫画", "http://exhentai.org/manga/{page:0}"));
//        categories.add(new Category(4, "同人CG", "http://exhentai.org/artistcg/{page:0}"));
//        categories.add(new Category(5, "游戏CG", "http://exhentai.org/gamecg/{page:0}"));
//        categories.add(new Category(6, "欧美", "http://exhentai.org/western/{page:0}"));
//        categories.add(new Category(7, "Non-H", "http://exhentai.org/non-h/{page:0}"));
//        categories.add(new Category(8, "图集", "http://exhentai.org/imageset/{page:0}"));
//        categories.add(new Category(9, "Cosplay", "http://exhentai.org/cosplay/{page:0}"));
//        categories.add(new Category(10, "亚洲AV", "http://exhentai.org/asianporn/{page:0}"));
//        categories.add(new Category(11, "MISC", "http://exhentai.org/misc/{page:0}"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("div.gallary_wrap ul li.gallary_item", null, null, null, null);
//        indexRule.idCode = new Selector("div.pic_box a", "attr", "href", "aid-(\\d+)", null);
//        indexRule.title = new Selector("div.info div.title a", "html", null, null, null);
//        indexRule.cover = new Selector("div.pic_box a img", "attr", "data-original", null, null);
//        indexRule.datetime = new Selector("div.info div.info_col", "html", null, "(\\d{4}-\\d{2}-\\d{2})", null);
//
//        galleryRule = new Rule();
//        galleryRule.item = new Selector("div.gallary_wrap ul li.gallary_item div.pic_box", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("a img", "attr", "data-original", null, null);
//
//        pic = new Selector("img#picarea", "attr", "src", null, null);
//
//        sites.add(new Site(4, "绅士漫画",
//                "http://www.wnacg.org/albums-index-page-{page:1}.html",
//                "http://www.wnacg.org/photos-index-page-{page:1}-aid-{idCode:}.html",
//                "http://www.wnacg.org/albums-index-page-{page:1}-sname-{keyword:}.html",
//                "http://www.wnacg.com/users-login.html",
//                indexRule, galleryRule, null, pic, Site.FLAG_NO_RATING+"|"+Site.FLAG_NO_TAG));
//        categories = new ArrayList<>();
//        categories.add(new Category(1, "首页", "http://www.wnacg.org/albums-index-page-{page:1}.html"));
//        categories.add(new Category(2, "同人志", "http://www.wnacg.com/albums-index-page-{page:1}-cate-5.html"));
//        categories.add(new Category(3, "同人志->汉化", "http://www.wnacg.com/albums-index-page-{page:1}-cate-1.html"));
//        categories.add(new Category(4, "同人志->日语", "http://www.wnacg.com/albums-index-page-{page:1}-cate-12.html"));
//        categories.add(new Category(5, "同人志->CG画集", "http://www.wnacg.com/albums-index-page-{page:1}-cate-2.html"));
//        categories.add(new Category(6, "同人志->Cosplay", "http://www.wnacg.com/albums-index-page-{page:1}-cate-3.html"));
//        categories.add(new Category(7, "单行本", "http://www.wnacg.com/albums-index-page-{page:1}-cate-6.html"));
//        categories.add(new Category(8, "单行本->汉化", "http://www.wnacg.com/albums-index-page-{page:1}-cate-9.html"));
//        categories.add(new Category(9, "单行本->日语", "http://www.wnacg.com/albums-index-page-{page:1}-cate-13.html"));
//        categories.add(new Category(10, "杂志", "http://www.wnacg.com/albums-index-page-{page:1}-cate-7.html"));
//        categories.add(new Category(11, "杂志->单篇汉化", "http://www.wnacg.com/albums-index-page-{page:1}-cate-10.html"));
//        categories.add(new Category(12, "杂志->日语", "http://www.wnacg.com/albums-index-page-{page:1}-cate-14.html"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("div.container div.gallery", null, null, null, null);
//        indexRule.idCode = new Selector("a", "attr", "href", null, null);
//        indexRule.title = new Selector("a div.caption", "html", null, null, null);
//        indexRule.cover = new Selector("a img", "attr", "src", "(.*)", "https:$1");
//
//        galleryRule = new Rule();
//        galleryRule.title = new Selector("div#info h2", "html", null, null, null);
//        galleryRule.category = new Selector(".tag-container:eq(6) span.tags a", "html", null, "(.*)<span", null);
//        galleryRule.tags = new Selector("span.tags a", "html", null, "(.*)<span", null);
//        galleryRule.item = new Selector("div.container div.thumb-container", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("a", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("a img", "attr", "data-src", "(.*)", "https:$1");
//
//        pic = new Selector("#image-container a img", "attr", "src", "(.*)", "https:$1");
//
//        sites.add(new Site(5, "nhentai",
//                "https://nhentai.net/?page={page:1}",
//                "https://nhentai.net{idCode:}",
//                "https://nhentai.net/search/?q={keyword:}&page={page:1}",
//                "https://nhentai.net/login/",
//                indexRule, galleryRule, null, pic, Site.FLAG_NO_RATING+"|"+Site.FLAG_NO_TAG));
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("#ajaxtable tr.tr3.t_one:gt(10)", null, null, null, null);
//        indexRule.idCode = new Selector("td:eq(1) h3 a", "attr", "href", "htm_data/(.*?).html", null);
//        indexRule.category = new Selector("td:eq(1)", "html", null, "\\[([^<>]*?)\\]", null);
//        indexRule.title = new Selector("td:eq(1) h3 a", "html", null, "(<font.*?>)?([^<>]*)(</font>)?", "$2");
//        indexRule.uploader = new Selector("td:eq(2) a", "html", null, null, null);
//        indexRule.datetime = new Selector("td:eq(2) div", "html", null, null, null);
//
//        galleryRule = new Rule();
//        galleryRule.cover = new Selector("div.tpc_content input:eq(0)", "attr", "src", null, null);
//        galleryRule.item = new Selector("div.tpc_content input", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("this", "attr", "src", null, null);
//        galleryRule.pictureThumbnail = new Selector("this", "attr", "src", null, null);
//
//        sites.add(new Site(6, "草榴社区",
//                "http://cl.deocool.pw/thread0806.php?fid=8&page={page:1}",
//                "http://cl.deocool.pw/htm_data/{idCode:}.html",
//                null,
//                "http://cl.deocool.pw/login.php",
//                indexRule, galleryRule, null, null, Site.FLAG_NO_COVER+"|"+Site.FLAG_NO_RATING+"|"+Site.FLAG_NO_TAG));
//        categories = new ArrayList<>();
//        categories.add(new Category(1, "贴图区", "http://cl.deocool.pw/thread0806.php?fid=8&page={page:1}"));
//        categories.add(new Category(2, "自拍区", "http://cl.deocool.pw/thread0806.php?fid=16&page={page:1}"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("div.post_box", null, null, null, null);
//        indexRule.idCode = new Selector("div.c-top div.tit h2 a", "attr", "href", "html/(.*)\\.html", null);
//        indexRule.title = new Selector("div.c-top div.tit h2 a", "html", null, null, null);
//        indexRule.cover = new Selector("div.c-con a[rel='bookmark'] img", "attr", "src", null, null);
//        indexRule.category = new Selector("div.c-top div.tit p span a", "html", null, null, null);
//        indexRule.datetime = new Selector("div.c-top div.datetime", "html", null, "(\\d{4})<br>(\\d{2})-(\\d{2})", "$1-$2-$3");
//
//        galleryRule = new Rule();
//        galleryRule.tags = new Selector("div#taglist table tr td:eq(1) div a", "html", null, null, null);
//        galleryRule.item = new Selector("div.entry-content > p", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("img", "attr", "src", null, null);
//        galleryRule.pictureThumbnail = new Selector("img", "attr", "src", null, null);
//
//        sites.add(new Site(7, "177漫画",
//                "http://www.177pic66.com/page/{page:1}?variant=zh-hans",
//                "http://www.177pic66.com/html/{idCode:}.html/{page:1}",
//                "http://www.177pic66.com/page/{page:1}?s={keyword:}&variant=zh-hans",
//                null,
//                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING));
//
//        categories = new ArrayList<>();
//        categories.add(new Category(1, "首页", "http://www.177pic66.com/page/{page:1}?variant=zh-hans"));
//        categories.add(new Category(2, "中文漫画", "http://www.177pic66.com/html/category/tt/page/{page:1}?variant=zh-hans"));
//        categories.add(new Category(3, "全彩CG", "http://www.177pic66.com/html/category/cg/page/{page:1}?variant=zh-hans"));
//        categories.add(new Category(4, "日文漫画", "http://www.177pic66.com/html/category/jj/page/{page:1}?variant=zh-hans"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("div.post", null, null, null, null);
//        indexRule.idCode = new Selector("h2 > a", "attr", "href", "/(\\d+).html", null);
//        indexRule.title = new Selector("h2 > a", "attr", "title", null, null);
//        indexRule.cover = new Selector("div.more-field > div.box > a", "attr", "href", null, null);
//        indexRule.category = new Selector("div.blog_info > ul > li.cat > a", "html", null, null, null);
//        indexRule.datetime = new Selector("div.blog_info > ul > li.cal", "html", null, "(\\d{4}.\\d{2}.\\d{2}. [0-9:]+)", null);
//        indexRule.tags = new Selector("div.blog_info > ul > li.tag > a[rel='tag']", "html", null, null, null);
//
//        galleryRule = new Rule();
//        galleryRule.item = new Selector("div.box > a", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("this", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("this", "attr", "href", null, null);
//
//        sites.add(new Site(8, "二次萌エロ画像ブログ",
//                "http://moeimg.net/page/{page:1}",
//                "http://moeimg.net/{idCode:}.html",
//                "http://moeimg.net/?cat=0&s={keyword:}&submit=%E6%A4%9C%E7%B4%A2",
//                null,
//                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING));
//
//        categories = new ArrayList<>();
//        categories.add(new Category(1, "首页", "http://moeimg.net/page/{page:1}"));
//        categories.add(new Category(2, "エロ画像", "http://moeimg.net/category/%E3%82%A8%E3%83%AD%E7%94%BB%E5%83%8F/page/{page:1}"));
//        categories.add(new Category(3, "非エロ・微エロ画像", "http://moeimg.net/category/%E9%9D%9E%E3%82%A8%E3%83%AD%E3%83%BB%E5%BE%AE%E3%82%A8%E3%83%AD%E7%94%BB%E5%83%8F/page/{page:1}"));
//        categories.add(new Category(4, "ネタ画像", "http://moeimg.net/category/%E3%83%8D%E3%82%BF%E7%94%BB%E5%83%8F/page/{page:1}"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//
//        /*******非和谐站*******/
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("div#postlist > div.pin", null, null, null, null);
//        indexRule.idCode = new Selector("div.pin-coat a", "attr", "href", "http://.*?/(\\d+)", null);
//        indexRule.title = new Selector("div.pin-coat > a span", "html", null, null, null);
//        indexRule.cover = new Selector("div.pin-coat > a img", "attr", "original", null, null);
//        indexRule.datetime = new Selector("div.pin-coat div.pin-data span.timer span", "html", null, null, null);
//
//        galleryRule = new Rule();
//        galleryRule.datetime = new Selector("div.main-header > div.main-meta > span:eq(0)", "html", null, null, null);
//        galleryRule.category = new Selector("div.main-header > div.main-meta > span:eq(1) > a", "html", null, null, null);
//        galleryRule.tags = new Selector("div.main-tags > a[rel='tag']", "html", null, null, null);
//        galleryRule.item = new Selector("div.main-body p > a", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("this", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("this", "attr", "href", null, null);
//
//        sites.add(new Site(10, "绝对领域",
//                "http://www.jdlingyu.moe/page/{page:1}/",
//                "http://www.jdlingyu.moe/{idCode:}/",
//                "http://www.jdlingyu.moe/page/{page:1}/?s={keyword:}",
//                "http://www.jdlingyu.moe/wp-login.php",
//                indexRule, galleryRule, null, null, Site.FLAG_NO_RATING));
//
//        categories = new ArrayList<>();
//        categories.add(new Category(1, "首页", "http://www.jdlingyu.moe/page/{page:1}/"));
//        categories.add(new Category(2, "专题", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(3, "专题->自拍", "http://www.jdlingyu.moe/%e8%87%aa%e6%8b%8d/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(4, "专题->放流", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/%e6%94%be%e6%b5%81/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(5, "专题->独家", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/%e7%8b%ac%e5%ae%b6/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(6, "专题->漫展最前线", "http://www.jdlingyu.moe/%e4%b8%93%e9%a2%98/mzzqx/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(7, "特点", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(8, "特点->胖次", "http://www.jdlingyu.moe/%e8%83%96%e6%ac%a1/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(9, "特点->丝袜", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e4%b8%9d%e8%a2%9c/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(10, "特点->汉服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e6%b1%89%e6%9c%8d/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(11, "特点->死库水", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e6%ad%bb%e5%ba%93%e6%b0%b4/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(12, "特点->体操服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e4%bd%93%e6%93%8d%e6%9c%8d/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(13, "特点->女仆装", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e5%a5%b3%e4%bb%86%e8%a3%85/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(14, "特点->水手服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e6%b0%b4%e6%89%8b%e6%9c%8d/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(15, "特点->和服", "http://www.jdlingyu.moe/%e7%89%b9%e7%82%b9/%e5%92%8c%e6%9c%8d%e6%b5%b4%e8%a1%a3/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(16, "弄潮", "http://www.jdlingyu.moe/%e5%bc%84%e6%bd%ae/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(16, "弄潮->束缚", "http://www.jdlingyu.moe/%e5%bc%84%e6%bd%ae/%e6%9d%9f%e7%bc%9a/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(17, "Cosplay", "http://www.jdlingyu.moe/cosplay/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(18, "写真", "http://www.jdlingyu.moe/%e5%86%99%e7%9c%9f/{pageStr:page/{page:1}/}"));
//        categories.add(new Category(19, "下载", "http://www.jdlingyu.moe/%e4%b8%8b%e8%bd%bd/{pageStr:page/{page:1}/}"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("div.display:has(.thumb)", null, null, null, null);
//        indexRule.idCode = new Selector(".title h2 a", "attr", "href", null, null);
//        indexRule.title = new Selector(".title h2 a", "html", null, null, null);
//        indexRule.uploader = new Selector(".meta dl dd span.reg_user", "html", null, null, null);
//        indexRule.cover = new Selector("a.thumb_image img", "attr", "src", null, null);
//        indexRule.datetime = new Selector(".meta dd:eq(3)", "html", null, null, null);
//        indexRule.tags = new Selector(".meta span.tag a", "html", null, null, null);
//
//        galleryRule = new Rule();
//        galleryRule.rating = new Selector(".display .meta dl dd[id^='rating']", "html", null, "(\\d*\\.?\\d*).*?<img", "$2/2");
//        galleryRule.item = new Selector(".image_thread .image_block", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("a.thumb_image", "attr", "href", null, null);
//        galleryRule.pictureThumbnail = new Selector("a.thumb_image img", "attr", "src", null, null);
//
//        sites.add(new Site(11, "E-shuushuu",
//                "http://e-shuushuu.net/?page={page:1}",
//                "http://e-shuushuu.net/{idCode:}",
//                null,
//                "http://e-shuushuu.net/",
//                indexRule, galleryRule, null, null, null));
//
//        categories = new ArrayList<>();
//        categories.add(new Category(1, "首页", "http://e-shuushuu.net/?page={page:1}"));
//        categories.add(new Category(2, "排行榜", "http://e-shuushuu.net/top.php?page={page:1}"));
//        sites.get(sites.size() - 1).setCategories(categories);
//
//        indexRule = new Rule();
//        indexRule.item = new Selector("ul._image-items > li.image-item", null, null, null, null);
//        indexRule.idCode = new Selector("a.work", "attr", "href", "illust_id=(.*)", null);
//        indexRule.title = new Selector("a > h1.title", "html", null, null, null);
//        indexRule.uploader = new Selector("a.user", "html", null, null, null);
//        indexRule.cover = new Selector("a.work > div._layout-thumbnail > img._thumbnail", "attr", "src", null, null);
//        indexRule.datetime = new Selector("a.work > div._layout-thumbnail > img._thumbnail", "attr", "src", ".*img/(\\d{4})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})", "$1-$2-$3 $4:$5:$6");
//
//        galleryRule = new Rule();
//        galleryRule.title = new Selector("div.ui-expander-target > h1.title", "html", null, null, null);
//        galleryRule.uploader = new Selector("a.user-link > hi.user", "html", null, null, null);
//        galleryRule.datetime = new Selector("ul.meta > li:eq(0)", "html", null, null, null);
//        galleryRule.tags = new Selector("ul.tags > li.tag > a.text", "html", null, null, null);
//        galleryRule.rating = new Selector("section.score", "html", null, "rated-count\">(\\d+).*score-count\">(\\d+)", "$2/$1/2");
//        galleryRule.item = new Selector("body", null, null, null, null);
//        galleryRule.pictureUrl = new Selector("div.works_display", "html", null, "\"(member_illust.php\\?mode=manga.*?|http://.*?.jpg)\"", null);
//        galleryRule.pictureThumbnail = new Selector("div.works_display div._layout-thumbnail > img", "attr", "src", "(http://.*?c)/\\d+x\\d+/(.*?\\.jpg)", "$1/150x150/$2");
//
//        Rule extraRule = new Rule();
//        extraRule.item = new Selector("div.item-container", null, null, null, null);
//        extraRule.pictureUrl = new Selector("img", "attr", "data-src", null, null);
//        extraRule.pictureThumbnail = new Selector("img", "attr", "data-src", "(http://.*?c)/\\d+x\\d+/(.*?\\.jpg)", "$1/150x150/$2");
//
//        sites.add(new Site(12, "Pixiv",
//                "http://www.pixiv.net/new_illust.php?p={page:1}",
//                "http://www.pixiv.net/member_illust.php?mode=medium&illust_id={idCode:}",
//                "http://www.pixiv.net/search.php?word={keyword:}&p={page:1}",
//                "https://accounts.pixiv.net/login",
//                indexRule, galleryRule, null, null, Site.FLAG_SECOND_LEVEL_GALLERY));
//        sites.get(sites.size() - 1).extraRule = extraRule;

        ListDataProvider<Site> dataProvider = new ListDataProvider<>(sites);
        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setInitiateOnTouch(true);

        siteAdapter = new SiteAdapter(dataProvider);
        // wrap for dragging
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(siteAdapter);
        rvSite.setAdapter(mWrappedAdapter);

        mRecyclerViewDragDropManager.attachRecyclerView(rvSite);

        siteAdapter.setOnItemClickListener(new SiteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position, boolean isGrid) {
                if (position == siteAdapter.getItemCount() - 1) {
                    Intent intent = new Intent(MainActivity.this, AddSiteActivity.class);
                    startActivityForResult(intent, RESULT_ADD_SITE);
                } else {
                    Site site = (Site) siteAdapter.getDataProvider().getItem(position);
                    CollectionFragment fragment = CollectionFragment.newInstance(site);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isGrid", isGrid);
                    fragment.setArguments(bundle);
                    selectSite(fragment, site);
                }
                drawer.closeDrawer(GravityCompat.START);
            }

            @Override
            public boolean onItemLongClick(View v, int position) {
                final Site site = (Site) siteAdapter.getDataProvider().getItem(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("操作")
                        .setItems(new String[]{"登录", "编辑", "删除"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    temp = site;
                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivityForResult(intent, RESULT_LOGIN);
                                } else if (i == 1) {
                                    temp = site;
                                    Intent intent = new Intent(MainActivity.this, ModifySiteActivity.class);
                                    startActivityForResult(intent, RESULT_MODIFY_SITE);
                                } else if (i == 2) {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("是否删除？")
                                            .setMessage("删除后将无法恢复")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    siteHolder.deleteSite(site);
                                                    siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                                                    siteAdapter.notifyDataSetChanged();
                                                }
                                            }).setNegativeButton("取消", null).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
        });

        siteAdapter.setOnItemMoveListener(new SiteAdapter.OnItemMoveListener() {
            @Override
            public void onItemMove(int fromPosition, int toPosition) {
                List<Site> sites = siteAdapter.getDataProvider().getItems();
                for(int i = 0; i < sites.size(); i++){
                    Site site = sites.get(i);
                    site.index = i+1;
                    siteHolder.updateSiteIndex(site);
                }
            }
        });

        siteAdapter.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final boolean right) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (right)
                            currFragment.setRecyclerViewToGrid();
                        else
                            currFragment.setRecyclerViewToList();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        }, 200);
                    }
                }, 300);
            }
        });

        ListDataProvider<Category> categoryProvider = new ListDataProvider<>(new ArrayList<Category>());
        categoryAdapter = new CategoryAdapter(categoryProvider);
        categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Category category = (Category) categoryAdapter.getDataProvider().getItem(position);
                categoryAdapter.selectedCid = category.cid;
                categoryAdapter.notifyDataSetChanged();
                currFragment.onCategorySelected(category);
                drawer.closeDrawer(GravityCompat.END);
            }
        });
        rvCategory.setAdapter(categoryAdapter);

        if (sites.size() > 0) {
            Site site = sites.get(0);
            selectSite(CollectionFragment.newInstance(site), site);
        }

//        SimpleFileUtil.writeString("/sdcard/sites.txt", new Gson().toJson(sites), "utf-8");

        HViewerApplication.checkUpdate(this);

    }

    private void initSearchSuggestions() {
        List<String> histories = HViewerApplication.searchHistoryHolder.getSearchHistory();
        List<String> suggestions = HViewerApplication.searchSuggestionHolder.getSearchSuggestion();
        suggestions.addAll(histories);
        suggestions = new ArrayList(new HashSet(suggestions));
        Collections.sort(suggestions, String.CASE_INSENSITIVE_ORDER);
        int size = suggestions.size();
        String[] kwStrings = new String[size];
        kwStrings = suggestions.toArray(kwStrings);
        final MySearchAdapter adapter = new MySearchAdapter(this, kwStrings);
        searchView.setAdapter(adapter);

        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] keywords = currQuery.toString().split(" ");
                String keyword = "";
                for (int i = 0; i < keywords.length - 1; i++)
                    keyword += keywords[i] + " ";

                keyword += adapter.getItem(position);
                searchView.setQuery(keyword, false);
            }
        });
        isSuggestionEmpty = false;
    }

    public void setTitle(String title) {
        collapsingToolbarLayout.setTitle(title);
    }


    public void replaceFragment(MyFragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
        currFragment = fragment;
    }

    public void selectSite(MyFragment fragment, Site site) {
        siteAdapter.selectedSid = site.sid;
        siteAdapter.notifyDataSetChanged();
        setTitle(site.title);
        replaceFragment(fragment, site.title);

        if (site.categories != null && site.categories.size() > 0) {
            ListDataProvider<Category> dataProvider = new ListDataProvider<>(site.categories);
            categoryAdapter.setDataProvider(dataProvider);
            categoryAdapter.notifyDataSetChanged();

            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            Category category = site.categories.get(0);
            categoryAdapter.selectedCid = category.cid;
            categoryAdapter.notifyDataSetChanged();
            currFragment.onCategorySelected(category);
        } else {
            categoryAdapter.getDataProvider().clear();
            categoryAdapter.notifyDataSetChanged();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        //一开始隐藏搜索按钮
        item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Intent intent;
        switch (item.getItemId()) {
            case R.id.action_download:
                intent = new Intent(MainActivity.this, DownloadActivity.class);
                break;
            case R.id.action_history:
                intent = new Intent(MainActivity.this, HistoryActivity.class);
                break;
            case R.id.action_favourite:
                intent = new Intent(MainActivity.this, FavouriteActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
            }
        }, 500);
        return true;
    }

    @OnClick(R.id.fab_search)
    void search() {
        appBar.setExpanded(false);
        searchView.showSearch();
        searchView.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_ADD_SITE) {
                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                siteAdapter.notifyDataSetChanged();
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            selectSite(CollectionFragment.newInstance(site), site);
                        }
                    };
                    handler.post(r);
                }
            } else if (requestCode == RESULT_MODIFY_SITE) {
                siteAdapter.getDataProvider().setDataSet(siteHolder.getSites());
                siteAdapter.notifyDataSetChanged();
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            selectSite(CollectionFragment.newInstance(site), site);
                        }
                    };
                    handler.post(r);
                }
            } else if (requestCode == RESULT_LOGIN) {
                if (temp instanceof Site) {
                    final Site site = (Site) temp;
                    siteHolder.updateSite(site);
                    Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            selectSite(CollectionFragment.newInstance(site), site);
                        }
                    };
                    handler.post(r);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (siteHolder != null)
            siteHolder.onDestroy();
        HViewerApplication.searchHistoryHolder.saveSearchHistory();
        HViewerApplication.searchSuggestionHolder.saveSearchSuggestion();
        new DownloadTaskHolder(this).setAllPaused();

        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (rvSite != null) {
            rvSite.setItemAnimator(null);
            rvSite.setAdapter(null);
            rvSite = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        super.onDestroy();
    }

    @OnClick(R.id.btn_setting)
    void openSetting() {
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_exit)
    void exit() {
        finish();
    }

}
