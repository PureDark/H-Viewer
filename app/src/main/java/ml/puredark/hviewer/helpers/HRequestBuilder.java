package ml.puredark.hviewer.helpers;

import okhttp3.Request;

public class HRequestBuilder extends Request.Builder {
    @Override
    public HRequestBuilder url(String url) {
        if (HProxy.isEnabled() && HProxy.isAllowRequest()) {
            HProxy proxy = new HProxy(url);
            this.header(proxy.getHeaderKey(), proxy.getHeaderValue());
            super.url(proxy.getProxyUrl());
        } else {
            super.url(url);
        }

        return this;
    }
}
