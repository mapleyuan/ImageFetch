package com.maple.imagefetchcore.imagemanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.ImageView;

import com.maple.imagefetchcore.utils.LogUtil;

/**
 * 图片管理器, 
 * @author matt
 * @备注 业务内容可在此处添加, 勿修改ImageLoader 
 * @备注 简化了图片加载接口, 所有loadXXX方法都调用这里的, 不要调用父类
 */
public class AsyncImageManager extends AsyncListImageLoader {
	//是否自动更新分组标签
	private final static boolean IS_AUTO_UPDATE_GROUPLABEL = true;
	//设置imageview时用到的key
	public final static int IMAGEVIEW_TAG_KEY = -123456;
	
	public final static String SDCARD = Environment.getExternalStorageDirectory().getPath();
	/**
	 * 需要保存到SD卡的图片, 统一保存在此目录, 路径尾部须带路径分隔符“/”
	 */
	private static final String IMAGE_DIR = SDCARD + "/commerce/images/";
	private static String sImageDir = null;
	private static AsyncImageManager sInstance = null;
	private Context mContext;

	private AsyncImageManager(Context context, IImageCache imageCache) {
		super(imageCache);
		mContext = context.getApplicationContext();
		sImageDir = IMAGE_DIR;
		
		//添加业务需要的图片加载器
		addImageLoader(new SdImageLoader());
	}
	
	public static AsyncImageManager getInstance(Context context) {
		if (null == sInstance) {
			int cacheSize = LruImageCache.getImagesMaxMemorySizeSuggested(context);
			IImageCache secondaryCache = new ImageWeakCache();
			sInstance = new AsyncImageManager(context, new LruImageCache(cacheSize, secondaryCache));
		}
		return sInstance;
	}
	
	/**
	 * 设置当前分组标签
	 * @param groupLabel
	 */
	private void setCurrentGroupLabel(String groupLabel) {
		//只允许有一个当前标签，简化逻辑
		mLabelManager.clearLabel();
		mLabelManager.addLabel(groupLabel);
	}
	
//	/**
//	 * 添加遮罩转换图片成圆形
//	 * @param imageView 外部务必不要设置此ImageView的key为{@link #IMAGEVIEW_TAG_KEY}的Tag，否则不能成功显示图片
//	 * @param groupLabel 图片分组标签，用于图片加载优先级。不建议传null或""。
//	 * @param imgUrl 图片URL
//	 * @param scaleCfg 图片压缩配置
//	 * @param width 图片圆角处理后的宽度
//	 * @param height 图片圆角处理后的高度
//	 * @return 是否图片在内存里
//	 * @备注 仅在接口返回false时，要设置ImageView的默认显示图， 如调用{@link ImageView#setImageResource}
//	 */
//	public boolean setImageViewRound(final ImageView imageView, String groupLabel, String imgUrl, ImageScaleConfig scaleCfg, int width, int height) {
//		return setImageView(imageView, groupLabel, imgUrl, scaleCfg, new BitmapRoundOperator(mContext, width, height));
//	}
//	
//	/**
//	 * 加载图片，图片通过回调返回
//	 * @param groupLabel 图片分组标签，用于图片加载优先级。不建议传null或""。
//	 * @param imgUrl 图片URL
//	 * @param scaleCfg 图片压缩配置
//	 * @param callBack 回调到UI线程
//	 * @param width
//	 * @param height
//	 * @return 是否图片在内存里
//	 * @备注 仅在接口返回false时，要设置ImageView的默认显示图， 如调用{@link ImageView#setImageResource}
//	 */
//	public boolean loadImageRound(String groupLabel, String imgUrl, ImageScaleConfig scaleCfg, final int width, final int height, AsyncImageLoadResultCallBack callBack) {
//		return loadImage(groupLabel, imgUrl, scaleCfg, new BitmapRoundOperator(mContext, width, height), callBack);
//	}
	
	/**
	 * 设置ImageView显示图片
	 * @param imageView 外部务必不要设置此ImageView的key为{@link #IMAGEVIEW_TAG_KEY}的Tag，否则不能成功显示图片
	 * @param groupLabel 图片分组标签，用于图片加载优先级。不建议传null或""。
	 * @param imgUrl 图片URL
	 * @param scaleCfg 图片压缩配置
	 * @param netBitmapOperator 网络图片处理器
	 * @return 是否图片在内存里
	 * @备注 仅在接口返回false时，要设置ImageView的默认显示图， 如调用{@link ImageView#setImageResource}
	 */
	public boolean setImageView(final ImageView imageView, String groupLabel, String imgUrl, 
			ImageScaleConfig scaleCfg, AsyncNetBitmapOperator netBitmapOperator) {
		imageView.setTag(IMAGEVIEW_TAG_KEY, imgUrl);
		return loadImage(groupLabel, imgUrl, scaleCfg, netBitmapOperator, new SimpleImageLoadResultCallBack() {
			
			@Override
			public void imageLoadSuccess(String imgUrl, Bitmap bmp, String imgSavePath) {
				Object tag = imageView.getTag(IMAGEVIEW_TAG_KEY);
				if (tag instanceof String && tag.equals(imgUrl)) {
					imageView.setImageBitmap(bmp);
				}
			}
		});
	}
	
