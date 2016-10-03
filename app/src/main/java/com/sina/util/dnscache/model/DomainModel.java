/**
 * 
 */
package com.sina.util.dnscache.model;

import com.sina.util.dnscache.Tools;

import org.json.JSONException;
import org.json.JSONStringer;

import java.util.ArrayList;

/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: DomainModel <br>
 * 类描述: 域名数据模型 - 对应domain表 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-3-26 下午5:04:01 <br>
 * 
 * 修改人:  <br>
 * 修改时间:  <br>
 * 修改备注: <br>
 * 
 * @version V1.0
 */
public class DomainModel {

	
	public DomainModel(){}
	
	/**
	 * 自增id <br>
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants } DOMAIN_COLUMN_ID 字段 <br>
	 */
	public long id = -1 ;
	
	/**
	 * 域名 <br>
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants } DOMAIN_COLUMN_DOMAIN 字段 <br>
	 */
	public String domain = "" ; 
	
	/**
	 * 运营商 <br>
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants } DOMAIN_COLUMN_SP 字段 <br>
	 */
	public String sp = "" ; 
	
	/**
	 * 域名过期时间 <br>
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants } DOMAIN_COLUMN_TTL 字段 <br>
	 */
	public String ttl = "0" ;

    /**
     * 域名最后查询时间 <br>
     *
     * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants } DOMAIN_COLUMN_TIME 字段 <br>
     */
    public String time = "0" ;




    /**
	 * 域名关联的ip数组 <br>
	 */
	public ArrayList<IpModel> ipModelArr = null ; 



    public String toString(){
        String str = "" ;
        str += "域名ID = " + id + "\n" ;
        str += "域名 = " + domain + "\n" ;
        str += "运营商ID = " + sp + "\n" ;
        str += "域名过期时间： = " + ttl + "\n" ;
        str += "域名最后查询时间：" + Tools.getStringDateShort(time) + "\n" ;
        if( ipModelArr != null && ipModelArr.size() > 0 ){
            for( IpModel temp : ipModelArr ){
            	if( temp == null ) continue ; 
                str += "-- " + temp.toString() ;
            }
        }

        str += "------------------------------------------------------\n\n";
        return str ;
    }

    public String tojson(){
        JSONStringer jsonStringer = new JSONStringer();
        try {
            StringBuilder ipmodelStr = new StringBuilder();
            ipmodelStr.append("[");
            if (null != ipModelArr) {
                for (int i = 0; i < ipModelArr.size(); i++) {
                    IpModel ipModel = ipModelArr.get(i);
                    ipmodelStr.append(ipModel.toJson() + ",");
                }
            }
            if (ipmodelStr.toString().endsWith(",")) {
                ipmodelStr.deleteCharAt(ipmodelStr.length() - 1);
            }
            ipmodelStr.append("]");
            
            jsonStringer.object()//
                    .key("domain").value(domain)//
                    .key("sp").value(sp)//
                    .key("ttl").value(ttl)//
                    .key("time").value(time)//
                    .key("ipModelArr").value(ipmodelStr.toString())//
                    .endObject();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
        return jsonStringer.toString();
    }
}