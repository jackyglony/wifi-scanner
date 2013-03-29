package com.shixunaoyou.wifiscanner;

import java.lang.reflect.Field;

import com.shixunaoyou.wifiscanner.util.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class AccountSettingsDialog extends AlertDialog implements
        DialogInterface.OnClickListener, TextWatcher, OnCheckedChangeListener {

    public static final int BUTTON_EDIT = DialogInterface.BUTTON_POSITIVE;

    public static final int BUTTON_DELETE = DialogInterface.BUTTON_NEUTRAL;

    private static final int EDIT_MODE = 1;

    private static final int VIEW_MODE = 2;

    private View mView;

    private View mAccountInfo;

    private View mAccountInputView;

    private TextView mAccountView;

    private EditText mNameEdit;

    private EditText mPasswordEdit;

    private CheckBox mShowPasswordCheckBox;

    private Context mContext;

    private int mMode = EDIT_MODE;

    public AccountSettingsDialog(Context context, OnDismissListener listener) {
        super(context);
        mContext = context;
        this.setOnDismissListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.account_dialog, null);
        mAccountInputView = mView.findViewById(R.id.account_input_fields);
        mAccountInfo = mView.findViewById(R.id.account_info);
        mAccountView = (TextView) mView.findViewById(R.id.current_account_info);
        mNameEdit = (EditText) mView.findViewById(R.id.account_user);
        mPasswordEdit = (EditText) mView.findViewById(R.id.account_password);
        mShowPasswordCheckBox = (CheckBox) mView
                .findViewById(R.id.account_show_password);
        mShowPasswordCheckBox.setOnCheckedChangeListener(this);
        mPasswordEdit.addTextChangedListener(this);
        mNameEdit.addTextChangedListener(this);
        setTitle(mContext.getString(R.string.wifi_account_settings));
        setView(mView);
        initiateView();
        setInverseBackgroundForced(true);
        super.onCreate(savedInstanceState);

        validate();
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
        setButtons(mMode);
    }

    private void setButtons(int mode) {
        switch (mode) {
            case EDIT_MODE:
                setButton(BUTTON_EDIT,
                        mContext.getString(R.string.wifi_account_dialog_save),
                        this);
                break;
            case VIEW_MODE:
                setButton(BUTTON_EDIT,
                        mContext.getString(R.string.wifi_account_dialog_edit),
                        this);
                setButton(
                        BUTTON_DELETE,
                        mContext.getString(R.string.wifi_account_dialog_delete),
                        this);
                break;
            default:
                break;
        }
        setButton(DialogInterface.BUTTON_NEGATIVE,
                mContext.getString(R.string.wifi_account_dialog_cancel), this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        setDialogCanotDismiss(true);
        if (EDIT_MODE == mMode) {
            if (which == BUTTON_EDIT) {
                String name = mNameEdit.getText().toString();
                String password = mPasswordEdit.getText().toString();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                    Utils.setPassword(mContext, password);
                    Utils.setUserName(mContext, name);
                }
            }
        } else {
            if (which == BUTTON_DELETE) {
                Utils.clearAccount(mContext);
            }

            if (which == BUTTON_EDIT) {
                setDialogCanotDismiss(false);
                mAccountInfo.setVisibility(View.GONE);
                mAccountInputView.setVisibility(View.VISIBLE);
                mMode = EDIT_MODE;
                setButtons(mMode);
                mNameEdit.requestFocus();
                getButton(BUTTON_EDIT).setText(
                        R.string.wifi_account_dialog_save);
                getButton(BUTTON_DELETE).setVisibility(View.GONE);
                validate();
            }
        }
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
        if (mMode == EDIT_MODE) {
            if (TextUtils.isEmpty(mNameEdit.getText().toString())
                    || TextUtils.isEmpty(mPasswordEdit.getText().toString())) {
                getButton(BUTTON_EDIT).setEnabled(false);
            } else {
                getButton(BUTTON_EDIT).setEnabled(true);
            }
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
}
