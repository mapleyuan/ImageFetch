package com.maple.imagefetchcore.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件相关操作工具类
 *
 */
public class FileUtils {
	//LogTag
	private static final String LOG_TAG = "appcenter_file";
	
	/**
	 * 创建文件夹
	 * @param dir
	 */
	public static void mkDir(final String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			try {
				file.mkdirs();
			} catch (Exception e) {
			}
		}
		file = null;
	}

	/**
	 * 安全地（使用临时文件）保存输入流内容到SD卡文件
	 * @param inputStream
	 * @param filePathName 待保存的文件完整路径名
	 * @return
	 */
	public static boolean saveInputStreamToSDFileSafely(InputStream inputStream,
														String filePathName) {
		if (null == inputStream) {
			return false;
		}

		boolean result = false;
		OutputStream os = null;
		try {
			String tempFilePathName = filePathName + "-temp";
			File file = createNewFile(tempFilePathName, false);
			os = new FileOutputStream(file);
			byte buffer[] = new byte[4 * 1024];
			int len = 0;
			while ((len = inputStream.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			os.flush();
			file.renameTo(new File(filePathName));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 保存位图到sd卡目录下
	 * 
	 * @param bitmap
	 *            ：位图资源
	 * @param filePathName
	 *            ：待保存的文件完整路径名
	 * @param iconFormat
	 *            ：图片格式
	 * @return true for 保存成功，false for 保存失败。
	 */
	public static boolean saveBitmapToSDFile(final Bitmap bitmap, final String filePathName,
			CompressFormat iconFormat) {
		boolean result = false;
		if (bitmap == null || bitmap.isRecycled()) {
			return result;
		}
		try {
			createNewFile(filePathName, false);
			OutputStream outputStream = new FileOutputStream(filePathName);
			result = bitmap.compress(iconFormat, 100, outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * sd卡是否可读写
	 * 
	 * @return
	 */
	public static boolean isSDCardAvaiable() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	/**
	 * 创建文件
	 * @param path 文件路径
	 * @param append 若存在是否插入原文件
	 * @return
	 */
	public static File createNewFile(String path, boolean append) {
		File newFile = new File(path);
		if (!append) {
			if (newFile.exists()) {
				newFile.delete();
			} else {
				// 不存在，则删除带png后缀名的文件
				File prePngFile = new File(path + ".png");
				if (prePngFile != null && prePngFile.exists()) {
					prePngFile.delete();
				}
			}
		}
		if (!newFile.exists()) {
			try {
				File parent = newFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				newFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return newFile;
	}
	
	/**
	 * 创建文件
	 * 注意:1:如果不存在父文件夹,则新建文件夹;2:如果文件已存在,则直接返回.
	 * @param destFileName
	 * @param replace
	 * @return
	 */
	public static boolean createFile(String destFileName, boolean replace) {
		File file = new File(destFileName);
		if (file.exists()) {
			if (replace) {
				file.delete();
			} else {
				LogUtil.d(LOG_TAG, "创建单个文件" + destFileName + "失败，目标文件已存在！");
				return false;
			}
		}
		if (destFileName.endsWith(File.separator)) {
			LogUtil.d(LOG_TAG, "创建单个文件" + destFileName + "失败，目标不能是目录！");
			return false;
		}
		if (!file.getParentFile().exists()) {
			LogUtil.d(LOG_TAG, "目标文件所在路径不存在，准备创建。。。");
			if (!file.getParentFile().mkdirs()) {
				LogUtil.d(LOG_TAG, "创建目录文件所在的目录失败！");
				return false;
			}
		}
		// 创建目标文件
		try {
			if (file.createNewFile()) {
				LogUtil.d(LOG_TAG, "创建单个文件" + destFileName + "成功！");
				return true;
			} else {
				LogUtil.d(LOG_TAG, "创建单个文件" + destFileName + "失败！");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.d(LOG_TAG, "创建单个文件" + destFileName + "失败！");
			return false;
		}
	}
	
	/**
	 * 保存字符串到sdcard
	 * @param string 要保存的字符串
	 * @param fileName 要保存的文件名称
	 * @return
	 */
	public static boolean saveStringToSDFile(final String string, final String fileName) {
		if (TextUtils.isEmpty(string)) {
			return false;
		}
		try {
			return saveLogByteToSDFile(string.getBytes("UTF-8"), fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 保存数据到指定文件
	 * 
	 * @author huyong
	 * @param byteData
	 * @param filePathName
	 * @return true for save successful, false for save failed.
	 */
	public static boolean saveLogByteToSDFile(final byte[] byteData, final String filePathName) {
		if (byteData == null || TextUtils.isEmpty(filePathName)) {
			return false;
		}
		boolean result = false;
		try {
			File newFile = createNewFile(filePathName, true);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			fileOutputStream.write(byteData);
			fileOutputStream.flush();
			fileOutputStream.close();
			result = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 保存数据到指定文件
	 * 
	 * @author huyong
	 * @param byteData
	 * @param filePathName
	 * @return true for save successful, false for save failed.
	 */
	public static boolean saveByteToSDFile(final byte[] byteData, final String filePathName) {
		if (byteData == null || TextUtils.isEmpty(filePathName)) {
			return false;
		}
		boolean result = false;
		try {
			File newFile = createNewFile(filePathName, false);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			fileOutputStream.write(byteData);
			fileOutputStream.flush();
			fileOutputStream.close();
			result = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 将InputStream保存到sdcard
	 * @param inputStream
	 * @param filePathName 文件路径
	 * @return
	 */
	public static boolean saveInputStreamToSDFile(InputStream inputStream, String filePathName) {
        boolean result = false;
        OutputStream os = null;
        try {
            File file = createNewFile(filePathName, false);
            os = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
	
	/**
	 * 复制文件
	 * @param srcStr 源文件路径
	 * @param decStr 目的文件路径
	 */
	public static void copyFile(String srcStr, String decStr) {
		File srcFile = new File(srcStr);
		if (!srcFile.exists()) {
			return;
		}
		File decFile = new File(decStr);
		if (!decFile.exists()) {
			File parent = decFile.getParentFile();
			parent.mkdirs();
			try {
				decFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(srcFile);
			output = new FileOutputStream(decFile);
			byte[] data = new byte[4 * 1024]; // 4k
			while (true) {
				int len = input.read(data);
				if (len <= 0) {
					break;
				}
				output.write(data);
			}
		} catch (Exception e) {
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (Exception e2) {
				}
			}
			if (null != output) {
				try {
					output.close();
				} catch (Exception e2) {
				}
			}
		}
	}
	
	/**
	 * 从sdcard读取文件
	 * @param filePathName 文件路径
	 * @return
	 */
	public static byte[] readByteFromSDFile(final String filePathName) {
		byte[] bs = null;
		try {
			File newFile = new File(filePathName);
			FileInputStream fileInputStream = new FileInputStream(newFile);
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			BufferedInputStream inPutStream = new BufferedInputStream(dataInputStream);
			bs = new byte[(int) newFile.length()];
			inPutStream.read(bs);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bs;
	}
	
	/**
	 * 从sdcard读取文件
	 * 
	 * @param filePath 文件路径
	 * @return
	 */
	public static String readFileToString(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		try {
			InputStream inputStream = new FileInputStream(file);
			return readInputStream(inputStream, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return null;
	}
	
	/**
	 * 读取输入流,转为字符串
	 * 
	 * @param in
	 * @param charset 字符格式
	 * @return
	 * @throws IOException
	 */
	public static String readInputStream(InputStream in, String charset) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		final int bufferLength = 1024;
		byte[] data;
		try {
			byte[] buf = new byte[bufferLength];
			int len = 0;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			data = out.toByteArray();
			return new String(data, charset);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		return null;
	}
	
	/**
	 * 读取stream并返回一个前length长的string
	 * @param in
	 * @param charset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static String readInputStreamWithLength(InputStream in, String charset, int length) throws IOException {
		if (in == null) {
			return "";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		final int bufferLength = 1024;
		byte[] data;
		try {
			byte[] buf = new byte[bufferLength];
			int len = 0;
			int i = 0;
			while ((len = in.read(buf)) > 0 && i < length) {
				out.write(buf, 0, len);
				i++;
			}
			data = out.toByteArray();
			return new String(data, TextUtils.isEmpty(charset) ? "UTF-8" : charset);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		return null;
	}
	
	/**
	 * 删除指定目录,包括目录下的文件夹和文件
	 * @param sPath
	 * @return
	 */
	public static boolean deleteDirectory(String sPath) {   
	    //如果sPath不以文件分隔符结尾，自动添加文件分隔符   
	    if (!sPath.endsWith(File.separator)) {   
	        sPath = sPath + File.separator;   
	    }   
	    File dirFile = new File(sPath);   
	    //如果dir对应的文件不存在，或者不是一个目录，则退出   
	    if (!dirFile.exists() || !dirFile.isDirectory()) {   
	        return false;   
	    }   
	    boolean flag = true;   
	    //删除文件夹下的所有文件(包括子目录)   
	    File[] files = dirFile.listFiles();   
	    for (int i = 0; i < files.length; i++) {   
	        //删除子文件   
	        if (files[i].isFile()) {   
	            flag = deleteFile(files[i].getAbsolutePath());   
				if (!flag) {
					break;
				}
	        } //删除子目录   
	        else {   
	            flag = deleteDirectory(files[i].getAbsolutePath());   
	            if (!flag) {
	            	break;
	            }   
	        }   
	    }   
	    if (!flag) {
	    	return false;
	    }
	    //删除当前目录   
	    if (dirFile.delete()) {   
	        return true;   
	    } else {   
	        return false;   
	    }   
	}  
	
	/**
	 * 根据给定路径参数删除单个文件的方法
	 * 
	 * @param filePath 要删除的文件路径
	 * @return 成功返回true,失败返回false
	 */
	public static boolean deleteFile(String filePath) {
		// 定义返回结果
		boolean result = false;
		if (!TextUtils.isEmpty(filePath)) {
			File file = new File(filePath);
			if (file.exists()) {
				result = file.delete();
			}
		}
		return result;
	}
	
	/**
	 * 指定路径文件是否存在
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean isFileExist(String filePath) {
		boolean result = false;
		try {
			File file = new File(filePath);
			result = file.exists();
			file = null;
		} catch (Exception e) {
		}
		return result;
	}
	
	/**
	 * 获取文件大小
	 * @param path 文件路径
	 * @return
	 */
	public static long getFileSize(String path) {
		long size = 0;
		if (path != null) {
			File file = new File(path);
			size = file.length();
		}
		return size;
	}
}
