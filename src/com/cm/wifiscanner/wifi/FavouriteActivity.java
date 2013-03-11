package com.cm.wifiscanner.wifi;

import com.cm.wifiscanner.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;

public class FavouriteActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getListView().setBackgroundResource(R.drawable.install);
    }
}
