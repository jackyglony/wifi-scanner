package com.cm.wifiscanner.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkAccess {

    public static boolean internetAccess(Context context, int timeout) {
        ConnectivityManager service = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConnect = service.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifiConnect.isConnected();
    }
}
