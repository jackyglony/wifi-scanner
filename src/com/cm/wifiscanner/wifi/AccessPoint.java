package com.cm.wifiscanner.wifi;

import com.cm.wifiscanner.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class AccessPoint extends Preference {
    static final String TAG = "AccessPoint";

    private static final String KEY_DETAILEDSTATE = "key_detailedstate";
    private static final String KEY_WIFIINFO = "key_wifiinfo";
    private static final String KEY_SCANRESULT = "key_scanresult";
    private static final String KEY_CONFIG = "key_config";

    private static final int INVALID_NETWORK_ID = -1;

    /** These values are matched in string arrays -- changes must be kept in sync */
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public String ssid;
    public String bssid;
    public int security;
    public int networkId;
    boolean wpsAvailable = false;

    PskType pskType = PskType.UNKNOWN;

    private WifiConfiguration mConfig;
    /* package */ScanResult mScanResult;

    private int mLevel;
    private WifiInfo mInfo;
    private DetailedState mState;

    private ImageView mSignalView;
    private ImageView mWifiSecurity;
    private TextView mSsidNameView;

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
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

    public String getSecurityString(boolean concise) {
        Context context = getContext();
        switch(security) {
            case SECURITY_EAP:
                return concise ? context.getString(R.string.wifi_security_short_eap) :
                    context.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch (pskType) {
                    case WPA:
                        return concise ? context.getString(R.string.wifi_security_short_wpa) :
                            context.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa2) :
                            context.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa_wpa2) :
                            context.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? context.getString(R.string.wifi_security_short_psk_generic)
                                : context.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return concise ? context.getString(R.string.wifi_security_short_wep) :
                    context.getString(R.string.wifi_security_wep);
            case SECURITY_NONE:
            default:
                return concise ? "" : context.getString(R.string.wifi_security_none);
        }
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    public AccessPoint(Context context, WifiConfiguration config) {
        super(context);
//        setWidgetLayoutResource(R.layout.preference_widget_signal);
//        this.setLayoutResource(R.layout.access_point_layout);
        onCreateView();
        loadConfig(config);
        refresh();
    }

    public AccessPoint(Context context, ScanResult result) {
        super(context);
//        setWidgetLayoutResource(R.layout.preference_widget_signal);
        onCreateView();
        loadResult(result);
        refresh();
    }

    public AccessPoint(Context context, Bundle savedState) {
        super(context);
//        setWidgetLayoutResource(R.layout.preference_widget_signal);
        onCreateView();
        mConfig = savedState.getParcelable(KEY_CONFIG);
        if (mConfig != null) {
            loadConfig(mConfig);
        }
        mScanResult = (ScanResult) savedState.getParcelable(KEY_SCANRESULT);
        if (mScanResult != null) {
            loadResult(mScanResult);
        }
        mInfo = (WifiInfo) savedState.getParcelable(KEY_WIFIINFO);
        if (savedState.containsKey(KEY_DETAILEDSTATE)) {
            mState = DetailedState.valueOf(savedState.getString(KEY_DETAILEDSTATE));
        }
        update(mInfo, mState);
    }

    private void onCreateView() {
        this.setLayoutResource(R.layout.access_point_layout);
    }

    public void saveWifiState(Bundle savedState) {
        savedState.putParcelable(KEY_CONFIG, mConfig);
        savedState.putParcelable(KEY_SCANRESULT, mScanResult);
        savedState.putParcelable(KEY_WIFIINFO, mInfo);
        if (mState != null) {
            savedState.putString(KEY_DETAILEDSTATE, mState.toString());
        }
    }

    private void loadConfig(WifiConfiguration config) {
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        bssid = config.BSSID;
        security = getSecurity(config);
        networkId = config.networkId;
        mLevel = Integer.MAX_VALUE;
        mConfig = config;
    }

    private void loadResult(ScanResult result) {
        ssid = result.SSID;
        bssid = result.BSSID;
        security = getSecurity(result);
        wpsAvailable = security != SECURITY_EAP && result.capabilities.contains("WPS");
        if (security == SECURITY_PSK)
            pskType = getPskType(result);
        networkId = -1;
        mLevel = result.level;
        mScanResult = result;
    }

    @Override
    protected void onBindView(View view) {
//        setIcon(R.drawable.ic_wifi_lock_signal_4);
        ImageView signal = (ImageView) view.findViewById(R.id.access_point_singal);
        mSsidNameView = (TextView)view.findViewById(R.id.access_point_ssid);
        mWifiSecurity = (ImageView)view.findViewById(R.id.access_point_security);
        if (mLevel == Integer.MAX_VALUE) {
            signal.setImageDrawable(null);
        } else {
            final int level = getLevel();
            final boolean isOpen = security == SECURITY_NONE;
            if (isOpen) {
                mWifiSecurity.setImageResource(R.drawable.access_point_open);
            } else {
                mWifiSecurity.setImageResource(R.drawable.access_point_lock);
            }
            switch (level) {
                case 0:
//                    if (lock)
                        signal.setImageResource(R.drawable.ic_wifi_signal_1);
//                    else
//                        signal.setImageResource(R.drawable.ic_wifi_lock_signal_1);
                    break;
                case 1:
//                    if (lock)
                        signal.setImageResource(R.drawable.ic_wifi_signal_2);
//                    else
//                        signal.setImageResource(R.drawable.ic_wifi_lock_signal_2);
                    break;
                case 2:
//                    if (lock)
                        signal.setImageResource(R.drawable.ic_wifi_signal_3);
//                    else
//                        signal.setImageResource(R.drawable.ic_wifi_lock_signal_3);
                    break;
                case 3:
//                    if (lock)
                        signal.setImageResource(R.drawable.ic_wifi_signal_4);
//                    else
//                        signal.setImageResource(R.drawable.ic_wifi_lock_signal_4);
                    break;
                default:
                    break;
            }
        }

        refresh();
        super.onBindView(view);
    }

    @Override
    public int compareTo(Preference preference) {
        if (!(preference instanceof AccessPoint)) {
            return 1;
        }
        AccessPoint other = (AccessPoint) preference;
        // Active one goes first.
        if (mInfo != other.mInfo) {
            return (mInfo != null) ? -1 : 1;
        }
        // Reachable one goes before unreachable one.
        if ((mLevel ^ other.mLevel) < 0) {
            return (mLevel != Integer.MAX_VALUE) ? -1 : 1;
        }
        // Configured one goes before unconfigured one.
        if ((networkId ^ other.networkId) < 0) {
            return (networkId != -1) ? -1 : 1;
        }
        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(other.mLevel, mLevel);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(other.ssid);
    }

    public boolean update(ScanResult result) {
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mLevel) > 0) {
                int oldLevel = getLevel();
                mLevel = result.level;
                if (getLevel() != oldLevel) {
                    notifyChanged();
                }
            }
            // This flag only comes from scans, is not easily saved in config
            if (security == SECURITY_PSK) {
                pskType = getPskType(result);
            }
            refresh();
            return true;
        }
        return false;
    }

    public void update(WifiInfo info, DetailedState state) {
        boolean reorder = false;
        if (info != null && networkId != INVALID_NETWORK_ID
                && networkId == info.getNetworkId()) {
            reorder = (mInfo == null);
            mLevel = info.getRssi();
            mInfo = info;
            mState = state;
            refresh();
        } else if (mInfo != null) {
            reorder = true;
            mInfo = null;
            mState = null;
            refresh();
        }
        if (reorder) {
            notifyHierarchyChanged();
        }
    }

    public int getLevel() {
        if (mLevel == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mLevel, 4);
    }

    public WifiConfiguration getConfig() {
        return mConfig;
    }

    public WifiInfo getInfo() {
        return mInfo;
    }

    public DetailedState getState() {
        return mState;
    }

    public static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    /** Updates the title and summary; may indirectly call notifyChanged()  */
    private void refresh() {
        setTitle(ssid);

//        Context context = getContext();
//        if (mState != null) { // This is the active connection
//            setSummary(Summary.get(context, mState));
//        } else if (mLevel == Integer.MAX_VALUE) { // Wifi out of range
//            setSummary(context.getString(R.string.wifi_not_in_range));
//        } else if (mConfig != null && mConfig.status == WifiConfiguration.Status.DISABLED) {
//            setSummary(context.getString(R.string.wifi_disabled_generic));
//        } else { // In range, not disabled.
//            StringBuilder summary = new StringBuilder();
//            if (mConfig != null) { // Is saved network
//                summary.append(context.getString(R.string.wifi_remembered));
//            }
//
//            if (security != SECURITY_NONE) {
//                String securityStrFormat;
//                if (summary.length() == 0) {
//                    securityStrFormat = context.getString(R.string.wifi_secured_first_item);
//                } else {
//                    securityStrFormat = context.getString(R.string.wifi_secured_second_item);
//                }
//                summary.append(String.format(securityStrFormat, getSecurityString(true)));
//            }
//
//            if (mConfig == null && wpsAvailable) { // Only list WPS available for unsaved networks
//                if (summary.length() == 0) {
//                    summary.append(context.getString(R.string.wifi_wps_available_first_item));
//                } else {
//                    summary.append(context.getString(R.string.wifi_wps_available_second_item));
//                }
//            }
//            setSummary(summary.toString());
//        }
    }

    /**
     * Generate and save a default wifiConfiguration with common values.
     * Can only be called for unsecured networks.
     * @hide
     */
    public void generateOpenNetworkConfig() {
        if (security != SECURITY_NONE)
            throw new IllegalStateException();
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = AccessPoint.convertToQuotedString(ssid);
        mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mSsidNameView != null) {
            mSsidNameView.setText(title);
        }
    }
}

