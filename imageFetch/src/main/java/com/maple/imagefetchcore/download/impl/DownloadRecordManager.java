package com.maple.imagefetchcore.download.impl;


import android.content.Context;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.maple.imagefetchcore.db.FTPDBHelpler;
import com.maple.imagefetchcore.download.DownloadManager;
import com.maple.imagefetchcore.download.IDownloadListener;
import com.maple.imagefetchcore.download.data.DownloadConstants;
import com.maple.imagefetchcore.download.inter.BaseDownloadParams;
import com.maple.imagefetchcore.download.inter.BaseDownloadRequest;
import com.maple.imagefetchcore.utils.FileUtils;
import com.maple.imagefetchcore.utils.LogUtil;
import com.maple.imagefetchcore.utils.WifiChangeMonitor;

import java.util.ArrayList;
import java.util.List;


/**
 * @author yuanweinan
 *
 */
public class DownloadRecordManager implements WifiChangeMonitor.IWifiStatusListener {

	public static final String LOG_TAG = "FTPSDK_DOWNLOAD";

	public static DownloadRecordManager getInstance(Context context) {
		if (sInstance == null) {
			synchronized (DownloadRecordManager.class) {
				if (sInstance == null) {
					sInstance = new DownloadRecordManager(context);
				}
			}
		}
		return sInstance;
	}

	public void pauseAll() {
		synchronized (LOCKDOWNLOAD) {
			LogUtil.i("pauseAll");
			if (mRequests.size() == 0) {
				LogUtil.i(LOG_TAG, "request 为空");
				return;
			}
			mIsDownloading = false;
			for (BaseDownloadRequest request : mRequests) {
				mDownloadManager.pauseRequest(request);
			}
		}
	}

	public void clearAll() {
		synchronized (LOCKDOWNLOAD) {
			LogUtil.i("clearAll");
			if (mRequests.size() == 0) {
				return;
			}
			mIsDownloading = false;
			List<BaseDownloadRequest> temps = new ArrayList<BaseDownloadRequest>();
			temps.addAll(mRequests);
			mRequests.clear();
			for (BaseDownloadRequest request : temps) {
				mDownloadManager.stopRequest(request);
				FileUtils.deleteFile(request.getParams().mFilePath);
				DownloadRecordData.delete(mDBHelper, request.getParams().mUrl);
			}
		}
	}

	public void restartAll() {
		synchronized (LOCKDOWNLOAD) {
			LogUtil.i("restartAll");
			if (mRequests.size() == 0) {
				LogUtil.i(LOG_TAG, "request 为空");
				return;
			}
			if (mIsDownloading) {
				return;
			}
			mIsDownloading = true;
			for (BaseDownloadRequest request : mRequests) {
				mDownloadManager.addRequest(request.getParams());
			}
		}
	}

	public void addDownload(String url) {
		synchronized (LOCKDOWNLOAD) {
			mDownloadListener = new DownloadBackgroundListener(mContext);
			String fileName = URLUtil.guessFileName(url, null, null);
			requestDownload(url, DownloadConstants.DOWNLOAD_PATH + "/" + fileName, mDownloadListener);
		}
	}

	public void onFinish(BaseDownloadRequest downloadRequest) {
		if (mRequests.size() == 0) {
			return;
		}
		synchronized (LOCKDOWNLOAD) {
			mIsDownloading = false;
			mRequests.remove(downloadRequest);
		}
		updateDownloaded(downloadRequest.getParams().mUrl);
	}
	
	public void onException() {
		synchronized (LOCKDOWNLOAD) {
			mIsDownloading = false;
		}
	}

	public void removeRequest(BaseDownloadRequest paramDownloadRequest) {
		mRequests.remove(paramDownloadRequest);
	}

