package com.maple.imagefetchcore.db;

/**
 * @author yuanweinan
 *
 */
public class DatabaseException extends Exception {

	private static final long serialVersionUID = 4487703496534256890L;

	public DatabaseException(Exception e) {
		super(e);
	}
}
