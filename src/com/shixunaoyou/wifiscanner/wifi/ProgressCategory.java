package com.shixunaoyou.wifiscanner.wifi;

import com.shixunaoyou.wifiscanner.R;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;

public class ProgressCategory extends PreferenceCategory {

    private boolean mProgress = false;

    public ProgressCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_progress_category);
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        View textView = view.findViewById(R.id.scanning_text);
        View progressBar = view.findViewById(R.id.scanning_progress);

        int visibility = mProgress ? View.VISIBLE : View.INVISIBLE;
        textView.setVisibility(visibility);
        progressBar.setVisibility(visibility);
    }

    public void setProgress(boolean progressOn) {
        mProgress = progressOn;
        notifyChanged();
    }
}
