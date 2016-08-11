package ml.puredark.hviewer.helpers;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.activities.SettingActivity;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

public class HProxy {
    private String mTarget;
    private String mProxyUrl;

    public static boolean isEnabled() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingActivity.SettingFragment.KEY_PREF_PROXY_ENABLED, false);
    }

    public static boolean isAllowRequest() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingActivity.SettingFragment.KEY_PREF_PROXY_REQUEST, false);
    }

    public static boolean isAllowPicture() {
        return (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingActivity.SettingFragment.KEY_PREF_PROXY_PICTURE, false);
    }

    private static String getProxyServer() {
        return (String) SharedPreferencesUtil.getData(HViewerApplication.mContext,
                SettingActivity.SettingFragment.KEY_PREF_PROXY_SERVER, false);
    }

    public HProxy(String target) {
        this.mTarget = target;

        int protocolEndPos = target.indexOf(":");
        int hostStartPos = protocolEndPos + 3;
        int hostEndPos = target.indexOf("/", hostStartPos);
        if (hostEndPos == -1) {
            hostEndPos = target.length();
        }

        this.mProxyUrl = getProxyServer() + target.substring(hostEndPos);
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
