package com.shixunaoyou.wifiscanner.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.WifiScannerMainTabActivity;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Utils;

public class ServiceNotificationHandler {
    private static ServiceNotificationHandler mInstance;
    private static final String SERVICE_NOTIFICATION_TAG = "com.cm.wifiscanner.service";
    private static final int SERVICE_NOTIFICATION_ID = R.id.service_notification_logo;
    private static final String SEARCH_URL = "http://www.baidu.com/s?wd=";
    private Context mContext;
    private Notification mServiceNotification;
    private boolean isEnableShowNotification;
    private RemoteViews mContentView;
    private NotificationManager mNm;
    private String mHotWord;
    private String mMessage;
    private List<String> mAllHotwordList;
    private Map<String, String> mAllHotwordMap;
    private int mHotwordIndex;

    public synchronized static ServiceNotificationHandler getInstance(
            Context context) {
        if (mInstance == null) {
            mInstance = new ServiceNotificationHandler(context);
        }
        return mInstance;
    }

    private void updateConfigure() {
        isEnableShowNotification = Utils.getEnableServiceNotification(mContext);
    }

    public void updateConfigure(boolean enable) {
        isEnableShowNotification = enable;
    }

    private ServiceNotificationHandler(Context context) {
        mContext = context;
        mHotwordIndex = 0;
        mNm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mMessage = mContext
                .getString(R.string.wifi_service_notificaion_no_wifi);
        updateAllHotword();
        initNotification();
        updateConfigure();
    }

    @SuppressWarnings("deprecation")
    private void initNotification() {
        String contextText = null;
        mServiceNotification = new Notification(R.drawable.ic_launcher,
                contextText, System.currentTimeMillis());
        mServiceNotification.flags = Notification.FLAG_ONGOING_EVENT;
        initContentView();
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, WifiScannerMainTabActivity.class), 0);
        mServiceNotification.contentIntent = contentIntent;
    }

    private void initContentView() {
        mContentView = new RemoteViews(mContext.getPackageName(),
                R.layout.service_notification_view);
        mContentView.setImageViewResource(R.id.service_notification_logo,
                R.drawable.ic_launcher);
        mContentView.setTextViewText(R.id.service_notifcation_hotword_title,
                mContext.getString(R.string.wifi_service_notification_title));
        setNotificationMessage(mMessage);
        setNotifcationHotWord(false);
        mServiceNotification.contentView = mContentView;
    }

    private void setNotificationMessage(String message) {
        mMessage = message;
        mContentView.setTextViewText(R.id.service_notifcation_message, message);
    }

    private void setNotifcationHotWord(boolean shouldHide) {
        if (shouldHide || TextUtils.isEmpty(mHotWord)) {
            mContentView.setViewVisibility(
                    R.id.service_notification_hotword_container, View.GONE);
        } else {
            mContentView.setViewVisibility(
                    R.id.service_notification_hotword_container, View.VISIBLE);
            mContentView.setTextViewText(
                    R.id.service_notification_hotword_content, mHotWord);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, getHotWordIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
            mContentView.setOnClickPendingIntent(
                    R.id.service_notification_hotword_container, pendingIntent);
        }
    }

    private Intent getHotWordIntent() {
        Intent intent = new Intent(mContext, HotwordHandlerActivity.class);
        intent.setAction(Constants.OPEN_HOTWORD_ACTION);
        intent.putExtra(Constants.HOT_WORD, mHotWord);
        intent.putExtra(Constants.HOT_WORD_URL, mAllHotwordMap.get(mHotWord));
        return intent;
    }

    public void updateNoficationMessage(String message, boolean showTicker) {
        setNotificationMessage(message);
        if (isEnableShowNotification) {
            if (showTicker) {
                mServiceNotification.tickerText = mMessage;
            } else {
                mServiceNotification.tickerText = null;
            }
            mNm.notify(SERVICE_NOTIFICATION_TAG, SERVICE_NOTIFICATION_ID,
                    mServiceNotification);
        }
    }

    public void updateNoficationHotWord(boolean shouldHide) {
        if(!shouldHide) {
            mHotWord = getNextHotword();;
        }
        setNotifcationHotWord(shouldHide);
        if (isEnableShowNotification) {
            mNm.notify(SERVICE_NOTIFICATION_TAG, SERVICE_NOTIFICATION_ID,
                    mServiceNotification);
            mServiceNotification.tickerText = null;
        }
    }

    public void updateAllHotword() {
        mAllHotwordList = new ArrayList<String>();
        mAllHotwordMap = new HashMap<String, String>();
        String hotwordList = Utils.getHotwordList(mContext);
        //TestData
        mAllHotwordList.add("test1");
        mAllHotwordList.add("test2");
        mAllHotwordMap.put("test1", SEARCH_URL + "test1");
        mAllHotwordMap.put("test2", SEARCH_URL + "test2");
        mHotwordIndex = 0;
        mHotWord = mAllHotwordList.get(mHotwordIndex);
    }

    private String getNextHotword() {
        mHotwordIndex++;
        if(mHotwordIndex >= mAllHotwordList.size()) {
            mHotwordIndex = 0;
        }
        return mAllHotwordList.get(mHotwordIndex);
    }

    public void sendNotification() {
        initNotification();
        if (isEnableShowNotification) {
            mNm.notify(SERVICE_NOTIFICATION_TAG, SERVICE_NOTIFICATION_ID,
                    mServiceNotification);
        }
    }

    public void cancelNotification() {
        if (mServiceNotification != null) {
            mNm.cancel(SERVICE_NOTIFICATION_TAG, SERVICE_NOTIFICATION_ID);
        }
    }
}
