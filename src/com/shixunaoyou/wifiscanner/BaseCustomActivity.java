package com.shixunaoyou.wifiscanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public abstract class BaseCustomActivity extends Activity implements
        View.OnClickListener {

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRecourse());
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar_with_back);
        setTitle();
        setBackButton();
    }

    private void setTitle() {
        TextView titleView = (TextView) findViewById(R.id.titlebar_title);
        titleView.setText(getCustomTitleResecouse());
    }

    private void setBackButton() {
        Button backBtn = (Button) this.findViewById(R.id.titlebar_back_btn);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.titlebar_back_btn) {
            onBackPressed();
        }
    }

    protected abstract int getCustomTitleResecouse();

    protected abstract int getLayoutRecourse();
}
