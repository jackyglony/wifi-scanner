package com.shixunaoyou.wifiscanner.more;

import com.shixunaoyou.wifiscanner.R;

public class ProductInfoWebViewActivity extends SettingsWebViewActivity {

    private static final String INFO_PAGE = "file:///android_asset/html/appintro.html";

    @Override
    protected String getUrl() {
        return INFO_PAGE;
    }

    @Override
    protected int getCustomTitleResecouse() {
        return R.string.wifi_about_product_introduce;
    }
}
