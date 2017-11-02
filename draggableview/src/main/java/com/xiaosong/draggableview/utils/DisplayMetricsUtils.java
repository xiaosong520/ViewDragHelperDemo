package com.xiaosong.draggableview.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DisplayMetricsUtils {
	
	/**
	 * 获取屏幕高度
	 * @param activity
	 * @return
	 */
	public static int getScreenHeightPixels(Activity activity){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.heightPixels;
	}
	
	/**
	 * 获取屏幕宽度
	 * @param activity
	 * @return
	 */
	public static int getScreenWidthPixels(Activity activity){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.widthPixels;
	}
	

	/**
	 * 获取屏幕像素密度因子
	 * @param activity
	 * @return
	 */
	public static float getDensity(Activity activity){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.density;
	}
	
	/**
	 * 获取actionbar高度
	 * @return
	 */
	public static int getActionbarHeight(Context context){
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
		    return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
		}
		return 0;
	}
}
