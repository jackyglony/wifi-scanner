package com.shixunaoyou.wifiscanner.more;

import com.shixunaoyou.wifiscanner.R;

public class QuestionWebViewActivity extends SettingsWebViewActivity {
    private static final String QUESTIONS_PAGE = "file:///android_asset/html/qa.html";

    @Override
    protected String getUrl() {
        return QUESTIONS_PAGE;
    }

    @Override
    protected int getCustomTitleResecouse() {
        return R.string.wifi_about_questions;
    }

}