	public void updateRequest(BaseDownloadRequest paramDownloadRequest) {
		if (mRequests.size() == 0) {
			return;
		}
		BaseDownloadParams params = paramDownloadRequest.getParams();
		DownloadRecordData.update(mDBHelper, new DownloadRecordData(
				params.mUrl, params.mFilePath,
				params.mStartPos, 0, 0));
		for (BaseDownloadRequest request : mRequests) {
			if (request.getParams().mUrl.equals(params.mUrl)) {
				request.getParams().mStartPos = params.mStartPos;
			}
		}
	}
	
	public boolean isDownloading() {
		synchronized (LOCKDOWNLOAD) {
			return mIsDownloading;
		}
	}

	public boolean isDownloaded(String url, String pkgName) {
		if (url == null) {
			return false;
		}
		DownloadRecordData data = DownloadRecordData.query(mDBHelper, url);
		if (data == null) {
			return false;
		}
		if (!FileUtils.isFileExist(data.mFilePath)) {
			return false;
		}
		return data.mStatus == 0 ? false : true;
	}

	public void onDestroy() {
		try {
			mDBHelper.close();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public void change(boolean ON) {
		// TODO Auto-generated method stub
	}

	private DownloadManager mDownloadManager;
	public FTPDBHelpler mDBHelper;
	private List<BaseDownloadRequest> mRequests = new ArrayList<BaseDownloadRequest>();
	private Context mContext;
	private boolean mIsDownloading = false;
	private static final Object LOCKDOWNLOAD = new Object();
	private static volatile DownloadRecordManager sInstance;
	public IDownloadListener mDownloadListener;

	private DownloadRecordManager(Context context) {
		mContext = context;
		mDBHelper = new FTPDBHelpler(context);
		WifiChangeMonitor.getInstance(context).registerListener(this);
		mDownloadManager = DownloadManager.getInstance();
	}

	private void updateDownloaded(String url) {
		DownloadRecordData data = DownloadRecordData.query(mDBHelper, url);
		if (data == null) {
			return;
		}
		data.mFinishTime = System.currentTimeMillis();
		data.mStatus = 1;
		DownloadRecordData.update(mDBHelper, data);
	}

	private synchronized BaseDownloadRequest getRequest(String url,
			String filePath) {
		if ((TextUtils.isEmpty(url)) || (TextUtils.isEmpty(filePath))) {
			LogUtil.w(LOG_TAG, "download url , filePath or pkgName is null!");
			return null;
		}
		if (isInQueue(url)) {
			LogUtil.w(LOG_TAG, "the url is downloading,return!");
			return null;
		}
		BaseDownloadRequest request;
		DownloadRecordData data = DownloadRecordData.query(this.mDBHelper, url);
		if (data != null) {
			LogUtil.d(LOG_TAG, url + "数据库有记录！当前位置 = " + data.mStartPos);
			request = new BaseDownloadRequest(new BaseDownloadParams(data.mUrl, data.mFilePath,
					data.mStartPos));
		} else {
			LogUtil.d(LOG_TAG, url + "数据库无记录，从0开始下，并写入数据库");
			request = new BaseDownloadRequest(new BaseDownloadParams(url, filePath, 0L));
			data = new DownloadRecordData(url, filePath, 0L, 0, 0);
			DownloadRecordData.insert(this.mDBHelper, data);
		}
		return request;
	}

	private void requestDownload(String url, String filePath,
			IDownloadListener listener) {
		BaseDownloadRequest request = getRequest(url, filePath);
		if (request == null) {
			return;
		}
		request.setDownloadListener(listener);
		synchronized (LOCKDOWNLOAD) {
			mIsDownloading = true;
		}
		this.mDownloadManager.addRequest(request.getParams());
		this.mRequests.add(request);
	}

	private boolean isInQueue(String url) {
		if (TextUtils.isEmpty(url)) {
			return false;
		}
		int curPos = 0;
		for (BaseDownloadRequest request : this.mRequests) {
			if (url.equals(request.getParams().mUrl)) {
				return true;
			}
			curPos++;
		}
		if (curPos < mRequests.size()) {
			LogUtil.i(LOG_TAG, "删除无效url");
			mRequests.remove(curPos);
		}

		return false;
	}


}
