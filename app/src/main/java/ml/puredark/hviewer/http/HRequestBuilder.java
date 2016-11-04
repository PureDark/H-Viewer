package ml.puredark.hviewer.http;

import java.net.URLEncoder;

import ml.puredark.hviewer.beans.Site;
import okhttp3.Request;

public class HRequestBuilder extends Request.Builder {
    private boolean disableHProxy = false;

    public HRequestBuilder(){
        super();
    }

    public HRequestBuilder(boolean disableHProxy){
        super();
        this.disableHProxy = disableHProxy;
    }

    @Override
    public HRequestBuilder url(String url) {
        if (!disableHProxy && HProxy.isEnabled() && HProxy.isAllowRequest()) {
            HProxy proxy = new HProxy(url);
            this.header(proxy.getHeaderKey(), URLEncoder.encode(proxy.getHeaderValue()));
            super.url(proxy.getProxyUrl());
        } else {
            super.url(url);
        }

        return this;
    }
}
