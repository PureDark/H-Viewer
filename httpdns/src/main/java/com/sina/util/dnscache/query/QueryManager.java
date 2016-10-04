package com.sina.util.dnscache.query;

import com.sina.util.dnscache.cache.IDnsCache;
import com.sina.util.dnscache.model.DomainModel;
import com.sina.util.dnscache.model.IpModel;
import com.sina.util.dnscache.speedtest.SpeedtestManager;

import java.net.InetAddress;
import java.util.ArrayList;

/**
*
* 项目名称: DNSCache <br>
* 类名称: QueryManager <br>
* 类描述: 查询模块管理类 <br>
* 创建人: fenglei <br>
* 创建时间: 2015-4-15 下午5:23:06 <br>
* 
* 修改人:  <br>
* 修改时间:  <br>
* 修改备注:  <br>
* 
* @version V1.0
*/
public class QueryManager implements IQuery {

    private IDnsCache dnsCache = null ;

    public QueryManager( IDnsCache dnsCache ){

        this.dnsCache = dnsCache ;
    }

    /**
     * 根据host名字查询server ip
     * @return
     */
    @Override
    public DomainModel queryDomainIp(String sp, String host) {

        // 从缓存中查询，如果为空 情况有两种 1：没有缓存数据 2：数据过期
        DomainModel domainModel = getCacheDomainIp(sp, host);

        // 如果缓存是无效数据，则取local返回
        if (inValidData(domainModel)) {
            
            String[] ipList = null;
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                ipList = new String[addresses.length];
                for (int i = 0; i < addresses.length; i++) {
                    ipList[i] = addresses[i].getHostAddress();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null != ipList) {
                domainModel = new DomainModel();
                domainModel.id = -1;
                domainModel.domain = host;
                domainModel.sp = sp;
                domainModel.ttl = "60";
                domainModel.time = String.valueOf(System.currentTimeMillis());
                domainModel.ipModelArr = new ArrayList<IpModel>();
                for (int i = 0; i < ipList.length; i++) {
                    domainModel.ipModelArr.add(new IpModel());
                    domainModel.ipModelArr.get(i).ip = ipList[i];
                    domainModel.ipModelArr.get(i).sp = sp;
                }
                dnsCache.addMemoryCache(host, domainModel);
            }
        }
        return domainModel;
    }
    
    /**
     * 是否是无效数据。判断依据：
     * 1.domainModel为null
     * 2.domainModel.ipModelArr == null
     * 3.domainModel.ipModelArr.size() == 0
     * 4.domainModel.ipModelArr的rtt都是计算都出错，即都不通
     * @param domainModel
     * @return
     */
    private boolean inValidData(DomainModel domainModel) {
        if (domainModel == null || domainModel.ipModelArr == null || domainModel.ipModelArr.size() == 0) {
            return true;
        }
        ArrayList<IpModel> ips = domainModel.ipModelArr;
        for (int i = 0; i < ips.size(); i++) {
            IpModel ipModel = ips.get(i);
            //只要有一个是通的，就认为是有效数据
            if (!("" + SpeedtestManager.MAX_OVERTIME_RTT).equals(ipModel.rtt)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从缓存层获取获取数据
     * @param sp
     * @param host
     * @return
     */
    public DomainModel getCacheDomainIp(String sp, String host){
    	return dnsCache.getDnsCache(sp, host) ;
    }
}
