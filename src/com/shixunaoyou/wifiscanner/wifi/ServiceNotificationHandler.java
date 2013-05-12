package com.shixunaoyou.wifiscanner.wifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.WifiScannerMainTabActivity;
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
        mHotWord = "Test";
        mNm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mMessage = mContext
                .getString(R.string.wifi_service_notificaion_no_wifi);
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
        setNotifcationHotWord(mHotWord);
        mServiceNotification.contentView = mContentView;
    }

    private void setNotificationMessage(String message) {
        mMessage = message;
        mContentView.setTextViewText(R.id.service_notifcation_message, message);
    }

    private void setNotifcationHotWord(String word) {
        if (TextUtils.isEmpty(word)) {
            mContentView.setViewVisibility(
                    R.id.service_notification_hotword_container, View.GONE);
        } else {
            mHotWord = word;
            mContentView.setViewVisibility(
                    R.id.service_notification_hotword_container, View.VISIBLE);

            mContentView.setTextViewText(
                    R.id.service_notification_hotword_content, word);
            String url = SEARCH_URL + word;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mContentView.setOnClickPendingIntent(
                    R.id.service_notification_hotword_container, pendingIntent);
        }
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
        updateNoficationHotWord(mHotWord);
    }

    public void updateNoficationHotWord(String hotward) {
        setNotifcationHotWord(hotward);
        if (isEnableShowNotification) {
            mNm.notify(SERVICE_NOTIFICATION_TAG, SERVICE_NOTIFICATION_ID,
                    mServiceNotification);
            mServiceNotification.tickerText = null;
        }
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
