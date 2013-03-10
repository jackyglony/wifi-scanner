package com.cm.wifiscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;

public class CheckUpdateAsyncTask extends AsyncTask<Void, Void, Boolean>
        implements OnClickListener {

    private CheckCompletedListener mCompletedListener;
    private Context mContext;

    CheckUpdateAsyncTask(Context context, CheckCompletedListener listener) {
        super();
        mContext = context;
        mCompletedListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: check whether a new version has been published.
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setPositiveButton(R.string.update_background_update, this);
        builder.setMessage(R.string.update_find_new_version_msg).setTitle(
                R.string.update_find_new_version_title);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
        mCompletedListener.onCheckCompleted(result);
    }

    public static interface CheckCompletedListener {
        public void onCheckCompleted(boolean isHaveNewVersion);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            startDownloadService();
        }
    }

    private void startDownloadService() {
        // Logger.debug(TAG, "CheckUpdateAsyncTask CallBack: " +
        // isHaveNewVersion);
        Intent startUpdateIntent = new Intent(mContext,
                DownloadUpdateService.class);
        mContext.startService(startUpdateIntent);
    }
}
