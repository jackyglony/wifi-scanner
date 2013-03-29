package com.shixunaoyou.wifiscanner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import com.shixunaoyou.wifiscanner.util.HttpUtils;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity implements View.OnClickListener {
    private static String TAG = "RegisterActivity";
    private static String TOKEN_URL = "http://www.591wifi.com/portal/getcodebysmobile?susername=";
    private static String REGISTER_URL = "http://www.591wifi.com/portal/"
            + "deputize?UserName=13693357261&Mobile=13693357261&"
            + "Password_1=123456&Password_2=123456&" + "scodes=130326";

    private static final int REQUEST_TOKEN_COMPLETED = 1;

    private static final int TOKEN_REQUEST_SUCCESS = 0;
    private static final int TOKEN_REQUEST_FAILED = 1;
    private static final int REGISTER_SUCCESS = 2;
    private static final int REGISTER_FAILED = 3;

    private Handler mUIHandler;
    private EditText mPhoneNumberView;
    private EditText mPasswordNumberView;
    private EditText mUsernameView;
    private EditText mTokenView;
    private Button mGetTokenButton;
    private Button mResigterButton;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_register_layout);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar_with_back);
        mContext = this;
        setTitle();
        setBackButton();
        getViews();
        initiateHandler();
    }

    private void setTitle() {
        TextView titleView = (TextView) findViewById(R.id.titlebar_title);
        titleView.setText(R.string.wifi_register_title);
    }

    private void getViews() {
        mPhoneNumberView = (EditText) findViewById(R.id.wifi_register_phone);
        mPasswordNumberView = (EditText) findViewById(R.id.wifi_register_password);
        mTokenView = (EditText) findViewById(R.id.wifi_register_token);
        mUsernameView = (EditText) findViewById(R.id.wifi_register_username);
        mGetTokenButton = (Button) findViewById(R.id.wifi_register_gettoken_btn);
        mResigterButton = (Button) findViewById(R.id.wifi_register_submit_btn);
        mGetTokenButton.setOnClickListener(this);
        mResigterButton.setOnClickListener(this);
    }

    private void initiateHandler() {
        mUIHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case TOKEN_REQUEST_SUCCESS:
                        break;
                    case TOKEN_REQUEST_FAILED:
                        break;
                    case REGISTER_SUCCESS:
                        saveAccount();
                        break;
                    case REGISTER_FAILED:
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private void saveAccount() {
        String name = mPhoneNumberView.getText().toString();
        String password = mPasswordNumberView.getText().toString();
        Utils.setUserName(this, name);
        Utils.setPassword(this, password);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.titlebar_back_btn) {
            onBackPressed();
        } else if (v.getId() == R.id.wifi_register_gettoken_btn) {
            sendTokenRequest();
        } else if (v.getId() == R.id.wifi_register_submit_btn) {
            sendRegisterRequeset();
        }
    }

    private void setBackButton() {
        Button backBtn = (Button) this.findViewById(R.id.titlebar_back_btn);
        backBtn.setOnClickListener(this);
    }

    private void sendTokenRequest() {
        if (verifyPhonNumber()) {
            final String requestTokenUrl = TOKEN_URL
                    + mPhoneNumberView.getText().toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final JSONObject result = HttpUtils
                                .sendPostRequest(requestTokenUrl);
                        String errorCode;
                        int resultIntCode;
                        errorCode = result.getString("errno");
                        final String errorMsg = result.getString("errMsg");
                        if (TextUtils.equals(errorCode, "0")) {
                            resultIntCode = TOKEN_REQUEST_SUCCESS;
                        } else {
                            resultIntCode = TOKEN_REQUEST_FAILED;
                        }
                        showResultInUiThread(errorMsg);
                        mUIHandler.sendEmptyMessage(resultIntCode);
                    } catch (JSONException e) {
                        showResultInUiThread(mContext
                                .getString(R.string.wifi_register_wrong_format));
                        Logger.debug(TAG, e.toString());
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    private void showResultInUiThread(final String errorMsg) {
        RegisterActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean verifyPhonNumber() {
        boolean result = true;
        String phone = mPhoneNumberView.getText().toString();
        int errorId = -1;
        if (TextUtils.isEmpty(phone)) {
            result = false;
            errorId = R.string.wifi_register_null_phone;
        } else if (!phone.startsWith("1") || phone.length() != 11) {
            result = false;
            errorId = R.string.wifi_register_wrong_format_phone;
        }
        if (!result) {
            Toast.makeText(this, errorId, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private void sendRegisterRequeset() {
        if (verifyRegisterInfo()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String registerUrl = getRegisterUrl();

                        final JSONObject result = HttpUtils
                                .sendPostRequest(registerUrl);
                        String errorCode;
                        int resultIntCode;

                        errorCode = result.getString("errno");
                        final String errorMsg = result.getString("errMsg");
                        if (TextUtils.equals(errorCode, "0")) {
                            resultIntCode = REGISTER_SUCCESS;
                        } else {
                            resultIntCode = REGISTER_FAILED;
                        }
                        showResultInUiThread(errorMsg);
                        mUIHandler.sendEmptyMessage(resultIntCode);
                    } catch (JSONException e) {
                        showResultInUiThread(mContext
                                .getString(R.string.wifi_register_wrong_format));
                        Logger.debug(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private String getRegisterUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append("http://www.591wifi.com/portal/deputize?");
        String phonenumber = mPhoneNumberView.getText().toString();
        String password = mPasswordNumberView.getText().toString();
        builder.append("UserName=" + phonenumber);
        builder.append("&Mobile=" + phonenumber);
        builder.append("&Password_1=" + password);
        builder.append("&Password_2=" + password);
        builder.append("&scodes=" + mTokenView.getText().toString());
        String username = mUsernameView.getText().toString();
        if (!TextUtils.isEmpty(username)) {
            try {
                builder.append("&RealName="
                        + URLEncoder.encode(username, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                Logger.debug(TAG, "Encode error: " + e.toString());
                e.printStackTrace();
            }
        }
        Logger.debug(TAG, "builder.toString()");
        return builder.toString();
    }

    private boolean verifyRegisterInfo() {
        boolean result = true;
        if (!verifyPhonNumber() || !verifyToken() || !verifyPassword()) {
            result = false;
        }
        return result;
    }

    private boolean verifyToken() {
        boolean result = true;
        if (TextUtils.isEmpty(mTokenView.getText().toString())) {
            Toast.makeText(this, R.string.wifi_register_null_token,
                    Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }

    private boolean verifyPassword() {
        boolean result = true;
        if (TextUtils.isEmpty(mPasswordNumberView.getText().toString())) {
            Toast.makeText(this, R.string.wifi_register_null_password,
                    Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }
}
