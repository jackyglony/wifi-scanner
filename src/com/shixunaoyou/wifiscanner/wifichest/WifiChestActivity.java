package com.shixunaoyou.wifiscanner.wifichest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Logger;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
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
        AdapterView.OnItemClickListener, View.OnClickListener {

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
        LoadProcossTask task = new LoadProcossTask();
        task.execute();
    }

    private void getViews() {
        mLoadingContainer = findViewById(R.id.wifi_chest_loading_container);
        mListContainer = findViewById(R.id.wifi_chest_applist_container);
    }

    public void updateView(AppItemViewHolder holder, AppItem appItem) {
        holder.mDescription.setText(appItem.getDescription());
        holder.mTitle.setText(appItem.getTitle());
        holder.mRanking.setImageResource(R.drawable.rating_bg_10);
        holder.mSize.setText(String.format("%.2fMB", (float) appItem.getSize()
                / (1024 * 1024)));
        holder.mIcon.setImageResource(R.drawable.ic_launcher);
        holder.mDownloadCount.setText(getString(
                R.string.wifi_chest_download_time, appItem.getDownload()));
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position,
            long id) {
        // TODO Auto-generated method stub
        Logger.debug(TAG, "onItemClick");
        AppItemViewHolder holder = (AppItemViewHolder) view.getTag();
        if (holder.mHideContainer.getVisibility() == View.GONE) {
            holder.mHideContainer.setVisibility(View.VISIBLE);
        } else {
            holder.mHideContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Logger.debug(TAG, "onClick");

        AppItemViewHolder holder = (AppItemViewHolder) v.getTag();
        if (holder.mHideContainer.getVisibility() == View.GONE) {
            holder.mHideContainer.setVisibility(View.VISIBLE);
        } else {
            holder.mHideContainer.setVisibility(View.GONE);
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
                holder.mPackupButton = (Button) convertView
                        .findViewById(R.id.wifi_app_packup_button);
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
                convertView.setTag(holder);
                convertView.setOnClickListener(WifiChestActivity.this);
            } else {
                holder = (AppItemViewHolder) convertView.getTag();
            }
            updateView(holder, mAppList.get(position));
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
        private Button mPackupButton;
        private ProgressBar mProgressBar;
        private TextView mProgressValue;
        private View mHideContainer;

        private AppItemViewHolder(View sparent) {
        }
    }

    public class LoadProcossTask extends AsyncTask<Void, Void, List<AppItem>> {

        @Override
        protected List<AppItem> doInBackground(Void... params) {
            String testData = "{\"iSize\":3145728,\"sFreeLicense\":\"免费|收费\",\"sLanguage\":\"英文|中文\",\"sVersion\":\"1.0.1\""
                    + ",\"dUpdateTime\":\"2013-04-09 00:00:00\""
                    + ",\"iDownloadTime\":\"10000\",\"sAppNameEN\":"
                    + "\"591WiFiServer\",\"sLogo\":\"http://ip:port/appServer/app/images/591wifi.png?v=123546123000\","
                    + "\"sAppNameZN\":\"时讯遨游客户端\",\"id\":1,\"iRanking\":\"10\",\"iDescription\":\"免费通软件\"}";
                String testData2 = "{\"iSize\":3145728,\"sFreeLicense\":\"免费|收费\",\"sLanguage\":\"英文|中文\",\"sVersion\":\"1.0.1\""
                        + ",\"dUpdateTime\":\"2013-04-09 00:00:00\""
                        + ",\"iDownloadTime\":\"10000\",\"sAppNameEN\":"
                        + "\"591WiFiServer\",\"sLogo\":\"http://ip:port/appServer/app/images/591wifi.png?v=123546123000\","
                        + "\"sAppNameZN\":\"时讯遨游客户端2\",\"id\":1,\"iRanking\":\"10\",\"iDescription\":\"免费通软件2\"}";
            JSONObject object = null, object2 = null;
            try {
                object = new JSONObject(testData);
                object2 = new JSONObject(testData2);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Logger.debug(TAG, "Error: " + e.toString());
                e.printStackTrace();
            }
            AppItem item = new AppItem(object, null);
            List<AppItem> list = new ArrayList<AppItem>();
            AppItem item2 = new AppItem(object2, null);

            list.add(item);
            list.add(item2);
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
