package com.cm.wifiscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class LoadingActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading);
        Handler handler = new Handler();
        handler.postDelayed(new splashhandler(), 2000);

    }

    class splashhandler implements Runnable {
        public void run() {
            startActivity(new Intent(getApplication(),
                    WifiScannerMainTabActivity.class));
            LoadingActivity.this.finish();
        }
    }
}
