package com.cm.wifiscanner.wifi;

import com.cm.wifiscanner.AccountSettingsDialog;
import com.cm.wifiscanner.R;
import com.cm.wifiscanner.hub.LoginUtils;
import com.cm.wifiscanner.util.Constants;
import com.cm.wifiscanner.util.Credentials;
import com.cm.wifiscanner.util.KeyStore;
import com.cm.wifiscanner.util.Logger;
import com.cm.wifiscanner.util.Utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class WifiListActivity extends PreferenceActivity implements
        DialogInterface.OnClickListener, OnDismissListener, ConnectionInfoDialog.LogoutListener {

    private static final String TAG = "WifiScannerActivity";

    private static final int INVALID_NETWORK_ID = -1;

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;

    private WifiManager mWifiManager;
    private ProgressCategory mAccessPoints;

    private AccessPoint mSelectedAccessPoint;

    private DetailedState mLastState;
    private WifiInfo mLastInfo;
    private int mLastPriority;

    private boolean mResetNetworks = false;
    private int mKeyStoreNetworkId = INVALID_NETWORK_ID;
    private Preference mLoginStatusPrefs;

    // indicate whether to connect a wifi ap.
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private WifiDialog mDialog;
    private WifiEnabler mWifiEnabler;
    // private CheckBoxPreference mWifiFilter;

    private ListPreference mWifiFilterListPref;

    private Scanner mScanner;

    // First Check Status is result of Check Baidu
    private int mFirstCheckStatus = Constants.CANNOT_CONNECT;

    // Second Check Status is result of 591 WiFi
    private boolean mSecondCheckStatus = false;;
    private boolean mIsResumed = false;

    private Context mContext;

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

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (getIntent().getBooleanExtra("only_access_points", false)) {
            addPreferencesFromResource(R.xml.wifi_access_points);
        } else {
            addPreferencesFromResource(R.xml.wifi_settings);
            mWifiEnabler = new WifiEnabler(this,
                    (CheckBoxPreference) findPreference("enable_wifi"));
        }
        mLoginStatusPrefs = this.findPreference(Constants.LOGIN_STATUS_KEY);

        mAccessPoints = (ProgressCategory) findPreference("access_points");
        mAccessPoints.setOrderingAsAdded(false);

        // mWifiFilter = (CheckBoxPreference)
        // findPreference("filter_open_wifi");

        mWifiFilterListPref = (ListPreference) findPreference("wifi_filter_mode");
        initSummaryOfWifiFilterListPref();
        setListnerForWifiFilterPref();
        // updateFilterState();
        // this.getListView().setBackgroundColor(0xfff3f3f3);
        // this.getListView().setBackgroundResource(R.drawable.bg);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, mFilter);
        if (mKeyStoreNetworkId != INVALID_NETWORK_ID
                && KeyStore.getInstance().test() == KeyStore.NO_ERROR) {
            connect(mKeyStoreNetworkId);
        }

        if (mWifiEnabler != null) {
            mWifiEnabler.resume();
        }

        mKeyStoreNetworkId = INVALID_NETWORK_ID;
        Intent intentService = new Intent(this, WiFiScanService.class);
        this.stopService(intentService);
        Logger.debug(TAG, "OnResume: " + Utils.getIsServiceChangeStatus(this)
                + "mIsResumed:" + mIsResumed);
        if (!mIsResumed || Utils.getIsServiceChangeStatus(this)) {
            if (!Utils.isHasWifiConnection(this)) {
                Utils.setLoginStatus(this, Constants.NO_WIFI);
                Utils.setIsServiceUpdate(this, false);
                updateView();
            } else {
                // Check network
                WifiFirstCheckStatus task = new WifiFirstCheckStatus(false);
//                WifiSecondCheckStatus task = new WifiSecondCheckStatus(this);
                if (Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }   
            }
        }
        mConnected.set(Utils.isHasWifiConnection(this));
        updateView();
        mIsResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
        mScanner.pause();

        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (mResetNetworks) {
            enableNetworks();
        }
        // if we enable auto login, we start the service when we exit the view
        if (Utils.getEnableAutoLogin(this)) {
            Intent intentService = new Intent(this, WiFiScanService.class);
            this.startService(intentService);
        }
    }

    @Override
    protected void onDestroy() {
        Logger.debug(TAG, "onDestroy");
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen,
            Preference preference) {
        if (preference instanceof AccessPoint) {
            mSelectedAccessPoint = (AccessPoint) preference;
            showDialog(mSelectedAccessPoint, false);

        } else if (TextUtils.equals(preference.getKey(),
                Constants.LOGIN_STATUS_KEY)) {
            int status = Utils.getLoginStatus(this);
            if (status == Constants.HAVE_LOGIN) {
                ConnectionInfoDialog connectionInfoDlg = new ConnectionInfoDialog(this, this);
                connectionInfoDlg.show();
            }
            if (status == Constants.HAVE_LOGOUT
                    || status == Constants.LOGIN_FALLURE) {
                String name = Utils.getUserName(this);
                if (TextUtils.isEmpty(name)) {
                    AccountSettingsDialog dialog = new AccountSettingsDialog(
                            this, this);
                    dialog.show();
                } else {
                    WiFiLoginTask  loginTask = new WiFiLoginTask(this);
                    if (Build.VERSION.SDK_INT >= 11) {
                        loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        loginTask.execute();
                    }   
                }
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

    private void initSummaryOfWifiFilterListPref() {
        String valueInString = mWifiFilterListPref.getValue();
        setSummaryListPrefbyValueInString(mWifiFilterListPref, valueInString);
    }

    private void setSummaryListPrefbyValueInString(ListPreference listPrefs,
            String value) {
        if (listPrefs != null) {
            String summary[] = getSummaryEntiesOfWifiFilter();
            int mode = parseIntFromString(value);
            listPrefs.setSummary(summary[mode]);
        }
    }

    private void setListnerForWifiFilterPref() {
        mWifiFilterListPref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference pref,
                            Object newValue) {
                        setSummaryListPrefbyValueInString(
                                (ListPreference) pref, (String) newValue);
                        updateAccessPoints((String) newValue);
                        return true;
                    }
                });
    }

    private int parseIntFromString(String value) {
        int defautValue = Constants.DEFAULT_MODE;
        try {
            defautValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return defautValue;
    }

    private String[] getSummaryEntiesOfWifiFilter() {
        final String[] summary = getResources().getStringArray(
                R.array.wifi_filter_mode_summary_entries);
        return summary;
    }

    // private void updateFilterState() {
    // mFilterNetwork = mWifiFilter.isChecked();
    // mWifiFilter
    // .setSummary(mFilterNetwork ? R.string.wifi_filter_only_open_summary
    // : R.string.wifi_filter_all_summary);
    // }

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
                    && mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
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
        updateAccessPoints(mWifiFilterListPref.getValue());
    }

    private void updateAccessPoints(String valueOfFilter) {
        Logger.debug(TAG, "updateAccessPoints");
        mAccessPoints.removeAll();
        int valueInInt = parseIntFromString(valueOfFilter);
        switch (valueInInt) {
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

    private void showDialog(AccessPoint accessPoint, boolean edit) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = new WifiDialog(this, this, accessPoint, edit);
        mDialog.show();
    }

    private void forget(int networkId) {
        mWifiManager.removeNetwork(networkId);
        saveNetworks();
    }

    private void connect(int networkId) {
        if (networkId == INVALID_NETWORK_ID) {
            return;
        }

        // Reset the priority of each network if it goes too high.
        if (mLastPriority > 1000000) {
            for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
                AccessPoint accessPoint = (AccessPoint) mAccessPoints
                        .getPreference(i);
                if (accessPoint.networkId != INVALID_NETWORK_ID) {
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
        mWifiManager.enableNetwork(networkId, true);
        mWifiManager.reconnect();
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
        int resId = R.string.wifi_login_unknow_status;
        int status = Utils.getLoginStatus(this);
        switch (status) {
            case Constants.HAVE_LOGIN:
                resId = R.string.wifi_login_successfully_status;
                break;
            case Constants.HAVE_LOGOUT:
                resId = R.string.wifi_logout_successfully_status;
                break;
            case Constants.NOT_FIND_SERVER:
                resId = R.string.wifi_cannot_login_status;
                break;
            case Constants.NO_WIFI:
                resId = R.string.wifi_no_wifi_status;
                break;
            case Constants.NOT_NEED_LOGIN:
                resId = R.string.wifi_not_need_login_status;
                break;
            case Constants.LOGIN_FALLURE:
                resId = R.string.wifi_login_fail_status;
                break;
            case Constants.STATUS_UNKOWN:
                resId = R.string.wifi_login_unknow_status;
                break;
            default:
                break;
        }
        if (resId == -1) {
            return;
        }
        if (status == Constants.LOGIN_FALLURE) {
            mLoginStatusPrefs.setSummary(Utils.getShowErrorMessage(this));
        } else {
            mLoginStatusPrefs.setSummary(resId);
        }
        if (status == Constants.HAVE_LOGIN || status == Constants.HAVE_LOGOUT
                || status == Constants.LOGIN_FALLURE
                || status == Constants.STATUS_UNKOWN) {
            mLoginStatusPrefs.setEnabled(true);
        } else {
            mLoginStatusPrefs.setEnabled(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void logout() {
        LogoutTask task = new LogoutTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == WifiDialog.BUTTON_FORGET && mSelectedAccessPoint != null) {
            forget(mSelectedAccessPoint.networkId);
        } else if (which == WifiDialog.BUTTON_SUBMIT && mDialog != null) {
            WifiConfiguration config = mDialog.getConfig();

            if (config == null) {
                if (mSelectedAccessPoint != null
                        && !requireKeyStore(mSelectedAccessPoint.getConfig())) {
                    connect(mSelectedAccessPoint.networkId);
                }
            } else if (config.networkId != INVALID_NETWORK_ID) {
                if (mSelectedAccessPoint != null) {
                    mWifiManager.updateNetwork(config);
                    saveNetworks();
                }
            } else {
                int networkId = mWifiManager.addNetwork(config);
                if (networkId != INVALID_NETWORK_ID) {
                    mWifiManager.enableNetwork(networkId, false);
                    config.networkId = networkId;
                    if (mDialog.edit || requireKeyStore(config)) {
                        saveNetworks();
                    } else {
                        connect(networkId);
                    }
                }
            }
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
            mLoginStatusPrefs.setEnabled(true);
            Utils.setIsServiceUpdate(WifiListActivity.this, false);
            updateView();
            Logger.debug(TAG, " ready to logout");
        }

        @Override
        protected void onPreExecute() {
            mLoginStatusPrefs.setSummary(R.string.wifi_logouting_status);
            mLoginStatusPrefs.setEnabled(false);
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

                mLoginStatusPrefs.setEnabled(true);
                Utils.setIsServiceUpdate(WifiListActivity.this, false);
            }
            updateView();
        }

        @Override
        protected void onPreExecute() {
            mLoginStatusPrefs.setSummary(R.string.wifi_loging_status);
            mLoginStatusPrefs.setEnabled(false);
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
            mLoginStatusPrefs.setSummary(R.string.wifi_checking_status);
            mLoginStatusPrefs.setEnabled(false);
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
                secondCheck = new WifiSecondCheckStatus(mContext);
                if (Build.VERSION.SDK_INT >= 11) {
                    secondCheck.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    secondCheck.execute();
                }
            } else if (mFirstCheckStatus == Constants.HAVE_LOGOUT) {
                Logger.debug(TAG, "WifiFirstCheckStatus: " + "Can Login now");
                Utils.setLoginStatus(mContext, mFirstCheckStatus);
                Utils.setIsServiceUpdate(WifiListActivity.this, false);
                updateView();
                if (mIsLogin) {
                    Logger.debug(TAG, "Will login automtically!!");
                    if (!TextUtils.isEmpty(Utils.getUserName(mContext))) {
                        WiFiLoginTask loginTask = new WiFiLoginTask(mContext);
                        if (Build.VERSION.SDK_INT >= 11) {
                            loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            mLoginStatusPrefs.setSummary(R.string.wifi_second_checking_satus);
            mLoginStatusPrefs.setEnabled(false);
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
            } else if (result == Constants.HAVE_LOGOUT) {
                Toast.makeText(WifiListActivity.this,
                        R.string.wifi_change_account_toast, Toast.LENGTH_SHORT)
                        .show();
            }
            mLoginStatusPrefs.setEnabled(true);
            Utils.setLoginStatus(mContext, result);
            Utils.setIsServiceUpdate(WifiListActivity.this, false);
            updateView();
        }
    }
}
