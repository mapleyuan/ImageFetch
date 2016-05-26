package com.maple.imagefetchcore.maple.imageselector;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.maple.imagefetchcore.R;
import com.maple.imagefetchcore.common.Constants;
import com.maple.imagefetchcore.imagemanager.AsyncImageManager;
import com.maple.imagefetchcore.maple.imageselector.pojo.FolderUnit;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 图片选择器
 * 
 * @author yuanweinan
 * @date 14-12-19
 */
public class ImagePickCustom extends RelativeLayout {
	public static final String CAMERA_FOLDER = "Camera";

	private static WindowManager sWindowManager;
	private LayoutParams mParams;
	private static ImagePickCustom sInstance;
	private Context mContext;
	// private static View sView;

	private GridView mGridView;
	private ImageFolderGridViewAdapter mCurGridViewAdapter;
	// private ListView mListView;
	/*
	 * private View mBackView; private View mQuitView; private View mOkView;
	 */
	private ImagePickSelectListener mImageSelectedListener;
	private ImageScanListener mImageScanListener;
	/**
	 * 扫描拿到所有的图片文件夹
	 */
	private List<FolderUnit> mImageFloders = new ArrayList<FolderUnit>();
	private HashSet<String> mDirPaths = new HashSet<String>();
	// 所有图片总量
	// private int mTotalCount = 0;

	private int mSelectedPos = -1;

