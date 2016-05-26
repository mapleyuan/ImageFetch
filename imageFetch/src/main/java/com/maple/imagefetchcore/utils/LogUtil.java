package com.maple.imagefetchcore.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * @author yuanweinan
 *
 */
public class LogUtil {
	// Log默认Tag
	public static final String LOG_TAG = "imagefetch";
	// 是否显示Log(true:显示;false:不显示)
	public static boolean sSHOW_LOG = true;
	
	public static boolean sStopService = true;

	/**
	 * 设置是否打印Log
	 * 
	 * @param onOff
	 *            是否打印::->true:打印;false:不打印
	 */
	public static void setEnableLog(boolean onOff) {
		sSHOW_LOG = onOff;
	}

	public static void i(String msg) {
		if (sSHOW_LOG) {
			Log.i(LOG_TAG, msg);
		} 
	}

	public static void v(String tag, String msg) {
		if (sSHOW_LOG) {
			Log.v(tag, msg);
		}
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (sSHOW_LOG) {
			Log.v(tag, msg, tr);
		}
	}

	public static void d(String tag, String msg) {
		if (sSHOW_LOG) {
			Log.d(tag, msg);
		}
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (sSHOW_LOG) {
			Log.d(tag, msg, tr);
		}
	}

	public static void i(String tag, String msg) {
		if (sSHOW_LOG) {
			Log.i(tag, msg);
		} 
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (sSHOW_LOG) {
			Log.i(tag, msg, tr);
		} 
	}

	public static void w(String tag, String msg) {
		if (sSHOW_LOG) {
			Log.w(tag, msg);
		} 
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (sSHOW_LOG) {
			Log.w(tag, msg, tr);
		} 
	}

	public static void w(String tag, Throwable tr) {
		if (sSHOW_LOG) {
			Log.w(tag, tr);
		}
	}

	/**
	 * error为错误日志，出现时是后台配置错误或其它必须处理的情形
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void e(String tag, String msg) {
		Log.e(tag, msg);
	}

	public static void e(String msg) {
		Log.e(LOG_TAG, msg);
	}

	/**
	 * error为错误日志，出现时是后台配置错误或其它必须处理的情形
	 * 
	 * @param tag
	 * @param msg
	 * @param tr
	 */
	public static void e(String tag, String msg, Throwable tr) {
		Log.e(tag, msg, tr);
	}

	public static void showToast(Context context, CharSequence text,
			int duration) {
		if (sSHOW_LOG) {
			Toast.makeText(context, text, duration).show();
		}
	}

	public static void showToast(Context context, int resId, int duration) {
		if (sSHOW_LOG) {
			Toast.makeText(context, resId, duration).show();
		}
	}

	/**
	 * 获取当前调用堆栈信息
	 * 
	 * @return
	 */
	public static String getCurrentStackTraceString() {
		return Log.getStackTraceString(new Throwable());
	}

	/**
	 * 获取堆栈信息
	 * 
	 * @param tr
	 * @return
	 */
	public static String getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}
}
