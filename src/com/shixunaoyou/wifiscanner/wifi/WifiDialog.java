package com.shixunaoyou.wifiscanner.wifi;

import com.shixunaoyou.wifiscanner.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class WifiDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {

    public static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    public static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

    public final boolean edit;
    private final DialogInterface.OnClickListener mListener;
    private final AccessPoint mAccessPoint;

    private View mView;
    private TextView mSsid;
    private int mSecurity;
    private TextView mPassword;
    private Spinner mEapMethod;
//    private Spinner mEapCaCert;
//    private Spinner mPhase2;
//    private Spinner mEapUserCert;
//    private TextView mEapIdentity;
//    private TextView mEapAnonymous;

    public static boolean requireKeyStore(WifiConfiguration config) {
//        String values[] = {config.ca_cert.value(), config.client_cert.value(),
//                config.private_key.value()};
//        for (String value : values) {
//            if (value != null && value.startsWith(KEYSTORE_SPACE)) {
//                return true;
//            }
//        }
        return false;
    }

    public WifiDialog(Context context, DialogInterface.OnClickListener listener,
            AccessPoint accessPoint, boolean edit) {
        super(context);
        this.edit = edit;
        mListener = listener;
        mAccessPoint = accessPoint;
        mSecurity = (accessPoint == null) ? AccessPoint.SECURITY_NONE : accessPoint.security;
    }

    public WifiConfiguration getConfig() {
        if (mAccessPoint != null && mAccessPoint.networkId != -1 && !edit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(mSsid.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == -1) {
            config.SSID = AccessPoint.convertToQuotedString(mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPassword.length() != 0) {
                    int length = mPassword.length();
                    String password = mPassword.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                return config;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                return config;

            case AccessPoint.SECURITY_EAP:
                return null;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        setView(mView);
        setInverseBackgroundForced(true);

        Context context = getContext();
        Resources resources = context.getResources();

        if (mAccessPoint == null) {
            setTitle(R.string.wifi_add_network);
            mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
            mSsid = (TextView) mView.findViewById(R.id.ssid);
            mSsid.addTextChangedListener(this);
            ((Spinner) mView.findViewById(R.id.security)).setOnItemSelectedListener(this);
            setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        } else {
            setTitle(mAccessPoint.ssid);
            ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);

            DetailedState state = mAccessPoint.getState();
            if (state != null) {
                addRow(group, R.string.wifi_status, Summary.get(getContext(), state));
            }

            String[] type = resources.getStringArray(R.array.wifi_security);
            addRow(group, R.string.wifi_security, type[mAccessPoint.security]);

            int level = mAccessPoint.getLevel();
            if (level != -1) {
                String[] signal = resources.getStringArray(R.array.wifi_signal);
                addRow(group, R.string.wifi_signal, signal[level]);
            }

            WifiInfo info = mAccessPoint.getInfo();
            if (info != null) {
                addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
                // TODO: fix the ip address for IPv6.
                int address = info.getIpAddress();
                if (address != 0) {
                    addRow(group, R.string.wifi_ip_address, Formatter.formatIpAddress(address));
                }
            }

            if (mAccessPoint.networkId == -1
                    || mSecurity == AccessPoint.SECURITY_NONE
                    || edit) {
                showSecurityFields();
            }

            if (edit) {
                setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
            } else {
                if (state == null && level != -1) {
                    setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_connect), mListener);
                }
                if (mAccessPoint.networkId != -1) {
                    setButton(BUTTON_FORGET, context.getString(R.string.wifi_forget), mListener);
                }
            }
        }

        setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.wifi_cancel), mListener);

        super.onCreate(savedInstanceState);

        if (getButton(BUTTON_SUBMIT) != null) {
            validate();
        }
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(name);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }

    private void validate() {
        // TODO: make sure this is complete.
        if ((mSsid != null && mSsid.length() == 0) ||
                ((mAccessPoint == null || mAccessPoint.networkId == -1) &&
                ((mSecurity == AccessPoint.SECURITY_WEP && mPassword.length() == 0) ||
                (mSecurity == AccessPoint.SECURITY_PSK && mPassword.length() < 8)))) {
            getButton(BUTTON_SUBMIT).setEnabled(false);
        } else {
            getButton(BUTTON_SUBMIT).setEnabled(true);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_password:
                mPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                        InputType.TYPE_TEXT_VARIATION_PASSWORD));
                break;
            default:
                break;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        mSecurity = position;
        showSecurityFields();
        validate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void showSecurityFields() {
        if (mSecurity == AccessPoint.SECURITY_NONE) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);

        } else {
            mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
            if (mPassword == null) {
                mPassword = (TextView) mView.findViewById(R.id.password);
                mPassword.addTextChangedListener(this);
                ((CheckBox) mView.findViewById(R.id.show_password))
                        .setOnClickListener(this);

                if (mAccessPoint != null && mAccessPoint.networkId != -1) {
                    mPassword.setHint(R.string.wifi_unchanged);
                }
            }

            if (mSecurity != AccessPoint.SECURITY_EAP) {
                mView.findViewById(R.id.eap).setVisibility(View.GONE);
                return;
            }
            mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);

            if (mEapMethod == null) {
                // TODO deal with EAP condition
            }
        }
    }
}
