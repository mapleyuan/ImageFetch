package com.maple.imagefetchcore.download;

import android.text.TextUtils;

import com.maple.imagefetchcore.download.data.DownloadConstants;
import com.maple.imagefetchcore.download.inter.BaseDownloadRequest;
import com.maple.imagefetchcore.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author yuanweinan
 *
 */
public class HttpDownloadHandler implements IDownloadHandler {
	private static final String LOG_TAG = "HttpDownloadHandler";
	private BaseDownloadRequest mRequest;
	private Object mStopProcessLock = new Object();
	private boolean mIsDownloading = false;
	private boolean mIsToStopProcess = false;
	private boolean mIsToRecordProcess = false;
	private float mProgressPrecision;
	public static final int RETRYTIMES = 3;
	private int mCurRetry;

	public HttpDownloadHandler(BaseDownloadRequest request, float progressPrecision) {
		this.mRequest = request;
		this.mProgressPrecision = progressPrecision;
		mCurRetry = 0;
	}

	public void handleRequest() {
		LogUtil.i(mRequest.getParams().mUrl);
		if (mIsDownloading) {
			return;
		}
		if ((this.mRequest == null)
				|| (TextUtils.isEmpty(mRequest.getParams().mFilePath))) {
			return;
		}

		File file = new File(this.mRequest.getParams().mFilePath);
		try {
			File dir = file.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}

			if (!dir.exists()) {
				this.mRequest.onException(DownloadConstants.ERROR_PATH_CREATE);
				return;
			}

			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.mRequest.onException(DownloadConstants.ERROR_PATH_CREATE);
			return;
		}

		FileOutputStream output = null;
		InputStream input = null;
		long mDownloadedCount = mRequest.getParams().mStartPos;
		try {
			URL url = new URL(this.mRequest.getParams().mUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			long startPos = this.mRequest.getParams().mStartPos;

			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setConnectTimeout(10000);
			httpURLConnection.setReadTimeout(60000);
			httpURLConnection.setRequestProperty("Range",
					"bytes=" + Math.max(0L, startPos) + "-");
			httpURLConnection.setAllowUserInteraction(true);
			int responseCode = httpURLConnection.getResponseCode();
			boolean resumable = false;
			if (responseCode == 206) {
				LogUtil.d(LOG_TAG, "支持断点续传！");

				if (startPos > 0L) {
					resumable = true;
				}
			} else if (responseCode == 200) {
				LogUtil.d(LOG_TAG, "不支持断点续传，重新建立连接");
				httpURLConnection.disconnect();
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection
						.setRequestProperty("Connection", "Keep-Alive");
				httpURLConnection.setConnectTimeout(60000);
				httpURLConnection.setReadTimeout(60000);
				httpURLConnection.setAllowUserInteraction(true);
				responseCode = httpURLConnection.getResponseCode();
			} else {
				LogUtil.d(LOG_TAG, "其他响应类型,断开连接，通知下载失败");
				httpURLConnection.disconnect();
				this.mRequest.onException(5);
				return;
			}

			this.mRequest.onStart();

			if (!resumable) {
				file.createNewFile();
				this.mRequest.getParams().mStartPos = 0L;
				mDownloadedCount = 0L;
			}

			long downloadLength = httpURLConnection.getContentLength();
			downloadLength += mDownloadedCount;
			LogUtil.d(LOG_TAG, "download content length = " + downloadLength
					+ ",curpos = " + mDownloadedCount);

			input = httpURLConnection.getInputStream();
			output = new FileOutputStream(file, resumable);
			byte[] buffer = new byte[1024];
			int rc = -1;
			float lastNotifyProcess = -1.0F;
			mIsDownloading = true;
			float progress = 0.0F;
			while (((rc = input.read(buffer)) != -1) && mIsDownloading) {
				output.write(buffer, 0, rc);

				mDownloadedCount += rc;
				progress = (float) mDownloadedCount / (float) downloadLength;
				if ((progress > this.mProgressPrecision)
						&& (progress > lastNotifyProcess
								+ this.mProgressPrecision)) {
					this.mRequest.onProgress(progress);
					lastNotifyProcess = progress;
				}

				synchronized (this.mStopProcessLock) {
					if (this.mIsToStopProcess) {
						break;
					}
				}
			}
	
			output.flush();
			output.close();
			output = null;
			mIsDownloading = false;

			if (this.mIsToStopProcess) {
				LogUtil.w(LOG_TAG, "停止/暂停");
				if (this.mIsToRecordProcess) {
					LogUtil.w(LOG_TAG, "记录下载位置！");
					this.mRequest.getParams().mStartPos = mDownloadedCount;
					this.mRequest.onProgress(progress);
					this.mRequest.onPause(mDownloadedCount);
				} else {
					LogUtil.w(LOG_TAG, "不记录下载位置！");
					this.mRequest.getParams().mStartPos = 0L;
					file.delete();
					this.mRequest.onStop();
				}
				return;
			}
			LogUtil.w(LOG_TAG, "下载完毕！");
			this.mRequest.getParams().mStartPos = 0L;
			this.mRequest.onProgress(1.0F);
			this.mRequest.onFinish();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(LOG_TAG, "网络不行，记录下载位置 = " + mDownloadedCount);
			this.mRequest.getParams().mStartPos = mDownloadedCount;
			//TODO wifi下重新下载
//			if (mCurRetry < RETRYTIMES && NetworkUtils.isNetworkOK(context)) {
//				LogUtil.e(LOG_TAG, "重试(" + mCurRetry + ")");
//				ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//				scheduledExecutorService.schedule(new Runnable() {
//					
//					@Override
//					public void run() {
//						handleRequest();
//						mCurRetry ++;
//					}
//				}, 10, TimeUnit.SECONDS);
//				return;
//			} 
				mIsDownloading = false;

				if (e.getMessage() != null) {
				LogUtil.e(LOG_TAG, e.getLocalizedMessage());
			}
			this.mRequest.onException(5);
			this.mRequest.onPause(mDownloadedCount);
			return;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException localIOException6) {
				}
				output = null;
			}

			if (input != null) {
				try {
					input.close();
				} catch (IOException localIOException7) {
				}
				input = null;
			}
		}

	}

	public void stop() {
		if (this.mIsDownloading) {
			synchronized (this.mStopProcessLock) {
				this.mIsToStopProcess = true;
				this.mIsToRecordProcess = false;
			}
		}
	}

	public void pause() {
		if (this.mIsDownloading) {
			synchronized (this.mStopProcessLock) {
				this.mIsToStopProcess = true;
				this.mIsToRecordProcess = true;
			}
		}
	}
}
