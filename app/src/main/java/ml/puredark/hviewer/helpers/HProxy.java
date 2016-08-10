package ml.puredark.hviewer.helpers;

public class HProxy {
    private final String PROXY_ADDRESSS = "https://h-viewer-proxy.herokuapp.com";

    private String mTarget;
    private String mProxyUrl;

    public static boolean isEnabled() {
        return true;
    }

    public HProxy(String target) {
        this.mTarget = target;

        int protocolEndPos = target.indexOf(":");
        int hostStartPos = protocolEndPos + 3;
        int hostEndPos = target.indexOf("/", hostStartPos);

        this.mProxyUrl = PROXY_ADDRESSS + target.substring(hostEndPos);
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
