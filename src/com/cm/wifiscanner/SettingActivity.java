package com.cm.wifiscanner;

import com.cm.wifiscanner.util.Constants;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.ImageView;

public class SettingActivity extends PreferenceActivity implements
        CheckUpdateAsyncTask.CheckCompletedListener {

//    private static final String TAG = "SettingActivity";
    private Preference mCheckUpdatePrefs;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.application_settings);
        mCheckUpdatePrefs = this.findPreference(Constants.CHECK_UPDATE_KEY);
        String version_number;
        try {
            version_number = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String version = String.format(getString(R.string.wifi_current_version), version_number);
            mCheckUpdatePrefs.setSummary(version);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
//        this.getListView().setBackgroundResource(R.drawable.bg);
        ImageView qrView = new ImageView(this);
        qrView.setImageResource(R.drawable.qrcode);
        this.getListView().addFooterView(qrView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen,
            Preference preference) {
        String key = preference.getKey();
        if (TextUtils.equals(key, Constants.CHECK_UPDATE_KEY)) {
            CheckUpdateAsyncTask task = new CheckUpdateAsyncTask(this, this);
            if(Build.VERSION.SDK_INT >= 11) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else {
                task.execute();
            }
        } else if (TextUtils.equals(key, Constants.ABOUT_KEY)) {
            // TODO: About Dialog.
        } else if (TextUtils.equals(key, Constants.SHARE_INFO_KEY)) {
            sendShareIntent();
        }
        return true;
    }

    private void sendShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_information)); 
        startActivity(shareIntent);
    }

    @Override
    public void onCheckCompleted(boolean isHaveNewVersion) {
//        if (isHaveNewVersion) {
//            Logger.debug(TAG, "CheckUpdateAsyncTask CallBack: " + isHaveNewVersion);
//            Intent startUpdateIntent = new Intent(this,
//                    DownloadUpdateService.class);
//            this.startService(startUpdateIntent);
//        }
    }
}
