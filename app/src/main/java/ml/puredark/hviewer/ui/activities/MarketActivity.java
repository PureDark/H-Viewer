package ml.puredark.hviewer.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.MarketSiteCategory;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.configs.UrlConfig;
import ml.puredark.hviewer.dataholders.SiteHolder;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.ui.adapters.MarketSiteAdapter;
import ml.puredark.hviewer.ui.adapters.ViewPagerAdapter;
import ml.puredark.hviewer.ui.customs.ExTabLayout;
import ml.puredark.hviewer.ui.customs.ExViewPager;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static ml.puredark.hviewer.ui.fragments.SettingFragment.KEY_PREF_MODE_R18_ENABLED;

public class MarketActivity extends AnimationActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tab_layout)
    ExTabLayout tabLayout;
    @BindView(R.id.view_pager)
    ExViewPager viewPager;
    @BindView(R.id.progress_bar)
    ProgressBarCircularIndeterminate progressBar;

    private SiteHolder siteHolder;

    private List<MarketSiteCategory> siteCategories;
    private List<MarketSiteAdapter> siteAdapters;

    private boolean getting = false;
    private boolean updated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);
        setReturnButton(btnReturn);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MDStatusBarCompat.setToolbarTabLayout(this);

        siteHolder = new SiteHolder(this);

        getAllSites(new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                try {
                    siteCategories = new Gson().fromJson((String) result, new TypeToken<List<MarketSiteCategory>>() {
                    }.getType());
                    initTabAndViewPager(siteCategories);
                    progressBar.setVisibility(View.GONE);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    onFailure(new HViewerHttpClient.HttpError(HViewerHttpClient.HttpError.ERROR_JSON));
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                showSnackBar(error.getErrorString());
            }
        });
    }


    private void initTabAndViewPager(List<MarketSiteCategory> siteCategories) {
        //初始化Tab和ViewPager
        List<View> views = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        siteAdapters = new ArrayList<>();

        boolean modeR18Enabled = (boolean) SharedPreferencesUtil.getData(this, KEY_PREF_MODE_R18_ENABLED, false);

        for (MarketSiteCategory category : siteCategories) {
            if (category.r18 && !modeR18Enabled)
                continue;

            View view = getLayoutInflater().inflate(R.layout.view_market_site_list, null);
            views.add(view);
            titles.add(category.title);

            RecyclerView rvMarketSites = (RecyclerView) view.findViewById(R.id.rv_market_sites);

            List<MarketSiteCategory.MarketSite> sites = new ArrayList<>();
            for (MarketSiteCategory.MarketSite site : category.sites) {
                if (!site.r18 || modeR18Enabled)
                    sites.add(site);
            }
            ListDataProvider<MarketSiteCategory.MarketSite> dataProvider = new ListDataProvider<>(sites);
            MarketSiteAdapter adapter = new MarketSiteAdapter(this, dataProvider, category.title);
            siteAdapters.add(adapter);
            adapter.setItemListener(new MarketSiteAdapter.ItemListener() {
                @Override
                public void onItemCheckUpdate(MarketSiteAdapter.MarketSiteViewHolder holder, int position, MarketSiteCategory.MarketSite marketSite) {
                    Site currSite = siteHolder.getSiteByTitle(marketSite.title);
                    if (currSite == null) {
                        holder.btnAdd.setText("添加");
                    } else if (currSite.versionCode >= marketSite.versionCode) {
                        holder.btnAdd.setText("已有");
                    } else {
                        holder.btnAdd.setText("更新");
                    }
                }

                @Override
                public void onItemBtnAddClick(View v, int position, MarketSiteCategory.MarketSite marketSite, String categoryTitle) {
                    if (marketSite != null && !getting) {
                        updateSite(marketSite, categoryTitle);
                    }
                }
            });

            rvMarketSites.setAdapter(adapter);
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(views, titles);
        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    public void showReplaceDialog(final Site currSite, final Site newSite, final int versionCode) {
        new AlertDialog.Builder(this).setTitle(currSite.title + "：是否直接覆盖同名站点？")
                .setMessage("选否则添加为新站点")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currSite.replace(newSite);
                        currSite.versionCode = versionCode;
                        siteHolder.updateSite(currSite);
                        showSnackBar("站点更新成功！");
                        updated = true;
                        for (MarketSiteAdapter adapter : siteAdapters) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newSite.gid = currSite.gid;
                        siteHolder.addSite(newSite);
                        showSnackBar("站点添加成功！");
                        updated = true;
                        for (MarketSiteAdapter adapter : siteAdapters) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }).show();
    }

    public void getAllSites(final HViewerHttpClient.OnResponseListener listener) {
        HViewerHttpClient.get(UrlConfig.siteSourceUrl, null, new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                if (result == null || result.equals(""))
                    return;
                try {
                    JsonArray siteSources = new JsonParser().parse((String) result).getAsJsonArray();
                    if (siteSources.size() > 0) {
                        HViewerHttpClient.get(siteSources.get(0).getAsString(), null, listener);
                    } else {
                        throw new Exception();
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    onFailure(new HViewerHttpClient.HttpError(HViewerHttpClient.HttpError.ERROR_JSON));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(new HViewerHttpClient.HttpError(HViewerHttpClient.HttpError.ERROR_NETWORK));
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                showSnackBar(error.getErrorString());
            }
        });
    }

    public void updateSite(MarketSiteCategory.MarketSite marketSite, String categoryTitle) {
        updateSite(marketSite, categoryTitle, false, 0);
    }

    public void updateSite(int flatPos) {
        updateSite(null, null, true, flatPos);
    }

    public void updateSite(MarketSiteCategory.MarketSite marketSite, String categoryTitle, final boolean silent, final int flatPos) {
        if (getting) return;
        if (silent) {
            int cPos = -1, sPos = -1;
            int size = 0;
            for (int i = 0; i < siteCategories.size(); i++) {
                MarketSiteCategory category = siteCategories.get(i);
                if (category.sites != null) {
                    if (size + category.sites.size() > flatPos) {
                        cPos = i;
                        sPos = flatPos - size;
                        categoryTitle = category.title;
                        break;
                    } else
                        size += category.sites.size();
                }
            }
            if (cPos == -1 || sPos == -1) {
                showSnackBar("站点全部更新完成！");
                for (MarketSiteAdapter adapter : siteAdapters) {
                    adapter.notifyDataSetChanged();
                }
                return;
            }
            marketSite = siteCategories.get(cPos).sites.get(sPos);
            Site currSite = siteHolder.getSiteByTitle(marketSite.title);
            if (currSite == null || currSite.versionCode >= marketSite.versionCode) {
                updateSite(flatPos+1);
                return;
            }
        }
        if(marketSite==null)
            return;
        if(categoryTitle==null)
            categoryTitle = "未分类";
        final String title = categoryTitle;
        final int versionCode = marketSite.versionCode;

        getting = true;
        HViewerHttpClient.get(marketSite.json, null, new HViewerHttpClient.OnResponseListener() {
            @Override
            public void onSuccess(String contentType, Object result) {
                getting = false;
                try {
                    Site newSite = new Gson().fromJson((String) result, Site.class);
                    Site currSite = siteHolder.getSiteByTitle(newSite.title);
                    if (currSite == null) {
                        SiteGroup siteGroup = siteHolder.getGroupByTitle(title);
                        if (siteGroup == null) {
                            newSite.gid = siteHolder.addSiteGroup(new SiteGroup(1, title));
                        }else{
                            newSite.gid = siteGroup.gid;
                        }
                        newSite.versionCode = versionCode;
                        siteHolder.addSite(newSite);
                        updated = true;
                        if (!silent) {
                            showSnackBar("站点添加成功！");
                            for (MarketSiteAdapter adapter : siteAdapters) {
                                adapter.notifyDataSetChanged();
                            }
                        }else
                            updateSite(flatPos+1);
                    } else if (!silent) {
                        showReplaceDialog(currSite, newSite, versionCode);
                    } else {
                        currSite.replace(newSite);
                        currSite.versionCode = versionCode;
                        siteHolder.updateSite(currSite);
                        updated = true;
                        if (!silent) {
                            showSnackBar("站点更新成功！");
                            for (MarketSiteAdapter adapter : siteAdapters) {
                                adapter.notifyDataSetChanged();
                            }
                        }else
                            updateSite(flatPos+1);
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    onFailure(new HViewerHttpClient.HttpError(HViewerHttpClient.HttpError.ERROR_JSON));
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(new HViewerHttpClient.HttpError(HViewerHttpClient.HttpError.ERROR_NETWORK));
                }
            }

            @Override
            public void onFailure(HViewerHttpClient.HttpError error) {
                getting = false;
                showSnackBar(error.getErrorString());
            }
        });
    }

    @OnClick(R.id.btn_update_all)
    void updateAll() {
        if (siteCategories == null) {
            showSnackBar("请等待站点加载完毕");
            return;
        } else {
            new AlertDialog.Builder(this).setTitle("全部更新？")
                    .setMessage("直接覆盖首个同名站点")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateSite(0);
                        }
                    })
                    .setNegativeButton("否", null).show();
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @OnClick(R.id.btn_return)
    void back() {
        if(getting){
            showSnackBar("正在更新站点，请等待更新完毕后再返回");
            return;
        }
        if(updated) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }else
            super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        siteHolder.onDestroy();
    }


}
