package com.maple.imagefetchcore.imagemanager;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

import java.util.Map;

/**
 * LRU图片缓存器
 * @可选择size计算方式为图片占用内存总和或图片数量
 * @可选择次级缓存 
 * @author matt
 */
public class LruImageCache implements IImageCache {
	/**
	 * 强引用缓存，线程安全
	 * 当缓存超过限定大小时，该缓存会把最久没有使用的图片从缓存中移除，直到小于限制值为止
	 */
	private LruCache<String, Bitmap> mLruCache = null;

	/**
	 * 次级缓存, 软引用或弱引用缓存
	 */
	private IImageCache mSecondaryCache = null;
	
	/**
	 * 缓存命中次数
	 */
	private int mHitCount = 0;
	/**
	 * 缓存没有命中的次数
	 */
	private int mMissCount = 0;

	/**
	 * 关注内存
	 * @param maxMemorySize
	 */
	public LruImageCache(int maxMemorySize, IImageCache secondaryCache) {
		mLruCache = new LruCache<String, Bitmap>(maxMemorySize) {

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
					Bitmap newValue) {
				//如果超过了大小，就把从强引用移除的图片加入到弱引用中
				if (evicted && mSecondaryCache != null) {
					mSecondaryCache.put(key, oldValue);
				}
			}

			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}

		};
		
		mSecondaryCache = secondaryCache;
	}
	
	/**
	 * 关注图片数量
	 * @param maxImageCount
	 * @param uselessParameter, 无效参数, 仅用来占位
	 */
	public LruImageCache(int maxImageCount, IImageCache secondaryCache, Object uselessParameter) {
		mLruCache = new LruCache<String, Bitmap>(maxImageCount) {

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
					Bitmap newValue) {
				//如果超过了大小，就把从强引用移除的图片加入到弱引用中
				if (evicted && mSecondaryCache != null) {
					mSecondaryCache.put(key, oldValue);
				}
			}
		};
		
		mSecondaryCache = secondaryCache;
	}

	/**
	 * 设置一个键值
	 * 
	 * @param key
	 * @param value
	 */
	public void put(String key, Bitmap value) {
		if (key == null || value == null) {
			return;
		}
		if (mLruCache != null) {
			mLruCache.put(key, value);
		}
	}

	/**
	 * 获取值，没有则返回空
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap get(String key) {
		if (key == null) {
			return null;
		}
		//先从强引用缓存中取，如果取不到的话，再从弱引用缓存里面取
		Bitmap bitmap = mLruCache.get(key);
		if (bitmap == null && mSecondaryCache != null) {
			bitmap = mSecondaryCache.get(key);
		}
		
		if (null == bitmap) {
			mMissCount ++;
		} else {
			mHitCount ++;
		}
		return bitmap;
	}

	/**
	 * 清空缓存
	 */
	@Override
	public void clear() {
		mLruCache.evictAll();
		if (mSecondaryCache != null) {
			mSecondaryCache.clear();
		}
	}

	@Override
	@Deprecated
	public void clear(String groupLabel) {
		for (Map.Entry<String, Bitmap> entry : mLruCache.snapshot().entrySet()) {
				if (entry.getKey().contains(groupLabel)) {
					mLruCache.remove(entry.getKey());
				}
		}
		mSecondaryCache.clear();

	}

	@Override
	public Bitmap remove(String key) {
		Bitmap removed = mLruCache.remove(key);
		if (removed != null) {
			return removed;
		} else if (mSecondaryCache != null) {
			return mSecondaryCache.remove(key);
		} else {
			return null;
		}
	}

	@Override
	public void recycle(String key) {
		Bitmap bitmap = remove(key);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
		bitmap = null;
	}
	
	@Override
	public void recycleAllImages() {
		mLruCache.evictAll();
		if (mSecondaryCache != null) {
			mSecondaryCache.recycleAllImages();
		}
	}
	
	@Override
	public int size() {
		return mLruCache.size();
	}
	
	@Override
	public float getHitRate() {
		return 1.0f * mHitCount / (mHitCount + mMissCount);
	}
	
	/**
	 * 获取建议的图片内存上限
	 * @param context
	 * @return
	 */
	public static int getImagesMaxMemorySizeSuggested(Context context) {
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
				.getMemoryClass();
		// Use 1/8th of the available memory for this memory cache.
		return 1024 * 1024 * memClass / 4;
	}
	
//	public static IImageCache createSecondarySoftCache() {
//		return new ImageSoftCache();
//	}
//	
//	public static IImageCache createSecondaryWeakCache() {
//		return new ImageWeakCache();
//	}
}
