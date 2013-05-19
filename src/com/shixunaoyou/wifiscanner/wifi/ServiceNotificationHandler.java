package com.shixunaoyou.wifiscanner.wifi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.WifiScannerMainTabActivity;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;
import com.umeng.analytics.j;

public class ServiceNotificationHandler {
    private static final String TAG = "ServiceNotificationHandler";

    private static ServiceNotificationHandler mInstance;
    private static final String SERVICE_NOTIFICATION_TAG = "com.cm.wifiscanner.service";
    private static final int SERVICE_NOTIFICATION_ID = R.id.service_notification_logo;
    private static final String BAIDU_TN_URL = "&tn=www.591wifi.com";
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
        mAllHotwordList = new ArrayList<String>();
        mAllHotwordMap = new HashMap<String, String>();
        mNm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mMessage = mContext
                .getString(R.string.wifi_service_notificaion_no_wifi);
        initNotification();
        updateConfigure();
        updateAllHotword();
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
        setNotificationHotWord(false);
        mServiceNotification.contentView = mContentView;
    }

    private void setNotificationMessage(String message) {
        mMessage = message;
        mContentView.setTextViewText(R.id.service_notifcation_message, message);
    }

    private void setNotificationHotWord(boolean shouldHide) {
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
                    R.id.service_notification_hotword_layout, pendingIntent);
        }
    }

    private Intent getHotWordIntent() {
        Intent intent = new Intent(mContext, HotwordHandlerActivity.class);
        intent.putExtra(Constants.HOT_WORD, mHotWord);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String url = mAllHotwordMap.get(mHotWord);
        if(TextUtils.isEmpty(url)) {
            url = SEARCH_URL + mHotWord + BAIDU_TN_URL;
        }
        intent.putExtra(Constants.HOT_WORD_URL, url);
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setData(Uri.parse(mAllHotwordMap.get(mHotWord)));
        return intent;
    }

    public void updateNotificationMessage(String message, boolean showTicker) {
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

    public void updateNotificationHotWord(boolean shouldHide) {
        if (mAllHotwordList.size() == 0) {
            return;
        }
        if (!shouldHide) {
            mHotWord = getNextHotword();
        }
        setNotificationHotWord(shouldHide);
        if (isEnableShowNotification) {
            mNm.notify(SERVICE_NOTIFICATION_TAG, SERVICE_NOTIFICATION_ID,
                    mServiceNotification);
            mServiceNotification.tickerText = null;
        }
    }

    public void updateAllHotword() {
        String hotwordList = Utils.getHotwordList(mContext);
        if (!TextUtils.isEmpty(hotwordList)) {
            parseJSONAndGenerateHotList(hotwordList);
        }
    }

    private void parseJSONAndGenerateHotList(String hotwordList) {
        Logger.debug(TAG, "parseJSONAndGenerateHotList");
        try {
            JSONObject result = new JSONObject(hotwordList);
            JSONArray hotwordArray = result.getJSONArray("allData");
            mAllHotwordList.clear();
            mAllHotwordMap.clear();
            for (int i = 0; i < hotwordArray.length(); i++) {
                JSONObject item = hotwordArray.getJSONObject(i);
                String hotword = Html.fromHtml(item.getString("sKeyword"))
                        .toString();
                String url = Html.fromHtml(item.getString("sURL")).toString() + BAIDU_TN_URL;
                mAllHotwordList.add(hotword);
                mAllHotwordMap.put(hotword, url);
                mHotwordIndex = 0;
                Logger.debug(TAG, "keyword id: " + hotword + " url:" + url);
                mHotWord = mAllHotwordList.get(mHotwordIndex);
                updateNotificationHotWord(false);
            }
        } catch (JSONException e) {
            Logger.debug(TAG, e.toString());
            e.printStackTrace();
        }
    }

    private String getNextHotword() {
        mHotwordIndex++;
        if (mHotwordIndex >= mAllHotwordList.size()) {
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
