package com.maple.imagefetchcore.download.impl;

import android.content.Context;

import com.maple.imagefetchcore.download.IDownloadListener;
import com.maple.imagefetchcore.download.inter.BaseDownloadRequest;
import com.maple.imagefetchcore.utils.LogUtil;

/**
 * @author yuanweinan
 *
 */
public class DownloadBackgroundListener implements IDownloadListener {

	public DownloadBackgroundListener(Context context) {
		mContext = context;
	}
	
	@Override
	public void onStart(BaseDownloadRequest paramDownloadRequest) {
		// TODO Auto-generated method stub
		LogUtil.i(TAG, "开始下载" + paramDownloadRequest.getParams().mUrl);
	}

	@Override
	public void onFinish(BaseDownloadRequest paramDownloadRequest) {
		LogUtil.i(TAG, paramDownloadRequest.getParams().mUrl + "下载成功,开始推荐倒计时...");
		DownloadRecordManager.getInstance(mContext).onFinish(paramDownloadRequest);
	}

	@Override
	public void onPause(BaseDownloadRequest paramDownloadRequest, long paramLong) {
		LogUtil.i(TAG, paramDownloadRequest.getParams().mUrl
				+ "下载中断，更新数据库记录！当前位置 = " + paramDownloadRequest.getParams().mStartPos);
		DownloadRecordManager.getInstance(mContext).updateRequest(paramDownloadRequest);
	}

	@Override
	public void onStop(BaseDownloadRequest paramDownloadRequest) {
		DownloadRecordManager.getInstance(mContext).removeRequest(paramDownloadRequest);
	}

	@Override
	public void onException(BaseDownloadRequest paramDownloadRequest, int paramInt) {
		LogUtil.e(TAG, "下载异常:" + paramInt);
	}

	@Override
	public void onProgress(BaseDownloadRequest paramDownloadRequest,
			float progress) {
		LogUtil.i(TAG, paramDownloadRequest.getParams().mUrl + "位置:"
				+ paramDownloadRequest.getParams().mFilePath + "下载进度:" + (progress * 100)
				+ "%");
		
	}

	
	private static final String TAG = DownloadRecordManager.LOG_TAG;
	private Context mContext;
}
