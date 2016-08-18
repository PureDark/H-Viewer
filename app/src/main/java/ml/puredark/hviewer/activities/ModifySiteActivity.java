package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.SitePropViewHolder;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;

public class ModifySiteActivity extends AnimationActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.edittext_container)
    RelativeLayout edittextContainer;
    @BindView(R.id.view_site_details)
    View viewSiteDetails;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private SitePropViewHolder holder;

    private Site site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_site);
        ButterKnife.bind(this);
        MDStatusBarCompat.setOrdinaryToolBar(this);

        setSupportActionBar(toolbar);
        setContainer(coordinatorLayout);

        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) edittextContainer.getLayoutParams();
            lp.topMargin += MDStatusBarCompat.getStatusBarHeight(this);
            edittextContainer.setLayoutParams(lp);
        }

        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);

        //获取传递过来的Site实例
        if (HViewerApplication.temp instanceof Site)
            site = (Site)HViewerApplication.temp;

        //获取失败则结束此界面
        if (site == null) {
            finish();
            return;
        }

        holder = new SitePropViewHolder(viewSiteDetails);

        holder.fillSitePropEditText(site);
    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.fab_submit)
    void submit() {
        Site newSite = holder.fromEditTextToSite();
        newSite.sid = site.sid;
        if(newSite==null){
            showSnackBar("规则缺少必要参数，请检查");
            return;
        }
        HViewerApplication.temp = newSite;

        List<Site> sites = HViewerApplication.siteHolder.getSites();
        for (int i = 0; i < sites.size(); i++) {
            Site currSite = sites.get(i);
            if (currSite.sid == newSite.sid) {
                sites.remove(i);
                sites.add(i, newSite);
            }
        }

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

}
