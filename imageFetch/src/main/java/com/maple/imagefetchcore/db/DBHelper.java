package com.maple.imagefetchcore.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.maple.imagefetchcore.utils.LogUtil;

import java.util.ArrayList;

/**
 * @author yuanweinan
 *
 */
public abstract class DBHelper extends SQLiteOpenHelper {
	private static final String LOG_TAG = DBHelper.class.getSimpleName();
	private boolean mIsNewDB = false;
	SQLiteQueryBuilder msqlQB = null;
	private final Context mContext;
	private boolean mUpdateResult = true;

	public DBHelper(Context context, String dataBaseName, int dataBaseVersion) {
		super(context, dataBaseName, null, dataBaseVersion);
		this.mContext = context;
		this.msqlQB = new SQLiteQueryBuilder();
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (!this.mUpdateResult) {
				if (db != null) {
					db.close();
				}
				this.mContext.deleteDatabase(getDbName());
			}
		} catch (SQLiteException ex) {
			this.mContext.deleteDatabase(getDbName());
		} catch (IllegalStateException localIllegalStateException) {
		}
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		this.mUpdateResult = true;
		this.mIsNewDB = true;
		db.beginTransaction();
		try {
			onCreateTables(db);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	private boolean isExistColumnInTable(SQLiteDatabase db, String tableName,
			String columnName) {
		boolean result = false;
		Cursor cursor = null;
		try {
			String[] columns = { columnName };
			cursor = db.query(tableName, columns, null, null, null, null, null);
			if ((cursor != null) && (cursor.getColumnIndex(columnName) >= 0)) {
				result = true;
			}
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "isExistColumnInTable has exception");
			result = false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	public void addColumnToTable(SQLiteDatabase db, String tableName,
			String columnName, String columnType, String defaultValue) {
		if (!isExistColumnInTable(db, tableName, columnName)) {
			db.beginTransaction();
			try {
				String updateSql = "ALTER TABLE " + tableName + " ADD "
						+ columnName + " " + columnType;
				db.execSQL(updateSql);
				if (defaultValue != null) {
					if (columnType.equals("text")) {
						defaultValue = "'" + defaultValue + "'";
					}
					updateSql = "update " + tableName + " set " + columnName
							+ " = " + defaultValue;
					db.execSQL(updateSql);
				}
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
			}
		}
	}
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogUtil.d(LOG_TAG, "onDowngrade oldVersion=" + oldVersion
				+ ", newVersion=" + newVersion);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ((oldVersion < 1) || (oldVersion > newVersion)
				|| (newVersion > getDbCurrentVersion())) {
			LogUtil.d(LOG_TAG, "onUpgrade() false oldVersion = " + oldVersion
					+ ", newVersion = " + newVersion);
			return;
		}
		ArrayList<UpgradeDB> upgradeDBFuncS = new ArrayList<UpgradeDB>();
		onAddUpgrades(upgradeDBFuncS);
		LogUtil.d(LOG_TAG, "onupgrade");
		for (int i = oldVersion - 1; i < newVersion - 1; i++) {
			this.mUpdateResult = ((UpgradeDB) upgradeDBFuncS.get(i))
					.onUpgradeDB(db);
			if (!this.mUpdateResult) {
				break;
			}
		}
		upgradeDBFuncS.clear();
	}

	public void execSql(String sql) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Cursor rawQuery(String sql, String[] selectionArgs) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			return db.rawQuery(sql, selectionArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Cursor query(String tableName, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		return query(tableName, projection, selection, selectionArgs, null,
				null, sortOrder);
	}

	public Cursor query(String tableName, String[] projection,
			String selection, String[] selectionArgs, String groupBy,
			String having, String sortOrder) {
		Cursor result = null;
		try {
			SQLiteDatabase db = getReadableDatabase();
			result = db.query(tableName, projection, selection, selectionArgs,
					groupBy, having, sortOrder);
		} catch (SQLException e) {
			e.printStackTrace();
			LogUtil.e(LOG_TAG, "SQLException when query in " + tableName + ", "
					+ selection);
			LogUtil.e(LOG_TAG, e.getMessage());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			LogUtil.e(LOG_TAG, "IllegalStateException when query in "
					+ tableName + ", " + selection);
			LogUtil.e(LOG_TAG, e.getMessage());
		}
		return result;
	}

	public Cursor queryCrossTables(String tableName, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		synchronized (this.msqlQB) {
			this.msqlQB.setTables(tableName);
			try {
				SQLiteDatabase db = getReadableDatabase();
				result = this.msqlQB.query(db, projection, selection,
						selectionArgs, null, null, sortOrder);
			} catch (SQLException e) {
				e.printStackTrace();
				LogUtil.e(LOG_TAG, "SQLException when query in " + tableName
						+ ", " + selection);
				LogUtil.e(LOG_TAG, e.getMessage());
			} catch (IllegalStateException e) {
				e.printStackTrace();
				LogUtil.e(LOG_TAG, "IllegalStateException when query in "
						+ tableName + ", " + selection);
				LogUtil.e(LOG_TAG, e.getMessage());
			}
		}
		return result;
	}

	public long insert(String tableName, ContentValues initialValues)
			throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		long rowId = 0L;
		try {
			rowId = db.insert(tableName, null, initialValues);
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "Exception when insert in " + tableName);
			LogUtil.e(LOG_TAG, e.getMessage());
			throw new DatabaseException(e);
		}
		return rowId;
	}

