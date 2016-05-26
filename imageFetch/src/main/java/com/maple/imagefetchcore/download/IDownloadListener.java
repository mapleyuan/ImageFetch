package com.maple.imagefetchcore.download;


import com.maple.imagefetchcore.download.inter.BaseDownloadRequest;

/**
 * @author yuanweinan
 *
 */
public  interface IDownloadListener {
	public  void onStart(BaseDownloadRequest paramDownloadRequest);

	public  void onFinish(BaseDownloadRequest paramDownloadRequest);

	public  void onPause(BaseDownloadRequest paramDownloadRequest,
						 long paramLong);

	public  void onStop(BaseDownloadRequest paramDownloadRequest);

	public  void onException(BaseDownloadRequest paramDownloadRequest,
							 int paramInt);

	public  void onProgress(BaseDownloadRequest paramDownloadRequest,
							float paramFloat);
}
