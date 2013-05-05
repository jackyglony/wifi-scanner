package com.shixunaoyou.wifiscanner;

import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;
import com.shixunaoyou.wifiscanner.wifi.WiFiScanService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class ExitConfirmDialog extends BaseCustomDialog implements
        View.OnClickListener {

    private Context mContext;

    public ExitConfirmDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getViews();
        initButtons();
        setMessage(R.string.wifi_exit_message);
        super.onCreate(savedInstanceState);
    }

    private void getViews() {
        mContentView = getLayoutInflater().inflate(
                R.layout.base_custom_alertdlg, null);
        setView(mContentView);
    }

    private void initButtons() {
        Button exitButton = (Button) mContentView
                .findViewById(R.id.base_custom_ok_btn);
        Button cancelButton = (Button) mContentView
                .findViewById(R.id.base_custom_cancel_btn);
        exitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.wifi_exit_confirm;
    }

    protected void setMessage(int resId) {
        TextView message = (TextView) mContentView
                .findViewById(R.id.base_custom_dlg_message);
        message.setText(resId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.base_custom_ok_btn) {
            onOkPressed();
        }
        dismiss();
    }

    protected void onOkPressed() {
        startService();
        ((Activity)mContext).finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void startService() {
        // if we enable auto login, we start the service when we exit the view
        if (Utils.getEnableAutoLogin(mContext)) {
            Logger.debug("Exit Dialog", "OnPause: ready to start service");
            Intent intentService = new Intent(mContext, WiFiScanService.class);
            mContext.startService(intentService);
        }
    }
}
