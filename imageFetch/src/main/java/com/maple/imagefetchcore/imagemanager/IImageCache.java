package com.maple.imagefetchcore.imagemanager;

import android.graphics.Bitmap;

/**
 * 图片缓存器接口
 * @author matt
 *
 */
public interface IImageCache {
	/**
	 * 添加图片进缓存
	 * @param key
	 * @param value
	 */
	public void put(String key, Bitmap value);
	/**
	 * 从缓存获取图片(不删除)
	 * @param key
	 * @return
	 * 备注: 在检索过程中, 如发现对应的bitmap已经recycled, 则返回null
	 */
	public Bitmap get(String key);
	/**
	 * 从缓存删除图片
	 * @param key
	 * @return 被删除的图片
	 */
	public Bitmap remove(String key);
	/**
	 * 从缓存删除图片,并回收位图数据内存
	 * @param key
	 */
	public void recycle(String key);
	/**
	 * 清空缓存
	 */
	public void clear();

	public void clear(String groupLabel);
	/**
	 * 清空缓存,并回收所有位图数据内存
	 */
	public void recycleAllImages();
	/**
	 * 获取缓存实际大小
	 * @return
	 */
	public int size();
	/**
	 * 获取命中率
	 * @return
	 */
	public float getHitRate();
}
