package com.shixunaoyou.wifiscanner.wifichest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
    private Map<Integer, View> mAllView;
    private Button mRefreshButton;
    private boolean mPreLoadSuccessfully;
    private boolean mNeedRefreshData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_chest_layout);
        getViews();
        Utils.checkAndCreatePath();
        mAllView = new HashMap<Integer, View>();
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
        mAdapter = new AppItemAdapter(WifiChestActivity.this);
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
        // Handler handler = new Handler();
        // handler.post(new Runnable() {
        // @Override
        // public void run() {
        // if (getListView() != null && view != null) {
        // getListView().requestChildRectangleOnScreen(
        // view,
        // new Rect(0, 0, view.getWidth(), view
        // .getHeight()), false);
        // }
        // }
        // });
        // } else {
        // holder.mHideContainer.setVisibility(View.GONE);
        // }
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
        } else {
            onShowOrHideDescription(v);
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

    private void onShowOrHideDescription(View v) {
        AppItemViewHolder holder = (AppItemViewHolder) v.getTag();
        AppItem item = (AppItem) v.getTag(R.id.wifi_app_download_count);
        if (holder.mHideContainer.getVisibility() == View.GONE) {
            holder.mHideContainer.setVisibility(View.VISIBLE);
            holder.mArrow.setImageResource(R.drawable.arrow_up);
            item.setShowingDescription(true);
        } else {
            holder.mArrow.setImageResource(R.drawable.arrow_downlad);
            holder.mHideContainer.setVisibility(View.GONE);
            item.setShowingDescription(false);
        }
    }

    @Override
    public void onImageDowlnloadCompleted() {
        // TODO Auto-generated method stub
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void updateView(AppItemViewHolder holder, AppItem appItem) {
        holder.mDescription.setText(appItem.getDescription());
        holder.mTitle.setText(appItem.getTitle());
        holder.mRanking
                .setImageResource(getRatingResource(appItem.getRanking()));
        holder.mSize.setText(String.format("%.2fMB", (float) appItem.getSize()
                / (1024 * 1024)));
        if (appItem.getAppIcon() == null) {
            holder.mIcon.setImageResource(R.drawable.ic_launcher);
        } else {
            holder.mIcon.setImageDrawable(appItem.getAppIcon());
        }
        holder.mVersionView.setText(getString(R.string.wifi_chest_app_version,
                appItem.getVersion()));
        holder.mUpdateView.setText(getString(
                R.string.wifi_chest_app_update_time, appItem.getUpdateTime()));
        int download = appItem.getDownload();
        if (download >= 10000) {
            holder.mDownloadCount.setText(getString(
                    R.string.wifi_chest_ten_thousand_times,
                    String.format("%.2f", (float) download / 10000)));
        } else {
            holder.mDownloadCount.setText(getString(
                    R.string.wifi_chest_download_time, download));
        }
        if (appItem.isShowDescription()) {
            holder.mHideContainer.setVisibility(View.VISIBLE);
            holder.mArrow.setImageResource(R.drawable.arrow_up);
        } else {
            holder.mHideContainer.setVisibility(View.GONE);
            holder.mArrow.setImageResource(R.drawable.arrow_downlad);
        }

        if (appItem.isDownloading()) {
            holder.mProgressContainer.setVisibility(View.VISIBLE);
            holder.mProgressBar.setProgress(appItem.getPercentage());
            holder.mProgressValue.setText(appItem.getPercentage() + "%");
        } else {
            holder.mProgressContainer.setVisibility(View.GONE);
        }

        if (appItem.isDownloading()) {
            holder.mDownloadButton.setEnabled(false);
        } else {
            holder.mDownloadButton.setEnabled(true);
        }

        if (appItem.isApkExisted()) {
            holder.mDownloadButton.setVisibility(View.GONE);
            holder.mInstallButton.setVisibility(View.VISIBLE);
        } else {
            holder.mDownloadButton.setVisibility(View.VISIBLE);
            holder.mInstallButton.setVisibility(View.GONE);
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

        private Context mContext;

        public AppItemAdapter(Context context) {
            mContext = context;
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

        public View getView(int position, View convertView, ViewGroup parent) {
            AppItemViewHolder holder = null;
            AppItem item = mAppList.get(position);
            View subView = null;
            if (mAllView.containsKey(position)) {
                subView = mAllView.get(position);
            }
            if (subView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);

                holder = new AppItemViewHolder(convertView);
                subView = inflater.inflate(
                        R.layout.wifi_chest_applicaition_item, null);
                holder.mDescription = (TextView) subView
                        .findViewById(R.id.wifi_app_description);
                holder.mDownloadButton = (Button) subView
                        .findViewById(R.id.wifi_app_download_btn);
                holder.mDownloadCount = (TextView) subView
                        .findViewById(R.id.wifi_app_download_count);
                holder.mIcon = (ImageView) subView
                        .findViewById(R.id.wifi_application_icon);
                holder.mProgressBar = (ProgressBar) subView
                        .findViewById(R.id.wifi_app_progressbar);
                holder.mProgressValue = (TextView) subView
                        .findViewById(R.id.wifi_app_progess_value);
                holder.mSize = (TextView) subView
                        .findViewById(R.id.wifi_application_size);
                holder.mRanking = (ImageView) subView
                        .findViewById(R.id.wifi_app_ranking);
                holder.mTitle = (TextView) subView
                        .findViewById(R.id.wifi_application_title);
                holder.mHideContainer = subView
                        .findViewById(R.id.wifi_app_hideinfo_container);
                holder.mVersionView = (TextView) subView
                        .findViewById(R.id.wifi_app_update_time);
                holder.mUpdateView = (TextView) subView
                        .findViewById(R.id.wifi_app_version);
                holder.mProgressContainer = subView
                        .findViewById(R.id.wifi_app_progress_container);
                holder.mArrow = (ImageView) subView
                        .findViewById(R.id.wifi_app_arrow);
                holder.mInstallButton = (Button) subView
                        .findViewById(R.id.wifi_app_install_btn);
                subView.setTag(holder);
                subView.setTag(R.id.wifi_app_download_count, item);
                mAllView.put(position, subView);
                subView.setOnClickListener(WifiChestActivity.this);
                holder.mDownloadButton
                        .setOnClickListener(WifiChestActivity.this);
                holder.mDownloadButton.setTag(R.id.wifi_app_download_btn,
                        subView);

                holder.mInstallButton
                        .setOnClickListener(WifiChestActivity.this);
                holder.mInstallButton.setTag(R.id.wifi_app_install_btn, item);
            } else {
                holder = (AppItemViewHolder) subView.getTag();
            }
            subView.setTag(R.id.wifi_app_download_count, item);
            holder.mDownloadButton.setTag(R.id.wifi_app_download_count, item);
            updateView(holder, item);
            return subView;
        }
    }

    class AppItemViewHolder {
        private ImageView mIcon;
        private TextView mTitle;
        private ImageView mRanking;
        private TextView mDownloadCount;
        private Button mDownloadButton;
        private Button mInstallButton;
        private TextView mSize;
        private TextView mDescription;
        private ProgressBar mProgressBar;
        private TextView mProgressValue;
        private View mHideContainer;
        private TextView mUpdateView;
        private TextView mVersionView;
        private View mProgressContainer;
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
                    Utils.setAppList(getApplicationContext(), result.toString());
                    list = getApplistFromJSONObject(result);
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
            if (!mNeedRefreshData) {
                Logger.debug(TAG, "Don't need refresh!");
                return;
            }
            if (list.size() == 0) {
                mLoadingContainer.setVisibility(View.GONE);
                mListContainer.setVisibility(View.GONE);
                mRefreshContainer.setVisibility(View.VISIBLE);
            } else {
                mLoadingContainer.setVisibility(View.GONE);
                mListContainer.setVisibility(View.VISIBLE);
                mAppList = list;
                setAdapter();
            }
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
