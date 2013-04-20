package com.shixunaoyou.wifiscanner.more;

import java.util.regex.Pattern;

import com.shixunaoyou.wifiscanner.BaseCustomActivity;
import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.mail.GmailSender;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.UMengUtils;
import com.shixunaoyou.wifiscanner.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackActivity extends BaseCustomActivity {
    private static final String TAG = "FeedbackActivity";

    private static final Pattern rfc2822 = Pattern
            .compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
    private static final String MAIL_USER_NAME = "591.feedback.android@gmail.com";
    private static final String MAIL_PASSWORD = "591wifiandroid";
    private static final String RECEPIENT = "service@591wifi.com";
    private static final int SEND_SUCCESSFUL = 1;
    private static final int SEND_FAILURE = 2;

    private TextView mSubjectView;
    private TextView mContentView;
    private TextView mAddressView;
    private String mSubject;
    private String mContent;
    private String mAddress;
    private Handler mHandler;

    @SuppressLint("HandlerLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == SEND_SUCCESSFUL) {
                    MobclickAgent.onEvent(FeedbackActivity.this,
                            UMengUtils.EVENT_FEEDBACK_SUCCESSFUL);

                } else if (msg.what == SEND_FAILURE) {
                    MobclickAgent.onEvent(FeedbackActivity.this,
                            UMengUtils.EVENT_FEEDBACK_FAILED);
                }
            }
        };
        getViews();
    }

    private void getViews() {
        mSubjectView = (TextView) findViewById(R.id.wifi_feedback_title);
        mContentView = (TextView) findViewById(R.id.wifi_feedback_content);
        mAddressView = (TextView) findViewById(R.id.wifi_feedback_address);
        Button button = (Button) findViewById(R.id.wifi_feedback_submit);
        button.setOnClickListener(this);
        mAddress = Utils.getUserAddress(this);
        if (!TextUtils.isEmpty(mAddress)) {
            mAddressView.setText(mAddress);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.wifi_feedback_submit) {
            sendFeedBack();
        }
        super.onClick(v);
    }

    private void sendFeedBack() {
        if (verfiyContent()) {
            sendMailInWorkThread();
            showToastMessage(R.string.wifi_feedback_submit_success);
            finish();
        }
    }

    private boolean verfiyContent() {
        mSubject = mSubjectView.getText().toString();
        mContent = mContentView.getText().toString();
        mAddress = mAddressView.getText().toString();
        if (TextUtils.isEmpty(mSubject)) {
            showToastMessage(R.string.wifi_feedback_null_subject);
            return false;
        }
        if (TextUtils.isEmpty(mContent)) {
            showToastMessage(R.string.wifi_feedback_content_tips);
            return false;
        }
        if (!TextUtils.isEmpty(mAddress)) {
            return checkAddressFormat(mAddress);
        }
        return true;
    }

    private void showToastMessage(int errorResId) {
        Toast.makeText(this, errorResId, Toast.LENGTH_SHORT).show();
    }

    private boolean checkAddressFormat(String address) {
        if (!rfc2822.matcher(address).matches()) {
            showToastMessage(R.string.wifi_feedback_wrong_mail_address);
            return false;
        }
        Utils.setUserAddress(this, address);
        return true;
    }

    private String getContent() {
        StringBuilder builder = new StringBuilder();
        builder.append(mContent);
        builder.append("\nUser's username: " + Utils.getUserName(this));
        builder.append("\nUser's address: " + Utils.getUserAddress(this));

        return builder.toString();
    }

    private void sendMailInWorkThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                GmailSender sender = new GmailSender(MAIL_USER_NAME,
                        MAIL_PASSWORD);
                try {
                    sender.sendMail(mSubject, getContent(), MAIL_USER_NAME,
                            RECEPIENT);
                    mHandler.sendEmptyMessage(SEND_SUCCESSFUL);
                    Logger.debug(TAG, "Send Mail successfully!");

                } catch (Exception e) {
                    mHandler.sendEmptyMessage(SEND_FAILURE);

                    Logger.debug(TAG, "Send Mail Error!");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected int getCustomTitleResecouse() {
        return R.string.more_feeback_title;
    }

    @Override
    protected int getLayoutRecourse() {
        return R.layout.feedback_layout;
    }
}
