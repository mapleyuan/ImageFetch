package com.maple.imagefetchcore.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.maple.imagefetchcore.download.impl.DownloadRecordTable;

import java.util.ArrayList;

/**
 * @author yuanweinan
 *
 */
public class FTPDBHelpler extends DBHelper {
	private static final int DB_VERSION_MAX = 1;
	private static final String DATABASE_NAME = "ftpcommerce.db";

	public FTPDBHelpler(Context context) {
		super(context, DATABASE_NAME, DB_VERSION_MAX);
	}
	@Override
	public int getDbCurrentVersion() {
		return DB_VERSION_MAX;
	}
	@Override
	public String getDbName() {
		return DATABASE_NAME;
	}
	@Override
	public void onCreateTables(SQLiteDatabase db) {
		db.execSQL(DownloadRecordTable.CREATETABLESQL);
	}
	@Override
	public void onAddUpgrades(ArrayList<UpgradeDB> upgrades) {
	}

}
