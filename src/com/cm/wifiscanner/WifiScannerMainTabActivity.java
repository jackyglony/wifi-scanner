package com.cm.wifiscanner;

import com.cm.wifiscanner.wifi.PartershipActivity;
import com.cm.wifiscanner.wifi.WifiListActivity;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.CompoundButton.OnCheckedChangeListener;

@SuppressWarnings("deprecation")
public class WifiScannerMainTabActivity extends ActivityGroup implements
        OnCheckedChangeListener {
    private static final String TAB_WIFI_TAB = "tab_wifi";
    private static final String TAB_PERSON_CENTRE = "tab_centre";
    private static final String TAB_PARTERSHIP = "tab_partership";
    private static final String TAB_SETTINGS = "tab_settings";

    private RadioButton mWifiRadio;
    private RadioButton mPersonCentreRadio;
    private RadioButton mPartershipRadio;
    private RadioButton mSettingsRadio;

    private TabHost mHost;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);

        initTabs();
        initRadios();
        updateFirstEnter();
    }

    private void initTabs() {
        mHost = (TabHost) findViewById(R.id.tabhost);
        mHost.setup(this.getLocalActivityManager());

        mHost.addTab(getWifiTab());
        mHost.addTab(getPersonCentreTab());
        mHost.addTab(getPartershipTab());
        mHost.addTab(getSettingsTab());
    }

    private TabHost.TabSpec getWifiTab() {
        Intent intent = new Intent(this, WifiListActivity.class);
        return buildTabSpec(TAB_WIFI_TAB, R.string.tab_title_wifi_list,
                R.drawable.icon_wifi, intent);
    }

    private TabHost.TabSpec getPersonCentreTab() {
        Intent intent = new Intent(this, PersonCentreActivity.class);
        return buildTabSpec(TAB_PERSON_CENTRE,
                R.string.tab_title_person_centre,
                R.drawable.icon_person_centre, intent);
    }

    private TabHost.TabSpec getPartershipTab() {
        Intent intent = new Intent(this, PartershipActivity.class);
        return buildTabSpec(TAB_PARTERSHIP, R.string.tab_title_partership,
                R.drawable.icon_partership, intent);
    }

    private TabHost.TabSpec getSettingsTab() {
        Intent intent = new Intent(this, SettingActivity.class);
        return buildTabSpec(TAB_SETTINGS, R.string.tab_title_settings,
                R.drawable.icon_settings, intent);
    }

    private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,
            final Intent content) {
        return this.mHost
                .newTabSpec(tag)
                .setIndicator(this.getString(resLabel),
                        getResources().getDrawable(resIcon))
                .setContent(content);
    }

    private void initRadios() {
        mWifiRadio = (RadioButton) this.findViewById(R.id.tab_wifi_scan);
        mPersonCentreRadio = (RadioButton) this
                .findViewById(R.id.tab_person_centre);
        mPartershipRadio = (RadioButton) this.findViewById(R.id.tab_partership);
        mSettingsRadio = (RadioButton) this.findViewById(R.id.tab_settings);

        mWifiRadio.setOnCheckedChangeListener(this);
        mPersonCentreRadio.setOnCheckedChangeListener(this);
        mPartershipRadio.setOnCheckedChangeListener(this);
        mSettingsRadio.setOnCheckedChangeListener(this);
    }

    private void updateFirstEnter() {
        mWifiRadio.setChecked(true);
        this.mHost.setCurrentTabByTag(TAB_WIFI_TAB);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.tab_wifi_scan:
                    this.mHost.setCurrentTabByTag(TAB_WIFI_TAB);
                    break;
                case R.id.tab_person_centre:
                    this.mHost.setCurrentTabByTag(TAB_PERSON_CENTRE);
                    break;
                case R.id.tab_partership:
                    this.mHost.setCurrentTabByTag(TAB_PARTERSHIP);
                    break;
                case R.id.tab_settings:
                    this.mHost.setCurrentTabByTag(TAB_SETTINGS);
                    break;
                default:
                    break;
            }
        }
    }
}
