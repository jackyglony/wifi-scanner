
package com.cm.wifiscanner.legacy;

import com.cm.wifiscanner.wifi.AccessPoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.util.List;

public class WifiAdapterManager {

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    private static final String EXTRA_HUB_NAME = "extra_hub_name";
    private static final String EXTRA_HUB_PASSWORD = "extra_hub_password";
    private static final String TEST_URL = "www.baidu.com";

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;

    private static WifiAdapterManager sInstance;

    private AccessPoint mSelectedAccessPoint;

    public synchronized static WifiAdapterManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WifiAdapterManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP)
                || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public interface WifiAdapterStateChangeListener {
        /* Broadcast intent action indicating that Wi-Fi AP has been enabled, disabled,
         * enabling, disabling, or failed. */
        public void onWifiStateChanged(int state);
        /*  */
        public void onStateChanged(int state);
        /*  */
        public void onScanResultAvailable(List<ScanResult> results);
    }

    private static final String NOT_AVAILABLE = "N/A";
    private final WifiManager mWifiManager;
    private final WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfigurations;
    private WifiLock mWifiLock;

    private WifiAdapterStateChangeListener mListener;
    private Scanner mScanner;
    private Context mContext;

    private Handler mHandler;

    public WifiAdapterManager(Context context) {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };

        mScanner = new Scanner();

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();

        mContext = context;

        HandlerThread thread = new HandlerThread("hub_login_handler");
        thread.start();
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
//                if (bundle != null) {
//                    String name = bundle.getString(EXTRA_HUB_NAME);
//                    String pwd = bundle.getString(EXTRA_HUB_PASSWORD);
//
//                    if (LoginUtils.getInstance(mContext).loginHub(name, pwd, TEST_URL)) {
//                        Toast.makeText(mContext, R.string.hub_login_success, Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(mContext, R.string.hub_login_failure, Toast.LENGTH_LONG).show();
//                    }
//                }
            }
        };
    }

    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(
                    intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN),
                    intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)intent
                    .getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(((NetworkInfo)intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }

    private void updateWifiState(int state, int prevState) {
        // TODO update the
    }

    private void updateAccessPoints() {

    }

    private void updateConnectionState(DetailedState state) {

    }

    public void resume() {
        if (mContext != null) {
            mContext.registerReceiver(mReceiver, mFilter);
        }

        if (mScanner != null) {
            mScanner.resume();
        }
    }

    public void pause() {
        if (mContext != null) {
            mContext.unregisterReceiver(mReceiver);
        }

        if (mScanner != null) {
            mScanner.pause();
        }
    }

    public void destroy() {
        if (mHandler != null) {
            Looper looper = mHandler.getLooper();
            if (looper != null) {
                looper.quit();
            }
        }
    }

    public void toggleWifi() {
        final boolean enabled = mWifiManager.isWifiEnabled();
        mWifiManager.setWifiEnabled(!enabled);
    }

    public void connetionConfiguration(int index) {
        if (index < mWifiConfigurations.size()) {
            mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
        }
    }

    public int checkWifiState() {
        return mWifiManager.getWifiState();
    }

    public List<ScanResult> getScanResult() {
        return mWifiList;
    }

    public String getMacAddress() {
        return (mWifiInfo == null) ? NOT_AVAILABLE : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (mWifiInfo == null) ? NOT_AVAILABLE : mWifiInfo.getBSSID();
    }

    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    public String getWifiInfo() {
        return (mWifiInfo == null) ? NOT_AVAILABLE : mWifiInfo.toString();
    }

    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        mWifiManager.enableNetwork(wcgID, true);
    }

    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    private void ensureLock() {
        if (mWifiLock != null) {
            mWifiLock = mWifiManager.createWifiLock("lock");
        }
    }

    public void requireLock() {
        ensureLock();
        mWifiLock.acquire();
    }

    public void releaseLock() {
        ensureLock();
        mWifiLock.release();
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public void setWifiEnabled(boolean enabled) {
        mWifiManager.setWifiEnabled(enabled);

        if (!enabled && mWifiList != null) {
            mWifiList.clear();
        }
    }

    public void setWifiStateChangeListener(WifiAdapterStateChangeListener listener) {
        mListener = listener;
    }

    public void connectToHub(String name, String pwd) {
        mHandler.removeMessages(0);
        Message msg = mHandler.obtainMessage(0);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_HUB_NAME, name);
        bundle.putString(EXTRA_HUB_PASSWORD, pwd);

        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                return;
            }

            sendEmptyMessageDelayed(0, 6000);
        }
    }
}
