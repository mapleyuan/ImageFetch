package com.maple.imagefetchcore.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maple.imagefetchcore.R;
import com.maple.imagefetchcore.manager.ImageFetchManager;
import com.maple.imagefetchcore.utils.LogUtil;

/**
 * 登录选择框
 * @author yuanweinan
 *
 */
public class IFSelectDialog {
	private Context mContext;
	private Dialog mDialog;
	private Button mButton;
	private long mLastClickTime;
	private IFetchListener mLoginListener;
	private EditText mTitleEdit;

	/**
	 */
	public static interface IFetchListener {
		/**
		 * 登录成功
		 */
		void onFetchSuccess();
		/**
		 * 登录失败
		 */
		void onFetchFail();
	}


	/**
	 */
	@SuppressLint("NewApi")
	public IFSelectDialog(Context activity) {
		mContext = activity.getApplicationContext();
		mDialog = new Dialog(activity, R.style.if_dialog);
		View view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.cp_tokencoin_login_dialog_view, null);
		view.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mDialog.setContentView(view);
		mButton = (Button) view.findViewById(R.id.login_dialog_ok_id);
		mTitleEdit = (EditText) view.findViewById(R.id.fs_rss_source_title_id);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isFastDoubleClick()) {
					return;
				}
				ImageFetchManager.getInstance(mContext).fetchImage(mTitleEdit.getText().toString(), new ImageFetchManager.IOperationResult() {
					@Override
					public void onSuccess(Object... objects) {
						if (mLoginListener != null) {
							mLoginListener.onFetchSuccess();
						}
					}

					@Override
					public void onFail(int reason) {
						LogUtil.i("解析出错reason:" + reason);
						Toast.makeText(mContext, "出错啦,请重试!", Toast.LENGTH_LONG).show();
					}
				});
				mDialog.dismiss();

			}
		});
	}

	public void show(IFetchListener listener) {
		if (mDialog != null) {
			mLoginListener = listener;
			mDialog.show();
		}
	}
	
	/**
	 * 快速重复点击
	 * @return
	 */
	private boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - mLastClickTime;
		if (0 < timeD && timeD < 800) {
			return true;
		}
		mLastClickTime = time;
		return false;
	}
}
