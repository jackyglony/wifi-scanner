package com.shixunaoyou.wifiscanner.wifi;

import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.UMengUtils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class HotwordHandlerActivity extends Activity {
    private static final String TAG = "HotwordHandlerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        handleActtionOfOpenHotWord(intent);
    }

    private void handleActtionOfOpenHotWord(final Intent intent) {
        Logger.debug(TAG, "handleActtionOfOpenHotWord");
        MobclickAgent.onEvent(this, UMengUtils.EVENT_CLICK_HOTWORD);
        String hotwordUrls = intent.getExtras().getString(
                Constants.HOT_WORD_URL);
        Intent openHotwordIntent = new Intent(Intent.ACTION_VIEW);
        openHotwordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openHotwordIntent.setData(Uri.parse(hotwordUrls));
        startActivity(openHotwordIntent);
        ServiceNotificationHandler notificationHandler = ServiceNotificationHandler
                .getInstance(this);
        notificationHandler.updateNoficationHotWord(false);
        finish();
    }
}
