package com.mydialog.util;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;

public class Util {

	public static String getCachePath(Context context){
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ){
			File file = context.getExternalCacheDir();
			if(file != null){
				return file.getPath(); //cachePath:  sdcard/Android/data/data/{packageName}/cache/
			}else{
				return Environment.getExternalStorageDirectory().getPath() + "/";// cachePath:/sdcard/
			}
		}else{
			return context.getCacheDir().getPath();
		}
	}
	
	
	public static int getWidthPixels(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	public static int getHeightPixels(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}
}
