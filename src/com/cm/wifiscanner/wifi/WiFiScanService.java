package com.cm.wifiscanner.wifi;

import java.util.concurrent.atomic.AtomicBoolean;

import com.cm.wifiscanner.R;
import com.cm.wifiscanner.WifiScannerMainTabActivity;
import com.cm.wifiscanner.util.Constants;
import com.cm.wifiscanner.util.Logger;
import com.cm.wifiscanner.util.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

public class WiFiScanService extends Service {
    private static final String TAG = "WiFiScanService";

    private static final String NOTIFICATION_TAG = "com.cm.wifiscanner";
    private static final int NOTIFICATION_ID = 0;

    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;

    // First Check Status is result of Check Baidu
    private int mFirstCheckStatus = Constants.CANNOT_CONNECT;

    // Second Check Status is result of 591 WiFi
    private boolean mSecondCheckStatus = false;;
    // indicate whether to connect a wifi ap.
    private AtomicBoolean mConnected = new AtomicBoolean(false);
    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug(TAG, "Service Create");
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext = this.getApplicationContext();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };
        registerReceiver(mReceiver, mFilter);
    }

    private void handleEvent(Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean lastStatus = mConnected.get();
            mConnected.set(info.isConnected());
            if (!lastStatus && mConnected.get()) {
                Logger.debug(TAG, "Service start to check network!");
                ServiceFirstCheckStatus task = new ServiceFirstCheckStatus();
                if( Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
            if (!info.isConnected()) {
                Utils.setLoginStatus(this, Constants.NO_WIFI);
                Utils.setIsServiceUpdate(this, true);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mConnected.set(Utils.isHasWifiConnection(this));
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.debug(TAG, "onDestroy");
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
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
                if( Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            } else {
                Utils.setIsServiceUpdate(WiFiScanService.this, true);
            }
        }
    }

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
                showAdDialog();
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

    @SuppressWarnings("deprecation")
    private void showAdDialog() {
        AdShowDialog dialog = new AdShowDialog(mContext);
        dialog.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        dialog.getWindow().setType(
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
        dialog.show();
        /* set size & pos */
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (display.getHeight() > display.getWidth()) {
            // lp.height = (int) (display.getHeight() * 0.5);
            lp.width = (int) (display.getWidth() * 1.0 );
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
                Logger.debug(TAG, "Second Check!!");
                ServiceSecondCheckStatus secondCheck = null;

                secondCheck = new ServiceSecondCheckStatus(mContext);
                if( Build.VERSION.SDK_INT >= 11) {
                    secondCheck.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    secondCheck.execute();
                }
            } else if (mFirstCheckStatus == Constants.HAVE_LOGOUT) {
                Logger.debug(TAG, "Will login automtically!!");
                String name = Utils.getUserName(mContext);
                if (!TextUtils.isEmpty(name)) {
                    ServiceLoginTask loginTask = new ServiceLoginTask(mContext);
                    if( Build.VERSION.SDK_INT >= 11) {
                        loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        loginTask.execute();
                    }
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
                if (Build.VERSION.SDK_INT >= 11) {
                    sendNotificationForICS(true);
                } else {
                    sendNotification(true);
                }
            }
            Utils.setLoginStatus(mContext, result);
            Utils.setIsServiceUpdate(mContext, true);
        }
    }
}
