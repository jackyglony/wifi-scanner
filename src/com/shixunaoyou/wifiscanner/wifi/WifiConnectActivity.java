package com.shixunaoyou.wifiscanner.wifi;

import java.util.List;

import com.shixunaoyou.wifiscanner.BaseCustomActivity;
import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WifiConnectActivity extends BaseCustomActivity {

    private static final String TAG = "WifiConnectActivity";

    private View mPasswordView;
    private Button mConnectButton;
    private TextView mSSIDView;
    private ImageView mWifiSignalView;
    private AccessPoint mAccessPoint;
    private TextView mWifiInfoView;
    private EditText mPasswordEdit;
    private Button mHaveConnectedButton;
    private Button mForgetButton;
    private ViewGroup mConnectInfoContainer;

    private WifiManager mWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        getViews();
        parseIntentAndInitiateView();
    }

    private void getViews() {
        mPasswordView = findViewById(R.id.wifi_connect_password_parent);
        mConnectButton = (Button) findViewById(R.id.wifi_connect_btn);
        mSSIDView = (TextView) findViewById(R.id.wifi_connect_ssid);
        mWifiSignalView = (ImageView) findViewById(R.id.wifi_connect_signal);
        mWifiInfoView = (TextView) findViewById(R.id.wifi_connect_info);
        mPasswordEdit = (EditText) findViewById(R.id.wifi_connect_password_edit);
        mHaveConnectedButton = (Button) findViewById(R.id.wifi_connect_internet_btn);
        mForgetButton = (Button) findViewById(R.id.wifi_connect_forget_btn);
        mConnectInfoContainer = (ViewGroup) findViewById(R.id.wifi_connect_info_container);
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
        updateConnectInfo();
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
        mSSIDView.setText(Utils.getOperatorWifiName(mAccessPoint.ssid));
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
        mForgetButton.setOnClickListener(this);
        if (isConnecting()) {
            mConnectButton.setVisibility(View.GONE);
            mHaveConnectedButton.setVisibility(View.VISIBLE);
        } else {
            mHaveConnectedButton.setVisibility(View.GONE);
            mConnectButton.setVisibility(View.VISIBLE);
        }

        if (mAccessPoint.getConfig() != null) {
            mForgetButton.setVisibility(View.VISIBLE);
        } else {
            mForgetButton.setVisibility(View.GONE);
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

    private void updateConnectInfo() {
        addBSSIDInfo();
        addSecurityInfo();
        addSpeedAndIPInfo();
        addSignalInfo();
        addChannelInfo();
    }

    private void addBSSIDInfo() {
        String bssid = mAccessPoint.bssid;
        if (TextUtils.isEmpty(bssid)) {
            WifiInfo info = mAccessPoint.getInfo();
            if (info != null) {
                bssid = info.getBSSID();
            }
        }
        if (!TextUtils.isEmpty(bssid)) {
            addRow(mConnectInfoContainer, R.string.wifi_connect_signal_bssid,
                    bssid);
        }
    }

    private void addSecurityInfo() {
        String[] type = this.getResources().getStringArray(
                R.array.wifi_security);
        addRow(mConnectInfoContainer, R.string.wifi_security,
                type[mAccessPoint.security]);
    }

    @SuppressWarnings("deprecation")
    private void addSpeedAndIPInfo() {
        WifiInfo info = mAccessPoint.getInfo();
        if (info != null) {
            addRow(mConnectInfoContainer, R.string.wifi_speed,
                    info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
            int address = info.getIpAddress();
            if (address != 0) {
                addRow(mConnectInfoContainer, R.string.wifi_ip_address,
                        Formatter.formatIpAddress(address));
                DhcpInfo dhcp = mWifiManager.getDhcpInfo();
                String netMask = Formatter.formatIpAddress(dhcp.netmask);
                String dns1 = Formatter.formatIpAddress(dhcp.dns1);
                String dns2 = Formatter.formatIpAddress(dhcp.dns2);
                String gateway = Formatter.formatIpAddress(dhcp.gateway);
                addRow(mConnectInfoContainer, R.string.wifi_connect_gateway,
                        gateway);
                addRow(mConnectInfoContainer, R.string.wifi_connect_netmask,
                        netMask);
                addRow(mConnectInfoContainer, R.string.wifi_connect_dns1, dns1);
                addRow(mConnectInfoContainer, R.string.wifi_connect_dns2, dns2);
            }
        }
    }

    private void addSignalInfo() {
        int level = mAccessPoint.getRawLevel();
        if (level != -1) {
            String signal = getString(
                    R.string.wifi_connect_signal_dbm_percentage, level,
                    Utils.getPercentageOfdBm(level));
            addRow(mConnectInfoContainer, R.string.wifi_signal, signal);
        }
    }

    private void addChannelInfo() {
        ScanResult scanResult = mAccessPoint.mScanResult;
        if (scanResult != null) {
            int channel = Utils.getChannelFromFrequency(scanResult.frequency);
            addRow(mConnectInfoContainer, R.string.wifi_connect_channel_info,
                    String.valueOf(channel));
        }
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = getLayoutInflater().inflate(R.layout.wifi_connect_info_row,
                group, false);
        ((TextView) row.findViewById(R.id.wifi_connect_info_row_name))
                .setText(name);
        ((TextView) row.findViewById(R.id.wifi_connect_info_row_value))
                .setText(value);
        group.addView(row);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.wifi_connect_btn) {
            connectWifi();
        } else if (id == R.id.wifi_connect_internet_btn) {
            openBrower();
        } else if (id == R.id.wifi_connect_forget_btn) {
            forgetWifi();
        }
        super.onClick(v);
    }

    private void connectWifi() {
        boolean interpret = false;
        if (shouldInputPassword()) {
            if (isNullPassword()) {
                Toast.makeText(this, R.string.wifi_connect_null_password,
                        Toast.LENGTH_SHORT).show();
                interpret = true;
            } else if (mAccessPoint.isPSKWifi() && !isLengthBiggerEight()) {
                Toast.makeText(this, R.string.wifi_connect_too_short_password,
                        Toast.LENGTH_SHORT).show();
                interpret = true;
            }
        }
        if (interpret) {
            return;
        }
        if (mAccessPoint.isWrongPassworid()) {
            mWifiManager.removeNetwork(mAccessPoint.networkId);
        }
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

    private boolean isNullPassword() {
        if (TextUtils.isEmpty(mPasswordEdit.getText().toString())) {
            return true;
        }
        return false;
    }

    private boolean isLengthBiggerEight() {
        if (mPasswordEdit.getText().toString().length() >= 8) {
            return true;
        }
        return false;
    }

    private void openBrower() {
        String url = "http://m.591wifi.com";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void forgetWifi() {
        Intent resturnIntent = new Intent();
        setResult(WifiListActivity.FORGET_RESPONE, resturnIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    protected int getCustomTitleResecouse() {
        return R.string.wifi_connect_title;
    }

    @Override
    protected int getLayoutRecourse() {
        return R.layout.wifi_connect_layout;
    }
}
