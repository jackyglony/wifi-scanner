package com.shixunaoyou.wifiscanner.wifi;

import com.shixunaoyou.wifiscanner.BaseCustomDialog;
import com.shixunaoyou.wifiscanner.R;
import com.shixunaoyou.wifiscanner.util.Utils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class WifiFilterDialog extends BaseCustomDialog implements
        View.OnClickListener {

    private View mContentView;
    private ListView mListView;
    private FilterItemAdapter mAdapter;
    private Context mParentContext;
    private OnWifiFilerChangeListener mListener;
    private int mLastStatus;

    public WifiFilterDialog(Context context, OnWifiFilerChangeListener listener) {
        super(context);
        mParentContext = context;
        mListener = listener;
        mLastStatus = Utils.getFilterMode(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDialog();
        super.onCreate(savedInstanceState);
    }

    private void initDialog() {
        mContentView = getLayoutInflater().inflate(
                R.layout.access_point_filter_dialog, null);
        setInverseBackgroundForced(true);
        setView(mContentView);
        initListView();
        initButton();
    }

    private void initListView() {
        mListView = (ListView) mContentView.findViewById(R.id.filter_list);
        mAdapter = new FilterItemAdapter(mParentContext);
        mListView.setAdapter(mAdapter);
    }

    private void initButton() {
        Button cancel = (Button) mContentView
                .findViewById(R.id.wifi_filter_cancel);
        cancel.setText(android.R.string.cancel);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wifi_filter_cancel) {
            dismiss();
        } else {
            int mode = (Integer) v.getTag();
            if (mLastStatus != mode) {
                Utils.setFilterMode(mParentContext, mode);
                mListener.onWifiFilterChanged();
            }
            this.dismiss();
        }
    }

    class FilterItemAdapter extends BaseAdapter {
        private String[] mItems;
        private int[] mIntItems;
        private Context mContext;

        FilterItemAdapter(Context context) {
            mContext = context;
            mItems = mContext.getResources().getStringArray(
                    R.array.wifi_filter_mode_entries);
            mIntItems = mContext.getResources().getIntArray(
                    R.array.wifi_filter_mode_int_values);
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mIntItems[position];
        }

        @Override
        public long getItemId(int position) {
            return mIntItems[position];
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            LayoutInflater inflator = LayoutInflater.from(mContext);
            View itemView = inflator
                    .inflate(R.layout.custom_radio_button, null);
            TextView title = (TextView) itemView
                    .findViewById(R.id.radio_button_title);
            RadioButton radio = (RadioButton) itemView
                    .findViewById(R.id.radio_button);
            title.setText(mItems[position]);
            int currMode = Utils.getFilterMode(mContext);

            if (currMode == mIntItems[position]) {
                radio.setChecked(true);
            } else {
                radio.setChecked(false);
            }
            itemView.setTag(position);
            itemView.setOnClickListener(WifiFilterDialog.this);
            return itemView;
        }
    }

    public interface OnWifiFilerChangeListener {
        public void onWifiFilterChanged();
    }

    @Override
    protected int getDialogTitle() {
        return R.string.settings_wifi_filter_mode_title;
    }
}
