package com.shixunaoyou.wifiscanner.more;

import com.shixunaoyou.wifiscanner.BaseCustomActivity;
import com.shixunaoyou.wifiscanner.R;
import com.umeng.analytics.MobclickAgent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutUSActivity extends BaseCustomActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setButton();
    }

    private void setButton() {
        Button callBtn = (Button) this.findViewById(R.id.call_button);
        callBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.call_button) {
            makeCall();
        }
        super.onClick(v);
    }

    private void makeCall() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:4000809500"));
        startActivity(intent);
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
    protected int getCustomTitleResecouse() {
        return R.string.more_about_us_title;
    }

    @Override
    protected int getLayoutRecourse() {
        return R.layout.about_us_activity;
    }
}
