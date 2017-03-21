package ml.puredark.hviewer.http;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

public class HProxy {
    //    private static final String PROXY_DEFAULT_SERVER = "https://h-viewer-proxy.herokuapp.com";
    private static final String PROXY_DEFAULT_SERVER = null;
    private String mTarget;
    private String mProxyUrl;

    public HProxy(String target) {
        this.mTarget = target;

        int protocolEndPos = target.indexOf(":");
        int hostStartPos = protocolEndPos + 3;
        int hostEndPos = target.indexOf("/", hostStartPos);
        if (hostEndPos == -1) {
            hostEndPos = target.length();
        }

        this.mProxyUrl = getProxyServer() + target.substring(hostEndPos);
        if (!mProxyUrl.startsWith("http"))
            mProxyUrl = mTarget;
    }

    public static boolean isEnabled() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingFragment.KEY_PREF_PROXY_ENABLED, false);
    }

    public static boolean isAllowRequest() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingFragment.KEY_PREF_PROXY_REQUEST, false);
    }

    public static boolean isAllowPicture() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingFragment.KEY_PREF_PROXY_PICTURE, false);
    }

    private static String getProxyServer() {
//        return PROXY_DEFAULT_SERVER;
        String proxy = (String) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingFragment.KEY_PREF_PROXY_SERVER, "");
        if (proxy.startsWith("http://") || proxy.startsWith("https://"))
            return proxy;
        else
            return PROXY_DEFAULT_SERVER;
    }

    public String getProxyUrl() {
        return mProxyUrl;
    }

    public String getHeaderKey() {
        return "origin-url";
    }

    public String getHeaderValue() {
        return mTarget;
    }
}
