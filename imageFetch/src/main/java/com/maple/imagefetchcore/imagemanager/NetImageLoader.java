package com.maple.imagefetchcore.imagemanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.maple.imagefetchcore.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * 网络图片加载器
 * @author matt
 *
 */
public class NetImageLoader implements ImageLoaderItf {
	//网络图片超时设置
	private static final int CONNECT_TIME_OUT = 12 * 1000;
	private static final int READ_TIME_OUT = 12 * 1000;
	
	@Override
	public boolean isHandle(String imgUrl) {
		return imgUrl != null && (imgUrl.startsWith("http:") || imgUrl.startsWith("https:"));
	}

	@Override
	public Bitmap loadImage(AsyncImageLoader.ImageLoadRequest request) {
		String imgUrl = request.getImageUrl();
		String imgSavePath = request.getImageSavePath();
		if (!FileUtils.isSDCardAvaiable()) {
			//SD卡不可用，直接读进内存
			return loadImgFromNetwork(imgUrl, request.mScaleCfg);
		} else if (request.mNetBitmapOperator != null) {
			//SD卡可用，且有网络图片处理器：先读进内存进行处理，再保存到sd卡，再从sd卡读取压缩图片(不需压缩则直接返回)
			Bitmap bmp = loadImgFromNetwork(imgUrl, request.mScaleCfg);
			//图片加载后的处理
			if (bmp != null) {
				try {
					bmp = request.mNetBitmapOperator.operateBitmap(bmp);
				} catch (OutOfMemoryError ex) {
					ex.printStackTrace();
					System.gc();
				}
			}
			boolean isSaveSucc = saveImgToSD(bmp, imgSavePath);
			if (!isSaveSucc) { //保存失败直接返回
				return bmp;
			}
			if (request.mScaleCfg == null) { //不需压缩则直接返回
				return bmp;
			}
			return SdImageLoader.loadImgFromSD(imgSavePath, request.mScaleCfg);
		} else {
			//SD卡可用，且无网络图片处理器：直接保存到SD卡，再从SD卡读取压缩图片
			boolean isSaveSucc = saveNetImgToSD(imgUrl, imgSavePath);
			if (!isSaveSucc) {
				return null;
			}
			return SdImageLoader.loadImgFromSD(imgSavePath, request.mScaleCfg);
		}
			
	}

	@Override
	public boolean isSave2SDCard() {
		return true;
	}
	
	/**
	 * 保存图片到SD卡
	 * @param bitmap
	 * @param imgFullPath
	 */
	private static boolean saveImgToSD(Bitmap bitmap, String imgFullPath) {
		if (null == bitmap || null == imgFullPath || !FileUtils.isSDCardAvaiable()) {
			return false;
		}
		return FileUtils.saveBitmapToSDFile(bitmap, imgFullPath, Bitmap.CompressFormat.PNG);
	}
	
	/**
	 * 保存网络图片到SD卡
	 * @param imgUrl
	 * @param savePath
	 * @return
	 */
	public static boolean saveNetImgToSD(String imgUrl, String savePath) {
		boolean result = false;
		InputStream inputStream = null;
		HttpURLConnection urlCon = null;
		try {
			urlCon = createURLConnection(imgUrl);
			inputStream = urlCon.getInputStream();
			result = FileUtils.saveInputStreamToSDFileSafely(inputStream, savePath);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
//		} catch (SocketTimeoutException e) {
//			e.printStackTrace();
		} catch (Exception e) {
//			String hashcode = imgUrl != null ? imgUrl.hashCode() + "" : null;
//			LogUtils.w("matt", "hashcode:" + hashcode + "<>imgUrl:" + imgUrl, e);
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (urlCon != null) {
				urlCon.disconnect();
			}
		}
		return result;
	}
	
	/**
	 * 从网络加载图片
	 * @param imgUrl
	 * @return
	 */
	public static Bitmap loadImgFromNetwork(String imgUrl, AsyncImageLoader.ImageScaleConfig scaleCfg) {
		Bitmap result = null;
		InputStream inputStream = null;
		HttpURLConnection urlCon = null;
		try {
			urlCon = createURLConnection(imgUrl);
			inputStream = urlCon.getInputStream();
			if (inputStream != null) {
				if (scaleCfg != null) {
					result = loadBitmap(inputStream, scaleCfg.mViewWidth, scaleCfg.mViewHeight, scaleCfg.mIsCropInView);
				} else {
					result = loadBitmap(inputStream);
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (urlCon != null) {
				urlCon.disconnect();
			}
		}
		return result;
	}
	
	/**
	 * 根据URL生成HttpURLConnection
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static HttpURLConnection createURLConnection(String url) throws Exception {
		// TODO:wangzhuobin 是否需要设置代理?
		HttpURLConnection urlCon = null;
		urlCon = (HttpURLConnection) new URL(url).openConnection();
		urlCon.setConnectTimeout(CONNECT_TIME_OUT);
		urlCon.setReadTimeout(READ_TIME_OUT);
		return urlCon;
	}
	
	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static Bitmap loadBitmap(InputStream is) throws IOException {
		byte[] data = toByteArray(is);
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
	
	 /** 
     * 通过压缩图片的尺寸来压缩图片大小，通过读入流的方式，可以有效防止网络图片数据流形成位图对象时内存过大的问题； 
     *  
     * @param viewWidth 视图宽度(缩放的目标宽度 )
     * @param viewHeight 视图高度(缩放的目标高度) 
     * @return 缩放后的图片 
     * @throws IOException 读输入流的时候发生异常 
     */  
    public static Bitmap loadBitmap(InputStream is, int viewWidth, int viewHeight, boolean isCropInView) throws IOException {  
        byte[] data = toByteArray(is); 
        
        //设置options获取图片宽高
        BitmapFactory.Options opts = new BitmapFactory.Options();  
        opts.inJustDecodeBounds = true;  
        BitmapFactory.decodeByteArray(data, 0, data.length, opts); 
        // 得到图片的宽度、高度；  
        int imgWidth = opts.outWidth;  
        int imgHeight = opts.outHeight;  
        
        // 分别计算图片宽度、高度与视图宽度、高度的比例；取大于该比例的最小整数；  
        int widthRatio = (int) Math.ceil(imgWidth / (float) viewWidth);  
        int heightRatio = (int) Math.ceil(imgHeight / (float) viewHeight);  
        if (isCropInView) {
        	opts.inSampleSize = Math.min(widthRatio, heightRatio);
        } else {
        	opts.inSampleSize = Math.max(widthRatio, heightRatio);
        }
        
        // 设置好缩放比例后，加载图片进内存；  
        opts.inJustDecodeBounds = false;  
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);  
        return bitmap;  
    } 
    
	public static byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        byte[] buff = new byte[1024];  
        int len = 0;  
        while ((len = is.read(buff)) != -1) {  
            baos.write(buff, 0, len);  
        }  
  
        return baos.toByteArray();  
    }
}
