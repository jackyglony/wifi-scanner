package com.shixunaoyou.wifiscanner.more;

import android.os.Bundle;
import android.webkit.WebView;

import com.shixunaoyou.wifiscanner.BaseCustomActivity;
import com.shixunaoyou.wifiscanner.R;

public abstract class SettingsWebViewActivity extends BaseCustomActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadWebView();
    }

    private void loadWebView() {
        WebView webView = (WebView) findViewById(R.id.wifi_information_webview);
        webView.loadUrl(getUrl());
    }

    @Override
    abstract protected int getCustomTitleResecouse();

    abstract protected String getUrl();

    @Override
    protected int getLayoutRecourse() {
        return R.layout.webview_layout;
    }
}
