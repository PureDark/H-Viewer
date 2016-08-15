package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.gc.materialdesign.views.ButtonFlat;
import com.github.clans.fab.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.helpers.SitePropViewHolder;

import static ml.puredark.hviewer.HViewerApplication.siteHolder;

public class AddSiteActivity extends AnimationActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_add_site_json)
    View viewAddSiteJson;
    @BindView(R.id.view_site_details)
    View viewSiteDetails;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.input_site)
    MaterialEditText inputSite;
    @BindView(R.id.btn_parse_json)
    ButtonFlat btnParseJson;

    @BindView(R.id.fab_submit)
    FloatingActionButton fabSubmit;

    private SitePropViewHolder holder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        MDStatusBarCompat.setOrdinaryToolBar(this);

        setContainer(coordinatorLayout);

        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);

        holder = new SitePropViewHolder(viewSiteDetails);

    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.btn_json_input)
    void switchBetweenJsonAndDetail() {
        if (viewAddSiteJson.getVisibility() == View.GONE) {
            viewAddSiteJson.setVisibility(View.VISIBLE);
            viewSiteDetails.setVisibility(View.GONE);
            fabSubmit.setVisibility(View.GONE);
        } else {
            viewAddSiteJson.setVisibility(View.GONE);
            viewSiteDetails.setVisibility(View.VISIBLE);
            fabSubmit.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_qr_scan)
    void scan() {
        IntentIntegrator integrator = new IntentIntegrator(AddSiteActivity.this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setOrientationLocked(true);
        integrator.setPrompt("请扫描二维码");
        integrator.addExtra("SCAN_WIDTH", 480);
        integrator.addExtra("SCAN_HEIGHT", 480);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    @OnClick(R.id.btn_parse_json)
    void parseJson() {
        String rule = inputSite.getText().toString();
        Site newSite = parseSite(rule);
        if (newSite == null)
            return;
        holder.fillSitePropEditText(newSite);
        switchBetweenJsonAndDetail();
    }

    @OnClick(R.id.fab_submit)
    void submit() {
        Site newSite = holder.fromEditTextToSite();
        if (newSite == null) {
            showSnackBar("规则缺少必要参数，请检查");
            return;
        }

        HViewerApplication.temp = newSite;
        siteHolder.addSite(newSite);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null && result.getContents() != null) {
            HViewerHttpClient.get(result.getContents(), null, new HViewerHttpClient.OnResponseListener() {
                @Override
                public void onSuccess(String result) {
                    Site newSite = parseSite(result);
                    if (newSite == null)
                        return;
                    holder.fillSitePropEditText(newSite);
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    showSnackBar(error.getErrorString());
                }
            });
        }
    }

    private Site parseSite(String json) {
        try {
            Site site = new Gson().fromJson(json, Site.class);
            List<Site> sites = siteHolder.getSites();
            int sid = sites.get(sites.size() - 1).sid + 1;
            site.sid = sid;
            if (site.indexRule == null || site.galleryRule == null)
                showSnackBar("输入的规则缺少信息");
            return site;
        } catch (JsonSyntaxException e) {
            showSnackBar("输入规则格式错误");
            return null;
        }
    }

}
