package com.maple.imagefetchcore.common;

import android.os.Environment;

/**
 * Created by yuanweinan on 16/4/29.
 */
public class Constants {

    public static final String PARENT_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/imagefetch/";
    public static final String IMAGE_PATH = PARENT_FILE_PATH + "image";

    public final static int LOAD_URL_RESULT_INVALID_URL = 0x0001;
    public final static int LOAD_URL_RESULT_PARSE_FAIL = 0x0002;
}
