package com.shixunaoyou.wifiscanner.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import com.shixunaoyou.wifiscanner.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";
    private static final int MIN_RSSI = -100;
    private static final int MAX_RSSI = -50;
    @SuppressWarnings("boxing")
    private final static ArrayList<Integer> channelsFrequency = new ArrayList<Integer>(
            Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447,
                    2452, 2457, 2462, 2467, 2472, 2484));

    public static Integer getFrequencyFromChannel(int channel) {
        return channelsFrequency.get(channel);
    }

    public static int getChannelFromFrequency(int frequency) {
        return channelsFrequency.indexOf(Integer.valueOf(frequency));
    }

    public static String getUserName(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.USER_NAME_KEY, null);
    }

    public static String getPassword(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.PASSWORD_KEY, null);
    }

    public static boolean setPref(Context context, String key, String newValue) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(key, newValue);
        return editor.commit();
    }

    public static void setUserName(Context context, String name) {
        setPref(context, Constants.USER_NAME_KEY, name);
    }

    public static void setPassword(Context context, String password) {
        setPref(context, Constants.PASSWORD_KEY, password);
    }

    public static void clearAccount(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.remove(Constants.USER_NAME_KEY);
        editor.remove(Constants.PASSWORD_KEY);
        editor.commit();
    }

    public static boolean getEnableAutoLogin(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.AUTO_LOGIN_KEY, true);
    }

    public static boolean getEnableNotification(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_NOTIFICATION_KEY, true);
    }

    public static boolean getEnableShowChannel(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_SHOW_CHANNEL, false);
    }

    public static boolean getEnableShowRssi(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_SHOW_RSSI, false);
    }

    public static void setLoginStatus(Context context, int status) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putInt(Constants.LOGIN_STATUS_KEY, status);
        editor.commit();
    }

    public static boolean getIsServiceChangeStatus(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.IS_SERVICE_UPDATE, false);
    }

    public static void setIsServiceUpdate(Context context, boolean update) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putBoolean(Constants.IS_SERVICE_UPDATE, update);
        editor.commit();
    }

    public static int getLoginStatus(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getInt(Constants.LOGIN_STATUS_KEY, 1);
    }

    public static boolean check591Server(Context context) {
        Logger.debug(TAG, "Check591 Server");
        HttpURLConnection urlc = null;
        boolean result = false;

        try {
            URL url = new URL("http://" + Utils.getGateway(context));
            Logger.debug(TAG, "URL: " + url.toString());
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(6000);
            urlc.setReadTimeout(6000);
            urlc.setRequestMethod("GET");
            urlc.setDoInput(true);
            urlc.connect();

            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_OK
                    || responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                Log.d(TAG, "Find 591WiFi Server!!");
                result = true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
        return result;
    }

    public static int checkTestUrl(Context context) {
        Logger.debug(TAG, "Check baidu");
        HttpURLConnection urlc = null;
        InputStream in = null;
        int result = Constants.CANNOT_CONNECT;
        try {
            URL url = new URL("http://" + Constants.TEST_SHORT_BAIDU);
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(10000);
            urlc.setReadTimeout(10000);
            urlc.setRequestMethod("GET");
            urlc.setInstanceFollowRedirects(false);
            urlc.setDoInput(true);
            urlc.connect();
            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_OK) {
                in = urlc.getInputStream();
                String s = Utils.inputStream2String(in);
                if (s.contains(Constants.TEST_KEYWORD)) {
                    Logger.debug(TAG, "Have connected");
                    result = Constants.CONNECTED;
                } else {
                    Logger.debug(TAG, "NO 571WIFI Server");
                    result = Constants.NOT_FIND_SERVER;
                }
            }
            if (responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = urlc.getHeaderField("Location");
                Log.d(TAG, "Location: " + location);
                if (!TextUtils.isEmpty(location)) {
                    if (location.contains(Constants.WIFI_SERVER_KEY)) {
                        Log.d(TAG, "Find 591 Server!!!");
                        result = Constants.HAVE_LOGOUT;
                        String gateway = getGateway(location);
                        Logger.debug(TAG, "gateway: " + gateway);
                        if (gateway != null) {
                            Utils.setGateway(context, gateway);
                        }
                    } else {
                        result = Constants.NOT_FIND_SERVER;
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlc != null) {
                urlc.disconnect();
            }
        }
        return result;
    }

    public synchronized static String executeReq(String hostname) {
        Log.d(TAG, "doInBackground: Start");
        HttpURLConnection urlc = null;
        InputStream in = null;
        String result = null;
        try {
            URL url = new URL("http://" + Constants.TEST_URL);
            urlc = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "doInBackground: CheckPoint 0");
            urlc.setConnectTimeout(10000);
            urlc.setReadTimeout(10000);
            urlc.setRequestMethod("GET");
            urlc.setDoInput(true);
            urlc.connect();

            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_OK) {
                in = urlc.getInputStream();
                result = Utils.inputStream2String(in);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    public static boolean isHasWifiConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static long getLastLoginTime(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getLong(Constants.LAST_LOGIN_TIME, -1);
    }

    public static void setLastLoginTime(Context context, long time) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putLong(Constants.LAST_LOGIN_TIME, time);
        editor.commit();
    }

    public static long getDataTarfficOfLogin(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getLong(Constants.DATA_TRAFFIC, -1);
    }

    public static void setDataTrafficWhenLogin(Context context, long data) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putLong(Constants.DATA_TRAFFIC, data);
        editor.commit();
    }

    public static String getGateway(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.GATE_CONFIGURE_KEY,
                Constants.DEFAULT_WIFI_SERVER);
    }

    public static void setGateway(Context context, String gateway) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.GATE_CONFIGURE_KEY, gateway);
        editor.commit();
    }

    public static String getGateway(String address) {
        String gateway = null;

        int doubleslash = address.indexOf("//");
        if (doubleslash == -1) {
            doubleslash = 0;
        } else {
            doubleslash += 2;
        }
        int end = address.indexOf('/', doubleslash);
        end = end >= 0 ? end : address.length();
        gateway = address.substring(doubleslash, end);
        return gateway;
    }

    public static String getErrorMessage(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.LOGIN_ERROR_MESSAGE,
                Constants.LOGIN_ERROR_MESSAGE);
    }

    public static void setErrorMessage(Context context, String gateway) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.LOGIN_ERROR_MESSAGE, gateway);
        editor.commit();
    }

    public static String getUpdateUrl(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.UPDATE_APK_URL,
                Constants.WRONG_UPDATE_URL);
    }

    public static void setUpdateUrl(Context context, String updateUrl) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.UPDATE_APK_URL, updateUrl);
        editor.commit();
    }

    public static String getShowErrorMessage(Context context) {
        String error = Utils.getErrorMessage(context);
        if (TextUtils.equals(error, Constants.LOGIN_ERROR_MESSAGE)) {
            return context.getResources().getString(
                    R.string.wifi_login_failure_unkown_reason);
        } else {
            return context.getResources().getString(
                    R.string.wifi_login_failure_with_reason)
                    + error;
        }
    }

    public static int getFilterMode(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getInt(Constants.FILTER_MODE_KEY,
                Constants.FILTER_MODE_OPEN);
    }

    public static void setFilterMode(Context context, int mode) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putInt(Constants.FILTER_MODE_KEY, mode);
        editor.commit();
    }

    public static String getUserAddress(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.MAIL_ADDRESS, null);
    }

    public static void setUserAddress(Context context, String updateUrl) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.MAIL_ADDRESS, updateUrl);
        editor.commit();
    }

    public static int getPercentageOfdBm(int dBm) {
        int result = 0;
        if (dBm > MAX_RSSI) {
            result = 100;
        } else if (dBm < MIN_RSSI) {
            result = 0;
        } else {
            result = 2 * (dBm + 100);
        }
        return result;
    }

    public static int getResIdofStatus(int status) {
        int resId = R.string.wifi_login_unknow_status;

        switch (status) {
            case Constants.CANNOT_CONNECT:
                resId = R.string.wifi_no_connection_satus;
                break;
            case Constants.HAVE_LOGIN:
                resId = R.string.wifi_login_successfully_status;
                break;
            case Constants.HAVE_LOGOUT:
                resId = R.string.wifi_logout_successfully_status;
                break;
            case Constants.NOT_FIND_SERVER:
                resId = R.string.wifi_cannot_login_status;
                break;
            case Constants.NO_WIFI:
                resId = R.string.wifi_no_wifi_status;
                break;
            case Constants.NOT_NEED_LOGIN:
                resId = R.string.wifi_not_need_login_status;
                break;
            case Constants.LOGIN_FALLURE:
                resId = R.string.wifi_login_fail_status;
                break;
            case Constants.STATUS_UNKOWN:
                resId = R.string.wifi_login_unknow_status;
                break;
            default:
                break;
        }
        return resId;
    }

    public static String getOperatorWifiName(String ssid) {
        if (TextUtils.equals(ssid, "CMCC")) {
            return Constants.CMCC;
        } else if (TextUtils.equals(ssid, "ChinaNet")) {
            return Constants.CHINANET;
        } else if (TextUtils.equals(ssid, "ChinaUnicom")) {
            return Constants.CHINA_UNICOM;
        } else {
            return ssid;
        }
    }

    public static void installUpdateApk(Context context, String fullPath) {
        File apkfile = new File(fullPath);
        if (!apkfile.exists()) {
            return;
        }
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        context.startActivity(installIntent);
    }

    public static boolean checkAndCreatePath() {
        boolean result = false;
        File file = new File(Environment.getExternalStorageDirectory()
                + Constants.SAVE_PATH);
        if (!file.exists()) {
            result = file.mkdirs();
        }
        result = file.canWrite();
        return result;
    }
}
