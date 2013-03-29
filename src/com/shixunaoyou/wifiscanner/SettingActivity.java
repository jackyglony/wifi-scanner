package com.shixunaoyou.wifiscanner;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingActivity extends PreferenceActivity implements
        View.OnClickListener {

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
    }

    private void setTitle() {
        TextView titleView = (TextView) findViewById(R.id.titlebar_title);
        titleView.setText(R.string.more_nofitication_title);
    }

    private void setBackButton() {
        Button backBtn = (Button) this.findViewById(R.id.titlebar_back_btn);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.titlebar_back_btn) {
            onBackPressed();
        }
    }
}
