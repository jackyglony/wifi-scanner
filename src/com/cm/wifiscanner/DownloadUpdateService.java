package com.cm.wifiscanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.cm.wifiscanner.util.Constants;
import com.cm.wifiscanner.util.Logger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.widget.Toast;

public class DownloadUpdateService extends Service {
    private static final String LOG_TAG = "DownloadUpdateService";
    
    private static final int DOWNLOAD_TIMEOUT = 20 * 1000;

    private static final String SAVE_PATH = "/android/Downloads/WifiScanner/";
    private static final String FILENAME = "WifiScanner.apk";
    // TODO: Test URL
    private String apkUrl = "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk";
    private String mFileFullPath;

    private Context mContext;
    private boolean isUpdating = false;
    private DownloadNotificationHandler mNotificationHandler;

    @Override
    public void onCreate() {
        mContext = this;
        mNotificationHandler = new DownloadNotificationHandler(mContext);
    }

    @Override
    public void onDestroy() {
        Logger.debug(LOG_TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isHaveNewVersion() {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Logger.debug(LOG_TAG, "DownloadUpdateService Started");
        if (!isUpdating) {
            DownloadTask task = new DownloadTask();
            if (Build.VERSION.SDK_INT >= 11) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
        return Service.START_NOT_STICKY;
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        Logger.debug(LOG_TAG, "PATH: " + path);
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private boolean checkAndUpdatePath() {
        boolean result = false;
        File file = new File(Environment.getExternalStorageDirectory()
                + SAVE_PATH);
        if (!file.exists()) {
            result = file.mkdirs();
        }
        result = file.canWrite();
        if (result) {
            mFileFullPath = Environment.getExternalStorageDirectory()
                    + SAVE_PATH + FILENAME;
        }
        return result;
    }

    private int checkPreConditionBeforeDownload() {
        int status = Constants.STATUS_UNKOWN;
        if (!checkAndUpdatePath()) {
            status = Constants.NO_AVAIL_DISK;
        } else {
            status = Constants.PRE_CONDITION_OK;
        }
        return status;
    }

//    private int checkMemorySizeOfSDCard() {
//        int status = Constants.NO_ENOUGH_SPACE;
//        if (getAvailableInternalMemorySize() > 0) {
//            status = Constants.PRE_CONDITION_OK;
//        }
//        return status;
//    }

    private void showToastMsg(int msgResId) {
        Toast.makeText(mContext, msgResId, Toast.LENGTH_SHORT).show();
    }

    
    class DownloadTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            Logger.debug(LOG_TAG, "doInBackground ");
            int status = Constants.STATUS_UNKOWN;
            status = checkPreConditionBeforeDownload();
            if (status == Constants.PRE_CONDITION_OK) {
                try {
                    downloadApk();
                    status = Constants.DOWNLOAD_COMPLETE;
                } catch (Exception e) {
                    status = Constants.DOWNLOAD_ERROR;
                    e.printStackTrace();
                }
            }
            return status;
        }

        private void downloadApk() throws Exception {
            URL url;
            url = new URL(apkUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(DOWNLOAD_TIMEOUT);
            int length = conn.getContentLength();
            File apkFile = new File(mFileFullPath);
            FileOutputStream fos = new FileOutputStream(apkFile);
            InputStream is = conn.getInputStream();
            int count = 0;
            byte buf[] = new byte[1024 * 2];
            int progress = 0;
            do {
                int numread = is.read(buf);
                count += numread;
                progress = (int) (((float) count / length) * 100);
                publishProgress(progress);
                if (numread <= 0) {
                    break;
                }
                fos.write(buf, 0, numread);
            } while (true);

        }

        @Override
        protected void onPreExecute() {
            isUpdating = true;
            showToastMsg(R.string.update_start_download_toast);
            mNotificationHandler.sendNotification();
        }

        @Override
        protected void onPostExecute(Integer result) {
            Logger.debug(LOG_TAG, "onPostExecute: " + result);
            mNotificationHandler.cancelNotification();
            isUpdating = false;
            if (result != Constants.DOWNLOAD_COMPLETE) {
                showErrorMessage(result);
            } else {
                installUpdateApk();
            }
            DownloadUpdateService.this.stopSelf();
        }

        private void installUpdateApk() {
            File apkfile = new File(mFileFullPath);
            if (!apkfile.exists()) {
                return;
            }
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setDataAndType(
                    Uri.parse("file://" + apkfile.toString()),
                    "application/vnd.android.package-archive");
            mContext.startActivity(installIntent);
        }

        private void showErrorMessage(Integer result) {
            int errorMsgResId = R.string.update_unknown_error_toast;
            switch (result) {
                case Constants.NO_AVAIL_DISK:
                    errorMsgResId = R.string.update_no_avail_dick_toast;
                    break;
                case Constants.NO_ENOUGH_SPACE:
                    errorMsgResId = R.string.update_no_enough_space_toast;
                    break;
                case Constants.DOWNLOAD_ERROR:
                    errorMsgResId = R.string.update_download_error_toast;
                    break;
                default:
                    break;
            }
            showToastMsg(errorMsgResId);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            Logger.debug(LOG_TAG, "Have completed download %" + progress[0]);
            mNotificationHandler.updateNotification(progress[0]);
        }
    }
}
