package com.maple.imagefetchcore.download;
import com.maple.imagefetchcore.download.inter.BaseDownloadParams;
import com.maple.imagefetchcore.download.inter.BaseDownloadRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 * 下载管理器
 * @author yuanweinan
 *
 */
public class DownloadManager {

	public static DownloadManager getInstance() {
		synchronized (DownloadManager.class) {
			if (sInstance == null) {
				synchronized (DownloadManager.class) {
					if (sInstance == null) {
						sInstance = new DownloadManager();
					}
				}
			}
		}
		return sInstance;
	}

	/**
	 * 添加下载url
	 * @param baseDownloadParams    下载参数
	 */
	public void addRequest(final BaseDownloadParams baseDownloadParams) {
		if (baseDownloadParams == null) {
			throw new IllegalArgumentException("BaseDownloadParams can not be null");
		}
		synchronized (LOCK) {
			mRequests.add(baseDownloadParams);
		}
		this.mThreadPool.submit(new Runnable() {
			public void run() {
				try {
					BaseDownloadRequest baseDownloadRequest = new BaseDownloadRequest(baseDownloadParams);
					baseDownloadRequest.bindDownloadHandler(new HttpDownloadHandler(baseDownloadRequest, DownloadManager.this.mProgressPrecision));
					baseDownloadRequest.getHandler().handleRequest();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					synchronized (LOCK) {
						mRequests.remove(baseDownloadParams);
					}
				}
			}
		});
	}

	public void pauseRequest(BaseDownloadRequest request) {
		if (request == null) {
			return;
		}

		IDownloadHandler handler = request.getHandler();
		if (handler != null) {
			handler.pause();
			request.unbindDownloadHandler();
		}
	}

	public void stopRequest(BaseDownloadRequest request) {
		if (request == null) {
			return;
		}

		IDownloadHandler handler = request.getHandler();
		if (handler != null) {
			handler.stop();
			request.unbindDownloadHandler();
		}
	}

	public void shutDown() {
		this.mThreadPool.shutdownNow();
	}

	private static DownloadManager sInstance;
	private ExecutorService mThreadPool;
	private float mProgressPrecision = 0.01F;
	private List<BaseDownloadParams> mRequests = new ArrayList<BaseDownloadParams>();
	private final static Object LOCK = new Object();

	private DownloadManager() {
		open();
	}


	private void open() {
		if ((this.mThreadPool == null) || (this.mThreadPool.isShutdown())) {
			this.mThreadPool = Executors.newFixedThreadPool(3);
		}
	}
}
