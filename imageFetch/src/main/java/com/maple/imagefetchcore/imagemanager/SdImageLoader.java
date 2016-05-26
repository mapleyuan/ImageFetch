package com.maple.imagefetchcore.imagemanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.maple.imagefetchcore.utils.FileUtils;

import java.io.File;

/**
 * sd卡图片加载器
 *
 * @author matt
 * @date: 2015年5月6日
 *
 */
public class SdImageLoader implements ImageLoaderItf {

	@Override
	public boolean isHandle(String imgUrl) {
		return imgUrl != null && imgUrl.startsWith(ImageLoaderTag.SD_IMAGE_PRENAME);
	}

	@Override
	public Bitmap loadImage(AsyncImageLoader.ImageLoadRequest request) {
		String imgUrl = request.getImageUrl();
		String imgFullPath = imgUrl.substring(ImageLoaderTag.SD_IMAGE_PRENAME.length());;
		return loadImgFromSD(imgFullPath, request.mScaleCfg);
	}

	@Override
	public boolean isSave2SDCard() {
		return false;
	}

	public static String buildUrl(String imgFullPath) {
		if (null == imgFullPath) {
			return null;
		}
		return ImageLoaderTag.SD_IMAGE_PRENAME + imgFullPath;
	}
	
	/**
	 * 加载sd卡图片
	 * @param imgFullPath
	 * @param scaleCfg
	 * @return
	 */
	public static Bitmap loadImgFromSD(String imgFullPath, AsyncImageLoader.ImageScaleConfig scaleCfg) {
		if (scaleCfg == null) {
			return loadImgFromSD(imgFullPath);
		} else {
			return loadImgFromSD(imgFullPath, scaleCfg.mViewWidth, scaleCfg.mViewHeight, scaleCfg.mIsCropInView);
		}
	}
	
	/**
	 * 加载sd卡图片
	 * @param imgFullPath
	 * @return
	 */
	private static Bitmap loadImgFromSD(String imgFullPath) {
		Bitmap result = null;
		try {
			if (FileUtils.isSDCardAvaiable()) {
				File file = new File(imgFullPath);
				if (file.exists()) {
					result = BitmapFactory.decodeFile(imgFullPath);
				}
			}
		} catch (OutOfMemoryError ex) {
			ex.printStackTrace();
			System.gc();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
    /**
     * 根据显示区域的大小加载sd卡图片
     * 
     * @param viewWidth 视图区域的宽度
     * @param viewHeight 视图区域的高度
     * @param isCropInView 是否允许图片显示时被剪切（即图片超出显示区域）
     * 		       如果为true, 那么对于高宽比例比较极端的图片加载时很大可能是以原图大小加载了
     * @return
     */
    private static Bitmap loadImgFromSD(String imgFullPath, int viewWidth, int viewHeight, boolean isCropInView) {
    	if (null == imgFullPath) {
    		return null;
    	}
    	File file = new File(imgFullPath);
        if (null == file || !file.exists()) {
            return null;
        }
        //设置options获取图片宽高
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        
        // 得到图片的宽度、高度；  
        int imgWidth = opts.outWidth;  
        int imgHeight = opts.outHeight;  
        
        // 分别计算图片宽度、高度与视图宽度、高度的比例；取大于该比例的最小整数；  
        int widthRatio = (int) Math.ceil(imgWidth / (float) viewWidth);  
        int heightRatio = (int) Math.ceil(imgHeight / (float) viewHeight);
        /**
         *  实际上最终opts.inSampleSize取的值是一个接近inSampleSize且是2的平方的数,
         *  所以图片的最终大小不一定是与计算的inSampleSize缩放后的值一样
         */
        if (isCropInView) {
        	opts.inSampleSize = Math.min(widthRatio, heightRatio);
        } else {
        	opts.inSampleSize = Math.max(widthRatio, heightRatio);
        }

        // 设置好缩放比例后，加载图片进内存；  
        opts.inJustDecodeBounds = false;
        boolean outMemory = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
            outMemory = false;
        } catch (OutOfMemoryError e) {
            outMemory = true;
            e.printStackTrace();
        }

        // XXX 如果由于爆内存而加载失败, 尝试进一步减小加载的图片大小
        while (null == bitmap && outMemory) {
            opts.inSampleSize = opts.inSampleSize + 1;
            try {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                outMemory = false;
            } catch (OutOfMemoryError e) {
                outMemory = true;
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
