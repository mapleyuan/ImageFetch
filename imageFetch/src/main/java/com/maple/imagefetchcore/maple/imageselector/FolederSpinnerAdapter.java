package com.maple.imagefetchcore.maple.imageselector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maple.imagefetchcore.R;
import com.maple.imagefetchcore.maple.imageselector.pojo.FolderUnit;

import java.util.List;


/**
 * 
 * @author zhouhong
 *
 */
public class FolederSpinnerAdapter extends BaseAdapter {
	public List<FolderUnit> mNameFolders;
	private Context mContext;
	private int mSelectedIndex;

	public FolederSpinnerAdapter(Context context, List<FolderUnit> folders, int selectedPos) {
		if (folders == null || context == null) {
			return;
		}
		this.mNameFolders = folders;
		mContext = context; 
		mSelectedIndex = selectedPos;
	}
	
	
	@Override
	public int getCount() {
		return mNameFolders.size();
	}

	@Override
	public Object getItem(int position) {
		return mNameFolders.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.cp_folder_spinner_item, null);
			viewHolder = new ViewHolder();
			viewHolder.mTextView = (TextView) convertView.findViewById(R.id.folder_name); 
			viewHolder.mSelectedImage = (ImageView) convertView.findViewById(R.id.folder_selected);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.mTextView.setText(mNameFolders.get(position).getName());
		if (position == mSelectedIndex) {
			viewHolder.mSelectedImage.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mSelectedImage.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	/**
	 * 
	 * @author zhouhong
	 *
	 */
	public class ViewHolder {
		public TextView mTextView;
		public ImageView mSelectedImage;
	}

}
