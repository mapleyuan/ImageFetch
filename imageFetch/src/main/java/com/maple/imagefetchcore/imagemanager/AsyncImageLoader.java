package com.maple.imagefetchcore.imagemanager;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;

import com.maple.imagefetchcore.thread.PriorityThreadPool;
import com.maple.imagefetchcore.thread.ThreadPool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 图片异步加载类, 实现图片3级存储管理
 * @author matt
 * @备注 默认添加“网络图片加载器”
 */
public class AsyncImageLoader {
	/** 本地图片加载线程池 */
	protected ThreadPool mLocalThreadPool = null;
	/** 网络图片加载线程池 */
	protected PriorityThreadPool mNetThreadPool = null;

	/** 同步主线程的Handler */
	protected Handler mHandler = new Handler();
	/** 图片缓存池 */
	protected IImageCache mImageCache = null;
	/** 图片加载器队列 */
	private List<ImageLoaderItf> mImageLoaders = new ArrayList<ImageLoaderItf>();
	/** 图片加载器队列锁 */
	private Object mImageLoadersLock = new Object();
	
	/** 标签管理器， 优先加载当前标签的图片  */
	protected LabelManager mLabelManager = new LabelManager();

	/**
	 * 构造方法, 必须在UI线程调用
	 * @param imageCache
	 */
	public AsyncImageLoader(IImageCache imageCache) {
		mImageCache = imageCache;
		mLocalThreadPool = new ThreadPool(1);
		mNetThreadPool = new PriorityThreadPool();
		
		//默认添加“网络图片加载器”
		addImageLoader(new NetImageLoader());
	}

	/**
	 * 清空缓存
	 */
	public void clear() {
		if (mImageCache != null) {
			mImageCache.clear();
		}
		//取消所用线程
		removeAllTask();
	}

	/**
	 * 清空制定label缓存
	 */
	public void clear(String groupLabel) {
		if (mImageCache != null) {
			mImageCache.clear(groupLabel);
		}
		//取消所用线程
		removeAllTask();
	}

	/**
	 * 清空缓存,并回收所有位图数据内存
	 */
	public void recyleAllImages() {
		if (mImageCache != null) {
			mImageCache.recycleAllImages();
		}
		//取消所用线程
		removeAllTask();
	}

	/**
	 * 从缓存删除图片,并回收位图数据内存
	 * @param key
	 */
	public void recycle(String key) {
		if (mImageCache != null) {
			mImageCache.recycle(key);
		}
	}

	/**
	 * 取消所有未执行的任务
	 */
	public void removeAllTask() {
		mLocalThreadPool.clear();
		mNetThreadPool.clear();
	}
	
	/**
	 * 取消组标签对应的未执行任务
	 * @param groupLabel
	 */
	public void removeTasks(String groupLabel) {
		if (groupLabel == null) {
			return;
		}
		BlockingQueue<Runnable> queue = mNetThreadPool.getThreadPoolExecutor().getQueue();
    	for (Iterator<Runnable> it = queue.iterator(); it.hasNext(); ) {
    		PriorityRunnable r = (PriorityRunnable) it.next(); 
    		if (groupLabel.equals(r.mRequest.mGroupLabel)) {
    			it.remove();
    		}
    	}
	}
	
	/**
	 * 添加图片加载器
	 * @param loader
	 */
	public void addImageLoader(ImageLoaderItf loader) {
		if (null == loader) {
			return;
		}
		synchronized (mImageLoadersLock) {
			mImageLoaders.add(loader);
		}
	}
	
	/**
	 * 查找关联处理器
	 * @return
	 */
	private ImageLoaderItf findRelativeLoader(String imgUrl) {
		if (null == imgUrl) {
			return null;
		}
		synchronized (mImageLoadersLock) {
			for (ImageLoaderItf loader : mImageLoaders) {
				if (loader.isHandle(imgUrl)) {
					return loader;
				}
			}
			return null;
		}
	}