	/*
	 * private Handler mHandler = new Handler() { public void
	 * handleMessage(android.os.Message msg) {
	 * 
	 * mListView.setAdapter(new ImageFolderListAdapter(mImageFloders,
	 * mContext)); mListView.setOnItemClickListener(new OnItemClickListener() {
	 * 
	 * @Override public void onItemClick(AdapterView<?> arg0, View arg1, int
	 * arg2, long arg3) { mBackView.setVisibility(View.VISIBLE);
	 * mGridView.setVisibility(View.VISIBLE);
	 * mOkView.setVisibility(View.VISIBLE); mListView.setVisibility(View.GONE);
	 * mQuitView.setVisibility(View.GONE); mCurGridViewAdapter = new
	 * ImageFolderGridViewAdapter(mContext, mImageFloders.get(arg2));
	 * mGridView.setAdapter(mCurGridViewAdapter);
	 * mGridView.setOnItemClickListener(new OnItemClickListener() {
	 * 
	 * @Override public void onItemClick(AdapterView<?> arg0, View arg1, int
	 * arg2, long arg3) { mCurGridViewAdapter.setItemIsSelected(mSelectedPos,
	 * false); mSelectedPos = arg2;
	 * mCurGridViewAdapter.setItemIsSelected(mSelectedPos, true);
	 * ToastCustom.makeText(mContext, "selected:" + arg2,
	 * Toast.LENGTH_LONG).show(); } }); } }); } };
	 */

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mImageFloders == null || mImageFloders.size() <= 0) { // 没有查找到图片
				mCurGridViewAdapter = new ImageFolderGridViewAdapter(mContext,
						null);
				mGridView.setAdapter(mCurGridViewAdapter);
				return;
			}
			if (mImageScanListener != null) {
				mImageScanListener.onScanFinished(mImageFloders);
			}

			mGridView.setVisibility(View.VISIBLE);
			mCurGridViewAdapter = new ImageFolderGridViewAdapter(mContext,
					mImageFloders.get(0));
			mGridView.setAdapter(mCurGridViewAdapter);
			mGridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					mCurGridViewAdapter.setItemIsSelected(mSelectedPos, false);
					mSelectedPos = position;
					mCurGridViewAdapter.setItemIsSelected(mSelectedPos, true);
					/*
					 * ToastCustom.makeText(mContext, "selected:" + arg2,
					 * Toast.LENGTH_LONG).show();
					 */
				}
			});

		}

	};

	public ImagePickCustom(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public ImagePickCustom(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// mListView = (ListView) findViewById(R.id.imagepick_listview);
		/*
		 * mQuitView = findViewById(R.id.quit); mBackView =
		 * findViewById(R.id.back); mOkView = findViewById(R.id.ok);
		 */
	}

	private void initView() {
		// mGridView = (GridView) findViewById(R.id.imagepick_gridView);
		// android:listSelector="@drawable/listitem_selector"
		// android:stretchMode="columnWidth"

		mGridView = new GridView(getContext());

		mGridView.setCacheColorHint(Color.TRANSPARENT);
		mGridView.setClipChildren(true);
		mGridView.setDrawSelectorOnTop(true);
		mGridView.setNumColumns(3);
		int spacing = getContext().getResources().getDimensionPixelOffset(
				R.dimen.chatplane_imageselector_grid_spacing);
		mGridView.setHorizontalSpacing(spacing);
		mGridView.setVerticalSpacing(spacing);
		mGridView.setBackgroundColor(Color.WHITE);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		addView(mGridView, params);

		mGridView.setOnScrollListener(new ScrollerListener());

	}

	public File getCameraPicture() {
		if (mCurGridViewAdapter == null) {
			return null;
		}
		return mCurGridViewAdapter.mCameraPicture;
	}

	public void refreshGridView(int position) {
		if (mGridView != null && position < mImageFloders.size()) {
			mCurGridViewAdapter.refreshData(mImageFloders.get(position));
			mGridView.smoothScrollToPositionFromTop(0, 0, 300);
		}
	}

	/*
	 * private ImagePickCustom(Context context) { mContext = context;
	 * sWindowManager = (WindowManager) context
	 * .getSystemService(Context.WINDOW_SERVICE); mParams = new LayoutParams();
	 * mParams.type = LayoutParams.TYPE_SYSTEM_ALERT; int flags =
	 * LayoutParams.FLAG_ALT_FOCUSABLE_IM; mParams.flags = flags; mParams.format
	 * = PixelFormat.TRANSLUCENT; mParams.width = LayoutParams.MATCH_PARENT;
	 * mParams.height = LayoutParams.MATCH_PARENT; mParams.gravity =
	 * Gravity.CENTER;
	 * 
	 * sView = LayoutInflater.from(context).inflate( R.layout.cp_imagefolder_view,
	 * null); mGridView = (GridView)
	 * sView.findViewById(R.id.imagepick_gridView); mListView = (ListView)
	 * sView.findViewById(R.id.imagepick_listview); mQuitView =
	 * sView.findViewById(R.id.quit); mBackView = sView.findViewById(R.id.back);
	 * mOkView = sView.findViewById(R.id.ok);
	 * 
	 * mQuitView.setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { // TODO Auto-generated method
	 * stub if (mSelectedPos < 0 || mGridView == null) { } else {
	 * mCurGridViewAdapter.setItemIsSelected(mSelectedPos, false); mSelectedPos
	 * = -1; } dismiss(); } });
	 * 
	 * mBackView.setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { // TODO Auto-generated method
	 * stub mListView.setVisibility(View.VISIBLE);
	 * mGridView.setVisibility(View.GONE);
	 * mQuitView.setVisibility(View.VISIBLE);
	 * mBackView.setVisibility(View.GONE); mOkView.setVisibility(View.GONE); if
	 * (mSelectedPos < 0 || mGridView == null) { return; }
	 * mCurGridViewAdapter.setItemIsSelected(mSelectedPos, false); mSelectedPos
	 * = -1; } });
	 */

	/**
	 * ok按钮点击世事件
	 */
	public void onSelectBtnClickListener() {
		if (mSelectedPos < 0 || mGridView == null) {
			return;
		}

		String path = ((ImageFolderGridViewAdapter) mGridView.getAdapter())
				.getImagePath(mSelectedPos);
		if (mImageSelectedListener != null) {
			mImageSelectedListener.selectedImage(path);
		}
		mCurGridViewAdapter.setItemIsSelected(mSelectedPos, false);
		mSelectedPos = -1;
	}

	public void show(Context context, ImageScanListener scanListener) {
		// sInstance = new ImagePickCustom(context);
		scanImageData();
		// mImageSelectedListener = listener;
		mImageScanListener = scanListener;
	}

	public void setImageSelectedListener(
			ImagePickSelectListener imgSelectedtedListener) {
		this.mImageSelectedListener = imgSelectedtedListener;
	}

	/*
	 * public static void dismiss() { if (sWindowManager == null) { return; }
	 * sWindowManager.removeView(sView); sView = null; sWindowManager = null;
	 * sInstance = null; }
	 */
	/**
	 * 扫描sdcard 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
	 */
	private void scanImageData() {

		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.i("maple", "暂无外部存储");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {

				String firstImage = null;
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = mContext
						.getContentResolver();

				// 只查询jpeg和png的图片
//				Cursor mCursor = mContentResolver.query(mImageUri, null,
//						MediaStore.Images.Media.MIME_TYPE + "=? or "
//								+ MediaStore.Images.Media.MIME_TYPE + "=?" + " or "
//								+ MediaStore.Images.Media.MIME_TYPE + "=?",
//						new String[]{"image/jpeg", "image/png", "image/jpg"},
//						MediaStore.Images.Media.DATE_MODIFIED);

//				while (mCursor.moveToNext()) {
					// 获取图片的路径
//					String path = mCursor.getString(mCursor
//							.getColumnIndex(MediaStore.Images.Media.DATA));

					// 拿到第一张图片的路径
//					if (firstImage == null) {
//						firstImage = path;
//					}
//					// 获取该图片的父路径名
//					File parentFile = new File(path).getParentFile();
//					if (parentFile == null) {
//						continue;
//					}
//					String dirPath = parentFile.getAbsolutePath();
					FolderUnit imageFloder = null;
//					// 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
//					if (mDirPaths.contains(dirPath)) {
//						continue;
//					} else {
//						mDirPaths.add(dirPath);
//						// 初始化imageFloder
//						imageFloder = new FolderUnit(dirPath, path);
//					}

					imageFloder = new FolderUnit(Constants.IMAGE_PATH);
					File parentFile = new File(Constants.IMAGE_PATH);
					String[] temp = parentFile.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".jpg")
									|| filename.endsWith(".png")
									|| filename.endsWith(".jpeg")) {
								return true;
							}
							return false;
						}
					});
					// 避免空指针
					if (temp == null) {
						return;
					}
					int picSize = temp.length;
					// mTotalCount += picSize;

					imageFloder.mCount = picSize;
					if (imageFloder.mFolderDir.equals(CAMERA_FOLDER)) {
						mImageFloders.add(0, imageFloder);
					} else {
						mImageFloders.add(imageFloder);
					}

