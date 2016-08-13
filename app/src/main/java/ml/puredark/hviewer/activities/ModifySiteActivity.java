package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.customs.SitePropViewHolder;

public class ModifySiteActivity extends AnimationActivity {

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

        setSupportActionBar(toolbar);
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
            Toast.makeText(this, "规则缺少必要参数，请检查", Toast.LENGTH_SHORT).show();
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
