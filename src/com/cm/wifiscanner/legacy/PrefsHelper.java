package com.cm.wifiscanner.legacy;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    private static final String PREFERENCE_NAME = "wifi_scanner";
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPrefs;
    private static PrefsHelper sInstance;

    private static final String PREFS_KEY_SERVER = "sock_server";
    private static final String PREFS_KEY_PORT = "sock_port";

    private PrefsHelper(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences(PREFERENCE_NAME, 0);
        mEditor = mPrefs.edit();
    }

    public synchronized static PrefsHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrefsHelper(context);
        }

        return sInstance;
    }

    public void saveServer(String addr) {
        mEditor.putString(PREFS_KEY_SERVER, addr);
        mEditor.commit();
    }

    public String getServer(String defServer) {
        return mPrefs.getString(PREFS_KEY_SERVER, defServer);
    }

    public void savePort(int port) {
        mEditor.putInt(PREFS_KEY_PORT, port);
        mEditor.commit();
    }

    public int getPort(int defPort) {
        return mPrefs.getInt(PREFS_KEY_PORT, defPort);
    }
}
