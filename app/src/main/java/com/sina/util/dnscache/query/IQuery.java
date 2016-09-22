package com.sina.util.dnscache.query;

import com.sina.util.dnscache.model.DomainModel;

/**
*
* 项目名称: DNSCache <br>
* 类名称: IQuery <br>
* 类描述: 查询模块 对外接口 <br>
* 创建人: fenglei <br>
* 创建时间: 2015-4-15 下午5:23:06 <br>
* 
* 修改人:  <br>
* 修改时间:  <br>
* 修改备注:  <br>
* 
* @version V1.0
*/
public interface IQuery {

    public DomainModel queryDomainIp(String sp, String host) ;

    
    public DomainModel getCacheDomainIp(String sp, String host) ;
}
