/**
 * 
 */
package com.sina.util.dnscache.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sina.util.dnscache.model.DomainModel;
import com.sina.util.dnscache.model.IpModel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: DNSCacheDatabaseHelper <br>
 * 类描述: 缓存数据库 创建、更新、删除、增删改查相关操作 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-3-26 下午4:04:23 <br>
 * 
 * 修改人:  <br>
 * 修改时间:  <br>
 * 修改备注:  <br>
 * 
 * @version V1.0
 */
public class DNSCacheDatabaseHelper extends SQLiteOpenHelper implements DBConstants{

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
    /**
     * 资源锁
     */
    private final static byte synLock[] = new byte[1];
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * 构造函数
     * @param context
     */
    public DNSCacheDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建数据库
     * 
     * 残酷的现实告诉我们，创建多个表时，要分开多次执行db.execSQL方法！！
     */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//Log.d("DB", "onCreate") ;
		db.execSQL(CREATE_DOMAIN_TABLE_SQL);
		db.execSQL(CREATE_IP_TEBLE_SQL);
		db.execSQL(CREATE_CONNECT_FAIL_TABLE_SQL);
	}

	/**
	 * 数据库版本更新策略（直接放弃旧表）
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//Log.d("DB", "onUpgrade") ;
        if (oldVersion != newVersion) {
            // 其它情况，直接放弃旧表.
            db.beginTransaction();
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DOMAIN + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_IP + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONNECT_FAIL + ";");
            db.setTransactionSuccessful();
            db.endTransaction();
            onCreate(db);
        }
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 添加一条新的记录 。如果域名重复删除旧数据。
	 *
	 * @param url
	 * @param sp
	 * @param model
	 * @return
	 */
    public DomainModel addDomainModel(String url, String sp, DomainModel model) {
        synchronized (synLock) {
            // 过滤重复数据
            ArrayList<DomainModel> domainList = (ArrayList<DomainModel>) QueryDomainInfo(model.domain, model.sp);
            if (domainList != null && domainList.size() > 0) {
                //之所以删除该记录是为了保证 与过期ip数据断开关联关系。
                //比如：第一次拉下来的是1、2、3ip数据。第二次拉下来是4、5、6ip数据。那么1、2、3关联的did就会失效。 即：1、2、3记录成为无效数据
                deleteDomainInfo(domainList);
            }
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            try {
                db.beginTransaction();
                cv.put(DOMAIN_COLUMN_DOMAIN, model.domain);
                cv.put(DOMAIN_COLUMN_SP, model.sp);
                cv.put(DOMAIN_COLUMN_TTL, model.ttl);
                cv.put(DOMAIN_COLUMN_TIME, model.time);
                model.id = db.insert(TABLE_NAME_DOMAIN, null, cv);
                for (int i = 0; i < model.ipModelArr.size(); i++) {
                    IpModel temp = model.ipModelArr.get(i);
                    // 更新内存中IP表中的d_id字段
                    IpModel oldModel = getIpModel(temp.ip, sp);
                    IpModel ipModel = null;
                    // 若数据库中无此条数据，则插入一条
                    if (oldModel == null) {
                        ipModel = temp;
                        ipModel.d_id = model.id;
                        ipModel.id = addIpModel(ipModel);
                    } else {
                        ipModel = oldModel;
                        ipModel.d_id = model.id;
                        // 若数据库中存在此条数据，则更新对应的domainId即可
                        updateIpInfo(ipModel);
                    }
                    model.ipModelArr.remove(i);
                    model.ipModelArr.add(i, ipModel);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
                // 上报错误
            } finally {
                if(db.isOpen())
                    db.endTransaction();
                db.close();
            }
            return model;
        }
    }
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 根据url 获取缓存domain
	 * 
	 * @param domain
	 * @param sp
	 * @return
	 */
	public List<DomainModel> QueryDomainInfo(String domain, String sp){

		synchronized (synLock) {
			List<DomainModel> list = new ArrayList<DomainModel>() ;
	        StringBuilder sql = new StringBuilder();
	        sql.append("SELECT * FROM ");
	        sql.append(TABLE_NAME_DOMAIN);
	        sql.append(" WHERE ");
	        sql.append(DOMAIN_COLUMN_DOMAIN);
	        sql.append(" =? ");
	        sql.append(" AND ");
	        sql.append(DOMAIN_COLUMN_SP);
	        sql.append(" =? ;");
	        SQLiteDatabase db = getReadableDatabase();
	        Cursor cursor = null;
			try {
				cursor = db.rawQuery(sql.toString(), new String[] { domain, sp });
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					do {
						DomainModel model = new DomainModel() ;
						model.id = cursor.getInt(cursor.getColumnIndex(DOMAIN_COLUMN_ID));
						model.domain = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_DOMAIN));
						model.sp = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_SP));
						model.ttl = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_TTL));
                        model.time = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_TIME));
						model.ipModelArr = (ArrayList<IpModel>) QueryIpModelInfo(model) ; 
						list.add(model) ;
	                } while (cursor.moveToNext());
				}
	
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} finally {
				cursor.close();
				db.close() ;
	        }
			return list ; 
		}
	}
	
	/**
	 * 通过url获取服务器ip信息
	 * 根据 domainModel 获取Ipmodel 对象。 
	 * @param domainModel
	 * @return
	 */
	private List<IpModel> QueryIpModelInfo( DomainModel domainModel ){
		
		// 内部方法 不需要加锁
			List<IpModel> list = new ArrayList<IpModel>() ; 
	        StringBuilder sql = new StringBuilder();
	        sql.append("SELECT * FROM ");
	        sql.append(TABLE_NAME_IP);
	        sql.append(" WHERE ");
	        sql.append(IP_COLUMN_DOMAIN_ID);
	        sql.append(" =? ;");
	        SQLiteDatabase db = getReadableDatabase();
	        Cursor cursor = null;
	        try {
	        	cursor = db.rawQuery(sql.toString(), new String[] { String.valueOf( domainModel.id ) });
	        	if (cursor != null && cursor.getCount() > 0) {
	        		cursor.moveToFirst();
	        		do{
	        			IpModel ip = new IpModel() ; 
	        			ip.id = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_ID));
	        			ip.d_id = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_DOMAIN_ID));
	        			ip.ip = cursor.getString(cursor.getColumnIndex(IP_COLUMN_IP));
