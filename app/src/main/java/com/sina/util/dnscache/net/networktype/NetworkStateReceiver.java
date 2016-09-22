package com.sina.util.dnscache.net.networktype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.sina.util.dnscache.DNSCache;

public class NetworkStateReceiver extends BroadcastReceiver {

	public String TAG = "TAG_NET" ; 
	
	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
		    NetworkInfo networkInfo = getActiveNetwork(context);
			if( networkInfo != null ){ 
				
				// 刷新网络环境
				if( NetworkManager.getInstance() != null ) {
					NetworkManager.getInstance().Init(); 
					if( DNSCache.getInstance() != null ){
						DNSCache.getInstance().onNetworkStatusChanged(networkInfo); 
					}
				}
			}
		}
	}

	public static NetworkInfo getActiveNetwork(Context context) {
		if (context == null)
			return null;
		ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (mConnMgr == null)
			return null;

		NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo(); // 获取活动网络连接信息
		return aActiveInfo;
	}
	
    public static void register(Context context) {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(new NetworkStateReceiver(), mFilter);
    }
}
