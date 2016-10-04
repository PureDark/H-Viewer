/**
 * 
 */
package com.sina.util.dnscache.dnsp;

import com.sina.util.dnscache.model.HttpDnsPack;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * 项目名称: DNSCache 类名称: IJsonParser 类描述: 创建人: fenglei 创建时间: 2015-3-30 下午3:37:05
 * 
 * 修改人: 修改时间: 修改备注:
 * 
 * @version V1.0
 */
public interface IJsonParser {

    public HttpDnsPack JsonStrToObj(String jsonStr) throws Exception;

    public class JavaJSON_SINAHTTPDNS implements IJsonParser {

        @Override
        public HttpDnsPack JsonStrToObj(String jsonStr) throws Exception {
            HttpDnsPack dnsPack = new HttpDnsPack();
            JSONObject jsonObj = new JSONObject(jsonStr);
            dnsPack.rawResult = jsonStr;
            dnsPack.domain = jsonObj.getString("domain");
            dnsPack.device_ip = jsonObj.getString("device_ip");
            dnsPack.device_sp = jsonObj.getString("device_sp");

            JSONArray jsonarray = jsonObj.getJSONArray("dns");
            dnsPack.dns = new HttpDnsPack.IP[jsonarray.length()];
            for (int i = 0; i < dnsPack.dns.length; i++) {
                JSONObject tempJsonObj = new JSONObject(jsonarray.getString(i));
                dnsPack.dns[i] = new HttpDnsPack.IP();
                dnsPack.dns[i].ip = tempJsonObj.getString("ip");
                dnsPack.dns[i].ttl = tempJsonObj.getString("ttl");
                dnsPack.dns[i].priority = tempJsonObj.getString("priority");
            }
            return dnsPack;
        }
    }
}