//                        ip.ip = String.valueOf( Tools.longToIP( Long.parseLong( ip.ip ) ) );
	        			ip.port = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_PORT));
	        			ip.sp = cursor.getString(cursor.getColumnIndex(IP_COLUMN_SP));
	        			ip.ttl = cursor.getString(cursor.getColumnIndex(IP_COLUMN_TTL));
	        			ip.priority = cursor.getString(cursor.getColumnIndex(IP_COLUMN_PRIORITY));
	        			ip.rtt = cursor.getString(cursor.getColumnIndex(IP_COLUMN_RTT));
	        			ip.success_num = cursor.getString(cursor.getColumnIndex(IP_COLUMN_SUCCESS_NUM));
	        			ip.err_num = cursor.getString(cursor.getColumnIndex(IP_COLUMN_ERR_NUM));
	        			ip.finally_success_time = cursor.getString(cursor.getColumnIndex(IP_COLUMN_FINALLY_SUCCESS_TIME));
	        			ip.finally_fail_time = cursor.getString(cursor.getColumnIndex(IP_COLUMN_FINALLY_FAIL_TIME));
	        			list.add(ip) ;
	        		}while(cursor.moveToNext());
	        	}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cursor.close();
				db.close() ;
	        }

	        return list ;
	}
	
    /**
     * 根据 服务器 ip 获取数据库的数据集
     *
     * @param serverIp
     * @return
     */
    private IpModel getIpModel(String serverIp, String sp){

    	ArrayList<IpModel> list = new ArrayList<IpModel>();

        synchronized (synLock) {

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ");
            sql.append(TABLE_NAME_IP);
            sql.append(" WHERE ");
            sql.append(IP_COLUMN_IP);
            sql.append(" =? ");
	        sql.append(" AND ");
	        sql.append(DOMAIN_COLUMN_SP);
	        sql.append(" =? ;");
	        
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(sql.toString(), new String[]{serverIp, sp});
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        IpModel ip = new IpModel();
                        ip.id = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_ID));
                        ip.d_id = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_DOMAIN_ID));
                        ip.ip = cursor.getString(cursor.getColumnIndex(IP_COLUMN_IP));
                        ip.port = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_PORT));
                        ip.sp = cursor.getString(cursor.getColumnIndex(IP_COLUMN_SP));
                        ip.ttl = cursor.getString(cursor.getColumnIndex(IP_COLUMN_TTL));
                        ip.priority = cursor.getString(cursor.getColumnIndex(IP_COLUMN_PRIORITY));
                        ip.rtt = cursor.getString(cursor.getColumnIndex(IP_COLUMN_RTT));
                        ip.success_num = cursor.getString(cursor.getColumnIndex(IP_COLUMN_SUCCESS_NUM));
                        ip.err_num = cursor.getString(cursor.getColumnIndex(IP_COLUMN_ERR_NUM));
                        ip.finally_success_time = cursor.getString(cursor.getColumnIndex(IP_COLUMN_FINALLY_SUCCESS_TIME));
                        ip.finally_fail_time = cursor.getString(cursor.getColumnIndex(IP_COLUMN_FINALLY_FAIL_TIME));
                        list.add(ip);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
                //db.close();
            }
        }

        // 排除下重复的IP 理论上是不会出现重复IP的， 多线程同时写数据库有锁。
        if( list.size() > 1 ){
        	for( int i = 0 ; i < list.size() - 1 ; i++ ){
        		IpModel ipModel = list.get(i);
        		deleteIpServer(ipModel.id) ; 
        	} 
        }

        return list.size() > 0 ? list.get(list.size() - 1) : null ;
    }
	
	/**
	 * 根据域名id 删除域名相关信息
	 */
	private void deleteDomainInfo(long domain_id){
		
		synchronized (synLock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				 db.delete(TABLE_NAME_DOMAIN, DOMAIN_COLUMN_ID + " = ?", new String[]{String.valueOf(domain_id)} ) ;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
	            db.close();
	        }
		}
	}

