package com.maple.imagefetchcore.download.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.maple.imagefetchcore.db.DBHelper;
import com.maple.imagefetchcore.db.DatabaseException;
import com.maple.imagefetchcore.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuanweinan
 *
 */
public class DownloadRecordData {

	private static final String LOG_TAG = "downloadrecordData";
	/**
	 * 下载地址
	 */
	public String mUrl;
	/**
	 * 文件地址
	 */
	public String mFilePath;
	// 开始写入位置
	public long mStartPos;
	// 状态:0,未完成,1完成
	public int mStatus = 0;

	/**
	 * 下载完成时间
	 */
	public long mFinishTime;

	public DownloadRecordData(String url, String filePath, long startPos,
			int status, long finishTime) {
		mUrl = url;
		mFilePath = filePath;
		mStartPos = startPos;
		mStatus = status;
		mFinishTime = finishTime;
	}

	public ContentValues getContentValue() {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownloadRecordTable.URL, this.mUrl);
		contentValues.put(DownloadRecordTable.FILEPATH, this.mFilePath);
		contentValues.put(DownloadRecordTable.STARTPOS,
				Long.valueOf(this.mStartPos));
		contentValues.put(DownloadRecordTable.STATUS, Integer.valueOf(mStatus));
		contentValues.put(DownloadRecordTable.FINISHTIME, mFinishTime);
		return contentValues;
	}

	public static List<DownloadRecordData> queryAll(DBHelper dbHelper) {
		List<DownloadRecordData> datas = new ArrayList<DownloadRecordData>();
		String[] columns = { DownloadRecordTable.URL,
				DownloadRecordTable.FILEPATH, DownloadRecordTable.STARTPOS,
				DownloadRecordTable.STATUS,
				DownloadRecordTable.FINISHTIME };
		Cursor cursor = dbHelper.query(DownloadRecordTable.TABLENAME, columns,
				null, null, null);
		if (cursor == null) {
			return datas;
		}
		try {
			if (cursor.moveToFirst()) {
				do {
					DownloadRecordData data = new DownloadRecordData(
							cursor.getString(0), cursor.getString(1),
							cursor.getLong(2), cursor.getInt(3),
							cursor.getLong(4));
					datas.add(data);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return datas;
	}

	public static DownloadRecordData query(DBHelper dbHelper, String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		String[] columns = { DownloadRecordTable.URL,
				DownloadRecordTable.FILEPATH, DownloadRecordTable.STARTPOS,
				DownloadRecordTable.STATUS,
				DownloadRecordTable.FINISHTIME };
		Cursor cursor = dbHelper.query(DownloadRecordTable.TABLENAME, columns,
				DownloadRecordTable.URL + "=\"" + url + "=\"",
				null, null);
		if (cursor == null) {
			return null;
		}
		DownloadRecordData downloadData = null;
		try {
			if (cursor.moveToFirst()) {
				downloadData = new DownloadRecordData(cursor.getString(0),
						cursor.getString(1), cursor.getLong(2),
						cursor.getInt(3), cursor.getLong(4));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return downloadData;
	}

	public static void insert(DBHelper dbHelper, DownloadRecordData data) {
		if (data == null) {
			return;
		}
		try {
			dbHelper.insert(DownloadRecordTable.TABLENAME,
					data.getContentValue());
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public static void update(DBHelper dbHelper, DownloadRecordData data) {
		if (data == null) {
			return;
		}
		LogUtil.d(LOG_TAG, "update||" + data.toString());
		ContentValues contentValues = new ContentValues();
		contentValues.put(DownloadRecordTable.FILEPATH, data.mFilePath);
		contentValues.put(DownloadRecordTable.STARTPOS,
				Long.valueOf(data.mStartPos));
		contentValues.put(DownloadRecordTable.STATUS,
				Integer.valueOf(data.mStatus));
		contentValues.put(DownloadRecordTable.FINISHTIME, data.mFinishTime);
		try {
			dbHelper.update(DownloadRecordTable.TABLENAME, contentValues,
					DownloadRecordTable.URL + "=\"" + data.mUrl + "\"", null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public static void delete(DBHelper dbHelper, String url) {
		if (url == null) {
			return;
		}
		LogUtil.d(LOG_TAG, "delete url=" + url);
		try {
			dbHelper.delete(DownloadRecordTable.TABLENAME,
					DownloadRecordTable.URL + "=\"" + url + "\"", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
