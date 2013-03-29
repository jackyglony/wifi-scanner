package com.shixunaoyou.wifiscanner;

import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PersonCentreActivity extends Activity implements
        View.OnClickListener, OnDismissListener {

    private Button mRegisterBtn;
    private Button mChangeAccountBtn;
    private Button mForgetAccountBtn;
    private Button mAccountSettingsBtn;
    private TextView mLoginStatusTextView;
    private TextView mAccountNameTextView;
    private View mHaveAccountButtons;
    private View mNoAccountButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_centre_layout);
        getViews();
        initButtonsListener();
    }

    private void getViews() {
        mHaveAccountButtons = this
                .findViewById(R.id.person_have_account_buttons);
        mNoAccountButtons = this.findViewById(R.id.person_no_account_buttons);

        mRegisterBtn = (Button) this.findViewById(R.id.person_register_btn);
        mAccountSettingsBtn = (Button) this
                .findViewById(R.id.person_settings_btn);
        mChangeAccountBtn = (Button) this
                .findViewById(R.id.person_change_account_btn);
        mForgetAccountBtn = (Button) this
                .findViewById(R.id.person_forget_account_btn);
        mLoginStatusTextView = (TextView) this
                .findViewById(R.id.person_account_status);
        mAccountNameTextView = (TextView) this
                .findViewById(R.id.person_account_name);
    }

    private void initButtonsListener() {
        mRegisterBtn.setOnClickListener(this);
        mChangeAccountBtn.setOnClickListener(this);
        mForgetAccountBtn.setOnClickListener(this);
        mAccountSettingsBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private void updateViews() {
        updateLoginStatusText();
        updateButtonsStatus();
    }

    private void updateLoginStatusText() {
        if (Utils.getLoginStatus(this) == Constants.HAVE_LOGIN) {
            mLoginStatusTextView.setText(R.string.person_centre_logout_status);
        } else {
            mLoginStatusTextView.setText(R.string.person_centre_logout_status);
        }
    }

    private void updateButtonsStatus() {
        String accountName = Utils.getUserName(this);
        if (TextUtils.isEmpty(accountName)) {
            mHaveAccountButtons.setVisibility(View.GONE);
            mNoAccountButtons.setVisibility(View.VISIBLE);
            mAccountNameTextView.setVisibility(View.GONE);
        } else {
            mHaveAccountButtons.setVisibility(View.VISIBLE);
            mNoAccountButtons.setVisibility(View.GONE);
            mAccountNameTextView.setVisibility(View.VISIBLE);
            mAccountNameTextView.setText(accountName);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.person_register_btn:
                registerAccount();
                break;
            case R.id.person_settings_btn:
                setAccount();
                break;
            case R.id.person_forget_account_btn:
                forgetAccount();
                break;
            case R.id.person_change_account_btn:
                changeAccount();
                break;
            default:
                break;
        }
    }

    private void registerAccount() {
        Intent intent = new Intent(this, RegisterActivity.class);
        this.startActivity(intent);
    }

    private void setAccount() {
        changeAccount();
    }

    private void forgetAccount() {
        Utils.clearAccount(this);
        updateButtonsStatus();
    }

    private void changeAccount() {
        AccountSettingsDialog dlg = new AccountSettingsDialog(this, this);
        dlg.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        updateButtonsStatus();
    }
}
