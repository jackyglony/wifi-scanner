package com.cm.wifiscanner;

import com.cm.wifiscanner.util.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import android.widget.EditText;

public class GatewaySettingsDialog extends AlertDialog implements
        DialogInterface.OnClickListener, TextWatcher {

    private EditText mGatewayName;

    private Context mContext;

    public GatewaySettingsDialog(Context context, OnDismissListener listener) {
        super(context);
        mContext = context;
        this.setOnDismissListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(
                R.layout.gateway_configure_dialog, null);
        mGatewayName = (EditText) view.findViewById(R.id.gateway_name_edit);
        mGatewayName.addTextChangedListener(this);
        setTitle(mContext.getString(R.string.wifi_configure_gateway));
        setView(view);
        setInverseBackgroundForced(true);
        setButton(DialogInterface.BUTTON_POSITIVE,
                mContext.getString(R.string.wifi_account_dialog_save), this);
        setButton(DialogInterface.BUTTON_NEGATIVE,
                mContext.getString(R.string.wifi_account_dialog_cancel), this);

        super.onCreate(savedInstanceState);
        validate();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Utils.setGateway(mContext, mGatewayName.getText().toString());
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
        if (TextUtils.isEmpty(mGatewayName.getText().toString())) {
            getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }else {
            getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        }
    }
}
