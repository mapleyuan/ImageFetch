package com.maple.imagefetchcore.maple.imageselector.pojo;

import java.io.File;

/**
 * @author yuanweinan
 * 
 */
public class FolderUnit {

	public String mFolderDir;
	public String mFirstImagePath;
	public int mCount;
	public boolean mIsFile;
	private String mName;
	public String mImageName;
	public String mDir;


	public FolderUnit(File file) {
		if (file.isDirectory()) {
			mIsFile = true;
			mImageName = file.getName();
			mDir = file.getAbsolutePath();
			return;
		}
		mIsFile = false;
		File[] files = file.listFiles();
		if (files == null) {
			return;
		}
		mFirstImagePath = files[0].getAbsolutePath();
		mFolderDir = file.getAbsolutePath();
	}

	public FolderUnit(String folderDir) {
		File f = new File(folderDir);
		File[] files = f.listFiles();
		mFirstImagePath = files[0].getAbsolutePath();
		mFolderDir = folderDir;
	}

	/**
	 * @return
	 */
	public String getName() {
		if (mName == null || mName.length() <= 0) {
			int lastIndexOf = mFolderDir.lastIndexOf("/");
			mName = mFolderDir.substring(lastIndexOf + 1);
		}
		return mName;
	}
}
