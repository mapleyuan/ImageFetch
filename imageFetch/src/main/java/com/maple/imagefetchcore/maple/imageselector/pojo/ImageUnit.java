package com.maple.imagefetchcore.maple.imageselector.pojo;


import com.maple.imagefetchcore.maple.imageselector.util.BroadCaster;

import java.io.File;

/**
 * @author yuanweinan
 *
 */
public class ImageUnit extends BroadCaster {
	public static final int SELECTED_CHANGED = 0x01;
	
	public String mImageName;
	public String mDir;
	public boolean mIsSelected = false;
	public ImageUnit() {

	}
	public ImageUnit(File file) {
		mImageName = file.getName();
		mDir = file.getAbsolutePath();
	}
}
