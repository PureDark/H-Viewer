package ml.puredark.hviewer.http;

import com.sina.util.dnscache.DNSCache;
import com.sina.util.dnscache.DomainInfo;
import com.sina.util.dnscache.Tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

/**
 * Created by PureDark on 2016/9/22.
 */

public class HttpDns implements Dns {

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        DomainInfo[] infoList = DNSCache.getInstance().getDomainServerIp(hostname);
        if (infoList != null) {
            DomainInfo domainModel = infoList[0];
            String ip = Tools.getHostName(domainModel.url);
            if (Tools.isIPV4(ip)) {
                List<InetAddress> inetAddresses;
                try {
                    inetAddresses = Arrays.asList(InetAddress.getAllByName(ip));
                    return inetAddresses;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        return Dns.SYSTEM.lookup(hostname);
    }
}