	/**
	 * 专门针对列表项里ImageView加载图片的方法
	 * @param imageView 外部务必不要设置此ImageView的key为{@link #IMAGEVIEW_TAG_KEY}的Tag，否则不能成功显示图片
	 * @param position 列表项位置
	 * @param groupLabel 图片分组标签，用于图片加载优先级。不建议传null或""。
	 * @param imgUrl 图片URL
	 * @param scaleCfg 图片压缩配置
	 * @param netBitmapOperator 网络图片处理器
	 * @return 是否图片在内存里
	 * @备注 仅在接口返回false时，要设置ImageView的默认显示图， 如调用{@link ImageView#setImageResource}
	 */
	public boolean setImageViewForList(final ImageView imageView, int position, String groupLabel, String imgUrl,
			ImageScaleConfig scaleCfg, AsyncNetBitmapOperator netBitmapOperator) {
		imageView.setTag(IMAGEVIEW_TAG_KEY, imgUrl);
		return loadImageForList(position, groupLabel, imgUrl, scaleCfg, netBitmapOperator, new SimpleImageLoadResultCallBack() {
			
			@Override
			public void imageLoadSuccess(String imgUrl, Bitmap bmp, String imgSavePath) {
				Object tag = imageView.getTag(IMAGEVIEW_TAG_KEY);
				if (tag instanceof String && tag.equals(imgUrl)) {
					imageView.setImageBitmap(bmp);
				}
			}
		});
	}
	
	/**
	 * 加载图片，图片通过回调返回
	 * @param groupLabel 图片分组标签，用于图片加载优先级。不建议传null或""。
	 * @param imgUrl 图片URL
	 * @param scaleCfg 图片压缩配置
	 * @param netBitmapOperator 网络图片处理器
	 * @param callBack 回调到UI线程
	 * @return 是否图片在内存里
	 */
	public boolean loadImage(String groupLabel, String imgUrl, ImageScaleConfig scaleCfg, 
			AsyncNetBitmapOperator netBitmapOperator, AsyncImageLoadResultCallBack callBack) {
		if (null == imgUrl) {
			return false;
		}
		if (IS_AUTO_UPDATE_GROUPLABEL) {
			setCurrentGroupLabel(groupLabel);
		}
		ImageLoadRequest request = new ImageLoadRequest(groupLabel, imgUrl, sImageDir);
		request.mScaleCfg = scaleCfg;
		request.mNetBitmapOperator = netBitmapOperator;
		request.mCallBack = callBack;
//		request.mCallBack = new ImageLoadResultLogger(callBack);
		
		return loadImage(request, groupLabel);
	}
	
	/**
	 * 专门针对列表项加载图片的方法
	 * @param position 列表项位置
	 * @param groupLabel 图片分组标签，用于图片加载优先级。不建议传null或""。
	 * @param imgUrl 图片URL
	 * @param scaleCfg 图片压缩配置
	 * @param netBitmapOperator 网络图片处理器
	 * @param callBack 回调到UI线程
	 * @return 是否图片在内存里
	 * @备注: 在列表停止滚动时才去加载图片
	 */
	public boolean loadImageForList(int position, String groupLabel, String imgUrl, ImageScaleConfig scaleCfg,
			AsyncNetBitmapOperator netBitmapOperator, AsyncImageLoadResultCallBack callBack) {
		if (null == imgUrl) {
			return false;
		}
		if (IS_AUTO_UPDATE_GROUPLABEL) {
			setCurrentGroupLabel(groupLabel);
		}
		ImageLoadRequest request = new ImageLoadRequest(groupLabel, imgUrl, sImageDir);
		request.mScaleCfg = scaleCfg;
		request.mNetBitmapOperator = netBitmapOperator;
		request.mCallBack = callBack;

		return loadImageForList(position, request, groupLabel);
	}
	
	/**
	 * 
	 *
	 * @author matt
	 * @date: 2015年9月22日
	 *
	 */
	private class ImageLoadResultLogger implements AsyncImageLoadResultCallBack {
		private AsyncImageLoadResultCallBack mCallBack;
		
		private ImageLoadResultLogger(AsyncImageLoadResultCallBack callBack) {
			mCallBack = callBack;
		}

		@Override
		public void imageLoadSuccess(String imgUrl, Bitmap bmp,
				String imgSavePath) {
			String hashcode = imgUrl != null ? "" + imgUrl.hashCode() : null;
			LogUtil.i("matt", "imageLoadSuccess<>hashcode:" + hashcode + "<>imgUrl:" + imgUrl);
			if (mCallBack != null) {
				mCallBack.imageLoadSuccess(imgUrl, bmp, imgSavePath);
			}
		}

		@Override
		public void imageLoadFail(String imgUrl, int exception) {
			String hashcode = imgUrl != null ? "" + imgUrl.hashCode() : null;
			LogUtil.i("matt", "imageLoadFail<>hashcode:" + hashcode + "<>imgUrl:" + imgUrl);
			if (mCallBack != null) {
				mCallBack.imageLoadFail(imgUrl, exception);
			}
		}
		
	}
}
