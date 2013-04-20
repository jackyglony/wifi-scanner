package com.shixunaoyou.wifiscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public abstract class BaseCustomDialog extends AlertDialog {

    protected View mContentView;

    protected BaseCustomDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setWifiStyleTitle();
        setInverseBackgroundForced(true);
        super.onCreate(savedInstanceState);
    }

    private void setWifiStyleTitle() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater
                .inflate(R.layout.alertdialog_custom_titlebar, null);
        setCustomTitle(view);
        TextView title = (TextView) view.findViewById(R.id.titlebar_title);
        title.setText(getDialogTitle());
    }

    protected abstract int getDialogTitle();
}
