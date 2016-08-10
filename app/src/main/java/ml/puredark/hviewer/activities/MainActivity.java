package ml.puredark.hviewer.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.adapters.SiteAdapter;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataproviders.AbstractDataProvider;
import ml.puredark.hviewer.dataproviders.ListDataProvider;
import ml.puredark.hviewer.fragments.CollectionFragment;
import ml.puredark.hviewer.fragments.MyFragment;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_search)
    FloatingActionButton fabSearch;

    @BindView(R.id.nav_header_view)
    LinearLayout navHeaderView;
    @BindView(R.id.rv_rule)
    RecyclerView rvRule;
    @BindView(R.id.btn_exit)
    LinearLayout btnExit;

    //记录当前加载的是哪个Fragment
    private MyFragment currFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        List<Site> sites = HViewerApplication.getSites();
        sites.clear();

        Rule indexRule = new Rule();
        indexRule.item = new Selector("#ig .ig", null, null, null);
        indexRule.url = new Selector("td.ii a", "attr", "href", null);
        indexRule.title = new Selector("table.it tr:eq(0) a", "html", null, null);
        indexRule.uploader = new Selector("table.it tr:eq(1) td:eq(1)", "html", null, "(by .*)");
        indexRule.cover = new Selector("td.ii img", "attr", "src", null);
        indexRule.category = new Selector("table.it tr:eq(2) td:eq(1)", "html", null, null);
        indexRule.datetime = new Selector("table.it tr:eq(1) td:eq(1)", "html", null, "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})");
        indexRule.rating = new Selector("table.it tr:eq(4) td:eq(1)", "html", null, null);
        indexRule.tags = new Selector("table.it tr:eq(3) td:eq(1)", "html", null, "([a-zA-Z0-9 -]+)");

        Rule galleryRule = new Rule();
        galleryRule.pictures = new Selector("#gh .gi a", null, null, "<a.*?href=\"(.*?)\".*?<img.*?src=\"(.*?)\"");

        sites.add(new Site(1, "Lofi E-hentai", "http://lofi.e-hentai.org",
                indexRule, galleryRule, null));


        AbstractDataProvider<Site> dataProvider = new ListDataProvider<>(sites);
        final SiteAdapter adapter = new SiteAdapter(dataProvider);
        rvRule.setAdapter(adapter);

        adapter.setOnItemClickListener(new SiteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Site site = (Site) adapter.getDataProvider().getItem(position);
                adapter.selectedRid = site.rid;
                adapter.notifyDataSetChanged();
                HViewerApplication.temp = site;
                replaceFragment(CollectionFragment.newInstance(), site.title);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        if (sites.size() > 0) {
            Site site = sites.get(0);
            adapter.selectedRid = site.rid;
            adapter.notifyDataSetChanged();
            HViewerApplication.temp = site;
            replaceFragment(CollectionFragment.newInstance(), site.title);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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

}
