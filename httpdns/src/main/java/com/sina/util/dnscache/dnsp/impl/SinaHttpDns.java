package com.sina.util.dnscache.dnsp.impl;

import android.text.TextUtils;

import com.sina.util.dnscache.dnsp.DnsConfig;
import com.sina.util.dnscache.dnsp.IDnsProvider;
import com.sina.util.dnscache.dnsp.IJsonParser;
import com.sina.util.dnscache.dnsp.IJsonParser.JavaJSON_SINAHTTPDNS;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.net.ApacheHttpClientNetworkRequests;

import java.util.ArrayList;

public class SinaHttpDns implements IDnsProvider{

    private ApacheHttpClientNetworkRequests netWork;
    private JavaJSON_SINAHTTPDNS jsonObj;
    private String usingServerApi = "";
    public SinaHttpDns() {
        netWork = new ApacheHttpClientNetworkRequests();
        jsonObj = new IJsonParser.JavaJSON_SINAHTTPDNS();
    }

    @Override
    public HttpDnsPack requestDns(String domain) {
        String jsonDataStr = null;
        HttpDnsPack dnsPack = null;
        ArrayList<String> serverApis = new ArrayList<String>();
        serverApis.addAll(DnsConfig.SINA_HTTPDNS_SERVER_API);
        while (null == dnsPack && serverApis.size() > 0) {
            try {
                String api = "";
                int index = serverApis.indexOf(usingServerApi);
                if (index != -1) {
                    api = serverApis.remove(index);
                } else {
                    api = serverApis.remove(0);
                }
                String sina_httpdns_api_url = api + domain;
                jsonDataStr = netWork.requests(sina_httpdns_api_url);
                dnsPack = jsonObj.JsonStrToObj(jsonDataStr);
                usingServerApi = api;
            } catch (Exception e) {
                e.printStackTrace();
                usingServerApi = "";
            }
        }
        return dnsPack;
    }

    @Override
    public boolean isActivate() {
        return DnsConfig.enableSinaHttpDns;
    }
    

    @Override
    public String getServerApi() {
        String serverApi = "";
        if (!TextUtils.isEmpty(usingServerApi)) {
            serverApi = usingServerApi;
        } else {
            boolean yes = DnsConfig.SINA_HTTPDNS_SERVER_API.size() > 0;
            if (yes) {
                serverApi = DnsConfig.SINA_HTTPDNS_SERVER_API.get(0);
            }
        }
        return serverApi;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
