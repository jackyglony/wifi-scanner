package com.shixunaoyou.wifiscanner.wifichest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadHandler {

    private static final String TAG = "DownloadHandler";
    private AppItem mItem;
    private View mView;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private String mFileFullPath;
    private String mDownloadUrl;
    private View mProgressContainer;
    private Button mDownloadButton;
    private Context mContext;
    private DownloadListener mDownloadListener;

    public DownloadHandler(Context context, AppItem item, View view) {
        mItem = item;
        mView = view;
        mContext = context;
        initialData();
    }

    private void initialData() {
        initialFileData();
        initalView();
    }

    private void initialFileData() {
        mFileFullPath = mItem.getTempApkFullPath();
        mDownloadUrl = mItem.getDownloadUrl();
    }

    private void initalView() {
        mProgressContainer = mView
                .findViewById(R.id.wifi_app_progress_container);
        mProgressBar = (ProgressBar) mView
                .findViewById(R.id.wifi_app_progressbar);
        mTextView = (TextView) mView.findViewById(R.id.wifi_app_progess_value);
        mDownloadButton = (Button) mView
                .findViewById(R.id.wifi_app_download_btn);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);
        mTextView.setText("0%");
    }

    public void startDownload() {
        DownloadTask task = new DownloadTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
    }

    class DownloadTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            int status = Constants.STATUS_UNKOWN;
            try {
                downloadApk(mDownloadUrl, mFileFullPath);
                status = Constants.DOWNLOAD_COMPLETE;

            } catch (Exception e) {
                status = Constants.DOWNLOAD_ERROR;
                Logger.debug(TAG, "error: " + e.toString());
                e.printStackTrace();
            }
            return status;
        }

        private void downloadApk(String address, String mFileFullPath)
                throws Exception {
            URL url;
            url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(200000);
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
            mProgressContainer.setVisibility(View.VISIBLE);
            mDownloadButton.setEnabled(false);
            mItem.setIsDownloading(true);
        }

        @Override
        protected void onPostExecute(Integer result) {
            mProgressContainer.setVisibility(View.GONE);
            mDownloadButton.setEnabled(true);
            mItem.setIsDownloading(false);
            if (result == Constants.DOWNLOAD_COMPLETE) {
                renameApkFile();
                Utils.installUpdateApk(mContext, mItem.getApkFullPath());
                mItem.updateStatus();
            } else {
                Toast.makeText(mContext,
                        R.string.wifi_chest_app_download_failed,
                        Toast.LENGTH_SHORT).show();
            }
            if (mDownloadListener != null) {
                mDownloadListener.onDownloadCompleted(result);
            }
        }

        private void renameApkFile() {
            File file = new File(mFileFullPath);
            if (file.exists()) {
                file.renameTo(new File(mItem.getApkFullPath()));
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            int centage = progress[0];
            if (centage <= 100) {
                mProgressBar.setProgress(progress[0]);
                mTextView.setText(progress[0] + "%");
                mItem.setPercentage(progress[0]);
            }
        }
    }

    public interface DownloadListener {
        public void onDownloadCompleted(int status);
    }
}