//    /**
//     * 根据域名id 删除服务器相关信息
//     * @param domain_id
//     */
//    private void deleteIpInfo(long domain_id){
//
//        synchronized (synLock) {
//            SQLiteDatabase db = getWritableDatabase();
//            try {
//                db.delete(TABLE_NAME_IP, IP_COLUMN_DOMAIN_ID + " = ?", new String[]{String.valueOf(domain_id)} ) ;
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                db.close();
//            }
//        }
//    }
    
    /**
     * 根据 ID 删除服务器信息
     * @param ip
     */
    private void deleteIpServer(long id){

        synchronized (synLock) {
            SQLiteDatabase db = getWritableDatabase();
            try {
                db.delete(TABLE_NAME_IP, IP_COLUMN_ID + " = ?", new String[]{String.valueOf(id)} ) ;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        }
    }


	/**
	 * 删除域名相关信息
	 */
	public void deleteDomainInfo(DomainModel domainModel){
		deleteDomainInfo( domainModel.id) ;
	}
	
	/**
	 * 删除域名相关信息
	 */
	public void deleteDomainInfo(ArrayList<DomainModel> domainModelArr){
		for( DomainModel temp : domainModelArr )
			deleteDomainInfo( temp.id) ;
	}
	
	
    /**
     * 清除缓存数据
     */
	public void clear() {
	    synchronized (synLock) {
	        SQLiteDatabase db = getWritableDatabase();
	        try {
	            db.delete(TABLE_NAME_DOMAIN, null, null);
	            db.delete(TABLE_NAME_IP, null, null);
	            db.delete(TABLE_NAME_CONNECT_FAIL, null, null);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            db.close();
	        }
        }
	}


	/**
	 * 返回 domain  表信息
	 */
	public ArrayList<DomainModel> getAllTableDomain(boolean appendIpInfo) {
	    ArrayList<DomainModel> list = new ArrayList<DomainModel>();
	    synchronized (synLock) {
	        StringBuilder sql = new StringBuilder();
	        sql.append("SELECT * FROM ");
	        sql.append(TABLE_NAME_DOMAIN);
	        sql.append(" ; ");
	        SQLiteDatabase db = getReadableDatabase();
	        Cursor cursor = null;
	        try {
	            cursor = db.rawQuery(sql.toString(), null);
	            if (cursor != null && cursor.getCount() > 0) {
	                cursor.moveToFirst();
	                do {
	                    DomainModel model = new DomainModel();
	                    model.id = cursor.getInt(cursor.getColumnIndex(DOMAIN_COLUMN_ID));
	                    model.domain = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_DOMAIN));
	                    model.sp = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_SP));
	                    model.ttl = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_TTL));
	                    model.time = cursor.getString(cursor.getColumnIndex(DOMAIN_COLUMN_TIME));
	                    if (appendIpInfo) {
                            model.ipModelArr = (ArrayList<IpModel>) QueryIpModelInfo(model);
                        }
	                    list.add(model);
	                } while (cursor.moveToNext());
	            }
	        } catch (Exception e) {
	            // TODO: handle exception
	            e.printStackTrace();
	        } finally {
	            cursor.close();
	            db.close();
	        }
	    }
	    return list;
	}
    /**
     * 返回 domain  表信息
     */
    public ArrayList<DomainModel> getAllTableDomain() {
        return getAllTableDomain(false);
    }


    /**
     * 返回 ip 表信息
     */
    public ArrayList<IpModel> getTableIP(){
        synchronized (synLock) {
        	ArrayList<IpModel> list = new ArrayList<IpModel>();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ");
            sql.append(TABLE_NAME_IP);
            sql.append(" ; ");

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(sql.toString(), null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        IpModel ip = new IpModel();
                        ip.id = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_ID));
                        ip.d_id = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_DOMAIN_ID));
                        ip.ip = cursor.getString(cursor.getColumnIndex(IP_COLUMN_IP));
                        ip.port = cursor.getInt(cursor.getColumnIndex(IP_COLUMN_PORT));
                        ip.sp = cursor.getString(cursor.getColumnIndex(IP_COLUMN_SP));
                        ip.ttl = cursor.getString(cursor.getColumnIndex(IP_COLUMN_TTL));
                        ip.priority = cursor.getString(cursor.getColumnIndex(IP_COLUMN_PRIORITY));
                        ip.rtt = cursor.getString(cursor.getColumnIndex(IP_COLUMN_RTT));
                        ip.success_num = cursor.getString(cursor.getColumnIndex(IP_COLUMN_SUCCESS_NUM));
                        ip.err_num = cursor.getString(cursor.getColumnIndex(IP_COLUMN_ERR_NUM));
                        ip.finally_success_time = cursor.getString(cursor.getColumnIndex(IP_COLUMN_FINALLY_SUCCESS_TIME));
                        ip.finally_fail_time = cursor.getString(cursor.getColumnIndex(IP_COLUMN_FINALLY_FAIL_TIME));
                        list.add(ip);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
                db.close();
            }
            return list ; 
        }
    }
    /**
     * 向数据库中新增一条ip记录
     * @param model
     * @return
     */
    public long addIpModel(IpModel model) {
        synchronized (synLock) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(IP_COLUMN_DOMAIN_ID, model.d_id);
            cv.put(IP_COLUMN_IP, model.ip);
            cv.put(IP_COLUMN_PORT, model.port);
            cv.put(IP_COLUMN_PRIORITY, model.priority);
            cv.put(IP_COLUMN_SP, model.sp);

            cv.put(IP_COLUMN_RTT, model.rtt);
            cv.put(IP_COLUMN_FINALLY_FAIL_TIME, model.finally_fail_time);
            cv.put(IP_COLUMN_FINALLY_SUCCESS_TIME, model.finally_success_time);
            cv.put(IP_COLUMN_SUCCESS_NUM, model.success_num);
            cv.put(IP_COLUMN_ERR_NUM, model.err_num);
            cv.put(IP_COLUMN_TTL, model.ttl);
            return db.insert(TABLE_NAME_IP, null, cv);
        }
    }
    
    /**
     * 更新数据库中的一条ip记录
     * @param model
     * @return
     */
    private void updateIpInfo(IpModel model) {
        synchronized (synLock) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            StringBuilder where = new StringBuilder();
            where.append(IP_COLUMN_ID);
            where.append(" = ? ");
            cv.put(IP_COLUMN_DOMAIN_ID, model.d_id);
            cv.put(IP_COLUMN_IP, model.ip);
            cv.put(IP_COLUMN_PORT, model.port);
            cv.put(IP_COLUMN_PRIORITY, model.priority);
            cv.put(IP_COLUMN_SP, model.sp);

            cv.put(IP_COLUMN_RTT, model.rtt);
            cv.put(IP_COLUMN_FINALLY_FAIL_TIME, model.finally_fail_time);
            cv.put(IP_COLUMN_FINALLY_SUCCESS_TIME, model.finally_success_time);
            cv.put(IP_COLUMN_SUCCESS_NUM, model.success_num);
            cv.put(IP_COLUMN_ERR_NUM, model.err_num);
            cv.put(IP_COLUMN_TTL, model.ttl);
            String[] args = new String[] { String.valueOf(model.id) };
            db.update(TABLE_NAME_IP, cv, where.toString(), args);
        }
    }
    
    /**
     * 批量更新ip表数据
     * @param model
     * @return
     */
    public void updateIpInfo(List<IpModel> ipModels) {
        synchronized (synLock) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                for (IpModel model : ipModels) {
                    ContentValues cv = new ContentValues();
                    StringBuilder where = new StringBuilder();
                    where.append(IP_COLUMN_ID);
                    where.append(" = ? ");
                    cv.put(IP_COLUMN_DOMAIN_ID, model.d_id);
                    cv.put(IP_COLUMN_IP, model.ip);
                    cv.put(IP_COLUMN_PORT, model.port);
                    cv.put(IP_COLUMN_PRIORITY, model.priority);
                    cv.put(IP_COLUMN_SP, model.sp);
                    
                    cv.put(IP_COLUMN_RTT, model.rtt);
                    cv.put(IP_COLUMN_FINALLY_FAIL_TIME, model.finally_fail_time);
                    cv.put(IP_COLUMN_FINALLY_SUCCESS_TIME, model.finally_success_time);
                    cv.put(IP_COLUMN_SUCCESS_NUM, model.success_num);
                    cv.put(IP_COLUMN_ERR_NUM, model.err_num);
                    cv.put(IP_COLUMN_TTL, model.ttl);
                    String[] args = new String[] { String.valueOf(model.id) };
                    db.update(TABLE_NAME_IP, cv, where.toString(), args);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                db.endTransaction();
                db.close();
            }
        }
    }
}
