package com.shixunaoyou.wifiscanner.more;

import com.shixunaoyou.wifiscanner.BaseCustomActivity;
import com.shixunaoyou.wifiscanner.R;

import android.os.Bundle;

public class ProductCenterActivity extends BaseCustomActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getCustomTitleResecouse() {
        return R.string.more_product_center_title;
    }

    @Override
    protected int getLayoutRecourse() {
        return R.layout.partership_view;
    }
}
