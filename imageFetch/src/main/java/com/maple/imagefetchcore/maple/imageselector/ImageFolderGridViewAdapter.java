package com.maple.imagefetchcore.maple.imageselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.maple.imagefetchcore.R;
import com.maple.imagefetchcore.imagemanager.AsyncImageLoader;
import com.maple.imagefetchcore.imagemanager.AsyncImageManager;
import com.maple.imagefetchcore.imagemanager.SdImageLoader;
import com.maple.imagefetchcore.maple.imageselector.pojo.FolderUnit;
import com.maple.imagefetchcore.maple.imageselector.pojo.ImageUnit;
import com.maple.imagefetchcore.maple.imageselector.util.BroadCaster;
import com.maple.imagefetchcore.maple.util.DrawUtils;
import com.maple.imagefetchcore.utils.LogUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yuanweinan
 */
public class ImageFolderGridViewAdapter extends BaseAdapter {
	private static final String CAMERA_NAME = "camera";
	// 图片加载器设置的标签，便以清除跟该适配类相关的线程任务
	public static final String IMAGE_LOADER_TAG = "tag_ImageFolderGridViewAdapter";

	// 调用系统相机返回的相片名前缀（存储在当前DCIM/Camera目录下），前缀+毫秒数.jpg
	public static final String CAMERA_PIC_NAME = "/DCIM/Camera/go";

	private File mFile;
	private List<ImageUnit> mImages;
	private FolderUnit mFolder;
	private Context mContext;
	File mCameraPicture; // 调用系统相机拍照返回的相片文件
	private MyClickListener mCameraListener;

	// ImageView宽和高
	private int mImageWidth;
	private int mImageHeight;

