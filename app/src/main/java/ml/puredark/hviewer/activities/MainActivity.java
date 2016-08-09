package ml.puredark.hviewer.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import ml.puredark.hviewer.dataproviders.DataProvider;
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

        Rule rule = new Rule();
        rule.item = new Selector("ul.Japan-comic-content.clearfix li.comic-content-item", null, null);
        rule.title = new Selector(".comic-text .Japan-comic-title", "attr", "title");
        rule.uploader = new Selector(".comic-text .Japan-comic-mask .japan-comic-a", "html", null);
        rule.cover = new Selector(".comic-text .Japan-comic-title img", "attr", "src");
        rule.category = new Selector("nothing", "html", null);
        rule.datetime = new Selector("nothing", "html", null);
        rule.rating = new Selector("nothing", "html", null);

        sites.add(new Site(1, "腾讯漫画", "http://ac.qq.com/Jump", rule));
        DataProvider<Site> dataProvider = new DataProvider<>(sites);
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
