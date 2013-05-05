package com.shixunaoyou.wifiscanner.wifichest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.Logger;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.View;

public class AppItem {
    private static String TAG = "AppItem";
    private static final String SAVE_PATH = "/android/Downloads/WifiScanner/";

    private static String ID_KEY = "id";
    private static String DESCRIPTION_KEY = "sIntroduction";
    private static String TITLE_KEY = "sAppNameZN";
    private static String ICON_URL_KEY = "sLogo";
    private static String LONG_KEY = "iSize";
    private static String RANKING_KEY = "iRank";
    private static String DOWNLOAD_COUNT = "iDownloadTime";
    private static String UPDATE_TIME = "dUpdateTime";
    private static String DOWNLOAD_URL = "sAddress";
    private static String VERSION = "sVersion";

    private Drawable mAppIcon;
    private String mTitle;
    private View mViewContainter;
    private String mImageUrl;
    private String mDownloadUrl;
    private int mId;
    private int mDownloadProgressValue;
    private String mDescrition;
    private boolean isPaserSuccessful;
    private int mDownloadCount;
    private long mSize;
    private int mRanking;
    // private boolean isImageDownloadCompleted;
    private ImageDownloadListener mListener;
    private String mUpdateTime;
    private String mVersion;
    private String mApkName;
    private boolean mIsShowDescription;
    private boolean mIsDownloading;
    private String mImageLocal;

    private boolean mIsApkExisted;
    private int mPercentage;

    public AppItem(JSONObject o, ImageDownloadListener listener) {
        mListener = listener;
        parseJSONObject(o);
        checkApkFile();
        loadingImage();
    }

    private void parseJSONObject(JSONObject o) {
        isPaserSuccessful = true;
        try {
            mTitle = o.getString(TITLE_KEY);
            mId = Integer.parseInt(o.getString(ID_KEY));
            mSize = Long.parseLong(o.getString(LONG_KEY));
            mImageUrl = o.getString(ICON_URL_KEY);
            mDescrition = o.getString(DESCRIPTION_KEY);
            mDownloadCount = Integer.parseInt(o.getString(DOWNLOAD_COUNT));
            mRanking = Integer.parseInt(o.getString(RANKING_KEY));
            mUpdateTime = o.getString(UPDATE_TIME);
            mDownloadUrl = o.getString(DOWNLOAD_URL);
            mVersion = o.getString(VERSION);
            mApkName = mTitle + ".apk";
            mImageLocal = Environment.getExternalStorageDirectory() + SAVE_PATH
                    + mTitle;
        } catch (JSONException e) {
            isPaserSuccessful = false;
            Logger.debug(TAG, "Error: " + e.toString());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            isPaserSuccessful = false;
            Logger.debug(TAG, "Error: " + e.toString());
            e.printStackTrace();
        }
    }

    private void loadingImage() {
        if (!loadLocalImage()) {
            startDownloadImage();
        }
    }

    private boolean loadLocalImage() {
        boolean isSuccessful = false;
        File file = new File(mImageLocal);
        if (file.exists()) {
            mAppIcon = Drawable.createFromPath(mImageLocal);
            if (mAppIcon != null) {
                isSuccessful = true;
            }
        }
        return isSuccessful;
    }

    private void startDownloadImage() {

        if (isPaserSuccessful) {
            ImageDownloadTask task = new ImageDownloadTask();
            if (Build.VERSION.SDK_INT > 11) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
    }

    private void checkApkFile() {
        File file = new File(getApkFullPath());
        if (file.exists()) {
            mIsApkExisted = true;
        } else {
            mIsApkExisted = false;
        }
    }

    public boolean isApkExisted() {
        return mIsApkExisted;
    }

    public boolean isShowDescription() {
        return mIsShowDescription;
    }

    public void setShowingDescription(boolean isShow) {
        mIsShowDescription = isShow;
    }

    public boolean isDownloading() {
        return mIsDownloading;
    }

    public void setIsDownloading(boolean isDownloading) {
        mIsDownloading = isDownloading;
    }

    public String getApkName() {
        return mApkName;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public boolean isParseSuccessful() {
        return isPaserSuccessful;
    }

    public String getVersion() {
        return mVersion;
    }

    public String getUpdateTime() {
        return mUpdateTime;
    }

    public Drawable getAppIcon() {
        return mAppIcon;
    }

    public int getRanking() {
        return mRanking;
    }

    public void setAppIcon(Drawable mAppIcon) {
        this.mAppIcon = mAppIcon;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getDownload() {
        return mDownloadCount;
    }

    public long getSize() {
        return mSize;
    }

    public int getId() {
        return mId;
    }

    public String getDescription() {
        return mDescrition;
    }

    public View getViewContainter() {
        return mViewContainter;
    }

    public int getProgessBar() {
        return mDownloadProgressValue;
    }

    public void setViewContainter(View mViewContainter) {
        this.mViewContainter = mViewContainter;
    }

    public int getPercentage() {
        return mPercentage;
    }

    public void setPercentage(int mPercentage) {
        this.mPercentage = mPercentage;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    class ImageDownloadTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            int status = Constants.STATUS_UNKOWN;
            try {
                downloadImage();
                status = Constants.DOWNLOAD_COMPLETE;
            } catch (Exception e) {
                status = Constants.DOWNLOAD_ERROR;
                // TODO Auto-generated catch block
                Logger.debug(TAG, "error: " + e.toString());
                e.printStackTrace();
            }
            return status;
        }

        private void downloadImage() throws Exception {
            URL url;
            url = new URL(mImageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(200000);
            File apkFile = new File(mImageLocal);
            FileOutputStream fos = new FileOutputStream(apkFile);
            InputStream is = conn.getInputStream();
            byte buf[] = new byte[1024 * 2];
            do {
                int numread = is.read(buf);
                if (numread <= 0) {
                    break;
                }
                fos.write(buf, 0, numread);
            } while (true);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == Constants.DOWNLOAD_COMPLETE) {
                mAppIcon = Drawable.createFromPath(mImageLocal);
                mListener.onImageDowlnloadCompleted();
            }
        }
    }

    public String getApkFullPath() {
        return Environment.getExternalStorageDirectory() + Constants.SAVE_PATH
                + getApkName();
    }

    public String getTempApkFullPath() {
        return Environment.getExternalStorageDirectory() + Constants.SAVE_PATH
                + getApkName() + ".tmp";
    }

    public void updateStatus() {
        checkApkFile();
    }
}
