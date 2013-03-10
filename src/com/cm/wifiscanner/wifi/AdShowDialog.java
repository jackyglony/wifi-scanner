package com.cm.wifiscanner.wifi;

import com.cm.wifiscanner.R;
import com.cm.wifiscanner.WifiScannerActivity;
import com.cm.wifiscanner.util.Logger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AdShowDialog extends AlertDialog implements
        DialogInterface.OnClickListener, View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "AdShowDialog";
    private View mView;
    private WebView mAdView;
    private Context mContext;

    protected AdShowDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.ad_dialog, null);
        mAdView = (WebView) mView.findViewById(R.id.ad_view);
        setView(mView);
        setInverseBackgroundForced(true);
        setButton(DialogInterface.BUTTON_NEGATIVE,
                mContext.getString(android.R.string.cancel), this);
        setButton(DialogInterface.BUTTON_POSITIVE,
                mContext.getString(R.string.wifi_check_wifi_settings), this);
        setTitle(mContext
                .getString(R.string.wifi_notification_login_successful));
        mAdView.loadUrl("http://www.baidu.com/img/shouye_b5486898c692066bd2cbaeda86d74448.gif");
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
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE) {
            Intent intent = new Intent();
            intent.setClass(mContext, WifiScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
        this.dismiss();
    }

    @Override
    public void onClick(View v) {
        Logger.debug(TAG, "onClick");
        String url = "http://www.baidu.com";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mContext.startActivity(i);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            Logger.debug(TAG, "ACTION_DOWN");
            String url = "http://www.baidu.com";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            mContext.startActivity(intent);
            this.dismiss();
        }
        return false;
    }
}
