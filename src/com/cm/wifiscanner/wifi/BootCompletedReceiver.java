package com.cm.wifiscanner.wifi;

import com.cm.wifiscanner.util.Logger;
import com.cm.wifiscanner.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "ConnectionChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.debug(TAG, "receive the boot complete message");
        if (Utils.getEnableAutoLogin(context)) {
            Intent intentService = new Intent(context, WiFiScanService.class);
            context.startService(intentService);
        }
    }
}