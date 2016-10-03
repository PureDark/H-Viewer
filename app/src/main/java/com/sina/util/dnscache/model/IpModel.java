/**
 * 
 */
package com.sina.util.dnscache.model;

import com.sina.util.dnscache.Tools;

import org.json.JSONException;
import org.json.JSONStringer;


/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: IpModel <br>
 * 类描述: ip数据模型 - 对应ip表 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-3-26 下午5:23:06 <br>
 * 
 * 修改人:  <br>
 * 修改时间:  <br>
 * 修改备注:  <br>
 * 
 * @version V1.0
 */
public class IpModel {
	
	public IpModel(){}
	
	/**
	 * 自增id <br>
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_ID }字段 <br>
	 */
	public long id = -1 ; 
	
	/**
	 * domain id 关联id
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_DOMAIN_ID }字段 <br>
	 */
	public long d_id = -1 ; 
	
	/**
	 * 服务器ip地址
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_PORT }字段 <br>
	 */
	public String ip = "" ;
	
	/**
	 * ip服务器对应的端口
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_PORT }字段 <br>
	 */
	public int port = -1 ; 
	
	/**
	 * ip服务器对应的sp运营商
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_SP }字段 <br>
	 */
	public String sp = "" ; 
	
	/**
	 * ip过期时间
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_TTL }字段 <br>
	 */
	public String ttl = "0" ; 
	
	/**
	 * ip服务器优先级-排序算法策略使用
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_PRIORITY }字段 <br>
	 */
	public String priority = "0" ; 
	/**
	 * 访问ip服务器的往返时延
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_PRIORITY }}字段 <br>
	 */
	public String rtt = "0" ; 
	
	/**
	 * ip服务器链接产生的成功数
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_SUCCESS_NUM }字段 <br>
	 */
	public String success_num = "0" ; 
	
	/**
	 * ip服务器链接产生的错误数
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_ERR_NUM }字段 <br>
	 */
	public String err_num = "0" ; 
	
	/**
	 * ip服务器最后成功链接时间
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_FINALLY_SUCCESS_TIME }字段 <br>
	 */
	public String finally_success_time = "0" ; 
	
	/**
	 * ip服务器最后失败链接时间
	 * 
	 * 该字段映射类 {@link com.sina.util.dnscache.cache.DBConstants#IP_COLUMN_FINALLY_FAIL_TIME }字段 <br>
	 */
	public String finally_fail_time = "0" ; 


	
	/**
	 * 评估体系 评分分值
	 */
	public float grade = 0 ; 
	

    public String toString(){

        String str = "*\n" ;

        str += "-- 服务器id = " + id + "\n" ;
        str += "-- 服务器ip = " + ip + "\n" ;
        str += "-- 域名ID索引 = " + d_id + "\n" ;
        str += "-- 服务器端口 = " + port + "\n" ;
        str += "-- 运营商 = " + sp + "\n" ;
        str += "-- 过期时间 = " + ttl + "\n" ;
        str += "-- 优先级 = " + priority + "\n" ;
        str += "-- 访问ip服务器的往返时延 = " + rtt + "\n" ;
        str += "-- 历史成功次数 = " + success_num + "\n" ;
        str += "-- 历史错误次数 = " + err_num + "\n" ;
        str += "-- 最后一次访问成功时间 = " + Tools.getStringDateShort(finally_success_time) + "\n" ;
        str += "-- 最后一次访问失败时间 = " + Tools.getStringDateShort(finally_fail_time) + "\n" ;
        str += "-- 系统对服务器的评分 = " + grade + "\n" ;
        str += "\n" ;
        
        return str ;
    }
    
    public String toJson(){
        JSONStringer jsonStringer = new JSONStringer();
        try {
            jsonStringer.object()//
                    .key("id").value(id)//
                    .key("d_id").value(d_id)//
                    .key("ip").value(ip)//
                    .key("port").value(port)//
                    .key("ttl").value(ttl)//
                    .key("priority").value(priority)//
                    .key("success_num").value(success_num)//
                    .key("err_num").value(err_num)//
                    .key("finally_success_time").value(finally_success_time)//
                    .key("finally_fail_time").value(finally_fail_time)//
                    .endObject();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
        return jsonStringer.toString();
    }

}
