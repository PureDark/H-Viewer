package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;

import static ml.puredark.hviewer.HViewerApplication.getSites;

public class AddSiteActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_site)
    MaterialEditText inputSite;
    @BindView(R.id.btn_return)
    ImageView btnReturn;

    private DrawerArrowDrawable btnReturnIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /* 为返回按钮加载图标 */
        btnReturnIcon = new DrawerArrowDrawable(this);
        btnReturnIcon.setColor(getResources().getColor(R.color.white));
        btnReturn.setImageDrawable(btnReturnIcon);
        btnReturnIcon.setProgress(1f);
    }

    @OnClick(R.id.btn_return)
    void back(){
        onBackPressed();
    }

    @OnClick(R.id.btn_add)
    void add(){
        String rule = inputSite.getText().toString();
        try {
            Site newSite = new Gson().fromJson(rule, Site.class);
            int sid = HViewerApplication.getSites().size() + 1;
            newSite.sid = sid;
            HViewerApplication.addSite(newSite);
            Intent intent = new Intent();
            intent.putExtra("sid", sid);
            setResult(RESULT_OK, intent);
            finish();
        }catch (JsonSyntaxException e){
            Toast.makeText(this, "输入规则格式错误", Toast.LENGTH_SHORT).show();
        }
    }

}
