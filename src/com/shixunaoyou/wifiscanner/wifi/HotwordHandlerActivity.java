package com.shixunaoyou.wifiscanner.wifi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.UMengUtils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

public class HotwordHandlerActivity extends Activity {
    private static final String TAG = "HotwordHandlerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        closeStatusBar();
        Intent intent = getIntent();
        handleActtionOfOpenHotWord(intent);
    }

    private void handleActtionOfOpenHotWord(final Intent intent) {
        Logger.debug(TAG, "handleActtionOfOpenHotWord");
        MobclickAgent.onEvent(this, UMengUtils.EVENT_CLICK_HOTWORD);
        String hotwordUrls = intent.getExtras().getString(
                Constants.HOT_WORD_URL);
        Logger.debug(TAG, hotwordUrls);
        Intent openHotwordIntent = new Intent(Intent.ACTION_VIEW);
        openHotwordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openHotwordIntent.setData(Uri.parse(hotwordUrls));
        startActivity(openHotwordIntent);
        ServiceNotificationHandler notificationHandler = ServiceNotificationHandler
                .getInstance(this);
        notificationHandler.updateNotificationHotWord(false);
        finish();
    }

    private void closeStatusBar() {
        Object sbService = getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class
                    .forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= 17) {
                showsb = statusBarManager.getMethod("collapsePanels");
            } else {
                showsb = statusBarManager.getMethod("collapse");
            }
            showsb.invoke(sbService);
        } catch (ClassNotFoundException e) {
            Logger.debug(TAG, "ClassNotFoundException :" + e.toString());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Logger.debug(TAG, "NoSuchMethodException :" + e.toString());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Logger.debug(TAG, "IllegalArgumentException :" + e.toString());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Logger.debug(TAG, "IllegalAccessException :" + e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Logger.debug(TAG, "InvocationTargetException :" + e.toString());
            e.printStackTrace();
        }
    }

}
