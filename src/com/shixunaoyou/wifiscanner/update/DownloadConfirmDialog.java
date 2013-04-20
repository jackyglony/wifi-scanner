package com.shixunaoyou.wifiscanner.update;

import com.shixunaoyou.wifiscanner.BaseCustomDialog;
import com.shixunaoyou.wifiscanner.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DownloadConfirmDialog extends BaseCustomDialog implements
        View.OnClickListener {

    private View mContentView;
    private Context mContext;
    private String mUpdateContent;
    private TextView mUpdateContentView;

    protected DownloadConfirmDialog(Context context, String updateContent) {
        super(context);
        mContext = context;
        mUpdateContent = updateContent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getViews();
        initButtons();
        super.onCreate(savedInstanceState);
    }

    private void getViews() {
        mContentView = getLayoutInflater().inflate(
                R.layout.update_confirm_dialog_layout, null);
        mUpdateContentView = (TextView) mContentView
                .findViewById(R.id.update_confirm_dialog_updateinfo);
        setView(mContentView);
        initView();
    }

    private void initView() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder
                .append(mContext.getString(R.string.update_content_message))
                .append('\n').append(mUpdateContent);
        mUpdateContentView.setText(messageBuilder.toString());
    }

    private void initButtons() {
        Button downloadBtn = (Button) mContentView
                .findViewById(R.id.update_confirm_dialog_download);
        Button cancelBtn = (Button) mContentView
                .findViewById(R.id.update_confirm_dialog_cancel);
        downloadBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.update_confirm_dialog_download) {
            startDownloadService();
        }
        dismiss();
    }

    private void startDownloadService() {
        Intent startUpdateIntent = new Intent(mContext,
                DownloadUpdateService.class);
        mContext.startService(startUpdateIntent);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.update_find_new_version_title;
    }
}
