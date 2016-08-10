package ml.puredark.hviewer.helpers;

import okhttp3.Request;

public class HViewerRequestBuilder extends Request.Builder {
    private final String proxyAddress = "https://h-viewer-proxy.herokuapp.com";

    @Override
    public HViewerRequestBuilder url(String url) {
        int protocolEndPos = url.indexOf(":");
        int hostStartPos = protocolEndPos + 3;
        int hostEndPos = url.indexOf("/", hostStartPos);

        String proxyUrl = proxyAddress + url.substring(hostEndPos);
        this.header("origin-url", url);
        super.url(proxyUrl);

        return this;
    }
}
