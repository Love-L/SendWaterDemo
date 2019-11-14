package com.qfdqc.views.demo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * NetUtil用于判断一个 Activity 是否有网络
 */
public class NetUtil {
    public static boolean isNetConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }
}