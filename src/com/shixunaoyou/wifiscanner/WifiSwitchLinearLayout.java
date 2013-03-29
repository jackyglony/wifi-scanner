package com.shixunaoyou.wifiscanner;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shixunaoyou.wifiscanner.wifi.Summary;

public class WifiSwitchLinearLayout extends LinearLayout implements
        OnClickListener {
    private Context mContext;
    private CheckBox mCheckBox;
    private CharSequence mOriginalSummary;

    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;
    private TextView mInfoView;

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

    public WifiSwitchLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

    }

    @Override
    protected void onFinishInflate() {
        mCheckBox = (CheckBox) this.findViewById(R.id.wifi_switch_checkbox);
        mInfoView = (TextView) this.findViewById(R.id.wifi_info_textview);
        mOriginalSummary = mInfoView.getText().toString();
    }

    //
    // public WifiCheckBoxEnabler(Context context, CheckBox checkBox,
    // TextView textView) {
    // mContext = context;
    // mCheckBox = checkBox;
    // mInfoView = textView;
    // mOriginalSummary = mInfoView.getText().toString();
    //
    // mWifiManager = (WifiManager) context
    // .getSystemService(Context.WIFI_SERVICE);
    // mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
    // // The order matters! We really should not depend on this. :(
    // mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
    // mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    // }

    public void resume() {
        // Wi-Fi state is sticky, so just let the receiver update UI
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mCheckBox.setOnClickListener(this);
        mOriginalSummary = mInfoView.getText().toString();

    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        mCheckBox.setOnCheckedChangeListener(null);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean enable = (Boolean) value;

        /**
         * Disable tethering if enabling Wifi
         */
        int wifiApState = mWifiManager.getWifiState();
        if (enable
                && ((wifiApState == WifiManager.WIFI_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
        }
        if (mWifiManager.setWifiEnabled(enable)) {
            mCheckBox.setEnabled(false);
        } else {
            mInfoView.setText(R.string.wifi_error);
        }

        // Don't update UI to opposite state until we're sure
        return false;
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mInfoView.setText(R.string.wifi_starting);
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                mCheckBox.setChecked(true);
                mInfoView.setText(null);
                mCheckBox.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mInfoView.setText(R.string.wifi_stopping);
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                mCheckBox.setChecked(false);
                mInfoView.setText(mOriginalSummary);
                mCheckBox.setEnabled(true);
                break;
            default:
                mCheckBox.setChecked(false);
                mInfoView.setText(R.string.wifi_error);
                mCheckBox.setEnabled(true);
        }
    }

    private void handleStateChanged(NetworkInfo.DetailedState state) {
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the check box as an optimization.
        if (state != null && mCheckBox.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                mInfoView.setText(Summary.get(mContext, info.getSSID(), state));
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        /**
         * Disable tethering if enabling Wifi
         */
        boolean isChecked = mCheckBox.isChecked();
        int wifiApState = mWifiManager.getWifiState();
        if (isChecked
                && ((wifiApState == WifiManager.WIFI_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
        }

        if (mWifiManager.setWifiEnabled(isChecked)) {
            mCheckBox.setEnabled(false);
        } else {
            mInfoView.setText(R.string.wifi_error);
        }
    }
}
