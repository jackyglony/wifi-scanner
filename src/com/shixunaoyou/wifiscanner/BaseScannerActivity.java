package com.shixunaoyou.wifiscanner;

import android.app.Activity;
import android.content.Intent;

public abstract class BaseScannerActivity extends Activity {

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
