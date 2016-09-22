package com.sina.util.dnscache;


/**
*
* 项目名称: DNSCache <br>
* 类名称: DomainInfo <br>
* 类描述: 为使用者封装的数据模型 <br>
* 创建人: fenglei <br>
* 创建时间: 2015-4-21 下午5:26:04 <br>
* 
* 修改人:  <br>
* 修改时间:  <br>
* 修改备注:  <br>
* 
* @version V1.0
*/
public class DomainInfo {

    /**
     * 工场方法
     *
     * @param ip
     * @param url
     * @param host
     * @return
     */
    public static DomainInfo DomainInfoFactory(String ip, String url, String host){

        url = Tools.getIpUrl(url, host, ip) ;

        return new DomainInfo( "" , url , host);
    }

    /**
     * 工场方法
     * @param serverIpArray
     * @param url
     * @param host
     * @return
     */
    public static DomainInfo[] DomainInfoFactory(String[] serverIpArray, String url, String host){

        DomainInfo[] domainArr = new DomainInfo[serverIpArray.length];

        for( int i = 0 ; i < serverIpArray.length ; i++  ){

            domainArr[i] = DomainInfoFactory( serverIpArray[i], url , host );
        }

        return domainArr ;
    }


    /**
     * 构造函数
     * @param id
     * @param url
     * @param host
     */
    public DomainInfo(String id, String url, String host){

        this.id = id ;
        this.url = url ;
        this.host = host ;

        this.startTime = String.valueOf( System.currentTimeMillis() );
    }

    /**
     * id
     */
    public String id = null;

    /**
     * 可以直接使用的url  已经替换了host为ip
     */
    public String url = null;

    /**
     * 需要设置到 http head 里面的主机头
     */
    public String host = "";



    /**
     * 返回的内容体
     */
    public String data = null ;


    /**
     * 开始请求的时间
     */
    public String startTime = null ;


    /**
     * 请求结束的时间 请求超时 结束时间为null
     */
    public String stopTime = null ;


    /**
     * 请求的状态值 200 \ 404 \ 500 等
     */
    public String code = null ;




    /**
     * 返回 url 信息
     * @return
     */
    public String toString(){

        String str = "DomainInfo: \n" ;
        str += "id = " + id + "\n" ;
        str += "url = " + url + "\n" ;
        str += "host = " + host + "\n" ;
        str += "data = " + data + "\n" ;
        str += "startTime = " + startTime + "\n" ;
        str += "stopTime = " + stopTime + "\n" ;
        str += "code = " + code + "\n" ;

        return str ;
    }


}
