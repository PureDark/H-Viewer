package com.sina.util.dnscache.cache;


/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: DBConstants <br>
 * 类描述: 数据库名、表明、列明 相关常量定义 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-3-26 下午3:54:59 <br>
 * 
 * 修改人:  <br>
 * 修改时间:  <br>
 * 修改备注:  <br>
 * 
 * @version V1.0
 */
public interface DBConstants {
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * db 数据库名字 
	 */
	public static final String DATABASE_NAME = "dns_ip_info.db" ; 
	/**
	 * db 版本
	 */
	public static final int DATABASE_VERSION = 4 ; 
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * domain表名称、列名定义
	 */
	public static final String TABLE_NAME_DOMAIN = "domain" ; 
	/**
	 * domain 自增id
	 */
	public static final String DOMAIN_COLUMN_ID = "id";
	/**
	 * 域名
	 */
	public static final String DOMAIN_COLUMN_DOMAIN = "domain" ;
	/**
	 * 运营商
	 */
	public static final String DOMAIN_COLUMN_SP = "sp" ;
	/**
	 * 域名过期时间
	 */
	public static final String DOMAIN_COLUMN_TTL = "ttl" ;
    /**
     * 最后查询时间
     */
    public static final String DOMAIN_COLUMN_TIME = "time" ;

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	/**
	 * ip表名称、列名定义
	 */
	public static final String TABLE_NAME_IP = "ip" ;
	/**
	 * ip 自增id
	 */
	public static final String IP_COLUMN_ID = "id" ;
	/**
	 * domain 关联id
	 */
	public static final String IP_COLUMN_DOMAIN_ID = "d_id" ; 
	/**
	 * 服务器 ip地址
	 */
	public static final String IP_COLUMN_IP = "ip" ; 
	/**
	 * ip服务器对应的端口
	 */
	public static final String IP_COLUMN_PORT = "port" ;
	/**
	 * ip服务器对应的sp运营商
	 */
	public static final String IP_COLUMN_SP = "sp";
	/**
	 * ip服务器对应域名过期时间
	 */
	public static final String IP_COLUMN_TTL = "ttl";
	/**
	 * ip服务器优先级-排序算法策略使用
	 */
	public static final String IP_COLUMN_PRIORITY = "priority" ;
	/**
	 * ip服务器访问延时时间(可用ping或http发送空包实现)。单位ms
	 */
	public static final String IP_COLUMN_RTT = "rtt" ;
	/**
	 * ip服务器链接产生的成功数
	 */
	public static final String IP_COLUMN_SUCCESS_NUM = "success_num" ;
	/**
	 * ip服务器链接产生的错误数
	 */
	public static final String IP_COLUMN_ERR_NUM = "err_num" ;
	/**
	 * ip服务器最后成功链接时间
	 */
	public static final String IP_COLUMN_FINALLY_SUCCESS_TIME = "finally_success_time" ; 
	/**
	 * ip服务器最后失败链接时间
	 */
	public static final String IP_COLUMN_FINALLY_FAIL_TIME = "finally_fail_time" ; 
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	
	/**
	 * 链接失败表名称、列定义（主要用来上报通知异常）
	 */
	public static final String TABLE_NAME_CONNECT_FAIL = "connect_fail" ;
	/**
	 * 链接失败表 自曾id
	 */
	public static final String CONNECT_FAIL_ID = "id" ;
	/**
	 * 链接失败的ip地址
	 */
	public static final String CONNECT_FAIL_IP = "ip" ;
	/**
	 * 链接失败服务器的端口号
	 */
	public static final String CONNECT_FAIL_PORT = "port" ; 
	/**
	 * 链接失败的错误代码，（一般都是http的错误代码）
	 */
	public static final String CONNECT_FAIL_ERRCODE = "errcode" ;
	/**
	 * 链接失败时本地网络类型
	 */
	public static final String CONNECT_FAIL_NETWORK_TYPE = "network_type" ;
	/**
	 * 链接失败时如果是手机运营商，则统计运营商sp—code（为了重现现场收集数据）
	 */
	public static final String CONNECT_FAIL_SPCODE = "spcode" ; 
	/**
	 * 链接该服务器总共的错误次数
	 */
	public static final String CONNECT_FAIL_COUNT = "count" ;
	/**
	 * 链接该服务器最后失败时间
	 */
	public static final String CONNECT_FAIL_FINALLY_TIME = "finally_time" ;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * 创建 domain 表 sql 语句
	 */
	public static  final String CREATE_DOMAIN_TABLE_SQL = 
			"CREATE TABLE " + TABLE_NAME_DOMAIN + " (" + 
					DOMAIN_COLUMN_ID + " INTEGER PRIMARY KEY," +
					DOMAIN_COLUMN_DOMAIN + " TEXT," +
					DOMAIN_COLUMN_SP + " TEXT," +
					DOMAIN_COLUMN_TTL + " TEXT," +
                    DOMAIN_COLUMN_TIME + " TEXT" +
			");";
	
	/**
	 * 创建 ip 表 sql 语句
	 */
	public static final String CREATE_IP_TEBLE_SQL = 
			"CREATE TABLE " + TABLE_NAME_IP + " (" +
					IP_COLUMN_ID + " INTEGER PRIMARY KEY," +
					IP_COLUMN_DOMAIN_ID + " INTEGER," + 
					IP_COLUMN_IP + " INTEGER," + 
					IP_COLUMN_PORT + " INTEGER," +
					IP_COLUMN_SP + " TEXT," +
					IP_COLUMN_TTL + " TEXT," +
					IP_COLUMN_PRIORITY + " INTEGER," +
					IP_COLUMN_RTT + " INTEGER," +
					IP_COLUMN_SUCCESS_NUM + " INTEGER," +
					IP_COLUMN_ERR_NUM + " INTEGER," +
					IP_COLUMN_FINALLY_SUCCESS_TIME + " TEXT," +
					IP_COLUMN_FINALLY_FAIL_TIME + " TEXT" +
			");";
	
	/**
	 * 创建 connect_fail 表 sql 语句
	 */
	public static final String CREATE_CONNECT_FAIL_TABLE_SQL = 
			"CREATE TABLE " + TABLE_NAME_CONNECT_FAIL + " (" + 
					CONNECT_FAIL_ID + " INTEGER PRIMARY KEY," +
					CONNECT_FAIL_IP + " TEXT," +
					CONNECT_FAIL_PORT + " INGEGER," +
					CONNECT_FAIL_ERRCODE + " TEXT," +
					CONNECT_FAIL_NETWORK_TYPE + " TEXT," +
					CONNECT_FAIL_SPCODE + " TEXT," +
					CONNECT_FAIL_COUNT + " INGEGER," +
					CONNECT_FAIL_FINALLY_TIME + " TEXT" +
			");" ;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
