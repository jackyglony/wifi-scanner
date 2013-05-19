package com.shixunaoyou.wifiscanner.wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shixunaoyou.wifiscanner.AccountSettingsDialog;
import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.hub.LoginUtils;
import com.shixunaoyou.wifiscanner.update.CheckUpdateAsyncTask;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Credentials;
import com.shixunaoyou.wifiscanner.util.KeyStore;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.UMengUtils;
import com.shixunaoyou.wifiscanner.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class WifiListActivity extends PreferenceActivity implements
        OnDismissListener, ConnectionInfoDialog.ActionListener,
        View.OnClickListener, WifiFilterDialog.OnWifiFilerChangeListener {

    public static final int FORGET_RESPONE = Activity.RESULT_FIRST_USER + 1;
    private static final String TAG = "WifiScannerActivity";

    private static final int CHECK_DETAIL_REQUEST = 1;

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;

    private WifiManager mWifiManager;
    private ProgressCategory mAccessPoints;

    private AccessPoint mSelectedAccessPoint;

    private DetailedState mLastState;
    private WifiInfo mLastInfo;
    private int mLastPriority;

    private boolean mResetNetworks = false;
    private int mKeyStoreNetworkId = Constants.INVALID_NETWORK_ID;
    // private Preference mLoginStatusPrefs;

    // indicate whether to connect a wifi ap.
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private WifiDialog mDialog;

    // private ListPreference mWifiFilterListPref;

    private Scanner mScanner;

    // First Check Status is result of Check Baidu
    private int mFirstCheckStatus = Constants.CANNOT_CONNECT;

    // Second Check Status is result of 591 WiFi
    private boolean mSecondCheckStatus = false;;
    private boolean mIsResumed = false;

    private Context mContext;

    private View mLoginStatusContainer;
    private TextView mFilterStatusView;
    private View mFilterStatusContainer;
    private TextView mLoginStatusView;
    private String mFilterStatus[];

    private int mAlertDialogWidth;
    private int mAlertDialogHeight;

    private ServiceNotificationHandler mNotificationHandler;

    public WifiListActivity() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };

        mScanner = new Scanner();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (getIntent().getBooleanExtra("only_access_points", false)) {
            addPreferencesFromResource(R.xml.wifi_access_points);
        } else {
            addPreferencesFromResource(R.xml.wifi_settings);
        }
        // mLoginStatusPrefs = this.findPreference(Constants.LOGIN_STATUS_KEY);

        mAccessPoints = (ProgressCategory) findPreference("access_points");
        mAccessPoints.setOrderingAsAdded(false);

        // mWifiFilterListPref = (ListPreference)
        // findPreference("wifi_filter_mode");
        mFilterStatus = this.getResources().getStringArray(
                R.array.wifi_filter_mode_summary_entries);

        initHeaderView();

        autoCheckUpdate();
        mAlertDialogWidth = (int) (Utils.getActivityDisplayWidth(this) * 0.9);
        mAlertDialogHeight = (int) (Utils.getActivityDisplayHeight(this) * 0.8);
        mNotificationHandler = ServiceNotificationHandler.getInstance(this);
    }

    private void initHeaderView() {
        LayoutInflater inflator = LayoutInflater.from(this);
        View headView = inflator.inflate(R.layout.wifi_list_header, null);
        getListView().addHeaderView(headView);
        mLoginStatusContainer = headView
                .findViewById(R.id.wifi_list_header_login_container);
        mFilterStatusView = (TextView) headView
                .findViewById(R.id.wifi_list_header_filter_status);
        mFilterStatusContainer = headView
                .findViewById(R.id.wifi_list_filter_containter);
        mLoginStatusView = (TextView) headView
                .findViewById(R.id.wifi_list_header_login_status);
        mFilterStatusView.setText(mFilterStatus[Utils.getFilterMode(this)]);

        mFilterStatusContainer.setOnClickListener(this);
        mLoginStatusContainer.setOnClickListener(this);
    }

    private void autoCheckUpdate() {
        CheckUpdateAsyncTask task = new CheckUpdateAsyncTask(this, null, false);
        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, mFilter);
        if (mKeyStoreNetworkId != Constants.INVALID_NETWORK_ID
                && KeyStore.getInstance().test() == KeyStore.NO_ERROR) {
            connect(mKeyStoreNetworkId);
        }
        Intent intentService = new Intent(this, WiFiScanService.class);
        this.stopService(intentService);
        mKeyStoreNetworkId = Constants.INVALID_NETWORK_ID;
        Logger.debug(TAG, "OnResume: " + Utils.getIsServiceChangeStatus(this)
                + "mIsResumed:" + mIsResumed);
        if (!mIsResumed || Utils.getIsServiceChangeStatus(this)) {
            if (!Utils.isHasWifiConnection(this)) {
                Utils.setLoginStatus(this, Constants.NO_WIFI);
                Utils.setIsServiceUpdate(this, false);
                updateView();
            } else {
                // Check network
                WifiFirstCheckStatus task = new WifiFirstCheckStatus(true);
                // WifiSecondCheckStatus task = new WifiSecondCheckStatus(this);
                if (Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
        }
        mConnected.set(Utils.isHasWifiConnection(this));
        updateNotification();
        mIsResumed = true;
        updateAccessPoints();
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService();
        unregisterReceiver(mReceiver);
        mScanner.pause();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (mResetNetworks) {
            enableNetworks();
        }
    }

    private void startService() {
        // if we enable auto login, we start the service when we exit the view
        if (Utils.getEnableAutoLogin(this)) {
            Logger.debug(TAG, "OnPause: ready to start service");
            Intent intentService = new Intent(this, WiFiScanService.class);
            this.startService(intentService);
        }
    }

    @Override
    protected void onDestroy() {
        Logger.debug(TAG, "onDestroy");
        // if we enable auto login, we start the service when we exit the view
        startService();
        logoutIfAccountIsDeleted();
        super.onDestroy();
    }

    private void logoutIfAccountIsDeleted() {
        if (isAccountIsDeleted()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LoginUtils.getInstance(mContext).logoutHub();
                }

            }).start();
        }
    }

    private boolean isAccountIsDeleted() {
        boolean result = false;
        if (TextUtils.isEmpty(Utils.getUserName(this))
                && Utils.getLoginStatus(this) == Constants.HAVE_LOGIN) {
            result = true;
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen,
            Preference preference) {
        if (preference instanceof AccessPoint) {
            mSelectedAccessPoint = (AccessPoint) preference;
            // showDialog(mSelectedAccessPoint, false);
            Intent intent = new Intent(this, WifiConnectActivity.class);
            intent.putExtra(Constants.SSID_KEY, mSelectedAccessPoint.ssid);
            this.startActivityForResult(intent, CHECK_DETAIL_REQUEST);
        } else if (TextUtils.equals(preference.getKey(),
                Constants.LOGIN_STATUS_KEY)) {
            int status = Utils.getLoginStatus(this);
            if (status == Constants.HAVE_LOGIN) {
                ConnectionInfoDialog connectionInfoDlg = new ConnectionInfoDialog(
                        this, this);
                connectionInfoDlg.show();
            }
            if (status == Constants.HAVE_LOGOUT
                    || status == Constants.LOGIN_FALLURE) {
                login();
            }
            if (status == Constants.STATUS_UNKOWN) {
                WifiFirstCheckStatus task = new WifiFirstCheckStatus(true);
                if (Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == CHECK_DETAIL_REQUEST) {
            Logger.debug(TAG, "onActivityResult");
            String password = getPassword(data);
            connectWifi(password);
        } else if (resultCode == FORGET_RESPONE) {
            forget(mSelectedAccessPoint.networkId);
        }
    }

    private String getPassword(Intent data) {
        String password = null;
        password = data.getExtras().getString(Constants.PASSWORD_KEY);
        return password;
    }

    private void connectWifi(String password) {
        WifiConfiguration config = getConfiguration(password);
        if (config == null) {
            if (mSelectedAccessPoint != null
                    && !requireKeyStore(mSelectedAccessPoint.getConfig())) {
                connect(mSelectedAccessPoint.networkId);
            }
        } else if (config.networkId != Constants.INVALID_NETWORK_ID) {
            if (mSelectedAccessPoint != null) {
                mWifiManager.updateNetwork(config);
                saveNetworks();
            }
        } else {
            int networkId = mWifiManager.addNetwork(config);
            if (networkId != Constants.INVALID_NETWORK_ID) {
                mWifiManager.enableNetwork(networkId, false);
                config.networkId = networkId;
                if (requireKeyStore(config)) {
                    saveNetworks();
                } else {
                    connect(networkId);
                }
            }
        }
    }

    private WifiConfiguration getConfiguration(String password) {
        if (mSelectedAccessPoint != null
                && mSelectedAccessPoint.networkId != Constants.INVALID_NETWORK_ID) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        if (mSelectedAccessPoint == null) {
            throw new IllegalArgumentException();
        }
        if (mSelectedAccessPoint.networkId == Constants.INVALID_NETWORK_ID) {
            config.SSID = AccessPoint
                    .convertToQuotedString(mSelectedAccessPoint.ssid);
        } else {
            config.networkId = mSelectedAccessPoint.networkId;
        }

        switch (mSelectedAccessPoint.security) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (password != null && password.length() != 0) {
                    int length = password.length();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;
            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (password != null && password.length() != 0) {
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;
            case AccessPoint.SECURITY_EAP:
            default:
                config = null;
                break;
        }
        return config;
    }

    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            Logger.debug(TAG, "status: SCAN_RESULTS_AVAILABLE_ACTION");
            updateAccessPoints();
        } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
            if (mSelectedAccessPoint != null
                    && mSelectedAccessPoint.networkId != Constants.INVALID_NETWORK_ID) {
                mSelectedAccessPoint = null;
            }
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            if (!mConnected.get()) {
                updateConnectionState(WifiInfo
                        .getDetailedStateOf((SupplicantState) intent
                                .getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean lastStatus = mConnected.get();
            mConnected.set(info.isConnected());
            if (!info.isConnected()) {
                Utils.setLoginStatus(this, Constants.NO_WIFI);
                Utils.setIsServiceUpdate(this, false);
                updateView();
            }
            updateNotification();
            if (!lastStatus && mConnected.get()) {
                WifiFirstCheckStatus task = new WifiFirstCheckStatus(true);
                if (Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
            updateConnectionState(info.getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }

    private void updateNotification() {
        if (!mConnected.get()) {
            mNotificationHandler.sendNotification();
            mNotificationHandler
                    .updateNotificationMessage(
                            getString(R.string.wifi_service_notificaion_no_wifi),
                            false);
            mNotificationHandler.updateNotificationHotWord(true);
        } else {
            WifiInfo info = mWifiManager.getConnectionInfo();
            String ssid = info.getSSID();
            mNotificationHandler.updateNotificationMessage(
                    getString(R.string.wifi_service_notificaion_connect_wifi,
                            ssid), false);
        }
    }

    private void updateLoginNotification() {
        WifiInfo info = mWifiManager.getConnectionInfo();
        String ssid = info.getSSID();
        mNotificationHandler.updateNotificationMessage(
                getString(R.string.wifi_service_notification_login_wifi, ssid),
                false);
    }

    private void enableNetworks() {
        for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
            WifiConfiguration config = ((AccessPoint) mAccessPoints
                    .getPreference(i)).getConfig();
            if (config != null && config.status != Status.ENABLED) {
                mWifiManager.enableNetwork(config.networkId, false);
            }
        }

        mResetNetworks = false;
    }

    private void saveNetworks() {
        // Always save the configuration with all networks enabled.
        enableNetworks();
        mWifiManager.saveConfiguration();
        updateAccessPoints();
    }

    private void updateAccessPoints() {
        updateAccessPoints(Utils.getFilterMode(this));
    }

    private void updateAccessPoints(int valueOfFilter) {
        Logger.debug(TAG, "updateAccessPoints");
        mAccessPoints.removeAll();
        switch (valueOfFilter) {
            case Constants.FILTER_MODE_VISIBLE:
                addVisibleAcessPoints();
                break;
            case Constants.FILTER_MODE_ALL:
                addAllAccessPoints();
                break;
            case Constants.FILTER_MODE_OPEN:
                addOpenAccessPoints();
                break;
            case Constants.FILTER_MODE_AUTHORIZE:
                addAuthorizeAssessPoints();
                break;
        }
    }

    private void addAuthorizeAssessPoints() {
        List<AccessPoint> allPoints = getAllAccessPoints();
        for (AccessPoint accessPoint : allPoints) {
            if (accessPoint.getLevel() != Constants.NOT_IN_RANGE
                    && accessPoint.security != AccessPoint.SECURITY_NONE) {
                mAccessPoints.addPreference(accessPoint);
            }
        }
    }

    private void addOpenAccessPoints() {
        List<AccessPoint> allPoints = getAllAccessPoints();
        for (AccessPoint accessPoint : allPoints) {
            if (accessPoint.getLevel() != Constants.NOT_IN_RANGE
                    && accessPoint.security == AccessPoint.SECURITY_NONE) {
                mAccessPoints.addPreference(accessPoint);
            }
        }
    }

    private void addAllAccessPoints() {
        List<AccessPoint> allPoints = getAllAccessPoints();
        for (AccessPoint accessPoint : allPoints) {
            mAccessPoints.addPreference(accessPoint);
        }
    }

    private void addVisibleAcessPoints() {
        List<AccessPoint> allPoints = getAllAccessPoints();
        for (AccessPoint accessPoint : allPoints) {
            if (accessPoint.getLevel() != Constants.NOT_IN_RANGE) {
                mAccessPoints.addPreference(accessPoint);
            }
        }
    }

    private List<AccessPoint> getAllAccessPoints() {
        List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            mLastPriority = 0;
            for (WifiConfiguration config : configs) {
                if (config.priority > mLastPriority) {
                    mLastPriority = config.priority;
                }

                // Shift the status to make enableNetworks() more efficient.
                if (config.status == Status.CURRENT) {
                    config.status = Status.ENABLED;
                } else if (mResetNetworks && config.status == Status.DISABLED) {
                    config.status = Status.CURRENT;
                }

                AccessPoint accessPoint = new AccessPoint(this, config);
                accessPoint.update(mLastInfo, mLastState);
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

    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (state == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
            ((AccessPoint) mAccessPoints.getPreference(i)).update(mLastInfo,
                    mLastState);
        }

        if (mResetNetworks
                && (state == DetailedState.CONNECTED
                        || state == DetailedState.DISCONNECTED || state == DetailedState.FAILED)) {
            updateAccessPoints();
            enableNetworks();
        }
    }

    private void updateWifiState(int state) {
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            mScanner.resume();
            updateAccessPoints();
        } else {
            mScanner.pause();
            mAccessPoints.removeAll();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        String name = Utils.getUserName(this);
        if (!TextUtils.isEmpty(name)) {
            WiFiLoginTask loginTask = new WiFiLoginTask(this);
            if (Build.VERSION.SDK_INT >= 11) {
                loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                loginTask.execute();
            }
        }
    }

    private void forget(int networkId) {
        mWifiManager.removeNetwork(networkId);
        saveNetworks();
    }

    private void connect(int networkId) {
        if (networkId == Constants.INVALID_NETWORK_ID) {
            return;
        }

        // Reset the priority of each network if it goes too high.
        if (mLastPriority > 1000000) {
            for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
                AccessPoint accessPoint = (AccessPoint) mAccessPoints
                        .getPreference(i);
                if (accessPoint.networkId != Constants.INVALID_NETWORK_ID) {
                    WifiConfiguration config = new WifiConfiguration();
                    config.networkId = accessPoint.networkId;
                    config.priority = 0;
                    mWifiManager.updateNetwork(config);
                }
            }
            mLastPriority = 0;
        }

        // Set to the highest priority and save the configuration.
        WifiConfiguration config = new WifiConfiguration();
        config.networkId = networkId;
        config.priority = ++mLastPriority;
        mWifiManager.updateNetwork(config);
        saveNetworks();

        // Connect to network by disabling others.
        mWifiManager.disconnect();
        mWifiManager.enableNetwork(networkId, true);
        boolean result = mWifiManager.reconnect();

        Logger.debug(TAG, "result = " + result);
        mResetNetworks = true;
    }

    private boolean requireKeyStore(WifiConfiguration config) {
        if (WifiDialog.requireKeyStore(config)
                && KeyStore.getInstance().test() != KeyStore.NO_ERROR) {
            mKeyStoreNetworkId = config.networkId;
            Credentials.getInstance().unlock(this);
            return true;
        }
        return false;
    }

    private void updateView() {
        Logger.debug(TAG, "updateView");
        int status = Utils.getLoginStatus(this);
        int resId = Utils.getResIdofStatus(status);

        if (status == Constants.LOGIN_FALLURE) {
            mLoginStatusView.setText(Utils.getShowErrorMessage(this));
        } else {
            mLoginStatusView.setText(resId);
        }
        if (status == Constants.HAVE_LOGIN || status == Constants.HAVE_LOGOUT
                || status == Constants.LOGIN_FALLURE
                || status == Constants.STATUS_UNKOWN) {
            // mLoginStatusPrefs.setEnabled(true);
        }
        // else {
        // mLoginStatusPrefs.setEnabled(false);
        // }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void doAction(int action) {
        if (action == Constants.LOGOUT_ACTION) {
            logout();
        } else if (action == Constants.LOGIN_ACTION) {
            login();
        }

    }

    private void logout() {
        LogoutTask task = new LogoutTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
        MobclickAgent.onEvent(this, UMengUtils.EVENT_LOGOUT_SUCCESS);
    }

    private void login() {
        String name = Utils.getUserName(this);
        if (TextUtils.isEmpty(name)) {
            AccountSettingsDialog dialog = new AccountSettingsDialog(this,
                    this, true);
            dialog.show();
            dialog.getWindow().setLayout(mAlertDialogWidth, mAlertDialogHeight);
        } else {
            WiFiLoginTask loginTask = new WiFiLoginTask(this);
            if (Build.VERSION.SDK_INT >= 11) {
                loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                loginTask.execute();
            }
        }

    }

    @Override
    public void onWifiFilterChanged() {
        updateFilter();
    }

    private void updateFilter() {
        int filterMode = Utils.getFilterMode(this);
        mFilterStatusView.setText(mFilterStatus[filterMode]);
        updateAccessPoints(filterMode);
    }

    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // if (which == WifiDialog.BUTTON_FORGET && mSelectedAccessPoint != null) {
    // forget(mSelectedAccessPoint.networkId);
    // } else if (which == WifiDialog.BUTTON_SUBMIT && mDialog != null) {
    // WifiConfiguration config = mDialog.getConfig();
    //
    // if (config == null) {
    // if (mSelectedAccessPoint != null
    // && !requireKeyStore(mSelectedAccessPoint.getConfig())) {
    // connect(mSelectedAccessPoint.networkId);
    // }
    // } else if (config.networkId != Constants.INVALID_NETWORK_ID) {
    // if (mSelectedAccessPoint != null) {
    // mWifiManager.updateNetwork(config);
    // saveNetworks();
    // }
    // } else {
    // int networkId = mWifiManager.addNetwork(config);
    // if (networkId != Constants.INVALID_NETWORK_ID) {
    // mWifiManager.enableNetwork(networkId, false);
    // config.networkId = networkId;
    // if (mDialog.edit || requireKeyStore(config)) {
    // saveNetworks();
    // } else {
    // connect(networkId);
    // }
    // }
    // }
    // }
    // }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.wifi_list_filter_containter) {
            WifiFilterDialog dlg = new WifiFilterDialog(this, this);
            dlg.show();
        } else if (id == R.id.wifi_list_header_login_container) {
            ConnectionInfoDialog dlg = new ConnectionInfoDialog(this, this);
            // dlg.setWifiInfo(null, signalLevel);
            dlg.show();
            dlg.getWindow().setLayout(mAlertDialogWidth, mAlertDialogHeight);
        }
    }

    @SuppressLint("HandlerLeak")
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

    public class LogoutTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... param) {
            Logger.debug(TAG, " start to logout");
            LoginUtils.getInstance(mContext).logoutHub();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Utils.setLoginStatus(mContext, Constants.HAVE_LOGOUT);
            Toast.makeText(WifiListActivity.this,
                    R.string.wifi_logout_success_toast, Toast.LENGTH_SHORT)
                    .show();
            // mLoginStatusPrefs.setEnabled(true);
            Utils.setIsServiceUpdate(WifiListActivity.this, false);
            updateView();
            Logger.debug(TAG, " ready to logout");
        }

        @Override
        protected void onPreExecute() {
            mLoginStatusView.setText(R.string.wifi_logouting_status);
        }
    }

    public class WiFiLoginTask extends LoginTask {
        WiFiLoginTask(Context context) {
            super(context);
        }

        @Override
        public void onPostExecute(String result) {
            Logger.debug(TAG, "=========Connect Status: " + result
                    + " ==========");
            super.onPostExecute(result);
            if (result == null) {
                // DoubleCheck task = new DoubleCheck();
                // task.execute();
                Logger.debug(TAG, "DoubleCheck: " + result);
                Toast.makeText(WifiListActivity.this,
                        R.string.wifi_notification_login_successful,
                        Toast.LENGTH_SHORT).show();
                // mLoginStatusPrefs.setEnabled(true);
                Utils.setIsServiceUpdate(WifiListActivity.this, false);
            }
            updateView();
        }

        @Override
        protected void onPreExecute() {
            mLoginStatusView.setText(R.string.wifi_loging_status);
        }
    }

    class WifiFirstCheckStatus extends AsyncTask<Void, Void, Integer> {
        private boolean mIsLogin;

        WifiFirstCheckStatus(boolean isLogin) {
            super();
            mIsLogin = isLogin;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return Utils.checkTestUrl(mContext);
        }

        @Override
        protected void onPreExecute() {
            Logger.debug(TAG, "start to check baidu");
            mLoginStatusView.setText(R.string.wifi_checking_status);
        }

        @Override
        public void onPostExecute(Integer result) {
            Logger.debug(TAG, "WifiFirstCheckStatus: " + result);
            mFirstCheckStatus = result;
            if (mFirstCheckStatus == Constants.CANNOT_CONNECT
                    || mFirstCheckStatus == Constants.NOT_FIND_SERVER) {
                Utils.setLoginStatus(mContext, mFirstCheckStatus);
                Utils.setIsServiceUpdate(WifiListActivity.this, false);
                updateView();
            } else if (mFirstCheckStatus == Constants.CONNECTED) {
                WifiSecondCheckStatus secondCheck = null;
                MobclickAgent.onEvent(mContext,
                        UMengUtils.EVENT_CONNECT_INTERNET);

                secondCheck = new WifiSecondCheckStatus(mContext);
                if (Build.VERSION.SDK_INT >= 11) {
                    secondCheck
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    secondCheck.execute();
                }
            } else if (mFirstCheckStatus == Constants.HAVE_LOGOUT) {
                Logger.debug(TAG, "WifiFirstCheckStatus: " + "Can Login now");
                Utils.setLoginStatus(mContext, mFirstCheckStatus);
                Utils.setIsServiceUpdate(mContext, false);
                MobclickAgent.onEvent(mContext,
                        UMengUtils.EVENT_CONNECT_TO_SERVER);

                updateView();
                if (mIsLogin) {
                    Logger.debug(TAG, "Will login automtically!!");
                    if (!TextUtils.isEmpty(Utils.getUserName(mContext))) {
                        WiFiLoginTask loginTask = new WiFiLoginTask(mContext);
                        if (Build.VERSION.SDK_INT >= 11) {
                            loginTask
                                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            loginTask.execute();
                        }
                    }
                }
            }
        }
    }

    class WifiSecondCheckStatus extends AsyncTask<Void, Void, Boolean> {
        private Context mMyConext;

        WifiSecondCheckStatus(Context context) {
            mMyConext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return Utils.check591Server(mMyConext);
        }

        @Override
        protected void onPreExecute() {
            mLoginStatusView.setText(R.string.wifi_second_checking_satus);
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
            Utils.setIsServiceUpdate(WifiListActivity.this, false);
            updateView();
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
                Toast.makeText(WifiListActivity.this,
                        R.string.wifi_notification_login_successful,
                        Toast.LENGTH_SHORT).show();
                updateLoginNotification();
            } else if (result == Constants.HAVE_LOGOUT) {
                Toast.makeText(WifiListActivity.this,
                        R.string.wifi_change_account_toast, Toast.LENGTH_SHORT)
                        .show();
            }
            // mLoginStatusPrefs.setEnabled(true);
            Utils.setLoginStatus(mContext, result);
            Utils.setIsServiceUpdate(WifiListActivity.this, false);
            updateView();
        }
    }
}
