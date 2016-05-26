package com.maple.imagefetchcore.maple.imageselector;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.maple.imagefetchcore.manager.ImageFetchManager;
import com.maple.imagefetchcore.maple.imageselector.pojo.FileUnit;
import com.maple.imagefetchcore.thread.CustomThreadExecutorProxy;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by yuanweinan on 16-5-26.
 */
public class IFImageFileManager {

    public static IFImageFileManager getInstance(Context context) {
        synchronized (IFImageFileManager.class) {
            if (sInstance == null) {
                synchronized (IFImageFileManager.class) {
                    if (sInstance == null) {
                        sInstance = new IFImageFileManager(context);
                    }
                }
            }
        }
        return sInstance;
    }

    /**
     * sdcard地址
     * @param directory
     */
    public void scanDirectory(final String directory, final ImageFetchManager.IOperationResult operationResult) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.i("maple", "暂无外部存储");
            return;
        }
        CustomThreadExecutorProxy.getInstance().runOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                File parentFile = new File(directory);
                File[] childs = parentFile.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if (filename.endsWith(".jpg")
                                || filename.endsWith(".png")
                                || filename.endsWith(".jpeg")) {
                            return true;
                        }

                        if (dir.isDirectory()) {
                            return true;
                        }
                        return false;
                    }
                });

                if (operationResult != null) {
                    operationResult.onSuccess(FileUnit.transferTo(childs));
                }
            }
        });

    }



    private static IFImageFileManager sInstance;
    private Context mContext;
    private IFImageFileManager(Context context) {
        mContext = context.getApplicationContext();
    }
}
