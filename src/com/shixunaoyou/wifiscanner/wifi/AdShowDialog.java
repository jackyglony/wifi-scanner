package com.shixunaoyou.wifiscanner.wifi;

import com.shixunaoyou.wifiscanner.BaseCustomDialog;
import com.shixunaoyou.wifiscanner.LoadingActivity;
import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Logger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class AdShowDialog extends BaseCustomDialog implements
        View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "AdShowDialog";
    private View mContentView;
    private WebView mAdView;
    private Context mContext;

    protected AdShowDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContentView = getLayoutInflater().inflate(R.layout.ad_dialog, null);
        mAdView = (WebView) mContentView.findViewById(R.id.ad_view);
        setView(mContentView);
        initButtons();
        mAdView.loadUrl("http://m.591wifi.com");
        super.onCreate(savedInstanceState);
        mAdView.setOnClickListener(this);
        mAdView.setOnTouchListener(this);
        mAdView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void initButtons() {
        Button downloadBtn = (Button) mContentView
                .findViewById(R.id.wifi_addialog_enter_application);
        Button cancelBtn = (Button) mContentView
                .findViewById(R.id.wifi_addialog_cancel);
        downloadBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Logger.debug(TAG, "onClick");
        int id = v.getId();
        if (id == R.id.wifi_addialog_enter_application) {
            Intent intent = new Intent();
            intent.setClass(mContext, LoadingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else if (id == R.id.ad_view) {
            String url = "http://www.baidu.com";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            mContext.startActivity(i);
        }
        this.dismiss();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Logger.debug(TAG, "ACTION_DOWN");
            String url = "http://m.591wifi.com";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            mContext.startActivity(intent);
            this.dismiss();
        }
        return false;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.wifi_notification_login_successful;
    }
}
