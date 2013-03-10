package com.cm.wifiscanner;

import com.cm.wifiscanner.util.Constants;
import com.cm.wifiscanner.util.Utils;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

public class PersonSettingsActivity extends PreferenceActivity implements OnDismissListener {

    private static final String TAG = "SettingActivity";
    private Preference mAccountSettingsPrefs;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.person_settings);
        mAccountSettingsPrefs = this.findPreference(Constants.ACCOUNT_SETTINGS_KEY);
//        mGatewayPrefs= this.findPreference(Constants.GATE_CONFIGURE_KEY);
//        String version_number;
//        try {
//            version_number = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//            String version = String.format(getString(R.string.wifi_current_version), version_number);
//            mCheckUpdatePrefs.setSummary(version);
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        this.getListView().setBackgroundResource(R.drawable.bg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen,
            Preference preference) {
        String key = preference.getKey();
        if (TextUtils.equals(key, Constants.ACCOUNT_SETTINGS_KEY)) {
//            Toast.makeText(this, "Account Settings", Toast.LENGTH_SHORT).show();
            AccountSettingsDialog dialog = new AccountSettingsDialog(this, this);
            dialog.show();
        } 
        return true;
    }

    private void updateView() {
        if (TextUtils.isEmpty(Utils.getUserName(this))) {
            mAccountSettingsPrefs.setSummary(R.string.wifi_add_account);
        } else {
            String account = String.format(
                    getString(R.string.wifi_edit_account),
                    Utils.getUserName(this));
            mAccountSettingsPrefs.setSummary(account);
        }
//        mGatewayPrefs.setSummary(Utils.getGateway(this));
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        updateView();
    }
}
