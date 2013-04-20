package com.shixunaoyou.wifiscanner.wifichest;

import org.json.JSONException;
import org.json.JSONObject;

import com.shixunaoyou.wifiscanner.util.Logger;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

public class AppItem {
    private static String TAG = "AppItem";
    private static String ID_KEY = "id";
    private static String DESCRIPTION_KEY = "iDescription";
    private static String TITLE_KEY = "sAppNameZN";
    private static String ICON_URL_KEY = "sLogo";
    private static String LONG_KEY = "iSize";
    private static String RANKING_KEY = "iRanking";
    private static String DOWNLOAD_COUNT = "iDownloadTime";
    private Drawable mAppIcon;
    private String mTitle;
    private View mViewContainter;
    private String mImageUrl;
    private int mId;
    private int mDownloadProgressValue;
    private String mDescrition;
    private boolean isPaserSuccessful;
    private int mDownloadCount;
    private long mSize;
    private int mRanking;
    private boolean isImageDownloadCompleted;
    private ImageDownloadListener mListener;

    public AppItem(JSONObject o, ImageDownloadListener listener) {
        mListener = listener;
        parseJSONObject(o);
        startDownloadImage();
    }

    private void parseJSONObject(JSONObject o) {
        isPaserSuccessful = true;
        try {
            mTitle = o.getString(TITLE_KEY);
            mId = Integer.parseInt(o.getString(ID_KEY));
            mSize = Long.parseLong(o.getString(LONG_KEY));
            mImageUrl = o.getString(ICON_URL_KEY);
            mDescrition = o.getString(DESCRIPTION_KEY);
            mRanking = Integer.parseInt(o.getString(RANKING_KEY));
            mDownloadCount = Integer.parseInt(o.getString(DOWNLOAD_COUNT));

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

    public boolean isParseSuccessful() {
        return isPaserSuccessful;
    }

    private void startDownloadImage() {
        if (isPaserSuccessful) {
        }
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
}