	/**
	 * 加载图片，如果图片在内存里，则直接返回图片，否则异步从SD卡或者网络加载图片
	 * @param request 请求信息
	 * @return 是否图片在内存里
	 */
	public boolean loadImage(final ImageLoadRequest request, final String groupLabel) {
//		LogUtils.i("matt", "[AsyncImageLoader::loadImage] " + request.toString());
//		LogUtils.d("matt", LogUtils.getStackTraceString(new Throwable()));
		
		//从内存读取
		Bitmap result = loadImgFromMemery(request.getImageSavePath(), request.mImageUrl, groupLabel);
		if (result != null && !result.isRecycled()) {
			//通知UI线程, 取图片的结果
			postImageLoadResultOnUiThread(request, result);
			return true;
		}

		mLocalThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				//从应用内,sd卡和网络 读取图片
				handleLoadImage(request, groupLabel);
			}
		});
		
		return false;
	}
	
	/**
	 * 在UI线程里通知图片加载结果
	 * @param request
	 * @param bmp
	 */
	protected void postImageLoadResultOnUiThread(ImageLoadRequest request, Bitmap bmp) {
		if (null == request || null == request.mCallBack) {
			return;
		}
		CallBackRunnable cbRunnable = new CallBackRunnable(bmp, request);
		mHandler.post(cbRunnable);
	}
	
	/**
	 * 实际处理读取图片的过程
	 * @param request
	 * @return
	 * @备注 先从SD卡读，如无则遍历图片加载器来处理。如果是本地图片则直接调用图片加载器，网络图片则切换到net线程池
	 */
	protected void handleLoadImage(final ImageLoadRequest request, final String groupLabel) {
		final ImageLoaderItf loader = findRelativeLoader(request.mImageUrl);
		if (null == loader) {
			//通知UI线程, 取图片的结果
			postImageLoadResultOnUiThread(request, null);
			return;
		}
		
		Bitmap bmp = null;
		boolean isSave2SDCard = loader.isSave2SDCard();
		if (isSave2SDCard) {
			//从sdcard读取
			bmp = SdImageLoader.loadImgFromSD(request.getImageSavePath(), request.mScaleCfg);
		}
		
		if (bmp == null) {
			//是否为网络图片，决定在哪个线程池来调用图片加载器
			boolean isNetTask = request.mImageUrl != null && (request.mImageUrl.startsWith("http:") || request.mImageUrl.startsWith("https:"));
			if (isNetTask) {
				mNetThreadPool.submit(new PriorityRunnable(request) {
					
					@Override
					public void run() {
						//解决重复投递请求引起的重复从网络加载图片的bug，先从内存找
						Bitmap bmp = loadImgFromMemery(request.getImageSavePath(), request.mImageUrl, groupLabel);
						if (bmp == null || bmp.isRecycled()) {
							//通过图片加载器来获取图片
							bmp = loader.loadImage(request);
							//保存到缓存
							if (request.mIsCache) {
								saveImgToMemory(bmp, request.getImageSavePath(), request.mImageUrl, groupLabel);
							}
						}
						
						//通知UI线程, 取图片的结果
						if (request.mCallBack != null) {
							postImageLoadResultOnUiThread(request, bmp);
						}
					}
				});
				return;
			} else {
				//通过图片加载器来获取图片
				bmp = loader.loadImage(request);
			}
		} 
		//保存到缓存
		if (request.mIsCache) {
			saveImgToMemory(bmp, request.getImageSavePath(), request.mImageUrl, groupLabel);
		}
		//通知UI线程, 取图片的结果
		postImageLoadResultOnUiThread(request, bmp);
	}
	
	/**
	 * 从缓存获取图片
	 * @param imgFullPath
	 * @param imgUrl
	 * @return
	 */
	protected final Bitmap loadImgFromMemery(String imgFullPath, String imgUrl, String groupLabel) {
		String key = getImgKeyInMemory(imgFullPath, imgUrl, groupLabel);
		if (null == key) {
			return null;
		}
		return mImageCache.get(key);
	}
	
	/**
	 * 保存图片到缓存
	 * @param bitmap
	 * @param imgFullPath
	 * @param imgUrl
	 */
	private void saveImgToMemory(Bitmap bitmap, String imgFullPath, String imgUrl, String groupLabel) {
		if (null == bitmap) {
			return;
		}
		String key = getImgKeyInMemory(imgFullPath, imgUrl, groupLabel);
		if (null == key) {
			return;
		}
		mImageCache.put(key, bitmap);
	}
	
	/**
	 * 获取图片在缓存中的key
	 * @param imgFullPath
	 * @param imgUrl
	 * @return
	 */
	private String getImgKeyInMemory(String imgFullPath, String imgUrl, String groupLabel) {
		if (imgUrl != null && imgUrl.startsWith("http:")) {
			return groupLabel + "," + imgUrl;
		}
		return groupLabel + "," + imgFullPath;
	}

	/**
	 * 分组标签管理器
	 *
	 * @author matt
	 * @date: 2015年5月6日
	 *
	 */
	protected class LabelManager {
		private List<String> mLabels = new ArrayList<String>();
		private byte[] mLockLabels = new byte[0];
		
		public void addLabel(String label) {
			synchronized (mLockLabels) {
				if (!mLabels.contains(label)) {
					mLabels.add(label);
				}
			}
		}
		
		public void removeLabel(String label) {
			synchronized (mLockLabels) {
				mLabels.remove(label);
			}
		}
		
		public void clearLabel() {
			synchronized (mLockLabels) {
				mLabels.clear();
			}
		}
		
		public boolean contains(String label) {
			synchronized (mLockLabels) {
				return mLabels.contains(label);
			}
		}
	}
	
	/**
	 * 图片加载请求信息
	 *
	 * @author matt
	 * @date: 2015年5月6日
	 *
	 */
	public static class ImageLoadRequest {
		/** 图片URL, 加载SD卡图片可传null，其它情况必须有值 */
		String mImageUrl;
		/** 图片保存到SD卡的目录, 路径尾部须带路径分隔符“/” */
		private String mImagePath;
		/** 图片保存到SD卡的名称 */
		private String mImageName;
		/** 加载图片成功后，是否缓存到内存 */
		protected boolean mIsCache = true;
		/** 网络图片处理器。当图片从网络加载完时，先经过operator处理再保存到SD卡，如果不需要处理，则传null */
		protected AsyncNetBitmapOperator mNetBitmapOperator;
		/** 图片加载结果回调, 回调在UI线程上执行 */
		protected AsyncImageLoadResultCallBack mCallBack;
		/** 图片分组标签，用于优先加载当前急需显示图片 */
		private String mGroupLabel;
		/** 图片压缩配置 */
		protected ImageScaleConfig mScaleCfg;
		/** 请求发起时间 */
		protected long mRequestTime;
		
		public ImageLoadRequest(String groupLabel, String imageUrl, String imagePath) {
			mGroupLabel = groupLabel == null ? "" : groupLabel;
			mImageUrl = imageUrl;
			mImagePath = imagePath;
			mImageName = imageUrl.hashCode() + "";
			if (!TextUtils.isEmpty(groupLabel)) {
				mImageName = groupLabel + "-" + mImageName;
			}
			mRequestTime = System.currentTimeMillis();
		}
		
		public String getImageUrl() {
			return mImageUrl;
		}
		
		/**
		 * 获取图片存储全路径
		 * @return
		 */
		public String getImageSavePath() {
			return mImagePath + mImageName;
		}
		
		@Override
		public String toString() {
			return String.format("[ImageLoadRequest] mGroupLabel:%s, mImageUrl:%s, mRequestTime:%d", 
					mGroupLabel, mImageUrl, mRequestTime);
		}
	}
	
	/**
	 * 图片（按比例）压缩配置信息
	 *
	 * @author matt
	 * @date: 2015年5月6日
	 *
	 */
	public static class ImageScaleConfig {
		//显示区域width，单位px
		public int mViewWidth;
		//显示区域height，单位px
		public int mViewHeight;
		//是否允许图片显示时被剪切（即图片超出显示区域）
		public boolean mIsCropInView;
		
		public ImageScaleConfig(int viewWidth, int viewHeight, boolean isCropInView) {
			mViewWidth = viewWidth;
			mViewHeight = viewHeight;
			mIsCropInView = isCropInView;
		}
	}
	
	/**
	 * 实现了优先级的Runnable
	 *
	 * @author matt
	 * @date: 2015年5月5日
	 *
	 */
	private abstract class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
		private static final int PRIORITY_HIGH = 1;
		private static final int PRIORITY_NORMAL = 0;
		//请求信息
		protected ImageLoadRequest mRequest;
		
		PriorityRunnable(ImageLoadRequest request) {
			mRequest = request; 
		}
		
		int getPriority() {
			return mLabelManager.contains(mRequest.mGroupLabel) ? PRIORITY_HIGH : PRIORITY_NORMAL;
		}
		
		@Override
		public int compareTo(PriorityRunnable another) {
			int priority1 = getPriority();
			int priority2 = another.getPriority();
//			return priority1 < priority2 ? 1 : (priority1 == priority2 ? 0 : -1);
			if (priority1 < priority2) {
				//this优先级低，排在后边
				return 1;
			} else if (priority1 > priority2) {
				//this优先级高，排在前边
				return -1;
			} else if (mRequest.mRequestTime < another.mRequest.mRequestTime) {
				//this请求时间早，排在前边
				return -1;
			} else if (mRequest.mRequestTime > another.mRequest.mRequestTime) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * 获取图片后，同步到主线程，回调Runable
	 * @author matt
	 *
	 */
	private static class CallBackRunnable implements Runnable {
		private Bitmap mBitmap;
		private ImageLoadRequest mRequest;

		CallBackRunnable(Bitmap img, ImageLoadRequest request) {
			mBitmap = img;
			mRequest = request;
		}

		@Override
		public void run() {
			if (null == mRequest.mCallBack) {
				return;
			}
			if (mBitmap != null) {
				mRequest.mCallBack.imageLoadSuccess(mRequest.mImageUrl, mBitmap, mRequest.getImageSavePath());
			} else {
				mRequest.mCallBack.imageLoadFail(mRequest.mImageUrl, -1);
			}
		}
	}

	/**
	 * 图片加载结果回调接口
	 * @author matt
	 *
	 */
	public static interface AsyncImageLoadResultCallBack {
		/**
		 * 图片加载成功
		 * @param imgUrl 图片url, 也是图片的key
		 * @param bmp 
		 * @param imgSavePath 图片加载成功后保存到SD卡的全路径
		 */
		public void imageLoadSuccess(String imgUrl, Bitmap bmp, String imgSavePath);
		
		/**
		 * 图片加载失败
		 * @param imgUrl
		 * @param exception 异常代码---暂无使用，预留接口
		 */
		public void imageLoadFail(String imgUrl, int exception);
	}
	
	/**
	 * 图片加载结果回调，用于简化代码，无需实现 {@link AsyncImageLoadResultCallBack#imageLoadFail(String, int)}
	 *
	 * @author matt
	 * @date: 2015年2月27日
	 *
	 */
	public static abstract class SimpleImageLoadResultCallBack implements AsyncImageLoadResultCallBack {
		public void imageLoadFail(String imgUrl, int exception) {
		}
	}
	
	/**
	 * 图片处理器, 如加载图片后，需要做处理则实现此接口, 如限制图片尺寸等
	 * 
	 */
	public static interface AsyncNetBitmapOperator {
		public Bitmap operateBitmap(Bitmap bmp);
	}
}