/**
 * 
 */
package com.sina.util.dnscache.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabaseLockedException;

import com.sina.util.dnscache.model.DomainModel;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.model.IpModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static android.R.id.list;

/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: DnsCacheManager <br>
 * 类描述: Dnd缓存层管理类 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-3-26 下午6:10:10 <br>
 * 
 * 修改人:  <br>
 * 修改时间:  <br>
 * 修改备注:  <br>
 * 
 * @version V1.0
 */
public class DnsCacheManager extends DNSCacheDatabaseHelper implements IDnsCache{

    public DnsCacheManager(Context context) {
		super(context);
		db = new DNSCacheDatabaseHelper(context) ;
	}

    /**
     * 数据库操作类
     */
    private DNSCacheDatabaseHelper db = null ; 
    
	/**
     * 缓存初始容量值
     */
    private final int INIT_SIZE = 8;

    /**
     * 缓存最大容量值
     */
    private final int MAX_CACHE_SIZE = 32;

    /**
     * 延迟差值，单位s
     */
    public static int ip_overdue_delay = 20 ;

    /**
     * 缓存链表
     */
	@SuppressLint("NewApi")
	private ConcurrentHashMap<String, DomainModel> data = new ConcurrentHashMap<String, DomainModel>(INIT_SIZE, MAX_CACHE_SIZE);


    /////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 获取url缓存
	 * @param url
	 * @return
	 */
    @Override
	public DomainModel getDnsCache(String sp, String url){

    	
		DomainModel model = data.get(url) ;
		
		if( model == null ){
			//缓存中没有从数据库中查找
            ArrayList<DomainModel> list = null;
            try {
                list = (ArrayList<DomainModel>) db.QueryDomainInfo(url, sp);
            } catch(SQLiteDatabaseLockedException e){
                e.printStackTrace();
            }
			if( list != null && list.size() != 0){
				model = list.get( list.size() - 1 ) ;
			}
			
			//查询到数据 添加到缓存中
	        if( model != null )addMemoryCache(url, model) ;
			
		}

		if( model != null){
			//检测是否过期  
            if( isExpire(model, ip_overdue_delay) ){
                model = null ;
            }
		}

		return model ; 
	}


    /**
     * 插入本地 cache 缓存
     *
     * @param dnsPack
     * @return
     */
    public DomainModel insertDnsCache(HttpDnsPack dnsPack){

        DomainModel domainModel = new DomainModel() ;
        domainModel.domain = dnsPack.domain ;
        domainModel.sp = dnsPack.localhostSp ;
        domainModel.time = String.valueOf(System.currentTimeMillis());

        domainModel.ipModelArr = new ArrayList<IpModel>();

        int domainTTL = 60 ;
        for( HttpDnsPack.IP temp : dnsPack.dns ){

            IpModel ipModel = new IpModel() ;
            ipModel.ip = temp.ip ;
            ipModel.ttl = temp.ttl ;
            ipModel.priority = temp.priority ;

            ipModel.port = 80 ;
            ipModel.sp = domainModel.sp ;
            
            domainModel.ipModelArr.add(ipModel) ;
            try {
                domainTTL = Math.min(domainTTL, Integer.valueOf(ipModel.ttl));
            }catch(NumberFormatException e){
            }

        }

        domainModel.ttl = String.valueOf(domainTTL);

        if( domainModel != null && domainModel.ipModelArr != null && domainModel.ipModelArr.size() > 0){
	        // 插入数据库
            try {
                domainModel = super.addDomainModel(dnsPack.domain, dnsPack.localhostSp, domainModel);
            }catch (SQLiteDatabaseLockedException e){
                e.printStackTrace();
            }
	        // 插入内存缓存
	        addMemoryCache( domainModel.domain, domainModel );
        }

        return domainModel ;
    }

    /**
     * 获取即将过期的 domain 数据
     * @return
     */
    @Override
	public ArrayList<DomainModel> getExpireDnsCache() {

		ArrayList<DomainModel> listDomain = new ArrayList<DomainModel>();

		for (Entry<String, DomainModel> entry : data.entrySet()) {
			DomainModel temp = data.get(entry.getKey());
			if (isExpire(temp)) {
				listDomain.add(temp);
			}
		}

		return listDomain;
	}


    /**
     * 获取缓存中全部的 DomainModel数据
     */
    public ArrayList<DomainModel> getAllMemoryCache() {
    	ArrayList<DomainModel> list = new ArrayList<DomainModel>() ; 
		for (Entry<String, DomainModel> entry : data.entrySet()) {
			DomainModel temp = data.get(entry.getKey());
			list.add(temp) ; 
		}
    	return list; 
    }    
    
    /**
     * 清除全部缓存
     */
    public void clear(){
        super.clear();
        data.clear();
    }
    
    /**
     * 清除内存缓存
     */
    public void clearMemoryCache(){
        data.clear();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////


    /**
	 * 添加url缓存
	 * @param model
	 */
	public void addMemoryCache(String url, DomainModel model) {

		if( model == null ) return ; 
		if( model.ipModelArr == null ) return ; 
		if( model.ipModelArr.size() <= 0 ) return ; 
		for( IpModel ipModel : model.ipModelArr ){
			if( ipModel == null ) return ; 
		}
		
		// 检测下缓存中是否存在， 如果存在删除旧数据在添加
		DomainModel temp = data.get(url);
		if (temp != null) {
			data.remove(url);
		}

		data.put(url, model);
	}


    /**
     * 检测是否过期，提前3秒刷一下
     * @param domainModel
     * @return
     */
    private boolean isExpire(DomainModel domainModel) {
        return isExpire(domainModel, -3);
    }

    /**
     * 检测是否过期
     * 
     * @param domainModel
     * @return
     */
    private boolean isExpire(DomainModel domainModel, long difference) {
        long queryTime = Long.parseLong(domainModel.time) / 1000;
        long ttl = Long.parseLong(domainModel.ttl);
        long newTime = System.currentTimeMillis() / 1000;
        if ((newTime - queryTime) > (ttl + difference)) {
            return true;
        }
        return false;
    }

    @Override
    public void setSpeedInfo(List<IpModel> ipModels) {
        updateIpInfo(ipModels);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

}
