package com.cm.wifiscanner.wifi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.cm.wifiscanner.R;
import com.cm.wifiscanner.util.Logger;
import com.cm.wifiscanner.util.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectionInfoDialog extends AlertDialog implements
        DialogInterface.OnClickListener {
    private View mView;
    private ViewGroup mInfoView;
    private Context mContext;
    private LogoutListener mLogoutListener;
    private WifiManager mWifiManager;

    protected ConnectionInfoDialog(Context context, LogoutListener listener) {
        super(context);
        mContext = context;
        mLogoutListener = listener;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDialog();
        super.onCreate(savedInstanceState);

        showDataOfConnection();
    }

    private void initDialog() {
        mView = getLayoutInflater().inflate(R.layout.connection_info, null);
        setView(mView);
        setButton(DialogInterface.BUTTON_NEGATIVE,
                mContext.getString(android.R.string.cancel), this);
        setButton(DialogInterface.BUTTON_POSITIVE,
                mContext.getString(R.string.connection_info_logout), this);
        setTitle(mContext
                .getString(R.string.connection_info_dialog_title));
        setInverseBackgroundForced(true);
    }

    private void showDataOfConnection() {
        mInfoView = (ViewGroup) mView.findViewById(R.id.conn_info);
        long loginTime = Utils.getLastLoginTime(mContext);
        Logger.debug("Test", "currentTime: " + loginTime);

        addSSIDInfo();
        addLoginStartTimeInfo(loginTime);
        addLoginStayTimeInfo(loginTime);
        addDataTarfficTime();
    }

    private void addSSIDInfo() {
        View titleView = addRow(mInfoView, R.string.connection_login_to_text,
                mWifiManager.getConnectionInfo().getSSID());
        TextView ssidView = (TextView) titleView.findViewById(R.id.value);
        ssidView.setTextAppearance(mContext,
                android.R.style.TextAppearance_Medium);
    }

    private void addLoginStartTimeInfo(long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat dateformat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String formatDate = dateformat.format(date);
        addRow(mInfoView, R.string.connection_login_start_time, formatDate);
    }

    private void addLoginStayTimeInfo(long loginTime) {
        long currentTime = System.currentTimeMillis();
        long lastTime = currentTime - loginTime;
        String timeInFormat = getFormatTime(lastTime);
        addRow(mInfoView, R.string.connection_login_time, timeInFormat);
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
        long dataTrafficLogin = Utils.getDataTarfficOfLogin(mContext);
        long currentDataTarffic = TrafficStats.getTotalRxBytes();
        long dataConsume = currentDataTarffic - dataTrafficLogin;
        float dataInFloat = (float) dataConsume / (1024 * 1024);
        addRow(mInfoView, R.string.connection_data_traffic_info,
                String.format("%.2fMB", dataInFloat));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mLogoutListener.logout();
        }
    }

    public interface LogoutListener {
        public void logout();
    }
}
