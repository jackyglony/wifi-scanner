package com.shixunaoyou.wifiscanner.update;

import org.json.JSONException;
import org.json.JSONObject;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.HttpUtils;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

public class CheckUpdateAsyncTask extends AsyncTask<Void, Void, JSONObject>
        implements OnClickListener {

    private final static String TAG = "CheckUpdateAsyncTask";
    private final String UPDATE_URL = "http://www.591wifi.com/portal/appupdate";
    private Context mContext;
    private boolean mShoulShowInfo = true;

    public CheckUpdateAsyncTask(Context context,
            CheckCompletedListener listener, boolean showInfo) {
        super();
        mContext = context;
        mShoulShowInfo = showInfo;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        JSONObject result = null;
        try {
            result = HttpUtils.sendPostRequest(UPDATE_URL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if (result != null) {
            try {
                String errNo = result.getString("errno");
                if (TextUtils.equals(errNo, Constants.SUCCESSFUL_CODE)) {
                    String version = result.getString("version");
                    String updateContent = result.getString("updateInfo");
                    if (checkVersion(version)) {
                        Utils.setUpdateUrl(mContext,
                                result.getString("updateUrl"));
                        showUpdateDialog(updateContent);
                    } else {
                        if (mShoulShowInfo) {
                            Toast.makeText(mContext,
                                    R.string.update_not_need_update,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (mShoulShowInfo) {
                        String errMsg = result.getString("errMsg");
                        Toast.makeText(mContext, errMsg, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            } catch (JSONException e) {
                if (mShoulShowInfo) {
                    Toast.makeText(mContext,
                            R.string.wifi_register_wrong_format,
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkVersion(String version) {
        boolean needUpdate = false;
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            String currentVersion = pInfo.versionName;
            float currVersion = Float.valueOf(currentVersion);
            float romoteVersion = Float.valueOf(version);
            Logger.debug(TAG, "current Version: " + currentVersion
                    + " remote Version : " + romoteVersion);
            if (currVersion < romoteVersion) {
                needUpdate = true;
            }
        } catch (NameNotFoundException e) {
            Logger.debug(TAG, "romoteVersion error: " + e.toString());
            e.printStackTrace();
        }
        return needUpdate;
    }

    private void showUpdateDialog(String updateContent) {
        if (!checkActivityStillAlive()) {
            Logger.debug(TAG, "Activity has finished");
            return;
        }
        DownloadConfirmDialog dlg = new DownloadConfirmDialog(mContext,
                updateContent);
        dlg.show();
    }

    private boolean checkActivityStillAlive() {
        if (mContext instanceof Activity) {
            Activity parent = (Activity) mContext;
            if (parent.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    public static interface CheckCompletedListener {
        public void onCheckCompleted(JSONObject isHaveNewVersion);
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
