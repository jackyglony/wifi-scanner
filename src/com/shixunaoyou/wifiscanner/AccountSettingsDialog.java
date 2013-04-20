package com.shixunaoyou.wifiscanner;

import java.lang.reflect.Field;

import com.shixunaoyou.wifiscanner.util.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class AccountSettingsDialog extends BaseCustomDialog implements
        View.OnClickListener, TextWatcher, OnCheckedChangeListener {

    public static final int BUTTON_EDIT = DialogInterface.BUTTON_POSITIVE;

    public static final int BUTTON_DELETE = DialogInterface.BUTTON_NEUTRAL;

    private static final int EDIT_MODE = 1;

    private static final int VIEW_MODE = 2;

    private View mContentView;

    private View mAccountInfo;

    private View mAccountInputView;

    private TextView mAccountView;

    private EditText mNameEdit;

    private EditText mPasswordEdit;

    private CheckBox mShowPasswordCheckBox;

    private Button mSaveButton;
    private Button mCancelButton;

    private Context mContext;

    private int mMode = EDIT_MODE;

    public AccountSettingsDialog(Context context, OnDismissListener listener) {
        super(context);
        mContext = context;
        this.setOnDismissListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getViews();
        initiateView();
        initButtons();
        super.onCreate(savedInstanceState);
        validate();
    }

    private void getViews() {
        mContentView = getLayoutInflater().inflate(R.layout.account_dialog,
                null);
        setView(mContentView);

        mAccountInputView = mContentView
                .findViewById(R.id.account_input_fields);
        mAccountInfo = mContentView.findViewById(R.id.account_info);
        mAccountView = (TextView) mContentView
                .findViewById(R.id.current_account_info);
        mNameEdit = (EditText) mContentView.findViewById(R.id.account_user);
        mPasswordEdit = (EditText) mContentView
                .findViewById(R.id.account_password);
        mShowPasswordCheckBox = (CheckBox) mContentView
                .findViewById(R.id.account_show_password);
        mShowPasswordCheckBox.setOnCheckedChangeListener(this);
        mPasswordEdit.addTextChangedListener(this);
        mNameEdit.addTextChangedListener(this);
    }

    private void initiateView() {
        if (mMode == EDIT_MODE) {
            mAccountInfo.setVisibility(View.GONE);
            mAccountInputView.setVisibility(View.VISIBLE);
            mMode = EDIT_MODE;
        } else {
            mAccountInfo.setVisibility(View.VISIBLE);
            mAccountInputView.setVisibility(View.GONE);
            mAccountView.setText(Utils.getUserName(mContext));
            mMode = VIEW_MODE;
        }
        // setButtons(mMode);
    }

    private void initButtons() {
        mSaveButton = (Button) mContentView
                .findViewById(R.id.account_dialog_save);
        mCancelButton = (Button) mContentView
                .findViewById(R.id.account_dialog_cancel);
        mSaveButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        validate();
    }

    private void validate() {
        if (TextUtils.isEmpty(mNameEdit.getText().toString())
                || TextUtils.isEmpty(mPasswordEdit.getText().toString())) {
            mSaveButton.setEnabled(false);
        } else {
            mSaveButton.setEnabled(true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (R.id.account_show_password == buttonView.getId()) {
            mPasswordEdit
                    .setInputType(InputType.TYPE_CLASS_TEXT
                            | (isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                    : InputType.TYPE_TEXT_VARIATION_PASSWORD));
        }
    }

    @SuppressWarnings("unused")
    private void setDialogCanotDismiss(boolean canDismiss) {
        try {
            Field field = this.getClass().getSuperclass().getSuperclass()
                    .getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(this, canDismiss);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.account_dialog_save) {
            String name = mNameEdit.getText().toString();
            String password = mPasswordEdit.getText().toString();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                Utils.setPassword(mContext, password);
                Utils.setUserName(mContext, name);
            }
        }
        this.dismiss();
    }

    @Override
    protected int getDialogTitle() {
        return R.string.wifi_account_settings;
    }
}
