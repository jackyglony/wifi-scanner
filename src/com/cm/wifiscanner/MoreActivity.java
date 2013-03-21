package com.cm.wifiscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MoreActivity extends Activity implements View.OnClickListener,
        CheckUpdateAsyncTask.CheckCompletedListener {

    private LinearLayout mContainterView;
    private LayoutInflater mFlater;

    enum MoreItemEnum {
        NotificationSettings {
            @Override
            int getImageResId() {
                return R.drawable.icon_notification;
            }

            @Override
            int getTitleResId() {
                return R.string.more_nofitication_title;
            }

            @Override
            public void action(Context context) {
                Intent intent = new Intent(context, SettingActivity.class);
                context.startActivity(intent);
            }
        },
        Update {
            @Override
            int getImageResId() {
                return R.drawable.icon_update;
            }

            @Override
            int getTitleResId() {
                return R.string.more_check_update_title;
            }

            @Override
            public void action(Context context) {
                CheckUpdateAsyncTask task = new CheckUpdateAsyncTask(context,
                        (MoreActivity) context);
                if (Build.VERSION.SDK_INT >= 11) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
        },
        Feedback {
            @Override
            int getImageResId() {
                return R.drawable.icon_feedback;
            }

            @Override
            int getTitleResId() {
                return R.string.more_feeback_title;
            }

            @Override
            public void action(Context context) {

            }
        },
        Share {
            @Override
            int getImageResId() {
                return R.drawable.icon_share;
            }

            @Override
            int getTitleResId() {
                return R.string.more_share_title;
            }

            @Override
            public void action(Context context) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        context.getString(R.string.share_information));
                context.startActivity(shareIntent);
            }
        },
        AboutUs {
            @Override
            int getImageResId() {
                return R.drawable.icon_about_us;
            }

            @Override
            int getTitleResId() {
                return R.string.more_about_us_title;
            }

            @Override
            public void action(Context context) {

            }
        },
        ExitApplication {
            @Override
            int getImageResId() {
                return R.drawable.icon_exit;
            }

            @Override
            int getTitleResId() {
                return R.string.more_exit_title;
            }

            @Override
            public void action(Context context) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }

        };
        abstract int getImageResId();

        abstract int getTitleResId();

        abstract void action(Context context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_layout);
        mFlater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mContainterView = (LinearLayout) this
                .findViewById(R.id.more_containter);
        initItem();
    }

    private void initItem() {
        for (MoreItemEnum item : MoreItemEnum.values()) {
            View itemView = createAndIntialItemView(item);
            mContainterView.addView(itemView);
        }
    }

    private View createAndIntialItemView(MoreItemEnum item) {
        View view = mFlater.inflate(R.layout.more_item_layout, null);
        ImageView imageView = (ImageView) view
                .findViewById(R.id.more_item_front_icon);
        imageView.setImageResource(item.getImageResId());

        TextView titleView = (TextView) view.findViewById(R.id.more_item_title);
        titleView.setText(item.getTitleResId());
        view.setClickable(true);
        view.setTag(item);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        MoreItemEnum items = (MoreItemEnum) v.getTag();
        items.action(this);
    }

    @Override
    public void onCheckCompleted(boolean isHaveNewVersion) {

    }
}
