package com.shixunaoyou.wifiscanner.wifichest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.HttpUtils;
import com.shixunaoyou.wifiscanner.util.Logger;
import com.shixunaoyou.wifiscanner.util.Utils;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
        ImageDownloadListener {
    private static String TAG = "WifiChestActivity";
    private View mLoadingContainer;
    private View mListContainer;
    private List<AppItem> mAppList;
    private AppItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_chest_layout);
        getViews();
        Utils.checkAndCreatePath();
        LoadProcossTask task = new LoadProcossTask();

        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private void getViews() {
        mLoadingContainer = findViewById(R.id.wifi_chest_loading_container);
        mListContainer = findViewById(R.id.wifi_chest_applist_container);
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
            holder.mDownload.setEnabled(false);
        } else {
            holder.mDownload.setEnabled(true);
        }
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
        if (id == R.id.wifi_app_download_btn) {
            AppItem item = (AppItem) v.getTag(R.id.wifi_app_download_count);
            View parentView = (View) v.getTag(R.id.wifi_app_download_btn);
            DownloadHandler handler = new DownloadHandler(this, item,
                    parentView);
            handler.startDownload();
        } else {
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
    }

    @Override
    public void onImageDowlnloadCompleted() {
        // TODO Auto-generated method stub
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
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
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);

                holder = new AppItemViewHolder(convertView);
                convertView = inflater.inflate(
                        R.layout.wifi_chest_applicaition_item, null);
                holder.mDescription = (TextView) convertView
                        .findViewById(R.id.wifi_app_description);
                holder.mDownload = (Button) convertView
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
                convertView.setTag(holder);
                convertView.setTag(R.id.wifi_app_download_count, item);
                convertView.setOnClickListener(WifiChestActivity.this);
                holder.mDownload.setOnClickListener(WifiChestActivity.this);
                holder.mDownload
                        .setTag(R.id.wifi_app_download_btn, convertView);
            } else {
                holder = (AppItemViewHolder) convertView.getTag();
            }
            convertView.setTag(R.id.wifi_app_download_count, item);
            holder.mDownload.setTag(R.id.wifi_app_download_count, item);
            updateView(holder, item);
            return convertView;
        }
    }

    class AppItemViewHolder {
        private ImageView mIcon;
        private TextView mTitle;
        private ImageView mRanking;
        private TextView mDownloadCount;
        private Button mDownload;
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
                JSONObject result = HttpUtils
                        .sendPostRequest("http://www.591wifi.com/portal/showallapp");
                JSONArray apps = (JSONArray) result.get("showallapp");
                for (int i = 0; i < apps.length(); i++) {
                    JSONObject app = apps.getJSONObject(i);
                    AppItem item = new AppItem(app, WifiChestActivity.this);
                    list.add(item);
                }
            } catch (JSONException e) {
                Logger.debug(TAG, "Error: " + e.toString());
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<AppItem> list) {
            mLoadingContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
            mAppList = list;
            mAdapter = new AppItemAdapter(WifiChestActivity.this);
            setListAdapter(mAdapter);
        }

        protected void onPreExecute() {
        }
    }

}
