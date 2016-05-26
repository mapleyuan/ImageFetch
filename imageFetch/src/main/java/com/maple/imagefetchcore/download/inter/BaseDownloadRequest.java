package com.maple.imagefetchcore.download.inter;

import com.maple.imagefetchcore.download.HttpDownloadHandler;
import com.maple.imagefetchcore.download.IDownloadHandler;
import com.maple.imagefetchcore.download.IDownloadListener;

/**
 * Created by yuanweinan on 16-5-4.
 */
public class BaseDownloadRequest {

    public BaseDownloadRequest(BaseDownloadParams baseDownloadParams) {
        mBaseDownloadParams = baseDownloadParams;
    }

    public void bindDownloadHandler(HttpDownloadHandler downloadHandler) {
        mDownloadHandler = downloadHandler;
    }
    public void unbindDownloadHandler(){
        mDownloadHandler = null;
    }

    public IDownloadHandler getHandler() {
        return mDownloadHandler;
    }

    public BaseDownloadParams getParams() {
        return mBaseDownloadParams;
    }

    public void setDownloadListener(IDownloadListener listener) {
        getParams().mListener = listener;
    }

    public  void onStart() {
        try {
            if (getParams().mListener != null) {
                getParams().mListener.onStart(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFinish() {
        try {
            if (getParams().mListener != null) {
                getParams().mListener.onFinish(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause(long currentPos) {
        try {
            if (getParams().mListener != null) {
                getParams().mListener.onPause(this, currentPos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStop() {
        try {
            if (getParams().mListener != null) {
                getParams().mListener.onStop(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onException(int exception) {
        try {
            if (getParams().mListener != null) {
                getParams().mListener.onException(this, exception);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onProgress(float progress) {
        try {
            if (getParams().mListener != null) {
                getParams().mListener.onProgress(this, progress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BaseDownloadParams mBaseDownloadParams;
    private IDownloadHandler mDownloadHandler;
}
