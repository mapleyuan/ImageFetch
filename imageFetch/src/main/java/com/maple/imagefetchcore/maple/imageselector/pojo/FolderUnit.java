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
	private String mName;

	public FolderUnit(String folderDir, String firstImagePath, int count) {
		mFolderDir = folderDir;
		mFirstImagePath = firstImagePath;
		mCount = count;
	}

	public FolderUnit(String folderDir, String firstImagePath) {
		mFolderDir = folderDir;
		mFirstImagePath = firstImagePath;
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