	public int delete(String tableName, String selection, String[] selectionArgs)
			throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		int count = 0;
		try {
			count = db.delete(tableName, selection, selectionArgs);
		} catch (Exception e) {
			LogUtil.e(LOG_TAG, "Exception when delete in " + tableName + ", "
					+ selection);
			LogUtil.e(LOG_TAG, e.getMessage());
			throw new DatabaseException(e);
		}
		return count;
	}

	public int update(String tableName, ContentValues values, String selection,
			String[] selectionArgs) throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		int count = 0;
		try {
			count = db.update(tableName, values, selection, selectionArgs);
		} catch (Exception e) {
			LogUtil.e("data", "Exception when update in " + tableName + ", "
					+ selection);
			LogUtil.e(LOG_TAG, e.getMessage());
			throw new DatabaseException(e);
		}
		return count;
	}

	public void exec(String sql) throws DatabaseException {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL(sql);
		} catch (SQLException e) {
			LogUtil.e(LOG_TAG, "Exception when exec " + sql);
			LogUtil.e(LOG_TAG, e.getMessage());
			throw new DatabaseException(e);
		}
	}

	public boolean isNewDB() {
		return this.mIsNewDB;
	}

	public boolean openDBWithWorldReadable() {
		try {
			close();
			if (this.mContext.openOrCreateDatabase(getDbName(), 1, null) == null) {
				return false;
			}
		} catch (Exception e) {
			close();
			return false;
		}
		return true;
	}

	public void beginTransaction() {
		try {
			SQLiteDatabase db = getWritableDatabase();
			if (db != null) {
				db.beginTransaction();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTransactionSuccessful() {
		try {
			SQLiteDatabase db = getWritableDatabase();
			if (db != null) {
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void endTransaction() {
		try {
			SQLiteDatabase db = getWritableDatabase();
			db.endTransaction();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract int getDbCurrentVersion();

	public abstract String getDbName();

	public abstract void onCreateTables(SQLiteDatabase paramSQLiteDatabase);

	public abstract void onAddUpgrades(ArrayList<UpgradeDB> paramArrayList);

	/**
	 * @author yuanweinan
	 *
	 */
	public abstract class UpgradeDB {
		public UpgradeDB() {
		}

		public abstract boolean onUpgradeDB(SQLiteDatabase paramSQLiteDatabase);
	}
}
