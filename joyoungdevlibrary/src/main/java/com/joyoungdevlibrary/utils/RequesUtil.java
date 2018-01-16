package com.joyoungdevlibrary.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;


import com.joyoungdevlibrary.utils.encryptdecryptutil.Encrypt;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Joyoung on 2016/5/20.
 */
public class RequesUtil {

	public static String loginHead() {
		long stamp = System.currentTimeMillis();
		String uri = "login?client=1&action=login&msgId=1&stamp=" + stamp + "&enc=0";
		return uri;
	}

	public static JSONObject getAPPData(Context context) throws Exception {
		JSONObject mob_data = new JSONObject();
		mob_data.put("model", android.os.Build.MANUFACTURER + android.os.Build.MODEL);
		mob_data.put("localsizeModel", "Android");
		mob_data.put("systemName", "Android" + android.os.Build.VERSION.RELEASE);
		mob_data.put("systemVersion", android.os.Build.VERSION.SDK);
		mob_data.put("mobileId", getmobile_id(context));
		mob_data.put("mobileName", android.os.Build.MANUFACTURER);
		mob_data.put("appVersion", getAPPVersion(context));
		return mob_data;
	}

	public static String getmobile_id(Context context) {
		TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		@SuppressLint("MissingPermission") String identity = telephonemanage.getDeviceId();
		return identity;
	}

	public static String getAPPVersion(Context context) throws PackageManager.NameNotFoundException {
		PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		return info.versionName;
	}

	/**
	 * 获取sign值
	 * 
	 * @param str
	 * @param data
	 * @param salt
	 * @return String
	 */
	public static String getSign(String str, byte[] data, String salt) {
		byte[] value = null;
		byte[] strByte = null;
		byte[] saltByte = null;
		try {
			saltByte = salt.getBytes("utf-8");
			strByte = str.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		int strlength = strByte.length;
		int datalength = 0;
		if (data != null) {
			datalength = data.length;
		}
		int saltlength = saltByte.length;
		value = new byte[strlength + datalength + saltlength];

		System.arraycopy(strByte, 0, value, 0, strlength);
		if (datalength > 0) {
			System.arraycopy(data, 0, value, strlength, datalength);
		}
		System.arraycopy(saltByte, 0, value, strlength + datalength, saltlength);

		return Encrypt.MD5(value);
	}

	public static String getSign(String value) {
		return Encrypt.MD5(value);
	}


}
