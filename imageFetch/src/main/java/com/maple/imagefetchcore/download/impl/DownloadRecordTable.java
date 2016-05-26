package com.maple.imagefetchcore.download.impl;

/**
 * @author yuanweinan
 *
 */
public class DownloadRecordTable {
	public final static String TABLENAME = "downloadinfo";

	public final static String URL = "url";

	public final static String FILEPATH = "file_path";

	public final static String STARTPOS = "start_pos";
	
	public final static String STATUS = "status";

	public final static String FINISHTIME = "finish_time";
	
	public final static String CREATETABLESQL = "create table " + TABLENAME + " ("
			+ URL + " text, " + FILEPATH + " text, " + STARTPOS + " text," + STATUS + " text," + FINISHTIME + " text" + ")";
}