	public ImageFolderGridViewAdapter(Context context, FolderUnit folder) {
		mContext = context;
		mCameraListener = new MyClickListener();
		DrawUtils.resetDensity(mContext);
		mImageWidth = DrawUtils.sWidthPixels / 3; // 宽度为屏幕的三分之一
		mImageHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.chatplane_imageselector_scaled_size);
		// 清除之前的线程任务
		AsyncImageManager.getInstance(mContext.getApplicationContext())
				.removeTasks(IMAGE_LOADER_TAG);
		initGridList(folder);
	}

	/**
	 * 获取文件夹内的图片
	 */
	private void initGridList(FolderUnit folder) {
		mFolder = folder;
		mFile = null;
		if (mImages == null) {
			mImages = new ArrayList<ImageUnit>();
		}
		mImages.clear();
		addCameraItem(); // 第一个为相机
		// 传进来的文件夹不为空
		if (folder != null) {
			mFile = new File(folder.mFolderDir);
			List<String> images = Arrays.asList(mFile
					.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".jpg")
									|| filename.endsWith(".png")
									|| filename.endsWith(".jpeg")) {
								return true;
							}
							return false;
						}
					}));

			for (String name : images) {
				ImageUnit unit = new ImageUnit();
				unit.mImageName = name;
				mImages.add(unit);
			}
		}
	}

	public void setItemIsSelected(int pos, boolean isSelected) {
		ImageUnit unit = (ImageUnit) getItem(pos);
		if (unit != null) {
			unit.mIsSelected = isSelected;
			unit.broadCast(ImageUnit.SELECTED_CHANGED, -1);
		}
	}

	@Override
	public int getCount() {
		if (mFolder != null) {
			LogUtil.i("mFolder.mCount=" + mFolder.mCount);
		}
		return mImages.size(); // 添加相机选项
	}

	@Override
	public Object getItem(int arg0) {
		if (null == mImages || arg0 < 0 || arg0 >= mImages.size()) {
			return null;
		}
		return mImages.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	/**
	 * 返回指定位置图片路径
	 * 
	 * @param position
	 * @return
	 */
	public String getImagePath(int position) {
		return mFolder.mFolderDir + "/" + mImages.get(position).mImageName;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(
					R.layout.cp_imagefolder_gridview_item, null, false);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (ImageView) view
					.findViewById(R.id.gridview_imageview);
			viewHolder.mCheckSelectView = view
					.findViewById(R.id.gridview_imageview_select);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		ImageUnit unit = mImages.get(position);
		if (position == 0) {
			viewHolder.mImageView.setTag(AsyncImageManager.IMAGEVIEW_TAG_KEY,
					"");
			viewHolder.mImageView.setOnClickListener(mCameraListener);
			viewHolder.mImageView.setBackgroundDrawable(null);
//			viewHolder.mImageView.setImageResource(R.drawable.cp_camera_icon);
		} else {
			viewHolder.mImageView.setOnClickListener(null); // 移除监听器
			viewHolder.mImageView.setClickable(false);
			viewHolder.bind(unit);
			viewHolder.mCheckSelectView
					.setVisibility(unit.mIsSelected ? View.VISIBLE : View.GONE);

			loadImage(
					viewHolder.mImageView,
					mFolder.mFolderDir + "/" + unit.mImageName,
					SdImageLoader.buildUrl(mFolder.mFolderDir + "/"
							+ unit.mImageName), position);

		}

		// //异步加载图片LRU
		// ImageLoader.getInstance(3, Type.LIFO).loadImage(mFolder.mFolderDir +
		// "/" + unit.mImageName, viewHolder.mImageView);

		/*
		 * AsyncImageManager.getInstance(mContext).setImageViewForList(imgview,
		 * arg0 / 3 + 1, null, SdImageLoader.buildUrl(mFolder.mFolderDir + "/" +
		 * unit.mImageName), null, null);
		 */
		return view;
	}

	private void loadImage(final ImageView imageView, final String originPath,
			final String imageLoaderPath, int position) {
		imageView.setTag(AsyncImageManager.IMAGEVIEW_TAG_KEY, imageLoaderPath);
		boolean isInMemory = AsyncImageManager.getInstance(mContext)
				.loadImageForList(
						position,
						IMAGE_LOADER_TAG,
						imageLoaderPath,
						new AsyncImageLoader.ImageScaleConfig(mImageWidth,
								mImageHeight, true), null,
						new AsyncImageLoader.SimpleImageLoadResultCallBack() {
							@Override
							public void imageLoadSuccess(String imgUrl,
									Bitmap bmp, String imgSavePath) {
							}
						});

		if (!isInMemory) { // 不在内存里，加载过程中显示默认图
			imageView.setBackgroundResource(R.drawable.cp_img_selector_default);
			imageView.setImageDrawable(null);
		}
	}

	public void refreshData(FolderUnit folderUnit) {
		initGridList(folderUnit);
		notifyDataSetChanged();
	}

	/**
	 * 添加相机选项
	 */
	private void addCameraItem() {
		ImageUnit imgUnit = new ImageUnit();
		imgUnit.mImageName = CAMERA_NAME;
		mImages.add(imgUnit);
	}

	/**
	 * @author yuanweinan
	 */
	private class ViewHolder implements BroadCaster.BroadCasterObserver {
		public ImageView mImageView;
		public View mCheckSelectView;

		private ImageUnit mImageInfo;

		private void bind(ImageUnit imageInfo) {
			if (mImageInfo != null) {
				mImageInfo.unRegisterObserver(this);
			}
			imageInfo.registerObserver(this);
			mImageInfo = imageInfo;
		}

		@Override
		public void onBgChange(int msgId, int param, Object... objects) {
			switch (msgId) {
			case ImageUnit.SELECTED_CHANGED:
				mCheckSelectView
						.setVisibility(mImageInfo.mIsSelected ? View.VISIBLE
								: View.GONE);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 
	 * @author zhouhong
	 * 
	 */
	private class MyClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (mContext instanceof Activity) {

				// 利用系统自带的相机应用:拍照
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 调用android自带的照相机
				String sdStatus = Environment.getExternalStorageState();
				mCameraPicture = null;
				if (sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
					mCameraPicture = new File(
							Environment.getExternalStorageDirectory(),
							CAMERA_PIC_NAME + System.currentTimeMillis()
									+ ".jpg");
					LogUtil.i("hzw", mCameraPicture.getAbsolutePath());
					mCameraPicture.getParentFile().mkdirs();
					// 图片临时存放的位置
					Uri uri = Uri.fromFile(mCameraPicture);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				}
//				((Activity) mContext).startActivityForResult(intent,
//						ChatPlaneImageSelectorActivity.CAMEARE_OK);
			} else {
				LogUtil.e("不能进行类型转换");
			}
		}
	}

}
