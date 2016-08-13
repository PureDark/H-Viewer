package ml.puredark.hviewer.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.adapters.MySearchAdapter;
import ml.puredark.hviewer.adapters.SiteAdapter;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.customs.AppBarStateChangeListener;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.fragments.CollectionFragment;
import ml.puredark.hviewer.fragments.MyFragment;
import ml.puredark.hviewer.holders.SearchHistoryHolder;
import ml.puredark.hviewer.utils.SimpleFileUtil;

public class MainActivity extends AppCompatActivity {
    private static int RESULT_ADD_SITE = 1;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_search)
    FloatingActionButton fabSearch;

    @BindView(R.id.nav_header_view)
    LinearLayout navHeaderView;
    @BindView(R.id.rv_site)
    RecyclerView rvSite;
    @BindView(R.id.btn_exit)
    LinearLayout btnExit;

    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    //记录当前加载的是哪个Fragment
    private MyFragment currFragment;

    //当前搜索的查询关键字
    private String currQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Initialize user settings
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // User interface
        setSupportActionBar(toolbar);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //appbar折叠时显示搜索按钮和搜索框，否则隐藏
        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {

            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.COLLAPSED) {
                    if (toolbar.getMenu().size() > 0)
                        toolbar.getMenu().getItem(0).setVisible(true);
                    searchView.animate().alpha(1f).setDuration(300);
                } else {
                    if (toolbar.getMenu().size() > 0)
                        toolbar.getMenu().getItem(0).setVisible(false);
                    searchView.animate().alpha(0f).setDuration(300);
                }
            }
        });

        initSearchSuggestions();

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                currQuery = keyword;
                HViewerApplication.searchHistoryHolder.addSearchHistory(keyword);
                if (!"".equals(keyword) && currFragment != null)
                    currFragment.onSearch(keyword);
                initSearchSuggestions();
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                currQuery = newText;
                return true;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            @Override
            public void onSearchViewClosed() {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
        });

        List<Site> sites = HViewerApplication.siteHolder.getSites();

        sites.clear();

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
        galleryRule.pictureUrl = new Selector("#gh .gi a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("#gh .gi a img", "attr", "src", null, null);

        Selector pic = new Selector("img#sm", "attr", "src", null, null);

        sites.add(new Site(1, "Lofi.E-hentai",
                "http://lofi.e-hentai.org/?page={page:0}",
                "http://lofi.e-hentai.org/g/{idCode:}/{page:0}",
                "http://lofi.e-hentai.org/?f_search={keyword:}&page={page:0}",
                indexRule, galleryRule, pic));

        indexRule = new Rule();
        indexRule.item = new Selector("table.itg tr.gtr0,tr.gtr1", null, null, null, null);
        indexRule.idCode = new Selector("td.itd div div.it5 a", "attr", "href", "/g/(.*)", null);
        indexRule.title = new Selector("td.itd div div.it5 a", "html", null, null, null);
        indexRule.uploader = new Selector("td.itu div a", "html", null, null, null);
        indexRule.cover = new Selector("td.itd div div.it2", "html", null, "(t/.*.jpg)", "http://ehgt.org/$1");
        indexRule.category = new Selector("td.itdc a img", "attr", "alt", null, null);
        indexRule.datetime = new Selector("td.itd:eq(0)", "html", null, null, null);
        indexRule.rating = new Selector("td.itd div div.it4 div", "attr", "style", "background-position:-(\\d+)px -(\\d+)px", "5-$1/16-($2-1)/40");

        galleryRule = new Rule();
        galleryRule.tags = new Selector("div#taglist table tr td:eq(1) div a", "html", null, null, null);
        galleryRule.pictureUrl = new Selector("div#gtd div.gdtm div a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("div#gtd div.gdtm div", null, null, "<div.*?style=\".*?background:.*?url\\((.*?)\\)", null);

        pic = new Selector("img#sm", "attr", "src", null, null);

        sites.add(new Site(2, "G.E-hentai",
                "http://g.e-hentai.org/?page={page:0}",
                "http://g.e-hentai.org/g/{idCode:}/?p={page:0}",
                "http://g.e-hentai.org/?f_search={keyword:}&page={page:0}",
                indexRule, galleryRule, pic));

        indexRule = new Rule();
        indexRule.item = new Selector("div.gallary_wrap ul li.gallary_item", null, null, null, null);
        indexRule.idCode = new Selector("div.pic_box a", "attr", "href", "aid-(\\d+)", null);
        indexRule.title = new Selector("div.info div.title a", "html", null, null, null);
        indexRule.cover = new Selector("div.pic_box a img", "attr", "data-original", null, null);
        indexRule.datetime = new Selector("div.info div.info_col", "html", null, "(\\d{4}-\\d{2}-\\d{2})", null);

        galleryRule = new Rule();
        galleryRule.pictureUrl = new Selector("div.gallary_wrap ul li.gallary_item div.pic_box a", "attr", "href", null, null);
        galleryRule.pictureThumbnail = new Selector("div.gallary_wrap ul li.gallary_item div.pic_box a img", "attr", "data-original", null, null);

        pic = new Selector("img#picarea", "attr", "src", null, null);

        sites.add(new Site(3, "绅士漫画",
                "http://www.wnacg.org/albums-index-page-{page:1}.html",
                "http://www.wnacg.org/photos-index-page-{page:1}-aid-{idCode:}.html",
                "http://www.wnacg.org/albums-index-page-{page:1}-sname-{keyword:}.html",
                indexRule, galleryRule, pic));


        AbstractDataProvider<Site> dataProvider = new ListDataProvider<>(sites);
        final SiteAdapter adapter = new SiteAdapter(dataProvider);
        rvSite.setAdapter(adapter);

        adapter.setOnItemClickListener(new SiteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (position == adapter.getItemCount() - 1) {
                    Intent intent = new Intent(MainActivity.this, AddSiteActivity.class);
                    startActivityForResult(intent, RESULT_ADD_SITE);
                } else {
                    Site site = (Site) adapter.getDataProvider().getItem(position);
                    adapter.selectedSid = site.sid;
                    adapter.notifyDataSetChanged();
                    HViewerApplication.temp = site;
                    replaceFragment(CollectionFragment.newInstance(), site.title);
                }
                drawer.closeDrawer(GravityCompat.START);
            }

            @Override
            public void onItemLongClick(View v, int position) {
                final Site site = (Site) adapter.getDataProvider().getItem(position);
                new AlertDialog.Builder(MainActivity.this).setTitle("是否删除？")
                        .setMessage("删除后将无法恢复")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HViewerApplication.siteHolder.deleteSite(site);
                                List<Site> sites = HViewerApplication.siteHolder.getSites();
                                adapter.setDataProvider(new ListDataProvider(sites));
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });

        if (sites.size() > 0) {
            Site site = sites.get(0);
            adapter.selectedSid = site.sid;
            adapter.notifyDataSetChanged();
            HViewerApplication.temp = site;
            replaceFragment(CollectionFragment.newInstance(), site.title);
        }

        //SimpleFileUtil.writeString("/sdcard/sites.txt", new Gson().toJson(sites), "utf-8");

    }

    private void initSearchSuggestions(){
        List<String> histories = HViewerApplication.searchHistoryHolder.getSearchHistory();
        List<String> suggestions = HViewerApplication.searchSuggestionHolder.getSearchSuggestion();
        suggestions.addAll(histories);
        suggestions = SearchHistoryHolder.removeDuplicate(suggestions);
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
                for(int i=0;i<keywords.length-1;i++)
                    keyword +=keywords[i]+" ";

                keyword += adapter.getItem(position);
                searchView.setQuery(keyword, false);
            }
        });
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
                int sid = data.getIntExtra("sid", 0);
                List<Site> sites = HViewerApplication.siteHolder.getSites();
                SiteAdapter adapter = ((SiteAdapter) rvSite.getAdapter());
                adapter.setDataProvider(new ListDataProvider(sites));
                adapter.selectedSid = sid;
                adapter.notifyDataSetChanged();
                final Site site = sites.get(sites.size() - 1);
                HViewerApplication.temp = site;
                Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        replaceFragment(CollectionFragment.newInstance(), site.title);
                    }
                };
                handler.post(r);
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

    public void replaceFragment(MyFragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
        currFragment = fragment;
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
