package com.cm.wifiscanner.wifi;

import com.cm.wifiscanner.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class PartershipActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.partership_view);

    }
}