//				}
//				mCursor.close();

				// 扫描完成，辅助的HashSet也就可以释放内存了
//				mDirPaths = null;

				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(0x110);

			}
		}).start();
	}

	/**
	 * 图片选择结果监听器
	 * 
	 * @author yuanweinan
	 */
	public interface ImagePickSelectListener {
		public void selectedImage(String path);
	}

	/**
	 * 图片扫描完成回调
	 */
	public interface ImageScanListener {
		public void onScanFinished(List<FolderUnit> folders);
	}

	/**
	 * 滑动时禁止加载图片
	 * 
	 * @author huangziwei
	 * 
	 */
	private class ScrollerListener implements OnScrollListener {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				onIdleListener(view);
				break;
			case OnScrollListener.SCROLL_STATE_FLING:
				onFlingListener(view);
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				onTouchScrollListener(view);
				break;
			default:
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}

		public void onTouchScrollListener(AbsListView view) {
			// 列表在滚动，图片控制器加锁
			AsyncImageManager.getInstance(mContext).lock();
		}

		public void onIdleListener(AbsListView view) {
			// 列表停止滚动时
			// 找出列表可见的第一项和最后一项
			int start = view.getFirstVisiblePosition();
			int end = view.getLastVisiblePosition();
			if (end >= view.getCount()) {
				end = view.getCount() - 1;
			}
			// 对图片控制器进行位置限制设置
			AsyncImageManager.getInstance(mContext)
					.setLimitPosition(start, end);
			// 然后解锁通知加载
			AsyncImageManager.getInstance(mContext).unlock();
		}

		public void onFlingListener(AbsListView view) {
			// 列表在滚动，图片控制器加锁
			AsyncImageManager.getInstance(mContext).lock();
		}
	}
}
