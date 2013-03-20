package com.cm.wifiscanner.wifi;

import com.cm.wifiscanner.R;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class PartershipActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // ImageView view = new ImageView(this);
        // view.setImageResource(R.drawable.install);
        // // getListView().addFooterView(view);
        setContentView(R.layout.partership_view);

        // this.getListView().setBackgroundResource(R.drawable.bg);
    }
}
