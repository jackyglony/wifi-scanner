package com.cm.wifiscanner;

import com.cm.wifiscanner.wifi.PartershipActivity;
import com.cm.wifiscanner.wifi.WifiListActivity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

//TODO: old main, Will delete it if new main has been stable
@SuppressWarnings("deprecation")
public class WifiScannerActivity extends TabActivity {

	private static final String WIFI_LIST_TAG = "tab-wifi-list";
	private static final String FAV_LIST_TAG = "tab-favorite-list";
	private static final String JABBER_TAG = "tab-jabber-list";
	private static final String SETTING_TAG = "tab-setting-list";

	private TabHost mTabHost;

	public WifiScannerActivity() {
	}

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		initTabs();

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
	}

	private void initTabs() {
		Intent intent = new Intent();
		intent.setClass(this, WifiListActivity.class);

		TabSpec spec = mTabHost
				.newTabSpec(WIFI_LIST_TAG)
				.setIndicator(getString(R.string.tab_title_wifi_list),
						getResources().getDrawable(R.drawable.ic_title_setting))
				.setContent(intent);
		mTabHost.addTab(spec);

		intent = new Intent();
		intent.setClass(this, PersonCentreActivity.class);
		spec = mTabHost
				.newTabSpec(JABBER_TAG)
				.setIndicator(getString(R.string.tab_title_person_centre),
						getResources().getDrawable(R.drawable.ic_title_respond))
				.setContent(intent);
		mTabHost.addTab(spec);

		intent = new Intent();
		intent.setClass(this, PartershipActivity.class);
		spec = mTabHost
				.newTabSpec(FAV_LIST_TAG)
				.setIndicator(getString(R.string.tab_title_partership),
						getResources().getDrawable(R.drawable.ic_title_about))
				.setContent(intent);
		mTabHost.addTab(spec);

		intent = new Intent();
		intent.setClass(this, SettingActivity.class);
		spec = mTabHost
				.newTabSpec(SETTING_TAG)
				.setIndicator(getString(R.string.tab_title_settings),
						getResources().getDrawable(R.drawable.ic_title_exit))
				.setContent(intent);
		mTabHost.addTab(spec);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
