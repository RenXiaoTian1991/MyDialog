package com.mydialog.engine;

public interface DownLoadListener {

	public abstract void onDownLoadStart(int fileMax);

	public abstract void onDownloading(int progress);

	public abstract void onDownLoadSuccess();

	public abstract void onDownLoadError();

}
