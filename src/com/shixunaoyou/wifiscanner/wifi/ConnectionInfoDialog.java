package com.shixunaoyou.wifiscanner.wifi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.shixunaoyou.wifiscanner.BaseCustomDialog;
import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectionInfoDialog extends BaseCustomDialog implements
        View.OnClickListener {
    private View mContentView;
    private ViewGroup mInfoView;
    private Context mContext;
    private ActionListener mLogoutListener;
    private WifiManager mWifiManager;
    private ImageView mSignalView;
    private View mLoginContainer;
    private View mLogoutContainer;
    private Button mBackButton3;
    private int mLevel = 4;
    private boolean hasLogin = false;

    protected ConnectionInfoDialog(Context context, ActionListener listener) {
        super(context);
        mContext = context;
        mLogoutListener = listener;
        if (Utils.getLoginStatus(mContext) == Constants.HAVE_LOGIN) {
            hasLogin = true;
        }
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDialog();
        super.onCreate(savedInstanceState);
        showDataOfConnection();
    }

    private void initDialog() {
        mContentView = getLayoutInflater().inflate(R.layout.connection_info, null);
        setView(mContentView);
        initViews();
        updateSignalView();
        setAllButtonsListener();
    }

    private void initViews() {
        mLoginContainer = mContentView.findViewById(R.id.connect_status_login_container);
        mLogoutContainer = mContentView.findViewById(R.id.connect_status_logout_container);
        mBackButton3 = (Button) mContentView.findViewById(R.id.connec_status_back_btn3);
        mSignalView = (ImageView) mContentView.findViewById(R.id.connect_status_signal);
        mSignalView.setVisibility(View.VISIBLE);
        updateButtonStatus();
    }

    private void updateButtonStatus() {
        int status = Utils.getLoginStatus(mContext);
        if (status == Constants.HAVE_LOGIN || status == Constants.LOGIN_FALLURE) {
            mLoginContainer.setVisibility(View.GONE);
            mLogoutContainer.setVisibility(View.VISIBLE);
            mBackButton3.setVisibility(View.GONE);
        } else if (status == Constants.HAVE_LOGOUT) {
            mLoginContainer.setVisibility(View.VISIBLE);
            mLogoutContainer.setVisibility(View.GONE);
            mBackButton3.setVisibility(View.GONE);
        } else {
            mLoginContainer.setVisibility(View.GONE);
            mLogoutContainer.setVisibility(View.GONE);
            mBackButton3.setVisibility(View.VISIBLE);
        }
    }

    private void setAllButtonsListener() {
        setButtonListener(R.id.connec_status_login_btn);
        setButtonListener(R.id.connec_status_logout_btn);
        setButtonListener(R.id.connect_status_back_btn1);
        setButtonListener(R.id.connec_status_back_btn2);
        setButtonListener(R.id.connec_status_back_btn3);
    }

    private void setButtonListener(int resId) {
        Button loginButton = (Button) mContentView.findViewById(resId);
        loginButton.setOnClickListener(this);
    }

    private void showDataOfConnection() {
        mInfoView = (ViewGroup) mContentView.findViewById(R.id.conn_info);
        long loginTime = Utils.getLastLoginTime(mContext);
        Logger.debug("Test", "currentTime: " + loginTime);

        updateLoginStayTimeInfo(loginTime);
        updateNetworkStatus();
        addSSIDInfo();
        addLoginStartTimeInfo(loginTime);
        addDataTarfficTime();
    }

    private void updateLoginStayTimeInfo(long loginTime) {
        String time = mContext
                .getString(R.string.connection_invalid_login_data);
        if (hasLogin) {
            long currentTime = System.currentTimeMillis();
            long lastTime = currentTime - loginTime;
            time = getFormatTime(lastTime);
        }
        TextView timeView = (TextView) mContentView
                .findViewById(R.id.connect_status_info_time);
        timeView.setText(time);
    }

    private void updateNetworkStatus() {
        int status = Utils.getLoginStatus(mContext);
        int resId = Utils.getResIdofStatus(status);
        TextView statusView = (TextView) mContentView
                .findViewById(R.id.connect_status_info);
        statusView.setText(resId);
    }

    private void addSSIDInfo() {
        String ssid = mContext
                .getString(R.string.connection_invalid_login_data);
        if (Utils.getLoginStatus(mContext) != Constants.NO_WIFI) {
            ssid = mWifiManager.getConnectionInfo().getSSID();
        }
        TextView ssidView = (TextView) mContentView
                .findViewById(R.id.connect_status_ssid);
        ssidView.setText(ssid);
    }

    private void addLoginStartTimeInfo(long longTime) {
        String time = mContext
                .getString(R.string.connection_invalid_login_data);
        if (hasLogin) {
            Date date = new Date(longTime);
            SimpleDateFormat dateformat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            time = dateformat.format(date);
        }

        addRow(mInfoView, R.string.connection_login_start_time, time);
    }

    private View addRow(ViewGroup group, int nameResId, String value) {
        View row = getLayoutInflater().inflate(R.layout.wifi_dialog_row, group,
                false);
        ((TextView) row.findViewById(R.id.name)).setText(nameResId);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
        return row;
    }

    private String getFormatTime(long lastTime) {
        StringBuilder builder = new StringBuilder();
        String days = getDays(lastTime);
        String hours = getHours(lastTime);
        String minutes = getMinutes(lastTime);
        if (days != null) {
            builder.append(days);
        }
        if (hours != null) {
            builder.append(hours);
        }
        if (minutes != null) {
            builder.append(minutes);
        }
        if (days == null && hours == null && minutes == null) {
            builder.append(mContext
                    .getString(R.string.connection_less_minuts_info));
        }
        return builder.toString();
    }

    private String getDays(long lastTime) {
        long days = lastTime / (24 * 1000 * 60 * 60);
        return combineString(days, R.string.connection_time_day_info);
    }

    private String getHours(long lastTime) {
        long hours = (lastTime % (24 * 1000 * 60 * 60)) / (1000 * 60 * 60);
        return combineString(hours, R.string.connection_time_hour_info);
    }

    private String getMinutes(long lastTime) {
        long minutes = (lastTime % (1000 * 60 * 60)) / (1000 * 60);
        return combineString(minutes, R.string.connection_minuts_info);
    }

    private String combineString(long date, int dateResId) {
        String result = null;
        if (date > 0) {
            result = date + mContext.getString(dateResId);
        }
        return result;
    }

    private void addDataTarfficTime() {
        String data = mContext
                .getString(R.string.connection_invalid_login_data);
        if (hasLogin) {
            long dataTrafficLogin = Utils.getDataTarfficOfLogin(mContext);
            long currentDataTarffic = TrafficStats.getTotalRxBytes();
            long dataConsume = currentDataTarffic - dataTrafficLogin;
            float dataInFloat = (float) dataConsume / (1024 * 1024);
            data = String.format("%.2fMB", dataInFloat);
        }
        addRow(mInfoView, R.string.connection_data_traffic_info, data);
    }

    public interface ActionListener {
        public void doAction(int action);
    }

    private void updateSignalView() {
        switch (mLevel) {
            case 0:
                mSignalView.setImageResource(R.drawable.ic_wifi_signal_1);
                break;
            case 1:
                mSignalView.setImageResource(R.drawable.ic_wifi_signal_2);
                break;
            case 2:
                mSignalView.setImageResource(R.drawable.ic_wifi_signal_3);
                break;
            case 3:
                mSignalView.setImageResource(R.drawable.ic_wifi_signal_4);
                break;
            default:
                break;
        }
    }

    public void setWifiInfo(String ssid, int signalLevel) {
        mLevel = signalLevel;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.connec_status_login_btn) {
            mLogoutListener.doAction(Constants.LOGIN_ACTION);
        } else if (id == R.id.connec_status_logout_btn) {
            mLogoutListener.doAction(Constants.LOGOUT_ACTION);
        }
        this.dismiss();
    }

    @Override
    protected int getDialogTitle() {
        return R.string.connection_info_dialog_title;
    }
}
