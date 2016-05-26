package com.maple.imagefetchcore.imagemanager;

import android.graphics.Bitmap;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片弱缓存,使用弱引用
 */
public class ImageWeakCache implements IImageCache {
    private ConcurrentHashMap<String, WeakReference<Bitmap>> mImageCache = new ConcurrentHashMap<String, WeakReference<Bitmap>>();

    /**
     * 缓存命中次数
     */
    private int mHitCount = 0;
    /**
     * 缓存没有命中的次数
     */
    private int mMissCount = 0;

    public ImageWeakCache() {
    }

    /**
     * 设置一个键值
     *
     * @param key
     * @param value
     */
    @Override
    public void put(String key, Bitmap value) {
        mImageCache.put(key, new WeakReference<Bitmap>(value));
    }

    /**
     * 获取值，没有则返回空
     *
     * @param key
     * @return
     */
    @Override
    public Bitmap get(String key) {
        Bitmap bitmap = null;
        WeakReference<Bitmap> ref = mImageCache.get(key);
        if (ref != null) {
            bitmap = ref.get();
            if (null == bitmap || bitmap.isRecycled()) {
                //bitmap已回收, 删除记录
                mImageCache.remove(key);
                //已经recycled的bitmap, 返回null
                bitmap = null;
            }
        }

        if (null == bitmap) {
            mMissCount++;
        } else {
            mHitCount++;
        }
        return bitmap;
    }

    @Override
    public int size() {
        return mImageCache.size();
    }

    @Override
    public void clear() {
        mImageCache.clear();
    }

    @Override
    public void clear(String groupLabel) {
        Enumeration<String> enumeration = mImageCache.keys();
        ArrayList<String> keys = new ArrayList<String>();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            if (key.contains(groupLabel)) {
                keys.add(key);
            }
        }
        for (String key : keys) {
            mImageCache.remove(key);
        }


    }

    @Override
    public Bitmap remove(String key) {
        WeakReference<Bitmap> ref = mImageCache.remove(key);
        if (null == ref) {
            return null;
        }
        return ref.get();
    }

    @Override
    public void recycle(String key) {
        Bitmap bitmap = remove(key);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    @Override
    public void recycleAllImages() {
        Iterator<Map.Entry<String, WeakReference<Bitmap>>> it = mImageCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, WeakReference<Bitmap>> entry = it.next();
            it.remove();
            WeakReference<Bitmap> ref = entry.getValue();
            if (ref != null && ref.get() != null && !ref.get().isRecycled()) {
                ref.get().recycle();
            }
        }
    }

    @Override
    public float getHitRate() {
        return 1.0f * mHitCount / (mHitCount + mMissCount);
    }
}
