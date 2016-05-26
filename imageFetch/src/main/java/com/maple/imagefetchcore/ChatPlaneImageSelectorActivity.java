package com.maple.imagefetchcore;//package com.maple.yuanweinan.imagefetch;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.commerce.chatplane.lib.main.view.SpinnerPopWindow;
//import com.commerce.chatplane.lib.main.view.SpinnerPopWindow.IOnPopItemClickListener;
//import com.commerce.chatplane.lib.main.view.SpinnerPopWindow.IOnTriangleDismissListener;
//import com.commerce.chatplane.lib.utils.BitmapUtils;
//import com.jb.ga0.commerce.util.LogUtils;
//import com.jb.ga0.commerce.util.imagemanager.AsyncImageManager;
//import com.maple.imageselector.FolederSpinnerAdapter;
//import com.maple.imageselector.ImageFolderGridViewAdapter;
//import com.maple.imageselector.ImagePickCustom;
//import com.maple.imageselector.ImagePickCustom.ImagePickSelectListener;
//import com.maple.imageselector.ImagePickCustom.ImageScanListener;
//import com.maple.imageselector.pojo.FolderUnit;
//
//import java.io.File;
//import java.util.List;
//
///**
// * 相册选择控件activity
// *
// * @author zhouhong
// *
// */
//public class ChatPlaneImageSelectorActivity extends Activity implements
//		ImageScanListener, ImagePickSelectListener, IOnTriangleDismissListener {
//	public static final String ACTIVITY_ACTION = "com.commerce.chatplane.imageselector.action";
//	public static final int CAMEARE_OK = 1;
//	public static final int CAMEARE_SELECT_OK = 2;
//	public static final int SELECTED_OK = 3;
//
//	private ImagePickCustom mPickCustom;
//	private TextView mFolderSpinner;
//	private ImageView mTriangle; // 三角形标志
//	private ImageView mBack;
//	private TextView mOk;
//
//	private List<FolderUnit> mFoldersList;
//	private int mSelectedIndex = 0;
//	private boolean mIsOpen = true; // true为折叠状态
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.cp_mail_send_image_slector_layout);
//		mPickCustom = (ImagePickCustom) findViewById(R.id.image_show_grid);
//		mFolderSpinner = (TextView) findViewById(R.id.folder_name_list);
//		mBack = (ImageView) findViewById(R.id.back);
//		mOk = (TextView) findViewById(R.id.ok);
//		mTriangle = (ImageView) findViewById(R.id.triangle);
//
//		mPickCustom.show(getApplicationContext(), this);
//		mPickCustom.setImageSelectedListener(this);
//
//		mBack.setOnClickListener(new MyClickListener());
//		mOk.setOnClickListener(new MyClickListener());
//	}
//
//	@Override
//	public void onScanFinished(final List<FolderUnit> folders) {
//		// FolederSpinnerAdapter spinnerAdapter = new
//		// FolederSpinnerAdapter(this, folders);
//		// mFolderSpinner.setAdapter(spinnerAdapter);
//		// 若加相机功能，此处size = 0 要区分处理
//		if (folders == null || folders.size() <= 0) {
//			return;
//		}
//		mFoldersList = folders;
//		String foldername = folders.get(0).getName();
//		foldername = foldername == null ? "" : foldername;
//		mFolderSpinner.setText(foldername);
//
//		if (folders.size() > 1) {
//			mFolderSpinner.setOnClickListener(new MyClickListener());
//			mTriangle.setOnClickListener(new MyClickListener());
//		} else {
//			mTriangle.setVisibility(View.GONE);
//		}
//	}
//
//	/**
//	 * 点击事件
//	 *
//	 * @author zhouhong
//	 *
//	 */
//	public class MyClickListener implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			int id = v.getId();
//			if (id == R.id.back) {
//				finish();
//				AsyncImageManager.getInstance(getApplicationContext()).clear(ImageFolderGridViewAdapter.IMAGE_LOADER_TAG);
//			} else if (id == R.id.ok) {
//				mPickCustom.onSelectBtnClickListener();
//			}
//			if (id == R.id.folder_name_list || id == R.id.triangle) {
//				if (mFoldersList == null) {
//					return;
//				}
//
//				Drawable drawableRight = getResources().getDrawable(
//						R.drawable.cp_folder_list_open);
//				mFolderSpinner.setCompoundDrawables(null, null, drawableRight,
//						null);
//				SpinnerPopWindow popUpWindow = new SpinnerPopWindow(
//						ChatPlaneImageSelectorActivity.this);
//				FolederSpinnerAdapter spinnerAdapter = new FolederSpinnerAdapter(
//						ChatPlaneImageSelectorActivity.this, mFoldersList,
//						mSelectedIndex);
//				popUpWindow.setAdapter(mFolderSpinner, spinnerAdapter);
//				popUpWindow
//						.setPopItemClickListener(new IOnPopItemClickListener() {
//
//							@Override
//							public void onPopItemClick(int position) {
//								LogUtils.d("ZH", "onPopItemClick");
//								String name = mFoldersList.get(position)
//										.getName();
//								name = name == null ? "" : name;
//								mFolderSpinner.setText(name);
//								mPickCustom.refreshGridView(position);
//								mSelectedIndex = position;
//								// 选择后合上
//								mTriangle
//										.setImageResource(R.drawable.cp_folder_list_unopen);
//								mIsOpen = true;
//							}
//						});
//				popUpWindow
//						.setTriangleDismissListener(ChatPlaneImageSelectorActivity.this);
//				if (mIsOpen) {
//					// 打开状态
//					LogUtils.d("cp_folder_list_open->" + mIsOpen);
//					mTriangle.setImageResource(R.drawable.cp_folder_list_open);
//					mIsOpen = false;
//				}
//			}
//		}
//
//	}
//
//	@Override
//	public void selectedImage(String path) {
//		Intent intent = new Intent();
//		Bundle bundle = new Bundle();
//		bundle.putString("path", path);
//
//		intent.putExtras(bundle);
//		setResult(SELECTED_OK, intent);
//
//		// 保存缩放后的图片，通过 BitmapUtils.getScaleSdPath()获取
//		int width = getResources().getDimensionPixelOffset(
//				R.dimen.chatplane_edit_image_width);
//		int height = getResources().getDimensionPixelOffset(
//				R.dimen.chatplane_edit_image_height);
//		Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromSd(path, width,
//				height);
//		BitmapUtils.savdBitmapToSd(this, bitmap, BitmapUtils.getName(path));
//
//		finish();
//		AsyncImageManager.getInstance(getApplicationContext()).clear(ImageFolderGridViewAdapter.IMAGE_LOADER_TAG);
//	}
//
//	@Override
//	public void onPopDismiss() {
//		if (mTriangle == null) {
//			return;
//		}
//		mTriangle.setImageResource(R.drawable.cp_folder_list_unopen);
//		mIsOpen = true;
//	}
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		switch (requestCode) {
//		case CAMEARE_OK:
//			String sdStatus = Environment.getExternalStorageState();
//			if (sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
//				File file = mPickCustom.getCameraPicture();
//				if (file != null && file.exists() && resultCode == RESULT_OK) {
//					selectedImage(file.getAbsolutePath());
//				}
//			}
//			break;
//		case Activity.RESULT_OK:// 照相完成点击确定
//			/*
//			 * String sdStatus = Environment.getExternalStorageState(); if
//			 * (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
//			 * LogUtils.v("SD card is not avaiable/writeable right now.");
//			 * return; } if (data == null) { return; } Bundle bundle =
//			 * data.getExtras(); Bitmap bitmap = (Bitmap) bundle.get("data");//
//			 * 获取相机返回的数据，并转换为Bitmap图片格式 FileOutputStream b = null; File file =
//			 * new File("/sdcard/pk4fun/"); file.mkdirs();// 创建文件夹，名称为pk4fun //
//			 * 照片的命名
//			 * ，目标文件夹下，以当前时间数字串为名称，即可确保每张照片名称不相同。网上流传的其他Demo这里的照片名称都写死了，则会发生无论拍照多少张
//			 * ，
//			 * 后一张总会把前一张照片覆盖。细心的同学还可以设置这个字符串，比如加上“ＩＭＧ”字样等；然后就会发现sd卡中myimage这个文件夹下
//			 * ，会保存刚刚调用相机拍出来的照片，照片名称不会重复。 String str = null; Date date = null;
//			 * SimpleDateFormat format = new
//			 * SimpleDateFormat("yyyyMMddHHmmss");// 获取当前时间，进一步转化为字符串 date = new
//			 * Date(resultCode); str = format.format(date); String fileName =
//			 * "/sdcard/myImage/" + str + ".jpg"; sendBroadcast(fileName); try {
//			 * b = new FileOutputStream(fileName);
//			 * bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件 }
//			 * catch (FileNotFoundException e) { e.printStackTrace(); } finally
//			 * { try { b.flush(); b.close(); } catch (IOException e) {
//			 * e.printStackTrace(); } }
//			 */
//			break;
//		case CAMEARE_SELECT_OK:
//			break;
//		}
//	}
//	/*
//	 * ImagePickCustom.show(getApplicationContext(), new
//	 * ImagePickCustom.ImagePickSelectListener() {
//	 *
//	 * @Override public void selectedImage(String path) { // TODO Auto-generated
//	 * method stub
//	 *
//	 * } });
//	 */
//
//}
