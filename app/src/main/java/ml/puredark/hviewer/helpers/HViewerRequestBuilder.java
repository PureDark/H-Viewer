package ml.puredark.hviewer.helpers;

import okhttp3.Request;

public class HViewerRequestBuilder extends Request.Builder {


    @Override
    public HViewerRequestBuilder url(String url) {
        if (HProxy.isEnabled()) {
            HProxy proxy = new HProxy(url);
            this.header(proxy.getHeaderKey(), proxy.getHeaderValue());
            super.url(proxy.getProxyUrl());
        } else {
            super.url(url);
        }

        return this;
    }
}
