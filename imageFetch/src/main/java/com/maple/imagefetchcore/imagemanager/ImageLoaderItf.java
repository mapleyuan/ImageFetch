package com.maple.imagefetchcore.imagemanager;

import android.graphics.Bitmap;


/**
 * 图片加载接口， 内置拦截器模式
 * @author matt
 *
 */
public interface ImageLoaderItf {
	/**
	 * 是否处理（拦截器)
	 * @param imgUrl
	 * @return
	 */
	boolean isHandle(String imgUrl);
	
	/**
	 * 加载位图
	 * @param request 请求信息
	 * @return
	 * @备注 如需保存到sd卡，需要先保存到sd卡，再从sd卡加载。实现参考 {@link NetImageLoader}
	 */
	Bitmap loadImage(AsyncImageLoader.ImageLoadRequest request);
	
	/**
	 * 是否要保存到sd卡
	 * @return
	 */
	boolean isSave2SDCard();
}
