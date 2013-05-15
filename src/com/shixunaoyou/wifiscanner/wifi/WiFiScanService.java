package com.shixunaoyou.wifiscanner.wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.WifiScannerMainTabActivity;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.HttpUtils;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.UMengUtils;
import com.shixunaoyou.wifiscanner.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class WiFiScanService extends Service {
    private static final String TAG = "WiFiScanService";

    private static final String NOTIFICATION_TAG = "com.cm.wifiscanner";
    private static final int NOTIFICATION_ID = 0;
    private static final long UPDATE_NOTIFICATION_INTERVAL = 1000 * 60 * 5;
    private static final long UPDATE_HOTWORD_INTERVAL = 1000 * 60 * 60 * 2;
    private static final String HOT_WORD_URL = "http://www.591wifi.com/portal/getrssinfolist&version=1.0";

    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;

    // First Check Status is result of Check Baidu
    private int mFirstCheckStatus = Constants.CANNOT_CONNECT;

    // Second Check Status is result of 591 WiFi
    private boolean mSecondCheckStatus = false;;
    // indicate whether to connect a wifi ap.
    private AtomicBoolean mConnected = new AtomicBoolean(false);
    private Context mContext;
    private ServiceNotificationHandler mNotificationHandler;
    private WifiManager mWifiManager;
    private boolean isStart;
    private long mLastNotificationUpdateTime;
    private long mLastHotwordUpdateTime;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug(TAG, "Service Create");
        mWifiManager = (WifiManager) getSystemService(Activity.WIFI_SERVICE);
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(Intent.ACTION_SCREEN_ON);
        mContext = this.getApplicationContext();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };
        mConnected.set(Utils.isHasWifiConnection(this));
        mNotificationHandler = ServiceNotificationHandler.getInstance(this);
        mLastHotwordUpdateTime = Utils.getHotwordUpdateTime(this);
        registerReceiver(mReceiver, mFilter);
    }

    private void updateNotification() {
        Logger.debug(TAG, "updateNotification");
        if (!mConnected.get()) {
            mNotificationHandler.sendNotification();
            mNotificationHandler
                    .updateNoficationMessage(
                            getString(R.string.wifi_service_notificaion_no_wifi),
                            false);
            mNotificationHandler.updateNoficationHotWord(true);
        } else {
            WifiInfo info = mWifiManager.getConnectionInfo();
            String ssid = info.getSSID();
            mNotificationHandler.updateNoficationMessage(
                    getString(R.string.wifi_service_notificaion_connect_wifi,
                            ssid), true);
            mNotificationHandler.updateNoficationHotWord(false);
        }
    }

    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Logger.debug(TAG, "handleEvent: "
                    + WifiManager.NETWORK_STATE_CHANGED_ACTION);
            boolean lastStatus = mConnected.get();
            mConnected.set(info.isConnected());
            if (!lastStatus && mConnected.get() && !isStart) {
                startCheckNetwork();
            }
            if (!info.isConnected()) {
                Utils.setLoginStatus(this, Constants.NO_WIFI);
                Utils.setIsServiceUpdate(this, true);
            }
            if (!isStart) {
                updateNotification();
            }
            isStart = false;
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            Logger.debug(TAG, "status: SCAN_RESULTS_AVAILABLE_ACTION");
            if (!mConnected.get() && shouldUpdateNotification()) {
                updateAvaibleNetworkNotification();
            }
            if (mConnected.get() && shoudUpdateHotWord()) {

            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
            if (status == WifiManager.WIFI_STATE_DISABLED) {
                resetNotificationUpdateTime();
            }
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            Logger.debug(TAG, "android.intent.action.SCREEN_ON");
            if (!mConnected.get()) {
                updateAvaibleNetworkNotification();
                mLastNotificationUpdateTime = System.currentTimeMillis();
            }
        }
    }

    private void resetNotificationUpdateTime() {
        Logger.debug(TAG, "resetUpdateTime");
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            mLastNotificationUpdateTime = 0;
        }
    }

    private boolean shouldUpdateNotification() {
        long current = System.currentTimeMillis();
        boolean shouldUpdate = false;
        if (mLastNotificationUpdateTime == 0) {
            shouldUpdate = true;
            mLastNotificationUpdateTime = current;
        } else if (current - mLastNotificationUpdateTime > UPDATE_NOTIFICATION_INTERVAL) {
            shouldUpdate = true;
            mLastNotificationUpdateTime = current;
        } else {
            shouldUpdate = false;
        }
        return shouldUpdate;
    }

    private boolean shoudUpdateHotWord() {
        boolean shouldUpdate = false;
        long current = System.currentTimeMillis();
        if (mLastHotwordUpdateTime == 0) {
            shouldUpdate = true;
            mLastHotwordUpdateTime = current;
        } else if (current - mLastHotwordUpdateTime > UPDATE_HOTWORD_INTERVAL) {
            shouldUpdate = true;
            mLastHotwordUpdateTime = current;
        } else {
            shouldUpdate = false;
        }
        return shouldUpdate;
    }

    private void updateAvaibleNetworkNotification() {
        Logger.debug(TAG, "updateAvaibleNetworkNotification");
        List<AccessPoint> points = getAllAccessPoints();
        int availableNetworkCount = 0;
        int openNetworkCount = 0;
        for (AccessPoint accessPoint : points) {
            if (accessPoint.getLevel() != Constants.NOT_IN_RANGE) {
                availableNetworkCount++;
                if (accessPoint.security == AccessPoint.SECURITY_NONE) {
                    openNetworkCount++;
                }
            }
        }
        updateNofitication(availableNetworkCount, openNetworkCount);
    }

    private void updateNofitication(int availableNetworkCount,
            int openNetworkCount) {
        if (availableNetworkCount > 0) {
            String message = getString(
                    R.string.wifi_service_notification_avail_wifi,
                    availableNetworkCount, openNetworkCount);
            mNotificationHandler.updateNoficationMessage(message, true);
        }
    }

    private List<AccessPoint> getAllAccessPoints() {
        List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(this, config);
                accessPoints.add(accessPoint);
            }
        }

        List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                if (result.frequency == 0) {
                    continue;
                }
                boolean found = false;
                for (AccessPoint accessPoint : accessPoints) {
                    if (accessPoint.update(result)) {
                        found = true;
                    }
                }
                if (!found) {
                    accessPoints.add(new AccessPoint(this, result));
                }
            }
        }
        return accessPoints;
    }

    private void startCheckNetwork() {
        Logger.debug(TAG, "Service start to check network!");
        ServiceFirstCheckStatus task = new ServiceFirstCheckStatus();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Logger.debug(TAG, "onStartCommand");
        boolean isHasWifiConnecton = Utils.isHasWifiConnection(this);
        if (intent != null) {
            boolean isReboot = intent.getBooleanExtra(Constants.REBOOT_START,
                    false);
            mConnected.set(isHasWifiConnecton);
            if (isReboot) {
                updateNotification();
            }
            isStart = true;
            if (isHasWifiConnecton && isReboot) {
                Logger.debug(TAG, "start try to login after rebooting");
                startCheckNetwork();
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.debug(TAG, "onDestroy");
        Utils.setHotwordUpdateTime(this, mLastHotwordUpdateTime);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private void updateLoginNotification() {
        Logger.debug(TAG, "updateLoginNotification");
        WifiInfo info = mWifiManager.getConnectionInfo();
        String ssid = info.getSSID();
        mNotificationHandler.updateNoficationMessage(
                getString(R.string.wifi_service_notification_login_wifi, ssid),
                true);
    }

    class ServiceLoginTask extends LoginTask {

        ServiceLoginTask(Context context) {
            super(context);
        }

        @Override
        public void onPostExecute(String result) {
            Logger.debug(TAG, "=========Connect Status: " + result
                    + " ==========");
            super.onPostExecute(result);
            if (result == null) {
                DoubleCheck task = new DoubleCheck();
                if (Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            } else {
                Utils.setIsServiceUpdate(WiFiScanService.this, true);
            }
        }
    }

    // TODO delete this method
    @SuppressWarnings("deprecation")
    @TargetApi(11)
    private void sendNotificationForICS(boolean isConnect) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        /* clear earlier notification */
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);

        Intent intent = new Intent(this, WifiScannerMainTabActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 11) {
            /** Create the notification */
            Notification.Builder builder = new Notification.Builder(this);
            builder.setWhen(System.currentTimeMillis())
                    .setContentIntent(mPendingIntent).setAutoCancel(true);
            String contextText = null;

            if (isConnect) {
                contextText = this.getResources().getString(
                        R.string.wifi_notification_login_successful);
            } else {
                contextText = this.getResources().getString(
                        R.string.wifi_notification_login_failure);
            }

            builder.setContentText(contextText).setContentTitle(
                    this.getResources().getString(R.string.app_name));

            builder.setSmallIcon(R.drawable.ic_launcher).setTicker(contextText);

            notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID,
                    builder.getNotification());
        }
    }

    // TODO delete this method
    @SuppressWarnings("deprecation")
    private void showAdDialog() {
        AdShowDialog dialog = new AdShowDialog(mContext);
        dialog.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        // dialog.getWindow().setType(
        // WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
        dialog.show();
        /* set size & pos */
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (display.getHeight() > display.getWidth()) {
            // lp.height = (int) (display.getHeight() * 0.5);
            lp.width = (int) (display.getWidth() * 1.0);
            lp.height = (int) (display.getHeight() * 0.8);
        } else {
            lp.height = (int) (display.getHeight() * 0.8);
            lp.width = (int) (display.getWidth() * 0.8);
        }
        dialog.getWindow().setAttributes(lp);
    }

    @SuppressWarnings("deprecation")
    private void sendNotification(boolean isConnect) {
        String contextText = null;
        if (isConnect) {
            contextText = this.getResources().getString(
                    R.string.wifi_notification_login_successful);
        } else {
            contextText = this.getResources().getString(
                    R.string.wifi_notification_login_failure);
        }
        Notification notification = new Notification(R.drawable.ic_launcher,
                contextText, System.currentTimeMillis());
        notification.defaults = Notification.DEFAULT_ALL;
        PendingIntent pt = PendingIntent.getActivity(this, 0, new Intent(this,
                WifiScannerMainTabActivity.class), 0);
        notification.setLatestEventInfo(this,
                this.getResources().getString(R.string.app_name), contextText,
                pt);
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID,
                notification);
    }

    class ServiceFirstCheckStatus extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int status = Constants.CANNOT_CONNECT;

            for (int i = 0; i < 2; i++) {
                status = Utils.checkTestUrl(mContext);
                if (status == Constants.CANNOT_CONNECT) {
                    SystemClock.sleep(1000);
                } else {
                    break;
                }
            }
            return status;
        }

        @Override
        public void onPostExecute(Integer result) {
            Logger.debug(TAG, "WifiFirstCheckStatus: " + result);
            mFirstCheckStatus = result;
            if (mFirstCheckStatus == Constants.CANNOT_CONNECT
                    || mFirstCheckStatus == Constants.NOT_FIND_SERVER) {
                Utils.setLoginStatus(mContext, mFirstCheckStatus);
                Utils.setIsServiceUpdate(mContext, true);
            } else if (mFirstCheckStatus == Constants.CONNECTED) {
                // Start to second check
                MobclickAgent.onEvent(mContext,
                        UMengUtils.EVENT_CONNECT_INTERNET);
                Logger.debug(TAG, "Second Check!!");
                ServiceSecondCheckStatus secondCheck = null;
                secondCheck = new ServiceSecondCheckStatus(mContext);
                if (Build.VERSION.SDK_INT >= 11) {
                    secondCheck
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    secondCheck.execute();
                }
            } else if (mFirstCheckStatus == Constants.HAVE_LOGOUT) {
                MobclickAgent.onEvent(mContext,
                        UMengUtils.EVENT_CONNECT_TO_SERVER);

                Logger.debug(TAG, "Will login automtically!!");
                String name = Utils.getUserName(mContext);
                if (!TextUtils.isEmpty(name)) {
                    ServiceLoginTask loginTask = new ServiceLoginTask(mContext);
                    if (Build.VERSION.SDK_INT >= 11) {
                        loginTask
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        loginTask.execute();
                    }
                    WifiInfo info = mWifiManager.getConnectionInfo();
                    String ssid = info.getSSID();
                    mNotificationHandler
                            .updateNoficationMessage(
                                    getString(
                                            R.string.wifi_service_notification_login_wifi,
                                            ssid), true);
                    Utils.setLoginStatus(mContext, mFirstCheckStatus);
                    Utils.setIsServiceUpdate(mContext, true);
                }
            }
        }
    }

    class ServiceSecondCheckStatus extends AsyncTask<Void, Void, Boolean> {
        private Context mMyConext;

        ServiceSecondCheckStatus(Context context) {
            mMyConext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return Utils.check591Server(mMyConext);
        }

        @Override
        public void onPostExecute(Boolean result) {
            Logger.debug(TAG, "WifiSecondCheckStatus: " + result);
            int status = Constants.STATUS_UNKOWN;
            mSecondCheckStatus = result;
            if (mSecondCheckStatus) {
                status = Constants.HAVE_LOGIN;
                updateLoginNotification();
            } else {
                status = Constants.NOT_NEED_LOGIN;
            }
            Utils.setLoginStatus(mContext, status);
            Utils.setIsServiceUpdate(mContext, true);
        }
    }

    class DoubleCheck extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            return Utils.checkTestUrl(mContext);
        }

        @Override
        public void onPostExecute(Integer result) {
            Logger.debug(TAG, "DoubleCheck: " + result);
            if (result == Constants.CONNECTED) {
                result = Constants.HAVE_LOGIN;
                // if (Build.VERSION.SDK_INT >= 11) {
                // sendNotificationForICS(true);
                // } else {
                // sendNotification(true);
                // }
                showAdDialog();
                updateLoginNotification();
                Toast.makeText(WiFiScanService.this,
                        R.string.wifi_notification_login_successful,
                        Toast.LENGTH_SHORT).show();
            }
            Utils.setLoginStatus(mContext, result);
            Utils.setIsServiceUpdate(mContext, true);
        }
    }

    class HotwordUpdateTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            try {
                result = getHotJson();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return result;
        }

        private String getHotJson() throws JSONException {
            JSONObject hotJSON = HttpUtils.sendPostRequest(HOT_WORD_URL);
            int errno = hotJSON.getInt("errNo");
            return hotJSON.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (TextUtils.isEmpty(result)) {
            } else {

            }
        }
    }
}
