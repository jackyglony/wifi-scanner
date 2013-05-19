package com.shixunaoyou.wifiscanner.wifichest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Constants;
import com.shixunaoyou.wifiscanner.util.HttpUtils;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.UMengUtils;
import com.shixunaoyou.wifiscanner.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WifiChestActivity extends ListActivity implements
        AdapterView.OnItemClickListener, View.OnClickListener,
        ImageDownloadListener, DownloadHandler.DownloadListener {
    private static String TAG = "WifiChestActivity";
    private static String APP_URL = "http://www.591wifi.com/portal/showallapp";
    private View mLoadingContainer;
    private View mListContainer;
    private View mRefreshContainer;
    private List<AppItem> mAppList;
    private AppItemAdapter mAdapter;
    private Button mRefreshButton;
    private boolean mPreLoadSuccessfully;
    private boolean mNeedRefreshData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_chest_layout);
        getViews();
        mPreLoadSuccessfully = preLoadList();
        if (preLoadList()) {
            mLoadingContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        }
        reloadList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkAndCreatePath();
        if (mAppList != null) {
            for (AppItem item : mAppList) {
                item.updateStatus();
            }
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void getViews() {
        mLoadingContainer = findViewById(R.id.wifi_chest_loading_container);
        mListContainer = findViewById(R.id.wifi_chest_applist_container);
        mRefreshContainer = findViewById(R.id.wifi_chest_refresh_container);
        mRefreshButton = (Button) findViewById(R.id.wifi_chest_refresh_btn);
        mRefreshButton.setOnClickListener(this);
    }

    private boolean preLoadList() {
        // TODO Auto-generated method stub
        boolean isSuccessfully = false;
        String appList = Utils.getAppList(this);
        if (appList != null) {
            try {
                JSONObject jsonList = new JSONObject(appList);
                mAppList = getApplistFromJSONObject(jsonList);
                setAdapter();
                isSuccessfully = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return isSuccessfully;
    }

    private void setAdapter() {
        mAdapter = new AppItemAdapter();
        setListAdapter(mAdapter);
    }

    private int getRatingResource(int rank) {
        int resId = R.drawable.rating_bg_10;
        switch (rank) {
            case 1:
                resId = R.drawable.rating_bg_1;
                break;
            case 2:
                resId = R.drawable.rating_bg_2;
                break;
            case 3:
                resId = R.drawable.rating_bg_3;
                break;
            case 4:
                resId = R.drawable.rating_bg_4;
                break;
            case 5:
                resId = R.drawable.rating_bg_5;
                break;
            case 6:
                resId = R.drawable.rating_bg_6;
                break;
            case 7:
                resId = R.drawable.rating_bg_7;
                break;
            case 8:
                resId = R.drawable.rating_bg_8;
                break;
            case 9:
                resId = R.drawable.rating_bg_9;
                break;
            case 10:
                resId = R.drawable.rating_bg_10;
                break;
            default:
                break;
        }
        return resId;
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, final View view,
            int position, long id) {
        // Logger.debug(TAG, "onItemClick");
        // AppItemViewHolder holder = (AppItemViewHolder) view.getTag();
        // if (holder.mHideContainer.getVisibility() == View.GONE) {
        // holder.mHideContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        Logger.debug(TAG, "onClick");
        int id = v.getId();
        if (id == R.id.wifi_chest_refresh_btn) {
            reloadList();
        } else if (id == R.id.wifi_app_download_btn) {
            onStartDownloadApk(v);
        } else if (id == R.id.wifi_app_install_btn) {
            onInstallApk(v);
        }
    }

    private void reloadList() {
        LoadProcossTask task = new LoadProcossTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private void onStartDownloadApk(View v) {
        AppItem item = (AppItem) v.getTag(R.id.wifi_app_download_count);
        View parentView = (View) v.getTag(R.id.wifi_app_download_btn);
        DownloadHandler handler = new DownloadHandler(this, item, parentView);
        MobclickAgent.onEvent(this, UMengUtils.EVENT_DOWNLOAD_APP,
                item.getTitle());
        handler.startDownload();
    }

    private void onInstallApk(View v) {
        AppItem item = (AppItem) v.getTag(R.id.wifi_app_install_btn);
        if (item.isApkExisted()) {
            Utils.installUpdateApk(this, item.getApkFullPath());
        }
    }

    @Override
    public void onImageDowlnloadCompleted() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadCompleted(int status) {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private List<AppItem> getApplistFromJSONObject(JSONObject json)
            throws JSONException {
        List<AppItem> list = new ArrayList<AppItem>();
        JSONArray apps = (JSONArray) json.get("showallapp");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            AppItem item = new AppItem(app, WifiChestActivity.this);
            list.add(item);
        }
        return list;
    }

    class AppItemAdapter extends BaseAdapter {

        public AppItemAdapter() {
        }

        public int getCount() {
            return mAppList.size();
        }

        public Object getItem(int position) {
            return mAppList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                ViewGroup parent) {
            AppItem item = mAppList.get(position);
            if (convertView == null) {
                LayoutInflater inflater = WifiChestActivity.this
                        .getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.wifi_chest_applicaition_item, null);
            }
            final AppItemViewHolder holder = getViewHolder(convertView);
            setTags(convertView, holder, item);
            setButtonsClickListener(convertView, holder, position);
            updateView(holder, item);
            return convertView;
        }

        private void setTags(View convertView, AppItemViewHolder holder,
                AppItem item) {
            convertView.setTag(holder);
            convertView.setTag(R.id.wifi_app_download_count, item);
            holder.mDownloadButton.setOnClickListener(WifiChestActivity.this);
            holder.mDownloadButton.setTag(R.id.wifi_app_download_btn,
                    convertView);

            holder.mInstallButton.setOnClickListener(WifiChestActivity.this);
            holder.mInstallButton.setTag(R.id.wifi_app_install_btn, item);

            convertView.setTag(R.id.wifi_app_download_count, item);
            holder.mDownloadButton.setTag(R.id.wifi_app_download_count, item);
        }

        private AppItemViewHolder getViewHolder(View convertView) {
            AppItemViewHolder holder = new AppItemViewHolder(convertView);
            holder.mDescription = (TextView) convertView
                    .findViewById(R.id.wifi_app_description);
            holder.mDownloadButton = (Button) convertView
                    .findViewById(R.id.wifi_app_download_btn);
            holder.mDownloadCount = (TextView) convertView
                    .findViewById(R.id.wifi_app_download_count);
            holder.mIcon = (ImageView) convertView
                    .findViewById(R.id.wifi_application_icon);
            holder.mProgressBar = (ProgressBar) convertView
                    .findViewById(R.id.wifi_app_progressbar);
            holder.mProgressValue = (TextView) convertView
                    .findViewById(R.id.wifi_app_progess_value);
            holder.mSize = (TextView) convertView
                    .findViewById(R.id.wifi_application_size);
            holder.mRanking = (ImageView) convertView
                    .findViewById(R.id.wifi_app_ranking);
            holder.mTitle = (TextView) convertView
                    .findViewById(R.id.wifi_application_title);
            holder.mHideContainer = convertView
                    .findViewById(R.id.wifi_app_hideinfo_container);
            holder.mVersionView = (TextView) convertView
                    .findViewById(R.id.wifi_app_update_time);
            holder.mUpdateView = (TextView) convertView
                    .findViewById(R.id.wifi_app_version);
            holder.mProgressContainer = convertView
                    .findViewById(R.id.wifi_app_progress_container);
            holder.mArrow = (ImageView) convertView
                    .findViewById(R.id.wifi_app_arrow);
            holder.mInstallButton = (Button) convertView
                    .findViewById(R.id.wifi_app_install_btn);
            holder.mButtonContainer = convertView
                    .findViewById(R.id.wifi_app_button_container);
            holder.mDeleteButton = (Button) convertView
                    .findViewById(R.id.wifi_app_delete_file);
            holder.mRedownloadButton = (Button) convertView
                    .findViewById(R.id.wifi_app_redownload);
            return holder;
        }

        private void setButtonsClickListener(final View convertView,
                final AppItemViewHolder holder, final int position) {
            setHideAndShowItemClickListener(convertView, holder, position);
            setDeleteFileButtonClickListener(holder, position);
            setRedownloadButtonClickListener(convertView, holder, position);

        }

        private void setHideAndShowItemClickListener(View convertView,
                final AppItemViewHolder holder, final int position) {
            View container = convertView
                    .findViewById(R.id.wifi_app_real_click_container);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.mHideContainer.getVisibility() == View.GONE) {
                        holder.mHideContainer.setVisibility(View.VISIBLE);
                        holder.mArrow.setImageResource(R.drawable.arrow_up);
                        Handler handler = new Handler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                View view = getListView().getChildAt(position);
                                if (getListView() != null && view != null) {
                                    getListView()
                                            .requestChildRectangleOnScreen(
                                                    view,
                                                    new Rect(0, 0, view
                                                            .getWidth(), view
                                                            .getHeight()),
                                                    false);
                                }
                            }
                        });
                    } else {
                        holder.mArrow
                                .setImageResource(R.drawable.arrow_downlad);
                        holder.mHideContainer.setVisibility(View.GONE);
                    }
                }
            });
        }

        private void setDeleteFileButtonClickListener(
                final AppItemViewHolder holder, final int position) {
            holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppItem item = mAppList.get(position);
                    item.deleteApkFile();
                    item.updateStatus();
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        private void setRedownloadButtonClickListener(final View convertView,
                AppItemViewHolder holder, final int position) {
            holder.mRedownloadButton
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppItem item = mAppList.get(position);
                            DownloadHandler handler = new DownloadHandler(
                                    WifiChestActivity.this, item, convertView);
                            MobclickAgent.onEvent(WifiChestActivity.this,
                                    UMengUtils.EVENT_DOWNLOAD_APP,
                                    item.getTitle());
                            handler.startDownload();
                        }
                    });
        }

        private void updateView(AppItemViewHolder holder, AppItem appItem) {
            updateBaseInfo(holder, appItem);
            updateLogoIcon(holder, appItem);
            updateDownloadButtonStatus(holder, appItem);
            updateDownloadProgressStatus(holder, appItem);
            updateHideInfoStatus(holder, appItem);
            updateDownloadProgressBarStatus(holder, appItem);
        }

        private void updateBaseInfo(AppItemViewHolder holder, AppItem appItem) {
            holder.mDescription.setText(appItem.getDescription());
            holder.mTitle.setText(appItem.getTitle());
            holder.mRanking.setImageResource(getRatingResource(appItem
                    .getRanking()));
            holder.mSize.setText(String.format("%.2fMB",
                    (float) appItem.getSize() / (1024 * 1024)));

            holder.mVersionView.setText(getString(
                    R.string.wifi_chest_app_version, appItem.getVersion()));
            holder.mUpdateView.setText(getString(
                    R.string.wifi_chest_app_update_time,
                    appItem.getUpdateTime()));
        }

        private void updateLogoIcon(AppItemViewHolder holder, AppItem appItem) {
            if (appItem.getAppIcon() == null) {
                holder.mIcon.setImageResource(R.drawable.ic_launcher);
            } else {
                holder.mIcon.setImageDrawable(appItem.getAppIcon());
            }
        }

        private void updateDownloadButtonStatus(AppItemViewHolder holder,
                AppItem appItem) {
            if (appItem.isApkExisted()) {
                holder.mDownloadButton.setVisibility(View.GONE);
                holder.mInstallButton.setVisibility(View.VISIBLE);
                holder.mButtonContainer.setVisibility(View.VISIBLE);
            } else {
                holder.mDownloadButton.setVisibility(View.VISIBLE);
                holder.mInstallButton.setVisibility(View.GONE);
                holder.mButtonContainer.setVisibility(View.GONE);
            }

            if (appItem.isDownloading()) {
                holder.mDownloadButton.setEnabled(false);
            } else {
                holder.mDownloadButton.setEnabled(true);
            }
        }

        private void updateDownloadProgressStatus(AppItemViewHolder holder,
                AppItem appItem) {
            int download = appItem.getDownload();
            if (download >= 10000) {
                holder.mDownloadCount.setText(getString(
                        R.string.wifi_chest_ten_thousand_times,
                        String.format("%.2f", (float) download / 10000)));
            } else {
                holder.mDownloadCount.setText(getString(
                        R.string.wifi_chest_download_time, download));
            }
        }

        private void updateHideInfoStatus(AppItemViewHolder holder,
                AppItem appItem) {
            if (appItem.isShowDescription()) {
                holder.mHideContainer.setVisibility(View.VISIBLE);
                holder.mArrow.setImageResource(R.drawable.arrow_up);
            } else {
                holder.mHideContainer.setVisibility(View.GONE);
                holder.mArrow.setImageResource(R.drawable.arrow_downlad);
            }
        }

        private void updateDownloadProgressBarStatus(AppItemViewHolder holder,
                AppItem appItem) {
            if (appItem.isDownloading()) {
                holder.mProgressContainer.setVisibility(View.VISIBLE);
                holder.mProgressBar.setProgress(appItem.getPercentage());
                holder.mProgressValue.setText(appItem.getPercentage() + "%");
            } else {
                holder.mProgressContainer.setVisibility(View.GONE);
            }
        }

    }

    class AppItemViewHolder {
        private ImageView mIcon;
        private TextView mTitle;
        private ImageView mRanking;
        private TextView mDownloadCount;
        private Button mDownloadButton;
        private Button mInstallButton;
        private Button mDeleteButton;
        private Button mRedownloadButton;
        private TextView mSize;
        private TextView mDescription;
        private ProgressBar mProgressBar;
        private TextView mProgressValue;
        private View mHideContainer;
        private TextView mUpdateView;
        private TextView mVersionView;
        private View mProgressContainer;
        private View mButtonContainer;
        private ImageView mArrow;

        private AppItemViewHolder(View sparent) {
        }
    }

    public class LoadProcossTask extends AsyncTask<Void, Void, List<AppItem>> {

        @Override
        protected List<AppItem> doInBackground(Void... params) {
            List<AppItem> list = new ArrayList<AppItem>();
            try {
                JSONObject result = HttpUtils.sendPostRequest(APP_URL);
                String oldData = Utils.getAppList(getApplicationContext());
                Logger.debug(TAG, oldData);
                if (TextUtils.equals(oldData, Constants.TOKEN_REQUEST_ERROR)
                        || !TextUtils.equals(oldData, result.toString())) {
                    Logger.debug(TAG, "Need refesh data");
                    mNeedRefreshData = true;
                }
                list = getApplistFromJSONObject(result);
                if (list.size() > 0) {
                    Utils.setAppList(getApplicationContext(), result.toString());
                }

            } catch (JSONException e) {
                Logger.debug(TAG, "Error: " + e.toString());
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<AppItem> list) {
            Logger.debug(TAG, "Real data coming");
            if (list.size() == 0) {
                mLoadingContainer.setVisibility(View.GONE);
                mRefreshContainer.setVisibility(View.VISIBLE);
            } else {
                mLoadingContainer.setVisibility(View.GONE);
                if (mNeedRefreshData && list.size() > 0) {
                    mAppList = list;
                    setAdapter();
                }
            }
            mListContainer.setVisibility(View.VISIBLE);
        }

        protected void onPreExecute() {
            mNeedRefreshData = false;
            if (mPreLoadSuccessfully) {
                mPreLoadSuccessfully = false;
            } else {
                mLoadingContainer.setVisibility(View.VISIBLE);
                mListContainer.setVisibility(View.GONE);
                mRefreshContainer.setVisibility(View.GONE);
            }
        }
    }
}
