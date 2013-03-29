package com.shixunaoyou.wifiscanner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AboutUSActivity extends Activity implements View.OnClickListener {
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_activity);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar_with_back);
        setTitle();
        setBackButton();
    }

    private void setTitle() {
        TextView titleView = (TextView) findViewById(R.id.titlebar_title);
        titleView.setText(R.string.more_about_us_title);
    }

    private void setBackButton() {
        Button backBtn = (Button) this.findViewById(R.id.titlebar_back_btn);
        Button callBtn = (Button) this.findViewById(R.id.call_button);
        backBtn.setOnClickListener(this);
        callBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.titlebar_back_btn) {
            onBackPressed();
        } else if (id == R.id.call_button) {
            makeCall();
        }

    }

    private void makeCall() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:4000809500"));
        startActivity(intent);
    }
}
