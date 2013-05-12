package com.shixunaoyou.wifiscanner.more;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.UMengUtils;
import com.shixunaoyou.wifiscanner.wifi.ServiceNotificationHandler;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingActivity extends PreferenceActivity implements
        View.OnClickListener, OnPreferenceChangeListener {

    private CheckBoxPreference mServiceNotificationSettings;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.application_settings);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar_with_back);
        setTitle();
        setBackButton();
        initPreference();
        this.getListView().setBackgroundColor(
                getResources().getColor(R.color.mainview_backgroud_color));
    }

    private void setTitle() {
        TextView titleView = (TextView) findViewById(R.id.titlebar_title);
        titleView.setText(R.string.more_nofitication_title);
    }

    private void setBackButton() {
        Button backBtn = (Button) this.findViewById(R.id.titlebar_back_btn);
        backBtn.setOnClickListener(this);
    }

    private void initPreference() {
        mServiceNotificationSettings = (CheckBoxPreference) this
                .findPreference(Constants.ENABLE_SERVICE_NOTIFICATION);
        mServiceNotificationSettings.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.titlebar_back_btn) {
            onBackPressed();
        }
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (TextUtils.equals(key, Constants.ENABLE_SERVICE_NOTIFICATION)) {
            Boolean enable = (Boolean) newValue;
            updateServiceNotification(enable);
        }
        return true;
    }

    private void updateServiceNotification(Boolean enable) {
        ServiceNotificationHandler handler = ServiceNotificationHandler
                .getInstance(this);
        handler.updateConfigure(enable);
        if (enable) {
            MobclickAgent.onEvent(this.getApplicationContext(),
                    UMengUtils.EVENT_OPEN_NOTIFICATION);
            handler.sendNotification();
        } else {
            MobclickAgent.onEvent(this.getApplicationContext(),
                    UMengUtils.EVENT_CLOSE_NOTIFICATION);
            handler.cancelNotification();
        }
    }
}
