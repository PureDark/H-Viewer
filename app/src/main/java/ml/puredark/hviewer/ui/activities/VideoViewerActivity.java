package ml.puredark.hviewer.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.Video;
import ml.puredark.hviewer.helpers.Logger;

public class VideoViewerActivity extends BaseActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.web_view)
    WebView webView;

    private Video video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);
        ButterKnife.bind(this);
        setContainer(coordinatorLayout);

        //获取传递过来的Video实例
        if (HViewerApplication.temp instanceof Video)
            video = (Video) HViewerApplication.temp;

        //获取失败则结束此界面
        if (video == null || TextUtils.isEmpty(video.content)) {
            Toast.makeText(this, "数据错误，请刷新后重试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        HViewerApplication.temp = null;

        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setUserAgentString(getResources().getString(R.string.UA));
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebChromeClient(new WebChromeClient(){
            private View myView = null;
            private CustomViewCallback myCallback = null;

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (myCallback != null) {
                    myCallback.onCustomViewHidden();
                    myCallback = null ;
                    return;
                }

                long id = Thread.currentThread().getId();
                Logger.d("WidgetChromeClient", "rong debug in showCustomView Ex: " + id);

                ViewGroup parent = (ViewGroup) webView.getParent();
                String s = parent.getClass().getName();
                Logger.d("WidgetChromeClient", "rong debug Ex: " + s);
                parent.removeView( webView);
                parent.addView(view);
                myView = view;
                myCallback = callback;
            }

            public void onHideCustomView() {

                long id = Thread.currentThread().getId();
                Logger.d("WidgetChromeClient", "rong debug in hideCustom Ex: " + id);


                if (myView != null) {

                    if (myCallback != null) {
                        myCallback.onCustomViewHidden();
                        myCallback = null ;
                    }

                    ViewGroup parent = (ViewGroup) myView.getParent();
                    parent.removeView(myView);
                    parent.addView(webView);
                    myView = null;
                }
            }
        });
        webView.loadUrl(video.content);
    }

    @Override
    public void onDestroy(){
        webView.loadUrl("");
        super.onDestroy();
    }

}
