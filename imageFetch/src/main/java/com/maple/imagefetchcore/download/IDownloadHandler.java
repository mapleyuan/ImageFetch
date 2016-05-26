package com.maple.imagefetchcore.download;

/**
 * @author yuanweinan
 *
 */
public  interface IDownloadHandler {
	public  void handleRequest();

	public  void stop();

	public  void pause();
}
