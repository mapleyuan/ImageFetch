package com.maple.imagefetchcore.manager;

import android.content.Context;
import android.webkit.URLUtil;

import com.maple.imagefetchcore.common.Constants;
import com.maple.imagefetchcore.core.ImageParserFactory;
import com.maple.imagefetchcore.core.bean.BaseUnit;
import com.maple.imagefetchcore.core.bean.DoubanDivUnit;
import com.maple.imagefetchcore.core.inter.IImageParser;
import com.maple.imagefetchcore.download.impl.DownloadRecordManager;

import java.util.List;

/**
 * Created by yuanweinan on 16/4/29.
 */
public class ImageFetchManager {

    /**
     * 操作结果
     * */
    public interface IOperationResult {
        void onSuccess();
        void onFail(int reason);
    }

    public static ImageFetchManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ImageFetchManager.class) {
                if (sInstance == null) {
                    sInstance = new ImageFetchManager(context);
                }
            }
        }
        return sInstance;
    }

    public void init() {

        //初始化数据，扫描文件夹下所有图片

    }

    /***
     * 爬取指定url的image
     * @param url
     */
    public void fetchImage(String url, final IOperationResult operationResult) {
        if (!URLUtil.isValidUrl(url)) {
            if (operationResult != null) {
                operationResult.onFail(Constants.LOAD_URL_RESULT_INVALID_URL);
            }
        }
        IImageParser parser = ImageParserFactory.create(url);
        parser.parse(url, new IImageParser.IParseResult<BaseUnit>() {
            @Override
            public void onFinish(List<BaseUnit> list) {

                handleParserData(list);

                if (operationResult != null) {
                    operationResult.onSuccess();
                }
            }

            @Override
            public void onFail() {
                if (operationResult != null) {
                    operationResult.onFail(Constants.LOAD_URL_RESULT_PARSE_FAIL);
                }
            }
        });
    }

    private static ImageFetchManager sInstance;
    private Context mContext;

    private ImageFetchManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context could not be null!");
        }
        mContext = context.getApplicationContext();
    }

    private void handleParserData(List<BaseUnit> list) {
        for (BaseUnit unit : list) {
            if (unit instanceof DoubanDivUnit) {
                DownloadRecordManager.getInstance(mContext).addDownload(((DoubanDivUnit)unit).mThumbnail);
            }
        }
    }
}
