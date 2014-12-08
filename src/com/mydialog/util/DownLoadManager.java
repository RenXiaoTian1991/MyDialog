package com.mydialog.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.mydialog.engine.DownLoadListener;

public class DownLoadManager {
	private static DownLoadManager mDownLoadManager;
	private static final int sNotifySize = 16 * 1024; // 每下载16KB通知进度条更新。
	private int notifyCurrent = 0;
	private boolean mIsCancelDownLoad;
	private boolean mIsDownLoadError;

	private DownLoadManager() {
	}

	public static synchronized DownLoadManager getInstance() {
		if (mDownLoadManager == null) {
			mDownLoadManager = new DownLoadManager();
		}
		return mDownLoadManager;
	}

	public void cancelDownLoad() {
		mIsCancelDownLoad = true;
	}

	/**
	 * 模拟下载
	 */
	public void getFileFromServer(Context context, DownLoadListener downLoadListener, String filepath) {
		InputStream is = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			is = context.getAssets().open("aaa.mp3");
			bis = new BufferedInputStream(is);
			file = new File(filepath);
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len;
			int total = 0;
			int fileMax = bis.available();
			downLoadListener.onDownLoadStart(fileMax);

			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				total += len;
				notifyCurrent += len;
				if (notifyCurrent > sNotifySize) {
					notifyCurrent = 0;
					downLoadListener.onDownloading(total);
				}
				if (mIsCancelDownLoad) {
					break;
				}
				Thread.currentThread().sleep(2);
			}
			downLoadListener.onDownloading(total);
		} catch (Exception e) {
			mIsDownLoadError = true;
			downLoadListener.onDownLoadError();
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (mIsCancelDownLoad || mIsDownLoadError) {
				deleteFailFile(file);
			} else if (!mIsCancelDownLoad && !mIsDownLoadError) {
				downLoadListener.onDownLoadSuccess();
			}
			mIsCancelDownLoad = false;
			mIsDownLoadError = false;
		}
	}

	public void deleteFailFile(File file) {
		if (file != null && file.exists()) {
			file.delete();
		}
	}
}
