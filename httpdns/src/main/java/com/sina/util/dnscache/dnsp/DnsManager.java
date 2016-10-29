package com.sina.util.dnscache.dnsp;

import com.sina.util.dnscache.DNSCacheConfig;
import com.sina.util.dnscache.Tools;
import com.sina.util.dnscache.dnsp.impl.HttpPodDns;
import com.sina.util.dnscache.dnsp.impl.LocalDns;
import com.sina.util.dnscache.dnsp.impl.SinaHttpDns;
import com.sina.util.dnscache.dnsp.impl.UdpDns;
import com.sina.util.dnscache.log.HttpDnsLogManager;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.net.networktype.NetworkManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DnsManager implements IDns {

    ArrayList<IDnsProvider> mDnsProviders = new ArrayList<IDnsProvider>();
    private ArrayList<String> debugInfo = new ArrayList<String>();

    public DnsManager() {
        mDnsProviders.add(new SinaHttpDns());
        mDnsProviders.add(new HttpPodDns());
        mDnsProviders.add(new UdpDns());
        mDnsProviders.add(new LocalDns());
    }

    @Override
    public HttpDnsPack requestDns(String domain) {
        Collections.sort(mDnsProviders, new Comparator<IDnsProvider>() {
            @Override
            public int compare(IDnsProvider lhs, IDnsProvider rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                } else {
                    // 按照降序排序
                    return rhs.getPriority() - lhs.getPriority();
                }
            }
        });
        int size = mDnsProviders.size();
        for (int i = 0; i < size; i++) {
            IDnsProvider dp = mDnsProviders.get(i);
            Tools.log("TAG", "访问" + dp.getClass().getSimpleName() + "接口开始," + "\n优先级是：" + dp.getPriority() + "\n该模块是否开启：" + dp.isActivate()
                    + "\n该模块的API地址是：" + dp.getServerApi());
            if (dp.isActivate()) {
                HttpDnsPack dnsPack = dp.requestDns(domain);
                Tools.log("TAG", "访问" + dp.getClass().getSimpleName() + "接口结束," + "\n返回的结果是：" + dnsPack);
                if (null != dnsPack) {
                    if (DNSCacheConfig.DEBUG) {
                        if (null != debugInfo) {
                            debugInfo.add(dnsPack.rawResult + "[from:" + dp.getClass().getSimpleName() + "]");
                        }
                    }

                    dnsPack.localhostSp = NetworkManager.getInstance().getSPID();
                    if (dnsPack.device_sp == null || !dnsPack.device_sp.equals(dnsPack.localhostSp)) {
                        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_ERR_SPINFO, dnsPack.toJson());
                    }

                    return dnsPack;
                }
            }
        }

        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_ERR_DOMAININFO, "{\"domain\":" + "\"" + domain + "\"}");

        return null;
    }

    @Override
    public ArrayList<String> getDebugInfo() {
        return debugInfo;
    }

    @Override
    public void initDebugInfo() {
        debugInfo = new ArrayList<String>();
    }
}
