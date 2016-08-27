package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;

public class LoginActivity extends AnimationActivity {

    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.web_view)
    WebView webView;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;

    private Site site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MDStatusBarCompat.setOrdinaryToolBar(this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            toolbar.setFitsSystemWindows(true);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) appBar.getLayoutParams();
            lp.height = (int) (MDStatusBarCompat.getStatusBarHeight(this) +
                    getResources().getDimension(R.dimen.tool_bar_height));
            appBar.setLayoutParams(lp);
        }

        setContainer(coordinatorLayout);
        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);

        //获取传递过来的Site实例
        if (HViewerApplication.temp instanceof Site)
            site = (Site) HViewerApplication.temp;

        //获取失败则结束此界面
        if (site == null || site.loginUrl == null || "".equals(site.loginUrl)) {
            Toast.makeText(this, "没有定义登录地址", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText("登录" + site.title);

        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        settings.setLoadWithOverviewMode(true);
//        settings.setUseWideViewPort(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(url);
                site.cookie = cookies;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(url);
                site.cookie = cookies;
                showSnackBar("请在登录成功后返回到主界面");
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl(site.loginUrl);
    }

    @OnClick(R.id.btn_return)
    void back() {
        HViewerApplication.temp = site;
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        onBackPressed();
    }
}
