package com.maple.imagefetchcore.imagemanager;

import android.graphics.Bitmap;

/**
 * 图片异步加载类, 
 * @author matt
 * 备注 对列表图片加载做了优化, 控制列表滑动停止后才加载图片
 */
public class AsyncListImageLoader extends AsyncImageLoader {
	//是否是列表首次加载
	private boolean mIsFirstLoad = true;
	//是否允许加载，列表停止时允许加载
	private boolean mIsAllowLoad = true;
	//列表允许加载的开始项
	private int mLimitStart = 0;
	//列表允许加载的结束项
	private int mLimitEnd = 0;
	private byte[] mLoadLock = new byte[0];

	/**
	 * 构造方法, 必须在UI线程调用
	 * @param context
	 * @param imageCache
	 */
	public AsyncListImageLoader(IImageCache imageCache) {
		super(imageCache);
	}

	/**
	 * 专门针对列表项加载图片的方法
	 * @param position 图片item在listview中的位置
	 * @param request 图片加载请求信息
	 * @return 是否图片在内存里
	 * @备注 在列表停止滚动时才去加载图片
	 */
	public boolean loadImageForList(final int position, final ImageLoadRequest request, final String groupLabel) {
		//从内存读取
		Bitmap result = loadImgFromMemery(request.getImageSavePath(), request.mImageUrl, groupLabel);
		if (result != null && !result.isRecycled()) {
			//通知UI线程, 取图片的结果
			postImageLoadResultOnUiThread(request, result);
			return true;
		}

		Thread loadimg = new Thread() {
			@Override
			public void run() {
				//如果不允许加载，那么就是列表正在滚动，线程等待列表滚动停止时通知加载
				synchronized (mLoadLock) {
					while (!mIsAllowLoad) {
						try {
							mLoadLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				//在允许加载时，只有是第一次加载，或者是加载项在列表的可视项的范围内，才加载
				boolean isLoad = mIsFirstLoad || (position >= mLimitStart && position <= mLimitEnd);
				if (!isLoad) {
					return;
				}
				//从应用内, sd卡和网络加载图片
				handleLoadImage(request, groupLabel);
			}
		};
		loadimg.setPriority(Thread.MIN_PRIORITY);
		//TODO:因为线程里面有mLoadLock.wait()，这里的线程没有使用线程池，可能线程创建消耗比较大
		loadimg.start();
		
		return false;
	}

	/**
	 * 重置列表控制信息的方法
	 * @备注 一般是Activity:onResume时调用
	 * @备注 或者从一个listview跳转到另一个listview时，可调用此接口恢复初始设置
	 */
	public void restore() {
		synchronized (mLoadLock) {
			mIsAllowLoad = true;
			mIsFirstLoad = true;
		}
	}

	/**
	 * 锁定列表控制信息
	 * @备注 一般是在监听到列表正在滚动时进行调用
	 */
	public void lock() {
		synchronized (mLoadLock) {
			mIsAllowLoad = false;
			mIsFirstLoad = false;
		}
	}

	/**
	 * 解锁列表控制信息
	 * @备注 一般是在监听到列表完成滚动时进行调用，会通知等待加载图片的线程开始加载图片
	 */
	public void unlock() {
		synchronized (mLoadLock) {
			mIsAllowLoad = true;
			mLoadLock.notifyAll();
		}
	}
	/**
	 * 设置加载位置控制的方法
	 * @param start
	 * @param end
	 * @备注 一般是列表可见的第一项和最后一项的位置，只有在可见区域内才加载
	 */
	public void setLimitPosition(int start, int end) {
		mLimitStart = start;
		mLimitEnd = end;
	}
}