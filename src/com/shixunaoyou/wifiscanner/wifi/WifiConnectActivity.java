package com.shixunaoyou.wifiscanner.wifi;

import java.util.List;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WifiConnectActivity extends Activity implements
        View.OnClickListener {

    private static final String TAG = "WifiConnectActivity";

    private View mPasswordView;
    private Button mConnectButton;
    private TextView mSSIDView;
    private ImageView mWifiSignalView;
    private AccessPoint mAccessPoint;
    private TextView mWifiInfoView;
    private EditText mPasswordEdit;
    private Button mHaveConnectedButton;

    private WifiManager mWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_connect_layout);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar_with_back);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        setTitle();
        setBackButton();
        getViews();
        parseIntentAndInitiateView();
    }

    private void setTitle() {
        TextView titleView = (TextView) findViewById(R.id.titlebar_title);
        titleView.setText(R.string.wifi_connect_title);

    }

    private void setBackButton() {
        Button backBtn = (Button) this.findViewById(R.id.titlebar_back_btn);
        backBtn.setOnClickListener(this);
    }

    private void getViews() {
        mPasswordView = findViewById(R.id.wifi_connect_password_parent);
        mConnectButton = (Button) findViewById(R.id.wifi_connect_btn);
        mSSIDView = (TextView) findViewById(R.id.wifi_connect_ssid);
        mWifiSignalView = (ImageView) findViewById(R.id.wifi_connect_signal);
        mWifiInfoView = (TextView) findViewById(R.id.wifi_connect_info);
        mPasswordEdit = (EditText) findViewById(R.id.wifi_connect_password_edit);
        mHaveConnectedButton = (Button) findViewById(R.id.wifi_connect_internet_btn);
    }

    private void parseIntentAndInitiateView() {
        Intent intent = getIntent();

        String ssid = intent.getExtras().getString(Constants.SSID_KEY);
        mAccessPoint = getAccessPoint(ssid);
        if (mAccessPoint == null) {
            accessPointIsInvalid();
            return;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        mAccessPoint.update(wifiInfo, null);
        initateView();
    }

    private AccessPoint getAccessPoint(String ssid) {
        AccessPoint accessPoint = getFromConfiguredNetwork(ssid);
        return accessPoint;
    }

    private AccessPoint getFromConfiguredNetwork(String ssid) {
        AccessPoint accessPoint = null;
        List<WifiConfiguration> configures = mWifiManager
                .getConfiguredNetworks();
        if (configures != null) {
            for (WifiConfiguration configure : configures) {
                if (TextUtils.equals(
                        AccessPoint.removeDoubleQuotes(configure.SSID), ssid)) {
                    accessPoint = new AccessPoint(this, configure);
                    break;
                }
            }
        }
        accessPoint = updateFromWifiScanResult(accessPoint, ssid);
        return accessPoint;
    }

    private AccessPoint updateFromWifiScanResult(AccessPoint accessPoint,
            String ssid) {
        List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                if (TextUtils.equals(result.SSID, ssid)) {
                    if (accessPoint == null) {
                        accessPoint = new AccessPoint(this, result);
                    } else {
                        accessPoint.update(result);
                    }
                    break;
                }
            }
        }
        return accessPoint;
    }

    private void accessPointIsInvalid() {
        Toast.makeText(this, R.string.wifi_connect_invalid_network,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initateView() {
        updateSignalView();
        updateSSId();
        updateSecurityInfo();
        updateButton();
    }

    private void updateSignalView() {
        final int level = mAccessPoint.getLevel();
        switch (level) {
            case 0:
                mWifiSignalView.setImageResource(R.drawable.ic_wifi_signal_1);
                break;
            case 1:
                mWifiSignalView.setImageResource(R.drawable.ic_wifi_signal_2);
                break;
            case 2:
                mWifiSignalView.setImageResource(R.drawable.ic_wifi_signal_3);
                break;
            case 3:
                mWifiSignalView.setImageResource(R.drawable.ic_wifi_signal_4);
                break;
            default:
                break;
        }
    }

    private void updateSSId() {
        mSSIDView.setText(mAccessPoint.ssid);
    }

    private void updateSecurityInfo() {
        if (shouldInputPassword()) {
            mWifiInfoView.setVisibility(View.GONE);
            mPasswordView.setVisibility(View.VISIBLE);
        } else {
            mWifiInfoView.setVisibility(View.VISIBLE);
            mPasswordView.setVisibility(View.GONE);
        }
    }

    private boolean shouldInputPassword() {
        boolean result = true;
        if (mAccessPoint.hasRememberPassword()) {
            mWifiInfoView.setText(R.string.wifi_connect_remember_info);
            result = false;
        }
        if (mAccessPoint.isOpenWifi()) {
            mWifiInfoView.setText(R.string.wifi_connect_open_info);
            result = false;
        }
        return result;
    }

    private void updateButton() {
        mConnectButton.setOnClickListener(this);
        mHaveConnectedButton.setOnClickListener(this);
        if (isConnecting()) {
            mConnectButton.setVisibility(View.GONE);
            mHaveConnectedButton.setVisibility(View.VISIBLE);
        } else {
            mHaveConnectedButton.setVisibility(View.GONE);
            mConnectButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isConnecting() {
        boolean result = false;
        String currentSSID = mWifiManager.getConnectionInfo().getSSID();
        if (TextUtils.equals(currentSSID, mAccessPoint.ssid)) {
            result = true;
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.titlebar_back_btn) {
            onBackPressed();
        } else if (id == R.id.wifi_connect_btn) {
            connectWifi();
        } else if (id == R.id.wifi_connect_internet_btn) {
            openBrower();
        }
    }

    private void connectWifi() {
        if (shouldInputPassword() && isNullPassword()) {
            Toast.makeText(this, R.string.wifi_connect_null_password,
                    Toast.LENGTH_SHORT).show();
        } else {
            int networkId = mAccessPoint.networkId;
            Logger.debug(TAG, "network id: " + networkId);

            String password = mPasswordEdit.getText().toString();
            Intent resturnIntent = new Intent();
            resturnIntent.putExtra(Constants.PASSWORD_KEY, password);
            setResult(RESULT_OK, resturnIntent);
            finish();
            // WifiConfiguration config = new WifiConfiguration();
            // config.networkId = networkId;
            // config.priority = 0;
            // mWifiManager.updateNetwork(config);
            // // Connect to network by disabling others.
            // mWifiManager.enableNetwork(networkId, true);
            // mWifiManager.reconnect();
            // // mResetNetworks = true;
            // onBackPressed();

        }
    }

    private boolean isNullPassword() {
        if (TextUtils.isEmpty(mPasswordEdit.getText().toString())) {
            return true;
        }
        return false;
    }

    private void openBrower() {
        String url = "http://www.591wifi.com";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
