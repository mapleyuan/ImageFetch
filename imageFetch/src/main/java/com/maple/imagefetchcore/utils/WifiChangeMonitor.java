package com.maple.imagefetchcore.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * wifi状态监听器
 * 
 * @author yuanweinan
 *
 */
public class WifiChangeMonitor {

	/**
	 * @author yuanweinan
	 *
	 */
	public interface IWifiStatusListener {
		void change(boolean ON);
	}

	public static WifiChangeMonitor getInstance(Context context) {
		if (sInstance == null) {
			synchronized (WifiChangeMonitor.class) {
				if (sInstance == null) {
					sInstance = new WifiChangeMonitor(context);
				}
			}
		}
		return sInstance;
	}

	public void onDestroy() {
		if (sInstance != null) {
			sInstance.stopBroadcastReceiver();
			mContext = null;
		}
	}

	public void registerReceiver() {
		if (mBrocastReceiver != null) {
			return;
		}
		startBroadcastReceiver();
	}

	public void registerListener(IWifiStatusListener wifiListener) {
		if (wifiListener == null) {
			return;
		}
		registerReceiver();

		synchronized (LOCK) {
			int size = sListeners.size();
			for (int i = 0; i < size; i++) {
				IWifiStatusListener listenrer = sListeners.get(i);
				if (listenrer == wifiListener) {
					return;
				}
			}
			sListeners.add(wifiListener);
		}
	}

	public void unregisterListener(IWifiStatusListener wifiListener) {
		if (wifiListener == null) {
			return;
		}
		synchronized (LOCK) {
			sListeners.remove(wifiListener);
		}
	}

	private static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	private static volatile WifiChangeMonitor sInstance;
	private Context mContext;
	private NetBrocastReceiver mBrocastReceiver;
	private static List<IWifiStatusListener> sListeners;
	private static final Object LOCK = new Object();

	private WifiChangeMonitor(Context context) {
		mContext = context;
		sListeners = new ArrayList<IWifiStatusListener>();
	}

	/**
	 * 启动screen状态广播接收器
	 */
	private void startBroadcastReceiver() {
		if (mBrocastReceiver == null) {
			mBrocastReceiver = new NetBrocastReceiver();
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		mContext.registerReceiver(mBrocastReceiver, filter);
	}

	/**
	 * 停止screen状态监听
	 */
	private void stopBroadcastReceiver() {
		if (mBrocastReceiver == null) {
			return;
		}
		try {
			mContext.unregisterReceiver(mBrocastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mBrocastReceiver = null;
		}
	}
	
	/**
	 * 判断当前网络环境是否为WiFi且可用
	 * @return
	 */
	private static boolean isWifiEnable(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
		if (activeNetInfo != null && activeNetInfo.isConnected() && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	/**
	 * @author yuanweinan
	 *
	 */
	public static class NetBrocastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String mAction = intent.getAction();
			synchronized (LOCK) {
				if (CONNECTIVITY_CHANGE_ACTION.equals(mAction)) {
					boolean isWifi = isWifiEnable(context);
					List<IWifiStatusListener> temps = new ArrayList<IWifiStatusListener>();
					temps.addAll(sListeners);
					for (IWifiStatusListener listener : temps) {
						if (!sListeners.contains(listener)) {
							continue;
						}
						if (listener != null) {
							listener.change(isWifi);
						}
					}
					temps.clear();
					temps = null;
				}
			}
		}
	}
}
