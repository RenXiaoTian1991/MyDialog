package com.mydialog;

import java.text.NumberFormat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mydialog.engine.DownLoadService;
import com.mydialog.util.Util;

public class MainActivity extends Activity {

	private LayoutInflater mLayoutInflater;
	private Dialog mDialog;
	private TextView title;
	private Button positive;
	private Button negative;
	private LinearLayout info_ll;
	private LinearLayout progress_ll;
	private Button mShowDialogButton;
	private ProgressBar mDownloadProgressBar;
	private TextView mDownloadPercentage;
	private TextView mDownloadSize;
	private int mFileMax;
	private boolean mIsDownLoadDialog; // 是否是下载对话框

	public static final int MSG_DOWN_ERROR = -1; // 下载失败
	public static final int MSG_SDCARD_NOMOUNTED = 1;// SD卡不可用
	public static final int MSG_DOWN_START = 2;// 开始下载
	public static final int MSG_DOWN_ING = 3;// 下载中
	public static final int MSG_DOWN_BACKGROUND = 4;// 开始后台下载
	public static final int MSG_DOWN_SUCCESS = 5;// 下载完成

	public static Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLayoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mShowDialogButton = (Button) findViewById(R.id.bt);
		mShowDialogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog();
			}

		});

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_DOWN_START:
					mFileMax = (Integer) msg.obj;
					mDownloadProgressBar.setMax(mFileMax);
					break;
				case MSG_DOWN_ING:
					int current = (Integer) msg.obj;
					refreshProgress(current);
					break;
				case MSG_DOWN_BACKGROUND:
					Intent intent = new Intent(DownLoadService.ACTION_NOTIFICATION_SHOW);
					MainActivity.this.startService(intent);
					if (mDialog.isShowing()) {
						mDialog.dismiss();
					}
					break;
				case MSG_DOWN_SUCCESS:
					if (mDialog.isShowing()) {
						mDialog.dismiss();
					}
					Toast.makeText(MainActivity.this, "下载完成。", Toast.LENGTH_SHORT).show();
					break;
				case MSG_SDCARD_NOMOUNTED:
					Toast.makeText(MainActivity.this, "SD卡不可用。", Toast.LENGTH_SHORT).show();
					break;
				case MSG_DOWN_ERROR:

					break;
				}
			}
		};
	}

	private void showDialog() {
		if (MainActivity.this.isFinishing()) {
			return;
		}
		mIsDownLoadDialog = false;
		View view = mLayoutInflater.inflate(R.layout.dialog, null, false);
		mDialog = new Dialog(MainActivity.this, R.style.MyDialog);
		int screenWidth = Util.getWidthPixels(MainActivity.this);
		int screenHeight = Util.getHeightPixels(MainActivity.this);
		mDialog.setContentView(view, new ViewGroup.LayoutParams(screenWidth * 8 / 9, screenHeight * 1 / 3));
		mDialog.setCanceledOnTouchOutside(false);
		String updateMessage = getResources().getString(R.string.updateinfo);
		TextView updateinfo_tv = (TextView) view.findViewById(R.id.updateinfo_tv);
		updateinfo_tv.setText(updateMessage);
		title = (TextView) view.findViewById(R.id.dialog_title);
		info_ll = (LinearLayout) view.findViewById(R.id.info_ll);
		progress_ll = (LinearLayout) view.findViewById(R.id.progress_ll);
		positive = (Button) view.findViewById(R.id.bt_positive);
		negative = (Button) view.findViewById(R.id.bt_negative);
		mDownloadProgressBar = (ProgressBar) view.findViewById(R.id.download_progressbar);
		mDownloadPercentage = (TextView) view.findViewById(R.id.download_percentage);
		mDownloadSize = (TextView) view.findViewById(R.id.download_size);
		positive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsDownLoadDialog) { // 后台下载
					mHandler.sendEmptyMessage(MSG_DOWN_BACKGROUND);
				} else {
					if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
						mIsDownLoadDialog = true;
						title.setText("正在下载...");
						positive.setText("后台下载");
						info_ll.setVisibility(View.GONE);
						progress_ll.setVisibility(View.VISIBLE);
						Intent intent = new Intent(DownLoadService.ACTION_UPDATE_DOWNLOAD_START);
						MainActivity.this.startService(intent);

					} else {
						Message msg = new Message();
						msg.what = MSG_SDCARD_NOMOUNTED;
						mHandler.sendMessage(msg);
					}
				}
			}
		});
		negative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsDownLoadDialog) {
					Intent intent = new Intent(DownLoadService.ACTION_UPDATE_DOWNLOAD_CANCEL);
					MainActivity.this.startService(intent);
					Toast.makeText(MainActivity.this, "下载已取消", Toast.LENGTH_SHORT).show();
				}
				mDialog.dismiss();
			}
		});
		mDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				if (mIsDownLoadDialog) {
					mHandler.sendEmptyMessage(MSG_DOWN_BACKGROUND);
					Toast.makeText(MainActivity.this, "后台正在下载更新,请稍后...", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mDialog.show();
	}

	private void refreshProgress(int current) {
		mDownloadProgressBar.setProgress(current);

		float current_f = current;
		float fileMax_f = mFileMax;
		NumberFormat percentFormat = NumberFormat.getPercentInstance(); // 得到百分比Format
		percentFormat.setMinimumFractionDigits(0);// 保留0位小数
		String percent_str = percentFormat.format(current_f / fileMax_f);
		mDownloadPercentage.setText(percent_str);

		float current_M = current_f / 1024 / 1024; // byte 转成 M
		float fileMax_M = fileMax_f / 1024 / 1024;
		NumberFormat numberFormat = NumberFormat.getNumberInstance(); // 得到数字Format
		numberFormat.setMaximumFractionDigits(1);// 保留1位小数
		String current_str = numberFormat.format(current_M);
		String fileMax_str = numberFormat.format(fileMax_M);
		if (current_str.length() == 1) {
			current_str = current_str + ".0";
		}
		mDownloadSize.setText(current_str + "M/" + fileMax_str + "M");
	}
}
