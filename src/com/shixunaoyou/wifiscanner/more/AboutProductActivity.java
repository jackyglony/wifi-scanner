package com.shixunaoyou.wifiscanner.more;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shixunaoyou.wifiscanner.BaseCustomActivity;
import com.shixunaoyou.wifiscanner.R;

public class AboutProductActivity extends BaseCustomActivity {
    enum AboutItem {
        ProductIntroduce {
            @Override
            int getTitleResId() {
                return R.string.wifi_about_product_introduce;
            }

            @Override
            public void action(Context context) {
                Intent intent = new Intent(context, ProductInfoWebViewActivity.class);
                context.startActivity(intent);
            }
        },
        Questions {
            @Override
            int getTitleResId() {
                return R.string.wifi_about_questions;
            }

            @Override
            public void action(Context context) {
                Intent intent = new Intent(context, QuestionWebViewActivity.class);
                context.startActivity(intent);
            }
        },
        AboutCompany {
            @Override
            int getTitleResId() {
                return R.string.wifi_about_company;
            }

            @Override
            public void action(Context context) {
                Intent intent = new Intent(context, AboutUSActivity.class);
                context.startActivity(intent);
            }
        },
        UserProtocol {
            @Override
            int getTitleResId() {
                return R.string.wifi_about_user_protocol;
            }

            @Override
            public void action(Context context) {
                Intent intent = new Intent(context, UserProtocolActivity.class);
                context.startActivity(intent);
            }
        };

        abstract int getTitleResId();

        abstract void action(Context context);
    }

    @Override
    protected void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        initViews();
    }

    private void initViews() {
        initVersionView();
        initItemViews();
    }

    private void initVersionView() {
        TextView version = (TextView) findViewById(R.id.wifi_about_version);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(getString(R.string.wifi_current_version,
                    pInfo.versionName));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initItemViews() {
        LinearLayout container = (LinearLayout) findViewById(R.id.wifi_about_items_containter);
        for (AboutItem item : AboutItem.values()) {
            View itemView = createAndIntialItemView(item);
            if (item.equals(AboutItem.UserProtocol)) {
                View divider = itemView.findViewById(R.id.more_item_divider);
                divider.setVisibility(View.GONE);
            }
            container.addView(itemView);
        }
    }

    private View createAndIntialItemView(AboutItem item) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.more_item_layout, null);
        ImageView imageView = (ImageView) view
                .findViewById(R.id.more_item_front_icon);
        imageView.setVisibility(View.GONE);
        TextView titleView = (TextView) view.findViewById(R.id.more_item_title);
        titleView.setText(item.getTitleResId());
        view.setClickable(true);
        view.setTag(item);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    protected int getCustomTitleResecouse() {
        return R.string.wifi_about_product_title;
    }

    @Override
    protected int getLayoutRecourse() {
        return R.layout.about_product_activity;
    }

    @Override
    public void onClick(View v) {
        AboutItem item = (AboutItem) v.getTag();
        if (item != null) {
            item.action(this);
        }
        super.onClick(v);
    }
}
