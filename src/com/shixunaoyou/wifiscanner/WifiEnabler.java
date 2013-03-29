package com.shixunaoyou.wifiscanner;

import java.util.concurrent.atomic.AtomicBoolean;

import com.shixunaoyou.wifiscanner.wifi.Summary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.CheckBoxPreference;

public class WifiEnabler implements Preference.OnPreferenceChangeListener {
    private final Context mContext;
    private final CheckBoxPreference mCheckBox;
    private final CharSequence mOriginalSummary;

    private final WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;

    // indicate whether to connect a wifi ap.
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION
                    .equals(action)) {
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo
                            .getDetailedStateOf((SupplicantState) intent
                                    .getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {

                NetworkInfo info = (NetworkInfo) intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                mConnected.set(info.isConnected());
                handleStateChanged(info.getDetailedState());
            }
        }
    };

    public WifiEnabler(Context context, CheckBoxPreference checkBox) {
        mContext = context;
        mCheckBox = checkBox;
        mOriginalSummary = checkBox.getSummary();
        checkBox.setPersistent(false);

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public void resume() {
        // Wi-Fi state is sticky, so just let the receiver update UI
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mCheckBox.setOnPreferenceChangeListener(this);
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        mCheckBox.setOnPreferenceChangeListener(null);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean enable = (Boolean) value;

        /**
         * Disable tethering if enabling Wifi
         */
        int wifiApState = mWifiManager.getWifiState();
        if (enable && ((wifiApState == WifiManager.WIFI_STATE_ENABLING) ||
                (wifiApState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
        }
        if (mWifiManager.setWifiEnabled(enable)) {
            mCheckBox.setEnabled(false);
        } else {
            mCheckBox.setSummary(R.string.wifi_error);
        }

        // Don't update UI to opposite state until we're sure
        return false;
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mCheckBox.setSummary(R.string.wifi_starting);
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                mCheckBox.setChecked(true);
                mCheckBox.setSummary(null);
                mCheckBox.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mCheckBox.setSummary(R.string.wifi_stopping);
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                mCheckBox.setChecked(false);
                mCheckBox.setSummary(mOriginalSummary);
                mCheckBox.setEnabled(true);
                break;
            default:
                mCheckBox.setChecked(false);
                mCheckBox.setSummary(R.string.wifi_error);
                mCheckBox.setEnabled(true);
        }
    }

    private void handleStateChanged(NetworkInfo.DetailedState state) {
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the check box as an optimization.
        if (state != null && mCheckBox.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                mCheckBox.setSummary(Summary.get(mContext, info.getSSID(), state));
            }
        }
    }
}
