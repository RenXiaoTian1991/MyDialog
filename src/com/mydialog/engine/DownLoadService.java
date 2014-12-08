package com.mydialog.engine;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.mydialog.MainActivity;
import com.mydialog.R;
import com.mydialog.util.DownLoadManager;
import com.mydialog.util.Util;

public class DownLoadService extends Service {
	private Context mContext;
	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;
	private int mProgresMax;
	private static final int sNotificationID = 123;

	public static final String ACTION_UPDATE_DOWNLOAD_START = "action.download.start"; // 开始下载
	public static final String ACTION_UPDATE_DOWNLOAD_CANCEL = "action.download.cancel"; // 取消下载

	public static final String ACTION_NOTIFICATION_SHOW = "action.notification.show"; // 显示notification
	public static final String ACTION_NOTIFICATION_CLICK = "action.notification.click";//notification被点击
	public static final String ACTION_NOTIFICATION_CANCEL = "action.notification.cancel";// notification被清除

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		String action = intent.getAction();
		if (ACTION_UPDATE_DOWNLOAD_START.equals(action)) {
			DownLoadTask downLoadAPKTask = new DownLoadTask();
			new Thread(downLoadAPKTask).start();
		} else if (ACTION_UPDATE_DOWNLOAD_CANCEL.equals(action)) {
			DownLoadManager.getInstance().cancelDownLoad();
			releaseNotification();
		} else if (ACTION_NOTIFICATION_SHOW.equals(action)) {
			showNotification();
		} else if (ACTION_NOTIFICATION_CLICK.equals(action)) {
			releaseNotification();
			stopSelf();
		} else if (ACTION_NOTIFICATION_CANCEL.equals(action)) {
			releaseNotification();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private class DownLoadTask implements Runnable {
		@Override
		public void run() {
			DownLoadManager.getInstance().getFileFromServer(mContext, new MyDownLoadListener(),
					Util.getCachePath(mContext) + "aaa.mp3");
		}
	}

	private class MyDownLoadListener implements DownLoadListener {

		@Override
		public void onDownLoadStart(int fileMax) {
			mProgresMax = fileMax;
			Message startMsg = Message.obtain();
			startMsg.what = MainActivity.MSG_DOWN_START;
			startMsg.obj = fileMax;
			MainActivity.mHandler.sendMessage(startMsg);
		}

		@Override
		public void onDownloading(int progress) {
			Message downloadingMsg = Message.obtain();
			downloadingMsg.what = MainActivity.MSG_DOWN_ING;
			downloadingMsg.obj = progress;
			MainActivity.mHandler.sendMessage(downloadingMsg);
			upDateNotification(progress);
		}

		@Override
		public void onDownLoadSuccess() {
			Message successMsg = Message.obtain();
			successMsg.what = MainActivity.MSG_DOWN_SUCCESS;
			MainActivity.mHandler.sendMessage(successMsg);
			finishNotification();
		}

		@Override
		public void onDownLoadError() {
			stopSelf();
		}

	}

	private void showNotification() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(mContext);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setTicker("正在下载新版本...");
		mBuilder.setContentTitle("正在下载");
		mBuilder.setContentText("请稍等...");
		mBuilder.setProgress(mProgresMax, 0, true);
		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(sNotificationID, notification);
	}

	private void upDateNotification(int progress) {
		if (mNotificationManager == null || mBuilder == null) {
			return;
		}
		float fm = mProgresMax;
		float percentage = progress / fm * 100;
		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setContentTitle("正在下载");
		mBuilder.setContentText("请稍等...");
		mBuilder.setProgress(mProgresMax, progress, false);
		mBuilder.setContentInfo((int) percentage + " %");
		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(sNotificationID, notification);
	}

	private void finishNotification() {
		if (mNotificationManager == null || mBuilder == null) {
			return;
		}
		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setContentTitle("下载完成!");
		mBuilder.setContentText("下载完成");
		Intent clickIntent = new Intent(ACTION_NOTIFICATION_CLICK);
		PendingIntent clickPendingIntent = PendingIntent.getService(mContext, 0, clickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(clickPendingIntent);

		Intent cancelIntent = new Intent(ACTION_NOTIFICATION_CANCEL);
		PendingIntent cancelPendingIntent = PendingIntent.getService(mContext, 0, cancelIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setDeleteIntent(cancelPendingIntent);

		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(sNotificationID, notification);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		releaseNotification();
	}

	@Override
	public void onDestroy() {
		releaseNotification();
		super.onDestroy();
	}

	private void releaseNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(sNotificationID);
		}
	}
}
