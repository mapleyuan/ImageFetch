package com.maple.imagefetchcore.download.inter;

import com.maple.imagefetchcore.download.IDownloadListener;

/**
 * Created by yuanweinan on 16-5-4.
 */
public class BaseDownloadParams {
    public String mUrl;
    public String mFilePath;
    public long mStartPos;
    public IDownloadListener mListener;

    public BaseDownloadParams(String url, String filePath) {
        this(url, filePath, 0);
    }

    public BaseDownloadParams(String url, String filePath, long startPos) {
        mUrl = url;
        mStartPos = startPos;
        mFilePath = filePath;
    }

//    public static List<BaseDownloadParams> build(List<String> urls) {
//        List<BaseDownloadParams> baseDownloadParamsList = new ArrayList<>();
//        for (String url : urls) {
//            if (!URLUtil.isValidUrl(url)) {
//                LogUtil.e("url非法自动移出队列:" + url);
//                continue;
//            }
//            baseDownloadParamsList.add(new BaseDownloadParams(url));
//        }
//        return baseDownloadParamsList;
//    }
}
