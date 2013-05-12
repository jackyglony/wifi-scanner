package com.shixunaoyou.wifiscanner;

import com.umeng.analytics.MobclickAgent;

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
        handler.postDelayed(new splashhandler(), 3000);
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

    class splashhandler implements Runnable {
        public void run() {
            startActivity(new Intent(getApplication(),
                    WifiScannerMainTabActivity.class));
            LoadingActivity.this.finish();
        }
    }
}
