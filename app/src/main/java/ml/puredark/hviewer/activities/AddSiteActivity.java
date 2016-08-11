package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nineoldandroids.animation.Animator;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.customs.AnimationOnActivity;

public class AddSiteActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_site)
    MaterialEditText inputSite;
    @BindView(R.id.btn_return)
    ImageView btnReturn;

    private DrawerArrowDrawable btnReturnIcon;

    //是否动画中
    private boolean animating = false;

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
            if(newSite.indexRule==null||newSite.galleryRule==null)
                Toast.makeText(this, "输入的规则缺少信息", Toast.LENGTH_SHORT).show();
            HViewerApplication.addSite(newSite);
            Intent intent = new Intent();
            intent.putExtra("sid", sid);
            setResult(RESULT_OK, intent);
            finish();
        }catch (JsonSyntaxException e){
            Toast.makeText(this, "输入规则格式错误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (animating)
            return;
        else
            AnimationOnActivity.reverse(btnReturnIcon, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    animating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animating = false;
                    finish();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        //super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (animating) return false;
        return super.dispatchTouchEvent(event);
    }

}
