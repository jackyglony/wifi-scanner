package com.cm.wifiscanner.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;

import com.cm.wifiscanner.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";
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
            if (responeCode == HttpURLConnection.HTTP_OK ||
                    responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
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
                if(s.contains(Constants.TEST_KEYWORD)) {
                    Logger.debug(TAG, "Have connected");
                    result = Constants.CONNECTED;
                }else {
                    Logger.debug(TAG, "NO 571WIFI Server");
                    result = Constants.NOT_FIND_SERVER;
                }
            } if( responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = urlc.getHeaderField("Location");
                Log.d(TAG,"Location: " + location);
                if(!TextUtils.isEmpty(location)) {
                    if(location.contains(Constants.WIFI_SERVER_KEY)) {
                        Log.d(TAG,"Find 591 Server!!!");
                        result = Constants.HAVE_LOGOUT;
                        String gateway = getGateway(location);
                        Logger.debug(TAG,"gateway: " + gateway);
                        if(gateway != null ) {
                            Utils.setGateway(context, gateway);
                        }
                    }else {
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
        return sPref.getString(Constants.GATE_CONFIGURE_KEY, Constants.DEFAULT_WIFI_SERVER);
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
}
